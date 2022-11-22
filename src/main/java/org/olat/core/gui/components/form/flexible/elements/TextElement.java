/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.util.filter.Filter;
/**
 * Initial Date: 02.02.2007 <br>
 * 
 * @author patrickb
 */
public interface TextElement extends FormItem{

	/**
	 * Get the value of the text element.
	 * 
	 * @return String
	 */
	public String getValue();

	/**
	 * Get the value and filter it using the given filter.
	 * 
	 * @param filter
	 * @return
	 */
	public String getValue(Filter filter);
	
	/**
	 * Sets the value. if null is given, an empty string is assumed.
	 * 
	 * @param value
	 *            The value to set
	 */
	public void setValue(String value);

	
	/**
	 * OO-31
	 * 
	 * if you set this to true, this will prevent the default behavior of trimming
	 * the input-value on set. ( setValue() )
	 * 
	 * @param preventTrim
	 */
	public void preventValueTrim(boolean preventTrim); 
	
	
	/**
	 * Set a new value as the original value that is used when resetting the
	 * form. This can be used when a form is saved and in a later form should be
	 * resetted to the intermediate save state.
	 * <p>
	 * Does not change the value of the element, just the reset-value!
	 * 
	 * @param value The new original value
	 */
	public void setNewOriginalValue(String value);

	/**
	 * The field cannot be empty, and will be as mandatory set. Error message is the
	 * default one.
	 */
	public void setNotEmptyCheck();
	
	/**
	 * The field cannot be empty, and will be as mandatory set.
	 * 
	 * @param errorKey The i18n key for the error message
	 */
	public void setNotEmptyCheck(String errorKey);

	/**
	 * @param maxLength
	 * @param errorKey
	 * @return
	 */
	public void setNotLongerThanCheck(int maxLength, String errorKey);

	/**
	 * compares a text value with another value
	 * 
	 * @param otherValue
	 * @param errorKey
	 * @return true if they are equal
	 */
	public void setIsEqualCheck(String otherValue, String errorKey);
	
	/**
	 * to be set if TextElement should be validated with its validate() method
	 * @param itemValidatorProvider
	 */
	public void setItemValidatorProvider(ItemValidatorProvider itemValidatorProvider);
	
	/**
	 * Check if the text element is empty
	 * 
	 * @return boolean true if is empty, false otherwise
	 */
	public boolean isEmpty();

	/**
	 * Check if the text element is empty
	 * 
	 * @param errorKey
	 * @return boolean true if is empty, false otherwise
	 */
	public boolean isEmpty(String errorKey);

	/**
	 * @param regExp
	 * @param errorKey
	 * @return
	 */
	public void setRegexMatchCheck(String regExp, String errorKey);
	/**
	 * defines the desired display size of the element
	 */
	public void setDisplaySize(int displaySize);
	
	
	public int getMaxLength();
	/**
	 * @param maxLength The maximum number of characters allowed in this field. Set
	 *          -1 for no limit
	 */
	public void setMaxLength(int maxLength);
	
	/**
	 * Check the visible length of the text (can be used for datas saved in XML file
	 * but not for DB)
	 * @param checkVisibleLength
	 */
	public void setCheckVisibleLength(boolean checkVisibleLength);
	

	/**
	 * Set the placeholder to be displayed inline in the input field. Text is
	 * displayed without translator
	 * 
	 * @param placeholderText placeholder text or NULL to reset placeholder
	 */
	public void setPlaceholderText(String placeholderText);	

	/**
	 * Set the placeholder to be displayed inline in the input field. Text is
	 * translated with the current translator
	 * @param i18nKey placeholder i18n key or NULL to reset placeholder
	 * @param args the translator arguments
	 */
	public void setPlaceholderKey(String i18nKey, String[] args);

	/**
	 * @return The placehodler text, escaped and translated ready to use
	 */
	public String getPlaceholder();

	/**
	 * @return true: has a placeholder text ; false: has no placeholder
	 */
	public boolean hasPlaceholder();
	
	public String getTextAddOn();
	
	/**
	 * Place an add-on after the field for, e.g., a
	 * unit.
	 * 
	 * @param text The text to place after the field
	 */
	public void setTextAddOn(String text);

	
	public String getAriaLabel();
	
	/**
	 * Set an ARIA label directly in the input tag.
	 * 
	 * @param label The translated label
	 */
	public void setAriaLabel(String label);
	
	/**
	 * Set the autocomplete behavior of the TextElement. Default value is null
	 * (is same behavior as "on"). To avoid the automatic completion of password
	 * fields use "new-password".
	 * 
	 * @param autocomplete
	 */
	public void setAutocomplete(String autocomplete);
	
	/**
	 * 
	 * @return the autocomplete value or null if not set
	 */
	public String getAutocomplete();
	
	/**
	 * If enabled the placeholder of this element is updated to the value of the
	 * TextElement with the elementId when the user is typing (keyup) in the
	 * TextElement with the elementId. The optional maxLength truncates the
	 * placeholder after this value.
	 *
	 * @param elementId
	 * @param maxLength
	 */
	public void enablePlaceholderUpdate(String elementId, Integer maxLength);
	
	public void disablePlaceholderUpdate();

	public void setDomReplacementWrapperRequired(boolean required);
	
}