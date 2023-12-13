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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class BadgeClassDAOTest extends OlatTestCase {

	@Autowired
	private BadgeClassDAO badgeClassDAO;

	private RepositoryEntry courseEntry;


	@Before
	public void setUp() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("badge-class-author");
		courseEntry = JunitTestHelper.deployBasicCourse(author);
	}

	@After
	public void tearDown() throws Exception {
		List<BadgeClass> globalBadgeClasses = badgeClassDAO.getBadgeClasses(null, false);
		for (BadgeClass globalBadgeClass : globalBadgeClasses) {
			badgeClassDAO.deleteBadgeClass(globalBadgeClass);
		}

		List<BadgeClass> courseBadgeClasses = badgeClassDAO.getBadgeClasses(courseEntry, false);
		for (BadgeClass courseBadgeClass : courseBadgeClasses) {
			badgeClassDAO.deleteBadgeClass(courseBadgeClass);
		}
	}

	@Test
	public void testCreateBadgeClass() {
		BadgeClassImpl badgeClassImpl = createTestBadgeClass("Test badge", "image.svg", null);

		BadgeClass badgeClassByUuid = badgeClassDAO.getBadgeClass(badgeClassImpl.getUuid());

		Assert.assertNotNull(badgeClassByUuid);
		Assert.assertEquals(badgeClassImpl.getKey(), badgeClassByUuid.getKey());
		Assert.assertEquals(badgeClassImpl.getUuid(), badgeClassByUuid.getUuid());
		Assert.assertEquals(badgeClassImpl.getVersion(), badgeClassByUuid.getVersion());
		Assert.assertEquals(badgeClassImpl.getSalt(), badgeClassByUuid.getSalt());
		Assert.assertEquals(BadgeClass.BadgeClassStatus.preparation, badgeClassByUuid.getStatus());
	}

	private BadgeClassImpl createTestBadgeClass(String name, String image, RepositoryEntry entry) {
		BadgeClassImpl badgeClassImpl = BadgeTestData.createTestBadgeClass(name, image, entry);
		badgeClassDAO.createBadgeClass(badgeClassImpl);
		return badgeClassImpl;
	}

	@Test
	public void testGetBadgeClasses() {
		BadgeClassImpl badgeClass1 = createTestBadgeClass("Test 1", "image1.svg", null);
		BadgeClassImpl badgeClass2 = createTestBadgeClass("Test 2", "image2.svg", null);
		BadgeClassImpl badgeClass3 = createTestBadgeClass("Test 3", "image3.svg", courseEntry);

		List<BadgeClass> globalBadgeClasses = badgeClassDAO.getBadgeClasses(null);
		Set<String> globalBadgeClassUuids = globalBadgeClasses.stream().map(BadgeClass::getUuid).collect(Collectors.toSet());
		Long nbGlobalBadgeClasses = badgeClassDAO.getNumberOfBadgeClasses(null);

		List<BadgeClass> courseBadgeClasses = badgeClassDAO.getBadgeClasses(courseEntry);
		Set<String> courseBadgeClassUuids = courseBadgeClasses.stream().map(BadgeClass::getUuid).collect(Collectors.toSet());
		Long nbCourseBadgeClasses = badgeClassDAO.getNumberOfBadgeClasses(courseEntry);

		Assert.assertTrue(globalBadgeClassUuids.contains(badgeClass1.getUuid()));
		Assert.assertTrue(globalBadgeClassUuids.contains(badgeClass2.getUuid()));
		Assert.assertFalse(globalBadgeClassUuids.contains(badgeClass3.getUuid()));
		Assert.assertEquals(2, (long) nbGlobalBadgeClasses);

		Assert.assertFalse(courseBadgeClassUuids.contains(badgeClass1.getUuid()));
		Assert.assertFalse(courseBadgeClassUuids.contains(badgeClass2.getUuid()));
		Assert.assertTrue(courseBadgeClassUuids.contains(badgeClass3.getUuid()));
		Assert.assertEquals(1, (long) nbCourseBadgeClasses);
	}

	@Test
	public void testGetBadgeClassesWithUseCounts() {
		BadgeClassImpl badgeClass1 = createTestBadgeClass("Test 1", "image1.svg", null);
		BadgeClassImpl badgeClass2 = createTestBadgeClass("Test 2", "image2.svg", null);
		BadgeClassImpl badgeClass3 = createTestBadgeClass("Test 3", "image3.svg", courseEntry);

		List<BadgeClassDAO.BadgeClassWithUseCount> globalItems = badgeClassDAO.getBadgeClassesWithUseCounts(null);
		List<BadgeClassDAO.BadgeClassWithUseCount> courseItems = badgeClassDAO.getBadgeClassesWithUseCounts(courseEntry);

		Assert.assertEquals(2, globalItems.size());
		Assert.assertEquals(0, (long) globalItems.get(0).getUseCount());
		Assert.assertEquals(0, (long) globalItems.get(1).getUseCount());
		Assert.assertEquals(badgeClass1.getNameWithScan(), globalItems.get(0).getBadgeClass().getNameWithScan());
		Assert.assertEquals(badgeClass2.getNameWithScan(), globalItems.get(1).getBadgeClass().getNameWithScan());
		Assert.assertEquals(1, courseItems.size());
		Assert.assertEquals(0, (long) courseItems.get(0).getUseCount());
		Assert.assertEquals(badgeClass3.getNameWithScan(), courseItems.get(0).getBadgeClass().getNameWithScan());
	}


	@Test
	public void testUpdateBadgeClass() {
		BadgeClassImpl badgeClass1 = createTestBadgeClass("Test 1", "image1.svg", null);

		BadgeClass badgeClass1Update = badgeClassDAO.getBadgeClass(badgeClass1.getUuid());
		badgeClass1Update.setStatus(BadgeClass.BadgeClassStatus.deleted);
		badgeClass1Update.setNameWithScan("Test 1 (edited)");
		badgeClass1Update.setDescriptionWithScan("Test 1 description (edited)");

		badgeClassDAO.updateBadgeClass(badgeClass1Update);

		BadgeClass badgeClass1Test = badgeClassDAO.getBadgeClass(badgeClass1.getUuid());

		Assert.assertNotNull(badgeClass1Test);
		Assert.assertEquals(BadgeClass.BadgeClassStatus.deleted, badgeClass1Test.getStatus());
		Assert.assertEquals("Test 1 (edited)", badgeClass1Test.getNameWithScan());
		Assert.assertEquals("Test 1 description (edited)", badgeClass1Test.getDescriptionWithScan());
	}

	@Test
	public void testDeleteBadgeClass() {
		BadgeClassImpl badgeClass1 = createTestBadgeClass("Test 1", "image1.svg", null);
		BadgeClassImpl badgeClass2 = createTestBadgeClass("Test 2", "image2.svg", null);

		badgeClassDAO.deleteBadgeClass(badgeClass1);

		BadgeClass badgeClass1Test = badgeClassDAO.getBadgeClass(badgeClass1.getUuid());
		BadgeClass badgeClass2Test = badgeClassDAO.getBadgeClass(badgeClass2.getUuid());

		Assert.assertNull(badgeClass1Test);
		Assert.assertNotNull(badgeClass2Test);
		Assert.assertEquals(badgeClass2.getNameWithScan(), badgeClass2Test.getNameWithScan());
	}
}