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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Pool;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QItemQueriesDAO qItemQueriesDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void createPool() {
		Pool pool = poolDao.createPool(null, "NGC", false);
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertNotNull(pool.getCreationDate());
		Assert.assertEquals("NGC", pool.getName());
		Assert.assertFalse(pool.isPublicPool());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createPool_withOwner() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Pool-owner-" + UUID.randomUUID().toString());
		Pool pool = poolDao.createPool(id, "NGC owned", true);
		Assert.assertNotNull(pool);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getPoolsAndGetNumOfPools() {
		//create a pool
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolName, true);
		Assert.assertNotNull(pool);
		Assert.assertNotNull(pool.getKey());
		Assert.assertEquals(poolName, pool.getName());
		Assert.assertTrue(pool.isPublicPool());
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
			if(poolName.equals(retrievedPool.getName())) {
				foundIt = true;
			}
		}
		Assert.assertTrue(foundIt);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getPrivatePool() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Pool-owner-" + UUID.randomUUID().toString());
		Identity quidam = JunitTestHelper.createAndPersistIdentityAsUser("Pool-quidam-" + UUID.randomUUID().toString());
		Pool pool = poolDao.createPool(owner, "Private pool", false);
		dbInstance.commitAndCloseSession();
		
		//owner has a private pool and public pools
		List<Pool> ownerPoolList = poolDao.getPools(owner, 0, -1);
		Assert.assertTrue(ownerPoolList.contains(pool));
		
		//quidam has only public pools
		List<Pool> quidamPoolList = poolDao.getPools(quidam, 0, -1);
		Assert.assertFalse(quidamPoolList.contains(pool));
	}
	
	@Test
	public void isMemberOfPrivatePools_poolOnly() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Pool-owner-" + UUID.randomUUID().toString());
		Pool pool = poolDao.createPool(id, "NGC owned", false);
		Assert.assertNotNull(pool);
		dbInstance.commitAndCloseSession();
		
		boolean isMember = poolDao.isMemberOfPrivatePools(id);
		Assert.assertTrue(isMember);
	}
	
	@Test
	public void isMemberOfPrivatePools_groupOnly() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Group-owner-" + UUID.randomUUID().toString());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsUser("Group-participant-" + UUID.randomUUID().toString());
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(owner, "Shared-Item-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		questionDao.share(item, group.getResource());

		businessGroupRelationDao.addRole(participant, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(item);
		Assert.assertNotNull(group);

		//retrieve them
		boolean isOwnerMember = poolDao.isMemberOfPrivatePools(owner);
		Assert.assertTrue(isOwnerMember);
		boolean isParticipantMember = poolDao.isMemberOfPrivatePools(participant);
		Assert.assertTrue(isParticipantMember);
	}
	
	@Test
	public void isMemberOfPrivatePools_negativeTest() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Pool-owner-" + UUID.randomUUID().toString());
		Identity somebody = JunitTestHelper.createAndPersistIdentityAsUser("Somebody-" + UUID.randomUUID().toString());
		//pool
		Pool pool = poolDao.createPool(owner, "NGC owned", false);
		Assert.assertNotNull(pool);
		//group without item
		BusinessGroup group = businessGroupDao.createAndPersist(owner, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(somebody, group, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		boolean isMember = poolDao.isMemberOfPrivatePools(somebody);
		Assert.assertFalse(isMember);
	}
	
	@Test
	public void addItemToPool() {
		//create a pool
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolName, true);
		Assert.assertNotNull(pool);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		//get pools
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commit();
	}
	
	@Test
	public void removeItemFromPool() {
		//create a pool
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolName, true);
		Assert.assertNotNull(pool);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		//get pools
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commit();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(null, null, Locale.ENGLISH);
		params.setPoolKey(pool.getKey());
		
		//check
		int numOfItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(1, numOfItems);
		
		//remove
		poolDao.removeFromPool(Collections.<QuestionItemShort>singletonList(item), pool);
		dbInstance.commit();
		
		//check empty pool
		int numOfStayingItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(0, numOfStayingItems);
		
		//but item exists
		QuestionItem reloadedItem = questionItemDao.loadById(item.getKey());
		Assert.assertNotNull(reloadedItem);
	}
	
	@Test
	public void removeItemFromPool_paranoid() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Poolman-" + UUID.randomUUID().toString());
		//create a pool
		String name1 = "NGC-" + UUID.randomUUID().toString();
		Pool pool1 = poolDao.createPool(null, name1, true);
		String name2 = "NGC-" + UUID.randomUUID().toString();
		Pool pool2 = poolDao.createPool(null, name2, true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item1 = questionItemDao.createAndPersist(id, "Cluster of stars", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionItemDao.createAndPersist(id, "Nebula", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);

		poolDao.addItemToPool(item1, Collections.singletonList(pool1), false);
		poolDao.addItemToPool(item1, Collections.singletonList(pool2), false);
		poolDao.addItemToPool(item2, Collections.singletonList(pool1), false);
		poolDao.addItemToPool(item2, Collections.singletonList(pool2), false);
		dbInstance.commit();
		
		SearchQuestionItemParams params1 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params1.setPoolKey(pool1.getKey());
		SearchQuestionItemParams params2 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params2.setPoolKey(pool2.getKey());
		
		//check
		int numOfItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(2, numOfItems_1);
		int numOfItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(2, numOfItems_2);
		
		//remove
		poolDao.removeFromPool(Collections.<QuestionItemShort>singletonList(item1), pool2);
		dbInstance.commit();
		
		//check empty pool
		int numOfStayingItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(2, numOfStayingItems_1);
		int numOfStayingItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(1, numOfStayingItems_2);
		
		//check content
		List<QuestionItemView> items_1 = qItemQueriesDao.getItems(params1, 0, -1);
		Assert.assertEquals(2, items_1.size());
		List<QuestionItemView> items_2 = qItemQueriesDao.getItems(params2, 0, -1);
		Assert.assertEquals(1, items_2.size());
		Assert.assertEquals(item2.getKey(), items_2.get(0).getKey());
		
		//but item exists
		QuestionItem reloadedItem = questionItemDao.loadById(item1.getKey());
		Assert.assertNotNull(reloadedItem);
	}
	
	@Test
	public void getPoolInfos_byItem() {
		//create a pool
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool1 = poolDao.createPool(null, poolName, true);
		Pool pool2 = poolDao.createPool(null, poolName, true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		List<Pool> pools = new ArrayList<>(2);
		pools.add(pool1);
		pools.add(pool2);
		poolDao.addItemToPool(item, pools, false);
		dbInstance.commitAndCloseSession();

		//retrieve
		List<QuestionItem2Pool> infos = poolDao.getQuestionItem2Pool(item);
		Assert.assertNotNull(infos);
		Assert.assertEquals(2, infos.size());
	}
	
	@Test
	public void getPools_ofItem() {
		//create a pool
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool1 = poolDao.createPool(null, poolName, true);
		Pool pool2 = poolDao.createPool(null, poolName + "-b", true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(null, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		poolDao.addItemToPool(item, Collections.singletonList(pool1), false);
		poolDao.addItemToPool(item, Collections.singletonList(pool2), true);
		dbInstance.commitAndCloseSession();
		
		//retrieve the pools
		List<Pool> pools = poolDao.getPools(item);
		Assert.assertNotNull(pools);
		Assert.assertEquals(2, pools.size());
		Assert.assertTrue(pools.contains(pool1));
		Assert.assertTrue(pools.contains(pool2));
	}
	
	@Test
	public void removeItemFromPools() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Poolman-" + UUID.randomUUID().toString());
		//create a pool with an item
		String poolName = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolName, true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(id, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();
		

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setPoolKey(pool.getKey());
		
		//check the pool and remove the items
		List<QuestionItemView> items = qItemQueriesDao.getItems(params, 0 , -1);
		Assert.assertEquals(1, items.size());
		List<QuestionItemShort> toDelete = Collections.<QuestionItemShort>singletonList(items.get(0));
		int count = poolDao.removeFromPools(toDelete);
		Assert.assertEquals(1, count);
		dbInstance.commitAndCloseSession();
		
		//check if the pool is empty
		List<QuestionItemView> emptyItems = qItemQueriesDao.getItems(params, 0 , -1);
		Assert.assertTrue(emptyItems.isEmpty());
	}
}