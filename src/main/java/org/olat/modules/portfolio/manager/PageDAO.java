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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AbstractPart;
import org.olat.modules.portfolio.model.AssignmentImpl;
import org.olat.modules.portfolio.model.PageBodyImpl;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PageDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private UserCommentsDAO userCommentsDAO;
	@Autowired
	private PortfolioService portfolioService;

	/**
	 * 
	 * @param title
	 * @param summary
	 * @param section If the section is null, the page is floating.
	 * @param body If the body is null, a new one is create.
	 * @return
	 */
	public Page createAndPersist(String title, String summary, String imagePath, PageImageAlign align, boolean editable, Section section, PageBody body) {
		PageImpl page = new PageImpl();
		page.setCreationDate(new Date());
		page.setLastModified(page.getCreationDate());
		page.setTitle(title);
		page.setSummary(summary);
		page.setImagePath(imagePath);
		page.setImageAlignment(align);
		page.setEditable(editable);
		page.setBaseGroup(groupDao.createGroup());
		if(body == null) {
			page.setBody(createAndPersistPageBody());
		} else {
			page.setBody(body);
		}
		if(section != null) {
			page.setSection(section);
			section.getPages().add(page);
			dbInstance.getCurrentEntityManager().persist(page);
			dbInstance.getCurrentEntityManager().merge(section);
		} else {
			dbInstance.getCurrentEntityManager().persist(page);
		}
		return page;
	}
	
	public Page updatePage(Page page) {
		((PageImpl)page).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(page);
	}
	
	public PageBody createAndPersistPageBody() {
		PageBodyImpl body = new PageBodyImpl();
		body.setCreationDate(new Date());
		body.setLastModified(body.getCreationDate());
		dbInstance.getCurrentEntityManager().persist(body);
		return body;
	}
	
	public Group getGroup(Page page) {
		StringBuilder sb = new StringBuilder();
		sb.append("select baseGroup from pfpage as page ")
		  .append(" inner join page.baseGroup as baseGroup")
		  .append(" where page.key=:pageKey");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Group.class)
				.setParameter("pageKey", page.getKey())
				.getSingleResult();
	}
	
	public List<Page> getPages(BinderRef binder, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch page.body as body")
		  .append(" where section.binder.key=:binderKey");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "page.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "page.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or exists (select cat from pfcategoryrelation rel ")
			  .append("   inner join rel.category cat")
			  .append("   where rel.resId=page.key and rel.resName='Page' and lower(cat.name) like :searchString")
			  .append(" )");
			sb.append(")");
		}
		sb.append(" order by section.pos, page.pos");
		
		TypedQuery<Page> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("binderKey", binder.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		return query.getResultList();
	}
	
	public List<Page> getPages(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch page.body as body")
		  .append(" where section.key=:sectionKey");
		sb.append(" order by page.pos");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("sectionKey", section.getKey())
			.getResultList();
	}
	
	public int countOwnedPages(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(distinct page.key) from pfpage as page")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");

		List<Long> count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("ownerKey", owner.getKey())
			.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null ? count.get(0).intValue() : 0;
	}
	
	public List<Page> getOwnedPages(IdentityRef owner, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.body as body")
		  .append(" left join fetch page.section as section")
		  .append(" left join fetch section.binder as binder")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "page.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "page.summary", "searchString", dbInstance.getDbVendor());
			sb.append(" or exists (select cat from pfcategoryrelation rel ")
			  .append("   inner join rel.category cat")
			  .append("   where rel.resId=page.key and rel.resName='Page' and lower(cat.name) like :searchString")
			  .append(" )");
			sb.append(")");
		}
		sb.append(" order by page.creationDate desc");
		
		TypedQuery<Page> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("ownerKey", owner.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		return query.getResultList();
	}
	
	public List<Page> getDeletedPages(IdentityRef owner, String searchString) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.body as body")
		  .append(" inner join fetch page.baseGroup as bGroup")
		  .append(" inner join bGroup.members as membership")
		  .append(" where membership.identity.key=:ownerKey and membership.role='").append(PortfolioRoles.owner.name()).append("'")
		  .append(" and page.section.key is null and page.status='").append(PageStatus.deleted.name()).append("'");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "page.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "page.summary", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		sb.append(" order by page.creationDate desc");
		
		TypedQuery<Page> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("ownerKey", owner.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		return query.getResultList();
	}
	
	public List<Identity> getMembers(Page page, String... roles)  {
		if(page == null || roles == null || roles.length == 0 || roles[0] == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.identity from pfpage as page")
		  .append(" inner join page.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where page.key=:pageKey and membership.role in (:roles)");
		
		List<String> roleList = new ArrayList<>(roles.length);
		for(String role:roles) {
			if(StringHelper.containsNonWhitespace(role)) {
				roleList.add(role);
			}
		}
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Identity.class)
			.setParameter("pageKey", page.getKey())
			.setParameter("roles", roleList)
			.getResultList();
	}
	
	public Page loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" left join fetch page.section as section")
		  .append(" left join fetch section.binder as binder")
		  .append(" left join fetch page.body as body")
		  .append(" where page.key=:pageKey");
		
		List<Page> pages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("pageKey", key)
			.getResultList();
		return pages == null || pages.isEmpty() ? null : pages.get(0);
	}
	
	public List<Page> loadByKeys(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(512);
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" left join fetch page.section as section")
		  .append(" left join fetch section.binder as binder")
		  .append(" left join fetch page.body as body")
		  .append(" where page.key in (:pageKeys)");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("pageKeys", keys)
			.getResultList();
	}
	
	public Page loadByBody(PageBody body) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" left join fetch page.section as section")
		  .append(" left join fetch section.binder as binder")
		  .append(" left join fetch page.body as body")
		  .append(" where body.key=:bodyKey");
		
		List<Page> pages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("bodyKey", body.getKey())
			.getResultList();
		return pages == null || pages.isEmpty() ? null : pages.get(0);
	}
	
	public PageBody loadPageBodyByKey(Long key) {
		String query = "select body from pfpagebody as body where body.key=:bodyKey";
		
		List<PageBody> bodies = dbInstance.getCurrentEntityManager()
			.createQuery(query, PageBody.class)
			.setParameter("bodyKey", key)
			.getResultList();
		return bodies == null || bodies.isEmpty() ? null : bodies.get(0);
	}
	
	public boolean isFormEntryInUse(RepositoryEntryRef formEntry) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select part.key from pfformpart as part")
		  .append(" where part.formEntry.key=:formEntryKey");

		List<Long> counts = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("formEntryKey", formEntry.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return counts != null && !counts.isEmpty() && counts.get(0) != null && counts.get(0).intValue() >= 0;
	}
	
	public List<Page> getLastPages(IdentityRef owner, int maxResults) {
		StringBuilder sc = new StringBuilder(1024);
		sc.append("select page.key, page.lastModified, body.lastModified, part.lastModified")
		  .append(" from pfpage as page")
		  .append(" inner join page.body as body")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" left join body.parts as part")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )");
		
		List<Object[]> rawLastModifieds = dbInstance.getCurrentEntityManager()
			.createQuery(sc.toString(), Object[].class)
			.setParameter("ownerKey", owner.getKey())
			.setFirstResult(0)
			.getResultList();
		
		Map<Long, PageLastModified> lastModifieMap = new HashMap<>();
		for(Object[] rawLastModified:rawLastModifieds) {
			Long pageKey = (Long)rawLastModified[0];
			Date lastModified = (Date)rawLastModified[1];
			Date bodyLastModified = (Date)rawLastModified[2];
			Date partLastModified = (Date)rawLastModified[3];
			if(bodyLastModified != null && bodyLastModified.after(lastModified)) {
				lastModified = bodyLastModified;
			}
			if(partLastModified != null && partLastModified.after(lastModified)) {
				lastModified = partLastModified;
			}
			
			PageLastModified pageLastModified = lastModifieMap
					.computeIfAbsent(pageKey, PageLastModified::new);
			if(pageLastModified.getLastModified() == null || lastModified.after(pageLastModified.getLastModified())) {
				pageLastModified.setLastModified(lastModified);
			}
		}
		
		List<PageLastModified> lastPages = new ArrayList<>(lastModifieMap.values());
		Collections.sort(lastPages);
		Collections.reverse(lastPages);
		List<Long> lastPageKeys = new ArrayList<>(maxResults);
		for(int i=0; i<lastPages.size() && i < maxResults; i++) {
			lastPageKeys.add(lastPages.get(i).getPageKey());
		}
		List<Page> pages = loadByKeys(lastPageKeys);
		if(pages.size() > 1) {
			Map<Long,Page> pageMap = pages.stream()
				.collect(Collectors.toMap(Page::getKey, p -> p, (u, v) -> u));
			
			List<Page> orderedPages = new ArrayList<>(pages.size());
			for(PageLastModified lastPage:lastPages) {
				Page page = pageMap.get(lastPage.getPageKey());
				if(page != null) {
					orderedPages.add(page);
				}
			}
			pages = orderedPages;
		}
		
		return pages;
	}
	
	private static class PageLastModified implements Comparable<PageLastModified> {
		
		private final Long pageKey;
		private Date lastModified;
		
		private PageLastModified(Long pageKey) {
			this.pageKey = pageKey;
		}

		public Long getPageKey() {
			return pageKey;
		}

		public Date getLastModified() {
			return lastModified;
		}

		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}

		@Override
		public int compareTo(PageLastModified o) {
			return lastModified.compareTo(o.lastModified);
		}
	}
	
	public Page removePage(Page page) {
		PageImpl reloadedPage = (PageImpl)loadByKey(page.getKey());
		Section section = reloadedPage.getSection();
		if(section != null) {
			section.getPages().remove(reloadedPage);
		}
		reloadedPage.setLastModified(new Date());
		reloadedPage.setSection(null);
		reloadedPage.setPageStatus(PageStatus.deleted);
		
		List<Assignment> assignments = loadAssignmentsForRemoval(page);
		for(Assignment assignment:assignments) {
			Assignment template = assignment.getTemplateReference();
			if(template != null && template.getSection() != null) {
				// assignment within a section
				((AssignmentImpl)assignment).setPage(null);
				((AssignmentImpl)assignment).setStatus(AssignmentStatus.notStarted.name());
				dbInstance.getCurrentEntityManager().merge(assignment);
			} else if(template != null && template.getSection() == null && template.getBinder() != null) {
				// assignment instance of a binder assignment template
				dbInstance.getCurrentEntityManager().remove(assignment);
			}
		}
		
		if(section != null) {
			dbInstance.getCurrentEntityManager().merge(section);
		}
		return dbInstance.getCurrentEntityManager().merge(reloadedPage);
	}
	
	private List<Assignment> loadAssignmentsForRemoval(Page page) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select assignment from pfassignment as assignment")
		  .append(" inner join fetch assignment.section as section")
		  .append(" inner join fetch assignment.page as page")
		  .append(" where page.key=:pageKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Assignment.class)
				.setParameter("pageKey", page.getKey())
				.getResultList();
	}
	
	public PagePart persistPart(PageBody body, PagePart part) {
		AbstractPart aPart = (AbstractPart)part;
		aPart.setCreationDate(new Date());
		aPart.setLastModified(aPart.getCreationDate());
		aPart.setBody(body);
		body.getParts().size();
		body.getParts().add(aPart);
		dbInstance.getCurrentEntityManager().persist(aPart);
		dbInstance.getCurrentEntityManager().merge(body);
		return aPart;
	}
	
	public PagePart persistPart(PageBody body, PagePart part, int index) {
		AbstractPart aPart = (AbstractPart)part;
		aPart.setCreationDate(new Date());
		aPart.setLastModified(aPart.getCreationDate());
		aPart.setBody(body);
		int size = body.getParts().size();
		if(index < size && index >= 0) {
			body.getParts().add(index, aPart);
		} else {
			body.getParts().add(aPart);
		}
		dbInstance.getCurrentEntityManager().persist(aPart);
		dbInstance.getCurrentEntityManager().merge(body);
		return aPart;
	}
	
	public void removePart(PageBody body, PagePart part) {
		PagePart aPart = dbInstance.getCurrentEntityManager()
				.getReference(part.getClass(), part.getKey());
		body.getParts().size();
		body.getParts().remove(aPart);
		dbInstance.getCurrentEntityManager().remove(aPart);
		((PageBodyImpl)body).setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(body);
	}
	
	public void moveUpPart(PageBody body, PagePart part) {
		body.getParts().size();
		int index = body.getParts().indexOf(part);
		if(index > 0) {
			PagePart reloadedPart = body.getParts().remove(index);
			body.getParts().add(index - 1, reloadedPart);
		} else if(index < 0) {
			body.getParts().add(0, part);
		}
		((PageBodyImpl)body).setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(body);
	}
	
	public void moveDownPart(PageBody body, PagePart part) {
		body.getParts().size();
		int index = body.getParts().indexOf(part);
		if(index >= 0 && index + 1 < body.getParts().size()) {
			PagePart reloadedPart = body.getParts().remove(index);
			body.getParts().add(index + 1, reloadedPart);
			((PageBodyImpl)body).setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(body);
		}
	}
	
	public void movePart(PageBody body, PagePart part, PagePart sibling, boolean after) {
		body.getParts().size();
		body.getParts().remove(part);
		
		int index;
		if(sibling == null) {
			index = body.getParts().size();
		} else {
			index = body.getParts().indexOf(sibling);
		}
		if(after) {
			index++;
		}
		
		List<PagePart> parts = body.getParts();
		if(index >= 0 && index < parts.size()) {
			parts.add(index, part);
		} else {
			parts.add(part);
		}
		((PageBodyImpl)body).setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(body);
	}
	
	public List<PagePart> getParts(PageBody body) {
		StringBuilder sb = new StringBuilder();
		sb.append("select part from pfpagepart as part")
		  .append(" inner join fetch part.body as body")
		  .append(" left join fetch part.media as media")
		  .append(" where body.key=:bodyKey")
		  .append(" order by part.pos");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), PagePart.class)
			.setParameter("bodyKey", body.getKey())
			.getResultList();
	}
	
	public PagePart merge(PagePart part) {
		((AbstractPart)part).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(part);
	}
	
	/**
	 * The page cannot be detached (reload it if necessary).
	 * 
	 * @param page
	 * @return
	 */
	public int deletePage(Page page) {
		if(page == null || page.getKey() == null) return 0;//nothing to do
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
		
		PageBody body = page.getBody();
		String partQ = "delete from pfpagepart part where part.body.key=:bodyKey";
		int parts = dbInstance.getCurrentEntityManager()
				.createQuery(partQ)
				.setParameter("bodyKey", body.getKey())
				.executeUpdate();
		
		String assignmentQ = "delete from pfassignment assignment where assignment.page.key=:pageKey";
		int assignments = dbInstance.getCurrentEntityManager()
				.createQuery(assignmentQ)
				.setParameter("pageKey", page.getKey())
				.executeUpdate();
		
		portfolioService.deleteSurvey(body);
		
		dbInstance.getCurrentEntityManager().remove(page);
		dbInstance.getCurrentEntityManager().remove(body);
		
		int comments = userCommentsDAO.deleteAllComments(ores, null);

		return comments + parts + assignments + 2;
	}
}
