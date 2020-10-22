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
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to query the members of curriculums and curriculum elements.
 * 
 * Initial date: 17 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumMemberQueries {
	
	@Autowired
	private DB dbInstance;
	
	public List<CurriculumMember> getMembers(CurriculumRef curriculum, SearchMemberParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident, membership.role, membership.inheritanceModeString, membership.creationDate from curriculum cur")
		  .append(" inner join cur.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where cur.key=:curriculumKey");
		createQueryPart(sb, params);
		createUserPropertiesQueryPart(sb, params.getLogin(), params.getSearchString(),
				params.getUserProperties(), params.getUserPropertiesSearch());
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("curriculumKey", curriculum.getKey());
		createQueryParameters(query, params);
		createUserPropertiesQueryParameters(query, params.getLogin(), params.getSearchString(), params.getUserPropertiesSearch());

		List<Object[]> rawObjects = query.getResultList();
		List<CurriculumMember> members = new ArrayList<>(rawObjects.size());
		for(Object[] object:rawObjects) {
			Identity identity = (Identity)object[0];
			String role = (String)object[1];
			String inheritanceModeString = (String)object[2];
			GroupMembershipInheritance inheritanceMode = GroupMembershipInheritance.none;
			if(StringHelper.containsNonWhitespace(inheritanceModeString)) {
				inheritanceMode = GroupMembershipInheritance.valueOf(inheritanceModeString);
			}
			Date creationDate = (Date)object[3];
			members.add(new CurriculumMember(identity, role, inheritanceMode, creationDate));
		}
		return members;
	}
	
	public List<CurriculumMember> getMembers(CurriculumElementRef element, SearchMemberParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident, membership.role, membership.inheritanceModeString, membership.creationDate from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where el.key=:elementKey");
		createQueryPart(sb, params);
		createUserPropertiesQueryPart(sb, params.getLogin(), params.getSearchString(),
				params.getUserProperties(), params.getUserPropertiesSearch());
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("elementKey", element.getKey());
		createQueryParameters(query, params);
		createUserPropertiesQueryParameters(query, params.getLogin(), params.getSearchString(), params.getUserPropertiesSearch());
					
		List<Object[]> objects = query.getResultList();
		List<CurriculumMember> members = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Identity identity = (Identity)object[0];
			String role = (String)object[1];
			String inheritanceModeString = (String)object[2];
			GroupMembershipInheritance inheritanceMode = GroupMembershipInheritance.none;
			if(StringHelper.containsNonWhitespace(inheritanceModeString)) {
				inheritanceMode = GroupMembershipInheritance.valueOf(inheritanceModeString);
			}
			Date creationDate = (Date)object[3];
			members.add(new CurriculumMember(identity, role, inheritanceMode, creationDate));
		}
		return members;
	}

	private void createQueryPart(QueryBuilder sb, SearchMemberParameters params) {
		if(params.getRoles() != null && !params.getRoles().isEmpty()) {
			sb.append(" and membership.role in (:roles)");
		}
	}
	
	private void createQueryParameters(TypedQuery<?> query, SearchMemberParameters params) {
		if(params.getRoles() != null && !params.getRoles().isEmpty()) {
			List<String> roles = params.getRoles().stream()
					.map(CurriculumRoles::toString)
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
}
