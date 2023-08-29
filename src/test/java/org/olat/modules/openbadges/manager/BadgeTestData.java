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

import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-08-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeTestData {

	public static BadgeClassImpl createTestBadgeClass(String name, String image, RepositoryEntry entry) {
		BadgeClassImpl badgeClassImpl = new BadgeClassImpl();

		badgeClassImpl.setUuid(OpenBadgesUIFactory.createIdentifier());
		badgeClassImpl.setStatus(BadgeClass.BadgeClassStatus.preparation);
		badgeClassImpl.setVersion("1.0");
		badgeClassImpl.setLanguage("en");
		badgeClassImpl.setValidityEnabled(false);
		badgeClassImpl.setImage(image);
		badgeClassImpl.setName(name);
		badgeClassImpl.setDescription("Test badge description");
		badgeClassImpl.setCriteria("<criteria></criteria>");
		badgeClassImpl.setSalt("badgeClass" + Math.abs(badgeClassImpl.getUuid().hashCode()));
		String issuer = "{\"name\":\"OpenOlat\",\"type\":\"Issuer\",\"@context\":\"https://w3id.org/openbadges/v2\",\"url\":\"https://test.openolat.org\"}";
		badgeClassImpl.setIssuer(issuer);
		badgeClassImpl.setEntry(entry);

		return badgeClassImpl;
	}
}
