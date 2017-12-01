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

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QItemQueriesDAOTest extends OlatTestCase  {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private CollectionDAO collectionDao;
	@Autowired
	private QItemQueriesDAO qItemQueriesDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	
	@Test
	public void getFavoritItems() {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item3 = questionDao.createAndPersist(id, "NGC 292", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1 + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2 + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		List<QuestionItemView> favorits = qItemQueriesDao.getFavoritItems(params, null, 0, -1);
		List<Long> favoritKeys = new ArrayList<>();
		for(QuestionItemView favorit:favorits) {
			favoritKeys.add(favorit.getKey());
		}
		Assert.assertNotNull(favorits);
		Assert.assertEquals(2, favorits.size());
		Assert.assertTrue(favoritKeys.contains(item1.getKey()));
		Assert.assertTrue(favoritKeys.contains(item2.getKey()));
		Assert.assertFalse(favoritKeys.contains(item3.getKey()));
		
		//limit to the first favorit
		List<QuestionItemView> limitedFavorits = qItemQueriesDao.getFavoritItems(params, Collections.singletonList(item1.getKey()), 0, -1);
		Assert.assertNotNull(limitedFavorits);
		Assert.assertEquals(1, limitedFavorits.size());
		Assert.assertEquals(item1.getKey(), limitedFavorits.get(0).getKey());
		
		//check the count
		int numOfFavorits = qItemQueriesDao.countFavoritItems(params);
		Assert.assertEquals(2, numOfFavorits);
	}
	
	@Test
	public void getFavoritItems_orderBy() {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1 + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2 + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getFavoritItems(params, null, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getFavoritItems(params, null, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getFavoriteItems_editable_author() {
		//create an author with an item and mark it as favorite
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem item = questionDao.createAndPersist(id, "FAV 4390", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		markManager.setMark(item, id, null, "[QuestionItem:" + item + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		List<QuestionItemView> favorites = qItemQueriesDao.getFavoritItems(params, null, 0, -1);
		QuestionItemView favoriteItem = favorites.get(0);
		
		Assert.assertTrue(favoriteItem.isEditable());
	}
	
	@Test
	public void getFavoriteItems_editable_pool() {
		//create a user who is not the author of the question
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		//create an author with an item
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity author = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem item = questionDao.createAndPersist(author, "FAV 4391", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		// Add the item as not editable to a public pool
		Pool poolNotEditable = poolDao.createPool(null, "Pool not editable", true);
		poolDao.addItemToPool(item, Collections.singletonList(poolNotEditable), false);
		dbInstance.commitAndCloseSession();
		
		//the user marks the question as a favorite and load his favorites
		markManager.setMark(item, user, null, "[QuestionItem:" + item + "]");
		dbInstance.commitAndCloseSession();
		SearchQuestionItemParams params = new SearchQuestionItemParams(user, null);
		List<QuestionItemView> favorites = qItemQueriesDao.getFavoritItems(params, null, 0, -1);
		QuestionItemView favoriteItem = favorites.get(0);
		Assert.assertFalse(favoriteItem.isEditable());
		
		//the author adds the item to an other pool as editable
		Pool poolEditable = poolDao.createPool(null, "Pool editable", true);
		poolDao.addItemToPool(item, Collections.singletonList(poolEditable), true);
		dbInstance.commitAndCloseSession();
		
		//the user reloads his favorites
		favorites = qItemQueriesDao.getFavoritItems(params, null, 0, -1);
		favoriteItem = favorites.get(0);
		Assert.assertTrue(favoriteItem.isEditable());
	}

	@Test
	public void getItemsOfCollection() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 97", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1, singletonList(coll));
		collectionDao.addItemToCollection(item2, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		//load the items of the collection
		List<QuestionItemView> items = qItemQueriesDao.getItemsOfCollection(id, coll, null, null, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		//count them
		int numOfItems = collectionDao.countItemsOfCollection(coll, null);
		Assert.assertEquals(2, numOfItems);
		
		//load limit sub set
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItemsOfCollection(id, coll, Collections.singletonList(item1.getKey()), null, 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsOfCollection_orderBy() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item = questionDao.createAndPersist(null, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItemsOfCollection(id, coll, null, null, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItemsOfCollection(id, coll, null, null, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getItemsByAuthor() {
		//create an author with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-2-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		params.setAuthor(id);
		
		//count the items of the author
		int numOfItems = questionDao.countItems(id);
		Assert.assertEquals(2, numOfItems);
		//retrieve the items of the author
		List<QuestionItemView> items = qItemQueriesDao.getItemsByAuthor(params, null, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		
		//check the count
		int count = qItemQueriesDao.countItemsByAuthor(params);
		Assert.assertEquals(2, count);
		
		//limit the list
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItemsByAuthor(params, Collections.singletonList(item1.getKey()), 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsByAuthor_orderBy() {
		//create an author with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-2-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(item1);
		Assert.assertNotNull(item2);
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		params.setAuthor(id);
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItemsByAuthor(params, null, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItemsByAuthor(params, null, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getItemsOfTaxonomy() {
		//create a taxonomy level with 2 items
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-10", "QPool Author", "", null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel("QPool-Biology", "QPool Biology", "Biology", null, null, null, null, taxonomy);
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QuestionItem item1 = questionDao.createAndPersist(id, "Bio 101", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItemImpl itemImpl1 = questionDao.loadById(item1.getKey());
		itemImpl1.setTaxonomyLevel(biology);
		itemImpl1.setQuestionStatus(QuestionStatus.draft);
		QuestionItem item2 = questionDao.createAndPersist(id, "Bio 102", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItemImpl itemImpl2 = questionDao.loadById(item2.getKey());
		itemImpl2.setTaxonomyLevel(biology);
		itemImpl2.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//create another item with the same taxonomy level but an other status
		QuestionItem itemOtherStatus = questionDao.createAndPersist(id, "Bio 104", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItemImpl itemImplOtherStatus = questionDao.loadById(itemOtherStatus.getKey());
		itemImplOtherStatus.setTaxonomyLevel(biology);
		itemImplOtherStatus.setQuestionStatus(QuestionStatus.revised);
		dbInstance.commitAndCloseSession();
		
		//create another taxonomy level with an item which should not be loaded later
		TaxonomyLevel geography = taxonomyLevelDao.createTaxonomyLevel("QPool-Geopraphy", "QPool Geopraphy", "Geopraphy", null, null, null, null, taxonomy);
		QuestionItem itemOtherTaxonomyLevel = questionDao.createAndPersist(id, "Bio 103", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItemImpl itemImpl3 = questionDao.loadById(itemOtherTaxonomyLevel.getKey());
		itemImpl3.setTaxonomyLevel(geography);
		dbInstance.commitAndCloseSession();

		//load the items of the taxonomy level
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		params.setTaxonomyLevelKey(biology.getKey());
		params.setQuestionStatus(QuestionStatus.draft);
		params.setAuthor(id);
		List<QuestionItemView> items = qItemQueriesDao.getItemsOfTaxonomyLevel(params, null, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		//count them
		int numOfItems = qItemQueriesDao.countItemsOfTaxonomy(params);
		Assert.assertEquals(2, numOfItems);
		
		//load limit sub set
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItemsOfTaxonomyLevel(params, Collections.singletonList(item1.getKey()), 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsOfPool() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Poolman-" + UUID.randomUUID().toString());
		//create a pool
		String poolTitle = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(id, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		params.setPoolKey(pool.getKey());
		
		//retrieve
		List<QuestionItemView> items = qItemQueriesDao.getItemsOfPool(params, null, 0 , -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(items.get(0).getKey().equals(item.getKey()));
		//count
		int numOfItems = poolDao.countItemsInPool(params);
		Assert.assertEquals(1, numOfItems);
	}

	@Test
	public void getItemsOfPool_orderBy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Poolman-" + UUID.randomUUID().toString());
		//create a pool
		String poolTitle = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionItemDao.createAndPersist(id, "Mega cluster of galaxies", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null);
		params.setPoolKey(pool.getKey());
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItemsOfPool(params, null, 0 , -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItemsOfPool(params, null, 0 , -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getSharedItemByResource() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-2-" + UUID.randomUUID());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdao-1", "gdao-desc", -1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdao-2", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		List<OLATResource> resources = new ArrayList<>();
		resources.add(group1.getResource());
		resources.add(group2.getResource());
		questionDao.share(item, resources, false);
		
		//retrieve them
		List<QuestionItemView> sharedItems1 = qItemQueriesDao.getSharedItemByResource(id, group1.getResource(), null, null, 0, -1);
		Assert.assertNotNull(sharedItems1);
		Assert.assertEquals(1, sharedItems1.size());
		Assert.assertEquals(item.getKey(), sharedItems1.get(0).getKey());
		List<QuestionItemView> sharedItems2 = qItemQueriesDao.getSharedItemByResource(id, group2.getResource(), null, null, 0, -1);
		Assert.assertNotNull(sharedItems2);
		Assert.assertEquals(1, sharedItems2.size());
		Assert.assertEquals(item.getKey(), sharedItems2.get(0).getKey());
	}
	
	@Test
	public void getSharedItemByResource_orderBy() {
		//create a group to share 1 item
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-2-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao-3", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		List<OLATResource> resources = new ArrayList<>();
		resources.add(group.getResource());
		questionDao.share(item, resources, false);
		dbInstance.commitAndCloseSession();
		
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao
					.getSharedItemByResource(id, group.getResource(), null, null, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao
					.getSharedItemByResource(id, group.getResource(), null, null, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getSharedItemByResource_subset() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-1-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(id, "Share-Item-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "Share-Item-2", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item1, group.getResource());
		questionDao.share(item2, group.getResource());
		
		//retrieve them
		List<QuestionItemView> sharedItems = qItemQueriesDao.getSharedItemByResource(id, group.getResource(), null, null, 0, -1);
		List<Long> sharedItemKeys = new ArrayList<>();
		for(QuestionItemView sharedItem:sharedItems) {
			sharedItemKeys.add(sharedItem.getKey());
		}
		Assert.assertNotNull(sharedItems);
		Assert.assertEquals(2, sharedItems.size());
		Assert.assertTrue(sharedItemKeys.contains(item1.getKey()));
		Assert.assertTrue(sharedItemKeys.contains(item2.getKey()));
		
		//retrieve limited sub set
		List<QuestionItemView> limitedSharedItems = qItemQueriesDao.getSharedItemByResource(id, group.getResource(), Collections.singletonList(item1.getKey()), null, 0, -1);
		Assert.assertNotNull(limitedSharedItems);
		Assert.assertEquals(1, limitedSharedItems.size());
		Assert.assertEquals(item1.getKey(), limitedSharedItems.get(0).getKey());
	}
}
