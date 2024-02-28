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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessagePanelController;
import org.olat.core.gui.control.generic.spacesaver.ExpandController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionSmallOverviewController;
import org.olat.course.assessment.ui.tool.event.AssessmentInspectionSelectionEvent;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.course.assessment.ui.tool.event.ShowOrdersEvent;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentStatisticsController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.PercentStat;
import org.olat.modules.assessment.ui.ScoreStat;
import org.olat.modules.assessment.ui.UserFilterController;
import org.olat.modules.assessment.ui.event.ParticipantTypeFilterEvent;
import org.olat.modules.assessment.ui.event.UserFilterEvent;
import org.olat.modules.grade.GradeModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseOverviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	private UserFilterController userFilterCtrl;
	private final AssessmentStatisticsController statisticCtrl;
	private final CourseNodeToReviewSmallController toReviewCtrl;
	private final Controller toReleaseCtrl;
	private ExpandController expandInspectionCtrl;
	private CourseNodeAssignedSmallController assignedCtrl;
	private CourseNodeToApplyGradeSmallController toApplyGradeCtrl;
	private final AssessmentModeOverviewListController assessmentModeListCtrl;
	private AssessmentInspectionSmallOverviewController inspectionListCtrl;
	
	private SearchAssessedIdentityParams params;

	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentInspectionService inspectionService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentNotificationsHandler assessmentNotificationsHandler;
	@Autowired
	private GradeModule gradeModule;
	
	public AssessmentCourseOverviewController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, UserCourseEnvironment coachUserEnv, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("course_overview");
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		boolean hasAssessableNodes = AssessmentHelper.checkForAssessableNodes(courseEntry, course.getRunStructure().getRootNode());
		mainVC.contextPut("hasAssessableNodes", Boolean.valueOf(hasAssessableNodes));
		
		// assessment changes subscription
		if (hasAssessableNodes) {
			SubscriptionContext subsContext = assessmentNotificationsHandler.getAssessmentSubscriptionContext(ureq.getIdentity(), course);
			if (subsContext != null) {
				PublisherData pData = assessmentNotificationsHandler.getAssessmentPublisherData(course, wControl.getBusinessControl().getAsString());
				Controller csc = new ContextualSubscriptionController(ureq, wControl, subsContext, pData);
				listenTo(csc); // cleanup on dispose
				mainVC.put("assessmentSubscription", csc.getInitialComponent());
			}
		}
		
		// certificate subscription
		SubscriptionContext subsContext = certificatesManager.getSubscriptionContext(course);
		if (subsContext != null) {
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData pData = certificatesManager.getPublisherData(course, businessPath);
			Controller certificateSubscriptionCtrl = new ContextualSubscriptionController(ureq, wControl, subsContext, pData);
			listenTo(certificateSubscriptionCtrl);
			mainVC.put("certificationSubscription", certificateSubscriptionCtrl.getInitialComponent());
		}
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		params = new SearchAssessedIdentityParams(courseEntry, rootNode.getIdent(), null, assessmentCallback);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		
		if (params.isNonMembers() || params.hasFakeParticipants()) {
			userFilterCtrl = new UserFilterController(ureq, wControl, true, params.isNonMembers(), params.hasFakeParticipants(), false, true, false, false, false);
			listenTo(userFilterCtrl);
			mainVC.put("user.filter", userFilterCtrl.getInitialComponent());
			params.setParticipantTypes(Set.of(ParticipantType.member));
		}
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, rootNode);
		PercentStat percentStat = null;
		if (Mode.none != assessmentConfig.getPassedMode()) {
			percentStat = PercentStat.passed;
		} else if (assessmentConfig.hasStatus() || LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachUserEnv).getType())) {
			percentStat = PercentStat.status;
		}
		ScoreStat scoreStat = ScoreStat.noScore();
		if (Mode.none != assessmentConfig.getScoreMode()) {
			Double minScore = assessmentConfig.getMinScore()!= null? Double.valueOf(assessmentConfig.getMinScore().doubleValue()): null;
			Double maxScore = assessmentConfig.getMaxScore()!= null? Double.valueOf(assessmentConfig.getMaxScore().doubleValue()): null;
			Double weightedMinScore = assessmentConfig.getWeightedMinScore()!= null? Double.valueOf(assessmentConfig.getWeightedMinScore().doubleValue()): null;
			Double weightedMaxScore = assessmentConfig.getWeightedMaxScore()!= null? Double.valueOf(assessmentConfig.getWeightedMaxScore().doubleValue()): null;
			boolean scoreScaleEnabled = ScoreScalingHelper.isEnabled(course);
			scoreStat = ScoreStat.of(minScore, maxScore, weightedMinScore, weightedMaxScore, false, scoreScaleEnabled);
		}
		
		statisticCtrl = new AssessmentStatisticsController(ureq, getWindowControl(), courseEntry, assessmentCallback, params, percentStat, scoreStat);
		statisticCtrl.reload();
		listenTo(statisticCtrl);
		mainVC.put("statistic", statisticCtrl.getInitialComponent());
		
		if(hasCoachAssignment(courseEntry, coachUserEnv.getCourseEnvironment().getRunStructure().getRootNode())) {
			assignedCtrl = new CourseNodeAssignedSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
			listenTo(assignedCtrl);
			mainVC.put("assigned", assignedCtrl.getInitialComponent());
		}
		
		toReviewCtrl = new CourseNodeToReviewSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(toReviewCtrl);
		mainVC.put("toReview", toReviewCtrl.getInitialComponent());
		
		if (coachUserEnv.isAdmin() || rootNode.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY, true)) {
			toReleaseCtrl = new CourseNodeToReleaseSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		} else {
			toReleaseCtrl = new MessagePanelController(ureq, wControl, "o_icon_results_hidden",
					translate("user.visibility.hidden.title"), translate("user.visibility.hidden.owner.only"));
		}
		listenTo(toReleaseCtrl);
		mainVC.put("toRelease", toReleaseCtrl.getInitialComponent());
		
		if (coachUserEnv.isAdmin() || rootNode.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY)) {
			List<String> manualGradeSubIdents = new ArrayList<>();
			addManualGradeSubIdents(manualGradeSubIdents, courseEntry, coachUserEnv.getCourseEnvironment().getRunStructure().getRootNode());
			if (gradeModule.isEnabled() && !manualGradeSubIdents.isEmpty()) {
				toApplyGradeCtrl = new CourseNodeToApplyGradeSmallController(ureq, getWindowControl(), courseEntry, assessmentCallback, manualGradeSubIdents);
				listenTo(toApplyGradeCtrl);
				mainVC.put("toApplyGrade", toApplyGradeCtrl.getInitialComponent());
			}
		}
		
		assessmentModeListCtrl = new AssessmentModeOverviewListController(ureq, getWindowControl(), courseEntry, assessmentCallback);
		listenTo(assessmentModeListCtrl);
		if(assessmentModeListCtrl.getNumOfAssessmentModes() > 0) {
			mainVC.put("assessmentModes", assessmentModeListCtrl.getInitialComponent());
		}
		
		if(assessmentModule.isAssessmentInspectionEnabled() && inspectionService.hasInspectionConfigurations(courseEntry) ) {
			expandInspectionCtrl = new ExpandController(ureq, wControl, "assessment-inspection-small-" + courseEntry.getKey().toString());
			listenTo(expandInspectionCtrl);
			mainVC.put("expandInspections", expandInspectionCtrl.getInitialComponent());
			
			inspectionListCtrl = new AssessmentInspectionSmallOverviewController(ureq, getWindowControl(), courseEntry);
			inspectionListCtrl.setExpanded(true);
			listenTo(inspectionListCtrl);
			if(inspectionListCtrl.getNumOfInspections() > 0) {
				mainVC.put("inspections", inspectionListCtrl.getInitialComponent());
			}
			expandInspectionCtrl.setExpandableController(inspectionListCtrl);
		}
		
		putInitialPanel(mainVC);
	}
	
	private boolean hasCoachAssignment(RepositoryEntry courseEntry, CourseNode courseNode) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		if(assessmentConfig.hasCoachAssignment()) {
			return true;
		}
		
		int childCount = courseNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = courseNode.getChildAt(i);
			if (child instanceof CourseNode childCourseNode && hasCoachAssignment(courseEntry, childCourseNode)) {
				return true;
			}
		}
		return false;
	}
	
	private void addManualGradeSubIdents(List<String> manualGradeSubIdents, RepositoryEntry courseEntry, CourseNode courseNode) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		if (Mode.none != assessmentConfig.getScoreMode() && assessmentConfig.hasGrade() && !assessmentConfig.isAutoGrade()) {
			manualGradeSubIdents.add(courseNode.getIdent());
		} 
		int childCount = courseNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = courseNode.getChildAt(i);
			if (child instanceof CourseNode childCourseNode) {
				addManualGradeSubIdents(manualGradeSubIdents, courseEntry, childCourseNode);
			}
		}
	}
	
	public void reload(List<ParticipantType> participantTypes) {
		params.setParticipantTypes(participantTypes);
		if (userFilterCtrl != null && participantTypes != null) {
			userFilterCtrl.select(
					participantTypes.contains(ParticipantType.member),
					participantTypes.contains(ParticipantType.nonMember),
					participantTypes.contains(ParticipantType.fakeParticipant),
					false);
		}
		reload();
	}

	void reload() {
		statisticCtrl.reload();
		reloadToDos();
	}

	private void reloadToDos() {
		toReviewCtrl.loadModel(params.getParticipantTypes());
		if (toReleaseCtrl instanceof CourseNodeToReleaseSmallController smallToReleaseCtrl) {
			smallToReleaseCtrl.loadModel(params.getParticipantTypes());
		}
		if (toApplyGradeCtrl != null) {
			toApplyGradeCtrl.loadModel(params.getParticipantTypes());
		}
		if(assignedCtrl != null) {
			assignedCtrl.loadModel(params.getParticipantTypes());
		}
		
	}
	
	public void reloadAssessmentModes() {
		assessmentModeListCtrl.loadModel();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userFilterCtrl) {
			if (event instanceof UserFilterEvent ufe) {
				List<ParticipantType> participantTypes = new ArrayList<>(3);
				if (ufe.isWithMembers()) {
					participantTypes.add(ParticipantType.member);
				}
				if (ufe.isWithNonParticipantUsers()) {
					participantTypes.add(ParticipantType.nonMember);
				}
				if (ufe.isWithFakeParticipants()) {
					participantTypes.add(ParticipantType.fakeParticipant);
				}
				params.setParticipantTypes(participantTypes);
				reload();
				fireEvent(ureq, new ParticipantTypeFilterEvent(participantTypes));
			}
		} else if(toReviewCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			} else if(event instanceof ShowOrdersEvent) {
				fireEvent(ureq, new ShowOrdersEvent(getEntries("Review", null)));
			}
		} else if(assignedCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			} else if(event instanceof ShowOrdersEvent) {
				fireEvent(ureq, new ShowOrdersEvent(getEntries("Review", "AssignedToMe")));
			}
		} else if(toReleaseCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			} else if(event instanceof ShowOrdersEvent) {
				fireEvent(ureq, new ShowOrdersEvent(getEntries("Release", null)));
			}
		} else if(toApplyGradeCtrl == source) {
			if(event instanceof CourseNodeIdentityEvent) {
				fireEvent(ureq, event);
			} else if(event instanceof ShowOrdersEvent) {
				fireEvent(ureq, new ShowOrdersEvent(getEntries("ApplyGrade", null)));
			}
		} else if(assessmentModeListCtrl == source) {
			if(event instanceof CourseNodeEvent || event instanceof AssessmentModeStatusEvent) {
				fireEvent(ureq, event);
			}
		} else if(inspectionListCtrl == source) {
			if(event instanceof AssessmentInspectionSelectionEvent) {
				fireEvent(ureq, event);
			}
		} else if (statisticCtrl == source) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
	
	private List<ContextEntry> getEntries(String resourceType, String subType) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromResourceType(resourceType);
		if(subType != null) {
			List<ContextEntry> subEntries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(subType);
			entries.addAll(subEntries);
		}
		return entries;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}