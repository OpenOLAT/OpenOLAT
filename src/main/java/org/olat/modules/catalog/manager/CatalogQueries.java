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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 May 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogQueries {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AccessControlModule acModule;

	public List<RepositoryEntryInfos> loadRepositoryEntries(CatalogEntrySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select new org.olat.modules.catalog.model.RepositoryEntryInfos(v, stats, cerconfig, cpconfig)")
		  .append(" from repositoryentry as v")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join fetch v.statistics as stats")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" left join fetch v.educationalType as educationalType")
		  .append(" left join certificateentryconfig as cerconfig on (cerconfig.entry.key=v.key)")
		  .append(" left join creditpointrepositoryentry as cpconfig on (cpconfig.repositoryEntry.key=v.key)");
		AddParams addParams = new AddParams();
		appendWhereRE(searchParams, sb, addParams);

		TypedQuery<RepositoryEntryInfos> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryInfos.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParamsRE(searchParams, query, addParams);

		return query.getResultList();
	}

	private void appendWhereRE(CatalogEntrySearchParams searchParams, QueryBuilder sb, AddParams addParams) {
		appendAccessSubSelectRE(sb, addParams, searchParams.isWebPublish(), searchParams.isGuestOnly(),
				searchParams.getOfferValidAt(), searchParams.getOfferOrganisations());

		if (searchParams.getResourceKeys() != null && !searchParams.getResourceKeys().isEmpty()) {
			sb.and().append("res.key in :resourceKeys");
		}
		
		if (searchParams.getLauncherResourceTypes() != null && !searchParams.getLauncherResourceTypes().isEmpty()) {
			sb.and().append("res.resName in :resourceTypes");
		}
		
		if (searchParams.getLauncherEducationalTypeKeys() != null && !searchParams.getLauncherEducationalTypeKeys().isEmpty()) {
			sb.and().append("v.educationalType.key in :educationalTypeKeys");
		}
		
		if (searchParams.getLauncherTaxonomyLevels() != null && !searchParams.getLauncherTaxonomyLevels().isEmpty()) {
			sb.and().append(" exists (select reToTax.key from repositoryentrytotaxonomylevel as reToTax");
			sb.append("  where reToTax.entry.key=v.key");
			sb.append("    and");
			for (int i = 0; i < searchParams.getLauncherTaxonomyLevels().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append(" reToTax.taxonomyLevel.materializedPathKeys like :materializedPath").append("_").append(i);
				if (i == searchParams.getLauncherTaxonomyLevels().size() - 1) {
					sb.append(")");
				}
			}
			sb.append(")");
		}
	}

	private void appendAccessSubSelectRE(QueryBuilder sb, AddParams addParams, boolean webPublish,
			boolean isGuestOnly, Date offerValidAt, List<? extends OrganisationRef> offerOrganisations) {
		boolean or = false;

		sb.and().append("(");

		if (isGuestOnly || webPublish) {
			or = true;
			sb.append(" res.key in (");
			sb.append("   select resource.key");
			sb.append("     from acoffer offer");
			sb.append("     inner join offer.resource resource");
			sb.append("     inner join repositoryentry re2");
			sb.append("        on re2.olatResource.key = resource.key");
			sb.append("       and re2.publicVisible = true");
			sb.append("    where offer.valid = true");
			sb.append("      and offer.guestAccess = true");
			if (webPublish) {
				sb.append("  and offer.catalogWebPublish = true");
			}
			sb.append("      and re2.status ").in(ACService.RESTATUS_ACTIVE_GUEST);
			sb.append(")");
			if (isGuestOnly) {
				sb.append(")");
				return;
			}
		}

		// Open access
		if (or) {
			sb.append(" or ");
		}
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
		if (webPublish) {
			sb.append("  and offer.catalogWebPublish = true");
		}
		sb.append("      and re2.status ").in(ACService.RESTATUS_ACTIVE_OPEN);
		if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
			sb.append("      and oto.organisation.key in :offerOrganisationKeys");
			addParams.setOfferOrganisations(true);
		}
		sb.append(")"); // in

		// Access methods
		if (acModule.isEnabled()) {
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
			if (webPublish) {
				sb.append(" and offer.catalogWebPublish = true");
			}
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
			sb.append(")"); // in
		}

		sb.append(")");
	}

	private void appendParamsRE(CatalogEntrySearchParams searchParams, TypedQuery<?> query, AddParams addParams) {
		if (addParams.isOfferValidAt() && searchParams.getOfferValidAt() != null) {
			query.setParameter("offerValidAt", searchParams.getOfferValidAt());
		}
		if (addParams.isOfferOrganisations() && searchParams.getOfferOrganisations() != null
				&& !searchParams.getOfferOrganisations().isEmpty()) {
			query.setParameter("offerOrganisationKeys", searchParams.getOfferOrganisations().stream()
					.map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		if (searchParams.getResourceKeys() != null && !searchParams.getResourceKeys().isEmpty()) {
			query.setParameter("resourceKeys", searchParams.getResourceKeys());
		}
		if (searchParams.getLauncherResourceTypes() != null && !searchParams.getLauncherResourceTypes().isEmpty()) {
			query.setParameter("resourceTypes", searchParams.getLauncherResourceTypes());
		}
		if (searchParams.getLauncherEducationalTypeKeys() != null && !searchParams.getLauncherEducationalTypeKeys().isEmpty()) {
			query.setParameter("educationalTypeKeys", searchParams.getLauncherEducationalTypeKeys());
		}
		if (searchParams.getLauncherTaxonomyLevels() != null && !searchParams.getLauncherTaxonomyLevels().isEmpty()) {
			for (int i = 0; i < searchParams.getLauncherTaxonomyLevels().size(); i++) {
				String parameter = new StringBuilder().append("materializedPath").append("_").append(i).toString();
				String pathKeys = searchParams.getLauncherTaxonomyLevels().get(i).getMaterializedPathKeys();
				query.setParameter(parameter, pathKeys + "%");
			}
		}
	}

	private final static class AddParams {

		private boolean offerValidAt;
		private boolean offerOrganisations;

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

	}

	public List<CurriculumElement> loadCurriculumElements(CatalogEntrySearchParams searchParams) {
		if (searchParams.isGuestOnly() || !acModule.isEnabled()) {
			return List.of();
		}
		
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select v");
		sb.append(" from curriculumelement as v");
		sb.append(" inner join v.curriculum as curriculum");
		sb.append(" inner join fetch v.resource as res");
		sb.append("  left join fetch v.type as type");
		sb.append("  left join fetch v.educationalType as educationalType");
		AddParams addParams = new AddParams();
		appendWhereCE(searchParams, sb, addParams);

		TypedQuery<CurriculumElement> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CurriculumElement.class)
				.setFlushMode(FlushModeType.COMMIT);
		appendParamsCE(searchParams, query, addParams);

		return query.getResultList();
	}

	private void appendWhereCE(CatalogEntrySearchParams searchParams, QueryBuilder sb, AddParams addParams) {
		appendAccessSubSelectCE(sb, addParams, searchParams.isWebPublish(), searchParams.getOfferValidAt(),
				searchParams.getOfferOrganisations());
		sb.and().append("(curriculum.status is null or curriculum.status ").in(CurriculumStatus.active.name()).append(")");
		
		if (searchParams.getResourceKeys() != null && !searchParams.getResourceKeys().isEmpty()) {
			sb.and().append("res.key in :resourceKeys");
		}
		
		if (searchParams.getLauncherResourceTypes() != null && !searchParams.getLauncherResourceTypes().isEmpty()) {
			sb.and().append("res.resName in :resourceTypes");
		}
		
		if (searchParams.getLauncherEducationalTypeKeys() != null && !searchParams.getLauncherEducationalTypeKeys().isEmpty()) {
			sb.and().append("v.educationalType.key in :educationalTypeKeys");
		}
		
		if (searchParams.getLauncherTaxonomyLevels() != null && !searchParams.getLauncherTaxonomyLevels().isEmpty()) {
			sb.and().append(" exists (select ceToTax.key from curriculumelementtotaxonomylevel as ceToTax");
			sb.append("  where ceToTax.curriculumElement.key=v.key");
			sb.append("    and");
			for (int i = 0; i < searchParams.getLauncherTaxonomyLevels().size(); i++) {
				if (i == 0) {
					sb.append("(");
				} else {
					sb.append(" or ");
				}
				sb.append(" ceToTax.taxonomyLevel.materializedPathKeys like :materializedPath").append("_").append(i);
				if (i == searchParams.getLauncherTaxonomyLevels().size() - 1) {
					sb.append(")");
				}
			}
			sb.append(")");
		}
	}

	private void appendAccessSubSelectCE(QueryBuilder sb, AddParams addParams, boolean webPublish,
			Date offerValidAt, List<? extends OrganisationRef> offerOrganisations) {
		
		sb.and().append("(");

		// Access methods
		sb.append(" res.key in (");
		sb.append("   select resource.key");
		sb.append("     from acofferaccess access");
		sb.append("     inner join access.offer offer");
		sb.append("     inner join offer.resource resource");
		sb.append("     inner join curriculumelement ce2");
		sb.append("        on ce2.resource.key = resource.key");
		sb.append("     inner join offertoorganisation oto");
		sb.append("        on oto.offer.key = offer.key");
		sb.append("   where offer.valid = true");
		sb.append("     and offer.catalogPublish = true");
		if (webPublish) {
			sb.append(" and offer.catalogWebPublish = true");
		}
		sb.append("     and offer.openAccess = false");
		sb.append("     and offer.guestAccess = false");
		sb.append("     and access.method.enabled = true");
		if (offerOrganisations != null && !offerOrganisations.isEmpty()) {
			sb.append(" and oto.organisation.key in :offerOrganisationKeys");
			addParams.setOfferOrganisations(true);
		}
		if (offerValidAt != null) {
			sb.append(" and (");
			sb.append(" ce2.status ").in(ACService.CESTATUS_ACTIVE_METHOD_PERIOD);
			sb.append(" and (offer.validFrom is not null or offer.validTo is not null)");
			sb.append(" and (offer.validFrom is null or date(offer.validFrom)<=:offerValidAt)");
			sb.append(" and (offer.validTo is null or date(offer.validTo)>=:offerValidAt)");
			sb.append(" or");
			sb.append(" ce2.status ").in(ACService.CESTATUS_ACTIVE_METHOD);
			sb.append(" and offer.validFrom is null and offer.validTo is null");
			sb.append(" )");
			addParams.setOfferValidAt(true);
		}
		sb.append(")"); // in

		sb.append(")");
	}

	private void appendParamsCE(CatalogEntrySearchParams searchParams, TypedQuery<?> query, AddParams addParams) {
		if (addParams.isOfferValidAt() && searchParams.getOfferValidAt() != null) {
			query.setParameter("offerValidAt", searchParams.getOfferValidAt());
		}
		if (addParams.isOfferOrganisations() && searchParams.getOfferOrganisations() != null
				&& !searchParams.getOfferOrganisations().isEmpty()) {
			query.setParameter("offerOrganisationKeys", searchParams.getOfferOrganisations().stream()
					.map(OrganisationRef::getKey).collect(Collectors.toList()));
		}
		if (searchParams.getResourceKeys() != null && !searchParams.getResourceKeys().isEmpty()) {
			query.setParameter("resourceKeys", searchParams.getResourceKeys());
		}
		if (searchParams.getLauncherResourceTypes() != null && !searchParams.getLauncherResourceTypes().isEmpty()) {
			query.setParameter("resourceTypes", searchParams.getLauncherResourceTypes());
		}
		if (searchParams.getLauncherEducationalTypeKeys() != null && !searchParams.getLauncherEducationalTypeKeys().isEmpty()) {
			query.setParameter("educationalTypeKeys", searchParams.getLauncherEducationalTypeKeys());
		}
		if (searchParams.getLauncherTaxonomyLevels() != null && !searchParams.getLauncherTaxonomyLevels().isEmpty()) {
			for (int i = 0; i < searchParams.getLauncherTaxonomyLevels().size(); i++) {
				String parameter = new StringBuilder().append("materializedPath").append("_").append(i).toString();
				String pathKeys = searchParams.getLauncherTaxonomyLevels().get(i).getMaterializedPathKeys();
				query.setParameter(parameter, pathKeys + "%");
			}
		}
	}

}