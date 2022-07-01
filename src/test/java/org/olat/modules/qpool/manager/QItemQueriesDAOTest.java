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
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.qpool.ui.QuestionItemDataModel;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
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
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	@Autowired
	private CommentAndRatingService commentAndRatingService;
	@Autowired
	private QPoolService qpoolService;
	
	private QItemType qItemType;
	
	@Before
	public void setUp() {
		List<QuestionItemFull> items = qpoolService.getAllItems(0, Integer.MAX_VALUE);
		qpoolService.deleteItems(items);

		qItemType = qItemTypeDao.loadByType(QuestionType.MC.name());
	}

	@Test
	public void getFavoritItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item3 = questionDao.createAndPersist(id, "NGC 292", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1 + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2 + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setFavoritOnly(true);
		List<QuestionItemView> favorits = qItemQueriesDao.getItems(params, 0, -1);
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
		params.setItemKeys(Collections.singletonList(item1.getKey()));
		List<QuestionItemView> limitedFavorits = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(limitedFavorits);
		Assert.assertEquals(1, limitedFavorits.size());
		Assert.assertEquals(item1.getKey(), limitedFavorits.get(0).getKey());
		
		//check the count
		SearchQuestionItemParams countSearchParams = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		countSearchParams.setFavoritOnly(true);
		int numOfFavorits = qItemQueriesDao.countItems(countSearchParams);
		Assert.assertEquals(2, numOfFavorits);
	}
	
	@Test
	public void getFavoritItems_orderBy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1 + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2 + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setFavoritOnly(true);
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getItemsOfCollection() {
		//create a collection with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 92", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 97", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		collectionDao.addItemToCollection(item1, singletonList(coll));
		collectionDao.addItemToCollection(item2, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll);
		
		//load the items of the collection
		List<QuestionItemView> items = qItemQueriesDao.getItems(params, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		//count them
		int numOfItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(2, numOfItems);
		
		//load limit sub set
		params.setItemKeys(Collections.singletonList(item1.getKey()));
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsOfCollection_orderBy() {
		//create a collection with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item = questionDao.createAndPersist(null, "NGC 92", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		collectionDao.addItemToCollection(item, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll);
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	/**
	 * Check if all the queries works with the order by used
	 * by the main table in question pool.
	 */
	@Test
	public void getItemsOfCollection_orderByColumns() {
		//create a collection with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3c-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3c", id);
		QuestionItem item = questionDao.createAndPersist(null, "NGC 92", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		collectionDao.addItemToCollection(item, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll);
		
		//test order by
		for(QuestionItemDataModel.Cols order: QuestionItemDataModel.Cols.values()) {
			if(order.sortable()) {
				SortKey sortAsc = new SortKey(order.name(), true);
				List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
				Assert.assertNotNull(ascOrderedItems);
				
				SortKey sortDesc = new SortKey(order.name(), false);
				List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortDesc);
				Assert.assertNotNull(descOrderedItems);
			}
		}
	}
	
	@Test
	public void getItemsByAuthor() {
		//create an author with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("QOwn-2-");
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setAuthor(id);
		
		//count the items of the author
		int numOfItems = questionDao.countItems(id);
		Assert.assertEquals(2, numOfItems);
		//retrieve the items of the author
		List<QuestionItemView> items = qItemQueriesDao.getItems(params, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		
		//check the count
		int count = qItemQueriesDao.countItems(params);
		Assert.assertEquals(2, count);
		
		//limit the list
		params.setItemKeys(Collections.singletonList(item1.getKey()));
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsByAuthor_orderBy() {
		//create an author with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("QOwn-2-");
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(item1);
		Assert.assertNotNull(item2);
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setAuthor(id);
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getItemsByOwner() {
		//create an author with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("QOwner-and-only-2-");
		QuestionItem item = questionDao.createAndPersist(id, "NGC 7837", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setOwner("Owner-and-only");
		
		SortKey sortAsc = new SortKey(QuestionItemView.OrderBy.title.name(), true);
		List<QuestionItemView> views = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
		Assert.assertNotNull(views);
		Assert.assertFalse(views.isEmpty());	
		boolean match = views.stream().anyMatch(view -> item.getKey().equals(view.getKey()));
		Assert.assertTrue(match);
	}
	
	@Test
	public void getItemsByOwner_negativeTest() {
		//create an author with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("QOwn-29-");
		QuestionItem item = questionDao.createAndPersist(id, "NGC 7838", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(item);

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setOwner(UUID.randomUUID().toString());
		
		SortKey sortAsc = new SortKey(QuestionItemView.OrderBy.title.name(), true);
		List<QuestionItemView> views = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
		Assert.assertNotNull(views);
		Assert.assertTrue(views.isEmpty());
	}
	
	@Test
	public void getItemsOfPool() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Poolman-");
		//create a pool
		String poolTitle = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		QuestionItem item = questionItemDao.createAndPersist(id, "Galaxy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setPoolKey(pool.getKey());
		
		//retrieve
		List<QuestionItemView> items = qItemQueriesDao.getItems(params, 0 , -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertEquals(item.getKey(), items.get(0).getKey());
		//count
		int numOfItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(1, numOfItems);
	}

	@Test
	public void getItemsOfPool_orderBy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Poolman-");
		//create a pool
		String poolTitle = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		QuestionItem item = questionItemDao.createAndPersist(id, "Mega cluster of galaxies", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		poolDao.addItemToPool(item, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setPoolKey(pool.getKey());
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0 , -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0 , -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getSharedItemByResource() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("QShare-2-");
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdao-1", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdao-2", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commit();
		
		//share them
		List<OLATResource> resources = new ArrayList<>();
		resources.add(group1.getResource());
		resources.add(group2.getResource());
		questionDao.share(item, resources, false);
		
		//retrieve them
		SearchQuestionItemParams params1 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params1.setResource(group1.getResource());
		List<QuestionItemView> sharedItems1 = qItemQueriesDao.getItems(params1, 0, -1);
		Assert.assertNotNull(sharedItems1);
		Assert.assertEquals(1, sharedItems1.size());
		Assert.assertEquals(item.getKey(), sharedItems1.get(0).getKey());

		SearchQuestionItemParams params2 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params2.setResource(group2.getResource());
		List<QuestionItemView> sharedItems2 = qItemQueriesDao.getItems(params2, 0, -1);
		Assert.assertNotNull(sharedItems2);
		Assert.assertEquals(1, sharedItems2.size());
		Assert.assertEquals(item.getKey(), sharedItems2.get(0).getKey());
	}
	
	@Test
	public void getSharedItemByResource_orderBy() {
		//create a group to share 1 item
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-2-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao-3", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commit();
		
		//share them
		List<OLATResource> resources = new ArrayList<>();
		resources.add(group.getResource());
		questionDao.share(item, resources, false);
		dbInstance.commitAndCloseSession();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		
		//test order by
		for(QuestionItemView.OrderBy order: QuestionItemView.OrderBy.values()) {
			SortKey sortAsc = new SortKey(order.name(), true);
			List<QuestionItemView> ascOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortAsc);
			Assert.assertNotNull(ascOrderedItems);
			
			SortKey sortDesc = new SortKey(order.name(), false);
			List<QuestionItemView> descOrderedItems = qItemQueriesDao.getItems(params, 0, -1, sortDesc);
			Assert.assertNotNull(descOrderedItems);
		}
	}
	
	@Test
	public void getSharedItemByResource_subset() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-1-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(id, "Share-Item-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "Share-Item-2", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item1, group.getResource());
		questionDao.share(item2, group.getResource());
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		
		//retrieve them
		List<QuestionItemView> sharedItems = qItemQueriesDao.getItems(params, 0, -1);
		List<Long> sharedItemKeys = new ArrayList<>();
		for(QuestionItemView sharedItem:sharedItems) {
			sharedItemKeys.add(sharedItem.getKey());
		}
		Assert.assertNotNull(sharedItems);
		Assert.assertEquals(2, sharedItems.size());
		Assert.assertTrue(sharedItemKeys.contains(item1.getKey()));
		Assert.assertTrue(sharedItemKeys.contains(item2.getKey()));
		
		//retrieve limited sub set
		params.setItemKeys(Collections.singletonList(item1.getKey()));
		List<QuestionItemView> limitedSharedItems = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(limitedSharedItems);
		Assert.assertEquals(1, limitedSharedItems.size());
		Assert.assertEquals(item1.getKey(), limitedSharedItems.get(0).getKey());
	}
	
	@Test
	public void shouldCountAllItemsWithFilter() {
		Identity owner1 = createRandomIdentity();
		questionDao.createAndPersist(owner1, "QPool 1", "IMS QTI 1.2", Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		questionDao.createAndPersist(owner1, "QPool 2", "IMS QTI 1.2", Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		createRandomItem(owner1);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setFormat("IMS QTI 1.2");
		int countItems = qItemQueriesDao.countItems(params);
		
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetAllItems() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item13 = createRandomItem(owner1);
		Identity owner2 = createRandomIdentity();
		QuestionItem item21 = createRandomItem(owner2);
		QuestionItem item22 = createRandomItem(owner2);
		QuestionItem item23 = createRandomItem(owner2);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(6);
		assertThat(keysOf(loadedItems))
			.containsExactlyInAnyOrderElementsOf(keysOf(item11, item12, item13, item21, item22, item23));
	}
	
	@Test
	public void shouldGetLimitedItems() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item13 = createRandomItem(owner1);
		Identity owner2 = createRandomIdentity();
		QuestionItem item21 = createRandomItem(owner2);
		QuestionItem item22 = createRandomItem(owner2);
		QuestionItem item23 = createRandomItem(owner2);
		dbInstance.commitAndCloseSession();
		
		List<Long> inKeys = Arrays.asList(item12.getKey(), item21.getKey());
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setItemKeys(inKeys);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(inKeys.size());
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(inKeys)
				.doesNotContainAnyElementsOf(keysOf(item11, item13, item22, item23));
	}
	
	@Test
	public void shouldGetAllItemsByFormat() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = questionDao.createAndPersist(owner1, "QPool 1", "IMS QTI 1.2", Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item12 = questionDao.createAndPersist(owner1, "QPool 2", "IMS QTI 1.2", Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item13 = createRandomItem(owner1);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setFormat("IMS QTI 1.2");
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrder(item11.getKey(), item12.getKey())
				.doesNotContainAnyElementsOf(keysOf(item13));
	}
	
	@Test
	public void shouldGetItemsIsAuthor() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		Identity owner2 = createRandomIdentity();
		QuestionItem item21 = createRandomItem(owner2);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(owner1, null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).isAuthor()).isTrue();
		assertThat(filterByKey(loadedItems, item21).isAuthor()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsTeacher() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool", "QPool", "", null);
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		TaxonomyLevel taxonomySubLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, taxonomyLevel, null, taxonomy);
		Identity ownerAndTeacher = createRandomIdentity();
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.teach, taxonomyLevel, ownerAndTeacher, null);
		Identity teacher = createRandomIdentity();
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.teach, taxonomyLevel, teacher, null);
		Identity noTeacher = createRandomIdentity();
		QuestionItemImpl item11 = createRandomItem(ownerAndTeacher);
		item11.setTaxonomyLevel(taxonomyLevel);
		QuestionItemImpl item12 = createRandomItem(ownerAndTeacher);
		item12.setTaxonomyLevel(taxonomySubLevel);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(ownerAndTeacher, null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isTeacher()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isTeacher()).isTrue();
		
		params = new SearchQuestionItemParams(ownerAndTeacher, null, Locale.ENGLISH);
		loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isTeacher()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isTeacher()).isTrue();
		
		params = new SearchQuestionItemParams(noTeacher, null, Locale.ENGLISH);
		loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isTeacher()).isFalse();
		assertThat(filterByKey(loadedItems, item12).isTeacher()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsManager() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool", "QPool", "", null);
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		TaxonomyLevel taxonomySubLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, taxonomyLevel, null, taxonomy);
		Identity ownerAndManager = createRandomIdentity();
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.manage, taxonomyLevel, ownerAndManager, null);
		Identity manager = createRandomIdentity();
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.manage, taxonomyLevel, manager, null);
		Identity noManager = createRandomIdentity();
		QuestionItemImpl item11 = createRandomItem(ownerAndManager);
		item11.setTaxonomyLevel(taxonomyLevel);
		QuestionItemImpl item12 = createRandomItem(ownerAndManager);
		item12.setTaxonomyLevel(taxonomySubLevel);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(ownerAndManager, null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isManager()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isManager()).isTrue();
		
		params = new SearchQuestionItemParams(ownerAndManager, null, Locale.ENGLISH);
		loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isManager()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isManager()).isTrue();
		
		params = new SearchQuestionItemParams(noManager, null, Locale.ENGLISH);
		loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		assertThat(filterByKey(loadedItems, item11).isManager()).isFalse();
		assertThat(filterByKey(loadedItems, item12).isManager()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsRater() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		commentAndRatingService.createRating(owner1, item11, null, 2);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(owner1, null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).isRater()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isRater()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsEditableInAPool() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item13 = createRandomItem(owner1);
		Pool pool = poolDao.createPool(null, "Pool", true);
		poolDao.addItemToPool(item11, Collections.singletonList(pool), true);
		poolDao.addItemToPool(item12, Collections.singletonList(pool), false);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).isEditableInPool()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isEditableInPool()).isFalse();
		assertThat(filterByKey(loadedItems, item13).isEditableInPool()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsEditableInAShare() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item13 = createRandomItem(owner1);
		BusinessGroup group = businessGroupDao.createAndPersist(owner1, "QPool", "QPool", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		List<OLATResource> groupResources = Arrays.asList(group.getResource());
		questionDao.share(item11, groupResources, true);
		questionDao.share(item12, groupResources, false);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).isEditableInShare()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isEditableInShare()).isFalse();
		assertThat(filterByKey(loadedItems, item13).isEditableInShare()).isFalse();
	}
	
	@Test
	public void shouldGetItemsIsMarked() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		markManager.setMark(item11, owner1, null, "[QuestionItem:" + item11 + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(owner1, null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).isMarked()).isTrue();
		assertThat(filterByKey(loadedItems, item12).isMarked()).isFalse();
	}
	
	@Test
	public void shouldGetItemsRating() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 2);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 3);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 4);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).getRating()).isEqualTo(3);
	}
	
	@Test
	public void shouldGetItemsNumberOfRating() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 2);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 3);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 4);
		commentAndRatingService.createRating(createRandomIdentity(), item11, null, 4);
		QuestionItem item12 = createRandomItem(owner1);
		commentAndRatingService.createRating(createRandomIdentity(), item12, null, 4);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(filterByKey(loadedItems, item11).getNumberOfRatings()).isEqualTo(4);
	}
	
	@Test
	public void shouldGetItemsFilteredByLikeTaxonomyLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool", "QPool", "", null);
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		TaxonomyLevel taxonomySubLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, taxonomyLevel, null, taxonomy);
		TaxonomyLevel otherTaxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		QuestionItemImpl item11 = createRandomItem(createRandomIdentity());
		item11.setTaxonomyLevel(taxonomyLevel);
		QuestionItemImpl item12 = createRandomItem(createRandomIdentity());
		item12.setTaxonomyLevel(taxonomySubLevel);
		QuestionItemImpl item21 = createRandomItem(createRandomIdentity());
		item21.setTaxonomyLevel(otherTaxonomyLevel);
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setLikeTaxonomyLevel(taxonomyLevel);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item11, item12))
				.doesNotContainAnyElementsOf(keysOf(item21, item22, item23));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetItemsFilteredByQuestionStatus() {
		QuestionStatus status = QuestionStatus.revised;
		QuestionItemImpl item11 = createRandomItem(createRandomIdentity());
		item11.setQuestionStatus(status);
		QuestionItemImpl item12 = createRandomItem(createRandomIdentity());
		item12.setQuestionStatus(status);
		QuestionItem item21 = createRandomItem(createRandomIdentity());
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setQuestionStatus(status);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item11, item12))
				.doesNotContainAnyElementsOf(keysOf(item21, item22, item23));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetItemsFilteredByWithoutTaxonomyLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool", "QPool", "", null);
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		TaxonomyLevel taxonomySubLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, taxonomyLevel, null, taxonomy);
		TaxonomyLevel otherTaxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QPool", random(), "QPool", "QPool", null, null, null, null, taxonomy);
		QuestionItemImpl item11 = createRandomItem(createRandomIdentity());
		item11.setTaxonomyLevel(taxonomyLevel);
		QuestionItemImpl item12 = createRandomItem(createRandomIdentity());
		item12.setTaxonomyLevel(taxonomySubLevel);
		QuestionItemImpl item21 = createRandomItem(createRandomIdentity());
		item21.setTaxonomyLevel(otherTaxonomyLevel);
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setWithoutTaxonomyLevelOnly(true);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item22, item23))
				.doesNotContainAnyElementsOf(keysOf(item11, item12, item21));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetItemsFilteredByWithoutAuthor() {
		QuestionItem item11 = createRandomItem(null);
		QuestionItem item12 = createRandomItem(createRandomIdentity());
		QuestionItem item21 = createRandomItem(null);
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setWithoutAuthorOnly(true);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item11, item21))
				.doesNotContainAnyElementsOf(keysOf(item12, item22, item23));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetItemsFilteredByOnlyAuthor() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item21 = createRandomItem(createRandomIdentity());
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setOnlyAuthor(owner1);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item11, item12))
				.doesNotContainAnyElementsOf(keysOf(item21, item22, item23));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}
	
	@Test
	public void shouldGetItemsFilteredByExcludAuthor() {
		Identity owner1 = createRandomIdentity();
		QuestionItem item11 = createRandomItem(owner1);
		QuestionItem item12 = createRandomItem(owner1);
		QuestionItem item21 = createRandomItem(createRandomIdentity());
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setExcludeAuthor(owner1);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(3);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item21, item22, item23))
				.doesNotContainAnyElementsOf(keysOf(item11, item12));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(3);
	}
	
	@Test
	public void shouldGetItemsFilteredByExcludeRater() {
		QuestionItem item11 = createRandomItem(createRandomIdentity());
		QuestionItem item12 = createRandomItem(createRandomIdentity());
		QuestionItem item21 = createRandomItem(createRandomIdentity());
		QuestionItem item22 = createRandomItem(createRandomIdentity());
		QuestionItem item23 = createRandomItem(createRandomIdentity());
		Identity rater1 = createRandomIdentity();
		commentAndRatingService.createRating(createRandomIdentity(), item21, null, 2);
		commentAndRatingService.createRating(rater1, item21, null, 2);
		commentAndRatingService.createRating(rater1, item22, null, 2);
		commentAndRatingService.createRating(rater1, item23, null, 2);
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(createRandomIdentity(), null, Locale.ENGLISH);
		params.setExcludeRated(rater1);
		List<QuestionItemView> loadedItems = qItemQueriesDao.getItems(params, 0, -1);
		
		assertThat(loadedItems).hasSize(2);
		assertThat(keysOf(loadedItems))
				.containsExactlyInAnyOrderElementsOf(keysOf(item11, item12))
				.doesNotContainAnyElementsOf(keysOf(item21, item22, item23));
		
		int countItems = qItemQueriesDao.countItems(params);
		assertThat(countItems).isEqualTo(2);
	}

	private Identity createRandomIdentity() {
		return JunitTestHelper.createAndPersistIdentityAsRndUser("qitems");
	}

	private QuestionItemImpl createRandomItem(Identity owner) {
		String title = UUID.randomUUID().toString();
		String format = QTI21Constants.QTI_21_FORMAT;
		String language = Locale.ENGLISH.getLanguage();
		TaxonomyLevel taxonLevel = null;
		String dir = null;
		String rootFilename = null;
		return questionItemDao.createAndPersist(owner, title, format, language, taxonLevel, dir, rootFilename, qItemType);
	}
	
	private QuestionItemView filterByKey(List<QuestionItemView> items, QuestionItem filter) {
		return items.stream()
				.filter(item -> item.getKey().equals(filter.getKey()))
				.findFirst()
				.get();
	}
	
	private Collection<Long> keysOf(List<QuestionItemView> items) {
		return items.stream()
				.map(QuestionItemView::getKey)
				.collect(Collectors.toList());
	}
	
	private Collection<Long> keysOf(QuestionItemShort... items) {
		return Arrays.stream(items)
				.map(QuestionItemShort::getKey)
				.collect(Collectors.toList());
	}
}
