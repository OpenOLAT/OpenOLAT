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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.PersonalCalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLATRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description: Displays a little calendar with links to the
 *         users personal calendar
 * @author gnaegi 
 * Initial Date: Jul 26, 2006
 * 
 */
public class CalendarPortletRunController extends BasicController {

	private static final String CMD_LAUNCH = "cmd.launch";
	private static final int MAX_EVENTS = 5;

	private VelocityContainer calendarVC;
	private TableController tableController;
	private boolean dirty = false;
	private Link showAllLink;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private PersonalCalendarManager personalCalendarManager;

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param wControl
	 */
	protected CalendarPortletRunController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		calendarVC = createVelocityContainer("calendarPortlet");
		showAllLink = LinkFactory.createLink("calendar.showAll", calendarVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");

		ComponentUtil.registerForValidateEvents(calendarVC, this);
		
		Date date = new Date();
		String today = DateFormat.getTimeInstance(DateFormat.MEDIUM, ureq.getLocale()).format(date);
		calendarVC.contextPut("today", today);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("calendar.noEvents"), null, "o_icon_calendar");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		// dummy header key, won't be used since setDisplayTableHeader is set to
		// false
		tableController.addColumnDescriptor(new PortletDateColumnDescriptor("calendar.date", 0, getTranslator()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("calendar.subject", 1, CMD_LAUNCH, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT));
		
		
		List<KalendarEvent> events = getMatchingEvents(ureq, wControl);
		tableController.setTableDataModel(new EventsModel(events));
		listenTo(tableController);
		
		calendarVC.put("table", tableController.getInitialComponent());

		putInitialPanel(this.calendarVC);
	}

	private List<KalendarEvent> getMatchingEvents(UserRequest ureq, WindowControl wControl) {
		Date startDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 7);
		Date endDate = cal.getTime();
		List<KalendarEvent> events = new ArrayList<>();
		List<KalendarRenderWrapper> calendars = personalCalendarManager.getListOfCalendarWrappers(ureq, wControl);
		for (Iterator<KalendarRenderWrapper> iter = calendars.iterator(); iter.hasNext();) {
			KalendarRenderWrapper calendarWrapper = iter.next();
			boolean readOnly = (calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY) && !calendarWrapper.isImported();
			List<KalendarEvent> eventsWithinPeriod = calendarManager.getEvents(calendarWrapper.getKalendar(), startDate, endDate, true);
			for (KalendarEvent event : eventsWithinPeriod) {
				// skip non-public events
				if (readOnly && event.getClassification() != KalendarEvent.CLASS_PUBLIC) {
					continue;
				}
				events.add(event);
			}
		}
		// sort events
		Collections.sort(events, new Comparator<KalendarEvent>() {
			public int compare(KalendarEvent arg0, KalendarEvent arg1) {
				Date begin0 = arg0.getBegin();
				Date begin1 = arg1.getBegin();
				return begin0.compareTo(begin1);
			}
		});
		if (events.size() > MAX_EVENTS) events = events.subList(0, MAX_EVENTS);
		return events;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink) {
			// activate homes tab in top navigation and active calendar menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][calendar:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		} else if (event == ComponentUtil.VALIDATE_EVENT && dirty) {
			List<KalendarEvent> events = getMatchingEvents(ureq, getWindowControl());
			tableController.setTableDataModel(new EventsModel(events));
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableController) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					KalendarEvent kalendarEvent = ((EventsModel)tableController.getTableDataModel()).getObject(rowid);
					String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][calendar:0]"
							+ BusinessControlFactory.getInstance().getContextEntryStringForDate(kalendarEvent.getBegin());
					BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
					WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
					NewControllerFactory.getInstance().launch(ureq, bwControl);
					
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}

class EventsModel extends DefaultTableDataModel<KalendarEvent> {

	private static final int COLUMNS = 2;
	private int MAX_SUBJECT_LENGTH = 30;
	
	public EventsModel(List<KalendarEvent> events) {
		super(events);
	}

	@Override
	public int getColumnCount() {
		return COLUMNS;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = getObject(row);
		switch (col) {
			case 0:
				return event;
			case 1:
				String subj = event.getSubject();
				if (subj.length() > MAX_SUBJECT_LENGTH )
					subj = subj.substring(0, MAX_SUBJECT_LENGTH) + "...";
				return subj;
		}
		throw new OLATRuntimeException("Unreacheable code.", null);
	}
}
