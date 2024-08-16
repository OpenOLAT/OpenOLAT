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

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.model.BadgeAssertionImpl;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;

/**
 * Initial date: 2023-08-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeTestData {

	public static BadgeClassImpl createTestBadgeClass(String name, String sourceImage, RepositoryEntry entry) {
		BadgeClassImpl badgeClassImpl = new BadgeClassImpl();

		badgeClassImpl.setUuid(OpenBadgesFactory.createIdentifier());
		badgeClassImpl.setStatus(BadgeClass.BadgeClassStatus.preparation);
		badgeClassImpl.setVersionWithScan("1.0");
		badgeClassImpl.setLanguage("en");
		badgeClassImpl.setValidityEnabled(false);
		badgeClassImpl.setImage(OpenBadgesFactory.createBadgeClassFileName(badgeClassImpl.getUuid(), sourceImage));
		badgeClassImpl.setNameWithScan(name);
		badgeClassImpl.setDescriptionWithScan("Test badge description");
		badgeClassImpl.setCriteria("<criteria></criteria>");
		badgeClassImpl.setSalt(OpenBadgesFactory.createSalt(badgeClassImpl));
		String issuer = "{\"name\":\"OpenOlat\",\"type\":\"Issuer\",\"@context\":\"https://w3id.org/openbadges/v2\",\"url\":\"https://test.openolat.org\"}";
		badgeClassImpl.setIssuer(issuer);
		badgeClassImpl.setEntry(entry);

		CoreSpringFactory.getImpl(BadgeClassDAO.class).createBadgeClass(badgeClassImpl);

		return badgeClassImpl;
	}

	public static BadgeAssertionImpl createTestBadgeAssertion(BadgeClass badgeClass, String recipientLogin, Identity awardedBy) {
		String uuid = OpenBadgesFactory.createIdentifier();
		String recipientObject = "{}";
		String verification = "{}";
		Date issuedOn = new Date();
		Identity recipient = JunitTestHelper.createAndPersistIdentityAsUser(recipientLogin);
		BadgeAssertionImpl badgeAssertionImpl = new BadgeAssertionImpl();

		CoreSpringFactory.getImpl(BadgeAssertionDAO.class).createBadgeAssertion(uuid, recipientObject, badgeClass,
				verification, issuedOn, recipient, awardedBy);

		return badgeAssertionImpl;
	}
}
