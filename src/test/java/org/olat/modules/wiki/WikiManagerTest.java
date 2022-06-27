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
package org.olat.modules.wiki;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiManagerTest extends OlatTestCase {
	
	@Autowired
	private WikiManager wikiManager;
	
	private File tmpWikiDir;
	
	@Before
	public void createTmpDir() {
		tmpWikiDir = new File(WebappHelper.getTmpDir(), "wiki" + CodeHelper.getForeverUniqueID());
	}
	
	@After
	public void deleteTmpDir() {
		FileUtils.deleteDirsAndFiles(tmpWikiDir, true, true);
	}
	
	@Test
	public void importWiki() throws URISyntaxException {
		URL wikiUrl = WikiManagerTest.class.getResource("wiki.zip");
		File wikiFile = new File(wikiUrl.toURI());
		wikiManager.importWiki(wikiFile, null, tmpWikiDir);
		
		File image = new File(tmpWikiDir, "media/IMG_1482.jpg");
		Assert.assertTrue(image.exists());
		File imageMetadata = new File(tmpWikiDir, "media/IMG_1482.jpg.metadata");
		Assert.assertTrue(imageMetadata.exists());
		File indexPage = new File(tmpWikiDir, "wiki/SW5kZXg=.wp");
		Assert.assertTrue(indexPage.exists());
	}
	
	@Test
	public void importWikiSlide() throws URISyntaxException {
		URL wikiUrl = WikiManagerTest.class.getResource("wiki_alt.zip");
		File wikiFile = new File(wikiUrl.toURI());
		boolean imported = wikiManager.importWiki(wikiFile, null, tmpWikiDir);
		Assert.assertFalse(imported);
	}

}
