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
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeAssertionImpl;
import org.olat.repository.RepositoryEntry;

import jakarta.persistence.TypedQuery;
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

	public BadgeAssertion createBadgeAssertion(String uuid, String recipientObject, BadgeClass badgeClass, String verification,
									 Date issuedOn, Identity recipient, Identity awardedBy) {
		BadgeAssertionImpl badgeAssertion = new BadgeAssertionImpl();
		badgeAssertion.setCreationDate(new Date());
		badgeAssertion.setLastModified(badgeAssertion.getCreationDate());
		badgeAssertion.setUuid(uuid);
		badgeAssertion.setStatus(BadgeAssertion.BadgeAssertionStatus.issued);
		badgeAssertion.setRecipientObject(recipientObject);
		badgeAssertion.setVerificationObject(verification);
		badgeAssertion.setIssuedOn(issuedOn);
		badgeAssertion.setBadgeClass(badgeClass);
		badgeAssertion.setRecipient(recipient);
		badgeAssertion.setAwardedBy(awardedBy);
		dbInstance.getCurrentEntityManager().persist(badgeAssertion);
		return badgeAssertion;
	}

	public BadgeAssertion getAssertion(String uuid) {
		String q = "select ba from badgeassertion ba where uuid=:uuid";
		List<BadgeAssertion> badgeAssertions = dbInstance.getCurrentEntityManager()
				.createQuery(q, BadgeAssertion.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return badgeAssertions == null || badgeAssertions.isEmpty() ? null : badgeAssertions.get(0);
	}

	public List<BadgeAssertion> getBadgeAssertions(Identity recipient, RepositoryEntry courseEntry, boolean nullEntryMeansAll) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append("inner join fetch ba.badgeClass bc ");
		if (courseEntry != null) {
			sb.append("where bc.entry.key = :courseEntryKey ");
		} else if (!nullEntryMeansAll) {
			sb.append("where bc.entry is null ");
		}
		if (recipient != null) {
			if (courseEntry == null && nullEntryMeansAll) {
				sb.append("where ba.recipient.key = :identityKey ");
			} else {
				sb.append("and ba.recipient.key = :identityKey ");
			}
		}
		sb.append("order by ba.status asc, ba.issuedOn desc ");
		TypedQuery<BadgeAssertion> typedQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BadgeAssertion.class);
		if (recipient != null) {
			typedQuery = typedQuery.setParameter("identityKey", recipient.getKey());
		}
		if (courseEntry != null) {
			typedQuery = typedQuery.setParameter("courseEntryKey", courseEntry.getKey());
		}
		return typedQuery.getResultList();
	}

	public List<BadgeAssertion> getBadgeAssertions(Identity recipient, BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append("where ba.recipient.key = :recipientKey ");
		sb.append("and ba.badgeClass.key = :badgeClassKey ");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BadgeAssertion.class)
				.setParameter("recipientKey", recipient.getKey())
				.setParameter("badgeClassKey", badgeClass.getKey())
				.getResultList();
	}

	public List<BadgeAssertion> getBadgeAssertions(BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append("where ba.badgeClass.key = :badgeClassKey ");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BadgeAssertion.class)
				.setParameter("badgeClassKey", badgeClass.getKey())
				.getResultList();
	}

	public Long getNumberOfBadgeAssertions(Identity recipient, BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(ba.key) from badgeassertion ba ");
		sb.append("where ba.recipient.key = :recipientKey ");
		sb.append("and ba.badgeClass.key = :badgeClassKey ");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("recipientKey", recipient.getKey())
				.setParameter("badgeClassKey", badgeClass.getKey())
				.getResultList().get(0);
	}

	public BadgeAssertion updateBadgeAssertion(BadgeAssertion badgeAssertion) {
		badgeAssertion.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeAssertion);
	}

	public void revokeBadgeAssertion(Long key) {
		String updateQuery = "update badgeassertion set status = :status where key = :key";
		dbInstance.getCurrentEntityManager()
				.createQuery(updateQuery)
				.setParameter("status", BadgeAssertion.BadgeAssertionStatus.revoked)
				.setParameter("key", key)
				.executeUpdate();
	}

	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		dbInstance.deleteObject(badgeAssertion);
	}
}
