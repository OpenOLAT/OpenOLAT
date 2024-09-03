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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeClassDAO {

	@Autowired
	private DB dbInstance;

	public void createBadgeClass(BadgeClassImpl badgeClass) {
		badgeClass.setKey(null);
		badgeClass.setCreationDate(new Date());
		badgeClass.setLastModified(badgeClass.getCreationDate());
		dbInstance.getCurrentEntityManager().persist(badgeClass);
	}

	public List<BadgeClass> getBadgeClasses(RepositoryEntryRef entry) {
		return getBadgeClasses(entry, true);
	}

	public List<BadgeClass> getBadgeClasses(RepositoryEntryRef entry, boolean excludeDeleted) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select class from badgeclass class ");
		if (entry != null) {
			sb.append(" where class.entry.key = :entryKey ");
		} else {
			sb.append(" where class.entry is null ");
		}
		if (excludeDeleted) {
			sb.append(" and class.status <> :excludedStatus ");
		}
		sb.append("order by class.name asc ");
		TypedQuery<BadgeClass> typedQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BadgeClass.class);
		if (entry != null) {
			typedQuery = typedQuery.setParameter("entryKey", entry.getKey());
		}
		if (excludeDeleted) {
			typedQuery = typedQuery.setParameter("excludedStatus", BadgeClass.BadgeClassStatus.deleted);
		}
		return typedQuery.getResultList();
	}

	/**
	 * Returns a list of badge classes that belong to courses owned by the owners of the course
	 * identified by 'courseEntry'.
	 *
	 * Only returns badge classes that can be issued (no deleted or revoked badge classes).
	 *
	 * Also returns the badge classes of the course identified by 'courseEntry'.
	 *
 	 * @param courseEntry The course to start the search with.
	 * @return All badge classes that can be issued and that are associated with a course via co-ownership.
	 */
	public List<BadgeClass> getBadgeClassesInCoOwnedCourseSet(RepositoryEntry courseEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb
				.append("select coBc from badgeclass coBc")
				.append(" inner join coBc.entry as coRe")
				.append(" inner join coRe.groups as coGroupRel")
				.append(" inner join coGroupRel.group as coGroup")
				.append(" inner join coGroup.members as coMembership")
				.append(" where coMembership.role ").in(GroupRoles.owner)
				.append(" and coBc.status ").in(BadgeClass.BadgeClassStatus.active, BadgeClass.BadgeClassStatus.preparation)
				.append(" and coMembership.identity.key in (")
				.append("  select membership.identity.key from repositoryentry re")
				.append("   inner join re.groups as groupRel")
				.append("   inner join groupRel.group as group")
				.append("   inner join group.members as membership")
				.append("   where membership.role ").in(GroupRoles.owner)
				.append("   and re.key =: courseEntryKey")
				.append(" )");

		return dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), BadgeClass.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.getResultList();
	}

	public Long getNumberOfBadgeClasses(RepositoryEntryRef entry) {
		return getNumberOfBadgeClasses(entry, true);
	}

	private Long getNumberOfBadgeClasses(RepositoryEntryRef entry, boolean excludeDeleted) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(bc.key) from badgeclass bc ");
		if (entry != null) {
			sb.append(" where bc.entry.key = :entryKey ");
		} else {
			sb.append(" where bc.entry is null ");
		}
		if (excludeDeleted) {
			sb.append(" and bc.status <> :excludedStatus ");
		}
		TypedQuery<Long> typedQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);
		if (entry != null) {
			typedQuery = typedQuery.setParameter("entryKey", entry.getKey());
		}
		if (excludeDeleted) {
			typedQuery = typedQuery.setParameter("excludedStatus", BadgeClass.BadgeClassStatus.deleted);
		}
		return typedQuery.getResultList().get(0);
	}

	/**
	 * Returns a list of badge classes with their use count. The use count is the number of badge assertions for
	 * each badge class.
	 *
	 * If a course repository entry is specified, only returns badge classes that belong to the specified course.
	 *
	 * If no course repository entry is specified, only returns badge classes that do not belong to any course.
	 * These are global badges.
	 *
	 * @param entry The course repository entry. Can be null, which means that no course repository entry is specified.
	 *
	 * @return A list of badge classes with their use count
	 */
	public List<BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry) {
		return getBadgeClassesWithUseCounts(entry, true);
	}

	private List<BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry, boolean excludeDeleted) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select bc, ");
		sb.append(" (select count(ba.key) from badgeassertion ba ");
		sb.append("   where ba.badgeClass.key = bc.key ");
		sb.append(" ), ");
		sb.append(" (select count(ba.key) from badgeassertion ba");
		sb.append("  where ba.badgeClass.key = bc.key");
		sb.append("  and ba.status = '").append(BadgeAssertion.BadgeAssertionStatus.revoked.name()).append("'");
		sb.append(" ), ");
		sb.append(" (select count(ba.key) from badgeassertion ba");
		sb.append("  where ba.badgeClass.key = bc.key");
		sb.append("  and ba.status = '").append(BadgeAssertion.BadgeAssertionStatus.reset.name()).append("'");
		sb.append(" ) ");
		sb.append("from badgeclass bc ");
		if (entry != null) {
			sb.append("where bc.entry.key = :entryKey ");
		} else {
			sb.append("where bc.entry is null ");
		}
		if (excludeDeleted) {
			sb.append(" and bc.status <> :excludedStatus ");
		}
		sb.append("order by bc.status asc, bc.name asc ");
		TypedQuery<Object[]> typedQuery = dbInstance
				.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if (entry != null) {
			typedQuery.setParameter("entryKey", entry.getKey());
		}
		if (excludeDeleted) {
			typedQuery = typedQuery.setParameter("excludedStatus", BadgeClass.BadgeClassStatus.deleted);
		}
		return typedQuery
				.getResultList()
				.stream()
				.map(BadgeClassWithUseCount::new)
				.toList();
	}

	public BadgeClass getBadgeClass(String uuid) {
		String query = "select bc from badgeclass bc where bc.uuid=:uuid";
		List<BadgeClass> badgeClasses = dbInstance.getCurrentEntityManager()
				.createQuery(query, BadgeClass.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return badgeClasses == null || badgeClasses.isEmpty() ? null : badgeClasses.get(0);
	}

	public BadgeClass getBadgeClass(Long key) {
		return dbInstance.getCurrentEntityManager().find(BadgeClassImpl.class, key);
	}

	public BadgeClass updateBadgeClass(BadgeClass badgeClass) {
		badgeClass.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(badgeClass);
	}

	public void deleteBadgeClass(BadgeClass badgeClass) {
		dbInstance.deleteObject(badgeClass);
	}

	public List<String> getBadgeClassNames(Collection<Long> badgeClassKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb
				.append("select bc.name from badgeclass bc")
				.append(" where bc.key in (:badgeClassKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("badgeClassKeys", badgeClassKeys)
				.getResultList();
	}

	/**
	 * Returns true if the specified author owns any course badges.
	 *
	 * @param author Author to perform the check for.
	 *
	 * @return true if the author owns at least one course badge.
	 */
	public boolean hasCourseBadgeClasses(Identity author) {
		QueryBuilder sb = new QueryBuilder();

		sb
				.append("select count(bc.key) from badgeclass bc")
				.append(" inner join bc.entry as re")
				.append(" inner join re.groups as groupRel")
				.append(" inner join groupRel.group as group")
				.append(" inner join group.members as membership")
				.append(" where membership.role ").in(GroupRoles.owner)
				.append(" and membership.identity.key = :authorKey")
				.append(" and bc.status ").in(BadgeClass.BadgeClassStatus.active, BadgeClass.BadgeClassStatus.preparation);

		return !dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("authorKey", author.getKey())
				.getSingleResult().equals(Long.valueOf(0));
	}

	/**
	 * Returns a list of badge classes with their use count (number of badge assertions per badge class).
	 *
	 * The list includes the badge classes that the specified 'author' has access to through course ownership.
	 * So it is all the badges of all the courses that the author is an owner of.
	 *
	 * @param author The author to use for the course ownership check.

	 * @return A list of badge classes with their respective number of badge assertions.
	 */
	public List<BadgeClassWithUseCount> getCourseBadgeClassesWithUseCounts(Identity author) {
		QueryBuilder sb = new QueryBuilder();

		sb
				.append("select bc,")
				.append(" (select count(ba.key) from badgeassertion ba")
				.append("   where ba.badgeClass.key = bc.key")
				.append(" ), ")
		        .append(" (select count(ba.key) from badgeassertion ba")
		        .append("  where ba.badgeClass.key = bc.key")
		        .append("  and ba.status = '").append(BadgeAssertion.BadgeAssertionStatus.revoked.name()).append("'")
		        .append(" ), ")
		        .append(" (select count(ba.key) from badgeassertion ba")
		        .append("  where ba.badgeClass.key = bc.key")
		        .append("  and ba.status = '").append(BadgeAssertion.BadgeAssertionStatus.reset.name()).append("'")
		        .append(" ) ")
				.append(" from badgeclass bc")
				.append(" inner join bc.entry as re")
				.append(" inner join re.groups as groupRel")
				.append(" inner join groupRel.group as group")
				.append(" inner join group.members as membership")
				.append(" where membership.role ").in(GroupRoles.owner)
				.append(" and membership.identity.key = :authorKey")
				.append(" and bc.status ").in(BadgeClass.BadgeClassStatus.active, BadgeClass.BadgeClassStatus.preparation)
				.append(" order by bc.creationDate desc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("authorKey", author.getKey())
				.getResultList()
				.stream()
				.map(BadgeClassWithUseCount::new)
				.toList();
	}

	public static class BadgeClassWithUseCount {
		private final BadgeClass badgeClass;
		private final Long useCount;
		private final Long revokedCount;
		private final Long resetCount;

		public BadgeClassWithUseCount(Object[] objectArray) {
			this.badgeClass = (BadgeClass) objectArray[0];
			this.useCount = PersistenceHelper.extractPrimitiveLong(objectArray, 1);
			this.revokedCount = PersistenceHelper.extractPrimitiveLong(objectArray, 2);
			this.resetCount = PersistenceHelper.extractPrimitiveLong(objectArray, 3);
		}

		public BadgeClass getBadgeClass() {
			return badgeClass;
		}

		public Long getUseCount() {
			return useCount;
		}

		public Long getRevokedCount() {
			return revokedCount;
		}

		public Long getResetCount() {
			return resetCount;
		}

		public boolean isActive() {
			return useCount > revokedCount;
		}

		public boolean isRevoked() {
			return useCount > 0 && useCount.equals(revokedCount);
		}

		public boolean isPreparation() {
			return useCount == 0 & revokedCount == 0;
		}
	}
}
