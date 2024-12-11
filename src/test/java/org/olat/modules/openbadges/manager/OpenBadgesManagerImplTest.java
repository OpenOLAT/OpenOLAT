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
package org.olat.modules.openbadges.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.OpenBadgesBakeContext;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.criteria.CourseElementPassedCondition;
import org.olat.modules.openbadges.criteria.CoursePassedCondition;
import org.olat.modules.openbadges.criteria.CoursesPassedCondition;
import org.olat.modules.openbadges.criteria.GlobalBadgesEarnedCondition;
import org.olat.modules.openbadges.criteria.OtherBadgeEarnedCondition;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.v2.Assertion;
import org.olat.modules.openbadges.v2.Badge;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NamedNodeMap;

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
public class OpenBadgesManagerImplTest extends OlatTestCase {

	@Autowired
	OpenBadgesManager openBadgesManager;

	@Autowired
	BadgeAssertionDAO badgeAssertionDAO;

	@Autowired
	BadgeClassDAO badgeClassDAO;

	@Autowired
	BadgeEntryConfigurationDAO badgeEntryConfigurationDAO;
	
	@Autowired
	DB dbInstance;
	
	@Autowired
	AssessmentEntryDAO assessmentEntryDAO;
	
	@Autowired
	GroupDAO groupDAO;

	@Autowired
	RepositoryEntryRelationDAO repositoryEntryRelationDAO;

	@After
	public void tearDown() throws Exception {
		List<BadgeAssertion> globalBadgeAssertions = badgeAssertionDAO.getBadgeAssertions(null, null, true);
		for (BadgeAssertion globalBadgeAssertion : globalBadgeAssertions) {
			badgeAssertionDAO.deleteBadgeAssertion(globalBadgeAssertion);
		}

		List<BadgeClass> globalBadgeClasses = badgeClassDAO.getBadgeClasses(null, false);
		for (BadgeClass globalBadgeClass : globalBadgeClasses) {
			badgeClassDAO.deleteBadgeClass(globalBadgeClass);
		}
	}

	@Test
	public void testBakePng() throws IOException {

		// Arrange

		String uuid = OpenBadgesFactory.createIdentifier();
		BadgeAssertion badgeAssertion = createBadgeAssertion(uuid);
		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		String jsonString = OpenBadgesManagerImpl.createBakedJsonString(badgeAssertion);
		String tmpFileName = uuid + ".png";

		InputStream inputStream = OpenBadgesManagerImplTest.class.getResourceAsStream("test_badge.png");
		IIOImage iioImage = OpenBadgesManagerImpl.readIIOImage(inputStream);
		IIOMetadata metadata = iioImage.getMetadata();

		// Act

		OpenBadgesManagerImpl.addNativePngTextEntry(metadata, "openbadges", jsonString);
		IIOImage bakedImage = new IIOImage(iioImage.getRenderedImage(), null, metadata);
		File tmpFile = new File(WebappHelper.getTmpDir(), tmpFileName);
		FileOutputStream fileOutputStream = new FileOutputStream(tmpFile, false);
		OpenBadgesManagerImpl.writeImageIOImage(bakedImage, fileOutputStream);

		// Assert

		InputStream bakedInputStream = new FileInputStream(tmpFile);
		NamedNodeMap attributes = OpenBadgesManagerImpl.extractAssertionJsonStringFromPng(bakedInputStream);

		Assert.assertNotNull(attributes);

		OpenBadgesBakeContext bakeContext = new OpenBadgesBakeContext(attributes);
		Assertion assertion = bakeContext.getTextAsAssertion();
		Badge badge = assertion.getBadge();

		Assert.assertNotNull(assertion);
		Assert.assertTrue(assertion.getId().endsWith(uuid));
		Assert.assertEquals(badgeClass.getSalt(), assertion.getRecipient().getSalt());
		Assert.assertTrue(badge.getImage().endsWith(badgeClass.getUuid()));
	}

	private BadgeAssertion createBadgeAssertion(String uuid) {
		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass("PNG badge", "image.png", null);
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient");
		String recipientObject = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeClassImpl.getSalt());
		String verification = "{\"type\":\"hosted\"}";
		Date issuedOn = new Date();

