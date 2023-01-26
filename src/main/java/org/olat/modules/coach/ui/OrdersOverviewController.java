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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
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
import org.olat.course.assessment.ui.tool.AssessmentApplyGradeListController;
import org.olat.course.assessment.ui.tool.AssessmentReleaseListController;
import org.olat.course.assessment.ui.tool.AssessmentReviewListController;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.ui.GradingAssignmentsListController;

/**
 * 
 * Initial date: 22 Nov 2021<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrdersOverviewController extends BasicController implements Activateable2 {

	private final Link assessmentReviewLink;
	private final Link assessmentReleaseLink;
	private final Link assessmentApplyGradeLink;
	private final Link gradingAssignmentsLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final TooledStackedPanel stackPanel;
	
	private AssessmentReviewListController assessmentReviewCtrl;
	private AssessmentReleaseListController assessmentReleaseCtrl;
	private AssessmentApplyGradeListController assessmentApplyGradeCtrl;
	private GradingAssignmentsListController gradingAssignmentsCtrl;
	
	private final CoachingSecurity coachingSec;
	private final GradingSecurityCallback secCallback;
	
	public OrdersOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CoachingSecurity coachingSec, GradingSecurityCallback gradingSec) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.coachingSec = coachingSec;
		this.secCallback = gradingSec;
		
		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		assessmentReviewLink = LinkFactory.createLink("orders.review", mainVC, this);
		assessmentReviewLink.setVisible(coachingSec.isCoach());
		segmentView.addSegment(assessmentReviewLink, true);
		if(coachingSec.isCoach()) {
			doOpenAssessmentReview(ureq);
		}
		
		assessmentApplyGradeLink = LinkFactory.createLink("orders.grades", mainVC, this);
		assessmentApplyGradeLink.setVisible(coachingSec.isCoach());
		segmentView.addSegment(assessmentApplyGradeLink, false);
		
		assessmentReleaseLink = LinkFactory.createLink("orders.release", mainVC, this);
		assessmentReleaseLink.setVisible(coachingSec.isCoach());
		segmentView.addSegment(assessmentReleaseLink, false);
		
		gradingAssignmentsLink = LinkFactory.createLink("orders.grading", mainVC, this);
		gradingAssignmentsLink.setVisible(secCallback.canGrade() && !secCallback.canManage());
		segmentView.addSegment(gradingAssignmentsLink, false);
		if(secCallback.canGrade() && !secCallback.canManage()) {
			doOpenMyGradingAssignments(ureq);
			segmentView.select(gradingAssignmentsLink);
		}
		if (mainVC.contextGet("segmentCmp") == null) {
			EmptyStateFactory.create("emptyStateCmp", mainVC, this);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Review".equalsIgnoreCase(type) && coachingSec.isCoach()) {
			doOpenAssessmentReview(ureq);
			segmentView.select(assessmentReviewLink);
		} else if("Release".equalsIgnoreCase(type)&& coachingSec.isCoach()) {
			doOpenAssessmentRelease(ureq);
			segmentView.select(assessmentReleaseLink);
		} else if("ApplyGrade".equalsIgnoreCase(type)&& coachingSec.isCoach()) {
			doOpenAssessmentApplyGrade(ureq);
			segmentView.select(assessmentReleaseLink);
		} else if("Assignments".equalsIgnoreCase(type) && secCallback.canGrade()) {
			doOpenMyGradingAssignments(ureq);
			segmentView.select(gradingAssignmentsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == assessmentReviewLink) {
					doOpenAssessmentReview(ureq);
				} else if (clickedLink == assessmentReleaseLink) {
					doOpenAssessmentRelease(ureq);
				} else if (clickedLink == assessmentApplyGradeLink) {
					doOpenAssessmentApplyGrade(ureq);
				} else if (clickedLink == gradingAssignmentsLink) {
					doOpenMyGradingAssignments(ureq);
				}
			}
		}
	}
	
	private void doOpenAssessmentReview(UserRequest ureq) {
		if(assessmentReviewCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Review"), null);
			assessmentReviewCtrl = new AssessmentReviewListController(ureq, swControl, stackPanel, translate("orders.review"));
			listenTo(assessmentReviewCtrl);
		} else {
			assessmentReviewCtrl.reload();
		}
		addToHistory(ureq, assessmentReviewCtrl);
		mainVC.put("segmentCmp", assessmentReviewCtrl.getInitialComponent());
	}
	
	private AssessmentReleaseListController doOpenAssessmentRelease(UserRequest ureq) {
		if(assessmentReleaseCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Release"), null);
			assessmentReleaseCtrl = new AssessmentReleaseListController(ureq, swControl, stackPanel, translate("orders.release"));
			listenTo(assessmentReleaseCtrl);
		} else {
			assessmentReleaseCtrl.reload();
		}
		addToHistory(ureq, assessmentReleaseCtrl);
		mainVC.put("segmentCmp", assessmentReleaseCtrl.getInitialComponent());
		return assessmentReleaseCtrl;
	}
	
	private AssessmentApplyGradeListController doOpenAssessmentApplyGrade(UserRequest ureq) {
		if(assessmentApplyGradeCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("ApplyGrade"), null);
			assessmentApplyGradeCtrl = new AssessmentApplyGradeListController(ureq, swControl, stackPanel, translate("orders.apply.grade"));
			listenTo(assessmentApplyGradeCtrl);
		} else {
			assessmentApplyGradeCtrl.reload();
		}
		addToHistory(ureq, assessmentApplyGradeCtrl);
		mainVC.put("segmentCmp", assessmentApplyGradeCtrl.getInitialComponent());
		return assessmentApplyGradeCtrl;
	}

	private void doOpenMyGradingAssignments(UserRequest ureq) {
		if(gradingAssignmentsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assignments"), null);
			GradingSecurityCallback mySecCallback = GradingSecurityCallbackFactory.mySecurityCalllback(secCallback);
			gradingAssignmentsCtrl = new GradingAssignmentsListController(ureq, swControl, getIdentity(), mySecCallback);
			listenTo(gradingAssignmentsCtrl);
			gradingAssignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
		addToHistory(ureq, gradingAssignmentsCtrl);
		mainVC.put("segmentCmp", gradingAssignmentsCtrl.getInitialComponent());
	}
}
