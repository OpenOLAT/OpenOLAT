/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.commons.calendar.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIEditEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarDetailsController extends BasicController {

	private final KalendarEvent calEvent;
	private final KalendarRenderWrapper calendar;
	
	private Link editButton;
	private final VelocityContainer mainVC;
	
	private final boolean isGuestOnly;
	
	@Autowired
	private CalendarModule calendarModule;
	
	public CalendarDetailsController(UserRequest ureq, WindowControl wControl,
			KalendarEvent event, KalendarRenderWrapper calendar) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		this.calEvent = event;
		this.calendar = calendar;
		isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		mainVC = createVelocityContainer("event_details");

		if(!isGuestOnly
				&& !(calendarModule.isManagedCalendars() && CalendarManagedFlag.isManaged(event, CalendarManagedFlag.all))
				&& calendar.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
			editButton = LinkFactory.createButtonSmall("edit", mainVC, this);
			mainVC.put("edit", editButton);
		}
		addDateToMainVC();
		
		if(!calendar.isPrivateEventsVisible() && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY) {
			mainVC.contextPut("subject", "");
			mainVC.contextPut("description", "");
			mainVC.contextPut("links", new ArrayList<LinkWrapper>(1));
		} else {
			mainVC.contextPut("subject", event.getSubject());
			// format line breaks and render links as clickable links
			StringBuilder description = Formatter.stripTabsAndReturns(Formatter.formatURLsAsLinks(event.getDescription(), true));
			mainVC.contextPut("description", description.toString());
			if(StringHelper.containsNonWhitespace(event.getLocation())) {
				mainVC.contextPut("location", event.getLocation());
			}
			mainVC.contextPut("links", renderEventLinks());
		}
		putInitialPanel(mainVC);
	}
	
	private void addDateToMainVC() {
		Locale locale = getLocale();
		Date begin = calEvent.getBegin();
		Date end = calEvent.getEnd();
		
		boolean sameDay = DateUtils.isSameDay(begin, end);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			mainVC.contextPut("date", dateSb.toString());
			if (!calEvent.isAllDayEvent()) {
				StringBuilder timeSb = new StringBuilder();
				timeSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
				timeSb.append(" - ");
				timeSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
				mainVC.contextPut("time", timeSb.toString());
			}
		} else {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			if (!calEvent.isAllDayEvent()) {
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			}
			dateSb.append(" -");
			mainVC.contextPut("date", dateSb.toString());
			StringBuilder date2Sb = new StringBuilder();
			date2Sb.append(StringHelper.formatLocaleDateFull(end.getTime(), locale));
			if (!calEvent.isAllDayEvent()) {
				date2Sb.append(" ");
				date2Sb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
			}
			mainVC.contextPut("date2", date2Sb.toString());
		}
	}
	
	private List<LinkWrapper> renderEventLinks() {
		List<LinkWrapper> linkWrappers = new ArrayList<>();
		List<KalendarEventLink> kalendarEventLinks = calEvent.getKalendarEventLinks();
		if (kalendarEventLinks != null && !kalendarEventLinks.isEmpty()) {
			String rootUri = Settings.getServerContextPathURI();
			for (KalendarEventLink link: kalendarEventLinks) {
				LinkWrapper wrapper = new LinkWrapper();
				
				String uri = link.getURI();
				String iconCssClass = link.getIconCssClass();
				if(!StringHelper.containsNonWhitespace(iconCssClass)) {
					String displayName = link.getDisplayName();
					iconCssClass = CSSHelper.createFiletypeIconCssClassFor(displayName);
				}
				
				wrapper.setUri(uri);
				wrapper.setDisplayName(link.getDisplayName());
				wrapper.setTitle(StringHelper.escapeHtml(link.getDisplayName()));
				if (StringHelper.containsNonWhitespace(iconCssClass)) {
					wrapper.setCssClass(iconCssClass);
				}

				if(uri.startsWith(rootUri)) {
					//intern link with absolute URL
					wrapper.setIntern(true);
				} else if(uri.contains("://")) {
					//extern link with absolute URL
					wrapper.setIntern(false);
				} else {
					wrapper.setIntern(true);
				}
				if(wrapper.isIntern()) {
					Link ooLink = LinkFactory.createLink("link-intern-" + CodeHelper.getRAMUniqueID(), "intern.link", getTranslator(), mainVC, this, Link.NONTRANSLATED);
					ooLink.setCustomDisplayText(StringHelper.escapeHtml(link.getDisplayName()));
					ooLink.setUserObject(wrapper);
					if(StringHelper.containsNonWhitespace(wrapper.getCssClass())) {
						ooLink.setIconLeftCSS("o_icon ".concat(wrapper.getCssClass()));
					}
					wrapper.setLink(ooLink);
				}
				linkWrappers.add(wrapper);
			}
		}
		return linkWrappers;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == editButton) {
			if(!isGuestOnly) {
				fireEvent(ureq, new CalendarGUIEditEvent(calEvent, calendar));
			}
		} else if(source instanceof Link) {
			Link internalLink = (Link)source;
			if(internalLink.getUserObject() instanceof LinkWrapper) {
				fireEvent(ureq, Event.DONE_EVENT);
				LinkWrapper wrapper = (LinkWrapper)internalLink.getUserObject();
				ureq.getDispatchResult()
					.setResultingMediaResource(new RedirectMediaResource(wrapper.getUri()));
			}
		}
	}
	
	public static class LinkWrapper {
		
		private boolean intern;
		private String uri;
		private String title;
		private String cssClass;
		private String displayName;
		private Link link;
		
		public boolean isIntern() {
			return intern;
		}

		public void setIntern(boolean intern) {
			this.intern = intern;
		}

		public String getUri() {
			return uri;
		}
		
		public void setUri(String uri) {
			this.uri = uri;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getCssClass() {
			return cssClass;
		}
		
		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}

		public Link getLink() {
			return link;
		}

		public void setLink(Link link) {
			this.link = link;
		}
	}
}