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

package org.olat.portal.calendar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.olat.commons.calendar.CalendarModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.util.Util;

/**
 * 
 * Description:
 * Displays a little calendar with links to the users personal calendar
 * 
 * @author gnaegi 
 * Initial Date:  Jul 26, 2006
 * 
 */
public class CalendarPortlet extends AbstractPortlet {
	private Controller runCtr;
	private Locale locale;
	
	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		CalendarPortlet p = new CalendarPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(CalendarPortlet.class, ureq.getLocale()));
		p.setLocale(ureq.getLocale());
		return p;
	}
	
	

	@Override
	public boolean isEnabled() {
		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		return calendarModule.isEnabled() && super.isEnabled();
	}



	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		Date date = new Date();
		String today = DateFormat.getDateInstance(DateFormat.MEDIUM, this.locale).format(date);
		return getTranslator().translate("calendar.title") + ": " + today;
	}
	
	/**
	 * @param locale
	 */
	private void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		return getTranslator().translate("calendar.description");
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr = new CalendarPortletRunController(ureq, wControl);
		return this.runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		return "o_day_" + day + " o_month_" + month + " o_portlet_cal";
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (this.runCtr != null) {
			this.runCtr.dispose();
			this.runCtr = null;
		}
	}

}
