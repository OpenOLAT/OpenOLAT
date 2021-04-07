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
package org.olat.course.nodes.gotomeeting;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingPeekViewController extends BasicController {
	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToMeetingPeekViewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, String subIdentifier) {
		super(ureq, wControl);
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<GoToMeeting> trainings = meetingMgr.getMeetings(GoToType.training, courseEntry, subIdentifier, null);
		Collections.sort(trainings, new GoToMeetingComparator());
		filterMyFutureTrainings(trainings);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.empty"), null, "o_cal_icon");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		TableController tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtrl);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to false
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.group", GoColumn.name.ordinal(), null, ureq.getLocale()));
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.begin", GoColumn.begin.ordinal(), null, ureq.getLocale()));
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("vc.table.end", GoColumn.end.ordinal(), null, ureq.getLocale()));
		tableCtrl.setTableDataModel(new GoToMeetingDataModel(trainings));
		tableCtrl.setSortColumn(2, false);
		putInitialPanel(tableCtrl.getInitialComponent());
	} 
	
	private void filterMyFutureTrainings(List<GoToMeeting> trainings) {
		//only the trainings in the future
		Date now = new Date();
		for(Iterator<GoToMeeting> it=trainings.iterator(); it.hasNext(); ) {
			GoToMeeting training = it.next();
			Date end = training.getEndDate();
			if(end.before(now)) {
				it.remove();
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public enum GoColumn {
		name,
		begin,
		end;
	}
	
	public static class GoToMeetingComparator implements Comparator<GoToMeeting> {
		@Override
		public int compare(GoToMeeting o1, GoToMeeting o2) {
			Date s1 = o1.getStartDate();
			Date s2 = o2.getStartDate();
			if(s2 == null) {
				return -1;
			}
			if(s1 == null) {
				return 1;
			}
			return -s1.compareTo(s2);
		}
	}
	
	public static class GoToMeetingDataModel implements TableDataModel<GoToMeeting> {
		
		private final List<GoToMeeting> meetings;
		
		public GoToMeetingDataModel(List<GoToMeeting> meetings) {
			this.meetings = meetings;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return meetings == null ? 0 :meetings.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			GoToMeeting meeting = getObject(row);
			switch(GoColumn.values()[col]) {
				case name: return meeting.getName();
				case begin: return meeting.getStartDate();
				case end: return meeting.getEndDate();
				default: return null;
			}
		}

		@Override
		public GoToMeeting getObject(int row) {
			return meetings.get(row);
		}

		@Override
		public void setObjects(List<GoToMeeting> objects) {
			//
		}

		@Override
		public Object createCopyWithEmptyList() {
			return null;
		}
	}
}
