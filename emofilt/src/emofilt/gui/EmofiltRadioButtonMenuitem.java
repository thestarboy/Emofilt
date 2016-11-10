/*
 * Created on 21.01.2005
 *
 * @author Felix Burkhardt
 */
package emofilt.gui;

import java.awt.Font;

import javax.swing.JRadioButtonMenuItem;

import emofilt.Emofilt;

/**
 * Emofilt specific GUI element.
 * 
 * @author Felix Burkhardt
 */
public class EmofiltRadioButtonMenuitem extends JRadioButtonMenuItem {

    /**
     * Constructor given a lebel.
     */
    public EmofiltRadioButtonMenuitem(String s) {
        super(s);
        setFont(new Font(Emofilt._config.getString("mainFrame.font.name"), Integer
                .parseInt(Emofilt._config.getString("mainFrame.font.style")),
                Integer.parseInt(Emofilt._config.getString("mainFrame.font.size"))));
    }

}
