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
package org.olat.course.nodes.ms;

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
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 17 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSRunController extends BasicController {

	private static final String PLAY_RES_TYPE = "my";
	private static final String EDIT_RES_TYPE = "all";
	
	private VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link myLink;
	private Link allLink;
	
	private Controller myCtrl;
	private Controller coachCtrl;

	private final  MSCourseNode msCourseNode;
	private final UserCourseEnvironment userCourseEnv;
	

	public MSRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, MSCourseNode msCourseNode) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.msCourseNode = msCourseNode;
		
		mainVC = createVelocityContainer("run_segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		if (userCourseEnv.isParticipant()) {
			myLink = LinkFactory.createLink("segment.my", mainVC, this);
			segmentView.addSegment(myLink, true);
		}
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			allLink = LinkFactory.createLink("segment.all", mainVC, this);
			segmentView.addSegment(allLink, false);
		}
		boolean segmentViewVisible = segmentView.getSegments().size() > 1;
		segmentView.setVisible(segmentViewVisible);

		if (myLink != null ) {
			doOpenMy(ureq);
		} else if (allLink != null) {
			doOpenCoach(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myLink) {
					doOpenMy(ureq);
				} else if (clickedLink == allLink){
					doOpenCoach(ureq);
				}
			}
		}
	}
	
	private void doOpenMy(UserRequest ureq) {
		if (myCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(PLAY_RES_TYPE), null);
			myCtrl = new MSCourseNodeRunController(ureq, swControl, userCourseEnv, msCourseNode, true, true);
			listenTo(myCtrl);
		} else {
			addToHistory(ureq, myCtrl);
		}
		segmentView.select(myLink);
		mainVC.put("segmentCmp", myCtrl.getInitialComponent());
	}
	
	private void doOpenCoach(UserRequest ureq) {
		if (coachCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(EDIT_RES_TYPE), null);
			coachCtrl = new MSCoachRunController(ureq, swControl, userCourseEnv, msCourseNode);
			listenTo(coachCtrl);
		} else {
			addToHistory(ureq, coachCtrl);
		}
		segmentView.select(allLink);
		mainVC.put("segmentCmp", coachCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

}
