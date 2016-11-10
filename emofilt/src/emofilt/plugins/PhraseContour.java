/*
 * Created on 21.07.2005
 *
 * @author Felix Burkhardt
 */
package emofilt.plugins;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Element;

import emofilt.Constants;
import emofilt.ElemNotFoundException;
import emofilt.Emofilt;
import emofilt.Language;
import emofilt.ModificationPlugin;
import emofilt.Syllable;
import emofilt.util.Util;
import emofilt.Utterance;
import emofilt.gui.ModificationPanel;
import emofilt.gui.RateTypePanel;

/**
 * Modification Plugin
 * 
 * @author Felix Burkhardt
 */
public class PhraseContour implements ModificationPlugin {
    private final String rateDesignator = "rate";

    private final String contourDesignator = "type";

    private String name = "";

    private String type = "";

    private final String initFileName = "init/phraseContour_init.txt";

    private int defaultRate = 0;

    private HashMap initValues = null;

    private Logger debugLogger = null;

    private RateTypePanel mp = null;

    private boolean useGui = false;

    PropertyChangeListener pcl = null;

    public String getModificationType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ModificationPanel getPanel() {
        return mp;
    }

    public void init(Logger logger, boolean useGui) {
        this.useGui = useGui;
        debugLogger = logger;
        debugLogger.debug(name + " initialisation, use GUI=" + useGui);
        try {
            initValues = Util.getValuesFromBufferedReader(new BufferedReader(new InputStreamReader(Emofilt.class.getResourceAsStream(initFileName))));
            name = (String) initValues.get("name");
            type = (String) initValues.get("type");
            defaultRate = Integer.parseInt((String) initValues
                    .get("defaultRate"));
            if (useGui) {
                Vector types = new Vector();
                types.add(Constants.CONTOUR_FALL);
                types.add(Constants.CONTOUR_STRAIGHT);
                types.add(Constants.CONTOUR_RISE);
                mp = new RateTypePanel(this, initValues, types);
            }
        } catch (Exception e) {
            e.printStackTrace();
            debugLogger.error(e.getMessage());
        }
    }

    public Utterance modify(Utterance utt, Element emotion, double globalRate, Language lang) {
        int rate = 0;
        String contour = "undefined";
        try {
            rate = Integer.parseInt(Util.getValueFromEmotion(emotion, name,
                    type, rateDesignator));
            contour = Util.getValueFromEmotion(emotion, name, type,
                    contourDesignator);
        } catch (ElemNotFoundException e) {
            rate = defaultRate;
        }
        if (rate == defaultRate) {
            return utt;
        }
        int change = (int)((rate-defaultRate));
        int GlobalRateFactor  = (int)(change*globalRate);
        rate = defaultRate + change + GlobalRateFactor;
        Utterance u = (Utterance) utt.clone();
        int tmpRate;
        int sylNum = u.getSyllables().size();
        int step = (rate) / sylNum;
        tmpRate = 0;

        if (contour.compareTo(Constants.CONTOUR_RISE) == 0
                || contour.compareTo(Constants.CONTOUR_FALL) == 0) {
            if (contour.compareTo(Constants.CONTOUR_FALL) == 0) {
                step = -step;
            }
            for (int tmpSyl = 0; tmpSyl < sylNum; tmpSyl++) {
                tmpRate = 100 + (step * tmpSyl);
                Syllable syl = (Syllable) u.getSyllables().elementAt(tmpSyl);
                syl.changeMeanF0(tmpRate);
            }
        } else if (contour.compareTo(Constants.CONTOUR_STRAIGHT) == 0) {
            for (int tmpSyl = 0; tmpSyl < sylNum; tmpSyl++) {
                Syllable syl = (Syllable) u.getSyllables().elementAt(tmpSyl);
                int f0MeanDiff = (int) (u.getF0Mean() / syl.getF0Mean() * 100.0);
                syl.changeMeanF0(f0MeanDiff);
            }
        }
        return u;
    }

    public String toString() {
        return "name: " + name + ", type: " + type;
    }

    public Element setEmotion(Element emotion) {
        String newRate = mp.getRateValue();
        Element returnElem = Util.setValueInEmotion(emotion, name, type,
                rateDesignator, newRate, String.valueOf(defaultRate));
        if (newRate.compareTo(String.valueOf(defaultRate)) != 0) {
            String newType = mp.getTypeValue();
            returnElem = Util.setValueInEmotion(returnElem, name, type,
                    contourDesignator, newType, "dummy");
        }
        return returnElem;
    }

    public void setGui(Element emotion) {
        int rate = defaultRate;
        String contourType = "undefined";
        try {
            rate = Integer.parseInt(Util.getValueFromEmotion(emotion, name,
                    type, rateDesignator));
            contourType = Util.getValueFromEmotion(emotion, name, type,
                    contourDesignator);
        } catch (ElemNotFoundException e) {
            debugLogger.debug(e.getMessage());
        }
        mp.setRateValue(new Integer(rate));
        mp.setTypeValue(contourType);
    }

    public void setGuiDefault() {
        mp.setDefault();
    }

    public void setPropertyChangeListener(PropertyChangeListener pcl) {
        this.pcl = pcl;
        mp.setPropertyChangeListener(pcl);
    }
}