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
package org.olat.ims.qti.qpool;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.jgroups.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.qpool.QTIImportProcessor.DocInfos;
import org.olat.ims.qti.qpool.QTIImportProcessor.ItemInfos;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIImportProcessorTest extends OlatTestCase {
	
	private static Identity owner;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	@Before
	public void setup() {
		if(owner == null) {
			owner = JunitTestHelper.createAndPersistIdentityAsUser("QTI-imp-owner-" + UUID.randomUUID().toString());
		}
	}
	
	@Test
	public void testImport_SC() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("mchc_i_002.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao);
		DocInfos docInfos = proc.getDocInfos();
		Assert.assertNotNull(docInfos);
		Assert.assertNotNull(docInfos.getFilename());
		Assert.assertNotNull(docInfos.getDocument());
		Assert.assertEquals("mchc_i_002.xml", docInfos.getFilename());
		
		//get the question DOM's
		List<ItemInfos> itemElements = proc.getItemList(docInfos);
		Assert.assertNotNull(itemElements);
		Assert.assertEquals(1, itemElements.size());
		
		//process item
		QuestionItem item = proc.processItem(itemElements.get(0));
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		//reload and check what is saved
		QuestionItem reloadItem = questionItemDao.loadById(item.getKey());
		Assert.assertNotNull(reloadItem);
		Assert.assertNotNull(reloadItem.getCreationDate());
		Assert.assertNotNull(reloadItem.getLastModified());
		Assert.assertEquals(QuestionStatus.draft, reloadItem.getQuestionStatus());
		Assert.assertEquals(QTIConstants.QTI_12_FORMAT, reloadItem.getFormat());
		//title
		Assert.assertEquals("Standard Multiple Choice with Images Item", reloadItem.getTitle());
		//description -> qticomment
		Assert.assertEquals("This is a multiple-choice example with image content. The rendering is a standard radio button style. No response processing is incorporated.", reloadItem.getDescription());
		//question type
		Assert.assertEquals(QuestionType.SC, reloadItem.getQuestionType());
	}
	
	@Test
	public void testImport_FIB() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("fibi_i_001.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		dbInstance.commitAndCloseSession();
		
		//reload and check what is saved
		QuestionItem reloadItem = questionItemDao.loadById(items.get(0).getKey());
		Assert.assertNotNull(reloadItem);
		Assert.assertNotNull(reloadItem.getCreationDate());
		Assert.assertNotNull(reloadItem.getLastModified());
		Assert.assertEquals(QuestionStatus.draft, reloadItem.getQuestionStatus());
		Assert.assertEquals(QTIConstants.QTI_12_FORMAT, reloadItem.getFormat());
		//title
		Assert.assertEquals("Standard FIB numerical Item", reloadItem.getTitle());
		//description -> qticomment
		Assert.assertEquals("This is a standard numerical fill-in-blank (integer) example. No response processing is incorporated.", reloadItem.getDescription());
		//question type
		Assert.assertEquals(QuestionType.FIB, reloadItem.getQuestionType());
	}
	
	
	
	
	
	
	
	
	
	
}
