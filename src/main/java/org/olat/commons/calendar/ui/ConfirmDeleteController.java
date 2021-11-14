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

import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIDeleteEvent;
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
 * Initial date: 26 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteController extends BasicController {
	
	private final Link cancelButton;
	private Link deleteButton, deleteAllButton, deleteFutureButton, deleteOneButton;
	
	private final KalendarEvent kalendarEvent;
	
	public ConfirmDeleteController(UserRequest ureq, WindowControl wControl, KalendarEvent kalendarEvent) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarModule.class, ureq.getLocale()));
		this.kalendarEvent = kalendarEvent;
		
		VelocityContainer mainVC = createVelocityContainer("confirm_delete");
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		
		if(StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceRule())) {
			if(kalendarEvent instanceof KalendarRecurEvent) {
				KalendarRecurEvent recurEvent = (KalendarRecurEvent)kalendarEvent;
				if(recurEvent.isOriginal()) {
					deleteAllButton = LinkFactory.createButton("delete.all", mainVC, this);
					deleteAllButton.setElementCssClass("o_sel_cal_delete_all");
				} else {
					deleteFutureButton = LinkFactory.createButton("delete.future", mainVC, this);
					deleteFutureButton.setElementCssClass("o_sel_cal_delete_future_events");
				}
				deleteOneButton = LinkFactory.createButton("delete.one", mainVC, this);
				deleteOneButton.setElementCssClass("o_sel_cal_delete_one");
			}
		} else {
			deleteButton = LinkFactory.createButton("delete", mainVC, this);
		}
		
		putInitialPanel(mainVC);
	}
	
	public KalendarEvent getKalendarEvent() {
		return kalendarEvent;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(cancelButton == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(deleteButton == source || deleteAllButton == source) {
			fireEvent(ureq, new CalendarGUIDeleteEvent(CalendarGUIDeleteEvent.Cascade.all));
		} else if(deleteFutureButton == source) {
			fireEvent(ureq, new CalendarGUIDeleteEvent(CalendarGUIDeleteEvent.Cascade.future));
		} else if(deleteOneButton == source) {
			fireEvent(ureq, new CalendarGUIDeleteEvent(CalendarGUIDeleteEvent.Cascade.once));
		}
	}
}