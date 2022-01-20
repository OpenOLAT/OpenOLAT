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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Date;

/**
 * @author patrickb
 *
 */
public interface DateChooser extends TextElement {

	/**
	 * @return the date or null if the value is not valid (parsed with
	 *         DateFormat.getDateInstance(DateFormat.SHORT, locale), or with new
	 *         SimpleDateFormat(customDataFormat))
	 */
	public Date getDate();

	/**
	 * @param date
	 */
	public void setDate(Date date);
	
	/**
	 * @return The initial date. Used to compare the difference between the new and initial date.
	 */
	public Date getInitialDate();
	
	/*+
	 * Set the initial date. Used to compare the difference between the new and initial date.
	 */
	public void setInitialDate(Date initialDate);
	
	/**
	 * @return Difference in milliseconds between the initial date and newly selected date
	 */
	public long getDateDifference();
	
	public Date getSecondDate();
	
	public void setSecondDate(Date date);
	
	public boolean isTimeOnly();
	
	public void setTimeOnly(boolean enable);

	/**
	 * @return true if hour / minute fields are enabled
	 */
	public boolean isDateChooserTimeEnabled();
	
	/**
	 * Keep the time when the time fields are not displayed. 
	 * Useful if you want to shift dates.
	 * To use this, the initial date must be set!
	 * 
	 * @return
	 */
	public void setKeepTime(boolean keepTime);
	
	public boolean isKeepTime();

	/**
	 * Enable or disable the hour and minute fields.
	 * 
	 * @param dateChooserTimeEnabled
	 */
	public void setDateChooserTimeEnabled(boolean enable);
	
	/**
	 * @return true if the item handles a second date
	 */
	public boolean isSecondDate();
	
	/**
	 * @param enableSecondDate true if the item needs to handle a second date
	 */
	public void setSecondDate(boolean enableSecondDate);
	
	/**
	 * If true, only one date chooser is shown
	 * @return
	 */
	public boolean isSameDay();
	
	/**
	 * If time is enabled, and the two dates are on the same day, this option
	 * allow to only show one date chooser with two times fileds.
	 * 
	 * @param sameDay
	 */
	public void setSameDay(boolean sameDay);
	
	public void setSeparator(String i18nKey);

	/**
	 * @param errorKey
	 * @return
	 */
	public void setValidDateCheck(String errorKey);

	public String getExampleDateString();
	
	
	public DateChooser getDefaultValue();
	
	/**
	 * Set an other date chooser as default value for this
	 * chooser.
	 * 
	 * @param dateChooser A date chooser
	 */
	public void setDefaultValue(DateChooser dateChooser);
	
	/**
	 * This will set the default time to 23:59 instead of 00:00
	 * 
	 * @param endOfDay
	 */
	public void setDefaultTimeAtEndOfDay(boolean endOfDay);
	
	/**
	 * Enable or disable whether the interactive parts of the element are active.
	 * The buttons should be disabled mainly in preview scenarios.
	 * 
	 * @param enable
	 */
	public void setButtonsEnabled(boolean enable);
	
	/**
	 * Fine grain control of the action event, especially the onchange
	 * event on the hours and minutes fields.
	 * 
	 * @param action The action
	 * @param dateOnly true if the hour/minute fields doesn't send onchange events.
	 */
	public void addActionListener(int action, boolean dateOnly);

}