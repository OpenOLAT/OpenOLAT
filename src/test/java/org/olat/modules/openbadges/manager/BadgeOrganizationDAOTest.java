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

import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.test.OlatTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeOrganizationDAOTest extends OlatTestCase {

	@Autowired
	private BadgeOrganizationDAO badgeOrganizationDao;

	@Test
	public void createLoadBadgeOrganization() {
		BadgeOrganization otherBadgeOrganization = badgeOrganizationDao.createBadgeOrganization(
				BadgeOrganization.BadgeOrganizationType.other,
				"otherKey",
				"otherValue"
		);

		BadgeOrganization linkedInBadgeOrganization = badgeOrganizationDao.createBadgeOrganization(
				BadgeOrganization.BadgeOrganizationType.linkedInOrganization,
				"linkedInKey",
				"linkedValue"
		);

		List<BadgeOrganization> otherOrganizations = badgeOrganizationDao.loadBadgeOrganizations(
				BadgeOrganization.BadgeOrganizationType.other
		);

		List<BadgeOrganization> linkedInOrganizations = badgeOrganizationDao.loadBadgeOrganizations(
				BadgeOrganization.BadgeOrganizationType.linkedInOrganization
		);

		Assert.assertEquals(1, otherOrganizations.size());
		Assert.assertEquals(1, linkedInOrganizations.size());
		Assert.assertTrue(otherOrganizations.contains(otherBadgeOrganization));
		Assert.assertTrue(linkedInOrganizations.contains(linkedInBadgeOrganization));
	}
}
