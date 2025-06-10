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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeAssertionImpl;
import org.olat.repository.RepositoryEntryRef;
import jakarta.persistence.Query;
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

	/**
	 * Is there a badge assertion (a received badge) for the person with key 'recipientKey'
	 * of the badge with UUID 'badgeClassUuid'?
	 *
	 * @param recipientKey Person to perform the check for.
	 * @param badgeClassUuid Badge class to perform the check for.
	 * @return true if the person has received the badge.
	 */
	public boolean hasBadgeAssertion(Long recipientKey, String badgeClassUuid) {
		QueryBuilder sb = new QueryBuilder();
		sb
				.append("select ba.key from badgeassertion ba")
				.append(" inner join ba.badgeClass as bc")
				.append(" where ba.recipient.key = :recipientKey and bc.uuid = :badgeClassUuid");

		List<Long> badgeAssertionKeys = dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("recipientKey", recipientKey)
				.setParameter("badgeClassUuid", badgeClassUuid).getResultList();

		return badgeAssertionKeys != null && !badgeAssertionKeys.isEmpty();
	}

	public boolean hasBadgeAssertion(Long recipientKey, Long badgeClassKey) {
		QueryBuilder sb = new QueryBuilder();
		sb
				.append("select ba.key from badgeassertion ba")
				.append(" inner join ba.badgeClass as bc")
				.append(" where ba.recipient.key = :recipientKey")
				.append(" and bc.key = :badgeClassKey");

		List<Long> badgeAssertionKeys = dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("recipientKey", recipientKey)
				.setParameter("badgeClassKey", badgeClassKey).getResultList();

		return badgeAssertionKeys != null && !badgeAssertionKeys.isEmpty();
	}
	
	public boolean hasBadgeAssertionByRootId(Long recipientKey, String badgeClassRootId) {
		QueryBuilder sb = new QueryBuilder();
		sb
				.append("select ba.key from badgeassertion ba ")
				.append("inner join ba.badgeClass as bc ")
				.and().append("ba.recipient.key = :recipientKey ")
				.and().append("bc.rootId = :badgeClassRootId ");
		
		List<Long> badgeAssertionKeys = dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("recipientKey", recipientKey)
				.setParameter("badgeClassRootId", badgeClassRootId).getResultList();
		
		return badgeAssertionKeys != null && !badgeAssertionKeys.isEmpty();
	}
	
	public List<BadgeAssertion> getBadgeAssertions(IdentityRef recipient) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append("inner join fetch ba.badgeClass bc ");
		sb.append("where ba.recipient.key = :recipientKey ");
		return dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), BadgeAssertion.class)
				.setParameter("recipientKey", recipient.getKey())
				.getResultList();
	}

	public List<BadgeAssertion> getBadgeAssertions(IdentityRef recipient, RepositoryEntryRef courseEntry, boolean nullEntryMeansAll) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append("inner join fetch ba.badgeClass bc ");
		if (courseEntry != null) {
			sb.and().append("bc.entry.key = :courseEntryKey ");
		} else if (!nullEntryMeansAll) {
			sb.and().append("bc.entry is null ");
		}
		if (recipient != null) {
			sb.and().append("ba.recipient.key = :identityKey ");
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

	public List<BadgeAssertion> getBadgeAssertions(BadgeClass badgeClass, boolean allVersions) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		if (allVersions) {
			sb.append("where ba.badgeClass.rootId = :badgeClassRootId ");
		} else {
			sb.append("where ba.badgeClass.key = :badgeClassKey ");
		}
		TypedQuery<BadgeAssertion> typedQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BadgeAssertion.class);
		if (allVersions) {
			typedQuery.setParameter("badgeClassRootId", badgeClass.getRootId());
		} else {
			typedQuery.setParameter("badgeClassKey", badgeClass.getKey());
		}
		return typedQuery.getResultList();
	}

	public List<Identity> getBadgeAssertionIdentities(Collection<String> badgeClassRootIds) {
		QueryBuilder sb = new QueryBuilder();

		sb
				.append("select ident from badgeassertion ba ")
				.append(" inner join ba.recipient ident")
				.append(" where ba.badgeClass.rootId in (:badgeClassRootIds)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("badgeClassRootIds", badgeClassRootIds)
				.getResultList();
	}
	
	public Set<Long> getBadgeAssertionIdentityKeys(String rootId) {
		QueryBuilder sb = new QueryBuilder();
		
		sb.append("select distinct ident.key from badgeassertion ba ");
		sb.append("inner join ba.recipient ident ");
		sb.where().append("ba.badgeClass.rootId = :rootKey ");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("rootKey", rootId).getResultStream().collect(Collectors.toSet());
	}

	public Long getNumberOfBadgeAssertions(Long badgeClassKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(ba.key) from badgeassertion ba ");
		sb.append("where ba.badgeClass.key = :badgeClassKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("badgeClassKey", badgeClassKey).getSingleResult();
	}

	public Long getNumberOfBadgeAssertions(Identity recipient, BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(ba.key) from badgeassertion ba ");
		sb.append("where ba.recipient.key = :recipientKey ");
		sb.append("and (ba.badgeClass.key = :badgeClassKey or ba.badgeClass.rootId = :badgeClassRootId) ");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("recipientKey", recipient.getKey())
				.setParameter("badgeClassKey", badgeClass.getKey())
				.setParameter("badgeClassRootId", badgeClass.getRootId())
				.getResultList().get(0);
	}

	public BadgeAssertion getBadgeAssertion(Identity recipient, BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ba from badgeassertion ba ");
		sb.append(" where ba.recipient.key = :recipientKey ");
		sb.append("and ba.badgeClass.key = :badgeClassKey ");
		List<BadgeAssertion> badgeAssertions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BadgeAssertion.class)
				.setParameter("recipientKey", recipient.getKey())
				.setParameter("badgeClassKey", badgeClass.getKey())
				.getResultList();
		return badgeAssertions != null && !badgeAssertions.isEmpty() ? badgeAssertions.get(0) : null;
	}

	public boolean unrevokedBadgeAssertionsExist(BadgeClass badgeClass) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(ba.key) from badgeassertion ba ");
		sb.append("where ba.badgeClass.key = :badgeClassKey ");
		sb.append("and ba.status <> :status");
		Long numberOfUnrevokedBadgeAssertions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("badgeClassKey", badgeClass.getKey())
				.setParameter("status", BadgeAssertion.BadgeAssertionStatus.revoked)
				.getResultList().get(0);
		return numberOfUnrevokedBadgeAssertions > 0;
	}

	public BadgeAssertion updateBadgeAssertion(BadgeAssertion badgeAssertion) {
		badgeAssertion.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeAssertion);
	}

	public void revokeBadgeAssertion(Long key) {
		String updateQuery = "update badgeassertion set status = :status, lastModified = :lastModified where key = :key";
		dbInstance.getCurrentEntityManager()
				.createQuery(updateQuery)
				.setParameter("status", BadgeAssertion.BadgeAssertionStatus.revoked)
				.setParameter("key", key)
				.setParameter("lastModified", new Date())
				.executeUpdate();
	}

	public void revokeBadgeAssertions(BadgeClass badgeClass, boolean allVersions) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update badgeassertion ");
		sb.append("set status = :status, lastModified = :lastModified ");
		if (allVersions) {
			sb.append("where badgeClass.rootId = :badgeClassRootId");
		} else {
			sb.append("where badgeClass.key = :badgeClassKey");
		}
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("status", BadgeAssertion.BadgeAssertionStatus.revoked)
				.setParameter("lastModified", new Date());
		
		if (allVersions) {
			query.setParameter("badgeClassRootId", badgeClass.getRootId());	
		} else {
			query.setParameter("badgeClassKey", badgeClass.getKey());
		}
		
		query.executeUpdate();
	}

	public void deleteBadgeAssertion(BadgeAssertion badgeAssertion) {
		dbInstance.deleteObject(badgeAssertion);
	}
}
