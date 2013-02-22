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
import java.util.Locale;

import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionType;
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
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-2-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 2", id);
		QuestionItem item = questionDao.create("NGC 81", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, QuestionType.FIB);
		dbInstance.commitAndCloseSession();
		
		//add the item to the collection
		collectionDao.addItemToCollection(coll, item);
		dbInstance.commit();//check if it's alright
	}
	
	@Test
	public void getItemsOfCollection() {
		//create a collection with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Onwer-3-" + UUID.randomUUID().toString());
		QuestionItemCollection coll = collectionDao.createCollection("NGC collection 3", id);
		QuestionItem item1 = questionDao.create("NGC 82", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, QuestionType.FIB);
		QuestionItem item2 = questionDao.create("NGC 83", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, QuestionType.FIB);
		collectionDao.addItemToCollection(coll, item1);
		collectionDao.addItemToCollection(coll, item2);
		dbInstance.commit();//check if it's alright
		
		//load the items of the collection
		List<QuestionItem> items = collectionDao.getItemsOfCollection(coll, 0, -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(items.contains(item1));
		Assert.assertTrue(items.contains(item2));
	}
	
	
}
