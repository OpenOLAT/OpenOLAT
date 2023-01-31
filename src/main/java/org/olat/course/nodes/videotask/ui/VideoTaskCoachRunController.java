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
package org.olat.course.nodes.videotask.ui;

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
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskCoachRunController extends BasicController implements Activateable2 {

	private static final String ORES_TYPE_PREVIEW = "Preview";
	private static final String ORES_TYPE_OVERVIEW = "Overview";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	
	private Link previewLink;
	private Link overviewLink;
	private Link participantsListLink;
	private Link remindersLink;
	
	private VelocityContainer mainVC;
	private CourseNodeSegmentPrefs segmentPrefs;
	private SegmentViewComponent segmentView;
	private TooledStackedPanel participantsPanel;
	
	private final VideoTaskCourseNode videoTaskNode;
	private final UserCourseEnvironment userCourseEnv;

	private VideoTaskRunController previewCtrl;
	private CourseNodeReminderRunController remindersCtrl;
	private AssessmentCourseNodeOverviewController overviewCtrl;
	private AssessmentCourseNodeController participantsListCtrl;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public VideoTaskCoachRunController(UserRequest ureq, WindowControl wControl,
			VideoTaskCourseNode videoTaskNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.videoTaskNode = videoTaskNode;
		this.userCourseEnv = userCourseEnv;

		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		mainVC = createVelocityContainer("run_segments");
		
		String mode = videoTaskNode.getModuleConfiguration().getStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
		boolean testMode = VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode);
		
		//Participants
		participantsPanel = new TooledStackedPanel("participantsPanel", getTranslator(), this);
		participantsPanel.setToolbarAutoEnabled(true);
		participantsPanel.setToolbarEnabled(false);
		participantsPanel.setShowCloseLink(true, false);
		participantsPanel.setCssClass("o_segment_toolbar o_block_top");
		
		segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		if(testMode) {
			overviewLink = LinkFactory.createLink("run.overview", mainVC, this);
			overviewLink.setElementCssClass("o_sel_course_video_overview");
			segmentView.addSegment(overviewLink, true);
		}
		
		participantsListLink = LinkFactory.createLink("run.coach.participants", mainVC, this);
		participantsListLink.setElementCssClass("o_sel_course_video_coaching");
		segmentView.addSegment(participantsListLink, !testMode);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_OVERVIEW), null);
		overviewCtrl = courseAssessmentService.getCourseNodeOverviewController(ureq, swControl, videoTaskNode, userCourseEnv, false, true, false);
		listenTo(overviewCtrl);
		
		// Preview
		previewLink = LinkFactory.createLink("segment.preview", mainVC, this);
		segmentView.addSegment(previewLink, false);
		
		if (testMode && userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, courseEntry, videoTaskNode.getReminderProvider(courseEntry, false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("run.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}
		
		doOpenPreferredSegment(ureq);
		mainVC.put("segments", segmentView);
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_OVERVIEW.equalsIgnoreCase(type)) {
			doOpenOverview(ureq, true);
		} else if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenParticipantsList(ureq, true).activate(ureq, subEntries, state);
		} else if(ORES_TYPE_PREVIEW.equalsIgnoreCase(type)) {
			doOpenPreview(ureq, true);
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type)) {
			doOpenReminders(ureq, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == overviewLink) {
					doOpenOverview(ureq, true);
				} else if (clickedLink == participantsListLink) {
					doOpenParticipantsList(ureq, true);
				} else if (clickedLink == previewLink) {
					doOpenPreview(ureq, true);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq, true);
				}
			}
		}
	}
	
	private void doOpenOverview(UserRequest ureq, boolean saveSegmentPref) {
		overviewCtrl.reload();
		addToHistory(ureq, overviewCtrl);
		mainVC.put("segmentCmp", overviewCtrl.getInitialComponent());
		segmentView.select(overviewLink);
		setPreferredSegment(ureq, CourseNodeSegment.overview, saveSegmentPref);
	}
	
	private Activateable2 doOpenParticipantsList(UserRequest ureq, boolean saveSegmentPref) {
		if(participantsListCtrl == null) {
			createParticipantsList(ureq);
		} else {
			participantsListCtrl.reload(ureq);
		}
		addToHistory(ureq, participantsListCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", participantsPanel);
		}
		segmentView.select(participantsListLink);
		setPreferredSegment(ureq, CourseNodeSegment.participants, saveSegmentPref);
		return participantsListCtrl;
	}
	
	private AssessmentCourseNodeController createParticipantsList(UserRequest ureq) {
		removeAsListenerAndDispose(participantsListCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
		participantsListCtrl = courseAssessmentService.getCourseNodeRunController(ureq, swControl, participantsPanel, 
				videoTaskNode, userCourseEnv);
		listenTo(participantsListCtrl);
		participantsListCtrl.activate(ureq, null, null);
		participantsPanel.pushController(translate("segment.participants"), participantsListCtrl);
		return participantsListCtrl;
	}
	
	private VideoTaskRunController doOpenPreview(UserRequest ureq, boolean saveSegmentPref) {
		removeAsListenerAndDispose(previewCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PREVIEW), null);
		previewCtrl = new VideoTaskRunController(ureq, swControl, videoTaskNode, userCourseEnv, true);
		listenTo(previewCtrl);
		mainVC.put("segmentCmp", previewCtrl.getInitialComponent());
		segmentView.select(previewLink);
		segmentPrefs.setSegment(ureq, CourseNodeSegment.preview, segmentView, saveSegmentPref);
		addToHistory(ureq, previewCtrl);
		return previewCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq, boolean saveSegmentPref) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			addToHistory(ureq, remindersCtrl);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
			segmentView.select(remindersLink);
			setPreferredSegment(ureq, CourseNodeSegment.reminders, saveSegmentPref);
		}
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.overview == segment && overviewLink != null) {
			doOpenOverview(ureq, false);
		} else if (CourseNodeSegment.participants == segment && participantsListLink != null) {
			doOpenParticipantsList(ureq, false);
		} else if (CourseNodeSegment.reminders == segment && remindersLink != null) {
			doOpenReminders(ureq, false);
		} else if(overviewLink != null) {
			doOpenOverview(ureq, false);
		} else {
			doOpenParticipantsList(ureq, false);
		}
	}
	
	private void setPreferredSegment(UserRequest ureq, CourseNodeSegment segment, boolean saveSegmentPref) {
		if (segmentPrefs != null) {
			segmentPrefs.setSegment(ureq, segment, segmentView, saveSegmentPref);
		}
	}
}
