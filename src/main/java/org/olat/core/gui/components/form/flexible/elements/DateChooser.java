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
public interface DateChooser extends TextElement{

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
	 * @return
	 */
	public boolean isDateChooserTimeEnabled();

	/**
	 * @param dateChooserTimeEnabled
	 */
	public void setDateChooserTimeEnabled(boolean dateChooserTimeEnabled);

	/**
	 * @return
	 */
	public String getDateChooserDateFormat();

	/**
	 * Set an optional date chooser format if the default format for the current
	 * locale is not good. It is recommended to use the locale dependent format
	 * when possible. (e.g. 22.02.99 for DE and 99/22/92 for EN)
	 * <br>
	 * Use a pattern that corresponds to the SimpleDateFormat patterns.
	 * 
	 * @param dateChooserDateFormat
	 */
	public void setDateChooserDateFormat(String dateChooserDateFormat);

	/**
	 * Set an optional date format if the default format for the current
	 * locale is not good. It is recommended to use the locale dependent format
	 * when possible. (e.g. 22.02.99 for DE and 99/22/92 for EN)
	 * <br>
	 * Together with setDateChooserDateFormat!! One has to set the format of 
	 * the js datechoose which is differently specified, then the date formatting
	 * string of java.
	 * <br>
	 * Use a pattern that corresponds to the SimpleDateFormat patterns.
	 * 
	 * @param customDateFormat
	 */
	public void setCustomDateFormat(String customDateFormat);

	/**
	 * @param errorKey
	 * @return
	 */
	public void setValidDateCheck(String errorKey);

	public String getExampleDateString();

}