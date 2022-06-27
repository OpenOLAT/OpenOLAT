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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
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
	@Autowired
	private AccessControlModule acModule;
	
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

		QueryBuilder query = new QueryBuilder(2048);
		if(Number.class.equals(type)) {
			query.append("select count(distinct v.key) from repositoryentry v ");
			query.append(" inner join v.olatResource as res");
		} else if(params.getParentEntry() != null) {
			query.append("select distinct v from catalogentry cei ")
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

		AddParams addParams = appendAccessSubSelects(query, params.getRoles(), params.isOnlyExplicitMember(), params.getOfferValidAt(), params.getOfferOrganisations());

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
		
		//quick search
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		if(StringHelper.containsNonWhitespace(params.getIdRefsAndTitle())) {
			quickRefs = params.getIdRefsAndTitle();
			quickText = PersistenceHelper.makeFuzzyQueryString(quickRefs);
			query.append(" and (v.externalId=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(query, "v.externalRef", "quickText", dbInstance.getDbVendor());
			query.append(" or v.softkey=:quickRef ");
			if (!params.isIdRefsOnly()) {
				query.append(" or ");
				PersistenceHelper.appendFuzzyLike(query, "v.displayname", "quickText", dbInstance.getDbVendor());
			}
			if(StringHelper.isLong(quickRefs)) {
				try {
					quickId = Long.parseLong(quickRefs);
					query.append(" or v.key=:quickVKey or res.resId=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			query.append(")");	
		}
		
		if(params.getAsParticipant() != null) {
			query.append(" and exists (select relpart from repoentrytogroup as relpart, bgroupmember as participant")
		      .append("   where relpart.entry.key=v.key and participant.group.key=relpart.group.key")
		      .append("   and participant.role='").append(GroupRoles.participant.name()).append("'")
		      .append("   and participant.identity.key=:participantKey")
		      .append(" )");
		}

		if (resourceTypes != null && !resourceTypes.isEmpty()) {
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
		if(quickId != null) {
			dbQuery.setParameter("quickVKey", quickId);
		}
		if(quickRefs != null) {
			dbQuery.setParameter("quickRef", quickRefs);
		}
		if(quickText != null) {
			dbQuery.setParameter("quickText", quickText);
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
		if(addParams.isIdentity()) {
			dbQuery.setParameter("identityKey", params.getIdentity().getKey());
		}
		if(params.getAsParticipant() != null) {
			dbQuery.setParameter("participantKey", params.getAsParticipant().getKey());
		}
		if (addParams.isOfferValidAt() && params.getOfferValidAt() != null) {
			dbQuery.setParameter( "offerValidAt", params.getOfferValidAt());
		}
		if (addParams.isOfferOrganisations() && params.getOfferOrganisations() != null && !params.getOfferOrganisations().isEmpty()) {
			dbQuery.setParameter("organisationKeys", params.getOfferOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
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
	private AddParams appendAccessSubSelects(QueryBuilder sb, Roles roles, boolean onlyExplicitMember, Date offerValidAt, List<? extends OrganisationRef> offerOrganisations) {
		if(roles.isGuestOnly()) {
			sb.append(" where v.publicVisible=true and v.status ").in(ACService.RESTATUS_ACTIVE_GUEST);
			sb.append(" and res.key in (");
			sb.append("   select resource.key");
			sb.append("     from acoffer offer");
			sb.append("     inner join offer.resource resource");
			sb.append("    where offer.valid = true");
			sb.append("      and offer.guestAccess = true");
			sb.append(")");
			return new AddParams(false, false, false);
		}
		
		boolean offerValidAtUsed = false;
		boolean offerOrganisationsUsed = false;
		
		if(dbInstance.isMySQL()) {
			sb.append(" where (v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("    where rel.entry.key=v.key and rel.group.key=membership.group.key and membership.identity.key=:identityKey")
			  .append("    and ");
		} else {
			sb.append(" where (exists (select rel.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("    where rel.entry.key=v.key and rel.group.key=membership.group.key and membership.identity.key=:identityKey")
			  .append("    and ");
		}

		if(onlyExplicitMember) {
			sb.append("(")
			  .append(" membership.role").in(GroupRoles.owner, GroupRoles.coach, GroupRoles.participant).append(" and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed())
			  .append("))");
		} else {
			//access rules as user
			sb.append("(")
			  .append("(")
			  .append(" membership.role ").in(OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, GroupRoles.owner, GroupRoles.coach, GroupRoles.participant)
			  .append("  and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
			if(roles.isAuthor()) {
				// as author
				sb.append(") or (")
				  .append(" membership.role ").in(OrganisationRoles.author).append("  and v.status ").in(RepositoryEntryStatusEnum.reviewToClosed());
			}
			sb.append("))");
			sb.append(")");
			
			// Open access
			sb.append(" or (");
			sb.append(" res.key in (");
			sb.append("   select resource.key");
			sb.append("     from acoffer offer");
			sb.append("     inner join offer.resource resource");
			sb.append("     inner join repositoryentry re2");
			sb.append("        on re2.olatResource.key = resource.key");
			sb.append("       and re2.publicVisible = true");
			sb.append("     inner join offertoorganisation oto");
			sb.append("        on oto.offer.key = offer.key");
			sb.append("    where offer.valid = true");
			sb.append("      and offer.openAccess = true");
			sb.append("      and re2.status ").in(ACService.RESTATUS_ACTIVE_OPEN);
			if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
				sb.append("      and oto.organisation.key in :organisationKeys");
				offerOrganisationsUsed = true;
			}
			sb.append(")"); // in
			sb.append(")"); // or
			
			// Access methods
			if (acModule.isEnabled()) {
				sb.append(" or (");
				sb.append(" res.key in (");
				sb.append("   select resource.key");
				sb.append("     from acofferaccess access");
				sb.append("     inner join access.offer offer");
				sb.append("     inner join offer.resource resource");
				sb.append("     inner join repositoryentry re2");
				sb.append("        on re2.olatResource.key = resource.key");
				sb.append("       and re2.publicVisible = true");
				sb.append("     inner join offertoorganisation oto");
				sb.append("        on oto.offer.key = offer.key");
				sb.append("   where offer.valid = true");
				sb.append("     and offer.openAccess = false");
				sb.append("     and offer.guestAccess = false");
				sb.append("     and access.method.enabled = true");
				if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
					sb.append("     and oto.organisation.key in :organisationKeys");
					offerOrganisationsUsed = true;
				}
				if (offerValidAt != null) {
					sb.append(" and (");
					sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD_PERIOD);
					sb.append(" and (offer.validFrom is not null or offer.validTo is not null)");
					sb.append(" and (offer.validFrom is null or offer.validFrom<=:offerValidAt)");
					sb.append(" and (offer.validTo is null or offer.validTo>=:offerValidAt)");
					sb.append(" or");
					sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD);
					sb.append(" and offer.validFrom is null and offer.validTo is null");
					sb.append(" )");
					offerValidAtUsed = true;
				}
				sb.append(")"); // in
				sb.append(")"); // or
			}
		}
		sb.append(")");
		return new AddParams(true, offerValidAtUsed, offerOrganisationsUsed);
	}
	
	private final static class AddParams {
		
		private final boolean identity;
		private final boolean offerValidAt;
		private final boolean offerOrganisations;
		
		public AddParams(boolean identity, boolean offerValidAt, boolean offerOrganisations) {
			this.identity = identity;
			this.offerValidAt = offerValidAt;
			this.offerOrganisations = offerOrganisations;
		}
		
		public boolean isIdentity() {
			return identity;
		}
		
		public boolean isOfferValidAt() {
			return offerValidAt;
		}
		
		public boolean isOfferOrganisations() {
			return offerOrganisations;
		}
		
	}
}
