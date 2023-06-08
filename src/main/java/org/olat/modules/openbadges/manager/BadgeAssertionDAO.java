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

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeAssertionImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-06-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeAssertionDAO {

	@Autowired
	private DB dbInstance;

	public void createBadgeAssertion(String uuid, String recipientEmail, BadgeClass badgeClass, String verification,
									 Date issuedOn, Identity awardedBy) {
		BadgeAssertionImpl badgeAssertion = new BadgeAssertionImpl();
		badgeAssertion.setCreationDate(new Date());
		badgeAssertion.setLastModified(badgeAssertion.getCreationDate());
		badgeAssertion.setUuid(uuid);
		badgeAssertion.setStatus(BadgeAssertion.BadgeAssertionStatus.editing);
		badgeAssertion.setRecipientObject(recipientEmail);
		badgeAssertion.setVerificationObject(verification);
		badgeAssertion.setIssuedOn(issuedOn);
		badgeAssertion.setBadgeClass(badgeClass);
		badgeAssertion.setAwardedBy(awardedBy);
	}

	public BadgeAssertion getAssertion(String uuid) {
		String q = "select ba from badgeassertion ba where uuid=:uuid";
		List<BadgeAssertion> badgeAssertions = dbInstance.getCurrentEntityManager()
				.createQuery(q, BadgeAssertion.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return badgeAssertions == null || badgeAssertions.isEmpty() ? null : badgeAssertions.get(0);
	}

	public List<BadgeAssertion> getBadgeAssertions() {
		String q = "select ba from badgeassertion ba";
		return dbInstance.getCurrentEntityManager().createQuery(q, BadgeAssertion.class).getResultList();
	}

	public BadgeAssertion updateBadgeAssertion(BadgeAssertion badgeAssertion) {
		badgeAssertion.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeAssertion);
	}

	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		dbInstance.deleteObject(badgeAssertion);
	}
}
