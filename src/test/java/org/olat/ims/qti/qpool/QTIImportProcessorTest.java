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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
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
	
	private static final Logger log = Tracing.createLoggerFor(QTIImportProcessorTest.class);
	
	private static Identity owner;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QuestionPoolModule qPoolModule;
	
	@Before
	public void setup() {
		if(owner == null) {
			owner = JunitTestHelper.createAndPersistIdentityAsUser("QTI-imp-owner-" + UUID.randomUUID().toString());
		}

		Taxonomy taxonomy = null;
		String taxonomyTreeKey = qPoolModule.getTaxonomyQPoolKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			taxonomy = taxonomyDao.loadByKey(new Long(taxonomyTreeKey));
		}
		
		if(taxonomy == null) {
			taxonomy = taxonomyDao.createTaxonomy("DP-1", "Doc-pool", "Taxonomy for document pool", null);
			dbInstance.commitAndCloseSession();
			qPoolModule.setTaxonomyQPoolKey(taxonomy.getKey().toString());
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
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
		List<DocInfos> docInfoList = proc.getDocInfos();
		Assert.assertNotNull(docInfoList);
		Assert.assertEquals(1, docInfoList.size());
		
		DocInfos docInfos = docInfoList.get(0);
		Assert.assertNotNull(docInfos);
		Assert.assertNotNull(docInfos.getFilename());
		Assert.assertNotNull(docInfos.getDocument());
		Assert.assertEquals("mchc_i_002.xml", docInfos.getFilename());
		
		//get the question DOM's
		List<ItemInfos> itemInfos = proc.getItemList(docInfos);
		Assert.assertNotNull(itemInfos);
		Assert.assertEquals(1, itemInfos.size());
		
		//process item, files...
		QuestionItemImpl item = proc.processItem(docInfos, itemInfos.get(0), null);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		proc.processFiles(item, itemInfos.get(0), null);
		
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
		Assert.assertEquals(QuestionType.SC.name().toLowerCase(), reloadItem.getType().getType());
		
		//check that the file is storead
		VFSContainer itemDir = qpoolFileStorage.getContainer(reloadItem.getDirectory());
		Assert.assertNotNull(itemDir);
		VFSItem qtiLeaf = itemDir.resolve(reloadItem.getRootFilename());
		Assert.assertNotNull(qtiLeaf);
		Assert.assertTrue(qtiLeaf instanceof VFSLeaf);
		Assert.assertTrue(qtiLeaf.exists());
		Assert.assertEquals(itemFile.length(), ((VFSLeaf)qtiLeaf).getSize());
		
		docInfos.close();
	}
	
	@Test
	public void testImport_FIB() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("fibi_i_001.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
		Assert.assertEquals(QuestionType.FIB.name().toLowerCase(), reloadItem.getType().getType());
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
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, testFile.getName(), testFile);
		List<DocInfos> docInfoList = proc.getDocInfos();
		Assert.assertNotNull(docInfoList);
		Assert.assertEquals(1, docInfoList.size());
		
		DocInfos docInfos = docInfoList.get(0);
		Assert.assertNotNull(docInfos);
		Assert.assertNotNull(docInfos.getFilename());
		Assert.assertNotNull(docInfos.getDocument());
		Assert.assertEquals("oo_test_qti.xml", docInfos.getFilename());
		
		//get the question DOM's
		List<ItemInfos> itemElements = proc.getItemList(docInfos);
		Assert.assertNotNull(itemElements);
		Assert.assertEquals(4, itemElements.size());
		
		docInfos.close();
	}
	
	@Test
	public void testImport_OpenOLATTest_process() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("oo_test_qti.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
			QuestionType type = QuestionType.valueOf(itemType.getType().toUpperCase());
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
			Node itemNode = null;
			try(InputStream is = ((VFSLeaf)itemLeaf).getInputStream()) {
				XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
				Document doc = xmlParser.parse(is, false);
				itemNode = doc.selectSingleNode("questestinterop/item");
			} catch(IOException e) {
				log.error("", e);
			}
			Assert.assertNotNull(itemNode);
		}
	}
	
	@Test
	public void testImport_OpenOLATTest_processAttachments_mattext() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("oo_test_qti_attachments.zip");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
			Node itemNode = null;
			try(InputStream is = ((VFSLeaf)itemLeaf).getInputStream()) {
				XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
				Document doc = xmlParser.parse(is, false);
				itemNode = doc.selectSingleNode("questestinterop/item");
			} catch(IOException e) {
				log.error("", e);
			}
			Assert.assertNotNull(itemNode);
			
		//check the attachments
			if(itemFull.getType().getType().equalsIgnoreCase(QuestionType.SC.name())) {
				Assert.assertTrue(exists(itemFull, "media/image1.gif"));
				Assert.assertTrue(exists(itemFull, "media/image2.gif"));
				Assert.assertFalse(exists(itemFull, "media/image3.gif"));
			} else if(itemFull.getType().getType().equalsIgnoreCase(QuestionType.MC.name())) {
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
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
			Node itemNode = null;
			try(InputStream is = ((VFSLeaf)itemLeaf).getInputStream()) {
				XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
				Document doc = xmlParser.parse(is, false);
				itemNode = doc.selectSingleNode("questestinterop/item");
			} catch(IOException e) {
				log.error("", e);
			}
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
	public void testImport_QTI12_multipleItems() throws IOException, URISyntaxException {
		URL itemsUrl = QTIImportProcessorTest.class.getResource("multiple_items.zip");
		Assert.assertNotNull(itemsUrl);
		File itemFile = new File(itemsUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
			Node itemNode = null;
			try(InputStream is = ((VFSLeaf)itemLeaf).getInputStream()) {
				XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
				Document doc = xmlParser.parse(is, false);
				itemNode = doc.selectSingleNode("questestinterop/item");
			} catch(IOException e) {
				log.error("", e);
			}
			Assert.assertNotNull(itemNode);
			
			//check the attachments
			if("Export (blue)".equals(itemFull.getTitle())) {
				Assert.assertTrue(exists(itemFull, "media/blue.png"));
				Assert.assertFalse(exists(itemFull, "media/purple.png"));
			} else if("Export (purple)".equals(itemFull.getTitle())) {
				Assert.assertFalse(exists(itemFull, "media/blue.png"));
				Assert.assertTrue(exists(itemFull, "media/purple.png"));
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
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
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
	
	@Test
	public void testImport_QTI12_sidecarMetadata() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("qitem_metadatas.zip");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		dbInstance.commitAndCloseSession();

		//reload and check metadata
		QuestionItem item = questionItemDao.loadById(items.get(0).getKey());
		Assert.assertEquals("Une information en plus", item.getAdditionalInformations());
		Assert.assertEquals("formative", item.getAssessmentType());
		Assert.assertEquals("large", item.getCoverage());
		Assert.assertEquals(0, new BigDecimal("-0.1").compareTo(item.getDifferentiation()));
		Assert.assertEquals(0, new BigDecimal("0.45").compareTo(item.getDifficulty()));
		Assert.assertEquals("OpenOLAT", item.getEditor());
		Assert.assertEquals("9.4", item.getEditorVersion());
		QEducationalContext level = item.getEducationalContext();
		Assert.assertNotNull(level);
		Assert.assertEquals("University", level.getLevel());
		Assert.assertEquals("P5DT4H3M2S", item.getEducationalLearningTime());
		Assert.assertEquals("IMS QTI 1.2", item.getFormat());
		Assert.assertEquals("6bae65ac-f333-40ba-bdd0-13b54d016d59", item.getMasterIdentifier());
		Assert.assertFalse("6bae65ac-f333-40ba-bdd0-13b54d016d59".equals(item.getIdentifier()));
		Assert.assertEquals("sc", item.getItemType());
		Assert.assertEquals("1.01", item.getItemVersion());
		Assert.assertEquals("question export import Pluton", item.getKeywords());
		Assert.assertEquals("de", item.getLanguage());
		Assert.assertEquals(1, item.getNumOfAnswerAlternatives());
		Assert.assertNotNull(item.getQuestionStatus());
		Assert.assertEquals("review", item.getQuestionStatus().name());
		Assert.assertEquals(0, new BigDecimal("0.56").compareTo(item.getStdevDifficulty()));
		Assert.assertEquals("/Physique/Astronomie/Astrophysique/", item.getTaxonomicPath());
		Assert.assertEquals("Une question sur Pluton", item.getTitle());
		Assert.assertEquals(0, item.getUsage());
		Assert.assertEquals(Integer.valueOf(2), item.getCorrectionTime());
	}
	
	@Test
	public void testImport_QTI12_film() throws IOException, URISyntaxException {
		URL itemUrl = QTIImportProcessorTest.class.getResource("sc_with_film.xml");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		
		//get the document informations
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		
		List<DocInfos> docInfoList = proc.getDocInfos();
		Assert.assertNotNull(docInfoList);
		Assert.assertEquals(1, docInfoList.size());
		
		DocInfos docInfos = docInfoList.get(0);
		List<ItemInfos> itemInfos = proc.getItemList(docInfos);
		Assert.assertNotNull(itemInfos);
		Assert.assertEquals(1, itemInfos.size());
		
		Element el = itemInfos.get(0).getItemEl();
		List<String> materials = proc.getMaterials(el);
		Assert.assertNotNull(materials);
		Assert.assertEquals(1, materials.size());
		Assert.assertEquals("media/filmH264.mp4", materials.get(0));
		
		docInfos.close();
	}
	
	private boolean exists(QuestionItemFull itemFull, String path) {
		String dir = itemFull.getDirectory();
		VFSContainer itemContainer = qpoolFileStorage.getContainer(dir);
		Assert.assertNotNull(itemContainer);
		VFSItem itemLeaf = itemContainer.resolve(path);
		return (itemLeaf instanceof VFSLeaf);
	}
}