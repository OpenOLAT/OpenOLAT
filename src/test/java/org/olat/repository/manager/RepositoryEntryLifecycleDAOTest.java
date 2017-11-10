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
package org.olat.repository.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifecycleDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifeCycleDao;
	
	@Test
	public void createLifeCycle() {
		String label = "My first life cycle";
		String softKey = UUID.randomUUID().toString();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, +2);
		Date to = cal.getTime();
		RepositoryEntryLifecycle relf = reLifeCycleDao.create(label, softKey, true, from, to);
		dbInstance.commitAndCloseSession();
		
		//check
		Assert.assertNotNull(relf);
		Assert.assertNotNull(relf.getKey());
		Assert.assertNotNull(relf.getCreationDate());
		Assert.assertNotNull(relf.getLastModified());
		Assert.assertEquals("My first life cycle", relf.getLabel());
		Assert.assertEquals(softKey, relf.getSoftKey());
		Assert.assertTrue(relf.isPrivateCycle());
		Assert.assertEquals(from, relf.getValidFrom());
		Assert.assertEquals(to, relf.getValidTo());
	}
	
	@Test
	public void getLifeCycle() {
		String label = "My second life cycle";
		String softKey = UUID.randomUUID().toString();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, +2);
		Date to = cal.getTime();
		RepositoryEntryLifecycle relf = reLifeCycleDao.create(label, softKey, true, from, to);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relf);
		Assert.assertNotNull(relf.getKey());
		
		//check
		RepositoryEntryLifecycle loadedLifeCycle = reLifeCycleDao.loadById(relf.getKey());
		Assert.assertNotNull(loadedLifeCycle);
		Assert.assertNotNull(loadedLifeCycle.getCreationDate());
		Assert.assertNotNull(loadedLifeCycle.getLastModified());
		Assert.assertEquals(relf.getKey(), loadedLifeCycle.getKey());
		Assert.assertEquals("My second life cycle", loadedLifeCycle.getLabel());
		Assert.assertEquals(softKey, loadedLifeCycle.getSoftKey());
		Assert.assertTrue(loadedLifeCycle.isPrivateCycle());
		Assert.assertNotNull(loadedLifeCycle.getValidFrom());
		Assert.assertNotNull(loadedLifeCycle.getValidTo());
	}
	
	@Test
	public void loadLifeCycle_byEntry() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry("", false);
		
		String label = "A life cycle";
		String softKey = UUID.randomUUID().toString();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, +2);
		Date to = cal.getTime();
		RepositoryEntryLifecycle cycle = reLifeCycleDao.create(label, softKey, true, from, to);
		re.setLifecycle(cycle);
		re = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(cycle);
		Assert.assertNotNull(cycle.getKey());
		
		//check
		RepositoryEntryLifecycle loadedLifeCycle = reLifeCycleDao.loadByEntry(re);
		Assert.assertNotNull(loadedLifeCycle);
		Assert.assertNotNull(loadedLifeCycle.getCreationDate());
		Assert.assertNotNull(loadedLifeCycle.getLastModified());
		Assert.assertEquals(cycle.getKey(), loadedLifeCycle.getKey());
		Assert.assertEquals("A life cycle", loadedLifeCycle.getLabel());
		Assert.assertEquals(softKey, loadedLifeCycle.getSoftKey());
		Assert.assertTrue(loadedLifeCycle.isPrivateCycle());
		Assert.assertNotNull(loadedLifeCycle.getValidFrom());
		Assert.assertNotNull(loadedLifeCycle.getValidTo());
	}
	
	@Test
	public void loadPublicLifeCycles() {
		//create a public life cycle object
		String label = "A public life cycle";
		RepositoryEntryLifecycle publicRelf = reLifeCycleDao.create(label, null, false, null, null);
		dbInstance.commitAndCloseSession();
		
		//load public life cycle
		List<RepositoryEntryLifecycle> publicLifeCycles = reLifeCycleDao.loadPublicLifecycle();
		Assert.assertNotNull(publicLifeCycles);
		Assert.assertFalse(publicLifeCycles.isEmpty());
		Assert.assertTrue(publicLifeCycles.contains(publicRelf));
	}
}
