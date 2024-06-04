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

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-08-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeAssertionDAOTest extends OlatTestCase {

	@Autowired
	private BadgeAssertionDAO badgeAssertionDAO;

	@Autowired
	private BadgeClassDAO badgeClassDAO;

	@Autowired
	private DB dbInstance;

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
	public void testCreateBadgeAssertion() {
		String uuid = OpenBadgesFactory.createIdentifier();
		String recipientObject = "{}";
		String verification = "{}";
		Date issuedOn = new Date();
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient");

		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass("Test badge", "image.svg", null);

		badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClassImpl, verification, issuedOn, recipient, null);

		BadgeAssertion badgeAssertion = badgeAssertionDAO.getAssertion(uuid);

		Assert.assertNotNull(badgeAssertion);
		Assert.assertEquals(uuid, badgeAssertion.getUuid());
		Assert.assertEquals(issuedOn, badgeAssertion.getIssuedOn());
		Assert.assertEquals(recipient, badgeAssertion.getRecipient());
		Assert.assertEquals(badgeClassImpl.getUuid(), badgeAssertion.getBadgeClass().getUuid());

		List<BadgeAssertion> badgeAssertions = badgeAssertionDAO.getBadgeAssertions(badgeClassImpl);

		Assert.assertEquals(1, badgeAssertions.size());
		Assert.assertEquals(uuid, badgeAssertions.get(0).getUuid());
	}

	@Test
	public void testReadBadgeAssertions() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("courseAuthor");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);

		BadgeClassImpl courseBadgeClass = BadgeTestData.createTestBadgeClass("Test badge with course", "image1.svg", courseEntry);
		BadgeClassImpl globalBadgeClass = BadgeTestData.createTestBadgeClass("Test badge without course", "image2.svg", null);

		String uuid1 = OpenBadgesFactory.createIdentifier();
		String uuid2 = OpenBadgesFactory.createIdentifier();
		String uuid3 = OpenBadgesFactory.createIdentifier();
		String uuid4 = OpenBadgesFactory.createIdentifier();
		Set<String> uuids = Set.of(uuid1, uuid2, uuid3, uuid4);
		String recipientObject = "{}";
		String verification = "{}";
		Date issuedOn = new Date();
		Identity recipient1 = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient1");
		Identity recipient2 = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient2");

		badgeAssertionDAO.createBadgeAssertion(uuid1, recipientObject, courseBadgeClass, verification, issuedOn, recipient1, null);
		badgeAssertionDAO.createBadgeAssertion(uuid2, recipientObject, courseBadgeClass, verification, issuedOn, recipient2, null);
		badgeAssertionDAO.createBadgeAssertion(uuid3, recipientObject, globalBadgeClass, verification, issuedOn, recipient1, null);
		badgeAssertionDAO.createBadgeAssertion(uuid4, recipientObject, globalBadgeClass, verification, issuedOn, recipient2, null);

		List<BadgeAssertion> allBadges = badgeAssertionDAO.getBadgeAssertions(null, null, true);
		Assert.assertEquals(4, allBadges.size());
		Assert.assertEquals(uuids, allBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));

		List<BadgeAssertion> allCourseBadges = badgeAssertionDAO.getBadgeAssertions(null, courseEntry, false);
		Assert.assertEquals(2, allCourseBadges.size());
		Assert.assertEquals(Set.of(uuid1, uuid2), allCourseBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));

		List<BadgeAssertion> allGlobalBadges = badgeAssertionDAO.getBadgeAssertions(null, null, false);
		Assert.assertEquals(2, allGlobalBadges.size());
		Assert.assertEquals(Set.of(uuid3, uuid4), allGlobalBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient1AllBadges = badgeAssertionDAO.getBadgeAssertions(recipient1, null, true);
		Assert.assertEquals(2, recipient1AllBadges.size());
		Assert.assertEquals(Set.of(uuid1, uuid3), recipient1AllBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient1.getKey()), recipient1AllBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient1CourseBadges = badgeAssertionDAO.getBadgeAssertions(recipient1, courseEntry, false);
		Assert.assertEquals(1, recipient1CourseBadges.size());
		Assert.assertEquals(Set.of(uuid1), recipient1CourseBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient1.getKey()), recipient1CourseBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient1GlobalBadges = badgeAssertionDAO.getBadgeAssertions(recipient1, null, false);
		Assert.assertEquals(1, recipient1GlobalBadges.size());
		Assert.assertEquals(Set.of(uuid3), recipient1GlobalBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient1.getKey()), recipient1GlobalBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient2AllBadges = badgeAssertionDAO.getBadgeAssertions(recipient2, null, true);
		Assert.assertEquals(2, recipient2AllBadges.size());
		Assert.assertEquals(Set.of(uuid2, uuid4), recipient2AllBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient2.getKey()), recipient2AllBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient2CourseBadges = badgeAssertionDAO.getBadgeAssertions(recipient2, courseEntry, false);
		Assert.assertEquals(1, recipient2CourseBadges.size());
		Assert.assertEquals(Set.of(uuid2), recipient2CourseBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient2.getKey()), recipient2CourseBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> recipient2GlobalBadges = badgeAssertionDAO.getBadgeAssertions(recipient2, null, false);
		Assert.assertEquals(1, recipient2GlobalBadges.size());
		Assert.assertEquals(Set.of(uuid4), recipient2GlobalBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));
		Assert.assertEquals(Set.of(recipient2.getKey()), recipient2GlobalBadges.stream().map(BadgeAssertion::getRecipient).map(IdentityRef::getKey).collect(Collectors.toSet()));

		List<BadgeAssertion> courseClassBadges = badgeAssertionDAO.getBadgeAssertions(courseBadgeClass);
		Assert.assertEquals(2, courseClassBadges.size());
		Assert.assertEquals(Set.of(uuid1, uuid2), courseClassBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));

		List<BadgeAssertion> globalClassBadges = badgeAssertionDAO.getBadgeAssertions(globalBadgeClass);
		Assert.assertEquals(2, globalClassBadges.size());
		Assert.assertEquals(Set.of(uuid3, uuid4), globalClassBadges.stream().map(BadgeAssertion::getUuid).collect(Collectors.toSet()));

		Assert.assertEquals(1, badgeAssertionDAO.getNumberOfBadgeAssertions(recipient1, courseBadgeClass).longValue());
		Assert.assertEquals(1, badgeAssertionDAO.getNumberOfBadgeAssertions(recipient1, globalBadgeClass).longValue());
		Assert.assertEquals(1, badgeAssertionDAO.getNumberOfBadgeAssertions(recipient2, courseBadgeClass).longValue());
		Assert.assertEquals(1, badgeAssertionDAO.getNumberOfBadgeAssertions(recipient2, globalBadgeClass).longValue());
	}

	@Test
	public void testRevokeBadgeAssertion() {
		String uuid = OpenBadgesFactory.createIdentifier();
		String recipientObject = "{}";
		String verification = "{}";
		Date issuedOn = new Date();
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser("badgeRecipient");

		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass("Test badge", "image.svg", null);
		BadgeAssertion badgeAssertion = badgeAssertionDAO.createBadgeAssertion(uuid, recipientObject, badgeClassImpl, verification, issuedOn, recipient, null);

		Assert.assertNotNull(badgeAssertion);
		Assert.assertEquals(uuid, badgeAssertion.getUuid());
		Assert.assertEquals(BadgeAssertion.BadgeAssertionStatus.issued, badgeAssertion.getStatus());

		badgeAssertionDAO.revokeBadgeAssertion(badgeAssertion.getKey());
		dbInstance.commitAndCloseSession();

		BadgeAssertion revokedReadBadgeAssertion = badgeAssertionDAO.getAssertion(uuid);

		Assert.assertNotNull(revokedReadBadgeAssertion);
		Assert.assertEquals(uuid, revokedReadBadgeAssertion.getUuid());
		Assert.assertEquals(badgeAssertion.getKey(), revokedReadBadgeAssertion.getKey());
		Assert.assertEquals(BadgeAssertion.BadgeAssertionStatus.revoked, revokedReadBadgeAssertion.getStatus());
	}
}
