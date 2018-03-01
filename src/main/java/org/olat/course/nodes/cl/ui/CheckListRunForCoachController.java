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
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListRunForCoachController extends BasicController {
	
	private final VelocityContainer mainVC;

	private final Link runLink, coachLink;
	private final SegmentViewComponent segmentView;
	
	private CheckListRunController runController;
	private CheckListAssessmentController assessmentController;
	
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	
	public CheckListRunForCoachController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseOres = courseOres;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		runLink = LinkFactory.createLink("run.run", mainVC, this);
		segmentView.addSegment(runLink, true);
		coachLink = LinkFactory.createLink("run.coach", mainVC, this);
		segmentView.addSegment(coachLink, false);
		
		doOpenRun(ureq);
		
		mainVC.put("segments", segmentView);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
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
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(runController == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenRun(UserRequest ureq) {
		if(runController == null) {
			runController = new CheckListRunController(ureq, getWindowControl(), userCourseEnv, courseOres, courseNode);
			listenTo(runController);
		}
		mainVC.put("segmentCmp", runController.getInitialComponent());
	}
	
	private void doOpenCoach(UserRequest ureq) {
		if(assessmentController == null) {
			assessmentController = new CheckListAssessmentController(ureq, getWindowControl(), userCourseEnv, courseOres, courseNode);
			listenTo(assessmentController);
		}
		mainVC.put("segmentCmp", assessmentController.getInitialComponent());
	}
}
