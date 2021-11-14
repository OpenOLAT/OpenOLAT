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

package org.olat.course.nodes.cal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <h3>Description:</h3>
 * A PeekViewController which show the next three events of the calendar. Next to
 * the current date if the course node is setup to show the actual date or next to
 * the date defined in the course node.
 * <p>
 * Initial Date: 9 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CourseCalendarPeekViewController extends BasicController {
	private TableController tableController;
	
	@Autowired
	private CalendarManager calendarManager;

	public CourseCalendarPeekViewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CalCourseNode courseNode, CalSecurityCallback secCallback) {
		super(ureq, wControl);

		init(ureq, courseNode, userCourseEnv, secCallback);

		putInitialPanel(tableController.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	//
	}

	private void init(UserRequest ureq, CalCourseNode courseNode, UserCourseEnvironment courseEnv, CalSecurityCallback secCallback) {
		CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, getWindowControl(), courseEnv, secCallback);

		Date refDate;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		if (CalEditController.getAutoDate(config)) {
			refDate = new Date();
		} else {
			refDate = CalEditController.getStartDate(config);
			if (refDate == null) refDate = new Date();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(refDate);
		cal.add(Calendar.YEAR, 1);

		List<KalendarEvent> nextEvents = new ArrayList<>();
		for (KalendarRenderWrapper calendar : myCal.getCalendars()) {
			Collection<KalendarEvent> events = calendarManager
					.getEvents(calendar.getKalendar(), refDate, cal.getTime(), calendar.isPrivateEventsVisible());
			for (KalendarEvent event : events) {
				if (refDate.compareTo(event.getBegin()) <= 0) {
					nextEvents.add(event);
				}
			}
		}
		Collections.sort(nextEvents, new KalendarEventComparator());
		List<KalendarEvent> nextThreeEvents = nextEvents.subList(0, Math.min(3, nextEvents.size()));

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("calendar.noEvents"), null, "o_cal_icon");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableController);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to
		// false
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("calendar.date", 0, null, ureq.getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("calendar.subject", 1, null, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT));
		tableController.setTableDataModel(new CourseCalendarPeekViewModel(nextThreeEvents, getTranslator()));
	}

	public class KalendarEventComparator implements Comparator<KalendarEvent> {
		@Override
		public int compare(KalendarEvent o1, KalendarEvent o2) {
			Date b1 = o1.getBegin();
			Date b2 = o2.getBegin();
			if (b1 == null) return -1;
			if (b2 == null) return 1;
			return b1.compareTo(b2);
		}
	}
}
