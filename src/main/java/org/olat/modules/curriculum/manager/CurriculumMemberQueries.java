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
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
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
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident, membership.role, membership.inheritanceModeString from curriculum cur")
		  .append(" inner join cur.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where cur.key=:curriculumKey");
		createUserPropertiesQueryPart(sb, params.getSearchString(), params.getUserProperties());
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("curriculumKey", curriculum.getKey());
		createUserPropertiesQueryParameters(query, params.getSearchString());

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
			members.add(new CurriculumMember(identity, role, inheritanceMode));
		}
		return members;
	}
	
	public List<CurriculumMember> getMembers(CurriculumElementRef element, SearchMemberParameters params) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident, membership.role, membership.inheritanceModeString from curriculumelement el")
		  .append(" inner join el.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where el.key=:elementKey");
		createUserPropertiesQueryPart(sb, params.getSearchString(), params.getUserProperties());
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("elementKey", element.getKey());
		createUserPropertiesQueryParameters(query, params.getSearchString());
					
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
			members.add(new CurriculumMember(identity, role, inheritanceMode));
		}
		return members;
	}
	
	private void createUserPropertiesQueryPart(StringBuilder sb, String searchString, List<UserPropertyHandler> handlers) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;
		
		// treat login and userProperties as one element in this query
		sb.append(" and ( ");			
		PersistenceHelper.appendFuzzyLike(sb, "ident.name", "searchString", dbInstance.getDbVendor());
		
		for(UserPropertyHandler handler:handlers) {
			if(handler.getDatabaseColumnName() != null) {
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "user.".concat(handler.getName()), "searchString", dbInstance.getDbVendor());
			}
		}
		sb.append(")");
	}
	
	private void createUserPropertiesQueryParameters(TypedQuery<?> query, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;
		
		String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(searchString);
		query.setParameter("searchString", fuzzySearch);
	}

}
