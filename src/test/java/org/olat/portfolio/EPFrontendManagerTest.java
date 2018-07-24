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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPMapPolicy;
import org.olat.portfolio.manager.EPMapPolicy.Type;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This an integration test for the frontend manager. The goal of this test is
 * to check the hibernate mapping and the constraints on the database too and
 * not only the manager.
 * 
 * This will only test methods, which are really implemented in the
 * frontendManager and not the ones which mainly pass through artefact- or
 * structure- manager
 * 
 * <P>
 * Initial Date: 28 jun. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public class EPFrontendManagerTest extends OlatTestCase {
	
	private static Identity ident1, ident2, ident3;
	private static boolean isInitialized = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EPFrontendManager epFrontendManager;
	@Autowired
	private EPStructureManager epStructureManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser("frtuse-1");
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser("frtuse-2");
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser("frtuse-3");
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(dbInstance);
		assertNotNull(epFrontendManager);
		assertNotNull(securityManager);
	}
	
	@Test
	public void testAssignMapTemplateToUser() {
		//create a template
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
		//create a repository entry
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(ident1, null, "-", "template-1", "map-template-1", resource,
				RepositoryEntryStatusEnum.preparation, defOrganisation);
		dbInstance.commitAndCloseSession();
		//create the template owned by ident1
		PortfolioStructureMap templateEl = epStructureManager.createAndPersistPortfolioMapTemplateFromEntry(ident1, addedEntry);
		//first page
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(templateEl, "template-page-1", "template-page-1");
		//structure element 1 from page 1
		PortfolioStructure struct11 = epFrontendManager.createAndPersistPortfolioStructureElement(page1, "template-structure-1.1", "template-structure-1.1");
		epStructureManager.addCollectRestriction(struct11, "Forum", "minimum", 2);
		epStructureManager.savePortfolioStructure(struct11);
		//structure element 2 from page 1
		PortfolioStructure struct12 = epFrontendManager.createAndPersistPortfolioStructureElement(page1, "template-structure-1.2", "template-structure-1.2");
		assertNotNull(struct12);
		//first page
		PortfolioStructure page2 = epFrontendManager.createAndPersistPortfolioPage(templateEl, "template-page-2", "template-page-2");
		//structure element 1 from page 2
		PortfolioStructure struct21 = epFrontendManager.createAndPersistPortfolioStructureElement(page2, "template-structure-2.1", "template-structure-2.1");
		epStructureManager.addCollectRestriction(struct21, "bc", "maximum", 4);
		epStructureManager.savePortfolioStructure(struct21);
		//save the template
		dbInstance.commitAndCloseSession();
		
		
		//make the copy
		PortfolioStructureMap map = epFrontendManager.assignStructuredMapToUser(ident2, templateEl, addedEntry, null, null, null);
		dbInstance.commitAndCloseSession();
		assertNotNull(map);
		
		//check the copy
		PortfolioStructure retrievedMap = epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		assertNotNull(retrievedMap);
		assertTrue(retrievedMap instanceof EPStructuredMap);
		assertNotNull(((EPStructuredMap)retrievedMap).getStructuredMapSource());
		assertEquals(templateEl.getKey(), ((EPStructuredMap)retrievedMap).getStructuredMapSource().getKey());

		
		//check pages of the copied map
		List<PortfolioStructure> pages = epFrontendManager.loadStructureChildren(retrievedMap);
		assertNotNull(pages);
		assertEquals(2, pages.size());
		assertTrue(pages.get(0) instanceof EPPage);
		assertTrue(pages.get(1) instanceof EPPage);
		assertEquals("template-page-1", ((EPStructureElement)pages.get(0)).getTitle());
		assertEquals("template-page-2", ((EPStructureElement)pages.get(1)).getTitle());
		//check root
		assertNotNull(((EPStructureElement)pages.get(0)).getRoot());
		assertEquals(retrievedMap.getKey(), ((EPStructureElement)pages.get(0)).getRoot().getKey());
		
		//check children of the pages
		List<PortfolioStructure> structs1 = epFrontendManager.loadStructureChildren(pages.get(0));
		assertNotNull(structs1);
		assertEquals(2, structs1.size());
		assertTrue(structs1.get(0) instanceof EPStructureElement);
		assertTrue(structs1.get(1) instanceof EPStructureElement);
		EPStructureElement struct11El = (EPStructureElement)structs1.get(0);
		assertEquals("template-structure-1.1", struct11El.getTitle());
		assertEquals("template-structure-1.2", ((EPStructureElement)structs1.get(1)).getTitle());
		//check root
		assertNotNull(((EPStructureElement)structs1.get(0)).getRoot());
		assertEquals(retrievedMap.getKey(), ((EPStructureElement)structs1.get(0)).getRoot().getKey());
		assertNotNull(((EPStructureElement)structs1.get(1)).getRoot());
		assertEquals(retrievedMap.getKey(), ((EPStructureElement)structs1.get(1)).getRoot().getKey());
		//check collect restriction
		assertNotNull(struct11El.getCollectRestrictions());
		assertEquals("Forum", struct11El.getCollectRestrictions().get(0).getArtefactType());
		assertEquals("minimum", struct11El.getCollectRestrictions().get(0).getRestriction());
		assertEquals(2, struct11El.getCollectRestrictions().get(0).getAmount());

		List<PortfolioStructure> structs2 = epFrontendManager.loadStructureChildren(pages.get(1));
		assertNotNull(structs2);
		assertEquals(1, structs2.size());
		assertTrue(structs2.get(0) instanceof EPStructureElement);
		EPStructureElement struct21El = (EPStructureElement)structs2.get(0);
		assertEquals("template-structure-2.1", struct21El.getTitle());
		//check root
		assertNotNull(struct21El.getRoot());
		assertEquals(retrievedMap.getKey(), struct21El.getRoot().getKey());
		//check collect restriction
		assertNotNull(struct21El.getCollectRestrictions());
		assertEquals("bc", struct21El.getCollectRestrictions().get(0).getArtefactType());
		assertEquals("maximum", struct21El.getCollectRestrictions().get(0).getRestriction());
		assertEquals(4, struct21El.getCollectRestrictions().get(0).getAmount());
	}
	
	@Test
	public void testSyncMapTemplateToUserMap() {
		//create a template
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
		//create a repository entry
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(ident1, null, "-", "Template in user", "Template in use", resource,
				RepositoryEntryStatusEnum.preparation, defOrganisation);
		//create the template owned by ident1
		PortfolioStructureMap templateEl = epStructureManager.createAndPersistPortfolioMapTemplateFromEntry(ident1, addedEntry);
		//create five pages
		List<PortfolioStructure> pageRefs = new ArrayList<PortfolioStructure>();
		List<PortfolioStructure> elementRefs = new ArrayList<PortfolioStructure>();
		
		for(int i=0; i<5; i++) {
			PortfolioStructure page = epFrontendManager.createAndPersistPortfolioPage(templateEl, "sync-template-page-" + i, "sync-template-page-" + i);
			pageRefs.add(page);
			
			for(int j=0; j<5; j++) {
				PortfolioStructure struct = epFrontendManager.createAndPersistPortfolioStructureElement(page, "template-structure-" + i + "." + j, "template-structure-" + i + "." + j);
				epStructureManager.addCollectRestriction(struct, "Forum", "minimum", 2);
				epStructureManager.savePortfolioStructure(struct);
				elementRefs.add(struct);
			}
		}
		//save the template
		dbInstance.commitAndCloseSession();
		//end create template
		//////////////////////

		//make the copy
		PortfolioStructureMap map = epFrontendManager.assignStructuredMapToUser(ident2, templateEl, addedEntry, null, null, null);
		dbInstance.commitAndCloseSession();
		assertNotNull(map);
		
		//////////////////////////////////
		//shuffle the pages and delete one
		PortfolioStructure retrievedTemplateEl = epFrontendManager.loadPortfolioStructureByKey(templateEl.getKey());
		List<PortfolioStructure> pages = epFrontendManager.loadStructureChildren(retrievedTemplateEl);
		epStructureManager.moveUp(retrievedTemplateEl, pages.get(1));
		epStructureManager.moveDown(retrievedTemplateEl, pages.get(2));
		epStructureManager.removeStructure(retrievedTemplateEl, pages.get(3));
		epStructureManager.savePortfolioStructure(retrievedTemplateEl);
		//shuffle a page 
		List<PortfolioStructure> page1Children = epFrontendManager.loadStructureChildren(pages.get(1));
		epStructureManager.moveUp(pages.get(1), page1Children.get(3));
		epStructureManager.moveUp(pages.get(1), page1Children.get(2));
		epStructureManager.moveUp(pages.get(1), page1Children.get(4));
		//and add an element and sub-elements
		PortfolioStructure newStruct = epFrontendManager.createAndPersistPortfolioStructureElement(pages.get(1), "new-template-structure-1.6", "template-structure-1.6");
		epStructureManager.addCollectRestriction(pages.get(1), "Forum", "minimum", 2);
		epStructureManager.savePortfolioStructure(newStruct);
		epStructureManager.savePortfolioStructure(pages.get(1));
		for(int k=0; k<5; k++) {
			PortfolioStructure struct = epFrontendManager.createAndPersistPortfolioStructureElement(newStruct, "new-template-structure-2." + k, "template-structure-2." + k);
			epStructureManager.addCollectRestriction(struct, "bc", "minimum", 2);
			epStructureManager.savePortfolioStructure(struct);
			elementRefs.add(struct);
		}
		dbInstance.commitAndCloseSession();
		//end shuffle the pages
		//////////////////////////////////
		
		////////////////////
		//check the template
		PortfolioStructure retrievedTemplate2El = epFrontendManager.loadPortfolioStructureByKey(templateEl.getKey());
		assertNotNull(retrievedTemplate2El);
		assertTrue(retrievedTemplate2El instanceof EPStructuredMapTemplate);
		List<PortfolioStructure> retrievedPages2 = epFrontendManager.loadStructureChildren(retrievedTemplate2El);
		assertEquals(4, retrievedPages2.size());
		assertEquals(4, ((EPStructuredMapTemplate)retrievedTemplate2El).getInternalChildren().size());
		//check the shuffled pages
		assertEquals(pageRefs.get(1).getKey(), retrievedPages2.get(0).getKey());
		assertEquals(pageRefs.get(0).getKey(), retrievedPages2.get(1).getKey());
		assertEquals(pageRefs.get(2).getKey(), retrievedPages2.get(2).getKey());
		assertEquals(pageRefs.get(4).getKey(), retrievedPages2.get(3).getKey());
		//check added element
		List<PortfolioStructure> retrievedChildren2 = epFrontendManager.loadStructureChildren(retrievedPages2.get(0));
		assertEquals(6, retrievedChildren2.size());
		
		dbInstance.commitAndCloseSession();
		//check the template
		////////////////////
		

		//sync the map
		epFrontendManager.synchronizeStructuredMapToUserCopy(map);
		dbInstance.commitAndCloseSession();
		
		
		/////////////////
		//check the sync
		PortfolioStructure synchedMap = epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		assertNotNull(synchedMap);
		assertTrue(synchedMap instanceof EPStructuredMap);
		List<PortfolioStructure> synchedPages = epFrontendManager.loadStructureChildren(synchedMap);
		assertNotNull(synchedPages);
		assertEquals(4, synchedPages.size());
		assertEquals(((EPStructureElement)pageRefs.get(1)).getTitle(), ((EPStructureElement)synchedPages.get(0)).getTitle());
		assertEquals(((EPStructureElement)pageRefs.get(0)).getTitle(), ((EPStructureElement)synchedPages.get(1)).getTitle());
		assertEquals(((EPStructureElement)pageRefs.get(2)).getTitle(), ((EPStructureElement)synchedPages.get(2)).getTitle());
		assertEquals(((EPStructureElement)pageRefs.get(4)).getTitle(), ((EPStructureElement)synchedPages.get(3)).getTitle());
		
		//check synched key
		assertEquals(((EPStructureElement)pageRefs.get(1)).getKey(), ((EPStructureElement)synchedPages.get(0)).getStructureElSource());
		assertEquals(((EPStructureElement)pageRefs.get(0)).getKey(), ((EPStructureElement)synchedPages.get(1)).getStructureElSource());
		assertEquals(((EPStructureElement)pageRefs.get(2)).getKey(), ((EPStructureElement)synchedPages.get(2)).getStructureElSource());
		assertEquals(((EPStructureElement)pageRefs.get(4)).getKey(), ((EPStructureElement)synchedPages.get(3)).getStructureElSource());
		
		//check the new elements
		List<PortfolioStructure> retrievedPage1Children = epFrontendManager.loadStructureChildren(synchedPages.get(0));
		assertEquals(6, retrievedPage1Children.size());
		PortfolioStructure retrievedNewStruct = retrievedPage1Children.get(5);
		assertEquals("new-template-structure-1.6", ((EPStructureElement)retrievedNewStruct).getTitle());
		List<PortfolioStructure> retrievedNewStructChildren = epFrontendManager.loadStructureChildren(retrievedNewStruct);
		assertNotNull(retrievedNewStructChildren);
		assertEquals(5, retrievedNewStructChildren.size());
		for(int k=0; k<5; k++) {
			assertEquals("new-template-structure-2." + k, ((EPStructureElement)retrievedNewStructChildren.get(k)).getTitle());
		}
		//end check the sync
		////////////////////
	}
	
	@Test
	public void testTaggingOfArtefacts() {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		dbInstance.commitAndCloseSession();
		assertNotNull(artefact);
		
		epFrontendManager.setArtefactTag(ident1, artefact, "Hello");
		epFrontendManager.setArtefactTag(ident2, artefact, "Hello");
		epFrontendManager.setArtefactTag(ident2, artefact, "Tchao");
		dbInstance.commitAndCloseSession();
		
		List<String> tags = epFrontendManager.getArtefactTags(artefact);
		assertNotNull(tags);
		assertEquals(2, tags.size());
		assertTrue(tags.get(0).equals("Hello") || tags.get(1).equals("Hello"));
		assertTrue(tags.get(0).equals("Tchao") || tags.get(1).equals("Tchao"));
	}
	
	@Test
	public void testCopyMap() {
		//create two artefacts
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "text");
		assertNotNull(artefact1);
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		assertNotNull(artefact2);
		
		dbInstance.commitAndCloseSession();
		
		//create a map with a page and the page has two artefacts
		PortfolioStructureMap originalMap = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title", "Description");
		PortfolioStructure newPage = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		boolean successfullLink1 = epFrontendManager.addArtefactToStructure(ident1, artefact1, newPage);
		assertTrue(successfullLink1);
		
		boolean successfullLink2 = epFrontendManager.addArtefactToStructure(ident1, artefact2, newPage);
		assertTrue(successfullLink2);

		dbInstance.commitAndCloseSession();
		
		//1 test: copy the map one shoot
		PortfolioStructureMap copyMap = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title copy", "Description copy");
		epFrontendManager.copyStructureRecursively(originalMap, copyMap, true);
		assertNotNull(copyMap.getKey());
		
		dbInstance.commitAndCloseSession();
		
		//2 test: copy the map two shoota
		PortfolioStructureMap copyMap2 = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title copy 2", "Description copy 2");
		dbInstance.commitAndCloseSession();
		assertNotNull(copyMap2.getKey());
		
		epFrontendManager.copyStructureRecursively(originalMap, copyMap2, true);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void closedArtefacts() {
		//create two artefacts
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "text");
		assertNotNull(artefact1);
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		assertNotNull(artefact2);
		
		dbInstance.commitAndCloseSession();
		
		//create a map with a page and the page has two artefacts
		PortfolioStructureMap originalMap = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title", "Description");
		PortfolioStructure newPage = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		
		boolean successfullLink1 = epFrontendManager.addArtefactToStructure(ident1, artefact1, newPage);
		assertTrue(successfullLink1);
		boolean successfullLink2 = epFrontendManager.addArtefactToStructure(ident1, artefact2, newPage);
		assertTrue(successfullLink2);
		
		dbInstance.commitAndCloseSession();
		
		//check if the artefact is in a closed map
		assertFalse(epFrontendManager.isArtefactClosed(artefact1));
		assertFalse(epFrontendManager.isArtefactClosed(artefact2));
		
		//closed the map artificially
		((EPDefaultMap)originalMap).setStatus(StructureStatusEnum.CLOSED);
		dbInstance.updateObject(originalMap);
		dbInstance.commitAndCloseSession();

		//check if the artefact is in a closed map
		assertTrue(epFrontendManager.isArtefactClosed(artefact1));
		assertTrue(epFrontendManager.isArtefactClosed(artefact2));
	}
	
	@Test
	public void isArtefactsInStructure() {
		
		//create two artefacts
		AbstractArtefact artefact1 = epFrontendManager.createAndPersistArtefact(ident1, "text");
		assertNotNull(artefact1);
		
		AbstractArtefact artefact2 = epFrontendManager.createAndPersistArtefact(ident1, "bc");
		assertNotNull(artefact2);
		
		dbInstance.commitAndCloseSession();
		
		//create a map with a page and the page has two artefacts
		PortfolioStructureMap originalMap = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		PortfolioStructure page2 = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		
		boolean successfullLink1 = epFrontendManager.addArtefactToStructure(ident1, artefact1, page1);
		assertTrue(successfullLink1);
		boolean successfullLink2 = epFrontendManager.addArtefactToStructure(ident1, artefact2, page2);
		assertTrue(successfullLink2);
		
		dbInstance.commitAndCloseSession();

		assertTrue(epFrontendManager.isArtefactInStructure(artefact1, page1));
		assertFalse(epFrontendManager.isArtefactInStructure(artefact1, page2));
		assertFalse(epFrontendManager.isArtefactInStructure(artefact2, page1));
		assertTrue(epFrontendManager.isArtefactInStructure(artefact2, page2));
	}
	
	@Test
	public void isMapOwner() {
		//create a map
		PortfolioStructureMap originalMap = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Title", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(originalMap, "Page title", "Page description");
		assertNotNull(page1);
		
		dbInstance.commitAndCloseSession();
		
		//check if ident1 is owner
		assertTrue(epFrontendManager.isMapOwner(ident1, originalMap.getOlatResource()));
		//check if ident2 is not owner
		assertFalse(epFrontendManager.isMapOwner(ident2, originalMap.getOlatResource()));
	}
	
	/**
	 * Same workflow as the repository. This workflow is pretty critical.
	 */
	@Test
	public void isStructuredMapOwner() {
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
		
		//create a repository entry
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(ident1, null, "-", "test repo", "desc repo", resource,
				RepositoryEntryStatusEnum.preparation, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		//create the template owned by ident1
		PortfolioStructureMap template = epStructureManager.createAndPersistPortfolioMapTemplateFromEntry(ident1, addedEntry);
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(template, "Page title", "Page description");
		assertNotNull(page1);

		dbInstance.commitAndCloseSession();
		
		//assign the template to ident2
		PortfolioStructureMap map = epFrontendManager.assignStructuredMapToUser(ident2, template, addedEntry, null, null, null);
		assertNotNull(map);
		dbInstance.commitAndCloseSession();
		
		//check if ident2 is owner of the map
		assertTrue(epFrontendManager.isMapOwner(ident2, map.getOlatResource()));
		//check if ident1 is not the owner of the map
		assertFalse(epFrontendManager.isMapOwner(ident1, map.getOlatResource()));
		//check if ident1 is owner of the template
		assertTrue(epFrontendManager.isMapOwner(ident1, template.getOlatResource()));
	}
	
	@Test
	public void isTemplateInUse() {
		//create a template
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
		//create a repository entry
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(ident1, null, "-", "Template in user", "Template in use", resource,
				RepositoryEntryStatusEnum.preparation, defOrganisation);
		//create the template owned by ident1
		PortfolioStructureMap template = epStructureManager.createAndPersistPortfolioMapTemplateFromEntry(ident1, addedEntry);
		dbInstance.commitAndCloseSession();
		//add a page to it
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(template, "Page title", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();
		
		//check: the template is not in use
		assertFalse(epFrontendManager.isTemplateInUse(template, null, null, null));
		
		//use the template: assign the template to ident2
		PortfolioStructureMap map = epFrontendManager.assignStructuredMapToUser(ident2, template, addedEntry, null, null, null);
		assertNotNull(map);
		dbInstance.commitAndCloseSession();
		
		//check: the template is  in use
		assertTrue(epFrontendManager.isTemplateInUse(template, null, null, null));
	}
	
	@Test
	public void saveMapPolicy() {
		//create a map
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Policies", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "Page policies", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();

		//policies are empty
		List<EPMapPolicy> policies = epFrontendManager.getMapPolicies(map);
		assertEquals(0, policies.size());//owner policy
		
		//save a user policy
		EPMapPolicy userPolicy = new EPMapPolicy();
		userPolicy.setType(Type.user);
		userPolicy.getIdentities().add(ident2);
		epFrontendManager.updateMapPolicies(map, Collections.singletonList(userPolicy));
		dbInstance.commitAndCloseSession();
		
		//one policy
		List<EPMapPolicy> policies1 = epFrontendManager.getMapPolicies(map);
		assertEquals(1, policies1.size());

		//check visiblity (is owner)
		assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
		//check visibility (is in policy)
		assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
		//check not visible (not in policy)
		assertFalse(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
	}
	
	@Test
	public void allUserPolicies() {
		//create a map
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Policies", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "Page policies", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();
		
		//check visiblity (is owner)
		assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
		//check visibility (no policy)
		assertFalse(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
		//check not visible (no policy)
		assertFalse(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));

		//add all user policy
		EPMapPolicy userPolicy = new EPMapPolicy();
		userPolicy.setType(Type.allusers);
		epFrontendManager.updateMapPolicies(map, Collections.singletonList(userPolicy));
		dbInstance.commitAndCloseSession();
		
		//one policy
		List<EPMapPolicy> policies1 = epFrontendManager.getMapPolicies(map);
		assertEquals(1, policies1.size());

		//check visiblity (is owner)
		assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
		//check visibility (is user)
		assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
		//check not visible (is user)
		assertTrue(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
	}
	
	@Test
	public void allMapPolicies() {
		//create a map
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Policies", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "Page policies", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();
		
		//save a list of policies
		List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
		
		//user policy
		EPMapPolicy userPolicy = new EPMapPolicy();
		userPolicy.setType(Type.user);
		userPolicy.getIdentities().add(ident2);
		userPolicy.getIdentities().add(ident3);
		policies.add(userPolicy);
		
		//invitation
		Invitation invitation = invitationDao.createAndPersistInvitation();
		invitation.setFirstName("John");
		invitation.setLastName("Doe");
		invitation.setMail("john@doe.ch");
		EPMapPolicy invitationPolicy = new EPMapPolicy();
		invitationPolicy.setType(Type.invitation);
		invitationPolicy.setInvitation(invitation);
		policies.add(invitationPolicy);
	
		epFrontendManager.updateMapPolicies(map, policies);
		dbInstance.commitAndCloseSession();
		
		//check visiblity (is owner)
		assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
		//check visibility (is in policy)
		assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
		//check visible (is in policy)
		assertTrue(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
		
		//retrieved policies
		List<EPMapPolicy> savedPolicies = epFrontendManager.getMapPolicies(map);
		assertTrue(!savedPolicies.isEmpty());
	}
	
	@Test
	public void removePolicyWithInvitation() {
	//create a map
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Remove policies", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "Page policies", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();
		
		//save a list of policies
		List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
		//invitation
		Invitation invitation = invitationDao.createAndPersistInvitation();
		invitation.setFirstName("John");
		invitation.setLastName("Doe");
		invitation.setMail("john2@doe.ch");
		EPMapPolicy invitationPolicy = new EPMapPolicy();
		invitationPolicy.setType(Type.invitation);
		invitationPolicy.setInvitation(invitation);
		policies.add(invitationPolicy);
		map = epFrontendManager.updateMapPolicies(map, policies);
		dbInstance.commitAndCloseSession();
		
		//remove the policy
		policies.clear();
		epFrontendManager.updateMapPolicies(map, policies);
		dbInstance.commitAndCloseSession();

		//check if the policies and the invitation are deleted
		List<EPMapPolicy> deletedPolicies = epFrontendManager.getMapPolicies(map);
		assertNotNull(deletedPolicies);
		assertTrue(deletedPolicies.isEmpty());
	}
	
	@Test
	public void mergeTwoUserPolicies() {
		//create a map
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Remove policies", "Description");
		PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "Page policies", "Page description");
		assertNotNull(page1);
		dbInstance.commitAndCloseSession();

		//save a list of policies
		List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
		//first user policy
		EPMapPolicy userPolicy1 = new EPMapPolicy();
		userPolicy1.setType(Type.user);
		userPolicy1.getIdentities().add(ident2);
		userPolicy1.getIdentities().add(ident3);
		policies.add(userPolicy1);
		//second user policy
		EPMapPolicy userPolicy2 = new EPMapPolicy();
		userPolicy2.setType(Type.user);
		userPolicy2.getIdentities().add(ident1);
		policies.add(userPolicy2);
		epFrontendManager.updateMapPolicies(map, policies);
		dbInstance.commitAndCloseSession();
		
		
		//check if the policies are correctly merged
		List<EPMapPolicy> mergedPolicies = epFrontendManager.getMapPolicies(map);
		assertNotNull(mergedPolicies);
		assertEquals(1, mergedPolicies.size());
		
		EPMapPolicy mergedPolicy = mergedPolicies.get(0);
		List<Identity> identities = mergedPolicy.getIdentities();
		assertEquals(3, identities.size());
		
		int count1, count2, count3;
		count1 = count2 = count3 = 0;
		for(Identity identity:identities) {
			if(identity.equalsByPersistableKey(ident1)) {
				count1++;
			} else if(identity.equalsByPersistableKey(ident2)) {
				count2++;
			} else if(identity.equalsByPersistableKey(ident3)) {
				count3++;
			}
		}
		assertEquals(1, count1);
		assertEquals(1, count2);
		assertEquals(1, count3);
	}
	
	/**
	 * Create a map with a page and an artefact. Delete it.
	 */
	@Test
	public void deleteMap_pageAndArtefact() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("frtuse-4");
		
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(id, "Delete map", "Description");
		PortfolioStructure page = epFrontendManager.createAndPersistPortfolioPage(map, "Page while be deleted", "Page description");
		assertNotNull(page);
		dbInstance.commitAndCloseSession();
		//create artefact
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(id, "Forum");
		dbInstance.commitAndCloseSession();		
		//create the link
		epFrontendManager.addArtefactToStructure(id, artefact, page);
		dbInstance.commitAndCloseSession();
		
		//reload and check
		PortfolioStructure reloadedMap = epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		Assert.assertNotNull(reloadedMap);
		Assert.assertEquals(map, reloadedMap);
		List<PortfolioStructure> reloadedPages = epFrontendManager.loadStructureChildren(reloadedMap);
		Assert.assertNotNull(reloadedPages);
		Assert.assertEquals(1, reloadedPages.size());
		PortfolioStructure reloadedPage = reloadedPages.get(0);
		Assert.assertEquals(page, reloadedPage);
		List<AbstractArtefact> reloadedArtefacts = epFrontendManager.getArtefacts(reloadedPage);
		Assert.assertNotNull(reloadedArtefacts);
		Assert.assertEquals(1, reloadedArtefacts.size());
		AbstractArtefact reloadedArtefact = reloadedArtefacts.get(0);
		Assert.assertEquals(artefact, reloadedArtefact);
		dbInstance.commitAndCloseSession();
		
		//delete the map
		epFrontendManager.deletePortfolioStructure(reloadedMap);
		dbInstance.commit();
		
		//what is deleted?
		AbstractArtefact notDeletedArtefact = epFrontendManager.loadArtefactByKey(artefact.getKey());
		Assert.assertNotNull(notDeletedArtefact);
		PortfolioStructure deletedMap = epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		Assert.assertNull(deletedMap);
		PortfolioStructure deletedPage = epFrontendManager.loadPortfolioStructureByKey(page.getKey());
		Assert.assertNull(deletedPage);
	}
	
	/**
	 * Delete a map with policies
	 */
	@Test
	public void deleteMap_pageAndPolicy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("frtuse-5");
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(id, "Delete map", "Description");
		PortfolioStructure page = epFrontendManager.createAndPersistPortfolioPage(map, "Page while be deleted", "Page description");
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(id, "Forum");
		epFrontendManager.addArtefactToStructure(id, artefact, page);
		dbInstance.commitAndCloseSession();
		//add policy
		List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
		EPMapPolicy userPolicy = new EPMapPolicy();
		userPolicy.setType(Type.user);
		userPolicy.getIdentities().add(ident2);
		userPolicy.getIdentities().add(ident3);
		policies.add(userPolicy);
		epFrontendManager.updateMapPolicies(map, policies);
		dbInstance.commitAndCloseSession();

		//reload and check
		PortfolioStructure reloadedMap = epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		Assert.assertNotNull(reloadedMap);
		OLATResource reloadedResource = reloadedMap.getOlatResource();
		Assert.assertNotNull(reloadedResource);		
		Assert.assertEquals(map, reloadedMap);
		boolean shared = epFrontendManager.isMapShared(reloadedResource);
		Assert.assertTrue(shared);
		boolean visibleToIdent2 = epFrontendManager.isMapVisible(ident2, reloadedResource);
		Assert.assertTrue(visibleToIdent2);
		
		//delete the map
		epFrontendManager.deletePortfolioStructure(reloadedMap);
		dbInstance.commit();
		boolean deletedShared = epFrontendManager.isMapShared(reloadedResource);
		Assert.assertFalse(deletedShared);
		boolean deletedVisibleToIdent2 = epFrontendManager.isMapVisible(ident2, reloadedResource);
		Assert.assertFalse(deletedVisibleToIdent2);
	}
	
	/**
	 * Delete a portfolio template
	 */
	@Test
	public void deleteMap_template() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("frtuse-6");
		//save parent and 20 children
		
		OLATResource resource = epStructureManager.createPortfolioMapTemplateResource();
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(id, null, "", "Template to delete", "", resource,
				RepositoryEntryStatusEnum.preparation, defOrganisation);
		PortfolioStructureMap template = epStructureManager.createAndPersistPortfolioMapTemplateFromEntry(id, re);
		PortfolioStructure page = epFrontendManager.createAndPersistPortfolioPage(template, "Page while be deleted", "Page description");
		dbInstance.commitAndCloseSession();
		
		//reload and check
		PortfolioStructure reloadedTemplate = epFrontendManager.loadPortfolioStructureByKey(template.getKey());
		Assert.assertNotNull(reloadedTemplate);
		OLATResource reloadedResource = reloadedTemplate.getOlatResource();
		Assert.assertNotNull(reloadedResource);		
		Assert.assertEquals(template, reloadedTemplate);
		List<PortfolioStructure> reloadedPages = epFrontendManager.loadStructureChildren(reloadedTemplate);
		Assert.assertNotNull(reloadedPages);
		Assert.assertEquals(1, reloadedPages.size());
		Assert.assertEquals(page, reloadedPages.get(0));

		//delete
		RepositoryEntry reloadedRe = repositoryService.loadByKey(re.getKey());
		Roles roles = Roles.administratorRoles();
		repositoryService.deletePermanently(reloadedRe, id, roles, Locale.GERMAN);
		dbInstance.commit();	
	}
}
