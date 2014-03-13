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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.catalog.CatalogEntryImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.SearchMyRepositoryEntryViewParams;
import org.olat.repository.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.SearchMyRepositoryEntryViewParams.OrderBy;
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
public class RepositoryEntryMyCourseViewQueries {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryMyCourseViewQueries.class);
	
	@Autowired
	private DB dbInstance;
	
	public int countMyView(SearchMyRepositoryEntryViewParams params) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return 0;
		}
		
		TypedQuery<Number> query = creatMyViewQuery(params, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	public List<RepositoryEntryMyView> searchMyView(SearchMyRepositoryEntryViewParams params, int firstResult, int maxResults) {
		if(params.getIdentity() == null) {
			log.error("No identity defined for query");
			return Collections.emptyList();
		}

		TypedQuery<RepositoryEntryMyView> query = creatMyViewQuery(params, RepositoryEntryMyView.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<RepositoryEntryMyView> views = query.getResultList();
		return views;
	}

	protected <T> TypedQuery<T> creatMyViewQuery(SearchMyRepositoryEntryViewParams params,
			Class<T> type) {

		Roles roles = params.getRoles();
		Identity identity = params.getIdentity();
		List<String> resourceTypes = params.getResourceTypes();

		boolean count = Number.class.equals(type);
		StringBuilder sb = new StringBuilder();
		if(count) {
			sb.append("select count(v.key) from repositoryentrymy as v ")
			  .append(" inner join v.olatResource as res")
			  .append(" left join v.lifecycle as lifecycle");
		} else {
			sb.append("select v from repositoryentrymy as v ")
			  .append(" inner join fetch v.olatResource as res")
			  .append(" left join fetch v.lifecycle as lifecycle");
		}

		sb.append(" where v.identityKey=:identityKey and ");
		appendMyViewAccessSubSelect(sb, roles, params.getFilters(), params.isMembershipMandatory());
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			sb.append(" and v.key=:repoEntryKeys ");
		}
		
		if(params.getFilters() != null) {
			for(Filter filter:params.getFilters()) {
				appendMyViewFilters(filter, sb);
			}
		}
		
		if(params.getParentEntry() != null) {
			sb.append(" and exists (select cei.parent.key from ").append(CatalogEntryImpl.class.getName()).append(" as cei ")
			  .append("   where cei.parent.key=:parentCeiKey and cei.repositoryEntry.key=v.key")
			  .append(" )");
		}
		if (params.isResourceTypesDefined()) {
			sb.append(" and res.resName in (:resourcetypes)");
		}
		if(params.getMarked() != null) {
			sb.append(" and v.markKey ").append(params.getMarked().booleanValue() ? " is not null " : " is null ");
		}
		
		if(!count) {
			appendMyViewOrderBy(params.getOrderBy(), sb);
		}

		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		if(params.getParentEntry() != null) {
			dbQuery.setParameter("parentCeiKey", params.getParentEntry().getKey());
		}
		if(params.getRepoEntryKeys() != null && params.getRepoEntryKeys().size() > 0) {
			dbQuery.setParameter("repoEntryKeys", params.getRepoEntryKeys());
		}
		if (params.isResourceTypesDefined()) {
			dbQuery.setParameter("resourcetypes", resourceTypes);
		}
		if(params.isLifecycleFilterDefined()) {
			dbQuery.setParameter("now", new Date());
		}
		dbQuery.setParameter("identityKey", identity.getKey());
		return dbQuery;
	}
	
	private void appendMyViewAccessSubSelect(StringBuilder sb, Roles roles, List<Filter> filters, boolean membershipMandatory) {
		sb.append("(v.access >= ");
		if (roles.isAuthor()) {
			sb.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
		} else if (roles.isGuestOnly()) {
			sb.append(RepositoryEntry.ACC_USERS_GUESTS);
		} else {
			sb.append(RepositoryEntry.ACC_USERS);
		}

		StringBuilder inRoles = new StringBuilder();
		if(filters != null && filters.size() > 0) {
			for(Filter filter: filters) {
				if(Filter.asAuthor.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.owner.name()).append("'");
				} else if(Filter.asCoach.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.coach.name()).append("'");
				} else if (Filter.asParticipant.equals(filter)) {
					if(inRoles.length() > 0) inRoles.append(",");
					inRoles.append("'").append(GroupRoles.participant.name()).append("'");
				}
			}
		}

		//+ membership
		if(!roles.isGuestOnly()) {
			if(inRoles.length() == 0 && !membershipMandatory) {
				//sub select are very quick
				sb.append(" or (")
				  .append("  v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true")
				  .append("  and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
				  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=v.identityKey")
				  .append("      and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
				  .append("  )")
				  .append(" )")
				  .append(")");
			} else {
				if(inRoles.length() == 0) {
					inRoles.append("'").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("'");
				}
				//make sure that in all case the role is mandatory
				sb.append(" or (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true))")
				  .append(" and exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
				  .append("    where rel.entry=v and rel.group=baseGroup and membership.group=baseGroup and membership.identity.key=v.identityKey")
				  .append("      and membership.role in (").append(inRoles).append(")")
				  .append(" )");
			}
		}
	}
	
	private void appendMyViewFilters(Filter filter, StringBuilder sb) {
		switch(filter) {
			case currentCourses:
				sb.append(" and ").append(" lifecycle.validFrom<=:now and lifecycle.validTo>=:now");
				break;
			case upcomingCourses:
				sb.append(" and ").append(" lifecycle.validFrom>=:now");
				break;
			case oldCourses:
				sb.append(" and ").append(" lifecycle.validTo<=:now");
				break;
			case passed:
				sb.append(" and ").append(" v.passed=true");
				break;
			case notPassed:
				sb.append(" and ").append(" v.passed=false");
				break;
			case withoutPassedInfos:
				sb.append(" and ").append(" v.passed is null");
				break;
			default: //do nothing
		}
	}
	
	private void appendMyViewOrderBy(OrderBy orderBy, StringBuilder sb) {
		if(orderBy != null) {
			switch(orderBy) {
				case automatic:
					sb.append(" order by lower(v.displayname) asc");
					break;
				case favorit:
					sb.append(" order by v.markKey nulls last, lower(v.displayname) asc");
					break;
				case lastVisited:
					sb.append(" order by v.recentLaunch nulls last, v.recentLaunch desc, lower(v.displayname) asc");
					break;
				case passed:
					sb.append(" order by v.passed nulls last, v.score desc, lower(v.displayname) asc");
					break;
				case score:
					sb.append(" order by v.score nulls last, v.score desc, lower(v.displayname) asc");
					break;
				case title:
					sb.append(" order by lower(v.displayname) asc");
					break;
				case lifecycle:
					sb.append(" order by v.lifecycle.key nulls last, lifecycle.validFrom desc, lower(v.displayname) asc");
					break;
				case author:
					sb.append(" order by v.authors nulls last, lower(v.authors) asc");
					break;
				case creationDate:
					sb.append(" order by v.creationDate desc, lower(v.displayname) asc");
					break;
				case lastModified:
					sb.append(" order by v.lastModified desc, lower(v.displayname) asc");
					break;
				case rating:
					sb.append(" order by v.averageRating nulls last, v.averageRating desc, lower(v.displayname) asc");
					break;	
				default:
					sb.append(" order by lower(v.displayname) asc");
					break;
			}
		}
	}

}
