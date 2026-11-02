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
package org.olat.modules.ceditor.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.CategoryToElement;
import org.olat.modules.ceditor.Page;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private CategoryDAO categoryDao;
	@Autowired
	private PortfolioService portfolioService;
	
	
	@Test
	public void createCategory() {
		Category category = categoryDao.createAndPersistCategory("Swag");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(category);
		Assert.assertNotNull(category.getKey());
		Assert.assertNotNull(category.getCreationDate());
		Assert.assertEquals("Swag", category.getName());
	}
	
	@Test
	public void createRelationToCategory() {
		Category category = categoryDao.createAndPersistCategory("Cool");
		dbInstance.commitAndCloseSession();
		
		// create relation
		String rndType = UUID.randomUUID().toString();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(rndType, 234l);
		categoryDao.appendRelation(ores, category);
		dbInstance.commitAndCloseSession();
		
		//load relation
		List<Category> categories = categoryDao.getCategories(ores);
		Assert.assertNotNull(categories);
		Assert.assertEquals(1, categories.size());
		Assert.assertEquals(category, categories.get(0));
	}
	
	@Test
	public void getCategorizedOwnedPages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-1");
		Binder binder = portfolioService.createNewBinder("Binder p2", "A binder with 2 page", null, author);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();

		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page1 = pageDao.createAndPersist("Jules Verne", "Cing semaine en ballon", null, null, true, reloadedSection, null);
		Page page2 = pageDao.createAndPersist("J. Verne", "Une ville flottante", null, null, true, reloadedSection, null);
		Page page3 = pageDao.createAndPersist("Verne", "Les Tribulations d'un Chinois en Chine", null, null, true, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		
		List<String> categories1 = new ArrayList<>();
		categories1.add("Jules");
		categories1.add("Verne");
		categories1.add("Aventure");
		categories1.add("Voyage");
		portfolioService.updateCategories(page1, categories1);
		
		List<String> categories2 = new ArrayList<>();
		categories2.add("Jules");
		categories2.add("Verne");
		categories2.add("Anticipation");
		categories2.add("Technologie");
		portfolioService.updateCategories(page2, categories2);
		
		List<String> categories3 = new ArrayList<>();
		categories3.add("Jules");
		categories3.add("Verne");
		categories3.add("Aventure");
		categories3.add("Chine");
		portfolioService.updateCategories(page3, categories3);
		dbInstance.commitAndCloseSession();
		
		List<CategoryToElement> categories = categoryDao.getCategorizedOwnedPages(author);
		Assert.assertNotNull(categories);
		Assert.assertEquals(12, categories.size());
	}
	
	@Test
	public void getOwnedCategories() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-6");
		Identity otherAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-7");
		Binder binder = portfolioService.createNewBinder("Binder owned", "A binder with categories", null, author);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Binder otherBinder = portfolioService.createNewBinder("Binder other", "A binder from someone else", null, otherAuthor);
		Section otherSection = binderDao.createSection("Section", "Other section", null, null, otherBinder);
		dbInstance.commitAndCloseSession();

		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Jules Verne", "Michel Strogoff", null, null, true, reloadedSection, null);
		Section reloadedOtherSection = binderDao.loadSectionByKey(otherSection.getKey());
		Page otherPage = pageDao.createAndPersist("Jules Verne", "Mathias Sandorf", null, null, true, reloadedOtherSection, null);
		dbInstance.commitAndCloseSession();

		String binderCategory = ("Binder-" + UUID.randomUUID()).substring(0, 30);
		String sectionCategory = ("Section-" + UUID.randomUUID()).substring(0, 31);
		String pageCategory = ("Page-" + UUID.randomUUID()).substring(0, 32);
		String otherCategory = ("Other-" + UUID.randomUUID()).substring(0, 29);
		portfolioService.updateCategories(binder, List.of(binderCategory));
		portfolioService.updateCategories(section, List.of(sectionCategory));
		portfolioService.updateCategories(page, List.of(pageCategory));
		portfolioService.updateCategories(otherPage, List.of(otherCategory));
		dbInstance.commitAndCloseSession();

		List<String> ownedCategories = categoryDao.getOwnedCategories(author).stream()
				.map(Category::getName)
				.toList();
		Assert.assertEquals(3, ownedCategories.size());
		Assert.assertTrue(ownedCategories.contains(binderCategory));
		Assert.assertTrue(ownedCategories.contains(sectionCategory));
		Assert.assertTrue(ownedCategories.contains(pageCategory));
		Assert.assertFalse(ownedCategories.contains(otherCategory));
	}

	@Test
	public void getCategorizedSectionsAndPages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-1");
		Binder binder = portfolioService.createNewBinder("Binder about Verne", "A binder with a single page", null, author);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();

		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Jules Verne", "Deux ans de vacances", null, null, true, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		
		List<String> categoriesSection = new ArrayList<>();
		categoriesSection.add("Jules");
		categoriesSection.add("Verne");
		portfolioService.updateCategories(section, categoriesSection);
		
		List<String> categoriesPage = new ArrayList<>();
		categoriesPage.add("Aventure");
		categoriesPage.add("Vacances");
		portfolioService.updateCategories(page, categoriesPage);
		dbInstance.commitAndCloseSession();

		//load by section
		List<CategoryToElement> categories = categoryDao.getCategorizedSectionAndPages(section);
		Assert.assertNotNull(categories);
		Assert.assertEquals(4, categories.size());
		
		//load by binder
		List<CategoryToElement> categoriesByBinder = categoryDao.getCategorizedSectionsAndPages(binder);
		Assert.assertNotNull(categoriesByBinder);
		Assert.assertEquals(4, categoriesByBinder.size());
	}

}
