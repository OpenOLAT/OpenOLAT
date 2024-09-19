/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.cns.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.CNSEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSAssessmentEnvironment implements CNSEnvironment {
	
	private final AssessmentManager assessmentManager;
	private final Identity assessedIdentity;
	private final RepositoryEntry courseEntry;
	private final CNSCourseNode cnsCourseNode;

	public CNSAssessmentEnvironment(UserCourseEnvironment userCourseEnv, CNSCourseNode cnsCourseNode) {
		this.assessmentManager = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		this.assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.cnsCourseNode = cnsCourseNode;
	}

	@Override
	public void select(CourseNode selectedCourseNode) {
		LearningPathService learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		
		AssessmentObligation cnsAssessmentObligation = AssessmentObligation.mandatory;
		AssessmentEntry cnsAssessmentEntry = assessmentManager.getAssessmentEntry(cnsCourseNode, assessedIdentity);
		if (cnsAssessmentEntry != null && cnsAssessmentEntry.getObligation() != null && cnsAssessmentEntry.getObligation().getCurrent() != null) {
			cnsAssessmentObligation = cnsAssessmentEntry.getObligation().getCurrent();
		}
		LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(selectedCourseNode, cnsCourseNode);
		Set<AssessmentObligation> availableObligations = learningPathConfigs.getAvailableObligations();
		if (!availableObligations.contains(cnsAssessmentObligation) && availableObligations.contains(AssessmentObligation.evaluated)) {
			cnsAssessmentObligation = AssessmentObligation.evaluated;
		}
		
		AssessmentEntry selectedAssessmentEntry = assessmentManager.getOrCreateAssessmentEntry(selectedCourseNode, assessedIdentity, Boolean.FALSE);
		ObligationOverridable selectedObligation = selectedAssessmentEntry.getObligation();
		selectedObligation.overrideConfig(cnsAssessmentObligation, null, cnsCourseNode.getIdent(), new Date());
		selectedAssessmentEntry.setObligation(selectedObligation);
		assessmentManager.updateAssessmentEntry(selectedAssessmentEntry);
	}

	@Override
	public Map<String, AssessmentEvaluation> getNodeIdentToAssessmentEvaluation(List<CourseNode> children) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		
		Map<String, CourseNode> nodeIdentToCourseNode = children.stream().collect(Collectors.toMap(CourseNode::getIdent, Function.identity()));
		
		List<AssessmentEntry> assessmentEntries = assessmentManager.getAssessmentEntries(assessedIdentity);
		Map<String, AssessmentEvaluation> nodeIdentToEvaluations = new HashMap<>(children.size());
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			CourseNode courseNode = nodeIdentToCourseNode.get(assessmentEntry.getSubIdent());
			if (courseNode != null) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				AssessmentEvaluation evaluation = AssessmentEvaluation.toAssessmentEvaluation(assessmentEntry, assessmentConfig);
				nodeIdentToEvaluations.put(courseNode.getIdent(), evaluation);
			}
		}
		
		return nodeIdentToEvaluations;
	}


}
