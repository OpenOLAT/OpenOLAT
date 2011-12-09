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
* <p>
*/ 

package org.olat.core.gui.formelements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class DateElement extends TextElement {

	private Locale locale;
	private String customDateFormat = null;
	private DateFormat df;

	/**
	 * @param labelKey
	 * @param date
	 * @param locale to produce the dateformat fitting to the locale
	 */
	public DateElement(String labelKey, Date date, Locale locale) {
		super(labelKey, null, false, 11, 10); // max input size is 10
		this.locale = locale;
		setDate(date);
		setUseDateChooser(true); // uses date chooser by default
		setDateChooserTimeEnabled(false); //date without time
	}
	
	/**
	 * @param labelKey
	 * @param date
	 * @param customDateFormat e.g. "dd.MM.yyyy HH:mm", see java.text.SimpleDateFormat
	 */
	public DateElement(String labelKey, Date date, String customDateFormat) {
		super(labelKey, null, false, customDateFormat.length() + 1, customDateFormat.length()); // max input size is 10
		this.customDateFormat = customDateFormat;
		setDate(date);
		setUseDateChooser(true); // uses date chooser by default
		setDateChooserTimeEnabled(false); //date without time
	}

	/**
	 * @param labelKey
	 * @param locale
	 */
	public DateElement(String labelKey, Locale locale) {
		this(labelKey, null, locale);
	}

	/**
	 * @param errorKey
	 * @return true if valid
	 */
	public boolean validDate(String errorKey) {
		String val = getValue();
		if (val == null) return false;
		if (getDate() != null) {
			clearError();
			return true;
		} else {
			setErrorKey(errorKey);
			return false;
		}
	}

	/**
	 * @return the date or null if the value is not valid (parsed with
	 *         DateFormat.getDateInstance(DateFormat.SHORT, locale), or with new
	 *         SimpleDateFormat(customDataFormat))
	 */
	public Date getDate() {
		Date d = null;
		try {
			d = getDateFormat().parse(getValue());
		} catch (ParseException e) {
			// return null
		}
		return d;
	}

	/**
	 * @param date
	 */
	public void setDate(Date date) {
		if (date == null) {
			setValue("");
		} else {
			setValue(formatDate(date));
		}
	}

	/**
	 * @return an example date as string
	 */
	public String getExampleDate() {
		String example = getDateFormat().format(new Date());
		return example;
	}

	private String formatDate(Date date) {
		String da = getDateFormat().format(date);
		return da;
	}
	
	private DateFormat getDateFormat() {
		if (df == null) {
			if (customDateFormat == null) {
				df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
				df.setLenient(false);
			} else {
				df = new SimpleDateFormat(customDateFormat);
				df.setLenient(false);
			}
		}
		return df;
	}

}