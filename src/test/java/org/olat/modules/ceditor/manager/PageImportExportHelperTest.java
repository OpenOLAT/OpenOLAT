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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.ceditor.Page;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageImportExportHelperTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(PageImportExportHelperTest.class);
	
	@Autowired
	private PageImportExportHelper pageImportExportHelper;
	
	@Test
	public void importPage() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("page-io-1");
		
		URL archiveUrl = PageImportExportHelperTest.class.getResource("page_withImage.zip");
		File archiveFile = new File(archiveUrl.toURI());
		Page importedPage = null;
		try(ZipFile pageArchive=new ZipFile(archiveFile)) {
			importedPage = pageImportExportHelper.importPage(pageArchive, author);
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}

		Assert.assertNotNull(importedPage);	
	}

}
