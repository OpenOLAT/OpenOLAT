package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

public interface FormToggle extends FormItem {

/**
 * switch to on / off depending on previous state
 */
public void toggle();

/**
 * get state of the toggle
 * @return true if toggled on
 */
public boolean isOn();

/**
 * set state to on and change the layout
 */
public void toggleOn();

/**
 * set state to off and change the layout
 */
public void toggleOff();

/**
 * set your custom css for the on-state of the toggle
 * @param toggledOnCSS
 */
public void setToggledOnCSS(String toggledOnCSS);

/**
 * set your custom css for the off-state of the toggle
 * @param toggledOffCSS
 */
public void setToggledOffCSS(String toggledOffCSS);

}