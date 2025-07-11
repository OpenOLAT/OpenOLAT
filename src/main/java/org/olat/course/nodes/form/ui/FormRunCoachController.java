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
package org.olat.course.nodes.form.ui;

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
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormSecurityCallback;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.CourseNodeBadgesController;
import org.olat.modules.reminder.ReminderModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormRunCoachController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	private static final String ORES_TYPE_BADGES = "Badges";

	private Link participantsLink;
	private Link remindersLink;
	private Link badgesLink;

	private VelocityContainer mainVC;
	private final CourseNodeSegmentPrefs segmentPrefs;
	private SegmentViewComponent segmentView;
	
	private final TooledStackedPanel participantsPanel;
	private final FormParticipationListController participantsCtrl;
	private CourseNodeReminderRunController remindersCtrl;
	private CourseNodeBadgesController badgesCtrl;

	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public FormRunCoachController(UserRequest ureq, WindowControl wControl, FormCourseNode formCourseNode,
			UserCourseEnvironment userCourseEnv, FormSecurityCallback secCallback) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		// Participants
		participantsPanel = new TooledStackedPanel("coachStackPanel", getTranslator(), this);
		participantsPanel.setToolbarAutoEnabled(false);
		participantsPanel.setToolbarEnabled(false);
		participantsPanel.setShowCloseLink(true, false);
		participantsPanel.setCssClass("o_segment_toolbar o_block_top");
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
		participantsCtrl = new FormParticipationListController(ureq, swControl, participantsPanel, formCourseNode, userCourseEnv, secCallback);
		listenTo(participantsCtrl);
		participantsCtrl.activate(ureq, null, null);
		
		participantsPanel.pushController(translate("segment.participants"), participantsCtrl);
		
		participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
		segmentView.addSegment(participantsLink, true);
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		// Reminders
		if (reminderModule.isEnabled() && userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, courseEntry, formCourseNode.getReminderProvider(courseEntry, false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("segment.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}

		// Badges
		if (openBadgesManager.showBadgesRunSegment(courseEntry, formCourseNode, userCourseEnv)) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_BADGES), null);
			badgesCtrl = new CourseNodeBadgesController(ureq, swControl, courseEntry, formCourseNode);
			listenTo(badgesCtrl);

			badgesLink = LinkFactory.createLink("segment.badges", mainVC, this);
			segmentView.addSegment(badgesLink, false);
		}
		
		doOpenPreferredSegment(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		participantsCtrl.activate(ureq, entries, state);
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenParticipants(ureq, true).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type)) {
			doOpenReminders(ureq, true);
		} else if(ORES_TYPE_BADGES.equalsIgnoreCase(type)) {
			doOpenBadges(ureq, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == participantsLink) {
					doOpenParticipants(ureq, true);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq, true);
				} else if (clickedLink == badgesLink) {
					doOpenBadges(ureq, true);
				}
			}
		}
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.participants == segment && participantsLink != null) {
			doOpenParticipants(ureq, false);
		} else if (CourseNodeSegment.reminders == segment && remindersLink != null) {
			doOpenReminders(ureq, false);
		} else if (CourseNodeSegment.badges == segment && badgesLink != null) {
			doOpenBadges(ureq, false);
		} else {
			doOpenParticipants(ureq, false);
		}
	}
	
	private Activateable2 doOpenParticipants(UserRequest ureq, boolean saveSegmentPref) {
		participantsCtrl.reload();
		addToHistory(ureq, participantsCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", participantsPanel);
			segmentView.select(participantsLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.participants, segmentView, saveSegmentPref);
		}
		return participantsCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq, boolean saveSegmentPref) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
			segmentView.select(remindersLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.reminders, segmentView, saveSegmentPref);
		}
	}

	private void doOpenBadges(UserRequest ureq, boolean saveSegmentPref) {
		if (badgesLink != null) {
			mainVC.put("segmentCmp", badgesCtrl.getInitialComponent());
			segmentView.select(badgesLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.badges, segmentView, saveSegmentPref);
		}
	}
}
