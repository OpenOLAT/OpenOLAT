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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.HTMLPart;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	
	
	@Test
	public void createBinderWithSectionAndPage() {
		BinderImpl binder = binderDao.createAndPersist("Binder p1", "A binder with a page", null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(page);
		Assert.assertNotNull(page.getKey());
		Assert.assertNotNull(page.getCreationDate());
		Assert.assertNotNull(page.getLastModified());
		Assert.assertEquals("New page", page.getTitle());
		Assert.assertEquals("A brand new page.", page.getSummary());
	}
	
	@Test
	public void createBinderWithSectionAndPages() {
		BinderImpl binder = binderDao.createAndPersist("Binder p1", "A binder with a page", null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		for(int i=0; i<5; i++) {
			pageDao.createAndPersist("New page " + i, "A brand new page.", null, reloadedSection, null);
		}
		dbInstance.commitAndCloseSession();
		
		List<Page> pages = pageDao.getPages(binder);
		Assert.assertNotNull(pages);
		Assert.assertEquals(5, pages.size());

	}
	
	@Test
	public void createBinderWithSectionAndPageAndPart() {
		BinderImpl binder = binderDao.createAndPersist("Binder p1", "A binder with a page", null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		
		HTMLPart htmlPart = new HTMLPart();
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, htmlPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		Page reloadedPage = pageDao.loadByKey(page.getKey());
		Assert.assertNotNull(reloadedPage);
		List<PagePart> parts = reloadedPage.getBody().getParts();
		Assert.assertNotNull(parts);
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals(htmlPart, parts.get(0));
		
		//reload only pages
		List<PagePart> onlyParts = pageDao.getParts(page.getBody());
		Assert.assertNotNull(onlyParts);
		Assert.assertEquals(1, onlyParts.size());
		Assert.assertEquals(htmlPart, onlyParts.get(0));
		
	}

}
