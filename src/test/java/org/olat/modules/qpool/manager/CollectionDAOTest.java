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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
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
 * Initial date: 22.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CollectionDAO collectionDao;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QItemQueriesDAO qItemQueriesDao;
	
	@Test
	public void createCollection() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Coll-Onwer-");
		QuestionItemCollection collection = collectionDao.createCollection("My first collection", id);
		dbInstance.commit();
		Assert.assertNotNull(collection);
	}
	
	@Test
	public void loadCollectionById() {
		//create an owner and its collection
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-" + UUID.randomUUID());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection", id);
		dbInstance.commitAndCloseSession();
		
		//load the collection
		QuestionItemCollection loadedColl = collectionDao.loadCollectionById(coll.getKey());
		Assert.assertNotNull(loadedColl);
		Assert.assertNotNull(loadedColl.getKey());
		Assert.assertNotNull(loadedColl.getCreationDate());
		Assert.assertNotNull(loadedColl.getLastModified());
		Assert.assertEquals(coll, loadedColl);
		Assert.assertEquals("NGC collection", loadedColl.getName());
		Assert.assertEquals(id, loadedColl.getOwner());
	}
	
	@Test
	public void addItemToCollectionById() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-2-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 2", id);
		QuestionItem item = questionDao.createAndPersist(null, "NGC 89", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		//add the item to the collection
		boolean added = collectionDao.addItemToCollection(item, singletonList(coll));
		dbInstance.commit();//check if it's alright
		Assert.assertTrue(added);
	}
	
	@Test
	public void getItemKeysOfCollection() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-4-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 4", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 99", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 101", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1, singletonList(coll));
		collectionDao.addItemToCollection(item2, singletonList(coll));
		dbInstance.commit();//check if it's alright
		
		//load the items of the collection
		List<Long> items = collectionDao.getItemKeysOfCollection(coll);
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(items.contains(item1.getKey()));
		Assert.assertTrue(items.contains(item2.getKey()));
	}
	
	@Test
	public void getCollections_myOhMy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-5-" + UUID.randomUUID().toString());
		QuestionItemCollection coll1 = collectionDao.createCollection("NGC collection part. 6", id);
		QuestionItemCollection coll2 = collectionDao.createCollection("NGC collection part. 7", id);
		dbInstance.commit();//check if it's alright
		
		//load the items of the collection
		List<QuestionItemCollection> items = collectionDao.getCollections(id);
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(items.contains(coll1));
		Assert.assertTrue(items.contains(coll2));
	}
	
	@Test
	public void countItemsOfCollection() {
		//create 2 collections with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-4-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 8", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 103", "IMS QTI 1.2", Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 104", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1, singletonList(coll));
		collectionDao.addItemToCollection(item2, singletonList(coll));
		dbInstance.commit();

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll);

		//check if it's alright
		int numOfItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(2, numOfItems);
		
		params.setFormat(QTI21Constants.QTI_21_FORMAT);
		int numOfItems_21 = qItemQueriesDao.countItems(params);
		Assert.assertEquals(1, numOfItems_21);
	}

	@Test
	public void removeFromCollection_paranoid() {
		//create 2 collections with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-4-" + UUID.randomUUID().toString());
		QuestionItemCollection coll1 = collectionDao.createCollection("NGC collection 8", id);
		QuestionItemCollection coll2 = collectionDao.createCollection("NGC collection 9", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 103", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 104", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1, singletonList(coll1));
		collectionDao.addItemToCollection(item1, singletonList(coll2));
		collectionDao.addItemToCollection(item2, singletonList(coll1));
		collectionDao.addItemToCollection(item2, singletonList(coll2));
		dbInstance.commit();
		
		SearchQuestionItemParams params1 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params1.setCollection(coll1);
		SearchQuestionItemParams params2 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params2.setCollection(coll2);
		
		//check if it's alright
		int numOfItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(2, numOfItems_1);
		int numOfItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(2, numOfItems_2);
		
		//remove
		collectionDao.removeItemFromCollection(Collections.<QuestionItemShort>singletonList(item1), coll2);
		dbInstance.commitAndCloseSession();
		
		//check if the item has been removed
		int numOfStayingItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(2, numOfStayingItems_1);
		int numOfStayingItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(1, numOfStayingItems_2);
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll2);
		List<QuestionItemView> items_2 = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertEquals(1, items_2.size());
		Assert.assertEquals(item2.getKey(), items_2.get(0).getKey());
	}
	
	@Test
	public void removeFromCollections() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Coll-Onwer-4-");
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 10", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 107", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 108", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1, singletonList(coll));
		collectionDao.addItemToCollection(item2, singletonList(coll));
		dbInstance.commit();
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(coll);
		
		//check if it's alright
		int numOfItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(2, numOfItems);
		
		//remove
		collectionDao.deleteItemFromCollections(Collections.<QuestionItemShort>singletonList(item1));
		dbInstance.commitAndCloseSession();
		
		//check if the item has been removed
		int numOfStayingItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(1, numOfStayingItems);
		List<QuestionItemView> items_2 = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertEquals(1, items_2.size());
		Assert.assertEquals(item2.getKey(), items_2.get(0).getKey());
	}
}
