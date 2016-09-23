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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedBinderSection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SharedWithMeQueries {
	
	@Autowired
	private DB dbInstance;
	

	public List<AssessedBinder> searchSharedBinders(Identity member, String searchString) {
		List<AssessedBinder> binders = searchSharedBindersOne(member, searchString);
		searchSharedBindersTwo(member, binders);
		return binders;
	}
	
	private List<AssessedBinder> searchSharedBindersOne(Identity member, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, binder.title, entry.displayname, aEntry.score, aEntry.passed, owner")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as ownership on (ownership.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append(" inner join ownership.identity as owner")
		  .append(" inner join fetch owner.user as owneruser")
		  .append(" left join binder.entry as entry")//entry -> assessment entry -> owner
		  .append(" left join assessmententry as aEntry on (aEntry.identity.key=owner.key and aEntry.repositoryEntry.key=entry.key and ((binder.subIdent is null and aEntry.subIdent is null) or binder.subIdent=aEntry.subIdent))")
		  .append(" where")
		  .append(" exists (select membership.key from bgroupmember as membership")
		  .append("   where membership.group.key=baseGroup.key and membership.identity.key=:identityKey and membership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" )")
		  .append(" or exists (select section.key from pfsection as section")
		  .append("   inner join section.baseGroup as sectionGroup")
		  .append("   inner join sectionGroup.members as sectionMembership on (sectionMembership.identity.key=:identityKey and sectionMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("'))")
		  .append("   where section.binder.key=binder.key")
		  .append(" )")
		  .append(" or exists (select page.key from pfpage as page")
		  .append("   inner join page.baseGroup as pageGroup")
		  .append("   inner join page.section as pageSection on (pageSection.binder.key=binder.key)")
		  .append("   inner join pageGroup.members as pageMembership on (pageMembership.identity.key=:identityKey and pageMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("'))")
		  .append(" ))");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "binder.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "binder.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "owner.name", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "owneruser.lastName", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "owneruser.firstName", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "entry.displayname", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", member.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		
		List<Object[]> objects = query.getResultList();
		List<AssessedBinder> assessedBinders = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			int pos = 0;
			Long binderKey = (Long)object[pos++];
			String binderTitle = (String)object[pos++];
			String entryDisplayname = (String)object[pos++];
			BigDecimal score = (BigDecimal)object[pos++];
			Boolean passed = (Boolean)object[pos++];
			Identity owner = (Identity)object[pos++];
			assessedBinders.add(new AssessedBinder(binderKey, binderTitle, entryDisplayname, passed, score, owner));
		}
		return assessedBinders;
	}
	
	private void searchSharedBindersTwo(Identity member, List<AssessedBinder> binders) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, section.key, section.status, section.title, section.pos, page.key, page.lastModified, max(parts.lastModified)")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.sections as section")
		  .append(" inner join section.pages as page")
		  .append(" inner join page.body as body")
		  .append(" left join body.parts as parts")
		  .append(" where exists (select membership.key from bgroupmember as membership")
		  .append("   where membership.group.key=binder.baseGroup.key and membership.identity.key=:identityKey and membership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select sectionMembership.key from bgroupmember as sectionMembership")
		  .append("   where sectionMembership.group.key=section.baseGroup.key and sectionMembership.identity.key=:identityKey and sectionMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select page.key from pfpage as coachedPage")
		  .append("   inner join coachedPage.baseGroup as pageGroup")
		  .append("   inner join pageGroup.members as pageMembership on (pageMembership.identity.key=:identityKey and pageMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("'))")
		  .append("   where coachedPage.key=page.key")
		  .append(" ) group by binder.key, section.key, section.status, page.key, page.lastModified");

		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", member.getKey())
				.getResultList();
		if(objects.size() > 0) {
			Set<Long> sectionKeys = new HashSet<>();
			Map<Long,AssessedBinder> keyToBinder = binders.stream().collect(Collectors.toMap (b -> b.getBinderKey(), b -> b));

			for(Object[] object:objects) {
				int pos = 0;
				Long binderKey = (Long)object[pos++];
				Long sectionKey = (Long)object[pos++];
				String sectionStatus = (String)object[pos++];
				String sectionTitle = (String)object[pos++];
				Number sectionPosNumber = (Number)object[pos++];
				pos++;//Object pageKey = object[pos++];
				Date pageLastModified = (Date)object[pos++];
				Date partLastModified = (Date)object[pos++];
				if(partLastModified == null) {
					partLastModified = pageLastModified;
				}

				AssessedBinder binder = keyToBinder.get(binderKey);
				if(partLastModified != null) {
					if(binder.getLastModified() == null || binder.getLastModified().after(partLastModified)) {
						binder.setLastModified(partLastModified);
					} 
				}
				
				if(!sectionKeys.contains(sectionKey)) {
					SectionStatus status = SectionStatus.notStarted;
					if(StringHelper.containsNonWhitespace(sectionStatus)) {
						status = SectionStatus.valueOf(sectionStatus);
					}
					if(status == SectionStatus.notStarted || status == SectionStatus.inProgress) {
						binder.setNumOfOpenSections(binder.getNumOfOpenSections() + 1);
					}

					if(status == SectionStatus.inProgress || status == SectionStatus.submitted || status == SectionStatus.closed) {
						if(binder.getSections() == null) {
							binder.setSections(new ArrayList<>());
						}
						int sectionPos = sectionPosNumber == null ? -1 : sectionPosNumber.intValue();
						binder.getSections().add(new AssessedBinderSection(sectionKey, sectionTitle, sectionPos));
						sectionKeys.add(sectionKey);
					}
				}
			}
		}
	}
}
