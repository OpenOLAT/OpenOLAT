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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.Node;
import org.jgroups.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.qpool.QTIImportProcessor.DocInfos;
import org.olat.ims.qti.qpool.QTIImportProcessor.ItemInfos;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.FileStorage;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
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
	private FileStorage qpoolFileStorage;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	
	@Before
	public void setup() {
		if(owner == null) {
			owner = JunitTestHelper.createAndPersistIdentityAsUser("QTI-imp-owner-" + UUID.randomUUID().toString());
		}
	}
	
	/**
	 * This test check every methods of the import process in details
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testImport_SC() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("mchc_i_002.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		DocInfos docInfos = proc.getDocInfos();
		Assert.assertNotNull(docInfos);
		Assert.assertNotNull(docInfos.getFilename());
		Assert.assertNotNull(docInfos.getDocument());
		Assert.assertEquals("mchc_i_002.xml", docInfos.getFilename());
		
		//get the question DOM's
		List<ItemInfos> itemInfos = proc.getItemList(docInfos);
		Assert.assertNotNull(itemInfos);
		Assert.assertEquals(1, itemInfos.size());
		
		//process item, files...
		QuestionItemImpl item = proc.processItem(docInfos, itemInfos.get(0));
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		proc.processFiles(item, itemInfos.get(0));
		
		//reload and check what is saved
		QuestionItemFull reloadItem = questionItemDao.loadById(item.getKey());
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
		Assert.assertNotNull(reloadItem.getType());
		Assert.assertEquals(QuestionType.SC.name(), reloadItem.getType().getType());
		
		//check that the file is storead
		VFSContainer itemDir = qpoolFileStorage.getContainer(reloadItem.getDirectory());
		Assert.assertNotNull(itemDir);
		VFSItem qtiLeaf = itemDir.resolve(reloadItem.getRootFilename());
		Assert.assertNotNull(qtiLeaf);
		Assert.assertTrue(qtiLeaf instanceof VFSLeaf);
		Assert.assertTrue(qtiLeaf.exists());
		Assert.assertEquals(itemFile.length(), ((VFSLeaf)qtiLeaf).getSize());
	}
	
	@Test
	public void testImport_FIB() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("fibi_i_001.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
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
		Assert.assertEquals(QuestionType.FIB.name(), reloadItem.getType().getType());
	}

	/**
	 * This test check every methods of the import process in details
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testImport_OpenOLATTest_extractItems() throws IOException, URISyntaxException {
		URL testUrl = QTIImportProcessorTest.class.getResource("oo_test_qti.xml");
		Assert.assertNotNull(testUrl);
		File testFile = new File(testUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, testFile.getName(), testFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		DocInfos docInfos = proc.getDocInfos();
		Assert.assertNotNull(docInfos);
		Assert.assertNotNull(docInfos.getFilename());
		Assert.assertNotNull(docInfos.getDocument());
		Assert.assertEquals("oo_test_qti.xml", docInfos.getFilename());
		
		//get the question DOM's
		List<ItemInfos> itemElements = proc.getItemList(docInfos);
		Assert.assertNotNull(itemElements);
		Assert.assertEquals(4, itemElements.size());
	}
	
	@Test
	public void testImport_OpenOLATTest_process() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("oo_test_qti.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(4, items.size());
		dbInstance.commitAndCloseSession();

		//check
		int sc = 0;
		int mc = 0;
		int kprim = 0;
		int fib = 0;

		for(QuestionItem item:items) {
			Assert.assertEquals(QTIConstants.QTI_12_FORMAT, item.getFormat());
			QItemType itemType = item.getType();
			Assert.assertNotNull(itemType);
			QuestionType type = QuestionType.valueOf(itemType.getType());
			if(type != null) {
				switch(type) {
					case SC: sc++; break;
					case MC: mc++; break;
					case KPRIM: kprim++; break;
					case FIB: fib++; break;
					default: {
						Assert.fail("No question type");
					}
				}
			}
		}

		Assert.assertEquals("1 single choice", 1, sc);
		Assert.assertEquals("1 multiple choice", 1, mc);
		Assert.assertEquals("1 krpim", 1, kprim);
		Assert.assertEquals("1 fill-in-blanck", 1, fib);
		
		//check the files
		for(QuestionItem item:items) {
			QuestionItemFull itemFull = (QuestionItemFull)item;
			String dir = itemFull.getDirectory();
			String file = itemFull.getRootFilename();
			VFSContainer itemContainer = qpoolFileStorage.getContainer(dir);
			Assert.assertNotNull(itemContainer);
			VFSItem itemLeaf = itemContainer.resolve(file);
			Assert.assertNotNull(itemLeaf);
			Assert.assertTrue(itemLeaf instanceof VFSLeaf);
			
			//try to parse it
			InputStream is = ((VFSLeaf)itemLeaf).getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(is, false);
			Node itemNode = doc.selectSingleNode("questestinterop/item");
			Assert.assertNotNull(itemNode);
		}
	}
	
	@Test
	public void testImport_OpenOLATTest_processAttachments_mattext() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("oo_test_qti_attachments.zip");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		dbInstance.commitAndCloseSession();
		
		//check the files
		for(QuestionItem item:items) {
			QuestionItemFull itemFull = (QuestionItemFull)item;
			String dir = itemFull.getDirectory();
			String file = itemFull.getRootFilename();
			VFSContainer itemContainer = qpoolFileStorage.getContainer(dir);
			Assert.assertNotNull(itemContainer);
			VFSItem itemLeaf = itemContainer.resolve(file);
			Assert.assertNotNull(itemLeaf);
			Assert.assertTrue(itemLeaf instanceof VFSLeaf);
			
			//try to parse it
			InputStream is = ((VFSLeaf)itemLeaf).getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(is, false);
			Node itemNode = doc.selectSingleNode("questestinterop/item");
			Assert.assertNotNull(itemNode);
			
		//check the attachments
			if(itemFull.getType().getType().equals(QuestionType.SC.name())) {
				Assert.assertTrue(exists(itemFull, "media/image1.gif"));
				Assert.assertTrue(exists(itemFull, "media/image2.gif"));
				Assert.assertFalse(exists(itemFull, "media/image3.gif"));
			} else if(itemFull.getType().getType().equals(QuestionType.MC.name())) {
				Assert.assertFalse(exists(itemFull, "media/image1.gif"));
				Assert.assertTrue(exists(itemFull, "media/image2.gif"));
				Assert.assertTrue(exists(itemFull, "media/image3.gif"));
			} else {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void testImport_QTI12_processAttachments_matimg() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("mchc_asmimr_106.zip");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(3, items.size());
		dbInstance.commitAndCloseSession();
		
		//check the files
		for(QuestionItem item:items) {
			QuestionItemFull itemFull = (QuestionItemFull)item;
			String dir = itemFull.getDirectory();
			String file = itemFull.getRootFilename();
			VFSContainer itemContainer = qpoolFileStorage.getContainer(dir);
			Assert.assertNotNull(itemContainer);
			VFSItem itemLeaf = itemContainer.resolve(file);
			Assert.assertNotNull(itemLeaf);
			Assert.assertTrue(itemLeaf instanceof VFSLeaf);
			
			//try to parse it
			InputStream is = ((VFSLeaf)itemLeaf).getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(is, false);
			Node itemNode = doc.selectSingleNode("questestinterop/item");
			Assert.assertNotNull(itemNode);
			
			//check the attachments
			if("Rivers in Germany".equals(itemFull.getTitle())) {
				Assert.assertFalse(exists(itemFull, "image1.gif"));
				Assert.assertFalse(exists(itemFull, "image2.gif"));
				Assert.assertFalse(exists(itemFull, "image3.gif"));
				Assert.assertTrue(exists(itemFull, "images/image02.jpg"));
			} else if("Capital of France".equals(itemFull.getTitle())) {
				Assert.assertTrue(exists(itemFull, "image1.gif"));
				Assert.assertTrue(exists(itemFull, "image2.gif"));
				Assert.assertFalse(exists(itemFull, "image3.gif"));
				Assert.assertFalse(exists(itemFull, "images/image02.jpg"));
			} else if("Rivers in France question".equals(itemFull.getTitle())) {
				Assert.assertFalse(exists(itemFull, "image1.gif"));
				Assert.assertTrue(exists(itemFull, "image2.gif"));
				Assert.assertTrue(exists(itemFull, "image3.gif"));
				Assert.assertFalse(exists(itemFull, "images/image02.jpg"));
			} else {
				Assert.fail();
			}
		}
	}
	
	@Test
	public void testImport_QTI12_metadata() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("mchc_i_001.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, itemFile.getName(), itemFile, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		dbInstance.commitAndCloseSession();
		
		//check metadata
		QuestionItem item = items.get(0);
		Assert.assertEquals("Standard Multiple Choice Item", item.getTitle());
		//qmd_levelofdifficulty
		QEducationalContext level = item.getEducationalContext();
		Assert.assertNotNull(level);
		Assert.assertEquals("basic", level.getLevel());
		//qmd_toolvendor
		Assert.assertEquals("QTITools", item.getEditor());	
	}
	
	private boolean exists(QuestionItemFull itemFull, String path) {
		String dir = itemFull.getDirectory();
		VFSContainer itemContainer = qpoolFileStorage.getContainer(dir);
		Assert.assertNotNull(itemContainer);
		VFSItem itemLeaf = itemContainer.resolve(path);
		return (itemLeaf instanceof VFSLeaf);
	}
}