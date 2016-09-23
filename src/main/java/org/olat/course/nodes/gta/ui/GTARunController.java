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
package org.olat.course.nodes.gta.ui;

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
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTARunController extends BasicController {
	
	private GTAParticipantController runCtrl;
	private GTACoachSelectionController coachCtrl;
	private GTACoachManagementController manageCtrl;

	private Link runLink, coachLink, manageLink;
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private final GTACourseNode gtaNode;
	private final UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTARunController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.userCourseEnv = userCourseEnv;
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		Membership membership = gtaManager.getMembership(getIdentity(), entry, gtaNode);
		if(membership.isCoach() && membership.isParticipant()) {
			mainVC = createVelocityContainer("run_segments");
			
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			runLink = LinkFactory.createLink("run.run", mainVC, this);
			segmentView.addSegment(runLink, true);
			coachLink = LinkFactory.createLink("run.coach", mainVC, this);
			segmentView.addSegment(coachLink, false);
			if(isManagementTabAvalaible(config)) {
				manageLink = LinkFactory.createLink("run.manage.coach", mainVC, this);
				segmentView.addSegment(manageLink, false);
			}
			doOpenRun(ureq);
			mainVC.put("segments", segmentView);
			putInitialPanel(mainVC);
		} else if(isManagementTabAvalaible(config)) {
			mainVC = createVelocityContainer("run_segments");

			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			coachLink = LinkFactory.createLink("run.coach", mainVC, this);
			segmentView.addSegment(coachLink, true);
			manageLink = LinkFactory.createLink("run.manage.coach", mainVC, this);
			segmentView.addSegment(manageLink, false);

			doOpenCoach(ureq);
			mainVC.put("segments", segmentView);
			putInitialPanel(mainVC);
		} else if(membership.isCoach() || userCourseEnv.isAdmin()) {
			createCoach(ureq);
			putInitialPanel(coachCtrl.getInitialComponent());
		} else if(membership.isParticipant()) {
			createRun(ureq);
			putInitialPanel(runCtrl.getInitialComponent());
		} else {
			String title = translate("error.not.member.title");
			String message = translate("error.not.member.message");
			Controller msgCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
			listenTo(msgCtrl);
			putInitialPanel(msgCtrl.getInitialComponent());
		}
		if (gtaNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)){
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, gtaNode);
			if (highScoreCtr.isViewHighscore()) {
				Component compi = highScoreCtr.getInitialComponent();
				mainVC.put("highScore", compi);							
			}
		}
	}
	
	private boolean isManagementTabAvalaible(ModuleConfiguration config) {
		return (userCourseEnv.isAdmin()
				|| (userCourseEnv.isCoach() && config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)))
				&& (config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT) || config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == runLink) {
					doOpenRun(ureq);
				} else if (clickedLink == coachLink) {
					doOpenCoach(ureq);
				} else if(clickedLink == manageLink) {
					doManage(ureq);
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doOpenRun(UserRequest ureq) {
		if(runCtrl == null) {
			createRun(ureq);
		}
		mainVC.put("segmentCmp", runCtrl.getInitialComponent());
	}
	
	private void doOpenCoach(UserRequest ureq) {
		if(coachCtrl == null) {
			createCoach(ureq);
		}
		mainVC.put("segmentCmp", coachCtrl.getInitialComponent());
	}
	
	private void doManage(UserRequest ureq) {
		if(manageCtrl == null) {
			createManage(ureq);
		}
		mainVC.put("segmentCmp", manageCtrl.getInitialComponent());
	}
	
	private GTAParticipantController createRun(UserRequest ureq) {
		removeAsListenerAndDispose(runCtrl);
		
		runCtrl = new GTAParticipantController(ureq, getWindowControl(), gtaNode, userCourseEnv);
		listenTo(runCtrl);
		return runCtrl;
	}
	
	private GTACoachSelectionController createCoach(UserRequest ureq) {
		removeAsListenerAndDispose(coachCtrl);
		
		coachCtrl = new GTACoachSelectionController(ureq, getWindowControl(), userCourseEnv, gtaNode);
		listenTo(coachCtrl);
		return coachCtrl;
	}
	
	private GTACoachManagementController createManage(UserRequest ureq) {
		removeAsListenerAndDispose(manageCtrl);
		manageCtrl = new GTACoachManagementController(ureq, getWindowControl(), userCourseEnv, gtaNode);
		listenTo(manageCtrl);
		return manageCtrl;
	}
}