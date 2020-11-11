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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private OLATResourceManager resourceManager;

	@Test
	public void loadByKey() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 1", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		RepositoryEntry loadedRe = repositoryEntryDao.loadByKey(re.getKey());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertNotNull(loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByKeys() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 1s", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		List<RepositoryEntry> loadedRes = repositoryEntryDao.loadByKeys(Collections.singletonList(re.getKey()));
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(loadedRes);
		Assert.assertEquals(1, loadedRes.size());
		
		RepositoryEntry loadedRe = loadedRes.get(0);
		Assert.assertEquals(re, loadedRe);
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertNotNull(loadedRe.getOlatResource());
	}

	@Test
	public void loadByResourceKey() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 2", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		RepositoryEntry loadedRe = repositoryEntryDao.loadByResourceKey(re.getOlatResource().getKey());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
	}

	@Test
	public void loadByResourceKeys() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re1 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 3a", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		RepositoryEntry re2 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 3b", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();

		List<Long> resourceKeys = new ArrayList<>(2);
		resourceKeys.add(re1.getOlatResource().getKey());
		resourceKeys.add(re2.getOlatResource().getKey());

		//load 2 resources
		List<RepositoryEntry> loadedRes = repositoryEntryDao.loadByResourceKeys(resourceKeys);
		Assert.assertNotNull(loadedRes);
		Assert.assertEquals(2,  loadedRes.size());
		Assert.assertTrue(loadedRes.contains(re1));
		Assert.assertTrue(loadedRes.contains(re2));

		//try with empty list
		List<RepositoryEntry> emptyRes = repositoryEntryDao.loadByResourceKeys(Collections.<Long>emptyList());
		Assert.assertNotNull(emptyRes);
		Assert.assertEquals(0,  emptyRes.size());
	}
	
	@Test
	public void loadByResource() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 12", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		RepositoryEntry loadedRe = repositoryEntryDao.loadByResource(re.getOlatResource());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertEquals(re, loadedRe);
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByResources() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 14", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		List<OLATResource> resources = Collections.singletonList(re.getOlatResource());
		List<RepositoryEntry> loadedRes = repositoryEntryDao.loadByResources(resources);
		Assert.assertNotNull(loadedRes);
		Assert.assertEquals(1, loadedRes.size());
		RepositoryEntry loadedRe = loadedRes.get(0);
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertEquals(re, loadedRe);
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByResourceId() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 10", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		OLATResourceable ores = re.getOlatResource();
		RepositoryEntry loadedRe = repositoryEntryDao.loadByResourceId(ores.getResourceableTypeName(), ores.getResourceableId());
		Assert.assertNotNull(loadedRe.getStatistics());
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
	}
	
	@Test
	public void loadByResourceIds() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 11", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		OLATResourceable ores = re.getOlatResource();
		Collection<Long> oresIds = Collections.singletonList(ores.getResourceableId());
		List<RepositoryEntry> loadedRes = repositoryEntryDao.loadByResourceIds(ores.getResourceableTypeName(), oresIds);
		Assert.assertNotNull(loadedRes);
		Assert.assertEquals(1, loadedRes.size());
		RepositoryEntry loadedRe = loadedRes.get(0);
		Assert.assertEquals(re, loadedRe);
		Assert.assertEquals(re.getOlatResource(), loadedRe.getOlatResource());
		Assert.assertNotNull(loadedRe.getStatistics());
	}

	@Test
	public void searchByIdAndRefs() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 4", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		String externalId = UUID.randomUUID().toString();
		String externalRef = UUID.randomUUID().toString();
		re = repositoryManager.setDescriptionAndName(re, null, null, null, null, externalId, externalRef, null, null);
		dbInstance.commitAndCloseSession();

		//by primary key
		List<RepositoryEntry> primaryKeyList = repositoryEntryDao.searchByIdAndRefs(Long.toString(re.getKey()));
		Assert.assertNotNull(primaryKeyList);
		Assert.assertEquals(1,  primaryKeyList.size());
		Assert.assertEquals(re, primaryKeyList.get(0));

		//by soft key
		List<RepositoryEntry> softKeyList = repositoryEntryDao.searchByIdAndRefs(re.getSoftkey());
		Assert.assertNotNull(softKeyList);
		Assert.assertEquals(1, softKeyList.size());
		Assert.assertEquals(re, softKeyList.get(0));

		//by resourceable id key
		List<RepositoryEntry> resourceableIdList = repositoryEntryDao.searchByIdAndRefs(Long.toString(re.getResourceableId()));
		Assert.assertNotNull(resourceableIdList);
		Assert.assertEquals(1, resourceableIdList.size());
		Assert.assertEquals(re, resourceableIdList.get(0));

		//by resource resourceable id
		Long resResourceableId = re.getOlatResource().getResourceableId();
		List<RepositoryEntry> resResourceableIdList = repositoryEntryDao.searchByIdAndRefs(resResourceableId.toString());
		Assert.assertNotNull(resResourceableIdList);
		Assert.assertEquals(1,  resResourceableIdList.size());
		Assert.assertEquals(re, resResourceableIdList.get(0));

		//by external id
		List<RepositoryEntry> externalIdList = repositoryEntryDao.searchByIdAndRefs(externalId);
		Assert.assertNotNull(externalIdList);
		Assert.assertEquals(1,  externalIdList.size());
		Assert.assertEquals(re, externalIdList.get(0));

		//by external ref
		List<RepositoryEntry> externalRefList = repositoryEntryDao.searchByIdAndRefs(externalRef);
		Assert.assertNotNull(externalRefList);
		Assert.assertEquals(1, externalRefList.size());
		Assert.assertEquals(re, externalRefList.get(0));

	}

	@Test
	public void getAllRepositoryEntries() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 4", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		List<RepositoryEntry> allRes = repositoryEntryDao.getAllRepositoryEntries(0, 25);
		Assert.assertNotNull(allRes);
		Assert.assertFalse(allRes.isEmpty());
		Assert.assertTrue(allRes.size() < 26);
	}

	@Test
	public void loadRepositoryEntryResource() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 5", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getSoftkey());
		Assert.assertNotNull(re.getOlatResource());

		OLATResource loadedResource = repositoryEntryDao.loadRepositoryEntryResource(re.getKey());
		Assert.assertNotNull(loadedResource);
		Assert.assertEquals(re.getOlatResource(), loadedResource);
	}

	@Test
	public void loadRepositoryEntryResourceBySoftKey() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 5", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getSoftkey());
		Assert.assertNotNull(re.getOlatResource());

		OLATResource loadedResource = repositoryEntryDao.loadRepositoryEntryResourceBySoftKey(re.getSoftkey());
		Assert.assertNotNull(loadedResource);
		Assert.assertEquals(re.getOlatResource(), loadedResource);
	}

	@Test
	public void loadRepositoryEntriesByExternalId() {
		String externalId = "myExternalId";

		// remove old test date
		String query = "update repositoryentry as v set v.externalId=null where v.externalId=:externalId";
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("externalId", externalId)
				.executeUpdate();

		// insert test data
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re1 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 7a", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re1.setExternalId(externalId);
		repositoryService.update(re1);
		RepositoryEntry re2 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 7b", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re2.setExternalId(externalId);
		repositoryService.update(re2);
		dbInstance.commitAndCloseSession();

		Collection<RepositoryEntry> entries = repositoryEntryDao.loadRepositoryEntriesByExternalId(externalId);
		Assert.assertNotNull(entries);
		Assert.assertEquals(2, entries.size());

		//try with null
		Collection<RepositoryEntry> emptyRes = repositoryEntryDao.loadRepositoryEntriesByExternalId(null);
		Assert.assertNotNull(emptyRes);
		Assert.assertEquals(0, emptyRes.size());
	}

	@Test
	public void loadRepositoryEntriesByExternalRef() {
		String externalRef = "myExternalRef";

		// remove old test date
		String query = "update repositoryentry as v set v.externalRef=null where v.externalRef=:externalRef";
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("externalRef", externalRef)
				.executeUpdate();

		// insert test data
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re1 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8a", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re1.setExternalRef(externalRef);
		repositoryService.update(re1);
		RepositoryEntry re2 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8b", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re2.setExternalRef(externalRef);
		repositoryService.update(re2);
		dbInstance.commitAndCloseSession();

		Collection<RepositoryEntry> entries = repositoryEntryDao.loadRepositoryEntriesByExternalRef(externalRef);
		Assert.assertNotNull(entries);
		Assert.assertEquals(2, entries.size());

		//try with null
		Collection<RepositoryEntry> emptyRes = repositoryEntryDao.loadRepositoryEntriesByExternalRef(null);
		Assert.assertNotNull(emptyRes);
		Assert.assertEquals(0, emptyRes.size());
	}

	@Test
	public void loadRepositoryEntriesLikeExternalRef() {
		String externalRef = "myExternalRef";

		// remove old test date
		String query = "update repositoryentry as v set v.externalRef=null where v.externalRef like (:externalRef)";
		String externalRefParamater = new StringBuilder("%").append(externalRef).append("%").toString();
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("externalRef", externalRefParamater)
				.executeUpdate();

		// insert test data
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re1 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8a", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re1.setExternalRef(externalRef);
		repositoryService.update(re1);
		RepositoryEntry re2 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8b", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re2.setExternalRef(externalRef + "123");
		repositoryService.update(re2);
		RepositoryEntry re3 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8c", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re3.setExternalRef("abc" + externalRef + "123");
		repositoryService.update(re3);
		RepositoryEntry re4 = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test 8d", "", null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		re4.setExternalRef("123");
		repositoryService.update(re4);
		dbInstance.commitAndCloseSession();

		Collection<RepositoryEntry> entries = repositoryEntryDao.loadRepositoryEntriesLikeExternalRef(externalRef);
		Assert.assertNotNull(entries);
		Assert.assertEquals(3, entries.size());
	}
	
	@Test
	public void loadRepositoryEntries() {
		// insert test data
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test all", "", null,
				RepositoryEntryStatusEnum.published, defOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);

		List<RepositoryEntry> oneEntry = repositoryEntryDao.loadRepositoryEntries(0, 1);
		Assert.assertNotNull(oneEntry);
		Assert.assertEquals(1, oneEntry.size());
	}
	
	@Test
	public void getLastUsedRepositoryEntries() {
		// insert test data
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("Wiki", Long.valueOf(CodeHelper.getForeverUniqueID()));
		OLATResource resource = resourceManager.createAndPersistOLATResourceInstance(resourceable);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "Rei Ayanami", "-", "Repository entry DAO Test all", "", resource,
				RepositoryEntryStatusEnum.published, defOrganisation);
		dbInstance.commit();
		repositoryService.setLastUsageNowFor(re);
		dbInstance.commitAndCloseSession();

		List<RepositoryEntry> lastUsed = repositoryEntryDao.getLastUsedRepositoryEntries("Wiki", 0, 100);
		Assert.assertNotNull(lastUsed);
		Assert.assertTrue(lastUsed.size() <= 100);
		Assert.assertTrue(lastUsed.contains(re));
	}
}