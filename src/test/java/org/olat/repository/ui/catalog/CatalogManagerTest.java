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
package org.olat.repository.ui.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.course.CourseModule;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CatalogEntry.Style;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OLATResourceManager orm;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	
	private static Random random = new Random();
	
	private void afterPropoertiesSet() {
		try {
			catalogManager.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private CatalogEntry generateEntry(int type, String name, Style style) {
		CatalogEntry catalogEntry = catalogManager.createCatalogEntry();
		
		catalogEntry.setName(name);
		catalogEntry.setType(type);
		catalogEntry.setStyle(style);
		
		return catalogEntry;
	}
	
	private CatalogEntry saveEntry(CatalogEntry catalogEntry, CatalogEntry parentEntry) {
		parentEntry = catalogManager.loadCatalogEntry(parentEntry);
		
		catalogEntry.setParent(parentEntry);
		catalogManager.saveCatalogEntry(catalogEntry);
		
		List<CatalogEntry> children = parentEntry.getChildren();
		children.add(catalogEntry);
		catalogManager.updateCatalogEntry(parentEntry);
		dbInstance.commit();
		
		return catalogEntry;
	}

	private CatalogEntry getRootEntry() {
		return catalogManager.getRootCatalogEntries().get(0);
	}
	
	private RepositoryEntry createRepository(String displayName, final Long resourceableId) {
		OLATResourceable resourceable = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {	return CourseModule.ORES_TYPE_COURSE;}
			@Override
			public Long getResourceableId() {return resourceableId;}
		};

		// create course and persist as OLATResourceImpl

		OLATResource r = orm.findResourceable(resourceable);
		if(r == null) {
			r = orm.createOLATResourceInstance(resourceable);
		}
		dbInstance.saveObject(r);
		dbInstance.intermediateCommit();
		
		RepositoryEntry d = repositoryManager.lookupRepositoryEntry(resourceable, false);
		if(d == null) {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			d = repositoryService.create(null, "Rei Ayanami", "-", displayName, "Repo entry",
					r, RepositoryEntryStatusEnum.trash, defOrganisation);
			dbInstance.saveObject(d);
		}
		dbInstance.intermediateCommit();
		return d;
	}
	
	@Test
	public void createCatalogEntry() {
		afterPropoertiesSet();
		CatalogEntry catalogEntry = catalogManager.createCatalogEntry();
		
		Assert.assertNotNull(catalogEntry);
		Assert.assertNotNull(catalogEntry.getOwnerGroup());
	}
	
	@Test
	public void getNodesChildrenOf() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "1parent", Style.compact);
		CatalogEntry child1 = generateEntry(CatalogEntry.TYPE_NODE, "1child1", Style.compact);
		CatalogEntry child2 = generateEntry(CatalogEntry.TYPE_NODE, "1child2", Style.compact);
		
		parentEntry.setParent(rootEntry);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		List<CatalogEntry> rootChildren = rootEntry.getChildren();
		rootChildren.add(parentEntry);
		catalogManager.saveCatalogEntry(rootEntry);
		dbInstance.commit();
		
		child1.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child1);
		child2.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child2);
		dbInstance.commit();

		List<CatalogEntry> children = parentEntry.getChildren();
		children.add(child1);
		children.add(child2);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		children = catalogManager.getNodesChildrenOf(parentEntry);
		
		Assert.assertNotNull(children);
		Assert.assertArrayEquals(new int[] {2}, new int[] {children.size()});
	}
	
	@Test
	public void getChildrenOf() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "2parent", Style.compact);
		CatalogEntry child1 = generateEntry(CatalogEntry.TYPE_NODE, "2child1", Style.compact);
		CatalogEntry child2 = generateEntry(CatalogEntry.TYPE_NODE, "2child2", Style.compact);
		CatalogEntry child3 = generateEntry(CatalogEntry.TYPE_LEAF, "2child3", null);
		CatalogEntry child4 = generateEntry(CatalogEntry.TYPE_LEAF, "2child4", null);
		
		parentEntry.setParent(rootEntry);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		List<CatalogEntry> rootChildren = rootEntry.getChildren();
		rootChildren.add(parentEntry);
		catalogManager.saveCatalogEntry(rootEntry);
		dbInstance.commit();
		
		child1.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child1);
		child2.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child2);
		child3.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child3);
		child4.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child4);
		dbInstance.commit();

		List<CatalogEntry> children = parentEntry.getChildren();
		children.add(child1);
		children.add(child2);
		children.add(child3);
		children.add(child4);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		children = catalogManager.getChildrenOf(parentEntry);
		
		Assert.assertNotNull(children);
		Assert.assertArrayEquals(new int[] {4}, new int[] {children.size()});
	}
	
	@Test
	public void getAllCatalogNodes() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "3parent", Style.compact);
		CatalogEntry child1 = generateEntry(CatalogEntry.TYPE_NODE, "3child1", Style.compact);
		CatalogEntry child2 = generateEntry(CatalogEntry.TYPE_NODE, "3child2", Style.compact);
		
		parentEntry.setParent(rootEntry);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		List<CatalogEntry> rootChildren = rootEntry.getChildren();
		rootChildren.add(parentEntry);
		catalogManager.saveCatalogEntry(rootEntry);
		dbInstance.commit();
		
		child1.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child1);
		child2.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child2);
		dbInstance.commit();

		List<CatalogEntry> children = parentEntry.getChildren();
		children.add(child1);
		children.add(child2);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		List<CatalogEntry> nodes = catalogManager.getAllCatalogNodes();
		
		Assert.assertNotNull(nodes);
		for (CatalogEntry catalogEntry : nodes) {
			Assert.assertEquals(CatalogEntry.TYPE_NODE, catalogEntry.getType());
		}
	}

	@Test
	public void hasChildEntries() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry child1 = generateEntry(CatalogEntry.TYPE_LEAF, "4child1", null);
		CatalogEntry child2 = generateEntry(CatalogEntry.TYPE_NODE, "4child2", Style.compact);
		
		child1.setParent(rootEntry);
		catalogManager.saveCatalogEntry(child1);
		child2.setParent(rootEntry);
		catalogManager.saveCatalogEntry(child2);
	
		List<CatalogEntry> children = rootEntry.getChildren();
		children.add(child1);
		children.add(child2);
		catalogManager.saveCatalogEntry(rootEntry);
		dbInstance.commit();
		
		Assert.assertTrue(catalogManager.hasChildEntries(rootEntry, CatalogEntry.TYPE_LEAF));
		Assert.assertTrue(catalogManager.hasChildEntries(rootEntry, CatalogEntry.TYPE_NODE));
	}
	
	@Test
	public void countChildrenOf() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "5parent", Style.compact);
		CatalogEntry child1 = generateEntry(CatalogEntry.TYPE_NODE, "5child1", Style.compact);
		CatalogEntry child2 = generateEntry(CatalogEntry.TYPE_NODE, "5child2", Style.compact);
		CatalogEntry child3 = generateEntry(CatalogEntry.TYPE_LEAF, "5child3", null);
		CatalogEntry child4 = generateEntry(CatalogEntry.TYPE_LEAF, "5child4", null);
		
		parentEntry.setParent(rootEntry);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		List<CatalogEntry> rootChildren = rootEntry.getChildren();
		rootChildren.add(parentEntry);
		catalogManager.saveCatalogEntry(rootEntry);
		dbInstance.commit();
		
		child1.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child1);
		child2.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child2);
		child3.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child3);
		child4.setParent(parentEntry);
		catalogManager.saveCatalogEntry(child4);
		dbInstance.commit();

		List<CatalogEntry> children = parentEntry.getChildren();
		children.add(child1);
		children.add(child2);
		children.add(child3);
		children.add(child4);
		catalogManager.saveCatalogEntry(parentEntry);
		dbInstance.commit();
		
		children = catalogManager.getChildrenOf(parentEntry);
		
		Assert.assertArrayEquals(new int[] {2, 2}, new int[] {
				catalogManager.countChildrenOf(parentEntry, CatalogEntry.TYPE_LEAF),
				catalogManager.countChildrenOf(parentEntry, CatalogEntry.TYPE_NODE)
		});
	}
	
	@Test
	public void loadCatalogEntryByKey() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry catalogEntry = generateEntry(CatalogEntry.TYPE_NODE, "6parent", Style.compact);
		
		saveEntry(catalogEntry, rootEntry);
		
		CatalogEntry compareEntry = catalogManager.getCatalogEntryByKey(catalogEntry.getKey());
		
		Assert.assertEquals(catalogEntry, compareEntry);
	}
	
	@Test
	public void saveCatalogEntry() {
		afterPropoertiesSet();
		
		String name = "7SaveTestName";
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry catalogEntry = generateEntry(CatalogEntry.TYPE_NODE, name, Style.compact);
		
		saveEntry(catalogEntry, rootEntry);
		
		CatalogEntry compareEntry = catalogManager.getCatalogEntryByKey(catalogEntry.getKey());
		
		Assert.assertEquals(catalogEntry.getName(), compareEntry.getName());
	}
	
	@Test
	public void deleteCatalogEntry() {
		afterPropoertiesSet();
		
		String name = "8DeleteTestName";
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "8parentDelete", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, name, Style.compact);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_LEAF, name, null);
		CatalogEntry catalogEntry3 = generateEntry(CatalogEntry.TYPE_LEAF, name, null);
		
		parentEntry = saveEntry(parentEntry, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry);
		catalogEntry2 = saveEntry(catalogEntry2, parentEntry);
		catalogEntry3 = saveEntry(catalogEntry3, parentEntry);
		
		catalogManager.deleteCatalogEntry(catalogEntry2);
		catalogManager.deleteCatalogEntry(catalogEntry1);
		dbInstance.commit();
		
		List<CatalogEntry> children = catalogManager.loadCatalogEntry(parentEntry).getChildren();
		System.out.println(children.size());
		
		Assert.assertNull(catalogManager.loadCatalogEntry(catalogEntry2));		
		assertThat(children).containsExactlyInAnyOrder(catalogEntry3);
	}
	
	@Test
	public void updateCatalogEntry() {
		afterPropoertiesSet();
		
		String name = "9UpdateTestName";
		String update = " - updated";
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry = generateEntry(CatalogEntry.TYPE_NODE, "9parentDelete", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, name, Style.compact);
		
		parentEntry = saveEntry(parentEntry, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry);
		
		catalogEntry1.setName(name + update);
		catalogManager.updateCatalogEntry(catalogEntry1);
		catalogEntry1 = catalogManager.loadCatalogEntry(catalogEntry1);
		
		assertThat(catalogEntry1.getName()).isEqualTo(name + update);
	}
	
	@Test
	public void getCatalogEntriesReferencing() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "10parent1", Style.compact);
		CatalogEntry parentEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "10parent2", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "10parent1", Style.compact);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "10parent1", Style.compact);
		
		RepositoryEntry re = createRepository("10test-entry", random.nextLong());
		catalogEntry1.setRepositoryEntry(re);
		catalogEntry2.setRepositoryEntry(re);
		
		parentEntry1 = saveEntry(parentEntry1, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry1);
		parentEntry2 = saveEntry(parentEntry2, rootEntry);
		catalogEntry2 = saveEntry(catalogEntry2, parentEntry1);
		
		assertThat(catalogManager.getCatalogEntriesReferencing(re)).containsExactlyInAnyOrder(catalogEntry1, catalogEntry2);
	}
	
	@Test
	public void getCatalogCategoriesFor() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "11parent1", Style.compact);
		CatalogEntry parentEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "11parent2", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "11parent1", Style.compact);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "11parent1", Style.compact);
		
		RepositoryEntry re = createRepository("test-entry", random.nextLong());
		catalogEntry1.setRepositoryEntry(re);
		catalogEntry2.setRepositoryEntry(re);
		
		parentEntry1 = saveEntry(parentEntry1, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry1);
		parentEntry2 = saveEntry(parentEntry2, rootEntry);
		catalogEntry2 = saveEntry(catalogEntry2, parentEntry2);
		
		assertThat(catalogManager.getCatalogCategoriesFor(re)).containsExactlyInAnyOrder(parentEntry1, parentEntry2);
	}
	
	@Test
	public void getCatalogEntryBy() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "12parent1", Style.compact);
		CatalogEntry parentEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "12parent2", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "12parent1", Style.compact);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "12parent1", Style.compact);
		
		RepositoryEntry re = createRepository("test-entry", random.nextLong());
		catalogEntry1.setRepositoryEntry(re);
		
		parentEntry1 = saveEntry(parentEntry1, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry1);
		parentEntry2 = saveEntry(parentEntry2, rootEntry);
		catalogEntry2 = saveEntry(catalogEntry2, parentEntry2);
		
		assertThat(catalogManager.getCatalogEntryBy(re, parentEntry1)).isEqualTo(catalogEntry1);
		assertThat(catalogManager.getCatalogEntryBy(re, parentEntry2)).isNull();
	}
	
	@Test
	public void isOwner() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "13parent1", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "13parent1", Style.compact);
		
		parentEntry1 = saveEntry(parentEntry1, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry1);
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("13catalog-test-identity");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		
		securityGroupDao.addIdentityToSecurityGroup(admin, catalogEntry1.getOwnerGroup());
		dbInstance.commit();
		
		assertThat(catalogManager.isOwner(admin)).isTrue();
		assertThat(catalogManager.isOwner(id1)).isFalse();
		assertThat(catalogManager.isOwner(catalogEntry1, admin)).isTrue();
	}
	
	@Test
	public void getOwners() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "14parent1", Style.compact);
		CatalogEntry parentEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "14parent2", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "14parent1", Style.compact);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "14parent1", Style.compact);
		
		RepositoryEntry re = createRepository("test-entry", random.nextLong());
		catalogEntry1.setRepositoryEntry(re);
		
		parentEntry1 = saveEntry(parentEntry1, rootEntry);
		catalogEntry1 = saveEntry(catalogEntry1, parentEntry1);
		parentEntry2 = saveEntry(parentEntry2, rootEntry);
		catalogEntry2 = saveEntry(catalogEntry2, parentEntry2);	
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("14catalog-test-identity");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		
		securityGroupDao.addIdentityToSecurityGroup(admin, catalogEntry1.getOwnerGroup());
		securityGroupDao.addIdentityToSecurityGroup(admin, catalogEntry2.getOwnerGroup());
		securityGroupDao.addIdentityToSecurityGroup(id1, catalogEntry2.getOwnerGroup());
		dbInstance.commit();
		
		assertThat(catalogManager.getOwners(catalogEntry1)).contains(admin);
		assertThat(catalogManager.getOwners(catalogEntry2)).contains(admin, id1);
	}
	
	@Test
	public void addCatalogEntry() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntry1 = generateEntry(CatalogEntry.TYPE_NODE, "15parent1", Style.compact);
		CatalogEntry parentEntry2 = generateEntry(CatalogEntry.TYPE_NODE, "15parent2", Style.compact);
		CatalogEntry catalogEntry1 = generateEntry(CatalogEntry.TYPE_LEAF, "15child1", null);
		CatalogEntry catalogEntry2 = generateEntry(CatalogEntry.TYPE_LEAF, "15child2", null);
		
		catalogManager.addCatalogEntry(rootEntry, parentEntry1);
		catalogManager.addCatalogEntry(rootEntry, parentEntry2);
		catalogManager.addCatalogEntry(parentEntry1, catalogEntry1);
		catalogManager.addCatalogEntry(parentEntry2, catalogEntry2);
		
		rootEntry = catalogManager.loadCatalogEntry(rootEntry);
		parentEntry1 = catalogManager.loadCatalogEntry(parentEntry1);
		parentEntry2 = catalogManager.loadCatalogEntry(parentEntry2);
		catalogEntry1 = catalogManager.loadCatalogEntry(catalogEntry1);
		catalogEntry2 = catalogManager.loadCatalogEntry(catalogEntry2);
		
		assertThat(rootEntry.getChildren()).contains(parentEntry1, parentEntry2);
		assertThat(parentEntry1.getChildren()).contains(catalogEntry1);
		assertThat(parentEntry2.getChildren()).contains(catalogEntry2);
		
		assertThat(parentEntry1.getChildren()).doesNotContain(catalogEntry2);
		assertThat(parentEntry2.getChildren()).doesNotContain(catalogEntry1);
	}
	
	@Test
	public void getRootCatalogEntries() {
		afterPropoertiesSet();
		assertThat(catalogManager.getRootCatalogEntries()).hasSize(1);
	}
	
	@Test
	public void moveCatalogEntry() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntryA = generateEntry(CatalogEntry.TYPE_NODE, "16parentA", Style.compact);
		CatalogEntry parentEntryAA = generateEntry(CatalogEntry.TYPE_NODE, "16parentAA", Style.compact);
		CatalogEntry parentEntryB = generateEntry(CatalogEntry.TYPE_NODE, "16parentB", Style.compact);
		CatalogEntry parentEntryBA = generateEntry(CatalogEntry.TYPE_NODE, "16parentBA", Style.compact);
		CatalogEntry catalogEntryAA1 = generateEntry(CatalogEntry.TYPE_LEAF, "16childAA1", null);
		CatalogEntry catalogEntryAA2 = generateEntry(CatalogEntry.TYPE_LEAF, "16childAA2", null);
		CatalogEntry catalogEntryAA3 = generateEntry(CatalogEntry.TYPE_LEAF, "16childAA3", null);
		CatalogEntry catalogEntryB1 = generateEntry(CatalogEntry.TYPE_LEAF, "16childB1", null);
		
		catalogEntryAA1.setRepositoryEntry(createRepository("16test-entry" + random.nextInt(), random.nextLong()));
		catalogEntryAA2.setRepositoryEntry(createRepository("16test-entry" + random.nextInt(), random.nextLong()));
		catalogEntryAA3.setRepositoryEntry(createRepository("16test-entry" + random.nextInt(), random.nextLong()));
		
		catalogManager.addCatalogEntry(rootEntry, parentEntryA);
		catalogManager.addCatalogEntry(rootEntry, parentEntryB);
		catalogManager.addCatalogEntry(parentEntryA, parentEntryAA);
		catalogManager.addCatalogEntry(parentEntryB, parentEntryBA);
		
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA1);
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA2);
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA3);
		catalogManager.addCatalogEntry(parentEntryB, catalogEntryB1);
		
		parentEntryA = catalogManager.loadCatalogEntry(parentEntryA);
		parentEntryAA = catalogManager.loadCatalogEntry(parentEntryAA);
		parentEntryB = catalogManager.loadCatalogEntry(parentEntryA);
		parentEntryBA = catalogManager.loadCatalogEntry(parentEntryBA);
		
		catalogEntryAA1 = catalogManager.loadCatalogEntry(catalogEntryAA1);
		catalogEntryAA2 = catalogManager.loadCatalogEntry(catalogEntryAA2);
		catalogEntryAA3 = catalogManager.loadCatalogEntry(catalogEntryAA3);
		catalogEntryB1 = catalogManager.loadCatalogEntry(catalogEntryB1);
		
		assertThat(catalogManager.moveCatalogEntry(parentEntryA, parentEntryAA)).isFalse();
		assertThat(catalogEntryAA3.getPosition()).isEqualTo(2);
		assertThat(catalogManager.moveCatalogEntry(catalogEntryAA2, parentEntryBA)).isTrue();
		assertThat(catalogEntryAA2.getPosition()).isEqualTo(1);
		assertThat(catalogManager.moveCatalogEntry(parentEntryAA, parentEntryBA)).isTrue();
		assertThat(catalogEntryB1.getPosition()).isEqualTo(1);
		assertThat(parentEntryAA.getPosition()).isZero();
	}
	
	@Test
	public void reorderCatalogEntry() {
		afterPropoertiesSet();
		
		CatalogEntry rootEntry = getRootEntry();
		CatalogEntry parentEntryA = generateEntry(CatalogEntry.TYPE_NODE, "17parentA", Style.compact);
		CatalogEntry parentEntryAA = generateEntry(CatalogEntry.TYPE_NODE, "17parentAA", Style.compact);
		CatalogEntry parentEntryB = generateEntry(CatalogEntry.TYPE_NODE, "17parentB", Style.compact);
		CatalogEntry parentEntryBA = generateEntry(CatalogEntry.TYPE_NODE, "17parentBA", Style.compact);
		CatalogEntry catalogEntryAA1 = generateEntry(CatalogEntry.TYPE_LEAF, "17childAA1", null);
		CatalogEntry catalogEntryAA2 = generateEntry(CatalogEntry.TYPE_LEAF, "17childAA2", null);
		CatalogEntry catalogEntryAA3 = generateEntry(CatalogEntry.TYPE_LEAF, "17childAA3", null);
		CatalogEntry catalogEntryB1 = generateEntry(CatalogEntry.TYPE_LEAF, "17childB1", null);
		
		catalogEntryAA1.setRepositoryEntry(createRepository("17test-entry" + random.nextInt(), random.nextLong()));
		catalogEntryAA2.setRepositoryEntry(createRepository("17test-entry" + random.nextInt(), random.nextLong()));
		catalogEntryAA3.setRepositoryEntry(createRepository("17test-entry" + random.nextInt(), random.nextLong()));
		
		catalogManager.addCatalogEntry(rootEntry, parentEntryA);
		catalogManager.addCatalogEntry(rootEntry, parentEntryB);
		catalogManager.addCatalogEntry(parentEntryA, parentEntryAA);
		catalogManager.addCatalogEntry(parentEntryB, parentEntryBA);
		
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA1);
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA2);
		catalogManager.addCatalogEntry(parentEntryAA, catalogEntryAA3);
		catalogManager.addCatalogEntry(parentEntryB, catalogEntryB1);
		
		parentEntryA = catalogManager.loadCatalogEntry(parentEntryA);
		parentEntryAA = catalogManager.loadCatalogEntry(parentEntryAA);
		parentEntryB = catalogManager.loadCatalogEntry(parentEntryA);
		parentEntryBA = catalogManager.loadCatalogEntry(parentEntryBA);
		
		catalogEntryAA1 = catalogManager.loadCatalogEntry(catalogEntryAA1);
		catalogEntryAA2 = catalogManager.loadCatalogEntry(catalogEntryAA2);
		catalogEntryAA3 = catalogManager.loadCatalogEntry(catalogEntryAA3);
		catalogEntryB1 = catalogManager.loadCatalogEntry(catalogEntryB1);
		
		assertThat(catalogManager.reorderCatalogEntry(parentEntryA.getKey(), catalogEntryB1.getKey(), true)).isEqualTo(1);
		assertThat(catalogManager.reorderCatalogEntry(parentEntryAA.getKey(), catalogEntryAA1.getKey(), false)).isZero();
		
		catalogEntryAA1 = catalogManager.loadCatalogEntry(catalogEntryAA1);
		assertThat(catalogEntryAA1.getPosition()).isEqualTo(1);
		
		assertThat(catalogManager.setPosition(catalogEntryB1.getKey(), 10)).isEqualTo(2);
		assertThat(catalogManager.setPosition(catalogEntryAA2.getKey(), 0)).isZero();
		
		catalogEntryAA2 = catalogManager.loadCatalogEntry(catalogEntryAA2);
		assertThat(catalogEntryAA2.getPosition()).isZero();
	}
	
}