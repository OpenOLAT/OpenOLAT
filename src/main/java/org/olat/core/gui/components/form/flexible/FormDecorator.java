package org.olat.core.gui.components.form.flexible;
/**
 * 
 * @author patrickb
 *
 */
public interface FormDecorator {

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean hasError(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean hasExample(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean hasLabel(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean isMandatory(String formItemName);
	
	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean isVisible(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean isEnabled(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public String getItemId(String formItemName);

	/**
	 * 
	 * @param formItemName
	 * @return
	 */
	public boolean isSpacerElement(String formItemName);
}