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

import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QItemType;
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
	
	@Test
	public void createCollection() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-" + UUID.randomUUID().toString());
		collectionDao.createCollection("My first collection", id);
		dbInstance.commit();
	}
	
	@Test
	public void loadCollectionById() {
		//create an owner and its collection
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-" + UUID.randomUUID().toString());
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
		QuestionItem item = questionDao.createAndPersist(null, "NGC 89", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		//add the item to the collection
		collectionDao.addItemToCollection(item.getKey(), coll);
		dbInstance.commit();//check if it's alright
	}
	
	@Test
	public void getItemsOfCollection() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 97", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1.getKey(), coll);
		collectionDao.addItemToCollection(item2.getKey(), coll);
		dbInstance.commit();//check if it's alright
		
		//load the items of the collection
		List<QuestionItemView> items = collectionDao.getItemsOfCollection(coll, null, 0, -1);
		List<Long> itemKeys = new ArrayList<Long>();
		for(QuestionItemView item:items) {
			itemKeys.add(item.getKey());
		}
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
		//count them
		int numOfItems = collectionDao.countItemsOfCollection(coll);
		Assert.assertEquals(2, numOfItems);
		
		//load limit sub set
		List<QuestionItemView> limitedItems = collectionDao.getItemsOfCollection(coll, Collections.singletonList(item1.getKey()), 0, -1);
		Assert.assertNotNull(limitedItems);
		Assert.assertEquals(1, limitedItems.size());
		Assert.assertEquals(item1.getKey(), limitedItems.get(0).getKey());
	}
	
	@Test
	public void getItemKeysOfCollection() {
		//create a collection with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-4-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 4", id);
		QuestionItem item1 = questionDao.createAndPersist(null, "NGC 99", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "NGC 101", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		collectionDao.addItemToCollection(item1.getKey(), coll);
		collectionDao.addItemToCollection(item2.getKey(), coll);
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
}
