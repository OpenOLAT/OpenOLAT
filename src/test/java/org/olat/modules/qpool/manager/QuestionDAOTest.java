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
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
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
public class QuestionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private MarkManager markManager;
	
	@Test
	public void createQuestion() {
		QuestionItem item = questionDao.create(null, "Stars", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.FIB);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getKey());
		Assert.assertNotNull(item.getUuid());
		Assert.assertNotNull(item.getCreationDate());
		Assert.assertNotNull(item.getLastModified());
		Assert.assertNotNull(item.getQuestionType());
		Assert.assertNotNull(item.getQuestionStatus());
		Assert.assertEquals("Stars", item.getSubject());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createQuestion_withOwner() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-1-" + UUID.randomUUID().toString());
		QuestionItem item = questionDao.create(id, "My fav. stars", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.FIB);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getKey());
		Assert.assertNotNull(item.getCreationDate());
		Assert.assertNotNull(item.getLastModified());
		Assert.assertNotNull(item.getQuestionType());
		Assert.assertNotNull(item.getQuestionStatus());
		Assert.assertEquals("My fav. stars", item.getSubject());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getItems_byAuthor() {
		//create an author with 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-2-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.create(id, "NGC 2171", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.FIB);
		QuestionItem item2 = questionDao.create(id, "NGC 2172", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.FIB);
		dbInstance.commitAndCloseSession();
		
		//count the items of the author
		int numOfItems = questionDao.countItems(id);
		Assert.assertEquals(2, numOfItems);
		//retrieve the items of the author
		List<QuestionItem> items = questionDao.getItems(id, 0, -1);
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		Assert.assertTrue(items.contains(item1));
		Assert.assertTrue(items.contains(item2));
	}

	@Test
	public void getNumOfQuestions() {
		QuestionItem item = questionDao.create(null, "NGC 1277", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		int numOfQuestions = questionDao.getNumOfQuestions();
		Assert.assertTrue(numOfQuestions >= 1);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getFavoritItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.create(id, "NGC 55", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		QuestionItem item2 = questionDao.create(id, "NGC 253", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		QuestionItem item3 = questionDao.create(id, "NGC 292", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1 + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2 + "]");
		dbInstance.commitAndCloseSession();
		
		List<QuestionItem> favorits = questionDao.getFavoritItems(id, 0, -1);
		Assert.assertNotNull(favorits);
		Assert.assertEquals(2, favorits.size());
		Assert.assertTrue(favorits.contains(item1));
		Assert.assertTrue(favorits.contains(item2));
		Assert.assertFalse(favorits.contains(item3));
	}

	@Test
	public void shareItems() {
		//create a group to share 2 items
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.create(null, "Share-Item-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		QuestionItem item2 = questionDao.create(null, "Share-Item-2", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		dbInstance.commit();
		
		//share them
		questionDao.share(item1, group.getResource());
		questionDao.share(item2, group.getResource());
		
		//retrieve them
		List<QuestionItem> sharedItems = questionDao.getSharedItemByResource(group.getResource(), 0, -1);
		Assert.assertNotNull(sharedItems);
		Assert.assertEquals(2, sharedItems.size());
		Assert.assertTrue(sharedItems.contains(item1));
		Assert.assertTrue(sharedItems.contains(item2));
	}
	
	@Test
	public void shareItems_avoidDuplicates() {
		//create a group to share 2 items
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.create(null, "Share-Item-Dup-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		dbInstance.commit();
		
		//share them
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		
		//retrieve them
		List<QuestionItem> sharedItems = questionDao.getSharedItemByResource(group.getResource(), 0, -1);
		Assert.assertNotNull(sharedItems);
		Assert.assertEquals(1, sharedItems.size());
		Assert.assertTrue(sharedItems.contains(item));
	}
	
	@Test
	public void shareItems_resources() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-item-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.create(id, "Share-Item-Dup-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		dbInstance.commit();
		
		//share them
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		
		//retrieve them
		List<BusinessGroup> shared = questionDao.getResourcesWithSharedItems(id);
		Assert.assertNotNull(shared);
		Assert.assertEquals(1, shared.size());
		Assert.assertTrue(shared.contains(group));
	}
	
	@Test
	public void removeFromShare() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-rm-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdrm", "gdrm-desc", -1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.create(id, "Share-item-rm-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, null, QuestionType.MC);
		dbInstance.commit();
		//share them
		questionDao.share(item, group.getResource());
		
		//retrieve them as a check
		List<QuestionItem> shared = questionDao.getSharedItemByResource(group.getResource(), 0, -1);
		Assert.assertEquals(1, shared.size());
		//and remove the items
		int count = questionDao.deleteFromShares(shared);
		Assert.assertEquals(1, count);
		dbInstance.commit();//make sure that changes are committed
	}
	

	
	
	
	

}
