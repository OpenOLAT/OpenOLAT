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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QPoolService;
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
public class QuestionPoolServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QPoolService qpoolService;
	
	@Test
	public void deleteItems() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-rm-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdrm", "gdrm-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(id, "Share-item-rm-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "Share-item-rm-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		//share them
		questionDao.share(item1, group.getResource());
		dbInstance.commitAndCloseSession();

		//delete the items
		List<QuestionItemShort> toDelete = new ArrayList<>();
		toDelete.add(item1);
		toDelete.add(item2);
		qpoolService.deleteItems(toDelete);
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
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Coll-Owner-3-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 92", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 97", QTI21Constants.QTI_21_FORMAT, Locale.GERMAN.getLanguage(), null, null, null, fibType);
		dbInstance.commit();
		
		//load the items of the collection
		List<QuestionItemShort> items = new ArrayList<>();
		items.add(item1);
		items.add(item2);
		QuestionItemCollection newColl = qpoolService.createCollection(id, "My private collection", items);
		Assert.assertNotNull(newColl);
		Assert.assertEquals("My private collection", newColl.getName());
		dbInstance.commit();//check if it's alright
		
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setCollection(newColl);
		
		//retrieve the list of items in the collection
		int numOfItemsInCollection = qpoolService.countItems(params);
		Assert.assertEquals(2, numOfItemsInCollection);
		ResultInfos<QuestionItemView> itemsOfCollection = qpoolService.getItems(params, 0, -1);
		Assert.assertNotNull(itemsOfCollection);
		Assert.assertEquals(2, itemsOfCollection.getObjects().size());
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemView item:itemsOfCollection.getObjects()) {
			itemKeys.add(item.getKey());
		}
		Assert.assertTrue(itemKeys.contains(item1.getKey()));
		Assert.assertTrue(itemKeys.contains(item2.getKey()));
	}

	@Test
	public void importItem_qti12_item() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Imp-Owner-1-" + UUID.randomUUID().toString());
		dbInstance.commit();
		URL itemUrl = QuestionPoolServiceTest.class.getResource("mchc_i_001.xml");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());

		qpoolService.importItems(owner, Locale.ENGLISH, "mchc_i_001.xml", itemFile);
	}
	
	@Test
	public void importItem_qti12_assessment() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("Imp-Owner-2-" + UUID.randomUUID().toString());
		dbInstance.commit();
		URL itemUrl = QuestionPoolServiceTest.class.getResource("mchc_asmimr_101.xml");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());

		qpoolService.importItems(owner, Locale.ENGLISH, "mchc_asmimr_101.xml", itemFile);
	}
	
}