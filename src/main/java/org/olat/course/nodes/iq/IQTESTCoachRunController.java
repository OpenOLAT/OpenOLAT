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
package org.olat.course.nodes.iq;

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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentModeOverviewListController;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Mar 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTCoachRunController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_ASSESSMENT_MODE = "AssessmentMode";
	private static final String ORES_TYPE_PREVIEW = "Preview";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	
	private Link participantsLink;
	private Link assessmentModeLink;
	private Link previewLink;
	private Link remindersLink;
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private TooledStackedPanel participantsPanel;
	private AssessmentCourseNodeController participantsCtrl;
	private AssessmentModeOverviewListController assessmentModeCtrl;
	private Controller previewCtrl;
	private CourseNodeReminderRunController remindersCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final IQTESTCourseNode courseNode;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	
	public IQTESTCoachRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		//Participants
		participantsPanel = new TooledStackedPanel("participantsPanel", getTranslator(), this);
		participantsPanel.setToolbarAutoEnabled(false);
		participantsPanel.setToolbarEnabled(true);
		participantsPanel.setShowCloseLink(true, false);
		participantsPanel.setCssClass("o_segment_toolbar o_block_top");
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
		participantsCtrl = courseAssessmentService.getCourseNodeRunController(ureq, swControl, participantsPanel, 
				courseNode, userCourseEnv);
		listenTo(participantsCtrl);
		participantsCtrl.activate(ureq, null, null);
		participantsPanel.pushController(translate("segment.participants"), participantsCtrl);
		
		participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
		segmentView.addSegment(participantsLink, true);
		
		// Assessment tool
		swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_ASSESSMENT_MODE), null);
		assessmentModeCtrl = new AssessmentModeOverviewListController(ureq, swControl, courseEntry, participantsCtrl.getAssessmentCallback());
		listenTo(assessmentModeCtrl);
		if(assessmentModeCtrl.getNumOfAssessmentModes() > 0) {
			assessmentModeLink = LinkFactory.createLink("segment.assessment.mode", mainVC, this);
			segmentView.addSegment(assessmentModeLink, false);
		}
		
		// Preview
		previewLink = LinkFactory.createLink("segment.preview", mainVC, this);
		segmentView.addSegment(previewLink, false);
		
		// Reminders
		if (userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, courseEntry, courseNode.getReminderProvider(false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("segment.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}
		
		doOpenParticipants(ureq);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			doOpenParticipants(ureq);
			segmentView.select(participantsLink);
		} else if(ORES_TYPE_ASSESSMENT_MODE.equalsIgnoreCase(type) && assessmentModeLink != null) {
			doOpenAssessmentMode();
			segmentView.select(assessmentModeLink);
		} else if(ORES_TYPE_PREVIEW.equalsIgnoreCase(type)) {
			doOpenPreview(ureq);
			segmentView.select(previewLink);
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type)) {
			doOpenReminders(ureq);
			segmentView.select(remindersLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == participantsLink) {
					doOpenParticipants(ureq);
				} else if (clickedLink == assessmentModeLink) {
					doOpenAssessmentMode();
				} else if (clickedLink == previewLink) {
					doOpenPreview(ureq);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq);
				}
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		participantsCtrl.reload(ureq);
		addToHistory(ureq, participantsCtrl);
		mainVC.put("segmentCmp", participantsPanel);
	}
	
	private void doOpenAssessmentMode() {
		if (assessmentModeLink != null) {
			mainVC.put("segmentCmp", assessmentModeCtrl.getInitialComponent());
		}
	}
	
	private void doOpenPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PREVIEW), null);
		previewCtrl = new QTI21AssessmentRunController(ureq, swControl, userCourseEnv, courseNode);
		listenTo(previewCtrl);
		mainVC.put("segmentCmp", previewCtrl.getInitialComponent());
	}
	
	private void doOpenReminders(UserRequest ureq) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
		}
	}
	
}
