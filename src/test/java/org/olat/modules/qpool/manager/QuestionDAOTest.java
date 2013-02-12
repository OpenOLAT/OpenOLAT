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
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItem;
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
	private QuestionItemDAO questionDao;
	@Autowired
	private MarkManager markManager;
	
	@Test
	public void createQuestion() {
		QuestionItem item = questionDao.create("Stars");
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getKey());
		Assert.assertNotNull(item.getCreationDate());
		Assert.assertEquals("Stars", item.getSubject());
		dbInstance.commitAndCloseSession();
	}

	@Test
	public void getNumOfQuestions() {
		QuestionItem item = questionDao.create("NGC 1277");
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		int numOfQuestions = questionDao.getNumOfQuestions();
		Assert.assertTrue(numOfQuestions >= 1);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getFavoritItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.create("NGC 55");
		QuestionItem item2 = questionDao.create("NGC 253");
		QuestionItem item3 = questionDao.create("NGC 292");
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
	
	

}
