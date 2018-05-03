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
package org.olat.portfolio;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.manager.EPStructureManagerTest;
import org.olat.portfolio.manager.EPXStreamHandler;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test different imports
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPImportTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private EPFrontendManager epFrontendManager;
	@Autowired
	private EPStructureManager epStructureManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	
	@Test
	public void testImportFromOpenOLAT_81_Hibernate3() throws URISyntaxException  {
		URL mapUrl = EPImportTest.class.getResource("map_81.xml.zip");
		assertNotNull(mapUrl);
		File mapFile = new File(mapUrl.toURI());
		PortfolioStructure rootStructure = EPXStreamHandler.getAsObject(mapFile, false);
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
	
		//import the map
		PortfolioStructureMap importedMap = epFrontendManager.importPortfolioMapTemplate(rootStructure, resource);
		Assert.assertNotNull(importedMap);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testCopy() throws URISyntaxException  {
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("ImPort-1");
		//save the map
		PortfolioStructureMap map = EPStructureManagerTest.createPortfolioMapTemplate(ident, "import-map-1", "map-template");
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();
		
		//check that the author are in the
		OLATResource resource = resourceManager.findResourceable(map.getResourceableId(), map.getResourceableTypeName());
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(resource, false);
		Assert.assertNotNull(re);
		dbInstance.commitAndCloseSession();

		RepositoryEntry copy = repositoryService.copy(re, ident, "ImPort - (Copy 1)");
		Assert.assertNotNull(copy);
		dbInstance.commitAndCloseSession();
		
		PortfolioStructure copiedMap = epFrontendManager.loadPortfolioStructure(copy.getOlatResource());
		Assert.assertNotNull(copiedMap);
	}
}
