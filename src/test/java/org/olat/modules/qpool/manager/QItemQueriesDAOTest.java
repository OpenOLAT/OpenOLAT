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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
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
	
	private QItemType qItemType;
	
	@Before
	public void setUp() {
		qItemType = qItemTypeDao.loadByType(QuestionType.MC.name());
	}

	@Test
	public void getFavoritItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item3 = questionDao.createAndPersist(id, "NGC 292", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 55", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 253", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
	public void getFavoriteItems_isAuthor() {
		//create an author with an item
		Identity me = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem myItem = questionDao.createAndPersist(me, "FAV 4390", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		//create another author with an item 
		Identity other = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem othersItem = questionDao.createAndPersist(other, "FAV 4391", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		//the first author marks the items as favorites
		markManager.setMark(myItem, me, null, "[QuestionItem:" + myItem + "]");
		markManager.setMark(othersItem, me, null, "[QuestionItem:" + othersItem + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(me, null);
		List<QuestionItemView> myFavorites = qItemQueriesDao.getFavoritItems(params, Collections.singletonList(myItem.getKey()), 0, -1);;
		QuestionItemView myFavorite = myFavorites.get(0);
		Assert.assertTrue(myFavorite.isAuthor());
		
		List<QuestionItemView> otherFavorites = qItemQueriesDao.getFavoritItems(params, Collections.singletonList(othersItem.getKey()), 0, -1);;
		QuestionItemView otherFavorite = otherFavorites.get(0);
		Assert.assertFalse(otherFavorite.isAuthor());
	}
	
	@Test
	public void getFavoriteItems_isReviwer() {
		//create two taxonomy levels
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-isT", "QPool Teacher", "", null);
		TaxonomyLevel taxWithTeach = taxonomyLevelDao.createTaxonomyLevel("QPool-Teach", "QPool Teach", "Teach", null, null, null, null, taxonomy);
		TaxonomyLevel taxWithoutTeach = taxonomyLevelDao.createTaxonomyLevel("QPool-NoTeach", "QPool NoTeach", "NoTeach", null, null, null, null, taxonomy);
		//create an author with teach rights in one taxonomy level and one item in the two taxonomy levels
		Identity me = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.teach, taxWithTeach, me);
		QuestionItem myItemTax = questionDao.createAndPersist(me, "FAV 200", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl myItemTaxImpl = questionDao.loadById(myItemTax.getKey());
		myItemTaxImpl.setTaxonomyLevel(taxWithTeach);
		QuestionItem myItemNoTax = questionDao.createAndPersist(me, "FAV 201", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl myItemNoTaxImpl = questionDao.loadById(myItemNoTax.getKey());
		myItemNoTaxImpl.setTaxonomyLevel(taxWithoutTeach);
		
		//create another author with teach rights in one taxonomy level and one item in the two taxonomy levels
		Identity other = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem otherItemTax = questionDao.createAndPersist(other, "FAV 210", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl otherItemTaxImpl = questionDao.loadById(otherItemTax.getKey());
		otherItemTaxImpl.setTaxonomyLevel(taxWithTeach);
		QuestionItem otherItemNoTax = questionDao.createAndPersist(other, "FAV 211", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		myItemNoTaxImpl.setTaxonomyLevel(taxWithoutTeach);
		markManager.setMark(myItemTax, me, null, "[QuestionItem:" + myItemTax + "]");
		markManager.setMark(myItemNoTax, me, null, "[QuestionItem:" + myItemNoTax + "]");
		markManager.setMark(otherItemTax, me, null, "[QuestionItem:" + otherItemTax + "]");
		markManager.setMark(otherItemNoTax, me, null, "[QuestionItem:" + otherItemNoTax + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(me, null);
		List<QuestionItemView> myFavorites = qItemQueriesDao.getFavoritItems(params, Arrays.asList(otherItemTax.getKey()), 0, -1);;
		Assert.assertTrue(myFavorites.get(0).isReviewer());
		
		Collection<Long> notReviewer = Arrays.asList(myItemTax.getKey(), myItemNoTax.getKey(), otherItemNoTax.getKey());
		List<QuestionItemView> otherFavorites = qItemQueriesDao.getFavoritItems(params, notReviewer,  0, -1);;
		Assert.assertFalse(otherFavorites.get(0).isReviewer());
		Assert.assertFalse(otherFavorites.get(1).isReviewer());
		Assert.assertFalse(otherFavorites.get(2).isReviewer());
	}
	
	@Test
	public void getFavoriteItems_isManager() {
		//create two taxonomy levels
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-ism", "QPool Manager", "", null);
		TaxonomyLevel taxWithManage = taxonomyLevelDao.createTaxonomyLevel("QPool-Manage", "QPool Manage", "Manage", null, null, null, null, taxonomy);
		TaxonomyLevel taxWithoutManage = taxonomyLevelDao.createTaxonomyLevel("QPool-NoManage", "QPool NoManage", "NoManage", null, null, null, null, taxonomy);
		//create an author with teach rights in one taxonomy level and one item in the two taxonomy levels
		Identity me = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.manage, taxWithManage, me);
		QuestionItem myItemTax = questionDao.createAndPersist(me, "FAV 300", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl myItemTaxImpl = questionDao.loadById(myItemTax.getKey());
		myItemTaxImpl.setTaxonomyLevel(taxWithManage);
		QuestionItem myItemNoTax = questionDao.createAndPersist(me, "FAV 301", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl myItemNoTaxImpl = questionDao.loadById(myItemNoTax.getKey());
		myItemNoTaxImpl.setTaxonomyLevel(taxWithoutManage);
		
		//create another author with teach rights in one taxonomy level and one item in the two taxonomy levels
		Identity other = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem otherItemTax = questionDao.createAndPersist(other, "FAV 310", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl otherItemTaxImpl = questionDao.loadById(otherItemTax.getKey());
		otherItemTaxImpl.setTaxonomyLevel(taxWithManage);
		QuestionItem otherItemNoTax = questionDao.createAndPersist(other, "FAV 311", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		myItemNoTaxImpl.setTaxonomyLevel(taxWithoutManage);
		markManager.setMark(myItemTax, me, null, "[QuestionItem:" + myItemTax + "]");
		markManager.setMark(myItemNoTax, me, null, "[QuestionItem:" + myItemNoTax + "]");
		markManager.setMark(otherItemTax, me, null, "[QuestionItem:" + otherItemTax + "]");
		markManager.setMark(otherItemNoTax, me, null, "[QuestionItem:" + otherItemNoTax + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(me, null);
		Collection<Long> manager = Arrays.asList(myItemTax.getKey(), otherItemTax.getKey());
		List<QuestionItemView> myFavorites = qItemQueriesDao.getFavoritItems(params, manager, 0, -1);;
		Assert.assertTrue(myFavorites.get(0).isManager());
		Assert.assertTrue(myFavorites.get(1).isManager());
		
		Collection<Long> notManager = Arrays.asList(myItemNoTax.getKey(), otherItemNoTax.getKey());
		List<QuestionItemView> otherFavorites = qItemQueriesDao.getFavoritItems(params, notManager,  0, -1);;
		Assert.assertFalse(otherFavorites.get(0).isManager());
		Assert.assertFalse(otherFavorites.get(1).isManager());
	}
	
	@Test
	public void getFavoriteItems_isEditableByPool() {
		String poolTitle = "isEByPool-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		Identity me = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		Identity other = JunitTestHelper.createAndPersistIdentityAsUser("fav-auth-" + UUID.randomUUID().toString());
		QuestionItem itemInPoolEditable = questionItemDao.createAndPersist(other, "FAV 400", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		poolDao.addItemToPool(itemInPoolEditable, Collections.singletonList(pool), true);
		QuestionItem itemInPoolNotEditable = questionItemDao.createAndPersist(other, "FAV 401", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		poolDao.addItemToPool(itemInPoolNotEditable, Collections.singletonList(pool), false);
		QuestionItem itemNotInPool = questionItemDao.createAndPersist(other, "FAV 402", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		markManager.setMark(itemInPoolEditable, me, null, "[QuestionItem:" + itemInPoolEditable + "]");
		markManager.setMark(itemInPoolNotEditable, me, null, "[QuestionItem:" + itemInPoolNotEditable + "]");
		markManager.setMark(itemNotInPool, me, null, "[QuestionItem:" + itemNotInPool + "]");
		dbInstance.commitAndCloseSession();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(me, null);
		Collection<Long> editable = Arrays.asList(itemInPoolEditable.getKey());
		List<QuestionItemView> editableFavorites = qItemQueriesDao.getFavoritItems(params, editable, 0, -1);;
		Assert.assertTrue(editableFavorites.get(0).isEditableInPool());
		
		Collection<Long> notEditable = Arrays.asList(itemInPoolNotEditable.getKey(), itemNotInPool.getKey());
		List<QuestionItemView> notEditableFavorites = qItemQueriesDao.getFavoritItems(params, notEditable,  0, -1);;
		Assert.assertFalse(notEditableFavorites.get(0).isEditableInPool());
		Assert.assertFalse(notEditableFavorites.get(1).isEditableInPool());
	}

	@Test
	public void getItemsOfCollection() {
		//create a collection with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 97", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item = questionDao.createAndPersist(null, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-2-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-2-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 2171", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 2172", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
	public void getItemsOfTaxonomy_noAuthor() {
		//create a taxonomy level with 2 items
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-10", "QPool Author", "", null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel("QPool-Biology", "QPool Biology", "Biology", null, null, null, null, taxonomy);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id1, "Bio 101", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl1 = questionDao.loadById(item1.getKey());
		itemImpl1.setTaxonomyLevel(biology);
		itemImpl1.setQuestionStatus(QuestionStatus.draft);
		QuestionItem item2 = questionDao.createAndPersist(id1, "Bio 102", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl2 = questionDao.loadById(item2.getKey());
		itemImpl2.setTaxonomyLevel(biology);
		itemImpl2.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//create another item with the same taxonomy level but an other status
		QuestionItem itemOtherStatus = questionDao.createAndPersist(id1, "Bio 104", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImplOtherStatus = questionDao.loadById(itemOtherStatus.getKey());
		itemImplOtherStatus.setTaxonomyLevel(biology);
		itemImplOtherStatus.setQuestionStatus(QuestionStatus.revised);
		dbInstance.commitAndCloseSession();
		
		//create another taxonomy level with an item which should not be loaded later
		TaxonomyLevel geography = taxonomyLevelDao.createTaxonomyLevel("QPool-Geopraphy", "QPool Geopraphy", "Geopraphy", null, null, null, null, taxonomy);
		QuestionItem itemOtherTaxonomyLevel = questionDao.createAndPersist(id1, "Bio 103", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl3 = questionDao.loadById(itemOtherTaxonomyLevel.getKey());
		itemImpl3.setTaxonomyLevel(geography);
		dbInstance.commitAndCloseSession();

		//create another item with another author
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item5 = questionDao.createAndPersist(id2, "Bio 105", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl5 = questionDao.loadById(item5.getKey());
		itemImpl5.setTaxonomyLevel(biology);
		itemImpl5.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//load the items of the taxonomy level
		SearchQuestionItemParams params = new SearchQuestionItemParams(id1, null);
		params.setTaxonomyLevelKey(biology.getKey());
		params.setQuestionStatus(QuestionStatus.draft);
		List<QuestionItemView> items = qItemQueriesDao.getItemsOfTaxonomyLevel(params, null, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(3, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		Assert.assertTrue(itemKeys.contains(item5.getKey()));
		//count them
		int numOfItems = qItemQueriesDao.countItemsOfTaxonomy(params);
		Assert.assertEquals(3, numOfItems);
		
		//load limit sub set
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItemsOfTaxonomyLevel(params, Collections.singletonList(item1.getKey()), 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsOfTaxonomy_onlyAuthor() {
		//create a taxonomy level with 2 items
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-20", "QPool Author", "", null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel("QPool-Biology", "QPool Biology", "Biology", null, null, null, null, taxonomy);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id1, "Bio 201", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl1 = questionDao.loadById(item1.getKey());
		itemImpl1.setTaxonomyLevel(biology);
		itemImpl1.setQuestionStatus(QuestionStatus.draft);
		QuestionItem item2 = questionDao.createAndPersist(id1, "Bio 202", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl2 = questionDao.loadById(item2.getKey());
		itemImpl2.setTaxonomyLevel(biology);
		itemImpl2.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//create another item with the same taxonomy level but an other status
		QuestionItem itemOtherStatus = questionDao.createAndPersist(id1, "Bio 204", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImplOtherStatus = questionDao.loadById(itemOtherStatus.getKey());
		itemImplOtherStatus.setTaxonomyLevel(biology);
		itemImplOtherStatus.setQuestionStatus(QuestionStatus.revised);
		dbInstance.commitAndCloseSession();
		
		//create another taxonomy level with an item which should not be loaded later
		TaxonomyLevel geography = taxonomyLevelDao.createTaxonomyLevel("QPool-Geopraphy", "QPool Geopraphy", "Geopraphy", null, null, null, null, taxonomy);
		QuestionItem itemOtherTaxonomyLevel = questionDao.createAndPersist(id1, "Bio 203", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl3 = questionDao.loadById(itemOtherTaxonomyLevel.getKey());
		itemImpl3.setTaxonomyLevel(geography);
		dbInstance.commitAndCloseSession();

		//create another item with another author
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item5 = questionDao.createAndPersist(id2, "Bio 205", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl5 = questionDao.loadById(item5.getKey());
		itemImpl5.setTaxonomyLevel(biology);
		itemImpl5.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//load the items of the taxonomy level
		SearchQuestionItemParams params = new SearchQuestionItemParams(id1, null);
		params.setTaxonomyLevelKey(biology.getKey());
		params.setQuestionStatus(QuestionStatus.draft);
		params.setOnlyAuthor(id1);
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
	public void getItemsOfTaxonomy_excludeAuthor() {
		//create a taxonomy level with 2 items
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("QPool-30", "QPool Author", "", null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel("QPool-Biology", "QPool Biology", "Biology", null, null, null, null, taxonomy);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id1, "Bio 301", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl1 = questionDao.loadById(item1.getKey());
		itemImpl1.setTaxonomyLevel(biology);
		itemImpl1.setQuestionStatus(QuestionStatus.draft);
		QuestionItem item2 = questionDao.createAndPersist(id1, "Bio 302", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl2 = questionDao.loadById(item2.getKey());
		itemImpl2.setTaxonomyLevel(biology);
		itemImpl2.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//create another item with the same taxonomy level but an other status
		QuestionItem itemOtherStatus = questionDao.createAndPersist(id1, "Bio 304", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImplOtherStatus = questionDao.loadById(itemOtherStatus.getKey());
		itemImplOtherStatus.setTaxonomyLevel(biology);
		itemImplOtherStatus.setQuestionStatus(QuestionStatus.revised);
		dbInstance.commitAndCloseSession();
		
		//create another taxonomy level with an item which should not be loaded later
		TaxonomyLevel geography = taxonomyLevelDao.createTaxonomyLevel("QPool-Geopraphy", "QPool Geopraphy", "Geopraphy", null, null, null, null, taxonomy);
		QuestionItem itemOtherTaxonomyLevel = questionDao.createAndPersist(id1, "Bio 303", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl3 = questionDao.loadById(itemOtherTaxonomyLevel.getKey());
		itemImpl3.setTaxonomyLevel(geography);
		dbInstance.commitAndCloseSession();

		//create another item with another author
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("Tax-" + UUID.randomUUID().toString());
		QuestionItem item5 = questionDao.createAndPersist(id2, "Bio 305", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, qItemType);
		QuestionItemImpl itemImpl5 = questionDao.loadById(item5.getKey());
		itemImpl5.setTaxonomyLevel(biology);
		itemImpl5.setQuestionStatus(QuestionStatus.draft);
		dbInstance.commitAndCloseSession();
		
		//load the items of the taxonomy level
		SearchQuestionItemParams params = new SearchQuestionItemParams(id1, null);
		params.setTaxonomyLevelKey(biology.getKey());
		params.setQuestionStatus(QuestionStatus.draft);
		params.setExcludeAuthor(id1);
		List<QuestionItemView> items = qItemQueriesDao.getItemsOfTaxonomyLevel(params, null, 0, -1);
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(itemKeys.contains(item5.getKey()));
		//count them
		int numOfItems = qItemQueriesDao.countItemsOfTaxonomy(params);
		Assert.assertEquals(1, numOfItems);
		
		//load limit sub set
		List<QuestionItemView> limitedItems = qItemQueriesDao.getItemsOfTaxonomyLevel(params, Collections.singletonList(item5.getKey()), 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item5.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemsOfPool() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Poolman-" + UUID.randomUUID().toString());
		//create a pool
		String poolTitle = "NGC-" + UUID.randomUUID().toString();
		Pool pool = poolDao.createPool(null, poolTitle, true);
		QuestionItem item = questionItemDao.createAndPersist(id, "Galaxy", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		QuestionItem item = questionItemDao.createAndPersist(id, "Mega cluster of galaxies", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-2-" + UUID.randomUUID());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdao-1", "gdao-desc", -1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdao-2", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-2-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao-3", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-3", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-1-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(id, "Share-Item-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
		QuestionItem item2 = questionDao.createAndPersist(id, "Share-Item-2", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, qItemType);
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
