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
package org.olat.modules.portfolio.manager;

import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;
import static org.olat.core.commons.persistence.PersistenceHelper.makeFuzzyQueryString;

import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SharedByMeQueries {
	
	@Autowired
	private DB dbInstance;
	
	public List<Binder> searchSharedBinders(Identity member, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" left join fetch binder.entry as entry")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(PortfolioRoles.owner.name()).append("'")
		  .append(" and (exists (select binderMembership.key from bgroupmember as binderMembership")
		  .append("   where binderMembership.group.key=baseGroup.key and binderMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" )")
		  .append(" or exists (select section.key from pfsection as section")
		  .append("   inner join section.baseGroup as sectionGroup")
		  .append("   inner join sectionGroup.members as sectionMembership")
		  .append("   where section.binder.key=binder.key and sectionMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" )")
		  .append(" or exists (select page.key from pfpage as page")
		  .append("   inner join page.baseGroup as pageGroup")
		  .append("   inner join page.section as pageSection")
		  .append("   inner join pageGroup.members as pageMembership")
		  .append("   where pageSection.binder.key=binder.key and pageMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ))")
		  .append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "binder.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "binder.summary", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		TypedQuery<Binder> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", member.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		return query.getResultList();
	}

}
