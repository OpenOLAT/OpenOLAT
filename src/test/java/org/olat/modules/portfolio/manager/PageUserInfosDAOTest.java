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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageUserInfosDAOTest extends OlatTestCase {
	
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageUserInfosDAO pageUserInfosDao;
	
	@Test
	public void createPageUserInfos() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-1");
		BinderImpl binder = binderDao.createAndPersist("Binder pui1", "A binder with a page for infos", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		dbInstance.commitAndCloseSession();

		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.incoming, page, coach);
		Assert.assertNotNull(infos);
		Assert.assertEquals(coach, infos.getIdentity());
		Assert.assertEquals(page, infos.getPage());
		Assert.assertEquals(PageUserStatus.incoming, infos.getStatus());
		Assert.assertFalse(infos.isMark());
		dbInstance.commit();
	}
	
	@Test
	public void getPageUserInfos() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-2");
		BinderImpl binder = binderDao.createAndPersist("Binder pui1", "A binder with a page for infos", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.inProcess, page, coach);
		dbInstance.commitAndCloseSession();
		
		PageUserInformations reloadedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNotNull(reloadedInfos);
		Assert.assertEquals(infos, reloadedInfos);
		Assert.assertEquals(coach, reloadedInfos.getIdentity());
		Assert.assertEquals(page, reloadedInfos.getPage());
		Assert.assertEquals(PageUserStatus.inProcess, reloadedInfos.getStatus());
		Assert.assertFalse(reloadedInfos.isMark());
		dbInstance.commit();
	}
	
	@Test
	public void updatePageUserInfos() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-2");
		BinderImpl binder = binderDao.createAndPersist("Binder pui1", "A binder with a page for infos", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.inProcess, page, coach);
		dbInstance.commitAndCloseSession();
		
		//update
		pageUserInfosDao.updateStatus(page, PageUserStatus.done);
		dbInstance.commitAndCloseSession();

		//check
		PageUserInformations reloadedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNotNull(reloadedInfos);
		Assert.assertEquals(infos, reloadedInfos);
		Assert.assertEquals(PageUserStatus.done, reloadedInfos.getStatus());
		dbInstance.commit();
	}
	
	@Test
	public void updatePageUserInfosRestricted() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-4");
		BinderImpl binder = binderDao.createAndPersist("Binder pui4", "A binder with a page for infos to batch update", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.inProcess, page, coach);
		dbInstance.commitAndCloseSession();
		
		//update
		pageUserInfosDao.updateStatus(page, PageUserStatus.done, PageUserStatus.inProcess);
		dbInstance.commitAndCloseSession();

		//check nothing changed
		PageUserInformations reloadedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNotNull(reloadedInfos);
		Assert.assertEquals(infos, reloadedInfos);
		Assert.assertEquals(PageUserStatus.done, reloadedInfos.getStatus());
		dbInstance.commit();
	}
	
	@Test
	public void updatePageUserInfosRestricted_nothing() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-5");
		BinderImpl binder = binderDao.createAndPersist("Binder pui5", "A binder with a page for infos to batch udate", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.inProcess, page, coach);
		dbInstance.commitAndCloseSession();
		
		//update
		pageUserInfosDao.updateStatus(page, PageUserStatus.done, PageUserStatus.incoming);
		dbInstance.commitAndCloseSession();

		//check nothing changed
		PageUserInformations reloadedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNotNull(reloadedInfos);
		Assert.assertEquals(infos, reloadedInfos);
		Assert.assertEquals(PageUserStatus.inProcess, reloadedInfos.getStatus());
		dbInstance.commit();
	}
	
	@Test
	public void deletePageUserInfosRestricted_page() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("pui-6");
		BinderImpl binder = binderDao.createAndPersist("Binder pui6", "A binder with a page for infos to delete", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		Page page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, section, null);
		PageUserInformations infos = pageUserInfosDao.create(PageUserStatus.inProcess, page, coach);
		dbInstance.commitAndCloseSession();
		
		//check that we have something to delete
		PageUserInformations reloadedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNotNull(reloadedInfos);
		Assert.assertEquals(infos, reloadedInfos);
		
		//update
		pageUserInfosDao.delete(page);
		dbInstance.commitAndCloseSession();
		
		//check that we have deleted something
		PageUserInformations deletedInfos = pageUserInfosDao.getPageUserInfos(page, coach);
		Assert.assertNull(deletedInfos);
	}
}
