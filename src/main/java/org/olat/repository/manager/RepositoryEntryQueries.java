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
package org.olat.repository.manager;

import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryQueries {
	
	@Autowired
	private DB dbInstance;
	
	public int countEntries(SearchRepositoryEntryParameters params) {
		TypedQuery<Number> dbQuery = createQuery(params, false, Number.class);
		Number count = dbQuery.getSingleResult();
		return count.intValue();
	}

	public List<RepositoryEntry> searchEntries(SearchRepositoryEntryParameters params, int firstResult, int maxResults, boolean orderBy) {
		TypedQuery<RepositoryEntry> dbQuery = createQuery(params, orderBy, RepositoryEntry.class);
		dbQuery.setFirstResult(firstResult);
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}
		return dbQuery.getResultList();
	}

	private <T> TypedQuery<T> createQuery(SearchRepositoryEntryParameters params, boolean orderBy, Class<T> type) {
		String author = params.getAuthor();
		String displayName = params.getDisplayName();
		List<String> resourceTypes = params.getResourceTypes();

		StringBuilder query = new StringBuilder(2048);
		if(Number.class.equals(type)) {
			query.append("select count(v.key) from repositoryentry v ");
			query.append(" inner join v.olatResource as res");
		} else if(params.getParentEntry() != null) {
			query.append("select v from ").append(CatalogEntry.class.getName()).append(" cei ")
			     .append(" inner join cei.parent parentCei")
			     .append(" inner join cei.repositoryEntry v")
			     .append(" inner join fetch v.olatResource as res")
			     .append(" inner join fetch v.statistics as statistics")
			     .append(" left join fetch v.lifecycle as lifecycle");
		} else {
			query.append("select distinct v from repositoryentry v ")
			     .append(" inner join fetch v.olatResource as res")
			     .append(" inner join fetch v.statistics as statistics")
			     .append(" left join fetch v.lifecycle as lifecycle");
		}

		boolean setIdentity = appendAccessSubSelects(query, params.getRoles(), params.isOnlyExplicitMember());

		if(params.getParentEntry() != null) {
			query.append(" and parentCei.key=:parentCeiKey");
		}

		if (StringHelper.containsNonWhitespace(author)) {
			// fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(author);
			query.append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=identity.key and user.identity.key=identity.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(query, "user.firstName", "author", dbInstance.getDbVendor());
			query.append(" or ");
			PersistenceHelper.appendFuzzyLike(query, "user.lastName", "author", dbInstance.getDbVendor());
			query.append(" or ");
			PersistenceHelper.appendFuzzyLike(query, "identity.name", "author", dbInstance.getDbVendor());
			query.append(" ))");
		}

		if (StringHelper.containsNonWhitespace(displayName)) {
			displayName = PersistenceHelper.makeFuzzyQueryString(displayName);
			query.append(" and ");
			PersistenceHelper.appendFuzzyLike(query, "v.displayname", "displayname", dbInstance.getDbVendor());
		}

		String desc = null;
		if (StringHelper.containsNonWhitespace(params.getDesc())) {
			desc = PersistenceHelper.makeFuzzyQueryString(params.getDesc());
			query.append(" and ");
			PersistenceHelper.appendFuzzyLike(query, "v.description", "desc", dbInstance.getDbVendor());
		}

		if (resourceTypes != null && resourceTypes.size() > 0) {
			query.append(" and res.resName in (:resourcetypes)");
		}

		if(params.getRepositoryEntryKeys() != null && !params.getRepositoryEntryKeys().isEmpty()) {
			query.append(" and v.key in (:entryKeys)");
		}

		if(params.getManaged() != null) {
			if(params.getManaged().booleanValue()) {
				query.append(" and v.managedFlagsString is not null");
			} else {
				query.append(" and v.managedFlagsString is null");
			}
		}

		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			query.append(" and v.externalId=:externalId");
		}

		if(StringHelper.containsNonWhitespace(params.getExternalRef())) {
			query.append(" and v.externalRef=:externalRef");
		}

		if(orderBy) {
			query.append(" order by v.displayname, v.key ASC");
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), type);
		if(params.getParentEntry() != null) {
			dbQuery.setParameter("parentCeiKey", params.getParentEntry().getKey());
		}
		if (StringHelper.containsNonWhitespace(author)) {
			dbQuery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(displayName)) {
			dbQuery.setParameter("displayname", displayName);
		}
		if (StringHelper.containsNonWhitespace(desc)) {
			dbQuery.setParameter("desc", desc);
		}
		if (resourceTypes != null && !resourceTypes.isEmpty()) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(params.getRepositoryEntryKeys() != null && !params.getRepositoryEntryKeys().isEmpty()) {
			dbQuery.setParameter("entryKeys", params.getRepositoryEntryKeys());
		}
		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			dbQuery.setParameter("externalId", params.getExternalId());
		}
		if(StringHelper.containsNonWhitespace(params.getExternalRef())) {
			dbQuery.setParameter("externalRef", params.getExternalRef());
		}
		if(setIdentity) {
			dbQuery.setParameter("identityKey", params.getIdentity().getKey());
		}
		return dbQuery;
	}

	/**
	 * This query need the repository entry as v, v.olatResource as res
	 * and v.baseGroup as baseGroup
	 * @param sb
	 * @param identity
	 * @param roles
	 * @return
	 */
	private boolean appendAccessSubSelects(StringBuilder sb, Roles roles, boolean onlyExplicitMember) {
		if(roles.isOLATAdmin()) {
			sb.append(" where v.access>=").append(RepositoryEntry.ACC_OWNERS);
			return false;	
		}
		if(roles.isGuestOnly()) {
			sb.append(" where v.access>=").append(RepositoryEntry.ACC_USERS_GUESTS);
			return false;
		}
		
		// no by-pass -> join the memberships
		sb.append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and");
		
		if(onlyExplicitMember) {
			sb.append(" (v.access>=").append(RepositoryEntry.ACC_USERS)
		      .append(" or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
		      .append("   and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
		      .append(" ))");
			return true;
		}

		//access rules as user
		sb.append("(v.access>=").append(RepositoryEntry.ACC_USERS);
		if(roles.isLearnResourceManager()) {
			// as learn resource manager
			sb.append(" or (v.access>=").append(RepositoryEntry.ACC_OWNERS)
			  .append("  and membership.role='").append(OrganisationRoles.learnresourcemanager.name()).append("')");
		}
		
		if(roles.isAuthor()) {
			// as author
			sb.append(" or (v.access>=").append(RepositoryEntry.ACC_OWNERS_AUTHORS)
			  .append("  and membership.role='").append(OrganisationRoles.author.name()).append("')");
		}
		// as owner
		sb.append(" or (v.access>=").append(RepositoryEntry.ACC_OWNERS)
		  .append("  and membership.role='").append(GroupRoles.owner.name()).append("')");
		// as member
		sb.append(" or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
		  .append("  and membership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("'))")
		  .append(")");

		return true;
	}
}
