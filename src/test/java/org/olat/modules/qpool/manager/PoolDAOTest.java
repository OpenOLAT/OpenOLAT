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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	@Test
	public void createPool() {
		Pool pool = poolDao.createPool("NGC");
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertNotNull(pool.getCreationDate());
		Assert.assertEquals("NGC", pool.getName());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getPoolsAndGetNumOfPools() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(name);
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertEquals(name, pool.getName());
		dbInstance.commitAndCloseSession();
		
		//get pools
		List<Pool> pools = poolDao.getPools();
		Assert.assertNotNull(pools);
		Assert.assertTrue(pools.size() >= 1);
		int numOfPools = poolDao.getNumOfPools();
		Assert.assertEquals(pools.size(), numOfPools);
		//retrieve our pool
		boolean foundIt = false;
		for(Pool retrievedPool:pools) {
			if(name.equals(retrievedPool.getName())) {
				foundIt = true;
			}
		}
		Assert.assertTrue(foundIt);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void addItemToPool() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(name);
		Assert.assertNotNull(pool);
		QuestionItem item = questionItemDao.create("Galaxy", QuestionType.MC);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		//get pools
		poolDao.addItemToPool(item, pool);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getItemsOfPool() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(name);
		QuestionItem item = questionItemDao.create("Galaxy", QuestionType.MC);
		poolDao.addItemToPool(item, pool);
		dbInstance.commitAndCloseSession();
		
		List<QuestionItem> items = poolDao.getItemsOfPool(pool, 0 , -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(items.contains(item));
		dbInstance.commitAndCloseSession();
	}
	
	

}
