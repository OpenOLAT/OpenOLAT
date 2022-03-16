/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.course.assessment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Assessment test class.
 * 
 * <P>
 * Initial Date:  26.08.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class AssessmentManagerTest extends OlatTestCase  {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentManagerTest.class);
	
	private AssessmentManager assessmentManager;	
	private ICourse course;
	private CourseNode assessableCourseNode;
	private Identity tutor;
	private Identity student;
	private final Float score = Float.valueOf(10);
	private final Boolean passed = Boolean.TRUE;
	
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Before
	public void setUp() throws Exception {		
		try {
			log.info("setUp start ------------------------");
			
			Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("junit_auth");
			tutor = JunitTestHelper.createAndPersistIdentityAsRndUser("junit_tutor");
			student = JunitTestHelper.createAndPersistIdentityAsRndUser("junit_student");
			
			//import "Demo course" into the bcroot_junittest
			RepositoryEntry repositoryEntry = JunitTestHelper.deployDemoCourse(author);
			Long resourceableId = repositoryEntry.getOlatResource().getResourceableId();
			log.info("Demo course imported - resourceableId: " + resourceableId);
			
			repositoryService.addRole(student, repositoryEntry, "participant");
		
			course = CourseFactory.loadCourse(resourceableId);
			DBFactory.getInstance().closeSession();
						
			CourseConfig config = course.getCourseEnvironment().getCourseConfig();
			config.setEfficencyStatementIsEnabled(true);
			CourseFactory.setCourseConfig(course.getResourceableId(), config);
			
			course = CourseFactory.loadCourse(resourceableId);
			config = course.getCourseEnvironment().getCourseConfig();
			Assert.assertTrue(config.isEfficencyStatementEnabled());
			
			log.info("setUp done ------------------------");					
		} catch (RuntimeException e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	/**
	 * Tests the AssessmentManager methods.
	 *
	 */
	@Test
	public void testSaveScoreEvaluation() {
		log.info("Start testSaveScoreEvaluation");
		
		assertNotNull(course);
		//find an assessableCourseNode
		List<CourseNode> assessableNodeList = AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), null);
		Iterator<CourseNode> nodesIterator = assessableNodeList.iterator();
		boolean testNodeFound = false; 
		while(nodesIterator.hasNext()) {
			CourseNode currentNode = nodesIterator.next();			
			if (currentNode.getType().equalsIgnoreCase("iqtest")) {
				log.info("Yes, we found a test node! - currentNode.getType(): " + currentNode.getType());
				assessableCourseNode = currentNode;
				testNodeFound = true;
				break;
			}
		}
		assertTrue("found no test-node of type 'iqtest' (hint: add one to DemoCourse) ",testNodeFound);
				
		assessmentManager = course.getCourseEnvironment().getAssessmentManager();				
		
		Long assessmentID = Long.valueOf("123456");
		Integer attempts = 1;
		String coachComment = "SomeUselessCoachComment";
		String userComment = "UselessUserComment";
		
		//store ScoreEvaluation for the assessableCourseNode and student
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(score, null, null, passed, null, null, null, null, null, assessmentID);
    
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(student);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		boolean incrementAttempts = true;
		assessmentManager.saveScoreEvaluation(assessableCourseNode, tutor, student, scoreEvaluation, userCourseEnv, incrementAttempts, Role.coach);
		DBFactory.getInstance().closeSession();
		//the attempts mut have been incremented
		assertEquals(attempts, assessmentManager.getNodeAttempts(assessableCourseNode, student));
						
		assessmentManager.saveNodeCoachComment(assessableCourseNode, student, coachComment);
		
		assessmentManager.saveNodeComment(assessableCourseNode, tutor, student, userComment);
    
		attempts++;
		assessmentManager.saveNodeAttempts(assessableCourseNode, tutor, student, attempts, null, Role.coach);
		assertEquals(attempts, assessmentManager.getNodeAttempts(assessableCourseNode, student));    
		        
		assertEquals(score, assessmentManager.getNodeScore(assessableCourseNode, student));
		assertEquals(passed, assessmentManager.getNodePassed(assessableCourseNode, student));
		assertEquals(assessmentID, assessmentManager.getAssessmentID(assessableCourseNode, student));
		
		assertEquals(coachComment, assessmentManager.getNodeCoachComment(assessableCourseNode, student));
		assertEquals(userComment, assessmentManager.getNodeComment(assessableCourseNode, student));
							
		log.info("Finish testing AssessmentManager read/write methods");

		checkEfficiencyStatementManager();
		assertNotNull("no course at the end of test",course);
		try {
			course = CourseFactory.loadCourse(course.getResourceableId());
		} catch (Exception ex ) {
			fail("Could not load course at the end of test Exception=" + ex);
		}
		assertNotNull("no course after loadCourse", course);
	}
	
	/**
	 * This assumes that the student identity has scoreEvaluation information stored into the o_property
	 * for at least one test node into the "Demo course".
	 * It tests the EfficiencyStatementManager methods. 
	 *
	 */
	private void checkEfficiencyStatementManager() {
		log.info("Start testUpdateEfficiencyStatement");
					
		List <Identity> identitiyList = new ArrayList<> ();
		identitiyList.add(student);
		
		Long courseResId = course.getCourseEnvironment().getCourseResourceableId(); 
		RepositoryEntry courseRepositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(
				OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);
		assertNotNull(courseRepositoryEntry);
		// check the stored EfficiencyStatement		
		EfficiencyStatement efficiencyStatement = checkEfficiencyStatement(courseRepositoryEntry);
		//force the storing of the efficiencyStatement - this is usually done only at Learnresource/modify properties/Efficiency statement (ON)
		efficiencyStatementManager.updateEfficiencyStatements(courseRepositoryEntry, identitiyList);
		DBFactory.getInstance().closeSession();
						
		//archive the efficiencyStatement into a temporary dir
		try {
			File archiveDir = File.createTempFile("junit", "output");
			if(archiveDir.exists()) {
				archiveDir.delete();
				if(archiveDir.mkdir()) {
					ManifestBuilder manifest = ManifestBuilder.createBuilder();
					efficiencyStatementManager.export(student, manifest, archiveDir, Locale.GERMAN);
					log.info("Archived EfficiencyStatement path: " + archiveDir.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		
		//delete the efficiencyStatements for the current course
		efficiencyStatementManager.deleteEfficiencyStatementsFromCourse(courseRepositoryEntry.getKey());
		DBFactory.getInstance().closeSession();
		efficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementByCourseRepositoryEntry(courseRepositoryEntry, student);
		DBFactory.getInstance().closeSession();
		assertNull(efficiencyStatement);
		
		//updateUserEfficiencyStatement of the student identity
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(student);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		efficiencyStatementManager.updateUserEfficiencyStatement(userCourseEnv);
		DBFactory.getInstance().closeSession();
		//check again the stored EfficiencyStatement	
		efficiencyStatement = checkEfficiencyStatement(courseRepositoryEntry);
		
		//delete the efficiencyStatement of the student
		efficiencyStatementManager.deleteEfficientyStatement(student);
		DBFactory.getInstance().closeSession();
		efficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementByCourseRepositoryEntry(courseRepositoryEntry, student);
		DBFactory.getInstance().closeSession();
		assertNull(efficiencyStatement);
	}
	
	/**
	 * Asserts that the stored efficiencyStatement is not null and it contains the correct score/passed info.
	 * 
	 * @param courseRepositoryEntry
	 * @return
	 */
	private EfficiencyStatement checkEfficiencyStatement(RepositoryEntry courseRepositoryEntry) {
		//check the stored EfficiencyStatement		
		EfficiencyStatement efficiencyStatement = efficiencyStatementManager
				.getUserEfficiencyStatementByCourseRepositoryEntry(courseRepositoryEntry, student);
		assertNotNull(efficiencyStatement);
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		Iterator<Map<String,Object>> listIterator = assessmentNodes.iterator();
		while(listIterator.hasNext()) {
			Map<String,Object> assessmentMap = listIterator.next();
			if(assessmentMap.get(AssessmentHelper.KEY_IDENTIFYER).equals(assessableCourseNode.getIdent())) {
				String scoreString = (String) assessmentMap.get(AssessmentHelper.KEY_SCORE);				
				log.info("scoreString: " + scoreString);
				assertEquals(score, Float.valueOf(scoreString));				
			}
		}
		Double scoreDouble = efficiencyStatementManager.getScore(assessableCourseNode.getIdent(), efficiencyStatement);
		log.info("scoreDouble: " + scoreDouble);
		assertEquals(Double.valueOf(score),efficiencyStatementManager.getScore(assessableCourseNode.getIdent(), efficiencyStatement));
		assertEquals(passed,efficiencyStatementManager.getPassed(assessableCourseNode.getIdent(), efficiencyStatement));
		return efficiencyStatement;
	}
}
