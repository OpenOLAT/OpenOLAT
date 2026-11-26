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
import org.olat.core.commons.services.color.ColorFamily;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorValidator;
import org.olat.core.commons.services.color.Swatch;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;


public class KalendarRenderWrapper {

	/**
	 * The default colours of the calendars: keys of the colour set M, see
	 * {@link #setColor(String)}.
	 */
	public static final String CALENDAR_COLOR_BLUE = "blue";
	public static final String CALENDAR_COLOR_ORANGE = "orange";
	public static final String CALENDAR_COLOR_GREEN = "green";
	/**
	 * The CSS classes of the default colour {@link #CALENDAR_COLOR_BLUE},
	 * "o_cal_swatch o_swatch_blue", see {@link ColorService#toCalendarEventCssClass(String)}
	 */
	public static final String CALENDAR_DEFAULT_CSS_CLASS = ColorService.CALENDAR_SWATCH_CSS_CLASS + " "
			+ ColorService.SWATCH_CSS_CLASS_PREFIX + CALENDAR_COLOR_BLUE;
	
	/** The class of the calendar colour on an event with its own colour, see {@link #getSecondaryCssClass()} */
	private static final String SECONDARY_CSS_CLASS = "o_cal_sec";
	/** The prefix of the secondary class of a swatch key, e.g. "o_cal_sec_blue" */
	private static final String SECONDARY_SWATCH_CSS_CLASS_PREFIX = "o_cal_sec_";
	
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
	private String color;
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
			color = CALENDAR_COLOR_BLUE;
			cssClass = CALENDAR_DEFAULT_CSS_CLASS;
		} else {
			visible = config.isVisible();
			inAggregatedFeed = config.isInAggregatedFeed();
			token = config.getToken();
			// the column holds a key of the chooser sets (e.g. "blue") or an old class (e.g. "o_cal_navy")
			if(StringHelper.containsNonWhitespace(config.getCssClass())) {
				setColor(config.getCssClass());
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

	/**
	 * Sets the CSS classes of the calendar. The classes are the colour of the
	 * calendar too, see {@link #getColor()}. A colour of the colour set M is
	 * set with {@link #setColor(String)}.
	 * 
	 * @param cssClass The classes, e.g. "o_rm_cal_pastel o_color_border o_swatch_blue"
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		this.color = cssClass;
	}

	/**
	 * @return The colour of the calendar as the calendar configuration stores
	 * 		it: a key of the chooser sets (e.g. "blue", "theme2") or CSS classes
	 * 		(e.g. "o_cal_navy"), see {@link #setColor(String)}
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Sets the colour of the calendar and its CSS classes. The value is stored
	 * in the calendar configuration and rendered in HTML and JSON: a key of
	 * the chooser sets (e.g. "blue") renders with the swatch classes ("o_cal_swatch
	 * o_swatch_blue"), a calendar colour key (e.g. "navy") with its class
	 * ("o_cal_navy"), every other value as CSS classes (e.g. the old class
	 * "o_cal_navy"). An empty or invalid value changes nothing, an invalid value is logged.
	 * 
	 * @param color The colour, e.g. "blue", "o_cal_navy"
	 */
	public void setColor(String color) {
		if(!StringHelper.containsNonWhitespace(color)) {
			return;
		}
		String value = color.trim();
		String colorCssClass = CoreSpringFactory.getImpl(ColorService.class).toCalendarEventCssClass(value);
		if(colorCssClass == null) {
			colorCssClass = ColorValidator.validCssClasses(value, "calendar configuration");
		}
		if(colorCssClass != null) {
			// the stored value stays byte-identical, e.g. when the configuration is saved again
			this.color = color;
			this.cssClass = colorCssClass;
		}
	}

	/**
	 * The calendar colour on an event with its own colour: the event has the
	 * classes of its colour and these classes, the calendar colour shows as
	 * border.
	 * 
	 * @return The classes, e.g. "o_cal_sec o_cal_sec_blue" for the key "blue",
	 * 		"o_cal_sec o_cal_navy_sec" for the class "o_cal_navy", null if the
	 * 		calendar has no calendar colour
	 */
	public String getSecondaryCssClass() {
		if(!StringHelper.containsNonWhitespace(cssClass)) {
			return null;
		}
		if(color != null && Swatch.fromKey(color.trim()) != null) {
			Swatch swatch = CoreSpringFactory.getImpl(ColorService.class).resolve(color, ColorFamily.SWATCH);
			return swatch == null ? null : SECONDARY_CSS_CLASS + " " + SECONDARY_SWATCH_CSS_CLASS_PREFIX + swatch.key();
		}
		if(cssClass.startsWith(ColorService.CALENDAR_CSS_CLASS_PREFIX)) {
			return SECONDARY_CSS_CLASS + " " + cssClass + "_sec";
		}
		return null;
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

	public LinkProviderCreator getLinkProviderCreator() {
		return linkProviderCreator;
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
