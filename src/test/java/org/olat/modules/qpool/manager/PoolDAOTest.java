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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionType;
import org.olat.test.JunitTestHelper;
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
		Pool pool = poolDao.createPool(null, "NGC");
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertNotNull(pool.getCreationDate());
		Assert.assertEquals("NGC", pool.getName());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createPool_withOwner() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Pool-owner-" + UUID.randomUUID().toString());
		Pool pool = poolDao.createPool(id, "NGC owned");
		Assert.assertNotNull(pool);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getPoolsAndGetNumOfPools() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, name);
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertEquals(name, pool.getName());
		dbInstance.commitAndCloseSession();
		
		//get pools
		List<Pool> pools = poolDao.getPools(0, -1);
		Assert.assertNotNull(pools);
		Assert.assertTrue(pools.size() >= 1);
		//count
		int numOfPools = poolDao.countPools();
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
		Pool pool = poolDao.createPool(null, name);
		Assert.assertNotNull(pool);
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, QuestionType.MC);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		//get pools
		poolDao.addItemToPool(item, pool);
		dbInstance.commit();
	}
	
	@Test
	public void getItemsOfPool() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, name);
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, QuestionType.MC);
		poolDao.addItemToPool(item, pool);
		dbInstance.commitAndCloseSession();
		
		//retrieve
		List<QuestionItemShort> items = poolDao.getItemsOfPool(pool, null, 0 , -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(items.contains(item));
		//count
		int numOfItems = poolDao.getNumOfItemsInPool(pool);
		Assert.assertEquals(1, numOfItems);
	}
	
	@Test
	public void getPools_ofItem() {
		//create a pool
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool1 = poolDao.createPool(null, name);
		Pool pool2 = poolDao.createPool(null, name + "-b");
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, QuestionType.MC);
		poolDao.addItemToPool(item, pool1);
		poolDao.addItemToPool(item, pool2);
		dbInstance.commitAndCloseSession();
		
		//retrieve the pools
		List<Pool> pools = poolDao.getPools(item);
		Assert.assertNotNull(pools);
		Assert.assertEquals(2, pools.size());
		Assert.assertTrue(pools.contains(pool1));
		Assert.assertTrue(pools.contains(pool2));
	}
	
	@Test
	public void removeItemFromPool() {
		//create a pool with an item
		String name = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, name);
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, QuestionType.MC);
		poolDao.addItemToPool(item, pool);
		dbInstance.commitAndCloseSession();
		
		//check the pool and remove the items
		List<QuestionItemShort> items = poolDao.getItemsOfPool(pool, null, 0 , -1);
		Assert.assertEquals(1, items.size());
		List<QuestionItemShort> toDelete = Collections.singletonList(items.get(0));
		int count = poolDao.deleteFromPools(toDelete);
		Assert.assertEquals(1, count);
		dbInstance.commitAndCloseSession();
		
		//check if the pool is empty
		List<QuestionItemShort> emptyItems = poolDao.getItemsOfPool(pool, null, 0 , -1);
		Assert.assertTrue(emptyItems.isEmpty());
	}
	
	

}
