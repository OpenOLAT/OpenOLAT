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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QuestionItem;
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
		QuestionItem item1 = questionDao.create("Share-item-rm-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, QuestionType.MC);
		QuestionItem item2 = questionDao.create("Share-item-rm-1", QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), null, QuestionType.MC);
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
}
