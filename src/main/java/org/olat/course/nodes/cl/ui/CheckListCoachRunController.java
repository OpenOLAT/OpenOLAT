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
package org.olat.course.nodes.cl.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.assessment.ui.tool.AssessmentEventToState;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CheckListCoachRunController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_OVERVIEW = "Overview";
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	private static final String ORES_TYPE_PREVIEW = "Preview";
	
	private Link overviewLink;
	private final Link participantsLink;
	private final Link previewLink;
	private Link remindersLink;
	
	private final VelocityContainer mainVC;
	private final CourseNodeSegmentPrefs segmentPrefs;
	private final SegmentViewComponent segmentView;
	
	private CheckListRunController previewCtrl;
	private CourseNodeReminderRunController remindersCtrl;
	private CheckListAssessmentController participantsCtrl;

	private final AssessmentCourseNodeOverviewController overviewCtrl;
	private final UserCourseEnvironment userCourseEnv;
	private final OLATResourceable ores;
	private final CheckListCourseNode courseNode;
	private final AssessmentEventToState assessmentEventToState;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public CheckListCoachRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			OLATResourceable ores, CheckListCourseNode courseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.ores = ores;
		this.courseNode = courseNode;
		
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, userCourseEnv);
		
		mainVC = createVelocityContainer("segments");
		segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		// Overview
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_OVERVIEW), null);
		overviewCtrl = courseAssessmentService.getCourseNodeOverviewController(ureq, swControl, courseNode, userCourseEnv, false, false, false);
		listenTo(overviewCtrl);
		assessmentEventToState = new AssessmentEventToState(overviewCtrl);
		
		overviewLink = LinkFactory.createLink("segment.overview", mainVC, this);
		segmentView.addSegment(overviewLink, true);
		
		// Participants
		participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
		segmentView.addSegment(participantsLink, true);
		
		// Preview
		previewLink = LinkFactory.createLink("segment.preview", mainVC, this);
		segmentView.addSegment(previewLink, false);
		
		// Reminders
		if (userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, courseEntry, courseNode.getReminderProvider(courseEntry, false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("segment.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}
		
		doOpenPreferredSegment(ureq);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_OVERVIEW.equalsIgnoreCase(type)) {
			doOpenOverview(ureq, true);
		} else if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			doOpenParticipants(ureq, true);
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type)) {
			doOpenReminders(ureq, true);
		} else if(ORES_TYPE_PREVIEW.equalsIgnoreCase(type)) {
			doOpenPreview(ureq, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (assessmentEventToState.handlesEvent(source, event)) {
			doOpenParticipants(ureq, true).activate(ureq, null, assessmentEventToState.getState(event));
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == overviewLink) {
					doOpenOverview(ureq, true);
				} else if (clickedLink == participantsLink) {
					doOpenParticipants(ureq, true);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq, true);
				} else if (clickedLink == previewLink) {
					doOpenPreview(ureq, true);
				}
			}
		}
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.overview == segment && overviewLink != null) {
			doOpenOverview(ureq, false);
		} else if (CourseNodeSegment.participants == segment && participantsLink != null) {
			doOpenParticipants(ureq, false);
		} else if (CourseNodeSegment.preview == segment && previewLink != null) {
			doOpenPreview(ureq, false);
		} else if (CourseNodeSegment.reminders == segment && remindersLink != null) {
			doOpenReminders(ureq, false);
		} else {
			doOpenOverview(ureq, false);
		}
	}

	private void doOpenOverview(UserRequest ureq, boolean saveSegmentPref) {
		overviewCtrl.reload();
		mainVC.put("segmentCmp", overviewCtrl.getInitialComponent());
		segmentView.select(overviewLink);
		segmentPrefs.setSegment(ureq, CourseNodeSegment.overview, segmentView, saveSegmentPref);
	}

	private CheckListAssessmentController doOpenParticipants(UserRequest ureq, boolean saveSegmentPref) {
		removeAsListenerAndDispose(participantsCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
		participantsCtrl = new CheckListAssessmentController(ureq, swControl, userCourseEnv, assessmentCallback, ores, courseNode);
		listenTo(participantsCtrl);
		mainVC.put("segmentCmp", participantsCtrl.getInitialComponent());
		segmentView.select(participantsLink);
		segmentPrefs.setSegment(ureq, CourseNodeSegment.participants, segmentView, saveSegmentPref);
		return participantsCtrl;
	}
	
	private void doOpenPreview(UserRequest ureq, boolean saveSegmentPref) {
		removeAsListenerAndDispose(previewCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PREVIEW), null);
		previewCtrl = new CheckListRunController(ureq, swControl, userCourseEnv, ores, courseNode, true);
		listenTo(previewCtrl);
		mainVC.put("segmentCmp", previewCtrl.getInitialComponent());
		segmentView.select(previewLink);
		segmentPrefs.setSegment(ureq, CourseNodeSegment.preview, segmentView, saveSegmentPref);
	}
	
	private void doOpenReminders(UserRequest ureq, boolean saveSegmentPref) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
			segmentView.select(remindersLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.reminders, segmentView, saveSegmentPref);
		}
	}
}
