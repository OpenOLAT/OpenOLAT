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
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 27 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmUpdateController extends BasicController {

	private final Link cancelButton;
	private Link updateAllButton, updateOneButton;
	
	private final Boolean allDay;
	private final Long dayDelta, minuteDelta; 
	private final KalendarRecurEvent kalendarEvent;
	private final boolean changeBegin;

	public ConfirmUpdateController(UserRequest ureq, WindowControl wControl, KalendarRecurEvent kalendarEvent) {
		this(ureq, wControl, kalendarEvent, null, null, null, true);
	}
	
	public ConfirmUpdateController(UserRequest ureq, WindowControl wControl, KalendarRecurEvent kalendarEvent,
			Long dayDelta, Long minuteDelta, Boolean allDay, boolean changeBegin) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarModule.class, ureq.getLocale()));
		this.allDay = allDay;
		this.dayDelta = dayDelta;
		this.minuteDelta = minuteDelta;
		this.kalendarEvent = kalendarEvent;
		this.changeBegin = changeBegin;
		
		VelocityContainer mainVC = createVelocityContainer("confirm_update");
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		updateAllButton = LinkFactory.createButton("update.all", mainVC, this);
		updateAllButton.setElementCssClass("o_sel_cal_update_all");
		updateOneButton = LinkFactory.createButton("update.once", mainVC, this);
		updateOneButton.setElementCssClass("o_sel_cal_update_one");
		putInitialPanel(mainVC);
	}
	
	public Boolean getAllDay() {
		return allDay;
	}

	public Long getDayDelta() {
		return dayDelta;
	}

	public Long getMinuteDelta() {
		return minuteDelta;
	}

	public boolean getChangeBegin() {
		return changeBegin;
	}

	public KalendarRecurEvent getKalendarEvent() {
		return kalendarEvent;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(cancelButton == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(updateAllButton == source) {
			fireEvent(ureq, new CalendarGUIUpdateEvent(CalendarGUIUpdateEvent.Cascade.all));
		} else if(updateOneButton == source) {
			fireEvent(ureq, new CalendarGUIUpdateEvent(CalendarGUIUpdateEvent.Cascade.once));
		}
	}
}
