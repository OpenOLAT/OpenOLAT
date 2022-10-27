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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryToOrganisationDAO repositoryEntryToOrganisationDao;
	
	@Test
	public void createRelation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-1", null, null, null, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryToOrganisation relation = repositoryEntryToOrganisationDao
				.createRelation(organisation, re, true);
		dbInstance.commit();
		Assert.assertNotNull(relation);
	}
	
	@Test
	public void getRelations() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-2", null, null, null, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel2", "rel2", null, null,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		RepositoryEntryToOrganisation relation = repositoryEntryToOrganisationDao
				.createRelation(organisation, re, true);
		dbInstance.commitAndCloseSession();
		
		// check the relations
		List<RepositoryEntryToOrganisation> relations = repositoryEntryToOrganisationDao.getRelations(re, organisation);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(relation, relations.get(0));
	}
	
	@Test
	public void getOrganisationReferences() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-3", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel3", "rel3", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation, re, true);
		dbInstance.commitAndCloseSession();
		
		// check the relations
		List<OrganisationRef> organisations = repositoryEntryToOrganisationDao.getOrganisationReferences(re);
		Assert.assertNotNull(organisations);
		Assert.assertEquals(1, organisations.size());
		Assert.assertEquals(organisation.getKey(), organisations.get(0).getKey());
	}
	
	@Test
	public void getRepositoryEntryOrganisations() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation1 = organisationDao.createAndPersistOrganisation("Repo-org-3", null, null, defOrganisation, null);
		Organisation organisation2 = organisationDao.createAndPersistOrganisation("Repo-org-3", null, null, defOrganisation, null);
		RepositoryEntry re1 = repositoryService.create(null, "Asuka Langley", "rel3", "rel3", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation1, re1, true);
		repositoryEntryToOrganisationDao.createRelation(organisation2, re1, true);
		RepositoryEntry re2 = repositoryService.create(null, "Asuka Langley", "rel3", "rel3", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation1, re2, true);
		dbInstance.commitAndCloseSession();
		
		Map<RepositoryEntryRef, List<Organisation>> repositoryEntryOrganisations = repositoryEntryToOrganisationDao.getRepositoryEntryOrganisations(List.of(re1, re2));
		
		Assert.assertNotNull(repositoryEntryOrganisations);
		Assert.assertEquals(2, repositoryEntryOrganisations.size());
		Assert.assertEquals(2, repositoryEntryOrganisations.get(new RepositoryEntryRefImpl(re1.getKey())).size());
		List<Long> re1OrgKeys = repositoryEntryOrganisations.get(new RepositoryEntryRefImpl(re1.getKey())).stream().map(OrganisationRef::getKey).collect(Collectors.toList());
		Assert.assertTrue(re1OrgKeys.contains(organisation1.getKey()));
		Assert.assertTrue(re1OrgKeys.contains(organisation2.getKey()));
		Assert.assertEquals(1, repositoryEntryOrganisations.get(new RepositoryEntryRefImpl(re2.getKey())).size());
	}
	
	@Test
	public void deleteRelation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-4", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel4", "rel4", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation, re, true);
		dbInstance.commitAndCloseSession();
		
		// delete the relations
		repositoryEntryToOrganisationDao.delete(re, organisation);
		dbInstance.commitAndCloseSession();
		
		// check the relations are really deleted
		List<RepositoryEntryToOrganisation> relations = repositoryEntryToOrganisationDao.getRelations(re, organisation);
		Assert.assertNotNull(relations);
		Assert.assertTrue(relations.isEmpty());
	}
	
	@Test
	public void deleteRelationByRelation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-5", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel5", "rel5", null, null,
				RepositoryEntryStatusEnum.trash, null);
		RepositoryEntryToOrganisation relation = repositoryEntryToOrganisationDao.createRelation(organisation, re, true);
		dbInstance.commitAndCloseSession();
		
		// delete the relations
		repositoryEntryToOrganisationDao.delete(relation);
		dbInstance.commitAndCloseSession();
		
		// check the relations are really deleted
		List<RepositoryEntryToOrganisation> relations = repositoryEntryToOrganisationDao.getRelations(re, organisation);
		Assert.assertTrue(relations.isEmpty());
	}
	
	@Test
	public void deleteRelationByRepositoryEntry() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation1 = organisationDao.createAndPersistOrganisation("Repo-org-6.1", null, null, defOrganisation, null);
		Organisation organisation2 = organisationDao.createAndPersistOrganisation("Repo-org-6.2", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel3", "rel6", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation1, re, true);
		repositoryEntryToOrganisationDao.createRelation(organisation2, re, true);
		dbInstance.commitAndCloseSession();
		
		// delete the relations
		repositoryEntryToOrganisationDao.delete(re);
		dbInstance.commitAndCloseSession();
		
		// check the relations are really deleted
		List<RepositoryEntryToOrganisation> relations1 = repositoryEntryToOrganisationDao.getRelations(re, organisation1);
		Assert.assertTrue(relations1.isEmpty());
		List<RepositoryEntryToOrganisation> relations2 = repositoryEntryToOrganisationDao.getRelations(re, organisation2);
		Assert.assertTrue(relations2.isEmpty());
	}

	
	@Test
	public void deleteRelationByOrganisation() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationDao.createAndPersistOrganisation("Repo-org-5", null, null, defOrganisation, null);
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel5", "rel5", null, null,
				RepositoryEntryStatusEnum.trash, null);
		repositoryEntryToOrganisationDao.createRelation(organisation, re, true);
		dbInstance.commitAndCloseSession();
		
		// delete the relations
		repositoryEntryToOrganisationDao.delete(organisation);
		dbInstance.commitAndCloseSession();
		
		// check the relations are really deleted
		List<RepositoryEntryToOrganisation> relations = repositoryEntryToOrganisationDao.getRelations(re, organisation);
		Assert.assertTrue(relations.isEmpty());
	}

}
