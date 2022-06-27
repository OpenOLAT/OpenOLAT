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
package org.olat.course.condition;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.STLearningPathConfigs;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GetPassedTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * Course (passed if ST1 and ST2 passed)
	 *   - SP
	 *   - ST1 (passed if ST11 passed)
	 *     - ST11 (passed if CHECKLIST passed)
	 *       - CHECKLIST
	 *   - ST2 (passed if MS passed)
	 *     - MS
	 */
	@Test
	public void shouldGetPassedHierarchically() {
		// Create course
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("author");
		RepositoryEntry courseEntry = JunitTestHelper.deployEmptyCourse(author, "Get Passed Hierarchically",
				RepositoryEntryStatusEnum.published);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		course.getCourseConfig().setNodeAccessType(ConditionNodeAccessProvider.TYPE);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		// Make the course runtime structure
		Structure runStructure = courseEnv.getRunStructure();
		STCourseNode root = (STCourseNode)runStructure.getRootNode();
		root.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		SPCourseNode sp_0_1 = new SPCourseNode();
		root.addChild(sp_0_1);
		// Structure 1
		STCourseNode st_1 = new STCourseNode();
		root.addChild(st_1);
		STCourseNode st_1_1 = new STCourseNode();
		st_1_1.getScoreCalculator().setExpertMode(true);
		st_1.addChild(st_1_1);
		CheckListCourseNode cl_1_1_1 = new CheckListCourseNode();
		cl_1_1_1.getModuleConfiguration().setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, true);
		st_1_1.addChild(cl_1_1_1);
		// Structure 2
		STCourseNode st_2 = new STCourseNode();
		st_2.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st_2);
		MSCourseNode ms_2_1 = new MSCourseNode();
		ms_2_1.getModuleConfiguration().setBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, true);
		st_2.addChild(ms_2_1);
		
		// Init the passed rules
		st_2.getScoreCalculator().setPassedExpression("(getPassed(\"" + ms_2_1.getIdent() +"\"))");
		st_1_1.getScoreCalculator().setPassedExpression("(getPassed(\"" + cl_1_1_1.getIdent() +"\"))");
		st_1.getScoreCalculator().setPassedExpression("(getPassed(\"" + st_1_1.getIdent() +"\"))");
		root.getScoreCalculator().setPassedExpression("(getPassed(\"" + st_1.getIdent() +"\") & getPassed(\"" + st_2.getIdent() + "\"))");
		
		// Add a participant to the course
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("participant");
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(participant);
		UserCourseEnvironmentImpl userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnv);
		userCourseEnv.setUserRoles(false, false, true);
		dbInstance.commitAndCloseSession();
		
		// Init the AssessmentAccounting
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getPassed()).as("root").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getPassed()).as("sp_0_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getPassed()).as("st_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1_1).getPassed()).as("sp_1_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(cl_1_1_1).getPassed()).as("cl_1_1_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getPassed()).as("st_2").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(ms_2_1).getPassed()).as("ms_2_1").isNull();
		softly.assertAll();
		
		// The participant passes cl_1_1_1
		setPassed(participant, courseEntry, cl_1_1_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getPassed()).as("root").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getPassed()).as("sp_0_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getPassed()).as("st_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1_1).getPassed()).as("sp_1_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(cl_1_1_1).getPassed()).as("cl_1_1_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getPassed()).as("st_2").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(ms_2_1).getPassed()).as("ms_2_1").isNull();
		softly.assertAll();
		
		// The participant passes ms_2_1
		setPassed(participant, courseEntry, ms_2_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getPassed()).as("root").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getPassed()).as("sp_0_1").isNull();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getPassed()).as("st_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(st_1_1).getPassed()).as("sp_1_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(cl_1_1_1).getPassed()).as("cl_1_1_1").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getPassed()).as("st_2").isTrue();
		softly.assertThat(scoreAccounting.evalCourseNode(ms_2_1).getPassed()).as("ms_2_1").isTrue();
		softly.assertAll();
	}

	private void setPassed(Identity identity, RepositoryEntry entry, CourseNode courseNode) {
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(identity, entry, courseNode.getIdent());
		assessmentEntry.setPassed(Boolean.TRUE);
		assessmentEntry.setUserVisibility(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(assessmentEntry);
		dbInstance.commitAndCloseSession();
	}

}
