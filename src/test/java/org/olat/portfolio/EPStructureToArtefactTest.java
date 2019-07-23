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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.fo.portfolio.ForumArtefact;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.restriction.RestrictionsConstants;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Description:<br>
 * Integration test for the link between structure element and artefact on the DB
 * 
 * <P>
 * Initial Date:  24 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class EPStructureToArtefactTest extends OlatTestCase {

	private static Identity ident1;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private EPStructureManager epStructureManager;
	
	@Autowired
	private EPFrontendManager epFrontendManager;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser("artuse-1");
		}
	}
	
	@Test
	public void createStructureToArtefactLink() {
		//create structure element
		PortfolioStructure structure = epFrontendManager.createAndPersistPortfolioStructureElement(null, "struc-to-art-el", "structure-to-artefact-element");

		//create artefact
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		dbInstance.commitAndCloseSession();
		
		//test if all is ok
		assertNotNull(structure);
		assertNotNull(artefact);
		assertTrue(artefact instanceof ForumArtefact);
		
		//create the link
		epFrontendManager.addArtefactToStructure(ident1, artefact, structure);
		dbInstance.commitAndCloseSession();
		
		//test if the link is persisted
		List<AbstractArtefact> linkedArtfeacts = epFrontendManager.getArtefacts(structure);
		assertNotNull(linkedArtfeacts);
		assertEquals(1, linkedArtfeacts.size());
		assertEquals(artefact.getKey(), linkedArtfeacts.get(0).getKey());
	}
	
	@Test
	public void addArtefactsToStructure() {
		//create structure element
		PortfolioStructure structure = epFrontendManager.createAndPersistPortfolioStructureElement(null, "struc-to-art-el", "structure-to-artefact-element");

		//create artefact
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		AbstractArtefact artefact3 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		epFrontendManager.addArtefactToStructure(ident1, artefact1, structure);
		epFrontendManager.addArtefactToStructure(ident1, artefact2, structure);
		epFrontendManager.addArtefactToStructure(ident1, artefact3, structure);
		dbInstance.commitAndCloseSession();

		//test if the link is persisted
		List<AbstractArtefact> linkedArtfeacts = epFrontendManager.getArtefacts(structure);
		assertNotNull(linkedArtfeacts);
		assertEquals(3, linkedArtfeacts.size());
		assertEquals(artefact1.getKey(), linkedArtfeacts.get(0).getKey());
		assertEquals(artefact2.getKey(), linkedArtfeacts.get(1).getKey());
		assertEquals(artefact3.getKey(), linkedArtfeacts.get(2).getKey());
	}
	
	@Test
	public void removeArtefactsToStructure() {
		//create structure element
		PortfolioStructure structure = epFrontendManager.createAndPersistPortfolioStructureElement(null, "struc-to-art-el", "structure-to-artefact-element");

		//create artefact
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		AbstractArtefact artefact3 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		epFrontendManager.addArtefactToStructure(ident1, artefact1, structure);
		epFrontendManager.addArtefactToStructure(ident1, artefact2, structure);
		epFrontendManager.addArtefactToStructure(ident1, artefact3, structure);
		dbInstance.commitAndCloseSession();
		
		epFrontendManager.removeArtefactFromStructure(artefact2, structure);
		dbInstance.commitAndCloseSession();

		//test if the link is persisted
		List<AbstractArtefact> linkedArtfeacts = epFrontendManager.getArtefacts(structure);
		assertNotNull(linkedArtfeacts);
		assertEquals(2, linkedArtfeacts.size());
		assertEquals(artefact1.getKey(), linkedArtfeacts.get(0).getKey());
		assertEquals(artefact3.getKey(), linkedArtfeacts.get(1).getKey());
	}
	
	/**
	 * Persist all properties and check them
	 */
	@Test
	public void saveArtefact() {
		//create artefact
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MILLISECOND, 0);
		Date collectionDate = cal.getTime();
		
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		artefact.setTitle("artefact-title");
		artefact.setBusinessPath("business-path");
		artefact.setCollectionDate(collectionDate);
		artefact.setDescription("artefact-description");
		artefact.setReflexion("artefact-reflexion");
		artefact.setSignature(70);
		artefact.setFulltextContent("fulltext-content");
		artefact.setSource("artefact-source");
		artefact = epFrontendManager.updateArtefact(artefact);
		dbInstance.commitAndCloseSession();

		//test if the link is persisted
		AbstractArtefact retrievedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
		assertNotNull(retrievedArtefact);
		assertEquals(artefact.getKey(), retrievedArtefact.getKey());
		assertEquals("artefact-title", retrievedArtefact.getTitle());
		assertEquals("business-path", retrievedArtefact.getBusinessPath());
		assertEquals("artefact-description", retrievedArtefact.getDescription());
		assertEquals("artefact-reflexion", retrievedArtefact.getReflexion());
		assertEquals(70, retrievedArtefact.getSignature());
		assertEquals("fulltext-content", retrievedArtefact.getFulltextContent());
		assertEquals("artefact-source", retrievedArtefact.getSource());
		assertEquals(ident1.getKey(), retrievedArtefact.getAuthor().getKey());
		
		//check date
		assertNotNull(retrievedArtefact.getCollectionDate());
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(collectionDate);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(retrievedArtefact.getCollectionDate());
		assertTrue(cal1.compareTo(cal2) == 0);
	}
	
	@Test
	public void testArtefactsPaging() {
		//save parent and 20 children
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "paged-structure-el", "paged-structure-element");
		
		List<AbstractArtefact> children = new ArrayList<>();
		for(int i=0;i<20;i++) {
			AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
			artefact.setTitle("paged-artefact-" + i);
			artefact = epFrontendManager.updateArtefact(artefact);
			epFrontendManager.addArtefactToStructure(ident1, artefact, structureEl);
			children.add(artefact);
		}
		dbInstance.commitAndCloseSession();
		
		List<AbstractArtefact> childrenSubset = epStructureManager.getArtefacts(structureEl, 15, 10);
		assertNotNull(childrenSubset);
		assertEquals(5, childrenSubset.size());
		assertEquals(children.get(15).getKey(), childrenSubset.get(0).getKey());
		assertEquals(children.get(16).getKey(), childrenSubset.get(1).getKey());
		assertEquals(children.get(17).getKey(), childrenSubset.get(2).getKey());
		assertEquals(children.get(18).getKey(), childrenSubset.get(3).getKey());
		assertEquals(children.get(19).getKey(), childrenSubset.get(4).getKey());
	}
	
	@Test
	public void moveUpArtefact() {
		//save parent and 5 children
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-up-structure-el-1", "move-up-structure-element");
		
		List<AbstractArtefact> children = new ArrayList<>();
		for(int i=0;i<5;i++) {
			AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
			artefact.setTitle("move-up-artefact-1-" + i);
			artefact = epFrontendManager.updateArtefact(artefact);
			epFrontendManager.addArtefactToStructure(ident1, artefact, structureEl);
			children.add(artefact);
		}
		dbInstance.commitAndCloseSession();
		
		//move up the first place
		epStructureManager.moveUp(structureEl, children.get(0));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren1 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren1);
		assertEquals(5, persistedChildren1.size());
		assertEquals(children.get(0).getKey(), persistedChildren1.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren1.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren1.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren1.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren1.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move the second to the first place
		epStructureManager.moveUp(structureEl, children.get(1));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren2 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren2);
		assertEquals(5, persistedChildren2.size());
		assertEquals(children.get(1).getKey(), persistedChildren2.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren2.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren2.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren2.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren2.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move up the last
		epStructureManager.moveUp(structureEl, children.get(4));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren3 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren3);
		assertEquals(5, persistedChildren3.size());
		assertEquals(children.get(1).getKey(), persistedChildren3.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren3.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren3.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren3.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren3.get(4).getKey());
	}
	
	@Test
	public void moveDownArtefact() {
		//save parent and 5 children
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-down-structure-el-1", "move-down-structure-element");
		
		List<AbstractArtefact> children = new ArrayList<>();
		for(int i=0;i<5;i++) {
			AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
			artefact.setTitle("move-down-artefact-1-" + i);
			artefact = epFrontendManager.updateArtefact(artefact);
			epFrontendManager.addArtefactToStructure(ident1, artefact, structureEl);
			children.add(artefact);
		}
		dbInstance.commitAndCloseSession();
		
		//move down the last
		epStructureManager.moveDown(structureEl, children.get(4));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren1 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren1);
		assertEquals(5, persistedChildren1.size());
		assertEquals(children.get(0).getKey(), persistedChildren1.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren1.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren1.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren1.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren1.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move to the last place
		epStructureManager.moveDown(structureEl, children.get(3));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren2 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren2);
		assertEquals(5, persistedChildren2.size());
		assertEquals(children.get(0).getKey(), persistedChildren2.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren2.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren2.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren2.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren2.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move down the first
		epStructureManager.moveDown(structureEl, children.get(0));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<AbstractArtefact> persistedChildren3 = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(persistedChildren3);
		assertEquals(5, persistedChildren3.size());
		assertEquals(children.get(1).getKey(), persistedChildren3.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren3.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren3.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren3.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren3.get(4).getKey());
	}
	
	@Test
	public void collectRestrictionTestByAdding() {
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-down-structure-el-1", "move-down-structure-element");
		epStructureManager.addCollectRestriction(structureEl, ForumArtefact.FORUM_ARTEFACT_TYPE, RestrictionsConstants.MAX, 2);
		epStructureManager.savePortfolioStructure(structureEl);
		dbInstance.commitAndCloseSession();
		
		//check collect restriction
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, ForumArtefact.FORUM_ARTEFACT_TYPE);
		artefact1.setTitle("collect-restriction-1");
		assertTrue(epFrontendManager.addArtefactToStructure(ident1, artefact1, structureEl));
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		artefact2.setTitle("collect-restriction-2");
		assertFalse(epFrontendManager.addArtefactToStructure(ident1, artefact2, structureEl));
		
		AbstractArtefact artefact3 = epFrontendManager.createAndPersistArtefact(ident1, ForumArtefact.FORUM_ARTEFACT_TYPE);
		artefact3.setTitle("collect-restriction-3");
		assertTrue(epFrontendManager.addArtefactToStructure(ident1, artefact3, structureEl));
		
		AbstractArtefact artefact4 = epFrontendManager.createAndPersistArtefact(ident1, ForumArtefact.FORUM_ARTEFACT_TYPE);
		artefact4.setTitle("collect-restriction-4");
		assertFalse(epFrontendManager.addArtefactToStructure(ident1, artefact4, structureEl));
		dbInstance.commitAndCloseSession();
		
		//check if the artefacts are really what we want
		List<AbstractArtefact> retrievedArtefacts = epFrontendManager.getArtefacts(structureEl);
		assertNotNull(retrievedArtefacts);
		assertEquals(2, retrievedArtefacts.size());
		
		//2 forums artefact, is ok
		assertTrue(epFrontendManager.checkCollectRestriction(structureEl));
	}
	
	@Test
	public void collectRestrictionTestAfterMin() {
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-down-structure-el-1", "move-down-structure-element");
		epStructureManager.addCollectRestriction(structureEl, ForumArtefact.FORUM_ARTEFACT_TYPE, RestrictionsConstants.MIN, 2);
		epStructureManager.savePortfolioStructure(structureEl);
		dbInstance.commitAndCloseSession();
		
		//try to add some artefacts
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		artefact1.setTitle("collect-restriction-1");
		assertTrue(epFrontendManager.addArtefactToStructure(ident1, artefact1, structureEl));
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		artefact2.setTitle("collect-restriction-2");
		assertFalse(epFrontendManager.addArtefactToStructure(ident1, artefact2, structureEl));
		dbInstance.commitAndCloseSession();
		
		//check if the structure element is not ok, need two artefacts, there is only one
		assertFalse(epFrontendManager.checkCollectRestriction(structureEl));
	}
	
	@Test
	public void collectRestrictionTestAfterMax() {
		PortfolioStructure structureEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-down-structure-el-1", "move-down-structure-element");
		epStructureManager.addCollectRestriction(structureEl, ForumArtefact.FORUM_ARTEFACT_TYPE, RestrictionsConstants.MAX, 2);
		epStructureManager.savePortfolioStructure(structureEl);
		dbInstance.commitAndCloseSession();
		
		//try to add some artefacts
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		artefact1.setTitle("collect-restriction-1");
		assertTrue(epFrontendManager.addArtefactToStructure(ident1, artefact1, structureEl));
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		artefact2.setTitle("collect-restriction-2");
		assertFalse(epFrontendManager.addArtefactToStructure(ident1, artefact2, structureEl));
		dbInstance.commitAndCloseSession();
		
		//check if the structure element is ok, it must be
		assertTrue(epFrontendManager.checkCollectRestriction(structureEl));
	}
}
