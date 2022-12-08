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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.olat.modules.catalog.CatalogSearchTerm;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

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
	
	public List<String> loadMainLangauages(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct v.mainLanguage");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		sb.and().append("v.mainLanguage != null");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		List<String> resultList = query.getResultList();
		resultList.removeIf(String::isBlank);
		return resultList;
	}
	
	public List<String> loadExpendituresOfWork(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct v.expenditureOfWork");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		sb.and().append("v.expenditureOfWork != null");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		List<String> resultList = query.getResultList();
		resultList.removeIf(String::isBlank);
		return resultList;
	}
	
	public List<String> loadLocations(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct v.location");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		sb.and().append("v.location != null");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		List<String> resultList = query.getResultList();
		resultList.removeIf(String::isBlank);
		return resultList;
	}

	public List<RepositoryEntryLifecycle> loadPublicLifecycles(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct lifecycle");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		sb.append(" inner join v.lifecycle as lifecycle");
		sb.and().append("lifecycle.privateCycle = false");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, false, false);
		
		TypedQuery<RepositoryEntryLifecycle> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryLifecycle.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		return query.getResultList();
	}
	
	public List<Long> loadLicenseTypeKeys(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct lic.licenseType.key");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, false, true);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		return query.getResultList();
	}
	
	public List<Long> loadTaxonomyLevelKeysWithOffers(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct reToTax.taxonomyLevel.key");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join repositoryentrytotaxonomylevel as reToTax");
		sb.append("         on reToTax.entry.key = v.key");
		sb.append(" inner join v.olatResource as res");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		sb.and().append(" reToTax.taxonomyLevel.key != null");
		sb.append(" group by reToTax.taxonomyLevel.key");
		sb.append(" having count(*) > 0");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		return query.getResultList();
	}
	
	public List<String> loadTaxonomyLevelPathKeysWithOffers(CatalogRepositoryEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct reToTax.taxonomyLevel.materializedPathKeys");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join repositoryentrytotaxonomylevel as reToTax");
		sb.append("         on reToTax.entry.key = v.key");
		sb.append(" inner join v.olatResource as res");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		sb.append(" group by reToTax.taxonomyLevel.materializedPathKeys");
		sb.append(" having count(*) > 0");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		return query.getResultList();
	}
	
	public Integer countRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams)  {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select count(v.key) ");
		sb.append(" from repositoryentry as v");
		sb.append(" inner join v.olatResource as res");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, true, false);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParams(searchParams, query, addParams, false);
		Number count = query.getSingleResult();
		return Integer.valueOf(count.intValue());
	}

	public List<RepositoryEntry> loadRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder(2048);
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
		sb.append("  left join fetch v.lifecycle as lifecycle");
		sb.append("  left join fetch v.educationalType as educationalType");
		AddParams addParams = new AddParams();
		appendWhere(searchParams, sb, addParams, false, false);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFlushMode(FlushModeType.COMMIT)
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		appendParams(searchParams, query, addParams, true);
		
		return query.getResultList().stream()
				.map(result -> (RepositoryEntry)result[0])
				.collect(Collectors.toList());
	}
	
	private void appendWhere(CatalogRepositoryEntrySearchParams searchParams, QueryBuilder sb, AddParams addParams,
			boolean addFromLifecycle, boolean addFormLicense) {
		if (addFormLicense || (searchParams.getLicenseTypeKeys() != null && !searchParams.getLicenseTypeKeys().isEmpty())) {
			sb.append(" inner join license as lic");
			sb.append("    on lic.resId = res.resId");
			sb.append("   and lic.resName = res.resName");
		}
		if (addFromLifecycle
				&& ((searchParams.getLifecyclesPublicKeys() != null && searchParams.getLifecyclesPublicKeys().isEmpty())
						|| searchParams.getLifecyclesPrivateFrom() != null
						|| searchParams.getLifecyclesPrivateTo() != null)) {
			sb.append(" left join v.lifecycle as lifecycle");
		}
		
		appendMyViewAccessSubSelect(sb, addParams, searchParams.isGuestOnly(),
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
			addParams.setResourceTypes(resourceTypes);
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
			addParams.setEducationalTypeKeys(educationalTypeKeys);
		}
		
		for (Entry<String, List<TaxonomyLevel>> identToTaxonomyLevels : searchParams.getIdentToTaxonomyLevels().entrySet()) {
			if (CatalogRepositoryEntrySearchParams.KEY_LAUNCHER.equals(identToTaxonomyLevels.getKey())
					&& searchParams.getIdentToTaxonomyLevels().keySet().contains(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER_OVERRIDE)) {
				// To not apply the launcher key if it is overridden by a filter
			} else if (identToTaxonomyLevels.getValue() != null && !identToTaxonomyLevels.getValue().isEmpty()) {
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
		
		if (searchParams.getMainLanguages() != null && !searchParams.getMainLanguages().isEmpty()) {
			sb.and().append("v.mainLanguage in :mainLangauges");
		}
		
		if (searchParams.getExpendituresOfWork() != null && !searchParams.getExpendituresOfWork().isEmpty()) {
			sb.and().append("v.expenditureOfWork in :expenditureOfWorks");
		}
		
		if (searchParams.getLocations() != null && !searchParams.getLocations().isEmpty()) {
			sb.and().append("v.location in :locations");
		}
		
		if (searchParams.getLicenseTypeKeys() != null && !searchParams.getLicenseTypeKeys().isEmpty()) {
			sb.and().append("lic.licenseType.key in :licenseTypeKeys");
		}
		
		if (searchParams.getLifecyclesPublicKeys() != null && !searchParams.getLifecyclesPublicKeys().isEmpty()) {
			sb.and().append("v.lifecycle.key in :lifecyclePublicKeys");
		}
		
		if (searchParams.getLifecyclesPrivateFrom() != null) {
			sb.and().append("lifecycle.privateCycle = true");
			sb.and().append("date(lifecycle.validFrom) >= date(:lifecyclePrivateFrom)");
		}
		
		if (searchParams.getLifecyclesPrivateTo() != null) {
			sb.and().append("lifecycle.privateCycle = true");
			sb.and().append("date(lifecycle.validTo) <= date(:lifecyclePrivateTo)");
		}
		
		String author = searchParams.getAuthor();
		if (StringHelper.containsNonWhitespace(author)) {
			author = PersistenceHelper.makeFuzzyQueryString(author);

			sb.and().append(" (v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership, ");
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
			addParams.setAuthor(author);
		}
		
		Map<String, String> paramToSearchText = null;
		Map<String, Collection<String>> paramToTaxonomyLevelI18nSuffix = null;
		if (searchParams.getSearchTerms() != null && !searchParams.getSearchTerms().isEmpty()) {
			paramToSearchText = new HashMap<>(searchParams.getSearchTerms().size());
			paramToTaxonomyLevelI18nSuffix = new HashMap<>(searchParams.getSearchTerms().size());
			
			for (int i = 0; i < searchParams.getSearchTerms().size(); i++) {
				CatalogSearchTerm searchTerm = searchParams.getSearchTerms().get(i);
				
				String fuzzySearchText = PersistenceHelper.makeFuzzyQueryString(searchTerm.getText());
				String paramName = "displaytext" + i;
				paramToSearchText.put(paramName, fuzzySearchText);
				
				sb.and().append("(");
				PersistenceHelper.appendFuzzyLike(sb, "v.displayname", paramName, dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "v.description", paramName, dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "v.objectives", paramName, dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "v.authors", paramName, dbInstance.getDbVendor());
				
				if (searchTerm.getTaxonomyLevelI18nSuffix() != null && !searchTerm.getTaxonomyLevelI18nSuffix().isEmpty()) {
					String taxParamName = "serachTaxonomyLevelI18nSuffix" + i;
					sb.append(" or v.key in (select reToTax.entry.key from repositoryentrytotaxonomylevel as reToTax");
					sb.append("  where reToTax.taxonomyLevel.i18nSuffix in :").append(taxParamName).append(")");
					paramToTaxonomyLevelI18nSuffix.put(taxParamName, searchTerm.getTaxonomyLevelI18nSuffix());
				}
				sb.append(")");
			}
		}
		addParams.setParamToSearchText(paramToSearchText);
		addParams.setParamToTaxonomyLevelI18nSuffix(paramToTaxonomyLevelI18nSuffix);
	}

	private void appendParams(CatalogRepositoryEntrySearchParams searchParams, TypedQuery<?> query, AddParams addParams, boolean selectRepositoryEntries) {
		if (addParams.isOfferValidAt() && searchParams.getOfferValidAt() != null) {
			query.setParameter( "offerValidAt", searchParams.getOfferValidAt());
		}
		if (addParams.isOfferOrganisations() && searchParams.getOfferOrganisations() != null && !searchParams.getOfferOrganisations().isEmpty()) {
			query.setParameter("offerOrganisationKeys", searchParams.getOfferOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		if (searchParams.getRepositoryEntryKeys() != null && !searchParams.getRepositoryEntryKeys().isEmpty()) {
			query.setParameter("repositoryEntryKeys", searchParams.getRepositoryEntryKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			Collection<String> status = searchParams.getStatus().stream().map(RepositoryEntryStatusEnum::name).collect(Collectors.toList());
			query.setParameter("status", status);
		}
		if (addParams.getResourceTypes() != null) {
			query.setParameter("resourceTypes", addParams.getResourceTypes());
		}
		if (addParams.getEducationalTypeKeys() != null) {
			query.setParameter("educationalTypeKeys", addParams.getEducationalTypeKeys());
		}
		for (Entry<String, List<TaxonomyLevel>> identToTaxonomyLevels : searchParams.getIdentToTaxonomyLevels().entrySet()) {
			if (CatalogRepositoryEntrySearchParams.KEY_LAUNCHER.equals(identToTaxonomyLevels.getKey())
					&& searchParams.getIdentToTaxonomyLevels().keySet().contains(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER_OVERRIDE)) {
				// To not apply the launcher key if it is overridden by a filter
			} else if (identToTaxonomyLevels.getValue() != null && !identToTaxonomyLevels.getValue().isEmpty()) {
				if (searchParams.isTaxonomyLevelChildren()) {
					for (int i = 0; i < identToTaxonomyLevels.getValue().size(); i++) {
						String parameter = new StringBuilder().append("materializedPath").append(identToTaxonomyLevels.getKey()).append("_").append(i).toString();
						String pathKeys = identToTaxonomyLevels.getValue().get(i).getMaterializedPathKeys();
						query.setParameter(parameter, pathKeys + "%");
					}
				} else {
					Collection<Long> keys = identToTaxonomyLevels.getValue().stream().map(TaxonomyLevel::getKey).collect(Collectors.toList());
					query.setParameter("taxonomyLevelKeys" + identToTaxonomyLevels.getKey(), keys );
				}
			}
		}
		if (searchParams.getMainLanguages() != null && !searchParams.getMainLanguages().isEmpty()) {
			query.setParameter("mainLangauges", searchParams.getMainLanguages());
		}
		if (searchParams.getExpendituresOfWork() != null && !searchParams.getExpendituresOfWork().isEmpty()) {
			query.setParameter("expenditureOfWorks", searchParams.getExpendituresOfWork());
		}
		if (searchParams.getLocations() != null && !searchParams.getLocations().isEmpty()) {
			query.setParameter("locations", searchParams.getLocations());
		}
		if (searchParams.getLicenseTypeKeys() != null && !searchParams.getLicenseTypeKeys().isEmpty()) {
			query.setParameter("licenseTypeKeys", searchParams.getLicenseTypeKeys());
		}
		if (searchParams.getLifecyclesPublicKeys() != null && !searchParams.getLifecyclesPublicKeys().isEmpty()) {
			query.setParameter("lifecyclePublicKeys", searchParams.getLifecyclesPublicKeys());
		}
		if (searchParams.getLifecyclesPrivateFrom() != null) {
			query.setParameter("lifecyclePrivateFrom", searchParams.getLifecyclesPrivateFrom());
		}
		if (searchParams.getLifecyclesPrivateTo() != null) {
			query.setParameter("lifecyclePrivateTo", searchParams.getLifecyclesPrivateTo());
		}
		if (addParams.isAccessMethods() && searchParams.getAccessMethods() != null && !searchParams.getAccessMethods().isEmpty()) {
			query.setParameter("accessMethods", searchParams.getAccessMethods());
		}
		if (addParams.getAuthor() != null) {
			query.setParameter("author", addParams.getAuthor());
		}
		Map<String, String> paramToSearchText = addParams.getParamToSearchText();
		if (paramToSearchText != null && !paramToSearchText.isEmpty()) {
			paramToSearchText.entrySet().stream().forEach(entrySet -> query.setParameter(entrySet.getKey(), entrySet.getValue()));
		}
		Map<String,Collection<String>> paramToTaxonomyLevelI18nSuffix = addParams.getParamToTaxonomyLevelI18nSuffix();
		if (paramToTaxonomyLevelI18nSuffix!= null && !paramToTaxonomyLevelI18nSuffix.isEmpty()) {
			paramToTaxonomyLevelI18nSuffix.entrySet().stream().forEach(entrySet -> query.setParameter(entrySet.getKey(), entrySet.getValue()));
		}
		if (selectRepositoryEntries && OrderBy.popularCourses == searchParams.getOrderBy()) {
			query.setParameter("statDay", DateUtils.addDays(new Date(), -28), TemporalType.DATE);
		}
	}
	
	private void appendMyViewAccessSubSelect(QueryBuilder sb, AddParams addParams, boolean isGuestOnly,
			Date offerValidAt, List<? extends OrganisationRef> offerOrganisations, Boolean openAccess,
			Boolean showAccessMethods, Collection<AccessMethod> accessMethods) {
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
			return;
		}
		
		boolean or = false;
		
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
				addParams.setOfferOrganisations(true);
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
				addParams.setOfferOrganisations(true);
			}
			if (offerValidAt != null) {
				sb.append(" and (");
				sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD_PERIOD);
				sb.append(" and (offer.validFrom is not null or offer.validTo is not null)");
				sb.append(" and (offer.validFrom is null or date(offer.validFrom)<=:offerValidAt)");
				sb.append(" and (offer.validTo is null or date(offer.validTo)>=:offerValidAt)");
				sb.append(" or");
				sb.append(" re2.status ").in(ACService.RESTATUS_ACTIVE_METHOD);
				sb.append(" and offer.validFrom is null and offer.validTo is null");
				sb.append(" )");
				addParams.setOfferValidAt(true);
			}
			if (accessMethods != null && !accessMethods.isEmpty()) {
				sb.append(" and access.method in :accessMethods");
				addParams.setAccessMethods(true);
			}
			sb.append(")"); // in
		}
		
		sb.append(")");
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
		
		private boolean offerValidAt;
		private boolean offerOrganisations;
		private boolean accessMethods;
		private String author;
		private Set<String> resourceTypes;
		private Set<Long> educationalTypeKeys;
		private Map<String, String> paramToSearchText;
		private Map<String, Collection<String>> paramToTaxonomyLevelI18nSuffix;
		
		public boolean isOfferValidAt() {
			return offerValidAt;
		}
		
		public void setOfferValidAt(boolean offerValidAt) {
			this.offerValidAt = offerValidAt;
		}
		
		public boolean isOfferOrganisations() {
			return offerOrganisations;
		}
		
		public void setOfferOrganisations(boolean offerOrganisations) {
			this.offerOrganisations = offerOrganisations;
		}
		
		public boolean isAccessMethods() {
			return accessMethods;
		}
		
		public void setAccessMethods(boolean accessMethods) {
			this.accessMethods = accessMethods;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public Set<String> getResourceTypes() {
			return resourceTypes;
		}

		public void setResourceTypes(Set<String> resourceTypes) {
			this.resourceTypes = resourceTypes;
		}

		public Set<Long> getEducationalTypeKeys() {
			return educationalTypeKeys;
		}

		public void setEducationalTypeKeys(Set<Long> educationalTypeKeys) {
			this.educationalTypeKeys = educationalTypeKeys;
		}

		public Map<String, String> getParamToSearchText() {
			return paramToSearchText;
		}

		public void setParamToSearchText(Map<String, String> paramToSearchText) {
			this.paramToSearchText = paramToSearchText;
		}

		public Map<String, Collection<String>> getParamToTaxonomyLevelI18nSuffix() {
			return paramToTaxonomyLevelI18nSuffix;
		}

		public void setParamToTaxonomyLevelI18nSuffix(Map<String, Collection<String>> paramToTaxonomyLevelI18nSuffix) {
			this.paramToTaxonomyLevelI18nSuffix = paramToTaxonomyLevelI18nSuffix;
		}
		
	}

}