package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

public interface StaticTextElement extends FormItem{
	
	/**
	 * Get the value of the static text field
	 * @return
	 */
	public String getValue();
	
	/**
	 * Replace the value of the static text field
	 * @param replacementValue
	 */
	public void setValue(String replacementValue);
}