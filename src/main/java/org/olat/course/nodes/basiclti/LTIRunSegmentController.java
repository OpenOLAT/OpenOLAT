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
package org.olat.course.nodes.basiclti;

import java.util.List;

import org.olat.core.CoreSpringFactory;
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
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LTIRunSegmentController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_CONTENT = "Content";
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	
	private Link contentLink;
	private Link participantsLink;
	private Link remindersLink;
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;

	private Controller contentCtrl;
	private TooledStackedPanel participantsPanel;
	private AssessmentCourseNodeController participantsCtrl;
	private CourseNodeReminderRunController remindersCtrl;

	private final UserCourseEnvironment userCourseEnv;
	private final BasicLTICourseNode courseNode;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public LTIRunSegmentController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			BasicLTICourseNode courseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		
		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		contentLink = LinkFactory.createLink("segment.content", mainVC, this);
		segmentView.addSegment(contentLink, true);
		
		// Participants
		if (userCourseEnv.isAdmin() || userCourseEnv.isCoach()) {
			if (courseAssessmentService.getAssessmentConfig(courseNode).isEditable()) {
				participantsPanel = new TooledStackedPanel("participantsPanel", getTranslator(), this);
				participantsPanel.setToolbarAutoEnabled(false);
				participantsPanel.setToolbarEnabled(false);
				participantsPanel.setShowCloseLink(true, false);
				participantsPanel.setCssClass("o_segment_toolbar o_block_top");
				
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
				participantsCtrl = courseAssessmentService.getCourseNodeRunController(ureq, swControl, participantsPanel, 
						courseNode, userCourseEnv);
				listenTo(participantsCtrl);
				participantsCtrl.activate(ureq, null, null);
				participantsPanel.pushController(translate("segment.participants"), participantsCtrl);
				
				participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
				segmentView.addSegment(participantsLink, false);
			}
		}
		
		// Reminders
		if (userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl,
					userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
					courseNode.getReminderProvider(false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("segment.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}
		
		doOpenContent(ureq);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_CONTENT.equalsIgnoreCase(type)) {
			doOpenContent(ureq);
			segmentView.select(contentLink);
		} else if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type) && participantsLink != null) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenParticipants(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			segmentView.select(participantsLink);
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type) && remindersLink != null) {
			doOpenReminders(ureq);
			segmentView.select(remindersLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == contentLink) {
					doOpenContent(ureq);
				} else if (clickedLink == participantsLink) {
					doOpenParticipants(ureq);
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
	
	public void doOpenContent(UserRequest ureq) {
		mainVC.contextRemove("cssClass");
		removeAsListenerAndDispose(contentCtrl);
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		String ltiVersion = config.getStringValue(LTIConfigForm.CONFIGKEY_LTI_VERSION, LTIConfigForm.CONFIGKEY_LTI_11);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_CONTENT), null);
		if(LTIConfigForm.CONFIGKEY_LTI_13.equals(ltiVersion)) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			LTI13ToolDeployment deployment = CoreSpringFactory.getImpl(LTI13Service.class).getToolDeployment(courseEntry, courseNode.getIdent());
			contentCtrl = new LTIRunController(ureq, swControl, courseNode, deployment, userCourseEnv);
		} else {
			contentCtrl = new LTIRunController(ureq, swControl, courseNode, userCourseEnv);
		}
		listenTo(contentCtrl);
		mainVC.put("segmentCmp", contentCtrl.getInitialComponent());
		if (segmentView.getSegments().size() > 1) {
			mainVC.contextPut("cssClass", "o_block_top");
		}
	}
	
	private Activateable2 doOpenParticipants(UserRequest ureq) {
		mainVC.contextRemove("cssClass");
		participantsCtrl.reload(ureq);
		addToHistory(ureq, participantsCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", participantsPanel);
		}
		return participantsCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq) {
		mainVC.contextRemove("cssClass");
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
		}
	}

}
