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

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.model.RepositoryEntryAuthorImpl;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.user.UserImpl;
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
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryAuthorQueries.class);
	
	@Autowired
	private DB dbInstance;
	
	public int countViews(SearchAuthorRepositoryEntryViewParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}
		
		TypedQuery<Number> query = createViewQuery(params, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	public List<RepositoryEntryAuthorView> searchViews(SearchAuthorRepositoryEntryViewParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return Collections.emptyList();
		}

		TypedQuery<Object[]> query = createViewQuery(params, Object[].class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<Object[]> objects =  query.getResultList();
		List<RepositoryEntryAuthorView> views = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			RepositoryEntry re = (RepositoryEntry)object[0];
			Number numOfMarks = (Number)object[1];
			boolean hasMarks = numOfMarks == null ? false : numOfMarks.longValue() > 0;
			Number numOffers = (Number)object[2];
			long offers = numOffers == null ? 0l : numOffers.longValue();
			views.add(new RepositoryEntryAuthorImpl(re, hasMarks, offers));
		}
		return views;
	}

	protected <T> TypedQuery<T> createViewQuery(SearchAuthorRepositoryEntryViewParams params,
			Class<T> type) {

		IdentityRef identity = params.getIdentity();
		Roles roles = params.getRoles();
		List<String> resourceTypes = params.getResourceTypes();
		boolean oracle = "oracle".equals(dbInstance.getDbVendor());
		boolean admin = (roles != null && (roles.isInstitutionalResourceManager() || roles.isOLATAdmin()));

		boolean count = Number.class.equals(type);
		boolean needIdentity = false;
		StringBuilder sb = new StringBuilder();
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
			sb.append(" (select count(offer.key) from ").append(OfferImpl.class.getName()).append(" as offer ")
			  .append("   where offer.resource=res and offer.valid=true")
			  .append(" ) as offers")
			  .append(" from repositoryentry as v")
			  .append(" inner join ").append(oracle ? "" : "fetch").append(" v.olatResource as res")
			  .append(" inner join fetch v.statistics as stats")
			  .append(" left join fetch v.lifecycle as lifecycle ");
		}

		sb.append(" where");
		if(params.isOwnedResourcesOnly()) {
			needIdentity = true;
			sb.append(" v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("    where rel.group.key=membership.group.key and membership.identity.key=:identityKey")
			  .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
			  .append(" )");
		} else if(admin) {
			sb.append(" v.access>=").append(RepositoryEntry.ACC_OWNERS);
		} else {
			needIdentity = true;
			sb.append(" (v.access>=").append(RepositoryEntry.ACC_OWNERS_AUTHORS)
			  .append(" or (v.access=").append(RepositoryEntry.ACC_OWNERS)
			  .append("   and v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("     where rel.group.key=membership.group.key and membership.identity.key=:identityKey")
			  .append("       and membership.role='").append(GroupRoles.owner.name()).append("'")
			  .append("   )")
			  .append(" ))");
		}
		
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			sb.append(" and v.key in (:repoEntryKeys)");
		}

		if (params.isResourceTypesDefined()) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		if(params.getMarked() != null && params.getMarked().booleanValue()) {
			needIdentity = true;
			sb.append(" and exists (select mark2.key from ").append(MarkImpl.class.getName()).append(" as mark2 ")
			  .append("   where mark2.creator.key=:identityKey and mark2.resId=v.key and mark2.resName='RepositoryEntry'")
			  .append(" )");
		}
		
		String author = params.getAuthor();
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			author = PersistenceHelper.makeFuzzyQueryString(author);

			sb.append(" and v.key in (select rel.entry.key from repoentrytogroup as rel, bgroupmember as membership, ")
			     .append(IdentityImpl.class.getName()).append(" as identity, ").append(UserImpl.class.getName()).append(" as user")
		         .append("    where rel.group.key=membership.group.key and membership.identity.key=identity.key and identity.user.key=user.key")
		         .append("      and membership.role='").append(GroupRoles.owner.name()).append("'")
		         .append("      and (");
			PersistenceHelper.appendFuzzyLike(sb, "user.userProperties['firstName']", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.userProperties['lastName']", "author", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identity.name", "author", dbInstance.getDbVendor());
			sb.append(" ))");
		}

		String displayname = params.getDisplayname();
		if (StringHelper.containsNonWhitespace(displayname)) {
			//displayName = '%' + displayName.replace('*', '%') + '%';
			//query.append(" and v.displayname like :displayname");
			displayname = PersistenceHelper.makeFuzzyQueryString(displayname);
			sb.append(" and ");
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "displayname", dbInstance.getDbVendor());
		}
		
		String desc = params.getDescription();
		if (StringHelper.containsNonWhitespace(desc)) {
			//desc = '%' + desc.replace('*', '%') + '%';
			//query.append(" and v.description like :desc");
			desc = PersistenceHelper.makeFuzzyQueryString(desc);
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
					sb.append(" or v.key=:quickVKey or res.resId=:quickVKey)");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}

		if(!count) {
			appendAuthorViewOrderBy(params.getOrderBy(), params.isOrderByAsc(), sb);
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			dbQuery.setParameter("repoEntryKeys", params.getRepoEntryKeys());
		}
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
		if (StringHelper.containsNonWhitespace(author)) { // fuzzy author search
			dbQuery.setParameter("author", author);
		}
		if (StringHelper.containsNonWhitespace(displayname)) {
			dbQuery.setParameter("displayname", displayname);
		}
		if (StringHelper.containsNonWhitespace(desc)) {
			dbQuery.setParameter("desc", desc);
		}

		if(needIdentity) {
			dbQuery.setParameter("identityKey", identity.getKey());
		}
		return dbQuery;
	}
	
	private void appendAuthorViewOrderBy(OrderBy orderBy, boolean asc, StringBuilder sb) {
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
				case access:
					if(asc) {
						sb.append(" order by v.access asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by v.access desc, lower(v.displayname) desc");
					}
					break;
				case ac:
					if(asc) {
						sb.append(" order by offers asc, lower(v.displayname) asc");
					} else {
						sb.append(" order by offers desc, lower(v.displayname) desc");
					}
					break;
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
					if(asc) {
						sb.append(" order by lifecycle.label nulls last, lower(v.displayname) asc");
					} else {
						sb.append(" order by lifecycle.label nulls last, lower(v.displayname) desc");
					}
					break;
				case lifecycleSoftkey:
					if(asc) {
						sb.append(" order by lifecycle.softKey nulls last, lower(v.displayname) asc");
					} else {
						sb.append(" order by lifecycle.softKey nulls last, lower(v.displayname) desc");
					}
					break;	
				case lifecycleStart:
					sb.append(" order by lifecycle.validFrom ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
				case lifecycleEnd:
					sb.append(" order by lifecycle.validTo ");
					appendAsc(sb, asc).append(" nulls last, lower(v.displayname) asc");
					break;
			}
		}
	}
	
	private final StringBuilder appendAsc(StringBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
}
