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
package org.olat.core.commons.modules.glossary;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.test.OlatTestCase;
import org.olat.test.VFSJavaIOFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GlossaryItemManagerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(GlossaryItemManagerTest.class);
	
	@Autowired
	private GlossaryItemManager glossaryItemManager;
	
	@Test
	public void loadGlossaryItemListFromFile() throws URISyntaxException {
		URL glossaryUrl = GlossaryItemManagerTest.class.getResource("glossary.xml");
		VFSLeaf glossaryLeaf = new VFSJavaIOFile(glossaryUrl.toURI());
		List<GlossaryItem> items = glossaryItemManager.loadGlossaryItemListFromFile(glossaryLeaf);
		Assert.assertNotNull(items);
	}
	
	@Test
	public void saveToFile() throws URISyntaxException, MalformedURLException{
		GlossaryItem item = new GlossaryItem("Definition", "This is a definition.");
		List<String> flexions = new ArrayList<>();
		flexions.add("Add");
		flexions.add("Flex");
		item.setGlossFlexions(flexions);
		List<URI> uris = new ArrayList<>();
		uris.add(new URL("https://www.frentix.com").toURI());
		item.setGlossLinks(uris);
		List<String> synonyms = new ArrayList<>();
		synonyms.add("Synonym");
		item.setGlossSynonyms(synonyms);
		
		List<GlossaryItem> items = new ArrayList<>();
		items.add(item);
		
		File glossaryFile = new File(WebappHelper.getTmpDir(), "glossary" + UUID.randomUUID() + ".xml");
		glossaryFile.getParentFile().mkdirs();
		VFSLeaf glossaryLeaf = new VFSJavaIOFile(glossaryFile);
		glossaryItemManager.saveToFile(glossaryLeaf, items);
		
		// read the files
		List<GlossaryItem> savedItems = glossaryItemManager.loadGlossaryItemListFromFile(glossaryLeaf);
		Assert.assertNotNull(items);
		Assert.assertEquals(1, savedItems.size());
		Assert.assertEquals("Definition", savedItems.get(0).getGlossTerm());
		
		if(!glossaryFile.delete()) {
			log.error("Cannot delete: {}", glossaryFile);
		}
	}
}
