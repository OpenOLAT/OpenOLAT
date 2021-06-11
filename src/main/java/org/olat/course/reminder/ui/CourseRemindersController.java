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
package org.olat.course.reminder.ui;

import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.reminder.rule.PassedRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseRemindersController extends BasicController implements Activateable2 {
	
	private final static CourseNodeReminderProvider COURSE_REMINDER_PROVIDER = new CourseProvider();

	private final Link remindersLink, logsLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel toolbarPanel;
	
	private CourseReminderListController reminderListCtrl;
	private CourseReminderLogsController reminderLogsCtrl;
	
	private final RepositoryEntry repositoryEntry;
	
	public CourseRemindersController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry repositoryEntry, TooledStackedPanel toolbarPanel) {
		super(ureq, wControl);
		this.toolbarPanel = toolbarPanel;
		this.repositoryEntry = repositoryEntry;
		
		mainVC = createVelocityContainer("reminders");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		remindersLink = LinkFactory.createLink("reminders", mainVC, this);
		remindersLink.setElementCssClass("o_sel_course_reminder_list_segment");
		segmentView.addSegment(remindersLink, true);
		logsLink = LinkFactory.createLink("logs", mainVC, this);
		logsLink.setElementCssClass("o_sel_course_reminder_log_segment");
		segmentView.addSegment(logsLink, false);
		
		doOpenRemindersConfiguration(ureq);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String entryPoint = entry.getOLATResourceable().getResourceableTypeName();
		if("Reminders".equalsIgnoreCase(entryPoint)) {
			doOpenRemindersConfiguration(ureq);
			segmentView.select(remindersLink);
		} else if("RemindersLogs".equalsIgnoreCase(entryPoint)) {
			doOpenReminderLogs(ureq);
			segmentView.select(logsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == remindersLink) {
					doOpenRemindersConfiguration(ureq);
				} else if (clickedLink == logsLink) {
					doOpenReminderLogs(ureq);
				}
			}
		}
	}
	
	private void doOpenRemindersConfiguration(UserRequest ureq) {
		if(reminderListCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Reminders", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			reminderListCtrl = new CourseReminderListController(ureq, bwControl, toolbarPanel, repositoryEntry,
					COURSE_REMINDER_PROVIDER, "reminders.intro");
			listenTo(reminderListCtrl);
		}
		mainVC.put("segmentCmp", reminderListCtrl.getInitialComponent());
		addToHistory(ureq, reminderListCtrl);
	}
	
	private void doOpenReminderLogs(UserRequest ureq) {
		if(reminderLogsCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("RemindersLogs", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			reminderLogsCtrl = new CourseReminderLogsController(ureq, bwControl, repositoryEntry, COURSE_REMINDER_PROVIDER);
			listenTo(reminderLogsCtrl);
		} else {
			reminderLogsCtrl.updateModel();
		}
		mainVC.put("segmentCmp", reminderLogsCtrl.getInitialComponent());	
		addToHistory(ureq, reminderLogsCtrl);
	}
	
	private static final class CourseProvider implements CourseNodeReminderProvider {
		
		@Override
		public String getCourseNodeIdent() {
			return null;
		}
		
		@Override
		public boolean filter(Collection<String> nodeIdents) {
			return true;
		}
		
		@Override
		public Collection<String> getMainRuleSPITypes() {
			return null;
		}

		@Override
		public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
			if (availableRuleTypes.contains(PassedRuleSPI.class.getSimpleName())) {
				return DateRuleSPI.class.getSimpleName();
			}
			return null;
		}

		@Override
		public void refresh() {
			//
		}
		
	}
}
