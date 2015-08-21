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

package org.olat.commons.calendar.ui.components;

import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.LinkProvider;


public class KalendarRenderWrapper {

	/**
	 * These CSS classes must be defined in the calendar.css file.
	 */
	public static final String CALENDAR_COLOR_BLUE = "o_cal_blue";
	public static final String CALENDAR_COLOR_ORANGE = "o_cal_orange";
	public static final String CALENDAR_COLOR_GREEN = "o_cal_green";
	public static final String CALENDAR_COLOR_YELLOW = "o_cal_yellow";
	public static final String CALENDAR_COLOR_RED = "o_cal_red";
	
	/**
	 * These are the access restrictions on this calendar.
	 */
	public static final int ACCESS_READ_WRITE = 0;
	public static final int ACCESS_READ_ONLY = 1;
	
	
	private Kalendar kalendar;
	private int access = ACCESS_READ_ONLY;
	private boolean imported = false;
	private boolean subscribed = false;
	private KalendarConfig kalendarConfig = new KalendarConfig();
	private LinkProvider linkProvider;
	
	/**
	 * Configure a calendar for rendering. Set default values
	 * for calendar color (BLUE) and access (READ_ONLY).
	 * 
	 * @param kalendar
	 * @param calendarColor
	 * @param access
	 */
	public KalendarRenderWrapper(Kalendar kalendar) {
		this.kalendar = kalendar;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public int getAccess() {
		return access;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}
	
	public boolean isImported() {
		return imported;
	}
	
	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

	public Kalendar getKalendar() {
		return kalendar;
	}
	
	public Kalendar reloadKalendar() {
		kalendar = CalendarManagerFactory.getInstance().getCalendarManager().getCalendar(this.getKalendar().getType(), this.getKalendar().getCalendarID());
		return kalendar;
	}

	public KalendarConfig getKalendarConfig() {
		return kalendarConfig;
	}

	public void setKalendarConfig(KalendarConfig calendarConfig) {
		this.kalendarConfig = calendarConfig;
	}

	/**
	 * @return Returns the linkProvider.
	 */
	public LinkProvider getLinkProvider() {
		return linkProvider;
	}

	/**
	 * @param linkProvider The linkProvider to set.
	 */
	public void setLinkProvider(LinkProvider linkProvider) {
		this.linkProvider = linkProvider;
	}
}
