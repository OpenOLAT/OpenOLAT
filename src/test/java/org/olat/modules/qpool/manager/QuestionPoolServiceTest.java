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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionPoolService;
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
public class QuestionPoolServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QuestionPoolService qpoolService;
	
	@Test
	public void deleteItems() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-rm-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdrm", "gdrm-desc", -1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.create(id, "Share-item-rm-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		QuestionItem item2 = questionDao.create(id, "Share-item-rm-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		dbInstance.commit();
		//share them
		questionDao.share(item1, group.getResource());
		dbInstance.commitAndCloseSession();

		//delete the items
		List<QuestionItem> shared = new ArrayList<QuestionItem>();
		shared.add(item1);
		shared.add(item2);
		qpoolService.deleteItems(shared);
		dbInstance.commit();//make sure that changes are committed
		
		//check if they exists
		QuestionItem deletedItem1 = questionDao.loadById(item1.getKey());
		Assert.assertNull(deletedItem1);
		QuestionItem deletedItem2 = questionDao.loadById(item2.getKey());
		Assert.assertNull(deletedItem2);
	}
	
	@Test
	public void createCollection() {
		//create an user with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Owner-3-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.create(id, "NGC 92", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, QuestionType.FIB);
		QuestionItem item2 = questionDao.create(id, "NGC 97", QTIConstants.QTI_12_FORMAT, Locale.GERMAN.getLanguage(), null, null, QuestionType.FIB);
		dbInstance.commit();
		
		//load the items of the collection
		List<QuestionItem> items = new ArrayList<QuestionItem>();
		items.add(item1);
		items.add(item2);
		QuestionItemCollection newColl = qpoolService.createCollection(id, "My private collection", items);
		Assert.assertNotNull(newColl);
		Assert.assertEquals("My private collection", newColl.getName());
		dbInstance.commit();//check if it's alright
		
		//retrieve the list of items in the collection
		int numOfItemsInCollection = qpoolService.countItemsOfCollection(newColl);
		Assert.assertEquals(2, numOfItemsInCollection);
		ResultInfos<QuestionItem> itemsOfCollection = qpoolService.getItemsOfCollection(newColl, null, 0, -1);
		Assert.assertNotNull(itemsOfCollection);
		Assert.assertEquals(2, itemsOfCollection.getObjects().size());
		Assert.assertTrue(itemsOfCollection.getObjects().contains(item1));
		Assert.assertTrue(itemsOfCollection.getObjects().contains(item2));
	}

	@Test
	public void importItem_qti12xml() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Imp-Owner-1-" + UUID.randomUUID().toString());
		dbInstance.commit();
		URL itemUrl = QuestionPoolServiceTest.class.getResource("mchc_i_001.xml");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		
		qpoolService.importItem(owner, "mchc_i_001.xml", itemFile);
		
		
	}
	
}
