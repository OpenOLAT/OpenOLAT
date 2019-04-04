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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.fo.portfolio.ForumArtefact;
import org.olat.portfolio.manager.EPArtefactManager;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.artefacts.EPStructureElementArtefact;
import org.olat.portfolio.model.artefacts.FileArtefact;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is an integration test of the EPArtefactManager to test the DB
 * 
 * <P>
 * Initial Date:  24 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class EPArtefactManagerTest extends OlatTestCase {
	
	private static Identity ident1;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private PortfolioModule portfolioModule;
	
	@Autowired
	private EPArtefactManager epArtefactManager;
	
	@Autowired
	private EPFrontendManager epFrontendManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(dbInstance);
		assertNotNull(epArtefactManager);
		assertNotNull(portfolioModule);
		assertNotNull(epFrontendManager);
	}
	
	@Test
	public void testDeleteArtefact() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		dbInstance.commitAndCloseSession();
		
		long artKey = artefact.getKey();
		AbstractArtefact loadedArtefact = epFrontendManager.loadArtefactByKey(artKey);
		// assure the artefact exists in db before deletion
		assertEquals(artefact.getKey(), loadedArtefact.getKey());
		epFrontendManager.deleteArtefact(loadedArtefact);
		AbstractArtefact loadedArtefact2 = epFrontendManager.loadArtefactByKey(artKey);
		assertNull(loadedArtefact2);
	}
		
	@Test
	public void testDeleteArtefactReferences() {
		// test deletion of references to artefact
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el", "structure-element");
		epFrontendManager.addArtefactToStructure(ident1, artefact2, el);
		assertTrue(epFrontendManager.getArtefacts(el).get(0).equalsByPersistableKey(artefact2));
		epFrontendManager.deleteArtefact(artefact2);
		assertEquals(0, epFrontendManager.getArtefacts(el).size());
	}
		
	@Test
	public void testDeleteArtefactVFSContainer() {
		// test deletion of vfs-artefactContainer
		AbstractArtefact artefact3 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		VFSContainer artCont = epFrontendManager.getArtefactContainer(artefact3);
		artCont.createChildLeaf("testfile.txt");
		assertEquals(1, artCont.getItems(new VFSSystemItemFilter()).size());
		Long artKey3 = artefact3.getKey();
		epFrontendManager.deleteArtefact(artefact3);
		VFSItem item = epFrontendManager.getArtefactsRoot().resolve(artKey3.toString());
		assertNull(item);
	}
	
	@Test
	public void testGetArtefactPoolForUser() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		List<AbstractArtefact> artefactList = epFrontendManager.getArtefactPoolForUser(ident1);
		assertEquals(2, artefactList.size());
		assertTrue(artefactList.contains(artefact));
		assertTrue(artefactList.contains(artefact2));
	}
	
	@Test
	public void testCreateForumArtefact() {
		EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler("Forum");
		AbstractArtefact artefact = handler.createArtefact();
		artefact.setAuthor(ident1);
		assertNotNull(artefact);
		assertTrue(artefact instanceof ForumArtefact);

		//update the artefact
		AbstractArtefact persistedArtefact = epFrontendManager.updateArtefact(artefact);
		assertNotNull(persistedArtefact);
		assertTrue(persistedArtefact instanceof ForumArtefact);
		dbInstance.commitAndCloseSession();
		
		//reload the artefact
		AbstractArtefact loadedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
		assertNotNull(loadedArtefact);
		assertTrue(loadedArtefact instanceof ForumArtefact);
		
		//get the VFS container of the artefact
		VFSContainer container = epFrontendManager.getArtefactContainer(loadedArtefact);
		assertNotNull(container);
		if(container instanceof LocalFolderImpl) {
			LocalFolderImpl folder = (LocalFolderImpl)container;
			assertNotNull(folder.getBasefile());
			assertTrue(folder.getBasefile().exists());
			assertTrue(folder.getBasefile().isDirectory());
		}
	}
	
	@Test
	public void testCreateForumArtefactAlternateVersion() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		assertNotNull(artefact);
		assertTrue(artefact instanceof ForumArtefact);
		dbInstance.commitAndCloseSession();
		assertNotNull(artefact.getKey());

		//reload the artefact
		AbstractArtefact persistedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
		assertNotNull(persistedArtefact);
		assertTrue(persistedArtefact instanceof ForumArtefact);
	}
	
	@Test
	public void testSaveFileArtefactWithAllProperties() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		assertNotNull(artefact);
		assertTrue(artefact instanceof FileArtefact);
		FileArtefact fileArtefact = (FileArtefact)artefact;
		fileArtefact.setBusinessPath("[CourseModule:526834956][path=/test/test.html:0]");
		fileArtefact.setCollectionDate(new Date());
		fileArtefact.setDescription("A description of the file artefact");
		fileArtefact.setFulltextContent("The text of the artefact");
		fileArtefact.setReflexion("A deep reflexion caused by this artefact");
		fileArtefact.setSignature(90);
		fileArtefact.setSource("A file");
		fileArtefact.setTitle("File artefact");
		epFrontendManager.updateArtefact(artefact);
		dbInstance.commitAndCloseSession();
		assertNotNull(artefact.getKey());

		//reload the artefact
		AbstractArtefact persistedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
		assertNotNull(persistedArtefact);
		assertTrue(persistedArtefact instanceof FileArtefact);
		assertEquals("[CourseModule:526834956][path=/test/test.html:0]", persistedArtefact.getBusinessPath());
		assertNotNull(persistedArtefact.getCollectionDate());
		assertEquals("A description of the file artefact", persistedArtefact.getDescription());
		assertEquals("The text of the artefact", persistedArtefact.getFulltextContent());
		assertEquals("A deep reflexion caused by this artefact", persistedArtefact.getReflexion());
		assertEquals(90, persistedArtefact.getSignature());
		assertEquals("A file", persistedArtefact.getSource());
		assertEquals("File artefact", persistedArtefact.getTitle());
	}
	
	@Test
	public void testCreateStructureElementArtefact() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "ep-structure-element");
		if (artefact != null) { // handler is disabled or another error occurred while
														// trying to create the artefact
			assertNotNull(artefact);
			assertTrue(artefact instanceof EPStructureElementArtefact);

			EPStructureElementArtefact elementArtefact = (EPStructureElementArtefact)artefact;
			PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el-for-artefact", "structure-element-for-artefact");
			elementArtefact.setStructureElement((EPStructureElement)el);
			
			dbInstance.commitAndCloseSession();
			assertNotNull(artefact.getKey());
	
			//reload the artefact
			AbstractArtefact persistedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
			assertNotNull(persistedArtefact);
			assertTrue(persistedArtefact instanceof EPStructureElementArtefact);
			EPStructureElementArtefact persistedElementArtefact = (EPStructureElementArtefact)persistedArtefact;
			assertNotNull(persistedElementArtefact.getStructureElement());
			assertEquals(el.getKey(), persistedElementArtefact.getStructureElement().getKey());
		}
	}
}
