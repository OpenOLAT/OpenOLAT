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
package org.olat.repository;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.repository.RepositoryEntryImportExport.RepositoryEntryImport;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryImportExportTest {
	
	/**
	 * Read a XML.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void readRepoXml() throws URISyntaxException {
		URL repoUrl = RepositoryEntryImportExportTest.class.getResource("repo_glossary_reduced.xml");
		File repoFile = new File(repoUrl.toURI());
		RepositoryEntryImport impExp = RepositoryEntryImportExport.readFromXml(repoFile);
		Assert.assertNotNull(impExp);
		Assert.assertEquals("Glossary", impExp.getDisplayname());
		Assert.assertEquals(Long.valueOf(86114307l), impExp.getKey());
		Assert.assertEquals("sropenpg_1_91372670216461", impExp.getSoftkey());
		Assert.assertEquals("Jean", impExp.getInitialAuthor());
	}
	
	/**
	 * Read a XML produce without the static keyword on RepositoryEntryImport.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void readRepoXmlBackwardsCompatibility() throws URISyntaxException {
		URL repoUrl = RepositoryEntryImportExportTest.class.getResource("repo_glossary_full.xml");
		File repoFile = new File(repoUrl.toURI());
		RepositoryEntryImport impExp = RepositoryEntryImportExport.readFromXml(repoFile);
		Assert.assertNotNull(impExp);
		Assert.assertEquals("Glossar", impExp.getDisplayname());
		Assert.assertEquals(Long.valueOf(86114306l), impExp.getKey());
		Assert.assertEquals("sropenpg_1_91372670216461", impExp.getSoftkey());
	}
}
