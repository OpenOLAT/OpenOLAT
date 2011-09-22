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
 * @author Felix Jost
 */
public class TextElement extends AbstractTextElement {

	private int size;
	private int maxLength;
	private boolean useDateChooser;
	private boolean dateChooserTimeEnabled;
	private String dateChooserDateFormat;

	/**
	 * Constructor for TextElement. use the constructor TextElement(String
	 * labelKey, String value) instead by giving the inital value
	 * 
	 * @param labelKey the lable key
	 * @param maxLength
	 */
	public TextElement(String labelKey, int maxLength) {
		this(labelKey, "", maxLength);
	}

	/**
	 * Constructor for TextElement
	 * 
	 * @param labelKey The lable key
	 * @param value The initial value
	 * @param maxLength
	 */
	public TextElement(String labelKey, String value, int maxLength) {
		this(labelKey, value, false, maxLength);
	}

	/**
	 * @param labelKey
	 * @param value
	 * @param mandatory
	 * @param maxLength
	 */
	public TextElement(String labelKey, String value, boolean mandatory, int maxLength) {
		this(labelKey, value, mandatory, 30, maxLength);
	}

	/**
	 * @param labelKey
	 * @param value
	 * @param mandatory
	 * @param size
	 * @param maxLength
	 */
	public TextElement(String labelKey, String value, boolean mandatory, int size, int maxLength) {
		setSize(size);
		setMaxLength(maxLength);
		setValue(value);
		setLabelKey(labelKey);
		setMandatory(mandatory);
	}

	/**
	 * Sets the value. if null is given, empty string is assumed. If the value ha
	 * a length longer than maxLenth a OLATRuntimeException is thrown
	 * 
	 * @param value The value to set
	 * @throws OLATRuntimeException if value.length > maxLength
	 */
	public void setValue(String value) {
		if (value != null && value.length() > this.maxLength) { throw new AssertException("Value of TextElement(name:"+getName()+")(" + value.length()
				+ ") was longer than allowed maxLength (" + this.maxLength + ")"); }
		super.setValue(value);
	}

	/**
	 * @return Returns the maxLength.
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return Returns the size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return true if date chooser should be used
	 */
	public boolean isUseDateChooser() {
		return useDateChooser;
	}

	/**
	 * @param useDateChooser true if date chooser should be used
	 */
	public void setUseDateChooser(boolean useDateChooser) {
		this.useDateChooser = useDateChooser;
	}

	/**
	 * @return
	 */
	public String getDateChooserDateFormat() {
		return dateChooserDateFormat;
	}

	/**
	 * @param dateChooserDateFormat
	 */
	public void setDateChooserDateFormat(String dateChooserDateFormat) {
		this.dateChooserDateFormat = dateChooserDateFormat;
	}

	/**
	 * @return
	 */
	public boolean isDateChooserTimeEnabled() {
		return dateChooserTimeEnabled;
	}

	/**
	 * @param dateChooserTimeEnabled
	 */
	public void setDateChooserTimeEnabled(boolean dateChooserTimeEnabled) {
		this.dateChooserTimeEnabled = dateChooserTimeEnabled;
	}
}