		BadgeAssertion badgeAssertion = badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClassImpl,
				verification, issuedOn, recipient, null);

		return badgeAssertion;
	}

	@Test
	public void testInsertingJsonIntoSvg() {
		String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
				"<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 200 201\" version=\"1.1\"\n" +
				"     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"\n" +
				"     xmlns:serif=\"http://www.serif.com/\"\n" +
				"     style=\"fill-rule:evenodd;clip-rule:evenodd;stroke-linecap:round;stroke-linejoin:round;stroke-miterlimit:1.5;\">\n" +
				"    <g transform=\"matrix(1,0,0,1,-651,-428)\">\n" +
				"    </g>\n" +
				"</svg>";

		String json = "{\n" +
				" \"@context\": \"https://w3id.org/openbadges/v2\",\n" +
				" \"id\": \"https://example.org/assertions/123\",\n" +
				" \"type\": \"Assertion\"\n" +
				"}";

		if (openBadgesManager instanceof OpenBadgesManagerImpl managerImpl) {
			String mergedSvg = managerImpl.mergeAssertionJson(svg, json, "https://test.openolat.org/badge/assertion/123");
			System.err.println(mergedSvg);
		}
	}

	@Test
	public void copyConfigurationAndBadgeClasses() {
		// arrange
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-class-author-1");
		RepositoryEntry sourceEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		BadgeClassImpl originalBadgeClass = BadgeTestData.createTestBadgeClass("Course badge", "image.png", sourceEntry);
		BadgeEntryConfiguration badgeEntryConfiguration = badgeEntryConfigurationDAO.createConfiguration(sourceEntry);
		badgeEntryConfiguration.setAwardEnabled(true);
		badgeEntryConfiguration.setOwnerCanAward(true);
		badgeEntryConfiguration.setCoachCanAward(true);
		badgeEntryConfigurationDAO.updateConfiguration(badgeEntryConfiguration);

		// act
		RepositoryEntry targetEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		openBadgesManager.copyConfigurationAndBadgeClasses(sourceEntry, targetEntry, author);

		// assert
		List<BadgeClass> originalBadgeClasses = badgeClassDAO.getBadgeClasses(sourceEntry);
		List<BadgeClass> copiedBadgeClasses = badgeClassDAO.getBadgeClasses(targetEntry);
		BadgeEntryConfiguration originalConfiguration = badgeEntryConfigurationDAO.getConfiguration(sourceEntry);
		BadgeEntryConfiguration copiedConfiguration = badgeEntryConfigurationDAO.getConfiguration(targetEntry);

		Assert.assertEquals(1, originalBadgeClasses.size());
		Assert.assertEquals(1, copiedBadgeClasses.size());
		Assert.assertNotNull(originalConfiguration);
		Assert.assertNotNull(copiedConfiguration);

		Assert.assertEquals(originalBadgeClass.getUuid(), originalBadgeClasses.get(0).getUuid());
		Assert.assertNotEquals(originalBadgeClass.getUuid(), copiedBadgeClasses.get(0).getUuid());
		Assert.assertEquals(originalBadgeClass.getName(), copiedBadgeClasses.get(0).getName());

		Assert.assertEquals(originalConfiguration.isAwardEnabled(), copiedConfiguration.isAwardEnabled());
		Assert.assertEquals(originalConfiguration.isCoachCanAward(), copiedConfiguration.isCoachCanAward());
		Assert.assertTrue(copiedConfiguration.isOwnerCanAward());
	}
	
	@Test
	public void getRuleEarnedBadgeAssertions() {
		
		// arrange
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("author-1");
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsRndUser("badge-assertion-recipient-1");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author);
		String verification = "{\"type\":\"hosted\"}";

		// badge A: directly related to course node A
		String courseNodeIdentA = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge A", "image.png", course);
		BadgeCriteria badgeCriteriaA = new BadgeCriteria();
		badgeCriteriaA.setAwardAutomatically(true);
		BadgeCondition badgeConditionA = new CourseElementPassedCondition(courseNodeIdentA);
		badgeCriteriaA.getConditions().add(badgeConditionA);
		badgeA.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaA));
		badgeClassDAO.updateBadgeClass(badgeA);
		String uuidA = OpenBadgesFactory.createIdentifier();
		String recipientObjectA = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeA.getSalt());
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.createBadgeAssertion(uuidA, recipientObjectA, badgeA,
				verification, new Date(), recipient, null);

		// badge B: indirectly related to course node A
		String courseNodeIdentB = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge B", "image.png", course);
		BadgeCriteria badgeCriteriaB = new BadgeCriteria();
		badgeCriteriaB.setAwardAutomatically(true);
		BadgeCondition badgeConditionB1 = new CourseElementPassedCondition(courseNodeIdentB);
		BadgeCondition badgeConditionB2 = new OtherBadgeEarnedCondition(badgeA.getUuid());
		badgeCriteriaB.getConditions().add(badgeConditionB1);
		badgeCriteriaB.getConditions().add(badgeConditionB2);
		badgeB.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaB));
		badgeClassDAO.updateBadgeClass(badgeB);
		String uuidB = OpenBadgesFactory.createIdentifier();
		String recipientObjectB = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeB.getSalt());
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.createBadgeAssertion(uuidB, recipientObjectB, badgeB,
				verification, new Date(), recipient, null);

		// badge C: not related to course node
		String courseNodeIdentC = OpenBadgesFactory.createIdentifier();
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge C", "image.png", course);
		BadgeCriteria badgeCriteriaC = new BadgeCriteria();
		badgeCriteriaC.setAwardAutomatically(true);
		BadgeCondition badgeConditionC = new CourseElementPassedCondition(courseNodeIdentC);
		badgeCriteriaC.getConditions().add(badgeConditionC);
		badgeC.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteriaC));
		badgeClassDAO.updateBadgeClass(badgeC);
		String uuidC = OpenBadgesFactory.createIdentifier();
		String recipientObjectC = OpenBadgesManagerImpl.createRecipientObject(recipient, badgeC.getSalt());
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.createBadgeAssertion(uuidC, recipientObjectC, badgeC,
				verification, new Date(), recipient, null);


		// act: 
		// 
		// Return the badge assertions that the recipient obtained and that are due to rules related to
		// course 'course' and course node 'courseNodeIdentA'.
		//
		List<BadgeAssertion> badgeAssertions = openBadgesManager.getRuleEarnedBadgeAssertions(recipient, course, courseNodeIdentA);
		
		// assert
		
		Assert.assertEquals(2, badgeAssertions.size());
		Assert.assertTrue(badgeAssertions.contains(badgeAssertionA));
		Assert.assertTrue(badgeAssertions.contains(badgeAssertionB));
		Assert.assertFalse(badgeAssertions.contains(badgeAssertionC));
	}
	
	@Test
	public void issueBadgeManually() {

		// arrange

		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badge-manually-doer-1");
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badge-manually-recipient-1");
		String badgeAUuid = OpenBadgesFactory.createIdentifier();

		BadgeClassImpl badgeD = BadgeTestData.createTestBadgeClass("Badge 1: D", "image.png", null);
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge 2: A", "image.png", null);
		BadgeClassImpl badgeE = BadgeTestData.createTestBadgeClass("Badge 3: E", "image.png", null);
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge 4: B", "image.png", null);
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge 5: C", "image.png", null);
		BadgeClassImpl badgeF = BadgeTestData.createTestBadgeClass("Badge 6: F", "image.png", null);	
		
		RepositoryEntry courseC = JunitTestHelper.deployBasicCourse(doer);
		passCourse(courseC, recipient);

		makeManual(badgeA);
		makeManual(badgeE);
		
		makeAutomaticAndGloballyDependent(badgeB, badgeA);

		makeAutomaticAndGloballyDependent(badgeC, courseC);
		makeAutomaticAndGloballyDependent(badgeC, badgeB);
		
		makeAutomaticAndGloballyDependent(badgeD, courseC);
		makeAutomaticAndGloballyDependent(badgeD, badgeC);
		makeAutomaticAndGloballyDependent(badgeD, badgeB);
		makeAutomaticAndGloballyDependent(badgeD, badgeA);
		makeAutomaticAndGloballyDependent(badgeF, badgeB);
		
		badgeF.setStatus(BadgeClass.BadgeClassStatus.revoked);
		badgeClassDAO.updateBadgeClass(badgeF);

		dbInstance.commit();
		
		// act:
		//
 		// Issue 'badgeA' manually. Due to down-stream badge dependencies, this should automatically issue another
		// badge B and another badge C.
		//

		openBadgesManager.issueBadgeManually(badgeAUuid, badgeA, recipient, doer);
		dbInstance.commit();
		
		// assert
		
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.getBadgeAssertion(recipient, badgeA);
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.getBadgeAssertion(recipient, badgeB);
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.getBadgeAssertion(recipient, badgeC);
		BadgeAssertion badgeAssertionD = badgeAssertionDAO.getBadgeAssertion(recipient, badgeD);
		BadgeAssertion badgeAssertionE = badgeAssertionDAO.getBadgeAssertion(recipient, badgeE);
		BadgeAssertion badgeAssertionF = badgeAssertionDAO.getBadgeAssertion(recipient, badgeF);
		List<BadgeAssertion> badgeAssertions = badgeAssertionDAO.getBadgeAssertions(recipient);
		
		Assert.assertNotNull(badgeAssertionA);
		Assert.assertNotNull(badgeAssertionB);
		Assert.assertNotNull(badgeAssertionC);
		Assert.assertNotNull(badgeAssertionD);
		Assert.assertNull(badgeAssertionE);
		Assert.assertNull(badgeAssertionF);
		Assert.assertEquals(4, badgeAssertions.size());
	}

	private void passCourse(RepositoryEntry course, Identity participant) {
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry assessmentEntry = assessmentEntryDAO.createAssessmentEntry(participant, null, 
				course, subIdent, true, null);
		assessmentEntry.setPassed(true);
		assessmentEntryDAO.updateAssessmentEntry(assessmentEntry);
	}

	private void makeManual(BadgeClassImpl badge) {
		BadgeCriteria badgeCriteria = new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(false);
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	private void makeAutomaticAndGloballyDependent(BadgeClassImpl badge, BadgeClassImpl badgeDependency) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new GlobalBadgesEarnedCondition(List.of(badgeDependency.getKey())));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}

	private void makeAutomaticAndGloballyDependent(BadgeClassImpl badge, RepositoryEntry course) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new CoursesPassedCondition(List.of(course.getKey())));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	@Test
	public void issueBadgesAutomatically() {
		
		// arrange:

		Identity ownerAB = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-owner-1");
		Identity ownerC = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-owner-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("issue-badges-automatically-participant");
		
		RepositoryEntry courseA = JunitTestHelper.deployBasicCourse(ownerAB);
		BadgeClassImpl badgeA = BadgeTestData.createTestBadgeClass("Badge A", "image.png", courseA);
		
		RepositoryEntry courseB = JunitTestHelper.deployBasicCourse(ownerAB);
		BadgeClassImpl badgeB = BadgeTestData.createTestBadgeClass("Badge B", "image.png", courseB);
		
		RepositoryEntry courseC = JunitTestHelper.deployBasicCourse(ownerC);
		BadgeClassImpl badgeC = BadgeTestData.createTestBadgeClass("Badge C", "image.png", courseC);
		
		Group group = groupDAO.createGroup();
		repositoryEntryRelationDAO.createRelation(group, courseA);
		repositoryEntryRelationDAO.createRelation(group, courseB);
		groupDAO.addMembershipOneWay(group, ownerAB, "owner");

		makeAutomaticOnPass(badgeA);
		makeAutomaticOnPass(badgeB);
		makeAutomaticOnPass(badgeC);
		makeAutomaticAndDependent(badgeA, badgeB);

		passCourse(courseA, participant);
		passCourse(courseB, participant);
		passCourse(courseC, participant);
		
		dbInstance.commit();
		
		// act:
		//
 		// Issue badges of 'courseA' automatically (simulating that courseA has been passed and that 
		// the openBadgesManager is called as a result of this). Should lead to a chain reaction.
		//

		openBadgesManager.issueBadgesAutomatically(courseA, ownerAB);
		dbInstance.commit();
		
		// assert:
		
		BadgeAssertion badgeAssertionA = badgeAssertionDAO.getBadgeAssertion(participant, badgeA);
		BadgeAssertion badgeAssertionB = badgeAssertionDAO.getBadgeAssertion(participant, badgeB);
		BadgeAssertion badgeAssertionC = badgeAssertionDAO.getBadgeAssertion(participant, badgeC);

		Assert.assertNotNull(badgeAssertionA);
		Assert.assertNotNull(badgeAssertionB);
		Assert.assertNull(badgeAssertionC);
	}

	private void makeAutomaticAndDependent(BadgeClassImpl badge, BadgeClassImpl badgeDependency) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new OtherBadgeEarnedCondition(badgeDependency.getUuid()));
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
	
	private void makeAutomaticOnPass(BadgeClassImpl badge) {
		BadgeCriteria badgeCriteria = StringHelper.containsNonWhitespace(badge.getCriteria()) ? BadgeCriteriaXStream.fromXml(badge.getCriteria()) : new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(true);
		badgeCriteria.getConditions().add(new CoursePassedCondition());
		badge.setCriteria(BadgeCriteriaXStream.toXml(badgeCriteria));
		badgeClassDAO.updateBadgeClass(badge);
	}
}
