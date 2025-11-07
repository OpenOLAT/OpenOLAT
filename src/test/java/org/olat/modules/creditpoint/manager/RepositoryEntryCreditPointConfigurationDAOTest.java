/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryCreditPointConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private RepositoryEntryCreditPointConfigurationDAO repositoryEntryCreditPointConfigurationDao;
	
	@Test
	public void createConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("config-1");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("config-coin-1", "CC1", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryCreditPointConfiguration config = repositoryEntryCreditPointConfigurationDao.createConfiguration(entry, cpSystem);
		dbInstance.commit();
		
		Assert.assertNotNull(config);
		Assert.assertEquals(cpSystem, config.getCreditPointSystem());
		Assert.assertEquals(entry, config.getRepositoryEntry());
	}

	@Test
	public void loadConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("config-2");
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("config-coin-2", "CC2", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryCreditPointConfiguration config = repositoryEntryCreditPointConfigurationDao.createConfiguration(entry, cpSystem);
		dbInstance.commit();
		
		RepositoryEntryCreditPointConfiguration loadedConfig = repositoryEntryCreditPointConfigurationDao.loadConfiguration(entry);
		Assert.assertNotNull(loadedConfig);
		Assert.assertEquals(config, loadedConfig);
	}
	

}
