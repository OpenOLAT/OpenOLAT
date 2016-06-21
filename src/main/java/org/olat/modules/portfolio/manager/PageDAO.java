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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AbstractPart;
import org.olat.modules.portfolio.model.PageBodyImpl;
import org.olat.modules.portfolio.model.PageImpl;
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
	
	
	/**
	 * 
	 * @param title
	 * @param summary
	 * @param section If the section is null, the page is floating.
	 * @param body If the body is null, a new one is create.
	 * @return
	 */
	public Page createAndPersist(String title, String summary, String imagePath, Section section, PageBody body) {
		PageImpl page = new PageImpl();
		page.setCreationDate(new Date());
		page.setLastModified(page.getCreationDate());
		page.setTitle(title);
		page.setSummary(summary);
		page.setImagePath(imagePath);
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
	
	public List<Page> getPages(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch page.body as body")
		  .append(" where section.binder.key=:binderKey")
		  .append(" order by section.pos, page.pos");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
	}
	
	public List<Page> getPages(SectionRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch page.body as body")
		  .append(" where section.key=:sectionKey")
		  .append(" order by page.pos");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("sectionKey", binder.getKey())
			.getResultList();
	}
	
	public List<Page> getOwnedPages(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.body as body")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("     inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(GroupRoles.owner.name()).append("')")
		  .append("  	where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key or pageMember.group.key=page.baseGroup.key")
		  .append(" )");
		
		List<Page> pages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("ownerKey", owner.getKey())
			.getResultList();
		return pages;
	}
	
	public Page loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page from pfpage as page")
		  .append(" inner join fetch page.baseGroup as baseGroup")
		  .append(" inner join fetch page.section as section")
		  .append(" inner join fetch page.body as body")
		  .append(" where page.key=:pageKey");
		
		List<Page> pages = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setParameter("pageKey", key)
			.getResultList();
		return pages == null || pages.isEmpty() ? null : pages.get(0);
	}
	
	public PageBody loadPageBodyByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select body from pfpagebody as body")
		  .append(" where body.key=:bodyKey");
		
		List<PageBody> bodies = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), PageBody.class)
			.setParameter("bodyKey", key)
			.getResultList();
		return bodies == null || bodies.isEmpty() ? null : bodies.get(0);
	}
	
	public PagePart persistPart(PageBody body, PagePart part) {
		AbstractPart aPart = (AbstractPart)part;
		aPart.setCreationDate(new Date());
		aPart.setLastModified(aPart.getCreationDate());
		aPart.setBody(body);
		body.getParts().add(aPart);
		dbInstance.getCurrentEntityManager().persist(aPart);
		dbInstance.getCurrentEntityManager().merge(body);
		return aPart;
	}
	
	public List<PagePart> getParts(PageBody body) {
		StringBuilder sb = new StringBuilder();
		sb.append("select part from pfpagepart as part")
		  .append(" inner join fetch part.body as body")
		  .append(" left join fetch part.media as media")
		  .append(" where body.key=:bodyKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), PagePart.class)
			.setParameter("bodyKey", body.getKey())
			.getResultList();
	}
	
	public PagePart merge(PagePart part) {
		return dbInstance.getCurrentEntityManager().merge(part);
	}
}
