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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.formelements;

import org.olat.core.logging.AssertException;

/**
 * Initial Date:  Apr 20, 2004
 *
 * @author gnaegi
 * 
 * Comment:  
 * Base class that implements common methods used in TextElements, 
 * TextAreaElements and PasswordElements. 
 */
public abstract class AbstractTextElement extends AbstractFormElement {

  	private String original;
	private String value;
	private boolean clean = false;
	private PopupData popupData; 
	
	/**
	 * @return String
	 */
	public String getValue() {
		if (!clean && value != null) {
			/**
			 * unicode line separator can break OLAT ajax or any other js string
			 */
			value = value.replaceAll("\u2028", "");
			clean = true;
		}
		return value != null ? value : "" ;
	}

	/**
	 * Sets the value. if null is given, an empty string is assumed.
	 * @param value The value to set
	 */
	public void setValue(String value) {
		if (value == null) value="";
		else {
		    // Remember original value for dirty evaluation. 
		    // null value is not regarded as initial value. only 
		    // real values are used inital values 
		    if (original == null) original = new String(value);
		}
		/**
		 * unicode line separator can break OLAT ajax or any other js string
		 */
		this.value = value.replaceAll("\u2028", "");
		}

	/**
	 * @param errorKey
	 * @return
	 */
	public boolean notEmpty(String errorKey) {
		if (value == null || value.equals("")) {
			setErrorKey(errorKey); return false;
		}
		else {
			clearError(); return true;
		}
	}
	
	/**
	 * 
	 * @param maxLength
	 * @param errorKey
	 * @return
	 */
	public boolean notLongerThan(int maxLength, String errorKey){
		if (value.length() > maxLength){
			setErrorKey(errorKey); return false;
		}
		else {
			clearError(); return true;
		}
		
	}
	
	
	/**
	 * compares a text value with another value
	 * @param otherValue
	 * @param errorKey
	 * @return true if they are equal
	 */
	public boolean isEqual(String otherValue, String errorKey){
		if (value == null || !value.equals(otherValue)) {
			setErrorKey(errorKey); return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Check if the text element is empty
	 * @return boolean true if is empty, false otherwhise
	 */
	public boolean isEmpty() {
		return value.equals("");	
	}
	
	/**
	 * Check if the text element is empty
	 * @param errorKey
	 * @return boolean true if is empty, false otherwhise
	 */
	public boolean isEmpty(String errorKey) {
		if (isEmpty()) {
			setErrorKey(errorKey);
			return true;
		}
		return false;
	}
	
	/**
	 * @param regExp
	 * @param errorKey
	 * @return
	 */
	public boolean matches(String regExp, String errorKey) {
		if (value == null || !value.matches(regExp)) {
			setErrorKey(errorKey);
			return false;
		}
		else { 
			return true;
		}
	}
	/** (non-Javadoc)
	 * @see org.olat.core.gui.formelements.FormElement#setValues(java.lang.String[])
	 */
	public void setValues(String[] values) {
		if (isReadOnly()) throw new AssertException("setting a readonly field:"+getName());
		if (values == null) throw new AssertException("no text (not even empty) for field:"+getName());
		if (values.length != 1) throw new AssertException("got multiple values for field:"+getName());
		setValue(values[0]);
	}

	/**
	 * @see org.olat.core.gui.formelements.FormElement#isDirty()
	 */
	public boolean isDirty(){
	    if (original == null) {
	        if (value == null || value.equals("")) return false;
	        return true;
	    }
	    return (original.equals(value)) ? false : true;
	}
	
	/**
	 * @return
	 */
	public PopupData getPopupData() {
		return popupData;
	}
	
	/**
	 * if set, a button will be placed to the right of the textelement. the buttons labelKey, the command/action of the button, 
	 * the id of the textelement, and the width and height of the popup window are determined by the attributes in popupData
	 * @param popupData
	 */
	public void setPopupData(PopupData popupData) {
		this.popupData = popupData;
	}
}
