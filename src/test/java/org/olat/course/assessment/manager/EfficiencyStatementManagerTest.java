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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.coach.CoachingLargeTest;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EfficiencyStatementManager effManager;
	
	/**
	 * Create and reload an efficiency statement.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testEfficiencyStatement() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-1");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();

		//load the efficiency statements
		List<UserEfficiencyStatementLight> statementsLight = effManager.findEfficiencyStatementsLight(participant);
		Assert.assertNotNull(statementsLight);
		Assert.assertEquals(1, statementsLight.size());
		UserEfficiencyStatementLight statementLight = statementsLight.get(0);
		Assert.assertEquals(statement.getKey(), statementLight.getKey());
		Assert.assertEquals(participant, statementLight.getIdentity());
		Assert.assertEquals(statement.getCourseRepoKey(), statementLight.getCourseRepoKey());
		Assert.assertEquals(re.getKey(), statementLight.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), statementLight.getShortTitle());
		Assert.assertEquals(re.getOlatResource(), statementLight.getResource());
		Assert.assertEquals(re.getOlatResource().getKey(), statementLight.getArchivedResourceKey());
		Assert.assertNotNull(statementLight.getCreationDate());
		Assert.assertNotNull(statementLight.getLastModified());
		Assert.assertTrue(statementLight.getPassed());
		Assert.assertEquals(6.0f, statementLight.getScore(), 0.00001);
	}
	
	
	@Test
	public void deleteUserData() throws URISyntaxException {
		RepositoryEntry re1 = deployTestcourse();
		RepositoryEntry re2 = deployTestcourse();
		
		//add some members
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Del-Part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Del-Part-2");
		repositoryService.addRole(participant1, re1, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re1, GroupRoles.participant.name());
		repositoryService.addRole(participant1, re2, GroupRoles.participant.name());
		repositoryService.addRole(participant2, re2, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement1_1 = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant1, re1.getOlatResource());
	    UserEfficiencyStatement statement1_2 = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant1, re2.getOlatResource());
	    UserEfficiencyStatement statement2_1 = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant2, re1.getOlatResource());
	    UserEfficiencyStatement statement2_2 = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant2, re2.getOlatResource());
		dbInstance.commitAndCloseSession();

		//load the efficiency statements
		List<UserEfficiencyStatementLight> statementsLight1 = effManager.findEfficiencyStatementsLight(participant1);
		Assert.assertEquals(2, statementsLight1.size());
		
		//delete user 1
		effManager.deleteEfficientyStatement(participant1);
		dbInstance.commitAndCloseSession();
		
		//check the efficiency statements
		List<UserEfficiencyStatementLight> deletedStatementsLight1 = effManager.findEfficiencyStatementsLight(participant1);
		Assert.assertTrue(deletedStatementsLight1.isEmpty());
		List<UserEfficiencyStatementLight> deletedStatementsLight2 = effManager.findEfficiencyStatementsLight(participant2);
		Assert.assertEquals(2, deletedStatementsLight2.size());
		
		//double check
		List<Identity> identitesRe1 = effManager.findIdentitiesWithEfficiencyStatements(re1.getKey());
		Assert.assertEquals(1, identitesRe1.size());
		Assert.assertTrue(identitesRe1.contains(participant2));
		List<Identity> identitesRe2 = effManager.findIdentitiesWithEfficiencyStatements(re2.getKey());
		Assert.assertEquals(1, identitesRe2.size());
		Assert.assertTrue(identitesRe2.contains(participant2));
		
		//triple check
		List<UserEfficiencyStatementLight> reloadStatemets_1_1 = effManager.findEfficiencyStatementsLight(Collections.<Long>singletonList(statement1_1.getKey()));
		Assert.assertTrue(reloadStatemets_1_1.isEmpty());
		List<UserEfficiencyStatementLight> reloadStatemets_1_2 = effManager.findEfficiencyStatementsLight(Collections.<Long>singletonList(statement1_2.getKey()));
		Assert.assertTrue(reloadStatemets_1_2.isEmpty());
		List<UserEfficiencyStatementLight> reloadStatemets_2_1 = effManager.findEfficiencyStatementsLight(Collections.<Long>singletonList(statement2_1.getKey()));
		Assert.assertEquals(1, reloadStatemets_2_1.size());
		List<UserEfficiencyStatementLight> reloadStatemets_2_2 = effManager.findEfficiencyStatementsLight(Collections.<Long>singletonList(statement2_2.getKey()));
		Assert.assertEquals(1, reloadStatemets_2_2.size());
	}
	
	@Test
	public void hasUserEfficiencyStatement() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Del-Part-3");
		Identity notParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Del-Part-4");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commit();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 6.0f, true, participant, re.getOlatResource());
	    dbInstance.commitAndCloseSession();
	    Assert.assertNotNull(statement);
	    
	    // has participant an efficiency statement
	    boolean hasOne = effManager.hasUserEfficiencyStatement(re.getKey(), participant);
	    Assert.assertTrue(hasOne);
	    boolean hasNot = effManager.hasUserEfficiencyStatement(re.getKey(), notParticipant);
	    Assert.assertFalse(hasNot);
	}
	
	private RepositoryEntry deployTestcourse() throws URISyntaxException {
		//deploy a course
		URL courseUrl = CoachingLargeTest.class.getResource("CoachingCourse.zip");
		RepositoryEntry re = JunitTestHelper.deployCourse(null, "Coaching course", courseUrl);// 4);
		Assert.assertNotNull(re);
		dbInstance.commitAndCloseSession();
		ICourse course = CourseFactory.loadCourse(re);			
		Assert.assertTrue(course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled());
		return re;
	}

}
