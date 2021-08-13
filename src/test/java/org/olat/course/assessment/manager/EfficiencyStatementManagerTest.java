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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.MailPackage;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.UserEfficiencyStatementForCoaching;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * Create and reload an efficiency statement.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void createEfficiencyStatement() throws URISyntaxException {
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
	public void getUserEfficiencyStatementLightByKey() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-4");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 4.5f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		UserEfficiencyStatementLight reloadedStatement = effManager.getUserEfficiencyStatementLightByKey(statement.getKey());
		Assert.assertNotNull(reloadedStatement);
		Assert.assertEquals(statement.getKey(), reloadedStatement.getKey());
		Assert.assertEquals(participant, reloadedStatement.getIdentity());
		Assert.assertEquals(re.getKey(), reloadedStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), reloadedStatement.getShortTitle());
		Assert.assertEquals(re.getOlatResource(), reloadedStatement.getResource());
		Assert.assertEquals(re.getOlatResource().getKey(), reloadedStatement.getArchivedResourceKey());
		Assert.assertNotNull(reloadedStatement.getCreationDate());
		Assert.assertNotNull(reloadedStatement.getLastModified());
		Assert.assertFalse(reloadedStatement.getPassed());
		Assert.assertEquals(4.5f, reloadedStatement.getScore(), 0.00001);
	}
	
	@Test
	public void getUserEfficiencyStatementByKey() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 4.5f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		effManager.updateEfficiencyStatements(re, Collections.singletonList(participant));
		
		EfficiencyStatement effStatement = effManager.getUserEfficiencyStatementByKey(statement.getKey());
		Assert.assertNotNull(effStatement);
		Assert.assertEquals(course.getCourseTitle(), effStatement.getCourseTitle());
		Assert.assertEquals(re.getKey(), effStatement.getCourseRepoEntryKey());
	}
	
	@Test
	public void getUserEfficiencyStatementFull() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		UserEfficiencyStatementImpl fullStatement = effManager.getUserEfficiencyStatementFull(re, participant);
		Assert.assertNotNull(fullStatement);
		Assert.assertEquals(statement.getKey(), fullStatement.getKey());
		Assert.assertEquals(participant, fullStatement.getIdentity());
		Assert.assertEquals(re.getKey(), fullStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), fullStatement.getShortTitle());
		Assert.assertEquals(re.getOlatResource(), fullStatement.getResource());
		Assert.assertNotNull(fullStatement.getCreationDate());
		Assert.assertNotNull(fullStatement.getLastModified());
		Assert.assertFalse(fullStatement.getPassed());
		Assert.assertEquals(3.75f, fullStatement.getScore(), 0.00001);
	}
	
	@Test
	public void getUserEfficiencyStatementLightByRepositoryEntry() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		UserEfficiencyStatement lightStatement = effManager.getUserEfficiencyStatementLightByRepositoryEntry(re, participant);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), lightStatement.getShortTitle());
		Assert.assertNotNull(lightStatement.getCreationDate());
		Assert.assertNotNull(lightStatement.getLastModified());
		Assert.assertFalse(lightStatement.getPassed());
		Assert.assertEquals(3.75f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void getUserEfficiencyStatementLight_identityRepo() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatement> lightStatements = effManager.getUserEfficiencyStatementLight(participant, Collections.singletonList(re));
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatement lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), lightStatement.getShortTitle());
		Assert.assertNotNull(lightStatement.getCreationDate());
		Assert.assertNotNull(lightStatement.getLastModified());
		Assert.assertFalse(lightStatement.getPassed());
		Assert.assertEquals(3.75f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void getUserEfficiencyStatementLight_identity() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-6");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatement> lightStatements = effManager.getUserEfficiencyStatementLight(participant);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatement lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
	}
	
	@Test
	public void getUserEfficiencyStatementForCoaching_repo() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatementForCoaching> lightStatements = effManager.getUserEfficiencyStatementForCoaching(re);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatementForCoaching lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant.getKey(), lightStatement.getIdentityKey());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
	}
	
	@Test
	public void getUserEfficiencyStatementForCoaching_repos() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatementForCoaching> lightStatements = effManager.getUserEfficiencyStatementForCoaching(re);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatementForCoaching lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant.getKey(), lightStatement.getIdentityKey());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
	}
	
	@Test
	public void getUserEfficiencyStatementForCoaching_group() throws URISyntaxException {
		RepositoryEntry re1 = deployTestcourse();
		RepositoryEntry re2 = deployTestcourse();
		
		//add some members
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7o");
		Identity participantRe = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		repositoryService.addRole(participantRe, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		BusinessGroup group = businessGroupService
				.createBusinessGroup(author, "gcoach", "Group coaching", BusinessGroup.BUSINESS_TYPE, null, null, 0, 15, false, false, re1);
		businessGroupService.addResourceTo(group, re2);
		
		Identity participantGrp1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7a");
		Identity participantGrp2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7b");
		Identity participantGrp3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7c");
		
		List<Identity> participantsGroup = new ArrayList<>();
		participantsGroup.add(participantGrp1);
		participantsGroup.add(participantGrp2);
		participantsGroup.add(participantGrp3);
		businessGroupService.addParticipants(author, Roles.administratorRoles(), participantsGroup, group, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement1 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantRe, re1.getOlatResource());
	    UserEfficiencyStatement statement2 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp1, re1.getOlatResource());
	    UserEfficiencyStatement statement3 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp2, re1.getOlatResource());
	    UserEfficiencyStatement statement4 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp3, re1.getOlatResource());
	    UserEfficiencyStatement statement5 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp1, re2.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatementForCoaching> lightStatements = effManager.getUserEfficiencyStatementForCoaching(group);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(4, lightStatements.size());
		List<Long> lightStatementKeys = lightStatements.stream()
				.map(UserEfficiencyStatementForCoaching::getKey)
				.collect(Collectors.toList());
		Assert.assertFalse(lightStatementKeys.contains(statement1.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement2.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement3.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement4.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement5.getKey()));
	}
	
	/**
	 * Test with 2 courses and 2 groups with people as participants across
	 * courses and groups.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void getUserEfficiencyStatementForCoaching_group_complex() throws URISyntaxException {
		RepositoryEntry re1 = deployTestcourse();
		RepositoryEntry re2 = deployTestcourse();
		
		//add some members
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7o");
		Identity participantRe = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		repositoryService.addRole(participantRe, re1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		BusinessGroup group1 = businessGroupService
				.createBusinessGroup(author, "gcoach-1", "Group coaching", BusinessGroup.BUSINESS_TYPE,
						null, null, 0, 15, false, false, re1);
		businessGroupService.addResourceTo(group1, re2);
		
		BusinessGroup group2 = businessGroupService
				.createBusinessGroup(author, "gcoach-2", "Group coaching", BusinessGroup.BUSINESS_TYPE,
						null, null, 0, 15, false, false, re1);
		businessGroupService.addResourceTo(group2, re2);
		
		Identity participantGrp1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7a");
		Identity participantGrp2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7b");
		Identity participantGrp3 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7c");
		
		List<Identity> participantsGroup1 = new ArrayList<>();
		participantsGroup1.add(participantGrp1);
		participantsGroup1.add(participantGrp2);
		participantsGroup1.add(participantGrp3);
		businessGroupService.addParticipants(author, Roles.administratorRoles(), participantsGroup1, group1, new MailPackage(false));
		
		Identity participantGrp4 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7a");
		Identity participantGrp5 = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7b");
		
		List<Identity> participantsGroup2 = new ArrayList<>();
		participantsGroup2.add(participantGrp1);
		participantsGroup2.add(participantGrp4);
		participantsGroup2.add(participantGrp5);
		businessGroupService.addParticipants(author, Roles.administratorRoles(), participantsGroup2, group2, new MailPackage(false));
		
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement1 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantRe, re1.getOlatResource());
	    UserEfficiencyStatement statement2 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp1, re1.getOlatResource());
	    UserEfficiencyStatement statement3 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp2, re1.getOlatResource());
	    UserEfficiencyStatement statement4 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp3, re1.getOlatResource());
	    UserEfficiencyStatement statement5 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp1, re2.getOlatResource());
	    // noise
	    UserEfficiencyStatement statement6 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp4, re1.getOlatResource());
	    UserEfficiencyStatement statement7 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp5, re1.getOlatResource());
	    UserEfficiencyStatement statement8 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp4, re2.getOlatResource());
	    UserEfficiencyStatement statement9 = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participantGrp5, re2.getOlatResource());  
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatementForCoaching> lightStatements = effManager.getUserEfficiencyStatementForCoaching(group1);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(4, lightStatements.size());
		List<Long> lightStatementKeys = lightStatements.stream()
				.map(UserEfficiencyStatementForCoaching::getKey)
				.collect(Collectors.toList());
		Assert.assertFalse(lightStatementKeys.contains(statement1.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement2.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement3.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement4.getKey()));
		Assert.assertTrue(lightStatementKeys.contains(statement5.getKey()));
		Assert.assertFalse(lightStatementKeys.contains(statement6.getKey()));
		Assert.assertFalse(lightStatementKeys.contains(statement7.getKey()));
		Assert.assertFalse(lightStatementKeys.contains(statement8.getKey()));
		Assert.assertFalse(lightStatementKeys.contains(statement9.getKey()));
	}
	
	@Test
	public void getUserEfficiencyStatementLightByResource() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 3.75f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		
		UserEfficiencyStatement lightStatement = effManager.getUserEfficiencyStatementLightByResource(re.getOlatResource().getKey(), participant);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
	}
	
	@Test
	public void getUserEfficiencyStatementLightByResource_standalone() throws URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-7");
		dbInstance.commitAndCloseSession();
		
		Long resourceKey = 725l;

		//make statements
	    UserEfficiencyStatement statement = effManager.createStandAloneUserEfficiencyStatement(new Date(), 22.0f, Boolean.TRUE,
	    		null, null, null, null, participant, resourceKey, "Hello");
		dbInstance.commitAndCloseSession();
		
		UserEfficiencyStatement lightStatement = effManager.getUserEfficiencyStatementLightByResource(resourceKey, participant);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertNull(lightStatement.getCourseRepoKey());
		Assert.assertEquals("Hello", lightStatement.getShortTitle());
		Assert.assertTrue(lightStatement.getPassed());
		Assert.assertEquals(22.0f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void findEfficiencyStatements() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 4.5f, false, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		effManager.updateEfficiencyStatements(re, Collections.singletonList(participant));
			
		List<EfficiencyStatement> effStatements = effManager.findEfficiencyStatements(participant);
		Assert.assertNotNull(effStatements);
		Assert.assertEquals(1, effStatements.size());
		EfficiencyStatement effStatement = effStatements.get(0);
		Assert.assertNotNull(effStatement);
		Assert.assertEquals(course.getCourseTitle(), effStatement.getCourseTitle());
		Assert.assertEquals(re.getKey(), effStatement.getCourseRepoEntryKey());
		Assert.assertEquals(statement.getCourseRepoKey(), effStatement.getCourseRepoEntryKey());
	}
	
	@Test
	public void findEfficiencyStatementsLight_standalone() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-9");
		dbInstance.commitAndCloseSession();
		
		Long resourceKey = 725l;

		//make statements
	    UserEfficiencyStatement statement = effManager.createStandAloneUserEfficiencyStatement(new Date(), 22.0f, Boolean.TRUE,
	    		null, null, null, null, participant, resourceKey, "Hello");
		dbInstance.commitAndCloseSession();
		
		List<UserEfficiencyStatementLight> lightStatements = effManager.findEfficiencyStatementsLight(participant);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatementLight lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertNull(lightStatement.getCourseRepoKey());
		Assert.assertEquals("Hello", lightStatement.getShortTitle());
		Assert.assertTrue(lightStatement.getPassed());
		Assert.assertEquals(22.0f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void findEfficiencyStatementsLight_identity() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 102.3f, true, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		// this will reset score to 0 and passed to false
		effManager.updateEfficiencyStatements(re, Collections.singletonList(participant));
		
		List<UserEfficiencyStatementLight> lightStatements = effManager.findEfficiencyStatementsLight(participant);
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatementLight lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), lightStatement.getShortTitle());
		Assert.assertFalse(lightStatement.getPassed());
		Assert.assertEquals(0f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void findEfficiencyStatementsLight_statementKeys() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		ICourse course = CourseFactory.loadCourse(re);
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 102.3f, true, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		// this will reset score to 0 and passed to false
		effManager.updateEfficiencyStatements(re, Collections.singletonList(participant));
		
		List<UserEfficiencyStatementLight> lightStatements = effManager
				.findEfficiencyStatementsLight(Collections.singletonList(statement.getKey()));
		Assert.assertNotNull(lightStatements);
		Assert.assertEquals(1, lightStatements.size());
		UserEfficiencyStatementLight lightStatement = lightStatements.get(0);
		Assert.assertNotNull(lightStatement);
		Assert.assertEquals(statement.getKey(), lightStatement.getKey());
		Assert.assertEquals(participant, lightStatement.getIdentity());
		Assert.assertEquals(re.getKey(), lightStatement.getCourseRepoKey());
		Assert.assertEquals(course.getCourseTitle(), lightStatement.getShortTitle());
		Assert.assertFalse(lightStatement.getPassed());
		Assert.assertEquals(0f, lightStatement.getScore(), 0.00001);
	}
	
	@Test
	public void findIdentitiesWithEfficiencyStatements() throws URISyntaxException {
		RepositoryEntry re = deployTestcourse();
		
		//add some members
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Eff-Part-5");
		repositoryService.addRole(participant, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		//make statements
	    UserEfficiencyStatement statement = effManager.createUserEfficiencyStatement(new Date(), 102.3f, true, participant, re.getOlatResource());
		dbInstance.commitAndCloseSession();
		// this will reset score to 0 and passed to false
		effManager.updateEfficiencyStatements(re, Collections.singletonList(participant));
		
		List<Identity> assessedIdentities = effManager.findIdentitiesWithEfficiencyStatements(re.getKey());
		Assert.assertNotNull(assessedIdentities);
		Assert.assertEquals(1, assessedIdentities.size());
		Assert.assertEquals(statement.getIdentity(), assessedIdentities.get(0));
		Assert.assertEquals(participant, assessedIdentities.get(0));
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
