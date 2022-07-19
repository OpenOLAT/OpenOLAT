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
package org.olat.modules.catalog.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.OrderBy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogRepositoryEntryQueries {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AccessControlModule acModule;
	
	public Integer countRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams)  {
		TypedQuery<Number> query = createMyViewQuery(searchParams, Number.class);
		Number count = query
				.setFlushMode(FlushModeType.COMMIT)
				.getSingleResult();
		return Integer.valueOf(count.intValue());
	}

	public List<RepositoryEntry> loadRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams, int firstResult, int maxResults) {
		TypedQuery<Object[]> query = createMyViewQuery(searchParams, Object[].class);
		query.setFlushMode(FlushModeType.COMMIT)
			.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList().stream()
				.map(result -> (RepositoryEntry)result[0])
				.collect(Collectors.toList());
	}

	protected <T> TypedQuery<T> createMyViewQuery(CatalogRepositoryEntrySearchParams searchParams, Class<T> type) {
		boolean count = Number.class.equals(type);
		QueryBuilder sb = new QueryBuilder(2048);
		
		if(count) {
			sb.append("select count(v.key) ");
			sb.append(" from repositoryentry as v");
			sb.append(" inner join v.olatResource as res");
			sb.append(" left join v.lifecycle as lifecycle");
		} else {
			sb.append("select v");
			if (OrderBy.popularCourses == searchParams.getOrderBy()) {
				sb.append(", (select sum(stat.value) ");
				sb.append("     from dailystat as stat ");
				sb.append("    where stat.resId = v.key and stat.day > :statDay");
				sb.append("  ) as popularCourses");
			} else {
				sb.append(", 0 as popularCourses");
			}
			sb.append(" from repositoryentry as v");
			sb.append(" inner join fetch v.olatResource as res");
			sb.append(" left join fetch v.lifecycle as lifecycle");
			sb.append(" left join fetch v.educationalType as educationalType");
		}
		
		AddParams addParams = appendMyViewAccessSubSelect(sb, searchParams.isGuestOnly(),
				searchParams.getOfferValidAt(), searchParams.getOfferOrganisations(), searchParams.getOpenAccess(),
				searchParams.getShowAccessMethods(), searchParams.getAccessMethods());
		
		if (searchParams.getRepositoryEntryKeys() != null && !searchParams.getRepositoryEntryKeys().isEmpty()) {
			sb.and().append("v.key in :repositoryEntryKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("v.status in :status");
		}
		
		Set<String> resourceTypes = null;
		for (Collection<String> identToResourceTypes : searchParams.getIdentToResourceTypes().values()) {
			if (resourceTypes == null) {
				resourceTypes = new HashSet<>(identToResourceTypes);
			} else {
				resourceTypes.retainAll(identToResourceTypes);
			}
		}
		// No check if is empty! retainAll() may have put all keys away, so no result must be found
		if (resourceTypes != null) {
			sb.and().append("res.resName in :resourceTypes");
		}
		
		Set<Long> educationalTypeKeys = null;
		for (Collection<Long> identToEducationalTypeKeys : searchParams.getIdentToEducationalTypeKeys().values()) {
			if (educationalTypeKeys == null) {
				educationalTypeKeys = new HashSet<>(identToEducationalTypeKeys);
			} else {
				educationalTypeKeys.retainAll(identToEducationalTypeKeys);
			}
		}
		// No check if is empty! retainAll() may have put all keys away, so no result must be found
		if (educationalTypeKeys != null) {
			sb.and().append("v.educationalType.key in :educationalTypeKeys");
		}
		
		for (Entry<String, List<TaxonomyLevel>> identToTaxonomyLevels : searchParams.getIdentToTaxonomyLevels().entrySet()) {
			if (identToTaxonomyLevels.getValue() != null && !identToTaxonomyLevels.getValue().isEmpty()) {
				if (searchParams.isTaxonomyLevelChildren()) {
					sb.and().append(" exists (select reToTax.key from repositoryentrytotaxonomylevel as reToTax");
					sb.append("  where reToTax.entry.key=v.key");
					sb.append("    and");
					for (int i = 0; i < identToTaxonomyLevels.getValue().size(); i++) {
						if (i == 0) {
							sb.append("(");
						} else {
							sb.append(" or ");
						}
						sb.append(" reToTax.taxonomyLevel.materializedPathKeys like :materializedPath").append(identToTaxonomyLevels.getKey()).append("_").append(i);
						if (i == identToTaxonomyLevels.getValue().size() - 1) {
							sb.append(")");
						}
					}
					sb.append(")");
				} else {
					sb.and().append(" exists (select reToTax.key from repositoryentrytotaxonomylevel as reToTax");
					sb.append("  where reToTax.entry.key=v.key");
					sb.append("    and reToTax.taxonomyLevel.key in (:taxonomyLevelKeys").append(identToTaxonomyLevels.getKey()).append("))");
				}
			}
		}
		
		String author = searchParams.getAuthor();
		if (StringHelper.containsNonWhitespace(author)) {
			author = PersistenceHelper.makeFuzzyQueryString(author);

			sb.append(" and (v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership, ");
			sb.append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user");
			sb.append("    where rel.group.key=membership.group.key and membership.identity.key=identity.key and user.identity.key=identity.key");
			sb.append("      and membership.role='").append(GroupRoles.owner.name()).append("'");
			sb.append("      and (");
			PersistenceHelper.appendFuzzyLike(sb, "user.firstName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.lastName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" )) or");
			PersistenceHelper.appendFuzzyLike(sb, "v.authors", "author", dbInstance.getDbVendor());
			sb.append(" )");
		}
		
		String text = searchParams.getSearchString();
		Collection<String> serachTaxonomyLevelI18nSuffix = null;
		if (StringHelper.containsNonWhitespace(text)) {
			text = PersistenceHelper.makeFuzzyQueryString(text);
			sb.and().append("(");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.description", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.objectives", "displaytext", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.authors", "displaytext", dbInstance.getDbVendor());
			if (searchParams.getSerachTaxonomyLevelI18nSuffix() != null && !searchParams.getSerachTaxonomyLevelI18nSuffix().isEmpty()) {
				sb.append(" or exists (select reToTax.key from repositoryentrytotaxonomylevel as reToTax");
				sb.append("  where reToTax.entry.key=v.key");
				sb.append("    and reToTax.taxonomyLevel.i18nSuffix in :serachTaxonomyLevelI18nSuffix)");
				serachTaxonomyLevelI18nSuffix = searchParams.getSerachTaxonomyLevelI18nSuffix();
			}
			sb.append(")");
		}
		
		if(!count) {
			appendOrderBy(searchParams, sb);
		}
		
		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if (addParams.isOfferValidAt() && searchParams.getOfferValidAt() != null) {
			dbQuery.setParameter( "offerValidAt", searchParams.getOfferValidAt());
		}
		if (addParams.isOfferOrganisations() && searchParams.getOfferOrganisations() != null && !searchParams.getOfferOrganisations().isEmpty()) {
			dbQuery.setParameter("offerOrganisationKeys", searchParams.getOfferOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		if (searchParams.getRepositoryEntryKeys() != null && !searchParams.getRepositoryEntryKeys().isEmpty()) {
			dbQuery.setParameter("repositoryEntryKeys", searchParams.getRepositoryEntryKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			Collection<String> status = searchParams.getStatus().stream().map(RepositoryEntryStatusEnum::name).collect(Collectors.toList());
			dbQuery.setParameter("status", status);
		}
		if (resourceTypes != null) {
			dbQuery.setParameter("resourceTypes", resourceTypes);
		}
		if (educationalTypeKeys != null) {
			dbQuery.setParameter("educationalTypeKeys", educationalTypeKeys);
		}
		for (Entry<String, List<TaxonomyLevel>> identToTaxonomyLevels : searchParams.getIdentToTaxonomyLevels().entrySet()) {
			if (identToTaxonomyLevels.getValue() != null && !identToTaxonomyLevels.getValue().isEmpty()) {
				if (searchParams.isTaxonomyLevelChildren()) {
					for (int i = 0; i < identToTaxonomyLevels.getValue().size(); i++) {
						String parameter = new StringBuilder().append("materializedPath").append(identToTaxonomyLevels.getKey()).append("_").append(i).toString();
						String pathKeys = identToTaxonomyLevels.getValue().get(i).getMaterializedPathKeys();
						dbQuery.setParameter(parameter, pathKeys + "%");
					}
				} else {
					Collection<Long> keys = identToTaxonomyLevels.getValue().stream().map(TaxonomyLevel::getKey).collect(Collectors.toList());
					dbQuery.setParameter("taxonomyLevelKeys" + identToTaxonomyLevels.getKey(), keys );
				}
			}
		}
		if (addParams.isAccessMethods() && searchParams.getAccessMethods() != null && !searchParams.getAccessMethods().isEmpty()) {
			dbQuery.setParameter("accessMethods", searchParams.getAccessMethods());
		}
		if (StringHelper.containsNonWhitespace(author)) {
			dbQuery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(text)) {
			dbQuery.setParameter("displaytext", text);
		}
		if (serachTaxonomyLevelI18nSuffix != null) {
			dbQuery.setParameter("serachTaxonomyLevelI18nSuffix", serachTaxonomyLevelI18nSuffix);
		}
		if (!count && OrderBy.popularCourses == searchParams.getOrderBy()) {
			dbQuery.setParameter("statDay", DateUtils.addDays(new Date(), -28));
		}
		return dbQuery;
	}
	
	private AddParams appendMyViewAccessSubSelect(QueryBuilder sb, boolean isGuestOnly, Date offerValidAt,
			List<? extends OrganisationRef> offerOrganisations, Boolean openAccess, Boolean showAccessMethods,
			Collection<AccessMethod> accessMethods) {
		if (isGuestOnly) {
			sb.and().append("v.publicVisible=true");
			sb.and().append("v.status ").in(ACService.RESTATUS_ACTIVE_GUEST);
			sb.and().append("res.key in (");
			sb.append("   select resource.key");
			sb.append("     from acoffer offer");
			sb.append("     inner join offer.resource resource");
			sb.append("    where offer.valid = true");
			sb.append("      and offer.guestAccess = true");
			sb.append(")");
			return AddParams.ALL_FALSE;
		}
		
		boolean or = false;
		boolean offerOrganisationsUsed = false;
		boolean offerValidAtUsed = false;
		boolean accessMethodsUsed = false;
		
		sb.and().append("(");
		// Open access
		if (openAccess == null || openAccess.booleanValue()) {
			or = true;
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
				sb.append("      and oto.organisation.key in :offerOrganisationKeys");
				offerOrganisationsUsed = true;
			}
			sb.append(")"); // in
		}
		
		// Access methods
		if (acModule.isEnabled() && (showAccessMethods == null || showAccessMethods.booleanValue())) {
			if (or) {
				sb.append(" or ");
			}
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
			sb.append("     and offer.catalogPublish = true");
			sb.append("     and offer.openAccess = false");
			sb.append("     and offer.guestAccess = false");
			sb.append("     and access.method.enabled = true");
			if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
				sb.append("     and oto.organisation.key in :offerOrganisationKeys");
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
			if (accessMethods != null && !accessMethods.isEmpty()) {
				sb.append(" and access.method in :accessMethods");
				accessMethodsUsed = true;
			}
			sb.append(")"); // in
		}
		
		sb.append(")");
		return new AddParams(offerValidAtUsed, offerOrganisationsUsed, accessMethodsUsed);
	}
	
	private void appendOrderBy(CatalogRepositoryEntrySearchParams searchParams, QueryBuilder sb) {
		OrderBy orderBy = searchParams.getOrderBy();
		boolean asc = searchParams.isOrderByAsc();
		
		if(orderBy != null) {
			switch(orderBy) {
				case key:
					sb.append(" order by v.key");
					appendAsc(sb, asc);
					break;
				case type:
					sb.append(" order by res.resName ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc, v.key asc");
					break;
				case displayName:
					sb.append(" order by lower(v.displayname)");
					appendAsc(sb, asc).append(", v.key asc");	
					break;
				case externalRef:
					sb.append(" order by lower(v.externalRef)");
					appendAsc(sb, asc).append(" nulls last, v.key asc");
					break;
				case externalId:
					sb.append(" order by lower(v.externalId)");
					appendAsc(sb, asc).append(" nulls last, v.key asc");
					break;
				case lifecycleLabel:
					sb.append(" order by lifecycle.label");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case lifecycleSoftkey:
					sb.append(" order by lifecycle.softKey");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case lifecycleStart:
					sb.append(" order by lifecycle.validFrom ")
					  .appendAsc(asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case lifecycleEnd:
					sb.append(" order by lifecycle.validTo ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case location:
					sb.append(" order by lower(v.location)");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case publishedDate:
					sb.append(" order by v.statusPublishedDate ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case popularCourses:
					sb.append(" order by popularCourses ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc, v.key asc");
					break;
				case random:
					sb.append(" order by ").append(PersistenceHelper.getOrderByRandom(dbInstance));
					break;
				default:
					if(asc) {
						sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last, v.key asc");
					} else {
						sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last, v.key asc");
					}
					break;
			}
		} else {
			if(asc) {
				sb.append(" order by lower(v.displayname) asc, lifecycle.validFrom desc nulls last, lower(v.externalRef) asc nulls last, v.key asc");
			} else {
				sb.append(" order by lower(v.displayname) desc, lifecycle.validFrom desc nulls last, lower(v.externalRef) desc nulls last, v.key asc");
			}
		}
	}
	
	private final QueryBuilder appendAsc(QueryBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
	
	private final static class AddParams {
		
		private static final AddParams ALL_FALSE = new AddParams(false, false, false);
		
		private final boolean offerValidAt;
		private final boolean offerOrganisations;
		private final boolean accessMethods;
		
		public AddParams(boolean offerValidAt, boolean offerOrganisations, boolean accessMethods) {
			this.offerValidAt = offerValidAt;
			this.offerOrganisations = offerOrganisations;
			this.accessMethods = accessMethods;
		}
		
		public boolean isOfferValidAt() {
			return offerValidAt;
		}
		
		public boolean isOfferOrganisations() {
			return offerOrganisations;
		}

		public boolean isAccessMethods() {
			return accessMethods;
		}
		
	}
}