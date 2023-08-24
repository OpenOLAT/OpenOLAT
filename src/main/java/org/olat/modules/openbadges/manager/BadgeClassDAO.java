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
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
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

	public List<BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry) {
		return getBadgeClassesWithUseCounts(entry, true);
	}

	private List<BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry, boolean excludeDeleted) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select bc, ");
		sb.append(" (select count(ba.key) from badgeassertion ba ");
		sb.append("   where ba.badgeClass.key = bc.key ");
		sb.append(" ) as useCount ");
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

	public static class BadgeClassWithUseCount {
		private final BadgeClass badgeClass;
		private final Long useCount;

		public BadgeClassWithUseCount(Object[] objectArray) {
			this.badgeClass = (BadgeClass) objectArray[0];
			this.useCount = PersistenceHelper.extractPrimitiveLong(objectArray, 1);
		}

		public BadgeClass getBadgeClass() {
			return badgeClass;
		}

		public Long getUseCount() {
			return useCount;
		}
	}
}
