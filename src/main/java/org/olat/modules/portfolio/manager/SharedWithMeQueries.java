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

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedBinderSection;
import org.olat.modules.portfolio.model.AssessedPage;
import org.olat.modules.portfolio.model.SearchSharePagesParameters;
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
	
	public List<AssessedPage> searchSharedPagesEntries(Identity member, SearchSharePagesParameters params) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select binder.key,")
		  .append("  section.key, section.endDate,")
		  .append("  page.key, page.title, page.status, page.lastModified,")
		  .append("  body.lastModified,")
		  .append("  (select max(part.lastModified) from pfpagepart as part")
		  .append("   where part.body.key=body.key")
		  .append("  ) as partLastModified,")
		  .append("  uinfos.userStatus, uinfos.mark,")
		  .append("  owner")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as ownership on (ownership.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append(" inner join ownership.identity as owner")
		  .append(" inner join fetch owner.user as owneruser")
		  .append(" inner join binder.sections as section")
		  .append(" inner join section.pages as page")
		  .append(" inner join page.body as body")
		  .append(params.isBookmarkOnly() ? " inner" : " left").append(" join pfpageuserinfos as uinfos on (uinfos.page.key=page.key and uinfos.identity.key=:identityKey)")
		  .append(" where ");
		if(params.isBookmarkOnly()) {
			sb.append(" uinfos.mark=true and");
		}
		String searchString = params.getSearchString();
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" (");
			appendFuzzyLike(sb, "page.title", "searchString", dbInstance.getDbVendor());
			sb.append(") and");
		}
		if(params.getExcludedPageStatus() != null && !params.getExcludedPageStatus().isEmpty()) {
			sb.append(" (page.status is null or page.status not in (:excludedPageStatus)) and");
		}
		if(params.getExcludedPageUserStatus() != null && !params.getExcludedPageUserStatus().isEmpty()) {
			sb.append(" (uinfos.userStatus is null or uinfos.userStatus not in (:excludedPageUserStatus)) and");
		}
		
		sb.append(" (exists (select membership.key from bgroupmember as membership")
		  .append("   where membership.group.key=binder.baseGroup.key and membership.identity.key=:identityKey and membership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select sectionMembership.key from bgroupmember as sectionMembership")
		  .append("   where sectionMembership.group.key=section.baseGroup.key and sectionMembership.identity.key=:identityKey and sectionMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select page.key from pfpage as coachedPage")
		  .append("   inner join coachedPage.baseGroup as pageGroup")
		  .append("   inner join pageGroup.members as pageMembership on (pageMembership.identity.key=:identityKey and pageMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("'))")
		  .append("   where coachedPage.key=page.key")
		  .append(" ))");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", member.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		if(params.getExcludedPageStatus() != null && !params.getExcludedPageStatus().isEmpty()) {
			List<String> excludedPageStatus = params.getExcludedPageStatus()
					.stream().map(s -> s.name()).collect(Collectors.toList());
			query.setParameter("excludedPageStatus", excludedPageStatus);
		}
		if(params.getExcludedPageUserStatus() != null && !params.getExcludedPageUserStatus().isEmpty()) {
			List<String> excludedPageUserStatus = params.getExcludedPageUserStatus()
					.stream().map(s -> s.name()).collect(Collectors.toList());
			query.setParameter("excludedPageUserStatus", excludedPageUserStatus);
		}
		
		List<Object[]> objects = query.getResultList();
		List<AssessedPage> items = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			int pos = 0;
			Long binderKey = (Long)object[pos++];
			pos++; // Section key
			Date sectionDate = (Date)object[pos++];
			Long pageKey = (Long)object[pos++];
			String pageTitle = (String)object[pos++];
			PageStatus pageStatus = PageStatus.valueOfOrNull((String)object[pos++]);
			Date pageLastModified = (Date)object[pos++];
			Date bodyLastModified = (Date)object[pos++];
			Date partLastModified = (Date)object[pos++];
			PageUserStatus userStatus = PageUserStatus.valueOfWithDefault((String)object[pos++]);
			Boolean mark =  (Boolean)object[pos++];
			Identity owner = (Identity)object[pos++];
			
			Date lastModified = pageLastModified;
			if(lastModified == null ||
					(lastModified != null && partLastModified != null && partLastModified.after(lastModified))) {
				lastModified = partLastModified;
			}
			if(lastModified == null ||
					(lastModified != null && bodyLastModified != null && bodyLastModified.after(lastModified))) {
				lastModified = bodyLastModified;
			}

			items.add(new AssessedPage(binderKey, sectionDate,
					pageKey, pageTitle, pageStatus, lastModified,
					mark, userStatus, owner));
		}
		return items;
	}
	

	public List<AssessedBinder> searchSharedBinders(Identity member, String searchString) {
		List<AssessedBinder> binders = searchSharedBindersOne(member, searchString);
		searchSharedBindersTwo(member, binders);
		return binders;
	}
	
	private List<AssessedBinder> searchSharedBindersOne(Identity member, String searchString) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select binder.key, binder.title, entry.key, entry.displayname, aEntry.score, aEntry.passed, owner, baseGroup")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as ownership on (ownership.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append(" inner join ownership.identity as owner")
		  .append(" inner join fetch owner.user as owneruser")
		  .append(" left join binder.entry as entry")//entry -> assessment entry -> owner
		  .append(" left join assessmententry as aEntry on (aEntry.identity.key=owner.key and aEntry.repositoryEntry.key=entry.key and ((binder.subIdent is null and aEntry.subIdent is null) or binder.subIdent=aEntry.subIdent))")
		  .append(" where (")
		  .append(" exists (select membership.key from bgroupmember as membership")
		  .append("   where membership.group.key=baseGroup.key and membership.identity.key=:identityKey and membership.role ").in(PortfolioRoles.coach, PortfolioRoles.reviewer, PortfolioRoles.invitee)
		  .append(" )")
		  .append(" or exists (select section.key from pfsection as section")
		  .append("   inner join section.baseGroup as sectionGroup")
		  .append("   inner join sectionGroup.members as sectionMembership on (sectionMembership.identity.key=:identityKey and sectionMembership.role ").in(PortfolioRoles.coach, PortfolioRoles.reviewer, PortfolioRoles.invitee).append(")")
		  .append("   where section.binder.key=binder.key")
		  .append(" )")
		  .append(" or exists (select page.key from pfpage as page")
		  .append("   inner join page.baseGroup as pageGroup")
		  .append("   inner join pageGroup.members as pageMembership on (pageMembership.identity.key=:identityKey and pageMembership.role ").in(PortfolioRoles.coach, PortfolioRoles.reviewer, PortfolioRoles.invitee).append(")")
		  .append("   where page.section.binder.key=binder.key")
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
		sb.append(" and (binder.status is null or binder.status='").append(BinderStatus.open.name()).append("')");
		
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
			Long entryKey = (Long)object[pos++];
			String entryDisplayname = (String)object[pos++];
			BigDecimal score = (BigDecimal)object[pos++];
			Boolean passed = (Boolean)object[pos++];
			Identity owner = (Identity)object[pos++];
			Group baseGroup = (Group)object[pos];
			assessedBinders.add(new AssessedBinder(binderKey, binderTitle, entryKey, entryDisplayname, passed, score, owner, baseGroup));
		}
		return assessedBinders;
	}
	
	private void searchSharedBindersTwo(Identity member, List<AssessedBinder> binders) {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("select binder.key,")
		  .append("  section.key, section.status, section.title, section.pos, section.endDate,")
		  .append("  page.key, page.status, page.lastModified, page.lastPublicationDate,")
		  .append("  body.lastModified, max(parts.lastModified), infos.recentLaunch")
		  .append(" from pfbinder as binder")
		  .append(" inner join binder.sections as section")
		  .append(" inner join section.pages as page")
		  .append(" inner join page.body as body")
		  .append(" left join body.parts as parts")
		  .append(" left join pfbinderuserinfos as infos on (infos.binder.key=binder.key and infos.identity.key=:identityKey)")
		  .append(" where exists (select membership.key from bgroupmember as membership")
		  .append("   where membership.group.key=binder.baseGroup.key and membership.identity.key=:identityKey and membership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select sectionMembership.key from bgroupmember as sectionMembership")
		  .append("   where sectionMembership.group.key=section.baseGroup.key and sectionMembership.identity.key=:identityKey and sectionMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("')")
		  .append(" ) or exists (select page.key from pfpage as coachedPage")
		  .append("   inner join coachedPage.baseGroup as pageGroup")
		  .append("   inner join pageGroup.members as pageMembership on (pageMembership.identity.key=:identityKey and pageMembership.role in ('").append(PortfolioRoles.coach.name()).append("','").append(PortfolioRoles.reviewer.name()).append("'))")
		  .append("   where coachedPage.key=page.key")
		  .append(" ) group by binder.key, section.key, section.status, section.title, section.pos, section.endDate,")
		  .append("    page.key, page.status, page.lastModified, page.lastPublicationDate, body.lastModified, infos.recentLaunch");

		Date now = new Date();
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
				AssessedBinder binder = keyToBinder.get(binderKey);
				if(binder == null) {
					continue;
				}

				Long sectionKey = (Long)object[pos++];
				String sectionStatus = (String)object[pos++];
				String sectionTitle = (String)object[pos++];
				Number sectionPosNumber = (Number)object[pos++];
				Date sectionEndDate = (Date)object[pos++];;
				Long pageKey = (Long)object[pos++];
				String pageStatus = (String)object[pos++];
				Date pageLastModified = (Date)object[pos++];
				Date pageLastPublication = (Date)object[pos++];
				Date bodyLastModified = (Date)object[pos++];
				Date partLastModified = (Date)object[pos++];
				Date recentLaunch = (Date)object[pos++];
				
				Date lastModified = pageLastModified;
				if(lastModified == null ||
						(lastModified != null && partLastModified != null && partLastModified.after(lastModified))) {
					lastModified = partLastModified;
				}
				if(lastModified == null ||
						(lastModified != null && bodyLastModified != null && bodyLastModified.after(lastModified))) {
					lastModified = bodyLastModified;
				}
				if(lastModified != null && (binder.getLastModified() == null || binder.getLastModified().before(lastModified))) {
					binder.setLastModified(lastModified);
				}
				
				if(binder.getRecentLaunch() == null) {
					binder.setRecentLaunch(recentLaunch);
				}
				
				if(pageKey != null && pageStatus == null) {
					binder.incrementNumOfDraftPages();
				} else if(StringHelper.containsNonWhitespace(pageStatus)) {
					PageStatus status = PageStatus.valueOf(pageStatus);
					if(PageStatus.isClosed(status, sectionEndDate, now)) {
						binder.incrementNumOfClosedPages();
					} else if(PageStatus.draft.name().equals(pageStatus)) {
						binder.incrementNumOfDraftPages();
					} else if(PageStatus.inRevision.name().equals(pageStatus)) {
						binder.incrementNumOfInRevisionPages();
					}
				}
				
				if(recentLaunch == null && pageLastPublication != null) {
					binder.incrementNumOfNewlyPublishedPages();
				} else if(recentLaunch != null && pageLastPublication != null && recentLaunch.before(pageLastPublication)) {
					binder.incrementNumOfNewlyPublishedPages();
				}
				
				if(!sectionKeys.contains(sectionKey)) {
					SectionStatus status = SectionStatus.notStarted;
					if(StringHelper.containsNonWhitespace(sectionStatus)) {
						status = SectionStatus.valueOf(sectionStatus);
					}
					if(status == SectionStatus.notStarted || status == SectionStatus.inProgress) {
						binder.setNumOfOpenSections(binder.getNumOfOpenSections() + 1);
					}

					if(status == SectionStatus.inProgress || status == SectionStatus.closed) {
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
