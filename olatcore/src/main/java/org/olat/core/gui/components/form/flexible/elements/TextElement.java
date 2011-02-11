package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.util.filter.Filter;
/**
 * 
 * Description:<br>
 * TODO: patrickb Class Description for TextElement
 * 
 * <P>
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
	 * Get the value and filter it using the given filter. To use multiple
	 * filters, use the ChainedFilter instead of a single filter.
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
	 * @param errorKey
	 * @return
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
	/**
	 * @param maxLength The maximum numbe of characters allowed in this field. Set
	 *          -1 for no limit
	 */
	public void setMaxLength(int maxLength);
	
}