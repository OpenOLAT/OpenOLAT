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
package org.olat.modules.project.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
@Service
public class ProjMemberQueries {
	
	@Autowired
	private DB dbInstance;
	
	public boolean isProjectMember(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership.key");
		sb.append("  from projproject project");
		sb.append(" inner join project.baseGroup baseGroup");
		sb.append(" inner join baseGroup.members membership");
		sb.append(" where membership.identity.key = :identityKey");
		
		return !dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(1)
				.getResultList()
				.isEmpty();
	}
	
	public List<GroupMembership> getProjMemberships(ProjMemberInfoSearchParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership");
		sb.append("  from bgroupmember membership");
		sb.append(" inner join fetch membership.identity ident");
		sb.append(" inner join fetch ident.user user");
		sb.and().append(" membership.group.key=:groupKey");
		createQueryPart(sb, params);
		createUserPropertiesQueryPart(sb, params.getLogin(), params.getSearchString(),
				params.getUserProperties(), params.getUserPropertiesSearch());
		
		TypedQuery<GroupMembership> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupMembership.class)
				.setParameter("groupKey", params.getProject().getBaseGroup().getKey());
		createQueryParameters(query, params);
		createUserPropertiesQueryParameters(query, params.getLogin(), params.getSearchString(), params.getUserPropertiesSearch());
		
		return query.getResultList();
	}

	private void createQueryPart(QueryBuilder sb, ProjMemberInfoSearchParameters searchParams) {
		if(searchParams.getRoles() != null && !searchParams.getRoles().isEmpty()) {
			sb.and().append("membership.role in :roles");
		}
	}
	
	private void createQueryParameters(TypedQuery<?> query, ProjMemberInfoSearchParameters params) {
		if(params.getRoles() != null && !params.getRoles().isEmpty()) {
			List<String> roles = params.getRoles().stream()
					.map(ProjectRole::name)
					.collect(Collectors.toList());
			query.setParameter("roles", roles);
		}
	}
	
	private void createUserPropertiesQueryPart(QueryBuilder sb, String login, String searchString, List<UserPropertyHandler> handlers, Map<String,String> userProperties) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			// treat login and userProperties as one element in this query
			sb.append(" and ( ");
			PersistenceHelper.appendFuzzyLike(sb, "ident.name", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "user.nickName", "searchString", dbInstance.getDbVendor());
			
			for(UserPropertyHandler handler:handlers) {
				if(handler.getDatabaseColumnName() != null) {
					sb.append(" or ");
					PersistenceHelper.appendFuzzyLike(sb, "user.".concat(handler.getName()), "searchString", dbInstance.getDbVendor());
				}
			}
			sb.append(")");
		}
		
		if(StringHelper.containsNonWhitespace(login) || (userProperties != null && !userProperties.isEmpty())) {
			sb.append(" and (");	
			
			// append query for login
			boolean appendOr = false;
			if (StringHelper.containsNonWhitespace(login)) {// backwards compatibility
				appendOr = true;
				PersistenceHelper.appendFuzzyLike(sb, "ident.name", "login", dbInstance.getDbVendor());
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "user.nickName", "login", dbInstance.getDbVendor());
			}
			
			// append queries for user fields
			if (userProperties != null && !userProperties.isEmpty()) {
				// add other fields
				for (String key : userProperties.keySet()) {
					if(appendOr) {
						sb.append(" or ");
					} else {
						appendOr = true;
					}
					PersistenceHelper.appendFuzzyLike(sb, "user.".concat(key), key.concat("_value"), dbInstance.getDbVendor());
				}
			}

			sb.append(" )");
		}
	}
	
	private void createUserPropertiesQueryParameters(TypedQuery<?> query, String login, String searchString, Map<String,String> userProperties) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(searchString);
			query.setParameter("searchString", fuzzySearch);
		}
		
		if (StringHelper.containsNonWhitespace(login)) {
			login = PersistenceHelper.makeFuzzyQueryString(login);
			query.setParameter("login", login);
		}

		//	 add user properties attributes
		if (userProperties != null && !userProperties.isEmpty()) {
			for (Map.Entry<String, String> entry : userProperties.entrySet()) {
				String value = PersistenceHelper.makeFuzzyQueryString(entry.getValue());
				query.setParameter(entry.getKey() + "_value", value);
			}
		}
	}

	public List<GroupMembership> getProjMemberships(Collection<ProjProject> projects, Collection<ProjectRole> roles) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership");
		sb.append("  from bgroupmember membership");
		sb.append(" inner join fetch membership.identity ident");
		sb.append(" inner join fetch ident.user user");
		sb.and().append(" membership.group.key in :groupKeys");
		sb.and().append(" membership.role in :roles");
		
		Collection<Long> groupKeys = projects.stream().map(ProjProject::getBaseGroup).map(Group::getKey).collect(Collectors.toSet());
		Collection<String> roleNames = roles.stream().map(ProjectRole::name).collect(Collectors.toSet());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupMembership.class)
				.setParameter("groupKeys", groupKeys)
				.setParameter("roles", roleNames)
				.getResultList();
	}

	public Map<Long, Set<Identity>> getGroupKeyToIdentities(Collection<Long> groupKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership");
		sb.append("  from bgroupmember membership");
		sb.append(" inner join fetch membership.identity ident");
		sb.append(" inner join fetch ident.user user");
		sb.and().append(" membership.group.key in :groupKeys");
		
		List<GroupMembership> memberships = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupMembership.class)
				.setParameter("groupKeys", groupKeys)
				.getResultList();
		
		return memberships.stream()
				.collect(Collectors.groupingBy(
						ms -> ms.getGroup().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								ms -> ms.stream()
										.map(GroupMembership::getIdentity)
										.collect(Collectors.toSet()))));
	}
	
	public Map<Long, Set<Long>> getGroupKeyToIdentityKeys(Collection<Long> groupKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership");
		sb.append("  from bgroupmember membership");
		sb.and().append(" membership.group.key in :groupKeys");
		
		List<GroupMembership> memberships = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GroupMembership.class)
				.setParameter("groupKeys", groupKeys)
				.getResultList();
		
		return memberships.stream()
				.collect(Collectors.groupingBy(
						ms -> ms.getGroup().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								ms -> ms.stream()
										.map(m -> m.getIdentity().getKey())
										.collect(Collectors.toSet()))));
	}
	
}
