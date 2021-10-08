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
package org.olat.course.run.scoring;

import java.util.Date;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.DateUtils;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.STLearningPathConfigs;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentAccountingTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private LearningPathService learningPathService;
	
	/**
	 * Course (sequential)
	 *   - SP
	 *   - ST (sequential)
	 *     - SP
	 *   - ST (without sequence)
	 *     - SP
	 *     - SP
	 *   - ST (sequential)
	 *     - SP
	 *     - SP
	 */
	@Test
	public void testSequentialStructures() {
		// Create course
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("author");
		RepositoryEntry courseEntry = JunitTestHelper.deployEmptyCourse(author, "Learning Path",
				RepositoryEntryStatusEnum.published, true, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		course.getCourseConfig().setNodeAccessType(LearningPathNodeAccessProvider.TYPE);
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
		st_1.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		root.addChild(st_1);
		SPCourseNode sp_1_1 = new SPCourseNode();
		st_1.addChild(sp_1_1);
		// Structure 2
		STCourseNode st_2 = new STCourseNode();
		st_2.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st_2);
		SPCourseNode sp_2_1 = new SPCourseNode();
		st_2.addChild(sp_2_1);
		SPCourseNode sp_2_2 = new SPCourseNode();
		st_2.addChild(sp_2_2);
		// Structure 3
		STCourseNode st_3 = new STCourseNode();
		st_3.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		root.addChild(st_3);
		SPCourseNode sp_3_1 = new SPCourseNode();
		st_3.addChild(sp_3_1);
		SPCourseNode sp_3_2 = new SPCourseNode();
		st_3.addChild(sp_3_2);
		
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
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertAll();
		
		// The participant executes sp_0_1
		setDone(participant, courseEntry, sp_0_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertAll();
		
		// The participant executes sp_1_1
		setDone(participant, courseEntry, sp_1_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertAll();
		
		// The participant executes sp_2_1
		setDone(participant, courseEntry, sp_2_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertAll();
		
		// The participant executes sp_2_2
		setDone(participant, courseEntry, sp_2_2);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertAll();
		
		// The participant executes sp_3_1
		setDone(participant, courseEntry, sp_3_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertAll();
		
		// The participant executes sp_3_2
		setDone(participant, courseEntry, sp_3_2);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
	}

	/**
	 * Course (without sequence)
	 *   - SP
	 *   - ST (without sequence)
	 *     - SP
	 *   - ST (sequential)
	 *     - SP
	 *     - SP
	 *   - ST (without sequence)
	 *     - SP
	 *     - SP
	 */
	@Test
	public void testWithoutSequenceStructures() {
		// Create course
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("author");
		RepositoryEntry courseEntry = JunitTestHelper.deployEmptyCourse(author, "Learning Path",
				RepositoryEntryStatusEnum.published, true, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		course.getCourseConfig().setNodeAccessType(LearningPathNodeAccessProvider.TYPE);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		// Make the course runtime structure
		Structure runStructure = courseEnv.getRunStructure();
		STCourseNode root = (STCourseNode)runStructure.getRootNode();
		root.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		SPCourseNode sp_0_1 = new SPCourseNode();
		root.addChild(sp_0_1);
		// Structure 1
		STCourseNode st_1 = new STCourseNode();
		st_1.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st_1);
		SPCourseNode sp_1_1 = new SPCourseNode();
		st_1.addChild(sp_1_1);
		// Structure 2
		STCourseNode st_2 = new STCourseNode();
		st_2.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		root.addChild(st_2);
		SPCourseNode sp_2_1 = new SPCourseNode();
		st_2.addChild(sp_2_1);
		SPCourseNode sp_2_2 = new SPCourseNode();
		st_2.addChild(sp_2_2);
		// Structure 3
		STCourseNode st_3 = new STCourseNode();
		st_3.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st_3);
		SPCourseNode sp_3_1 = new SPCourseNode();
		st_3.addChild(sp_3_1);
		SPCourseNode sp_3_2 = new SPCourseNode();
		st_3.addChild(sp_3_2);
		
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
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertAll();
		
		// The participant executes sp_1_1
		setDone(participant, courseEntry, sp_1_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertAll();
		
		// The participant executes sp_3_2
		setDone(participant, courseEntry, sp_3_2);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
		
		// The participant executes sp_3_1
		setDone(participant, courseEntry, sp_3_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
		
		// The participant executes sp_2_1
		setDone(participant, courseEntry, sp_2_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
		
		// The participant executes sp_0_1
		setDone(participant, courseEntry, sp_0_1);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
		
		// The participant executes sp_2_2
		setDone(participant, courseEntry, sp_2_2);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(st_3).getAssessmentStatus()).as("st_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_1).getAssessmentStatus()).as("sp_3_1").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_3_2).getAssessmentStatus()).as("sp_3_2").isEqualTo(AssessmentEntryStatus.done);
		softly.assertAll();
		
	}
	
	/**
	 * Course (without sequence)
	 *   - SP: no date
	 *   - ST: (without sequence)
	 *     - SP: no date
	 *     - SP: date
	 *     - SP: no date
	 *     - SP: date
	 *     - ST: without sequence
	 *       - SP: no date
	 *   - ST (sequential)
	 *     - SP: no date
	 *     - SP: date
	 *     - SP: no date
	 *     - ST: (without sequence)
	 *       - SP: no date
	 *   - SP: no date
	 */
	@Test
	public void shouldRespectStartDates() {
		// Create course
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("author");
		RepositoryEntry courseEntry = JunitTestHelper.deployEmptyCourse(author, "Learning Path",
				RepositoryEntryStatusEnum.published, true, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		course.getCourseConfig().setNodeAccessType(LearningPathNodeAccessProvider.TYPE);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		// Make the course runtime structure
		Structure runStructure = courseEnv.getRunStructure();
		STCourseNode root = (STCourseNode)runStructure.getRootNode();
		root.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		SPCourseNode sp_0_1 = new SPCourseNode();
		root.addChild(sp_0_1);
		// Structure 1
		STCourseNode st_1 = new STCourseNode();
		st_1.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st_1);
		SPCourseNode sp_1_1 = new SPCourseNode();
		st_1.addChild(sp_1_1);
		SPCourseNode sp_1_2 = new SPCourseNode();
		setStartInFuture(sp_1_2);
		st_1.addChild(sp_1_2);
		SPCourseNode sp_1_3 = new SPCourseNode();
		st_1.addChild(sp_1_3);
		SPCourseNode sp_1_4 = new SPCourseNode();
		setStartInFuture(sp_1_4);
		st_1.addChild(sp_1_4);
		// Structure 11
		STCourseNode st_11 = new STCourseNode();
		st_11.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		st_1.addChild(st_11);
		SPCourseNode sp_11_1 = new SPCourseNode();
		st_11.addChild(sp_11_1);
		// Structure 2
		STCourseNode st_2 = new STCourseNode();
		st_2.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		root.addChild(st_2);
		SPCourseNode sp_2_1 = new SPCourseNode();
		st_2.addChild(sp_2_1);
		SPCourseNode sp_2_2 = new SPCourseNode();
		setStartInFuture(sp_2_2);
		st_2.addChild(sp_2_2);
		SPCourseNode sp_2_3 = new SPCourseNode();
		st_2.addChild(sp_2_3);
		// Structure 21
		STCourseNode st_21 = new STCourseNode();
		st_21.getModuleConfiguration().setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY,
				STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		st_2.addChild(st_21);
		SPCourseNode sp_21_1 = new SPCourseNode();
		st_21.addChild(sp_21_1);
		//Structure 0 again
		SPCourseNode sp_0_2 = new SPCourseNode();
		root.addChild(sp_0_2);
		
		
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
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_2).getAssessmentStatus()).as("sp_1_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_3).getAssessmentStatus()).as("sp_1_3").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_4).getAssessmentStatus()).as("sp_1_4").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_11).getAssessmentStatus()).as("st_11").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_11_1).getAssessmentStatus()).as("sp_11_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_3).getAssessmentStatus()).as("sp_2_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_21).getAssessmentStatus()).as("st_21").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_21_1).getAssessmentStatus()).as("sp_21_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_2).getAssessmentStatus()).as("sp_0_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertAll();
		
		// The participant executes sp_1_3
		setDone(participant, courseEntry, sp_1_3);
		scoreAccounting.evaluateAll(true);
		dbInstance.commitAndCloseSession();
		
		softly = new SoftAssertions();
		softly.assertThat(scoreAccounting.evalCourseNode(root).getAssessmentStatus()).as("root").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_1).getAssessmentStatus()).as("sp_0_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_1).getAssessmentStatus()).as("st_1").isEqualTo(AssessmentEntryStatus.inProgress);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_1).getAssessmentStatus()).as("sp_1_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_2).getAssessmentStatus()).as("sp_1_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_3).getAssessmentStatus()).as("sp_1_3").isEqualTo(AssessmentEntryStatus.done);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_1_4).getAssessmentStatus()).as("sp_1_4").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_11).getAssessmentStatus()).as("st_11").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_11_1).getAssessmentStatus()).as("sp_11_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(st_2).getAssessmentStatus()).as("st_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_1).getAssessmentStatus()).as("sp_2_1").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_2).getAssessmentStatus()).as("sp_2_2").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_2_3).getAssessmentStatus()).as("sp_2_3").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(st_21).getAssessmentStatus()).as("st_21").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_21_1).getAssessmentStatus()).as("sp_21_1").isEqualTo(AssessmentEntryStatus.notReady);
		softly.assertThat(scoreAccounting.evalCourseNode(sp_0_2).getAssessmentStatus()).as("sp_0_2").isEqualTo(AssessmentEntryStatus.notStarted);
		softly.assertAll();
	}

	private void setDone(Identity identity, RepositoryEntry entry, SPCourseNode courseNode) {
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(identity, entry, courseNode.getIdent());
		assessmentEntry.setFullyAssessed(Boolean.TRUE);
		assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.done);
		assessmentService.updateAssessmentEntry(assessmentEntry);
		dbInstance.commitAndCloseSession();
	}

	private void setStartInFuture(CourseNode courseNode) {
		Date future = DateUtils.addDays(new Date(), 1);
		learningPathService.getConfigs(courseNode).setStartDate(future);
	}

}
