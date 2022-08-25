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
package org.olat.modules.catalog.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherToOrganisation;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.model.CatalogLauncherToOrganisationImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.08.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CatalogLauncherToOrganisationDAO sut;
	
	@Test
	public void shouldCreateRelation() {
		CatalogLauncher launcher = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation = organisationService.createOrganisation(random(), null, null, null,null);
		dbInstance.commitAndCloseSession();
		
		CatalogLauncherToOrganisation launcherToOrganisation = sut.createRelation(launcher, organisation);
		dbInstance.commitAndCloseSession();
		
		assertThat(((CatalogLauncherToOrganisationImpl)launcherToOrganisation).getCreationDate()).isNotNull();
		assertThat(((CatalogLauncherToOrganisationImpl)launcherToOrganisation).getLastModified()).isNotNull();
		assertThat(launcherToOrganisation.getLauncher()).isEqualTo(launcher);
		assertThat(launcherToOrganisation.getOrganisation()).isEqualTo(organisation);
	}
	
	@Test
	public void shouldLoadRelationsByCatalogLauncherOrgOrganistion() {
		CatalogLauncher launcher1 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher2 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		CatalogLauncherToOrganisation launcherToOrganisation11 = sut.createRelation(launcher1, organisation1);
		CatalogLauncherToOrganisation launcherToOrganisation12 = sut.createRelation(launcher1, organisation2);
		sut.createRelation(launcher2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(launcher1, null)).hasSize(2).containsExactlyInAnyOrder(launcherToOrganisation11, launcherToOrganisation12);
		assertThat(sut.loadRelations(null, organisation1)).hasSize(1).containsExactlyInAnyOrder(launcherToOrganisation11);
		assertThat(sut.loadRelations(launcher1, organisation2)).hasSize(1).containsExactlyInAnyOrder(launcherToOrganisation12);
	}
	
	@Test
	public void shouldLoadRelationsByCatalogLaunchers() {
		CatalogLauncher launcher1 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher2 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher3 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		CatalogLauncherToOrganisation launcherToOrganisation11 = sut.createRelation(launcher1, organisation1);
		CatalogLauncherToOrganisation launcherToOrganisation12 = sut.createRelation(launcher1, organisation2);
		CatalogLauncherToOrganisation launcherToOrganisation21 = sut.createRelation(launcher2, organisation1);
		sut.createRelation(launcher3, organisation2);
		dbInstance.commitAndCloseSession();
		
		List<CatalogLauncherToOrganisation> relations = sut.loadRelations(List.of(launcher1,  launcher2));
		
		assertThat(relations).containsExactlyInAnyOrder(
				launcherToOrganisation11,
				launcherToOrganisation12,
				launcherToOrganisation21
				);
	}
	
	@Test
	public void shouldLoadOrganisationsByCatalogLauncher() {
		CatalogLauncher launcher1 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher2 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(launcher1, organisation1);
		sut.createRelation(launcher1, organisation2);
		sut.createRelation(launcher2, organisation2);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadOrganisations(launcher1)).containsExactlyInAnyOrder(organisation1, organisation2);
		assertThat(sut.loadOrganisations(launcher2)).containsExactlyInAnyOrder(organisation2);
	}
	
	@Test
	public void shouldDeleteReleation() {
		CatalogLauncher launcher1 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher2 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		CatalogLauncherToOrganisation launcherToOrganisation11 = sut.createRelation(launcher1, organisation1);
		CatalogLauncherToOrganisation launcherToOrganisation12 = sut.createRelation(launcher1, organisation2);
		CatalogLauncherToOrganisation launcherToOrganisation21 = sut.createRelation(launcher2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(launcherToOrganisation12);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(launcher1, null)).containsExactlyInAnyOrder(launcherToOrganisation11);
		assertThat(sut.loadRelations(launcher2, null)).containsExactlyInAnyOrder(launcherToOrganisation21);
	}
	
	@Test
	public void shouldDeleteByCatalogLauncher() {
		CatalogLauncher launcher1 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		CatalogLauncher launcher2 = catalogService.createCatalogLauncher(miniRandom(), miniRandom());
		Organisation organisation1 = organisationService.createOrganisation(random(), null, null, null,null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, null, null,null);
		sut.createRelation(launcher1, organisation1);
		sut.createRelation(launcher1, organisation2);
		CatalogLauncherToOrganisation launcherToOrganisation21 = sut.createRelation(launcher2, organisation2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(launcher1);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadRelations(launcher1, null)).isEmpty();
		assertThat(sut.loadRelations(launcher2, null)).containsExactlyInAnyOrder(launcherToOrganisation21);
	}

}
