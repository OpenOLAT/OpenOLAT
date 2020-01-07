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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QPoolFileStorage;
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
public class QTIExportProcessorTest extends OlatTestCase {
	
	private static Identity owner;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QuestionItemDAO questionItemDao;

	
	@Before
	public void setup() {
		if(owner == null) {
			owner = JunitTestHelper.createAndPersistIdentityAsUser("QTI-imp-owner-" + UUID.randomUUID().toString());
		}
	}
	
	
	@Test
	public void testImport_QTI12_metadata() throws IOException, URISyntaxException {
		//first import
		URL itemUrl = QTIExportProcessorTest.class.getResource("mchc_asmimr_106.zip");
		Assert.assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());
		QTIImportProcessor proc = new QTIImportProcessor(owner, Locale.ENGLISH, itemFile.getName(), itemFile);
		List<QuestionItem> items = proc.process();
		Assert.assertNotNull(items);
		dbInstance.commitAndCloseSession();
		
		//after export
		QTIExportProcessor exportProc = new QTIExportProcessor(qpoolFileStorage);
		List<QuestionItemFull> fullItems = questionItemDao.loadByIds(Collections.singletonList(items.get(0).getKey()));
		
		OutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(out);
		exportProc.assembleTest(fullItems, zout);
		
		zout.close();
		out.close();
	}
}