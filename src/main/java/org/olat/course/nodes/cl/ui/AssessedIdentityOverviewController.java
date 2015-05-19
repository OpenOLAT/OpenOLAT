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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentityInfosController;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentEditController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final Link checkListLink, assessmentLink;
	private final SegmentViewComponent segmentView;
	
	private AssessedIdentityCheckListController listCtrl;
	private AssessmentEditController assessmentCtrl;
	
	private final Identity assessedIdentity;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	
	private boolean changes = false;
	
	public AssessedIdentityOverviewController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, OLATResourceable courseOres,
			UserCourseEnvironment userCourseEnv, CheckListCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		this.courseOres = courseOres;
		this.assessedIdentity = assessedIdentity;
		this.userCourseEnv = userCourseEnv;
		
		mainVC = createVelocityContainer("user_assessment");
		
		AssessedIdentityInfosController identityInfos = new AssessedIdentityInfosController(ureq, wControl, assessedIdentity);
		mainVC.put("identityInfos", identityInfos.getInitialComponent());
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		checkListLink = LinkFactory.createLink("checklist", mainVC, this);
		segmentView.addSegment(checkListLink, true);
		assessmentLink = LinkFactory.createLink("assessment", mainVC, this);
		segmentView.addSegment(assessmentLink, false);
		
		doOpenCheckList(ureq);
		
		mainVC.put("segments", segmentView);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public boolean isChanges() {
		return changes;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(listCtrl == source) {
			if(Event.DONE_EVENT == event) {
				changes = true;
				fireEvent(ureq, event);
			} else if (Event.CHANGED_EVENT == event) {
				changes = true;
				if(assessmentCtrl != null) {
					assessmentCtrl.reloadData(ureq);
				}
				fireEvent(ureq, event);
			} else if(Event.CANCELLED_EVENT == event) {
				changes = false;
				fireEvent(ureq, event);
			}

		} else if(assessmentCtrl == source) {
			if(Event.DONE_EVENT == event) {
				changes = true;
				fireEvent(ureq, event);
			} else if(Event.CHANGED_EVENT == event) {
				changes = true;
			} else if(Event.CANCELLED_EVENT == event) {
				changes = false;
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == checkListLink) {
					doOpenCheckList(ureq);
				} else if (clickedLink == assessmentLink) {
					doOpenAssessment(ureq);
				}
			}
		}
	}

	private void doOpenCheckList(UserRequest ureq) {
		if(listCtrl == null) {
			listCtrl = new AssessedIdentityCheckListController(ureq, getWindowControl(), assessedIdentity,
					courseOres, userCourseEnv, courseNode, true);
			listenTo(listCtrl);
		}
		mainVC.put("segmentCmp", listCtrl.getInitialComponent());
	}

	private void doOpenAssessment(UserRequest ureq) {
		if(assessmentCtrl == null) {
			ICourse course = CourseFactory.loadCourse(courseOres);
			UserCourseEnvironment uce = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			AssessedIdentityWrapper idWrapper = AssessmentHelper.wrapIdentity(uce, null, courseNode);
			assessmentCtrl = new AssessmentEditController(ureq, getWindowControl(), null, course, courseNode,
					idWrapper, false, true, true);
			assessmentCtrl.setIdentityInfos(false);
			assessmentCtrl.setCourseNodeInfos(false);
			assessmentCtrl.setTitleInfos(false);
			listenTo(assessmentCtrl);
		}
		mainVC.put("segmentCmp", assessmentCtrl.getInitialComponent());
	}
}
