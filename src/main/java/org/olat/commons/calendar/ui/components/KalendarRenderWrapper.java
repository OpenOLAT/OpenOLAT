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

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ICalFileCalendarManager;
import org.olat.commons.calendar.model.CalendarKey;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;


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
	
	private String displayName;
	private String identifier;
	
	private Kalendar kalendar;
	private LinkProviderCreator linkProviderCreator;
	
	private int access = ACCESS_READ_ONLY;
	private boolean imported = false;
	private boolean subscribed = false;
	private boolean visible;
	private boolean inAggregatedFeed;
	private boolean privateEventsVisible;
	private String cssClass;
	private String token;

	/**
	 * Configure a calendar for rendering. Set default values
	 * for calendar color (BLUE) and access (READ_ONLY).
	 * 
	 * @param kalendar
	 * @param calendarColor
	 * @param access
	 */
	public KalendarRenderWrapper(Kalendar kalendar, String displayName, String identifier) {
		this(kalendar, null, displayName, identifier);
	}
	
	public KalendarRenderWrapper(Kalendar kalendar, CalendarUserConfiguration config, String displayName, String identifier) {
		this.kalendar = kalendar;
		this.displayName = displayName;
		this.identifier = identifier;
		setConfiguration(config);
	}
	
	public void setConfiguration(CalendarUserConfiguration config) {
		if(config == null) {
			visible = true;
			inAggregatedFeed = true;
			cssClass = CALENDAR_COLOR_BLUE;
		} else {
			visible = config.isVisible();
			inAggregatedFeed = config.isInAggregatedFeed();
			token = config.getToken();
			if(StringHelper.containsNonWhitespace(config.getCssClass())) {
				cssClass = config.getCssClass();
			}
		}
	}
	
	public CalendarKey getCalendarKey() {
		return new CalendarKey(kalendar.getCalendarID(), kalendar.getType());
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public int getAccess() {
		return access;
	}
	
	public void setAccess(int access) {
		this.access = access;
	}
	
	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public boolean isPrivateEventsVisible() {
		return privateEventsVisible;
	}

	public void setPrivateEventsVisible(boolean privateEventsVisible) {
		this.privateEventsVisible = privateEventsVisible;
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
		kalendar = CoreSpringFactory.getImpl(CalendarManager.class).getCalendar(this.getKalendar().getType(), this.getKalendar().getCalendarID());
		return kalendar;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isInAggregatedFeed() {
		return inAggregatedFeed;
	}

	public void setInAggregatedFeed(boolean inAggregatedFeed) {
		this.inAggregatedFeed = inAggregatedFeed;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	/**
	 * @return Returns the link provider.
	 */
	public LinkProvider createLinkProvider(UserRequest ureq, WindowControl wControl) {
		if(linkProviderCreator != null) {
			return linkProviderCreator.createController(ureq, wControl);
		}
		return null;
	}

	/**
	 * @param linkProvider The link provider factory.
	 */
	public void setLinkProviderCreator(LinkProviderCreator linkProviderCreator) {
		this.linkProviderCreator = linkProviderCreator;
	}
	
	public String getFeedUrl(Identity identity) {
		if(token == null) {
			return null;
		}
		
		String calendarType = kalendar.getType();
		String calendarId = kalendar.getCalendarID();
		if (calendarType.equals(ICalFileCalendarManager.TYPE_USER)) {
			if(isImported()) {
				return Settings.getServerContextPathURI() + "/ical" + "/" + calendarType + "/" + identity.getName() + "/" + token + "/" + calendarId + ".ics";
			} else {
				return Settings.getServerContextPathURI() + "/ical" + "/" + calendarType + "/" + identity.getName() + "/" + token + ".ics";
			}
		} else {
			return Settings.getServerContextPathURI() + "/ical" + "/" + calendarType + "/" + identity.getName() + "/" + token + "/" + calendarId + ".ics";
		}
	}

	@Override
	public int hashCode() {
		return kalendar.getCalendarID().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof KalendarRenderWrapper) {
			KalendarRenderWrapper wrapper = (KalendarRenderWrapper)obj;
			return wrapper.getCalendarKey().equals(getCalendarKey());
			
		}
		return false;
	}
	
	public interface LinkProviderCreator {
		
		public LinkProvider createController(UserRequest lureq, WindowControl lwControl);
		
	}
}
