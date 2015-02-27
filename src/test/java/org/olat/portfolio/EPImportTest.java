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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPXStreamHandler;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
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
	
	
	@Test
	public void testImportFromOpenOLAT_81_Hibernate3() throws URISyntaxException  {
		URL mapUrl = EPImportTest.class.getResource("map_81.xml.zip");
		assertNotNull(mapUrl);
		File mapFile = new File(mapUrl.toURI());
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("EPMapTemplate");
		OLATResource resource = resourceManager.createAndPersistOLATResourceInstance(ores);
		//import the map
		PortfolioStructure rootStructure = EPXStreamHandler.getAsObject(mapFile, false);
		PortfolioStructureMap importedMap = epFrontendManager.importPortfolioMapTemplate(rootStructure, resource);
		Assert.assertNotNull(importedMap);
		dbInstance.commitAndCloseSession();
	}
	

	
}
