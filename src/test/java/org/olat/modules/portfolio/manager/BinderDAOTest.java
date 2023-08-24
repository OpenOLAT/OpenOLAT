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
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.BinderPageUsage;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private PortfolioService portfolioService;
	
	@Test
	public void createNewBinder() {
		String title = "My portfolio";
		String summary = "My live";
		
		BinderImpl binder = binderDao.createAndPersist(title, summary, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(binder);
		Assert.assertNotNull(binder.getKey());
		Assert.assertNotNull(binder.getCreationDate());
		Assert.assertNotNull(binder.getLastModified());
		Assert.assertNotNull(binder.getBaseGroup());
		Assert.assertEquals(title, binder.getTitle());
		Assert.assertEquals(summary, binder.getSummary());
		
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		Assert.assertNotNull(reloadedBinder);
		Assert.assertNotNull(reloadedBinder.getKey());
		Assert.assertEquals(binder.getKey(), reloadedBinder.getKey());
		Assert.assertEquals(binder, reloadedBinder);
		Assert.assertNotNull(reloadedBinder.getCreationDate());
		Assert.assertNotNull(reloadedBinder.getLastModified());
		Assert.assertEquals(title, reloadedBinder.getTitle());
		Assert.assertEquals(summary, reloadedBinder.getSummary());
	}
	
	@Test
	public void createBinderWithSection() {
		String title = "Binder 2";
		String summary = "Binder with one section.";
		
		BinderImpl binder = binderDao.createAndPersist(title, summary, null, null);
		
		String sectionTitle = "First section";
		String sectionDesc = "My first section.";
		binderDao.createSection(sectionTitle, sectionDesc, null, null, binder);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createBinderWithSection_2steps() {
		String title = "Binder 2";
		String summary = "Binder with one section.";
		
		Binder binder = binderDao.createAndPersist(title, summary, null, null);
		dbInstance.commitAndCloseSession();
		
		String section1Title = "First section";
		String section1Desc = "My first section.";
		binder = binderDao.loadByKey(binder.getKey());
		Section section1 = binderDao.createSection(section1Title, section1Desc, null, null, binder);
		dbInstance.commitAndCloseSession();
		
		String section2Title = "Second section";
		String section2Desc = "My second section.";
		binder = binderDao.loadByKey(binder.getKey());
		Section section2 = binderDao.createSection(section2Title, section2Desc, null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		List<Section> sections = ((BinderImpl)reloadedBinder).getSections();
		Assert.assertNotNull(sections);
		Assert.assertEquals(2, sections.size());
		Assert.assertEquals(section1, sections.get(0));
		Assert.assertEquals(section2, sections.get(1));
	}
	
	@Test
	public void loadSections() {
		String title = "Binder 3";
		String summary = "Binder with two sections.";
		
		Binder binder = binderDao.createAndPersist(title, summary, null, null);
		dbInstance.commitAndCloseSession();
		
		String section1Title = "1. section";
		String section1Desc = "My first section.";
		binder = binderDao.loadByKey(binder.getKey());
		Section section1 = binderDao.createSection(section1Title, section1Desc, null, null, binder);
		dbInstance.commitAndCloseSession();
		
		String section2Title = "2. section";
		String section2Desc = "My second section.";
		binder = binderDao.loadByKey(binder.getKey());
		Section section2 = binderDao.createSection(section2Title, section2Desc, null, null, binder);
		dbInstance.commitAndCloseSession();

		List<Section> sections = binderDao.getSections(binder);
		Assert.assertNotNull(sections);
		Assert.assertEquals(2, sections.size());
		Assert.assertEquals(section1, sections.get(0));
		Assert.assertEquals(section2, sections.get(1));
	}
	
	@Test
	public void getOwnedBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner");
		Binder binder = portfolioService.createNewBinder("My own binder", "", "", owner);
		dbInstance.commit();
		
		Binder deletedBinder = portfolioService.createNewBinder("My own deleted binder", "", "", owner);
		deletedBinder.setBinderStatus(BinderStatus.deleted);
		deletedBinder = portfolioService.updateBinder(deletedBinder);
		dbInstance.commitAndCloseSession();
		
		List<Binder> ownedBinders = binderDao.getOwnedBinders(owner);
		Assert.assertNotNull(ownedBinders);
		Assert.assertEquals(1, ownedBinders.size());
		Assert.assertTrue(ownedBinders.contains(binder));
		Assert.assertFalse(ownedBinders.contains(deletedBinder));
	}
	
	@Test
	public void searchOwnedLastBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner");
		Binder binder = portfolioService.createNewBinder("My own binder", "", "", owner);
		dbInstance.commit();
		portfolioService.updateBinderUserInformations(binder, owner);
		dbInstance.commitAndCloseSession();
		
		List<BinderStatistics> lastBinders = binderDao.searchOwnedLastBinders(owner, 2);
		Assert.assertNotNull(lastBinders);
		Assert.assertEquals(1, lastBinders.size());
		Assert.assertEquals(binder.getKey(), lastBinders.get(0).getKey());
	}
	
	@Test
	public void searchOwnedBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner");
		Binder binder = portfolioService.createNewBinder("My own binder", "", "", owner);
		dbInstance.commit();
		
		List<BinderStatistics> lastBinders = binderDao.searchOwnedBinders(owner, false);
		Assert.assertNotNull(lastBinders);
		Assert.assertEquals(1, lastBinders.size());
		Assert.assertEquals(binder.getKey(), lastBinders.get(0).getKey());
	}
	
	@Test
	public void countOwnedBinders() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner");
		Binder binder = portfolioService.createNewBinder("My own binder", "", "", owner);
		dbInstance.commit();
		Assert.assertNotNull(binder);
		
		int ownedBinders = binderDao.countOwnedBinders(owner, false);
		Assert.assertEquals(1, ownedBinders);
	}
	
	/**
	 * Only check that the query doesn't make an error
	 */
	@Test
	public void getBinderStatistics() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("binder-owner");
		Binder binder = portfolioService.createNewBinder("My statistical binder", "", "", owner);
		dbInstance.commit();
		Assert.assertNotNull(binder);
		
		BinderStatistics stats = binderDao.getBinderStatistics(binder);
		Assert.assertNotNull(stats);
		Assert.assertEquals(binder.getKey(), stats.getKey());
		Assert.assertEquals("open", stats.getStatus());
		Assert.assertEquals("My statistical binder", stats.getTitle());
	}
	
	@Test
	public void usedInBinders() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Binder binder = portfolioService.createNewBinder("Binder p2", "A binder with 2 page", null, author);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, reloadedSection, null);
		Media media = mediaDao.createMediaAndVersion("Media", "Binder", "Alt-Binder", "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		List<BinderPageUsage> binders = binderDao.usedInBinders(media);
		Assert.assertNotNull(binders);
		Assert.assertEquals(1, binders.size());
		Assert.assertTrue(binders.get(0).getBinderKey().equals(binder.getKey()));
	}
}
