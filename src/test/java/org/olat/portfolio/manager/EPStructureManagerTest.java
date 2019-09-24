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

package org.olat.portfolio.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.core.id.Roles;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureElementToGroupRelation;
import org.olat.portfolio.model.structel.EPStructureToStructureLink;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.EPTargetResource;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Integration test for the DB
 * 
 * <P>
 * Initial Date:  24 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class EPStructureManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EPStructureManager epStructureManager;
	@Autowired
	private EPFrontendManager epFrontendManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	
	private static Identity ident1, ident2;
	private static boolean isInitialized = false;
	
	@Before
	public void setUp() {
		if(!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		}
	}
	
	@Test
	public void testManagers() {
		assertNotNull(dbInstance);
		assertNotNull(epStructureManager);
	}
	
	@Test
	public void testGetStructureElementsForUser() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-1-");
		
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "users-test-map", "a-map-to-test-get-afterwards");
		Assert.assertNotNull(el);
		dbInstance.commitAndCloseSession();
		
		List<PortfolioStructure> elRes = epStructureManager.getStructureElementsForUser(user, ElementType.DEFAULT_MAP);
		Assert.assertNotNull(elRes);
		Assert.assertEquals(1, elRes.size());
		Assert.assertEquals("users-test-map", elRes.get(0).getTitle());

		// get another map
		PortfolioStructure el2 = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "users-test-map-2", "2-a-map-to-test-get-afterwards");
		Assert.assertNotNull(el2);
		dbInstance.commitAndCloseSession();
		List<PortfolioStructure> elRes2 = epStructureManager.getStructureElementsForUser(user);
		Assert.assertNotNull(elRes2);
		Assert.assertEquals(2, elRes2.size());
	}
	
	@Test
	public void testGetStructureElementsForUser_byElementTypes() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-1-");
		
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioDefaultMap(user, "users-def-map", "");
		Assert.assertNotNull(el);
		dbInstance.commitAndCloseSession();
		
		//by default map
		List<PortfolioStructure> defaultRes = epStructureManager.getStructureElementsForUser(user, ElementType.DEFAULT_MAP);
		Assert.assertNotNull(defaultRes);
		Assert.assertEquals(1, defaultRes.size());
		Assert.assertEquals("users-def-map", defaultRes.get(0).getTitle());
		
		//by default map and structured
		List<PortfolioStructure> multipleRes = epStructureManager.getStructureElementsForUser(user, ElementType.DEFAULT_MAP, ElementType.STRUCTURED_MAP);
		Assert.assertNotNull(multipleRes);
		Assert.assertEquals(1, multipleRes.size());
		Assert.assertEquals("users-def-map", multipleRes.get(0).getTitle());
		
		//by structured
		List<PortfolioStructure> structuredRes = epStructureManager.getStructureElementsForUser(user, ElementType.STRUCTURED_MAP);
		Assert.assertNotNull(structuredRes);
		Assert.assertTrue(structuredRes.isEmpty());
	}
	
	@Test
	public void testGetReferencedMapsForArtefact() {
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el", "structure-element");
		dbInstance.commitAndCloseSession();
		
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, "Forum");
		epFrontendManager.addArtefactToStructure(ident1, artefact, el);
		dbInstance.commitAndCloseSession();
		
		//get the referenced maps
		List<PortfolioStructure> mapList = epFrontendManager.getReferencedMapsForArtefact(artefact);
		assertTrue(((Persistable)el).equalsByPersistableKey((Persistable)mapList.get(0)));
		dbInstance.commitAndCloseSession();
		
		//make the test more complex
		//reload the structure element
		el = epFrontendManager.loadPortfolioStructureByKey(el.getKey());
		// add artefact to substructure (page) and check for the same map
		PortfolioStructure childEl = epFrontendManager.createAndPersistPortfolioStructureElement(el, "child-structure-el", "child-structure-element");
		el = epFrontendManager.removeArtefactFromStructure(artefact, el);
		
		epFrontendManager.addArtefactToStructure(ident1, artefact, childEl);
		dbInstance.commitAndCloseSession();
		
		//get the referenced maps
		List<PortfolioStructure> mapList2 = epFrontendManager.getReferencedMapsForArtefact(artefact);
		assertTrue(((Persistable)el).equalsByPersistableKey((Persistable)mapList2.get(0)));
		dbInstance.commitAndCloseSession();
		
		// add artefact to 3 maps and check to get all of them
		PortfolioStructure el2 = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el-2", "structure-element-2");
		epFrontendManager.addArtefactToStructure(ident1, artefact, el2);
		
		PortfolioStructure el3 = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el-3", "structure-element-3");
		epFrontendManager.addArtefactToStructure(ident1, artefact, el3);
		
		List<PortfolioStructure> mapList3 = epFrontendManager.getReferencedMapsForArtefact(artefact);
		assertEquals(3, mapList3.size());
		boolean found = false;
		for(PortfolioStructure mapValue:mapList3) {
			if(((Persistable)mapValue).equalsByPersistableKey((Persistable)el)) {
				found= true;
			}
		}
		assertTrue(found);
	}
	
	@Test
	public void testCreateAndSaveElement() {
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el", "structure-element");
		dbInstance.commitAndCloseSession();
		
		assertNotNull(el);
		assertNotNull(el.getOlatResource());
		
		PortfolioStructure retrievedEl = epFrontendManager.loadPortfolioStructureByKey(el.getKey());
		assertNotNull(retrievedEl);
		assertNotNull(retrievedEl.getOlatResource());
		
		OLATResource resource = resourceManager.findResourceable(el.getResourceableId(), el.getResourceableTypeName());
		assertNotNull(resource);
	}
	
	@Test
	public void testCreateAndSaveTreeOfElements() {
		//test save parent and child
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "parent-structure-el", "parent-structure-element");
		PortfolioStructure childEl = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "child-structure-el", "child-structure-element");
		dbInstance.commitAndCloseSession();
		
		//test load by key
		PortfolioStructure retrievedParentEl = epFrontendManager.loadPortfolioStructureByKey(parentEl.getKey());
		assertNotNull(retrievedParentEl);
		assertNotNull(retrievedParentEl.getOlatResource());
		
		//test load by key
		PortfolioStructure retrievedChildEl = epFrontendManager.loadPortfolioStructureByKey(childEl.getKey());
		PortfolioStructure retrievedParentEl2 = epFrontendManager.loadStructureParent(retrievedChildEl);
		assertNotNull(retrievedChildEl);
		assertNotNull(retrievedChildEl.getOlatResource());
		assertNotNull(retrievedParentEl2);
		assertEquals(parentEl.getKey(), retrievedParentEl2.getKey());
		dbInstance.commitAndCloseSession();
		
		//test get children
		List<PortfolioStructure> retrievedChilrenEl = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(retrievedChilrenEl);
		assertEquals(1, retrievedChilrenEl.size());
		assertEquals(childEl.getKey(), retrievedChilrenEl.get(0).getKey());
		assertNotNull(((EPStructureElement)retrievedChilrenEl.get(0)).getRoot());
		assertEquals(parentEl.getKey(), ((EPStructureElement)retrievedChilrenEl.get(0)).getRoot().getKey());
	}
	
	@Test
	public void testCreateAndRetrieveElement() {
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el-2", "structure-element-2");
		dbInstance.commitAndCloseSession();
		
		PortfolioStructure el2 = epStructureManager.loadPortfolioStructure(el.getOlatResource());
		assertNotNull(el2);
	}
	
	@Test
	public void testCreateAndRetrieveCollectRestrictionElement() {
		PortfolioStructure el = epFrontendManager.createAndPersistPortfolioStructureElement(null, "structure-el-3", "structure-element-3");
		epStructureManager.addCollectRestriction(el, "Forum", "minimum", 3);
		epStructureManager.savePortfolioStructure(el);
		dbInstance.commitAndCloseSession();
		
		PortfolioStructure retrievedEl = epStructureManager.loadPortfolioStructure(el.getOlatResource());
		assertNotNull(retrievedEl);
		assertTrue(retrievedEl instanceof EPStructureElement);
		EPStructureElement retrievedStructEl = (EPStructureElement)retrievedEl;
		assertNotNull(retrievedStructEl.getCollectRestrictions());
		assertEquals("Forum", retrievedStructEl.getCollectRestrictions().get(0).getArtefactType());
		assertEquals("minimum", retrievedStructEl.getCollectRestrictions().get(0).getRestriction());
		assertEquals(3, retrievedStructEl.getCollectRestrictions().get(0).getAmount());
	}
	
	@Test
	public void testChildrenBetweenSeveralSessions() {
		//test save parent and child
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "parent-structure-el", "parent-structure-element");
		PortfolioStructure childEl1 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "multi-session-structure-el-1", "child-structure-element");
		dbInstance.commitAndCloseSession();

		PortfolioStructure childEl2 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "multi-session-structure-el-2", "child-structure-element");
		dbInstance.commitAndCloseSession();
		
		PortfolioStructure childEl3 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "multi-session-structure-el-3", "child-structure-element");
		((EPStructureElement)parentEl).setTitle("parent-structure-el-prime");
		epStructureManager.savePortfolioStructure(parentEl);
		dbInstance.commitAndCloseSession();	
		
		//test if all children are saved
		List<PortfolioStructure> retrievedChilrenEl = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(retrievedChilrenEl);
		assertEquals(3, retrievedChilrenEl.size());
		//test if they are ordered
		assertEquals(childEl1.getKey(), retrievedChilrenEl.get(0).getKey());
		assertEquals(childEl2.getKey(), retrievedChilrenEl.get(1).getKey());
		assertEquals(childEl3.getKey(), retrievedChilrenEl.get(2).getKey());
		//test the title too (why not?)
		assertEquals("multi-session-structure-el-1", ((EPStructureElement)retrievedChilrenEl.get(0)).getTitle());
		assertEquals("multi-session-structure-el-2", ((EPStructureElement)retrievedChilrenEl.get(1)).getTitle());
		assertEquals("multi-session-structure-el-3", ((EPStructureElement)retrievedChilrenEl.get(2)).getTitle());
		
		//test if the change to the parent was not lost
		PortfolioStructure retrievedParentEl = epFrontendManager.loadPortfolioStructureByKey(parentEl.getKey());
		assertEquals("parent-structure-el-prime", ((EPStructureElement)retrievedParentEl).getTitle());
		dbInstance.commitAndCloseSession();
		
		//test that the children are not always loaded
		PortfolioStructure retrievedParentEl2 = epFrontendManager.loadPortfolioStructureByKey(parentEl.getKey());
		dbInstance.commitAndCloseSession();
		
		boolean failedToLazyLoadChildren;
		try {
			List<EPStructureToStructureLink> children = ((EPStructureElement)retrievedParentEl2).getInternalChildren();
			failedToLazyLoadChildren = (children == null || children.isEmpty());
		} catch(Exception e) {
			failedToLazyLoadChildren = true;
		}
		assertTrue(failedToLazyLoadChildren);
		dbInstance.commitAndCloseSession();
		
		//test load parent
		PortfolioStructure retrievedParentEl3 = epFrontendManager.loadStructureParent(childEl1);
		assertNotNull(retrievedParentEl3);
		assertEquals(parentEl.getKey(), retrievedParentEl3.getKey());
		PortfolioStructure retrievedParentEl4 = epFrontendManager.loadStructureParent(childEl2);
		assertNotNull(retrievedParentEl4);
		assertEquals(parentEl.getKey(), retrievedParentEl4.getKey());
		PortfolioStructure retrievedParentEl5 = epFrontendManager.loadStructureParent(childEl3);
		assertNotNull(retrievedParentEl5);
		assertEquals(parentEl.getKey(), retrievedParentEl5.getKey());
	}
	
	@Test
	public void testDeleteChildren() {
		//test save parent and 3 children
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "remove-parent-structure-el", "parent-structure-element");
		PortfolioStructure childEl1 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "remove-structure-el-1", "remove-child-structure-element");
		PortfolioStructure childEl2 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "remove-structure-el-2", "remove-child-structure-element");
		PortfolioStructure childEl3 = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "remove-structure-el-3", "remove-child-structure-element");
		dbInstance.commitAndCloseSession();
		
		//remove a child
		epStructureManager.removeStructure(parentEl, childEl2);
		dbInstance.commitAndCloseSession();
		
		//check if the structure element has been removed
		List<PortfolioStructure> retrievedChildrenEl = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(retrievedChildrenEl);
		assertEquals(2, retrievedChildrenEl.size());
		assertEquals(childEl1.getKey(), retrievedChildrenEl.get(0).getKey());
		assertEquals(childEl3.getKey(), retrievedChildrenEl.get(1).getKey());
	}
	
	@Test
	public void testChildrenPaging() {
		//save parent and 20 children
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "paged-parent-structure-el", "parent-structure-element");
		
		List<PortfolioStructure> children = new ArrayList<>();
		for(int i=0;i<20;i++) {
			PortfolioStructure childEl = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "paged-structure-el-" + i, "paged-child-structure-element");
			children.add(childEl);
		}
		dbInstance.commitAndCloseSession();
		
		//check if the paging return the right children
		List<PortfolioStructure> childrenSubset = epFrontendManager.loadStructureChildren(parentEl, 15, 10);
		assertNotNull(childrenSubset);
		assertEquals(5, childrenSubset.size());
		assertEquals(children.get(15).getKey(), childrenSubset.get(0).getKey());
		assertEquals(children.get(16).getKey(), childrenSubset.get(1).getKey());
		assertEquals(children.get(17).getKey(), childrenSubset.get(2).getKey());
		assertEquals(children.get(18).getKey(), childrenSubset.get(3).getKey());
		assertEquals(children.get(19).getKey(), childrenSubset.get(4).getKey());
	}
	
	@Test
	public void testCreateStructureMapTemplate() {
		//save parent and 20 children
		PortfolioStructureMap template = createPortfolioMapTemplate(ident1, "paged-parent-structure-el", "parent-structure-element");
		epStructureManager.savePortfolioStructure(template);
		dbInstance.commitAndCloseSession();
		
		//not very usefull but...
		assertNotNull(template);
		//check if the olat resource is persisted
		OLATResource resource = resourceManager.findResourceable(template.getResourceableId(), template.getResourceableTypeName());
		assertNotNull(resource);
		//check if the repository entry is persisted
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(resource, false);
		assertNotNull(re);
	}
	
	@Test
	public void testUseStructureMapTemplate() {
		//save parent and 20 children
		PortfolioStructureMap template = createPortfolioMapTemplate(ident1, "paged-parent-structure-el", "parent-structure-element");
		epStructureManager.savePortfolioStructure(template);
		dbInstance.commitAndCloseSession();
		
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioStructuredMap(template, ident1, "cloned-map", "cloned-map-from-template", null, null, null);
		
		((EPStructuredMap)map).setReturnDate(new Date());
		EPTargetResource targetResource = ((EPStructuredMap)map).getTargetResource();
		targetResource.setResourceableTypeName("CourseModule");
		targetResource.setResourceableId(234l);
		targetResource.setSubPath("3894580");
		targetResource.setBusinessPath("[RepositoryEntry:23647598][CourseNode:934598]");
		
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();
		
		//test
		PortfolioStructureMap retrievedMap = (PortfolioStructureMap)epFrontendManager.loadPortfolioStructureByKey(map.getKey());
		assertNotNull(retrievedMap);
		assertNotNull(((EPStructuredMap)retrievedMap).getReturnDate());
		assertNotNull(((EPStructuredMap)retrievedMap).getStructuredMapSource());
		assertNotNull(((EPStructuredMap)retrievedMap).getTargetResource());
		
		EPTargetResource retriviedTargetResource = ((EPStructuredMap)retrievedMap).getTargetResource();
		assertEquals("CourseModule", retriviedTargetResource.getResourceableTypeName());
		assertEquals(new Long(234l), retriviedTargetResource.getResourceableId());
		assertEquals("3894580", retriviedTargetResource.getSubPath());
		assertEquals("[RepositoryEntry:23647598][CourseNode:934598]", retriviedTargetResource.getBusinessPath());
	}
	
	@Test
	public void testLoadPortfolioStructuredMap(){
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-tmp-");
		//create a template
		PortfolioStructureMap template = createPortfolioMapTemplate(user, "paged-parent-structure-el", "parent-structure-element");
		epStructureManager.savePortfolioStructure(template);
		dbInstance.commitAndCloseSession();
		//clone the template
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioStructuredMap(template, user, "cloned-map", "cloned-map-from-template", null, null, null);
		((EPStructuredMap)map).setReturnDate(new Date());
		EPTargetResource targetResource = ((EPStructuredMap)map).getTargetResource();
		targetResource.setResourceableTypeName("CourseModule");
		targetResource.setResourceableId(234l);
		targetResource.setSubPath(UUID.randomUUID().toString());
		targetResource.setBusinessPath("[RepositoryEntry:23647599][CourseNode:934599]");
		
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();

		//load the cloned map another map
		PortfolioStructureMap myClonedMap = epStructureManager.loadPortfolioStructuredMap(user, template,
				targetResource.getOLATResourceable(), targetResource.getSubPath(), targetResource.getBusinessPath());
		Assert.assertNotNull(myClonedMap);
	}
	
	@Test
	public void testLoadPortfolioStructuredMaps() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-tmp-");
		//a template
		PortfolioStructureMap template = createPortfolioMapTemplate(user, "paged-parent-structure-el", "parent-structure-element");
		epStructureManager.savePortfolioStructure(template);
		dbInstance.commitAndCloseSession();
		//clone the template
		PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioStructuredMap(template, user, "cloned-map", "cloned-map-from-template", null, null, null);
		((EPStructuredMap)map).setReturnDate(new Date());
		EPTargetResource targetResource = ((EPStructuredMap)map).getTargetResource();
		targetResource.setResourceableTypeName("CourseModule");
		targetResource.setResourceableId(234l);
		targetResource.setSubPath(UUID.randomUUID().toString());
		targetResource.setBusinessPath("[RepositoryEntry:23647600][CourseNode:934600]");
		
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();

		//load the cloned map another map
		List<PortfolioStructureMap> myCloneAlt = epStructureManager.loadPortfolioStructuredMaps(user,
				targetResource.getOLATResourceable(), targetResource.getSubPath(), targetResource.getBusinessPath());
		Assert.assertNotNull(myCloneAlt);
		Assert.assertEquals(1, myCloneAlt.size());
		Assert.assertEquals(map, myCloneAlt.get(0));
	}
	
	@Test
	public void loadPortfolioStructure_resourceable() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-res-tmp-");
		//a template
		PortfolioStructureMap template = createPortfolioMapTemplate(user, "resourced-el", "resource-element");
		epStructureManager.savePortfolioStructure(template);
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = template.getOlatResource();
		PortfolioStructure structure = epStructureManager.loadPortfolioStructure(resource);
		Assert.assertNotNull(structure);
		Assert.assertEquals(template, structure);
	}
	
	@Test
	public void testCountStructureElementsFromOthers() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("EP-tmp-");
		
		PortfolioStructureMap map = epStructureManager.createPortfolioDefaultMap("map-el", "map-element");
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();
		
		//clone the template
		int count = epStructureManager.countStructureElementsFromOthers(user, null);
		Assert.assertEquals(0, count);
	}
	
	@Test
	public void testMoveUp() {
		//save parent and 5 children
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-up-parent-structure-el-1", "move-up-structure-element");
		
		List<PortfolioStructure> children = new ArrayList<>();
		for(int i=0;i<5;i++) {
			PortfolioStructure childEl = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "paged-structure-el-" + i, "paged-child-structure-element");
			children.add(childEl);
		}
		dbInstance.commitAndCloseSession();
		
		//check if the paging return the right children
		List<PortfolioStructure> childrenSubset = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(childrenSubset);
		assertEquals(5, childrenSubset.size());
		assertEquals(children.get(0).getKey(), childrenSubset.get(0).getKey());
		assertEquals(children.get(1).getKey(), childrenSubset.get(1).getKey());
		assertEquals(children.get(2).getKey(), childrenSubset.get(2).getKey());
		assertEquals(children.get(3).getKey(), childrenSubset.get(3).getKey());
		assertEquals(children.get(4).getKey(), childrenSubset.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move up the first place
		epStructureManager.moveUp(parentEl, children.get(0));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren1 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren1);
		assertEquals(5, persistedChildren1.size());
		assertEquals(children.get(0).getKey(), persistedChildren1.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren1.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren1.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren1.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren1.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move the second to the first place
		epStructureManager.moveUp(parentEl, children.get(1));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren2 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren2);
		assertEquals(5, persistedChildren2.size());
		assertEquals(children.get(1).getKey(), persistedChildren2.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren2.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren2.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren2.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren2.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move up the last
		epStructureManager.moveUp(parentEl, children.get(4));
		epStructureManager.savePortfolioStructure(parentEl);
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren3 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren3);
		assertEquals(5, persistedChildren3.size());
		assertEquals(children.get(1).getKey(), persistedChildren3.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren3.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren3.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren3.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren3.get(4).getKey());
	}
	

	@Test
	public void testMoveDown() {
		//save parent and 5 children
		PortfolioStructure parentEl = epFrontendManager.createAndPersistPortfolioStructureElement(null, "move-up-parent-structure-el-1", "move-up-structure-element");
		
		List<PortfolioStructure> children = new ArrayList<>();
		for(int i=0;i<5;i++) {
			PortfolioStructure childEl = epFrontendManager.createAndPersistPortfolioStructureElement(parentEl, "paged-structure-el-" + i, "paged-child-structure-element");
			children.add(childEl);
		}
		dbInstance.commitAndCloseSession();
		
		//check if the paging return the right children
		List<PortfolioStructure> childrenSubset = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(childrenSubset);
		assertEquals(5, childrenSubset.size());
		assertEquals(children.get(0).getKey(), childrenSubset.get(0).getKey());
		assertEquals(children.get(1).getKey(), childrenSubset.get(1).getKey());
		assertEquals(children.get(2).getKey(), childrenSubset.get(2).getKey());
		assertEquals(children.get(3).getKey(), childrenSubset.get(3).getKey());
		assertEquals(children.get(4).getKey(), childrenSubset.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move down the last
		epStructureManager.moveDown(parentEl, children.get(4));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren1 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren1);
		assertEquals(5, persistedChildren1.size());
		assertEquals(children.get(0).getKey(), persistedChildren1.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren1.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren1.get(2).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren1.get(3).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren1.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move down to the last place
		epStructureManager.moveDown(parentEl, children.get(3));
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren2 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren2);
		assertEquals(5, persistedChildren2.size());
		assertEquals(children.get(0).getKey(), persistedChildren2.get(0).getKey());
		assertEquals(children.get(1).getKey(), persistedChildren2.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren2.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren2.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren2.get(4).getKey());
		dbInstance.commitAndCloseSession();
		
		
		//move down the first to the second position
		epStructureManager.moveDown(parentEl, children.get(0));
		epStructureManager.savePortfolioStructure(parentEl);
		dbInstance.commitAndCloseSession();
		//check that all is the same
		List<PortfolioStructure> persistedChildren3 = epFrontendManager.loadStructureChildren(parentEl);
		assertNotNull(persistedChildren3);
		assertEquals(5, persistedChildren3.size());
		assertEquals(children.get(1).getKey(), persistedChildren3.get(0).getKey());
		assertEquals(children.get(0).getKey(), persistedChildren3.get(1).getKey());
		assertEquals(children.get(2).getKey(), persistedChildren3.get(2).getKey());
		assertEquals(children.get(4).getKey(), persistedChildren3.get(3).getKey());
		assertEquals(children.get(3).getKey(), persistedChildren3.get(4).getKey());
	}
	
	@Test
	public void testAddAuthorToMap() {
		//save the map
		PortfolioStructureMap map = createPortfolioMapTemplate(ident1, "add-author-map-1", "add-an-author-to-map-template");
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();
		
		//add an author
		epStructureManager.addAuthor(map, ident2);
		dbInstance.commitAndCloseSession();
		
		//check that the author are in the
		OLATResource resource = resourceManager.findResourceable(map.getResourceableId(), map.getResourceableTypeName());
		assertNotNull(resource);
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(resource, false);
		assertNotNull(re);
		List<Identity> authors = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		assertEquals(2, authors.size());
		assertTrue(authors.contains(ident1));//owner
		assertTrue(authors.contains(ident2));//owner
	}
	
	@Test
	public void testRemoveAuthorToMap() {
		//save the map
		PortfolioStructureMap map = createPortfolioMapTemplate(ident1, "add-author-map-1", "add-an-author-to-map-template");
		epStructureManager.savePortfolioStructure(map);
		dbInstance.commitAndCloseSession();
		
		//add an author
		epStructureManager.addAuthor(map, ident2);
		dbInstance.commitAndCloseSession();
		
		//check that the author are in the
		OLATResource resource = resourceManager.findResourceable(map.getResourceableId(), map.getResourceableTypeName());
		assertNotNull(resource);
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(resource, false);
		assertNotNull(re);
		List<Identity> authors = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		assertEquals(2, authors.size());
		dbInstance.commitAndCloseSession();
		
		//and remove the author
		epStructureManager.removeAuthor(map, ident2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> singleAuthor = repositoryService.getMembers(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		assertEquals(1, singleAuthor.size());
		assertTrue(singleAuthor.contains(ident1));//owner
		assertFalse(singleAuthor.contains(ident2));//owner
		
		securityGroupDao.getSecurityGroupsForIdentity(ident1);
		repositoryManager.queryResourcesLimitType(ident1, Roles.userRoles(), false, null, null, null, null, true, false);
	}
	
	
	/**
	 * Create a map template, create an OLAT resource and a repository entry with a security group
	 * of type owner to the repository and add the identity has an owner.
	 * @param identity
	 * @param title
	 * @param description
	 * @return The structure element
	 */
	public static PortfolioStructureMap createPortfolioMapTemplate(Identity identity, String title, String description) {
		EPStructureManager epStructureManager = CoreSpringFactory.getImpl(EPStructureManager.class);
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		DB dbInstance = CoreSpringFactory.getImpl(DB.class);
		
		
		EPStructuredMapTemplate el = new EPStructuredMapTemplate();
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		
		epStructureManager.fillStructureElement(el, title, description);

		//create a repository entry with default security settings
		RepositoryEntry re = repositoryService.create(identity, null, "-", title, null, el.getOlatResource(),
				RepositoryEntryStatusEnum.preparation, defOrganisation);
				
		dbInstance.commit();
		
		Group ownerGroup = repositoryService.getDefaultGroup(re);
		
		EPStructureElementToGroupRelation relation = epStructureManager.createBaseRelation(el, ownerGroup);
		Set<EPStructureElementToGroupRelation> relations = new HashSet<>();
		relations.add(relation);
		el.setGroups(relations);
		return el;
	}
	
	
	
	
}