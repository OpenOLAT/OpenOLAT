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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryAuthorViewResults;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryAuthorImpl;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.ResourceUsage;
import org.olat.user.UserImpl;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Queries for the view "RepositoryEntryMyCourseView" dedicated to the "My course" feature.
 * The identity is a mandatory parameter.
 * 
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryAuthorQueries {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryAuthorQueries.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumModule curriculumModule;
	
	public int countViews(SearchAuthorRepositoryEntryViewParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}

		TypedQuery<Number> query = createViewQuery(params, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	public RepositoryEntryAuthorViewResults searchViews(SearchAuthorRepositoryEntryViewParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return new RepositoryEntryAuthorViewResults(Collections.emptyList(), true);
		}

		TypedQuery<Object[]> query = createViewQuery(params,  Object[].class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> objects =  query.getResultList();
		List<RepositoryEntryAuthorView> views = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			RepositoryEntry re = (RepositoryEntry)object[0];
			
			Number numOfMarks = (Number)object[1];
			boolean hasMarks = numOfMarks != null && numOfMarks.longValue() > 0;

			long offers = PersistenceHelper.extractPrimitiveLong(object, 2);
			int references = PersistenceHelper.extractPrimitiveInt(object, 3);
			int curriculumElements = PersistenceHelper.extractPrimitiveInt(object, 4);
			
			Number numOfBinders = (Number)object[5];
			references += numOfBinders == null ? 0 : numOfBinders.intValue();

			boolean lectureEnabled = false;
			boolean rollCallEnabled = false;
			if(object.length > 5) {
				lectureEnabled = PersistenceHelper.extractBoolean(object, 6, false);
				rollCallEnabled = PersistenceHelper.extractBoolean(object, 7, false);
			}
			
			String deletedByName = null;
			if(params.hasStatus(RepositoryEntryStatusEnum.trash)) {
				Identity deletedBy = re.getDeletedBy();
				if(deletedBy != null) {
					deletedByName = userManager.getUserDisplayName(deletedBy);
				}
			}
			
			views.add(new RepositoryEntryAuthorImpl(re, hasMarks, offers,
					references, curriculumElements, deletedByName,
					lectureEnabled, rollCallEnabled));
		}
		return new RepositoryEntryAuthorViewResults(views, maxResults <= 0);
	}

	private <T> TypedQuery<T> createViewQuery(SearchAuthorRepositoryEntryViewParams params, Class<T> type) {

		IdentityRef identity = params.getIdentity();
		List<String> resourceTypes = params.getResourceTypes();
		boolean oracle = "oracle".equals(dbInstance.getDbVendor());

		boolean count = Number.class.equals(type);
		boolean needIdentity = false;
		QueryBuilder sb = new QueryBuilder(2048);
		if(count) {
			sb.append("select count(v.key) ")
			  .append(" from repositoryentry as v")
			  .append(" inner join v.olatResource as res")
			  .append(" left join v.lifecycle as lifecycle");
		} else {
			sb.append("select v, ");
			if(params.getMarked() != null && params.getMarked().booleanValue()) {
				sb.append(" 1 as marks,");
			} else {
				sb.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
				  .append("   where mark.creator.key=:identityKey and mark.resId=v.key and mark.resName='RepositoryEntry'")
				  .append(" ) as marks,");
				needIdentity = true;
			}
			sb.append(" (select count(offer.key) from acoffer as offer ")
			  .append("   where offer.resource.key=res.key and offer.valid=true")
			  .append(" ) as offers,")
			  .append(" (select count(ref.key) from references as ref ")
			  .append("   where ref.target.key=res.key")
			  .append(" ) as references,");
			
			if(curriculumModule.isEnabled()) {
				sb.append(" (select count(curel.key) from curriculumelement as curel")
				  .append("   inner join curel.group as curElGroup")
				  .append("   inner join repoentrytogroup as reToCurElGroup on (curElGroup.key=reToCurElGroup.group.key)")
				  .append("   where reToCurElGroup.entry.key=v.key")
				  .append(" ) as curriculumElements,");
			} else {
				sb.append(" 0 as curriculumElements,");
			}
			
			if(params.includeResourceType("BinderTemplate")) {
				sb.append(" (select count(binder.key) from pfbinder as binder")
				  .append("   inner join binder.template as template")
				  .append("   where res.resName='BinderTemplate' and res.key=template.olatResource.key")
				  .append(" ) as binders");
			} else {
				sb.append(" 0 as binders");
			}
			if(lectureModule.isEnabled()) {
				sb.append(", lectureConfig.lectureEnabled")
				  .append(", case when lectureConfig.rollCallEnabled=true then true else ").append(lectureModule.isRollCallDefaultEnabled()).append(" end as rollCallEnabled");
			}
			sb.append(" from repositoryentry as v")
			  .append(" inner join ").append(oracle ? "" : "fetch").append(" v.olatResource as res")
			  .append(" inner join fetch v.statistics as stats")
			  .append(" left join fetch v.educationalType as educationalType")
			  .append(" left join fetch v.lifecycle as lifecycle ");
			if(params.hasStatus(RepositoryEntryStatusEnum.trash)) {
				sb.append(" left join fetch v.deletedBy as deletedBy")
				  .append(" left join fetch deletedBy.user as deletedByUser");
			}
			if(lectureModule.isEnabled()) {
				sb.append(" left join fetch lectureentryconfig as lectureConfig on (lectureConfig.entry.key=v.key) ");
			}
		}

		sb.append(" where");
		
		needIdentity |= appendAccessSubSelect(sb, params);

		if (params.getOerRelease() != null && params.getOerRelease() != SearchAuthorRepositoryEntryViewParams.OERRelease.all) {
			if (params.getOerRelease() == SearchAuthorRepositoryEntryViewParams.OERRelease.notReleased) {
				sb.append(" and v.canIndexMetadata=false");
			} else {
				sb.append(" and v.canIndexMetadata=true");
			}
		}
		
		if(params.getResourceUsage() != null && params.getResourceUsage() != ResourceUsage.all) {
			sb.append(" and res.resName!='CourseModule' and");	
			if(params.getResourceUsage() == ResourceUsage.notUsed) {
				sb.append(" not");
			}
			sb.append(" exists (select ref.key from references as ref where ref.target.key=res.key)");
		}

		if (params.isResourceTypesDefined()) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		if (params.isTechnicalTypeDefined()) {
			sb.append(" and v.technicalType in (:technicalTypes)");
		}
		if (params.isEducationalTypeDefined()) {
			sb.append(" and v.educationalType.key in (:educationalTypeKeys)");
		}
		if(params.getMarked() != null && params.getMarked().booleanValue()) {
			needIdentity = true;
			sb.append(" and exists (select mark2.key from ").append(MarkImpl.class.getName()).append(" as mark2 ")
			  .append("   where mark2.creator.key=:identityKey and mark2.resId=v.key and mark2.resName='RepositoryEntry'")
			  .append(" )");
		}
		if (params.isLicenseTypeDefined()) {
			sb.append(" and exists (");
			sb.append(" select license.key from license as license");
			sb.append("  where license.resId=res.resId and license.resName=res.resName");
			sb.append("    and license.licenseType.key in (:licenseTypeKeys))");
		}
		if (params.isEntryOrganisationsDefined()) {
			sb.append(" and exists (select reToOrg.key from repoentrytoorganisation as reToOrg")
			  .append("  where reToOrg.entry.key=v.key and reToOrg.organisation.key in (:organisationKeys))");
		}
		if (params.getTaxonomyLevels() != null) {
			sb.append(" and exists (select reToTax.key from repositoryentrytotaxonomylevel as reToTax")
			  .append("  where reToTax.entry.key=v.key and reToTax.taxonomyLevel.key in (:taxonomyLevelKeys))");
		}
		if (params.getExcludeEntryKeys() != null && !params.getExcludeEntryKeys().isEmpty()) {
			sb.append(" and v.key not in (:excludeEntryKeys)");
		}
		
		String author = null;
		if (StringHelper.containsNonWhitespace(params.getAuthor())) { // fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(params.getAuthor());

			sb.append(" and (v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.group.key=membership.group.key and membership.identity.key=identity.key and user.identity.key=identity.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(sb, "user.firstName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.lastName", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" )) or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.authors", "author", dbInstance.getDbVendor());
			sb.append(" )");
		}

		String displayname = params.getDisplayname();
		if (StringHelper.containsNonWhitespace(displayname)) {
			if (!params.isExactSearch()) {
				displayname = PersistenceHelper.makeFuzzyQueryString(displayname);
			}
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displayname", dbInstance.getDbVendor());
		}
		
		String reference = params.getReference();
		if (StringHelper.containsNonWhitespace(reference)) {
			if (!params.isExactSearch()) {
				reference = PersistenceHelper.makeFuzzyQueryString(reference);
			}
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "externalRef", dbInstance.getDbVendor());
		}
		
		String desc = params.getDescription();
		if (StringHelper.containsNonWhitespace(desc)) {
			if (!params.isExactSearch()) {
				desc = PersistenceHelper.makeFuzzyQueryString(desc);
			}
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.description", "desc", dbInstance.getDbVendor());
		}
		
		Long id = null;
		String refs = null;
		String fuzzyRefs = null;
		if(StringHelper.containsNonWhitespace(params.getIdAndRefs())) {
			refs = params.getIdAndRefs();
			fuzzyRefs = PersistenceHelper.makeFuzzyQueryString(refs);
			sb.append(" and (v.externalId=:ref or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "fuzzyRefs", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:ref");

			if(StringHelper.isLong(refs)) {
				try {
					id = Long.parseLong(refs);
					sb.append(" or v.key=:vKey or res.resId=:vKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		//quick search
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		if(StringHelper.containsNonWhitespace(params.getIdRefsAndTitle())) {
			quickRefs = params.getIdRefsAndTitle();
			sb.append(" and (v.externalId=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "quickText", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:quickRef or ");
			quickText = PersistenceHelper.makeFuzzyQueryString(quickRefs);
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "quickText", dbInstance.getDbVendor());
			if(StringHelper.isLong(quickRefs)) {
				try {
					quickId = Long.parseLong(quickRefs);
					sb.append(" or v.key=:quickVKey or res.resId=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}

		if(!count) {
			appendAuthorViewOrderBy(params, sb);
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if (params.isResourceTypesDefined()) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(id != null) {
			dbQuery.setParameter("vKey", id);
		}
		if(refs != null) {
			dbQuery.setParameter("ref", refs);
		}
		if(fuzzyRefs != null) {
			dbQuery.setParameter("fuzzyRefs", fuzzyRefs);
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
		
		if (params.isTechnicalTypeDefined()) {
			dbQuery.setParameter("technicalTypes", params.getTechnicalTypes());
		}
		if (params.isEducationalTypeDefined()) {
			dbQuery.setParameter("educationalTypeKeys", params.getEducationalTypeKeys());
		}
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			dbQuery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(displayname)) {
			dbQuery.setParameter("displayname", displayname);
		}
		if (StringHelper.containsNonWhitespace(reference)) {
			dbQuery.setParameter("externalRef", reference);
		}
		if (StringHelper.containsNonWhitespace(desc)) {
			dbQuery.setParameter("desc", desc);
		}
		if(needIdentity) {
			dbQuery.setParameter("identityKey", identity.getKey());
		}
		if (params.isLicenseTypeDefined()) {
			dbQuery.setParameter("licenseTypeKeys", params.getLicenseTypeKeys());
		}
		if(params.isEntryOrganisationsDefined()) {
			List<Long> organisationKeys = params.getEntryOrganisation().stream()
					.map(OrganisationRef::getKey).collect(Collectors.toList());
			dbQuery.setParameter("organisationKeys", organisationKeys);
		}
		if (params.getTaxonomyLevels() != null) {
			List<Long> taxonomyLevelKeys = params.getTaxonomyLevels().stream()
					.map(TaxonomyLevelRef::getKey).collect(Collectors.toList());
			dbQuery.setParameter("taxonomyLevelKeys", taxonomyLevelKeys);
		}
		if (params.getExcludeEntryKeys() != null && !params.getExcludeEntryKeys().isEmpty()) {
			dbQuery.setParameter("excludeEntryKeys", params.getExcludeEntryKeys());
		}
		return dbQuery;
	}
	
	private boolean appendAccessSubSelect(QueryBuilder sb, SearchAuthorRepositoryEntryViewParams params) {
		if(dbInstance.isMySQL()) {
			sb.append(" v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("     where rel.group.key=membership.group.key and rel.entry.key=v.key and membership.identity.key=:identityKey");
		} else {
			sb.append(" exists (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("     where rel.group.key=membership.group.key and rel.entry.key=v.key and membership.identity.key=:identityKey");
		}
		
		if(params.isOwnedResourcesOnly()) {
			sb.append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
			  .append(" ) and v.status");
			if(params.hasStatus()) {
				sb.in(params.getStatus());
			} else {
				sb.in(RepositoryEntryStatusEnum.preparationToClosed());
			}			
		} else {
			Roles roles = params.getRoles();
			if(roles == null) {
				sb.append(" and membership.role ").in( GroupRoles.owner)
				  .append("   and v.status ");
				if(params.hasStatus()) {
					sb.in(params.getStatus());
				} else {
					sb.in(RepositoryEntryStatusEnum.preparationToClosed());
				} 
				sb.append(" )");
				
			} else {
				sb.append(" and (")
				  // owner, principal, learn resource manager and administrator which can see all
				  .append("     ( membership.role ").in(OrganisationRoles.administrator, OrganisationRoles.principal, OrganisationRoles.learnresourcemanager, GroupRoles.owner)
				  .append("       and v.status ");
				if(params.hasStatus()) {
					sb.in(params.getStatus());
				} else {
					sb.in(RepositoryEntryStatusEnum.preparationToClosed());
				} 
				
				sb.append(" )");
				  
				if(roles.isAuthor() && (!params.hasStatus() || (params.hasStatus() && hasOnly(params, RepositoryEntryStatusEnum.reviewToClosed())))
						&& (params.isCanCopy() || params.isCanDownload() || params.isCanReference())) {
					sb.append(" or ( membership.role ='").append(OrganisationRoles.author).append("'")
					  .append("   and v.status ");
					if(params.hasStatus()) {
						sb.in(params.getStatus());
					} else {
						sb.in(RepositoryEntryStatusEnum.reviewToClosed());
					}
					sb.append(" and (");
					if(params.isCanCopy()) {
						sb.append(" v.canCopy=true");	
					}
					if(params.isCanReference()) {
						if(params.isCanCopy()) {
							sb.append(" or");
						}
						sb.append(" v.canReference=true");	
					}
					if(params.isCanDownload()) {
						if(params.isCanCopy() || params.isCanReference()) {
							sb.append(" or");
						}
						sb.append(" v.canDownload=true");	
					}
					sb.append("))");
				}
				sb.append(" ))");
			}
		}
		return true;
	}
	
	private boolean hasOnly(SearchAuthorRepositoryEntryViewParams params, RepositoryEntryStatusEnum[] allowed) {
		if(!params.hasStatus()) return false;
		
		RepositoryEntryStatusEnum[] status = params.getStatus();
		for(int i=status.length; i-->0; ) {
			if(!RepositoryEntryStatusEnum.isInArray(status[i], allowed)) {
				return false;
			}
		}
		return true;
	}
	
	private void appendAuthorViewOrderBy(SearchAuthorRepositoryEntryViewParams params, QueryBuilder sb) {
		OrderBy orderBy = params.getOrderBy();
		boolean asc = params.isOrderByAsc();
		
		if(orderBy != null) {
			switch(orderBy) {
				case key:
					sb.append(" order by v.key");
					appendAsc(sb, asc);
					break;
				case favorit:
					if(asc) {
						sb.append(" order by marks asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by marks desc, lower(v.displayname) desc");
					}
					break;
				case type:
					sb.append(" order by res.resName");
					appendAsc(sb, asc);
					break;
				case technicalType:
					sb.append(" order by lower(v.technicalType)");
					appendAsc(sb, asc);	
					break;
				case displayname:
					sb.append(" order by lower(v.displayname)");
					appendAsc(sb, asc);	
					break;
				case authors:
					sb.append(" order by lower(v.authors)");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case author:
					sb.append(" order by lower(v.initialAuthor)");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");	
					break;
				case location:
					sb.append(" order by lower(v.location)");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");	
					break;
				case guests:
					sb.append(" order by v.guests");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");	
					break;
				case oer:
					sb.append(" order by v.canIndexMetadata");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case access:
					if(asc) {
						sb.append(" order by v.status asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by v.status desc, lower(v.displayname) desc");
					}
					break;
				case ac:
					if(asc) {
						sb.append(" order by offers asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by offers desc, lower(v.displayname) desc");
					}
					break;
				case references: {
					if(asc) {
						sb.append(" order by references asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by references desc, lower(v.displayname) desc");
					}
					break;
				}
				case creationDate:
					sb.append(" order by v.creationDate ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case lastUsage:
					sb.append(" order by v.statistics.lastUsage ");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case externalId:
					sb.append(" order by lower(v.externalId)");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case externalRef:
					sb.append(" order by lower(v.externalRef)");
					appendAsc(sb, asc).append(", lower(v.displayname) asc");
					break;
				case lifecycleLabel:
					sb.append(" order by lifecycle.label ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleSoftkey:
					sb.append(" order by lifecycle.softKey ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;	
				case lifecycleStart:
					sb.append(" order by lifecycle.validFrom ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleEnd:
					sb.append(" order by lifecycle.validTo ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case deletionDate:
					sb.append(" order by v.deletionDate ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case deletedBy:
					if(params.hasStatus(RepositoryEntryStatusEnum.trash)) {
						sb.append(" order by deletedByUser.lastName ");
						appendAsc(sb, asc).append(" nulls last, deletedByUser.firstName ");
						appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					}
					break;
				case lectureEnabled:
					sb.append(" order by");
					if(asc) {
						sb.append(" lectureConfig.lectureEnabled").appendAsc(asc).append(" nulls first");
					} else {
						sb.append(" lectureConfig.lectureEnabled").appendAsc(asc).append(" nulls last");
					}
					sb.append(", lower(v.displayname) asc");
					break;
				case license:
					sb.append(" order by v.key");
					appendAsc(sb, asc);
					break;
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
}
