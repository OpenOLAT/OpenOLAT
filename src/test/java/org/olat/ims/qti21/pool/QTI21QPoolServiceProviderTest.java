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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItem;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21QPoolServiceProviderTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21QPoolServiceProvider poolServiceProvider;
	
	@Test
	public void compatibleArchive_qpoolQuestions() throws URISyntaxException {
		String filename = "ExportItems_pool_sc_mc.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		boolean compatible = poolServiceProvider.isCompatible(filename, questionFile);
		Assert.assertTrue(compatible);
	}
	
	@Test
	public void importArchive_qpoolQuestions() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("imp-pool-1");
		
		String filename = "ExportItems_pool_sc_mc.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		List<QuestionItem> items = poolServiceProvider.importItems(owner, Locale.ENGLISH, filename, questionFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		int sc = 0;
		int mc = 0;
		for(QuestionItem item:items) {
			if(item.getType() != null) {
				if("sc".equals(item.getType().getType())) {
					sc++;
				} else if("mc".equals(item.getType().getType())) {
					mc++;
				}
			}
		}

		Assert.assertEquals(1, sc);
		Assert.assertEquals(1, mc);
	}
	
	@Test
	public void importArchive_qpoolQuestionsMetadata() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("imp-pool-1");
		
		String filename = "ExportItems_pool_metadata.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		List<QuestionItem> items = poolServiceProvider.importItems(owner, Locale.ENGLISH, filename, questionFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		QuestionItem item = items.get(0);
		Assert.assertEquals("OpenOLAT", item.getEditor());
		Assert.assertEquals("10.2.1", item.getEditorVersion());
		Assert.assertEquals("IMS QTI 2.1", item.getFormat());
		Assert.assertEquals(0.54d, item.getDifficulty().doubleValue(), 0.00001);
		Assert.assertEquals(0.5d, item.getDifferentiation().doubleValue(), 0.00001);
		Assert.assertEquals(0.33d, item.getStdevDifficulty().doubleValue(), 0.00001);
		Assert.assertEquals(2, item.getNumOfAnswerAlternatives());
		Assert.assertEquals("Image", item.getKeywords());
	}
	
	@Test
	public void compatibleArchive_openolatTest() throws URISyntaxException {
		String filename = "QTI_21_test_sc_mc.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		boolean compatible = poolServiceProvider.isCompatible(filename, questionFile);
		Assert.assertTrue(compatible);
	}
	
	@Test
	public void importArchive_openolatTest() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("imp-pool-2");
		
		String filename = "QTI_21_test_sc_mc.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		List<QuestionItem> items = poolServiceProvider.importItems(owner, Locale.ENGLISH, filename, questionFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		int sc = 0;
		int mc = 0;
		for(QuestionItem item:items) {
			if(item.getType() != null) {
				if("sc".equals(item.getType().getType())) {
					sc++;
				} else if("mc".equals(item.getType().getType())) {
					mc++;
				}
			}
		}

		Assert.assertEquals(1, sc);
		Assert.assertEquals(1, mc);
	}
	/*
	@Test
	public void compatibleArchive_cyber_20_Questions() throws URISyntaxException {
		String filename = "QTI_20_cyber_hotspot.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		boolean compatible = poolServiceProvider.isCompatible(filename, questionFile);
		Assert.assertTrue(compatible);
	}
	
	@Test
	public void importArchive_cyber_20_Questions() throws URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsUser("imp-pool-3");
		
		String filename = "QTI_20_cyber_hotspot.zip";
		URL fileUrl = QTI21QPoolServiceProviderTest.class.getResource(filename);
		File questionFile = new File(fileUrl.toURI());
		
		List<QuestionItem> items = poolServiceProvider.importItems(owner, Locale.ENGLISH, filename, questionFile);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		int hotspot = 0;
		for(QuestionItem item:items) {
			if(item.getType() != null) {
				if("hotspot".equals(item.getType().getType())) {
					hotspot++;
				}
			}
		}

		Assert.assertEquals(1, hotspot);
	}
	*/

}
