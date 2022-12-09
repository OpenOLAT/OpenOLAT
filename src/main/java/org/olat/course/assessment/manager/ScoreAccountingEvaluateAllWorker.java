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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.MultiUserObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreAccountingEvaluateAllWorker implements Runnable {

	private static final Logger log = Tracing.createLoggerFor(ScoreAccountingEvaluateAllWorker.class);
	
	private final Long courseResId;
	private final boolean update;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryService repositoryService;

	public ScoreAccountingEvaluateAllWorker(Long courseResId, boolean update) {
		this.courseResId = courseResId;
		this.update = update;
	}

	@Override
	public void run() {
		try {
			log.debug("Evaluate all score accountings for course {}", courseResId);
			CoreSpringFactory.autowireObject(this);
			evaluateAll();
			log.debug("Evaluate all score accountings successfull for course {}", courseResId);
		} catch (Exception e) {
			log.error("Evaluate all score accountings failed for course " + courseResId, e);
			dbInstance.rollbackAndCloseSession();
		}
	}
	
	private void evaluateAll() {
		ICourse course = CourseFactory.loadCourse(courseResId);
		if (course == null) return;
		
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		CoursePropertyManager pm = courseEnv.getCoursePropertyManager();
		MultiUserObligationContext obligationContext = new MultiUserObligationContext();
		
		Set<Identity> identities = new HashSet<>();
		List<Identity> assessedIdentities = pm.getAllIdentitiesWithCourseAssessmentData(null);
		identities.addAll(assessedIdentities);
		List<Identity> members = repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		identities.addAll(members);
		
		identities.forEach(identity -> tryEvaluateAll(course, courseEnv, obligationContext, identity));
	}
	
	private void tryEvaluateAll(ICourse course, CourseEnvironment courseEnv,
			MultiUserObligationContext obligationContext, Identity identity) {
		try {
			evaluateAll(courseEnv, obligationContext, identity);
			log.debug("Evaluated score accounting in {} for {}", course, identity);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.warn("Evaluated score accounting failed in {} for {}", course, identity);
			dbInstance.rollbackAndCloseSession();
		}
	}

	private void evaluateAll(CourseEnvironment courseEnv, MultiUserObligationContext obligationContext, Identity assessedIdentity) {
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(assessedIdentity);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnv);
		
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		AssessmentEntry rootAssessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, courseEntry, rootNode.getIdent());
		Boolean previousPassed = rootAssessmentEntry != null
				? rootAssessmentEntry.getPassedOverridable().getCurrent()
				: null;
		
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		scoreAccounting.setObligationContext(obligationContext);
		scoreAccounting.evaluateAll(update);
		
		AssessmentEvaluation rootAssessmentEvaluation = scoreAccounting.evalCourseNode(rootNode);
		Boolean currentPassed = rootAssessmentEvaluation.getPassed();
		
		// Save root score evaluation to propagate to efficiency statement
		if (!Objects.equals(previousPassed, currentPassed)) {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			am.saveScoreEvaluation(rootNode, null, assessedIdentity, rootAssessmentEvaluation, userCourseEnv, false, null);
		}
	}

}
