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
package org.olat.course.assessment.manager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentHandler;
import org.olat.course.assessment.handler.NonAssessmentHandler;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseAssessmentServiceImpl implements CourseAssessmentService {
	
	private static final String NON_ASSESSMENT_TYPE = NonAssessmentHandler.NODE_TYPE;
	
	@Autowired
	private List<AssessmentHandler> loadedAssessmentHandlers;
	private Map<String, AssessmentHandler> assessmentHandlers = new HashMap<>();
	private AssessmentHandler nonAssessmentHandler;
	
	@PostConstruct
	void initProviders() {
		for (AssessmentHandler handler: loadedAssessmentHandlers) {
			if (NON_ASSESSMENT_TYPE.equals(handler.acceptCourseNodeType())) {
				nonAssessmentHandler = handler;
			} else {
				assessmentHandlers.put(handler.acceptCourseNodeType(), handler);
			}
		}
	}

	private  AssessmentHandler getAssessmentHandler(CourseNode courseNode) {
		AssessmentHandler handler = assessmentHandlers.get(courseNode.getType());
		if (handler == null) {
			handler = nonAssessmentHandler;
		}
		return handler;
	}

	@Override
	public AssessmentConfig getAssessmentConfig(CourseNode node) {
		return getAssessmentHandler(node).getAssessmentConfig(node);
	}
	
	@Override
	public void updateUserScoreEvaluation(CourseNode courseNode, ScoreEvaluation scoreEvaluation,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, boolean incrementAttempts, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.saveScoreEvaluation(courseNode, coachingIdentity, assessedIdentity, new ScoreEvaluation(scoreEvaluation),
				userCourseEnvironment, incrementAttempts, by);
	}

	@Override
	public ScoreCalculator getScoreCalculator(CourseNode courseNode) {
		return getAssessmentHandler(courseNode).getScoreCalculator(courseNode);
	}

	@Override
	public Double getUserCurrentRunCompletion(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeCurrentRunCompletion(courseNode, assessedIdentity);
	}
	
	@Override
	public void updateCurrentCompletion(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Identity identity, Double currentCompletion, AssessmentRunStatus runStatus, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateCurrentCompletion(courseNode, assessedIdentity, userCourseEnvironment, currentCompletion, runStatus,
				by);
	}

	@Override
	public Integer getUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		return am.getNodeAttempts(courseNode, assessedIdentity);
	}

	@Override
	public void incrementUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(courseNode, assessedIdentity, userCourseEnvironment, by);
	}

	@Override
	public void updateUserAttempts(CourseNode courseNode, Integer userAttempts,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role by) {
		if (userAttempts != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeAttempts(courseNode, coachingIdentity, assessedIdentity, userAttempts, by);
		}
	}

	@Override
	public String getUserComment(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeComment(courseNode, assessedIdentity);
	}

	@Override
	public void updatedUserComment(CourseNode courseNode, String userComment,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (userComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeComment(courseNode, coachingIdentity, assessedIdentity, userComment);
		}
	}

	@Override
	public String getCoachComment(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeCoachComment(courseNode, assessedIdentity);
	}

	@Override
	public void updateCoachComment(CourseNode courseNode, String coachComment,
			UserCourseEnvironment userCourseEnvironment) {
		if (coachComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeCoachComment(courseNode, assessedIdentity, coachComment);
		}
	}
	
	@Override
	public List<File> getIndividualAssessmentDocuments(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getIndividualAssessmentDocuments(courseNode, assessedIdentity);
	}

	@Override
	public void addIndividualAssessmentDocument(CourseNode courseNode, File document, String filename,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.addIndividualAssessmentDocument(courseNode, coachingIdentity, assessedIdentity, document, filename);
		}
	}

	@Override
	public void removeIndividualAssessmentDocument(CourseNode courseNode, File document,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.removeIndividualAssessmentDocument(courseNode, coachingIdentity, assessedIdentity, document);
		}
	}

	@Override
	public void updateLastModifications(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Identity identity, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateLastModifications(courseNode, assessedIdentity, userCourseEnvironment, by);
	}

	@Override
	public String getUserLog(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getUserNodeLog(courseNode, assessedIdentity);
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			UserCourseEnvironment assessedUserCourseEnvironment) {
		return getAssessmentHandler(courseNode).getDetailsEditController(ureq, wControl, stackPanel, courseNode,
				coachCourseEnv, assessedUserCourseEnvironment);
	}

	@Override
	public boolean hasCustomIdentityList(CourseNode courseNode) {
		return getAssessmentHandler(courseNode).hasCustomIdentityList();
	}

	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry, BusinessGroup group,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback) {
		return getAssessmentHandler(courseNode).getIdentityListController(ureq, wControl, stackPanel, courseNode,
				courseEntry, group, coachCourseEnv, toolContainer, assessmentCallback);
	}

}
