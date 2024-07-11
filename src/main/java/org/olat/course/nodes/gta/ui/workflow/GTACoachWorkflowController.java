/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.workflow;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachWorkflowController extends BasicController implements Activateable2 {

	private final Link assignmentsLink;
	private final Link submissionsLink;
	private final Link reviewAndCorrectionLink;
	private final Link revisionLink;
	private final Link peerReviewLink;
	private final Link solutionLink;
	private final Link gradingLink;

	private final VelocityContainer mainVC;
	private final BreadcrumbPanel stackPanel;
	
	private final GTACourseNode gtaNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private final List<Identity> assessableIdentities;

	private GTACoachGradingListController gradingCtrl;
	private GTACoachSolutionListController solutionsCtrl;
	private GTACoachRevisionListController revisionsCtrl;
	private GTACoachPeerReviewListController peerReviewCtrl;
	private GTACoachAssignmentListController assignmentsCtrl;
	private GTACoachSubmissionListController submissionsCtrl;
	private GTACoachReviewAndCorrectionListController reviewsAndCorrectionsCtrl;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GTACoachWorkflowController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		
		this.gtaNode = gtaNode;
		this.coachCourseEnv = coachCourseEnv;
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, coachCourseEnv);
		assessableIdentities = getAssessableIdentities();
		
		mainVC = createVelocityContainer("workflow");

		String assignmentLabel = decorateLinks("workflow.list.assignment", GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		assignmentsLink = LinkFactory.createLink("workflow.list.assignment", "workflow.list.assignment", "assignment", assignmentLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		assignmentsLink.setElementCssClass("btn-arrow-right so_sel_course_gta_assignment_list");
		assignmentsLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));

		String submissionLabel = decorateLinks("workflow.list.submission", GTACourseNode.GTASK_SUBMIT_DEADLINE);
		submissionsLink = LinkFactory.createLink("workflow.list.submission", "workflow.list.submission", "submission", submissionLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		submissionsLink.setElementCssClass("btn-arrow-right o_sel_course_gta_submission_list");
		submissionsLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT));

		// Review and correction
		String reviewAndCorrectionLabel  = decorateLinks("workflow.list.review.correction", GTACourseNode.GTASK_PEER_REVIEW_DEADLINE);
		reviewAndCorrectionLink = LinkFactory.createLink("workflow.list.review.correction", "workflow.list.review.correction", "review", reviewAndCorrectionLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		reviewAndCorrectionLink.setElementCssClass("btn-arrow-right o_sel_course_gta_review_list");
		reviewAndCorrectionLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION));

		String revisionLabel  = decorateLinks("workflow.list.revision", null);
		revisionLink = LinkFactory.createLink("workflow.list.revision", "workflow.list.revision", "revision", revisionLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		revisionLink.setElementCssClass("btn-arrow-right o_sel_course_gta_revision_list");
		revisionLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD));
		
		// Peer review
		String peerReviewLabel = decorateLinks("workflow.list.peerreview", GTACourseNode.GTASK_PEER_REVIEW_DEADLINE);
		peerReviewLink = LinkFactory.createLink("workflow.list.peerreview", "workflow.list.peerreview", "peerreview", peerReviewLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		peerReviewLink.setElementCssClass("btn-arrow-right o_sel_course_gta_peerreview_list");
		peerReviewLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW));
		
		// Solution
		String solutionLabel = decorateLinks("workflow.list.solution", GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		solutionLink = LinkFactory.createLink("workflow.list.solution", "workflow.list.solution", "solution", solutionLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		solutionLink.setElementCssClass("btn-arrow-right o_sel_course_gta_solution_list");
		solutionLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
		
		// Grading
		String gradingLabel = decorateLinks("workflow.list.grading", null);
		gradingLink = LinkFactory.createLink("workflow.list.grading", "workflow.list.grading", "grading", gradingLabel,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		gradingLink.setElementCssClass("btn-arrow-right o_sel_course_gta_grading_list");
		gradingLink.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_GRADING));
		
		int numOfSteps = countVisible(assignmentsLink, submissionsLink, reviewAndCorrectionLink, revisionLink, peerReviewLink, solutionLink, gradingLink);
		mainVC.contextPut("menuVisible", Boolean.valueOf(numOfSteps > 1));
		
		putInitialPanel(mainVC);
		openFirstStep(ureq);
	}
	
	private int countVisible(Link... links) {
		int countVisible = 0;
		for(Link link:links) {
			if(link != null && link.isVisible()) {
				countVisible++;
			}
		}
		return countVisible;
	}
	
	private void openFirstStep(UserRequest ureq) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			doOpenAssignments(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			doOpenSubmissions(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			doOpenReviewAndCorrections(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			doOpenRevisions(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)) {
			doOpenPeerReview(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
			doOpenSolution(ureq);
		} else if(config.getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			doOpenGrading(ureq);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Assignments".equalsIgnoreCase(type)) {
			if(assignmentsLink != null && assignmentsLink.isVisible()) {
				doOpenAssignments(ureq);
			}
		} else if("Submissions".equalsIgnoreCase(type)) {
			if(submissionsLink != null && submissionsLink.isVisible()) {
				doOpenSubmissions(ureq);
			}
		} else if("Reviews".equalsIgnoreCase(type)) {
			if(reviewAndCorrectionLink != null && reviewAndCorrectionLink.isVisible()) {
				doOpenReviewAndCorrections(ureq);
			}
		} else if("Revisions".equalsIgnoreCase(type)) {
			if(revisionLink != null && revisionLink.isVisible()) {
				doOpenRevisions(ureq);
			}
		} else if("PeerReview".equalsIgnoreCase(type)) {
			if(peerReviewLink != null && peerReviewLink.isVisible()) {
				doOpenPeerReview(ureq);
			}
		} else if("Solutions".equalsIgnoreCase(type)) {
			if(solutionLink != null && solutionLink.isVisible()) {
				doOpenSolution(ureq);	
			}
		} else if("Grading".equalsIgnoreCase(type)) {
			if(gradingLink != null && gradingLink.isVisible()) {
				doOpenGrading(ureq);
			}
		}
	}

	private String decorateLinks(String i18nKey, String dueDateConfig) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span><strong>").append(translate(i18nKey)).append("</strong>");
		
		if(StringHelper.containsNonWhitespace(dueDateConfig)) {
			DueDateConfig dueDates = gtaNode.getDueDateConfig(dueDateConfig);
			if(dueDates.getAbsoluteDate() != null) {
				String date = dateToString(dueDates.getAbsoluteDate());
				sb.append("<br>").append(date);
			}
		}
		sb.append("<span>");
		
		return sb.toString();
	}
	
	protected final boolean isDateOnly(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
	}
	
	protected final String dateToString(Date date) {
		Formatter formatter = Formatter.getInstance(getLocale());
		return isDateOnly(date) ? formatter.formatDate(date) : formatter.formatDateAndTime(date);
	}
	
	private void selectLink(Link selectedLink) {
		setElementCssLink(assignmentsLink, selectedLink, "o_businessgroup_active");
		setElementCssLink(submissionsLink, selectedLink, "o_sel_course_gta_submission_list");
		setElementCssLink(reviewAndCorrectionLink, selectedLink, "o_sel_course_gta_review_list");
		setElementCssLink(revisionLink, selectedLink, "o_sel_course_gta_revision_list");
		setElementCssLink(peerReviewLink, selectedLink, "o_sel_course_gta_peerreview_list");
		setElementCssLink(solutionLink, selectedLink, "o_sel_course_gta_solution_list");
		setElementCssLink(gradingLink, selectedLink, "o_sel_course_gta_grading_list");
	}
	
	private void setElementCssLink(Link link, Link selectedLink, String elementCssClass) {
		String css = link == selectedLink ? "btn-primary btn-arrow-right " : "btn-arrow-right ";
		link.setElementCssClass(css + elementCssClass);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == assignmentsLink) {
			doOpenAssignments(ureq);
		} else if (source == submissionsLink) {
			doOpenSubmissions(ureq);
		} else if(source == reviewAndCorrectionLink) {
			doOpenReviewAndCorrections(ureq);
		} else if(source == revisionLink) {
			doOpenRevisions(ureq);
		} else if(source == peerReviewLink) {
			doOpenPeerReview(ureq);
		} else if(source == solutionLink) {
			doOpenSolution(ureq);
		} else if(source == gradingLink) {
			doOpenGrading(ureq);
		}
	}
	
	private void doOpenAssignments(UserRequest ureq) {
		removeAsListenerAndDispose(assignmentsCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assignments"), null);
		assignmentsCtrl = new GTACoachAssignmentListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(assignmentsCtrl);
		
		addToHistory(ureq, assignmentsCtrl);
		mainVC.put("segmentCmp", assignmentsCtrl.getInitialComponent());
		selectLink(assignmentsLink);
	}
	
	private void doOpenSubmissions(UserRequest ureq) {
		removeAsListenerAndDispose(submissionsCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Submissions"), null);
		submissionsCtrl = new GTACoachSubmissionListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(submissionsCtrl);
		
		addToHistory(ureq, submissionsCtrl);
		mainVC.put("segmentCmp", submissionsCtrl.getInitialComponent());
		selectLink(submissionsLink);
	}
	
	private void doOpenReviewAndCorrections(UserRequest ureq) {
		removeAsListenerAndDispose(reviewsAndCorrectionsCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Reviews"), null);
		reviewsAndCorrectionsCtrl = new GTACoachReviewAndCorrectionListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(reviewsAndCorrectionsCtrl);

		addToHistory(ureq, reviewsAndCorrectionsCtrl);
		mainVC.put("segmentCmp", reviewsAndCorrectionsCtrl.getInitialComponent());
		selectLink(reviewAndCorrectionLink);
	}
	
	private void doOpenRevisions(UserRequest ureq) {
		removeAsListenerAndDispose(revisionsCtrl);

		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Revisions"), null);
		revisionsCtrl = new GTACoachRevisionListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(revisionsCtrl);
		
		addToHistory(ureq, revisionsCtrl);
		mainVC.put("segmentCmp", revisionsCtrl.getInitialComponent());
		selectLink(revisionLink);
	}
	
	private void doOpenPeerReview(UserRequest ureq) {
		removeAsListenerAndDispose(peerReviewCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("PeerReview"), null);
		peerReviewCtrl = new GTACoachPeerReviewListController(ureq, swControl, stackPanel,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(peerReviewCtrl);

		addToHistory(ureq, peerReviewCtrl);
		mainVC.put("segmentCmp", peerReviewCtrl.getInitialComponent());
		selectLink(peerReviewLink);
	}
	
	private void doOpenSolution(UserRequest ureq) {
		removeAsListenerAndDispose(solutionsCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Solutions"), null);
		solutionsCtrl = new GTACoachSolutionListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(solutionsCtrl);
	
		addToHistory(ureq, solutionsCtrl);
		mainVC.put("segmentCmp", solutionsCtrl.getInitialComponent());
		selectLink(solutionLink);
	}
	
	private void doOpenGrading(UserRequest ureq) {
		removeAsListenerAndDispose(gradingCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Grading"), null);
		gradingCtrl = new GTACoachGradingListController(ureq, swControl,
				coachCourseEnv, assessableIdentities, gtaNode);
		listenTo(gradingCtrl);
	
		addToHistory(ureq, gradingCtrl);
		mainVC.put("segmentCmp", gradingCtrl.getInitialComponent());
		selectLink(gradingLink);
	}
	
	public List<Identity> getAssessableIdentities() {
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();
		
		return assessmentCallback.isAdmin()
				? repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name())
						.stream().distinct().collect(Collectors.toList())
				: repositoryService.getCoachedParticipants(getIdentity(), re);
	}
}
