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

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Test
	public void loadByKey() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 1", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry loadedRe = repositoryEntryDao.loadByKey(re.getKey());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertNotNull(loadedRe.getOlatResource());
	}
	
	@Test
	public void getAllRepositoryEntries() {
		RepositoryEntry re = repositoryService.create("Rei Ayanami", "-", "Repository entry DAO Test 1", "", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		List<RepositoryEntry> allRes = repositoryEntryDao.getAllRepositoryEntries(0, 25);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(allRes);
		Assert.assertFalse(allRes.isEmpty());
		Assert.assertTrue(allRes.size() < 26);
	}
	

}