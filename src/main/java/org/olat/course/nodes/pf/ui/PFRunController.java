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
package org.olat.course.nodes.pf.ui;

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
import org.olat.core.id.Identity;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.pf.manager.PFEvent;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.manager.PFView;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFRunController extends BasicController {
	
	private PFCourseNode pfNode;
	private UserCourseEnvironment userCourseEnv;
	private PFView pfView;
	
	private PFCoachController coachController;
	private PFParticipantController participantController;
	
	private Link coachLink;
	private Link participantLink;
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	

	@Autowired
	private PFManager pfManager;

	public PFRunController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.pfNode = pfNode;
		this.userCourseEnv = userCourseEnv;
		pfView = pfManager.providePFView(pfNode);
		
		mainVC = createVelocityContainer("run");
	
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			if ((userCourseEnv.isCoach() || userCourseEnv.isAdmin()) && userCourseEnv.isParticipant()) {
				segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
				coachLink = LinkFactory.createLink("tab.coach", mainVC, this);
				segmentView.addSegment(coachLink, true);
				participantLink = LinkFactory.createLink("tab.participant", mainVC, this);
				segmentView.addSegment(participantLink, false);			
			}		
			doOpenCoachView(ureq);
		} else {
			doOpenParticipantsView(ureq);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == coachLink) { 
					doOpenCoachView(ureq);
				} else if (clickedLink == participantLink) {
					doOpenParticipantsView(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == coachController) {
			if (event instanceof PFEvent) {
				segmentView.select(participantLink);
				PFEvent sfe = (PFEvent)event;
				doOpenParticipantsView(ureq, sfe.getIdentity(), pfView);
			} else if (event == Event.CHANGED_EVENT) {
				doOpenCoachView(ureq);
			} 			
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doOpenCoachView(UserRequest ureq) {
		removeAsListenerAndDispose(coachController);
		
		coachController = new PFCoachController(ureq, getWindowControl(), pfNode, userCourseEnv, pfView);
		listenTo(coachController);
		mainVC.put("segmentCmp", coachController.getInitialComponent());
	}
	
	private void doOpenParticipantsView (UserRequest ureq) {
		doOpenParticipantsView(ureq, ureq.getIdentity(), pfView); 
	}
	
	private void doOpenParticipantsView(UserRequest ureq, Identity identity, PFView view) {
		removeAsListenerAndDispose(participantController);
		
		participantController = new PFParticipantController(ureq, getWindowControl(), pfNode, 
				userCourseEnv, identity, view, false, false);
		listenTo(participantController);
		mainVC.put("segmentCmp", participantController.getInitialComponent());
	}
	
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq) {
		// integrate it into the olat menu
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, pfNode, "o_pf_icon");
		return new NodeRunConstructionResult(ctrl);
	}

}
