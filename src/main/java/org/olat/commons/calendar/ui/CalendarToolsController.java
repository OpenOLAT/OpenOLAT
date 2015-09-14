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

import org.olat.commons.calendar.CalendarManager;
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

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarToolsController extends BasicController {
	
	private Link injectFileLink, injectSynchronizedUrlLink, deleteTokenLink, deleteCalendarLink;
	
	private final CalendarPersonalConfigurationRow row;
	
	public CalendarToolsController(UserRequest ureq, WindowControl wControl, CalendarPersonalConfigurationRow row) {
		super(ureq, wControl);
		this.row = row;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("tools");
		if(row.getAccess() == 0 && !row.isImported()) {
			injectFileLink = LinkFactory.createLink("cal.import.type.file", mainVC, this);
			injectFileLink.setIconLeftCSS("o_icon o_icon_import");
			injectSynchronizedUrlLink = LinkFactory.createLink("cal.synchronize.type.url", mainVC, this);
			injectSynchronizedUrlLink.setIconLeftCSS("o_icon o_icon_calendar_sync");
		}
		
		if(StringHelper.containsNonWhitespace(row.getToken())) {
			deleteTokenLink = LinkFactory.createLink("cal.icalfeed.subscribe.remove", mainVC, this);
			deleteTokenLink.setIconLeftCSS("o_icon o_icon_delete");
		}
		if(row.isImported()) {
			deleteCalendarLink = LinkFactory.createLink("cal.delete.imported.calendar", mainVC, this);
			deleteCalendarLink.setIconLeftCSS("o_icon o_icon_remove");
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
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}
