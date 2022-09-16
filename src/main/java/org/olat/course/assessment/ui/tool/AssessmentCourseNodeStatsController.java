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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentStatsController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.PercentStat;
import org.olat.modules.assessment.ui.ScoreStat;
import org.olat.modules.assessment.ui.UserFilterController;
import org.olat.modules.assessment.ui.event.ParticipantTypeFilterEvent;
import org.olat.modules.assessment.ui.event.UserFilterEvent;
import org.olat.modules.grade.GradeModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseNodeStatsController extends BasicController implements AssessmentCourseNodeOverviewController {

	private final AssessmentStatsController assessmentStatsCtrl;
	private UserFilterController userFilterCtrl;
	protected Controller detailsCtrl;

	protected final UserCourseEnvironment userCourseEnv;
	protected final CourseNode courseNode;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	protected final SearchAssessedIdentityParams params;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;

	public AssessmentCourseNodeStatsController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode, AssessmentToolSecurityCallback assessmentCallback, boolean courseInfoLaunch, boolean readOnly) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.assessmentCallback = assessmentCallback;
		
		// Reset the velocity root, so that the children find the template
		setVelocityRoot(Util.getPackageVelocityRoot(AssessmentCourseNodeStatsController.class));
		VelocityContainer mainVC = createVelocityContainer("course_node_stats");
		
		params = new SearchAssessedIdentityParams(
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent(),
				courseNode.getReferencedRepositoryEntry(), assessmentCallback);
		if(assessmentCallback.canAssessBusinessGoupMembers() && assessmentCallback.getCoachedGroups() != null && !assessmentCallback.getCoachedGroups().isEmpty()) {
			params.setBusinessGroupKeys(assessmentCallback.getCoachedGroups().stream().map(BusinessGroup::getKey).collect(Collectors.toList()));
		}
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		params.setParticipantTypes(Set.of(ParticipantType.member));
		
		if (params.isNonMembers() || params.hasFakeParticipants()) {
			userFilterCtrl = new UserFilterController(ureq, wControl, true, params.isNonMembers(), params.hasFakeParticipants(), false, true, false, false, false);
			listenTo(userFilterCtrl);
			mainVC.put("user.filter", userFilterCtrl.getInitialComponent());
		}
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), courseNode);
		PercentStat percentStat = null;
		if (Mode.none != assessmentConfig.getPassedMode()) {
			percentStat = PercentStat.passed;
		} else if (assessmentConfig.hasStatus() || LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(userCourseEnv).getType())) {
			percentStat = PercentStat.status;
		}
		ScoreStat scoreStat = ScoreStat.noScore();
		if (Mode.none != assessmentConfig.getScoreMode()) {
			Double minScore = assessmentConfig.getMinScore()!= null? Double.valueOf(assessmentConfig.getMinScore().doubleValue()): null;
			Double maxScore = assessmentConfig.getMaxScore()!= null? Double.valueOf(assessmentConfig.getMaxScore().doubleValue()): null;
			boolean gradeEnabled = gradeModule.isEnabled() && assessmentConfig.hasGrade();
			scoreStat = ScoreStat.of(minScore, maxScore, gradeEnabled);
		}
		
		assessmentStatsCtrl = new AssessmentStatsController(ureq, wControl, assessmentCallback, params, percentStat, scoreStat, courseInfoLaunch, readOnly, false);
		assessmentStatsCtrl.setExpanded(true);
		listenTo(assessmentStatsCtrl);
		mainVC.put("stats", assessmentStatsCtrl.getInitialComponent());
		
		if (hasDetails()) {
			detailsCtrl = createDetailsController(ureq, wControl);
			listenTo(detailsCtrl);
			mainVC.put("details", detailsCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}

	protected boolean hasDetails() {
		return false;
	}

	@SuppressWarnings("unused")
	protected Controller createDetailsController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public void reload() {
		assessmentStatsCtrl.reload();
	}
	
	@Override
	public void reload(List<ParticipantType> participantTypes) {
		if (userFilterCtrl != null && participantTypes != null) {
			userFilterCtrl.select(
					participantTypes.contains(ParticipantType.member),
					participantTypes.contains(ParticipantType.nonMember),
					participantTypes.contains(ParticipantType.fakeParticipant),
					false);
		}
		params.setParticipantTypes(participantTypes);
		reload();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userFilterCtrl) {
			if (event instanceof UserFilterEvent) {
				UserFilterEvent ufe = (UserFilterEvent)event;
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
		} else if (source == assessmentStatsCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
