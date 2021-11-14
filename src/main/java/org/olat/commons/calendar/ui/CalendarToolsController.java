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

import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarToolsController extends BasicController {
	
	private Link injectFileLink;
	private Link deleteTokenLink;
	private Link resetCalendarLink;
	private Link deleteCalendarLink;
	private Link injectSynchronizedUrlLink;
	private Link deleteImportedToCalendarLink;
	
	private final CalendarPersonalConfigurationRow row;
	
	@Autowired
	private ImportToCalendarManager importToCalendarManager;
	
	public CalendarToolsController(UserRequest ureq, WindowControl wControl, CalendarPersonalConfigurationRow row) {
		super(ureq, wControl);
		this.row = row;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("tools");
		if(row.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE && !row.isImported()) {
			injectFileLink = LinkFactory.createLink("cal.import.type.file", mainVC, this);
			injectFileLink.setIconLeftCSS("o_icon o_icon_import");
			injectSynchronizedUrlLink = LinkFactory.createLink("cal.synchronize.type.url", mainVC, this);
			injectSynchronizedUrlLink.setIconLeftCSS("o_icon o_icon_calendar_sync");
			
			List<ImportedToCalendar> importedToCalendars = importToCalendarManager
					.getImportedCalendarsIn(row.getWrapper().getKalendar());
			if(!importedToCalendars.isEmpty()) {
				deleteImportedToCalendarLink = LinkFactory.createLink("cal.delete.imported.to.calendar", mainVC, this);
				deleteImportedToCalendarLink.setIconLeftCSS("o_icon o_icon_delete_item");
			}
		}
		
		if(StringHelper.containsNonWhitespace(row.getToken())) {
			deleteTokenLink = LinkFactory.createLink("cal.icalfeed.subscribe.remove", mainVC, this);
			deleteTokenLink.setIconLeftCSS("o_icon o_icon_delete");
		}
		
		if(row.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY && row.isImported()) {
			deleteCalendarLink = LinkFactory.createLink("cal.delete.imported.calendar", mainVC, this);
			deleteCalendarLink.setIconLeftCSS("o_icon o_icon_delete_item");
		} else if(row.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE && !row.isImported()) {
			resetCalendarLink = LinkFactory.createLink("cal.reset.calendar", mainVC, this);
			resetCalendarLink.setIconLeftCSS("o_icon o_icon_delete_item");
		}
		
		putInitialPanel(mainVC);
	}
	
	public CalendarPersonalConfigurationRow getRow() {
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(injectFileLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.IMPORT_BY_FILE));
		} else if(injectSynchronizedUrlLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.IMPORT_SYNCHRONIZED_URL));
		} else if(deleteTokenLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.DELETE_TOKEN));
		} else if(deleteCalendarLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.DELETE_CALENDAR));
		} else if(resetCalendarLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.RESET_CALENDAR));
		} else if(deleteImportedToCalendarLink == source) {
			fireEvent(ureq, new CalendarGUIEvent(CalendarGUIEvent.DELETE_IMPORTED_TO));
		}
	}
}
