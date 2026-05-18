/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement.manager;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BuildingDAO buildingDAO;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void createAndLoad() {
		Building building = buildingDAO.create("Test Building " + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Building reloaded = buildingDAO.loadByKey(building);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(building.getKey(), reloaded.getKey());
		Assert.assertEquals(RoomStatus.active, reloaded.getStatus());
	}

	@Test
	public void update() {
		Building building = buildingDAO.create("Update Test " + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		building.setDescription("Updated Description");
		building.setAddress("Updated Address");
		Building updated = buildingDAO.update(building);
		dbInstance.commitAndCloseSession();

		Building reloaded = buildingDAO.loadByKey(updated);
		Assert.assertEquals("Updated Description", reloaded.getDescription());
		Assert.assertEquals("Updated Address", reloaded.getAddress());
	}

	@Test
	public void loadByExternalId() {
		String extId = "ext-" + UUID.randomUUID();
		Building building = buildingDAO.create("Ext ID Test");
		building.setExternalId(extId);
		buildingDAO.update(building);
		dbInstance.commitAndCloseSession();

		Building found = buildingDAO.loadByExternalId(extId);
		Assert.assertNotNull(found);
		Assert.assertEquals(extId, found.getExternalId());
	}

	@Test
	public void searchByDescription() {
		String uniqueDescription = "UniqueBldDesc_" + UUID.randomUUID();
		buildingDAO.create(uniqueDescription);
		dbInstance.commitAndCloseSession();

		SearchBuildingParameters params = new SearchBuildingParameters();
		params.setSearchString(uniqueDescription.substring(0, 14));
		List<Building> results = buildingDAO.search(params);

		Assertions.assertThat(results)
				.extracting(Building::getDescription)
				.contains(uniqueDescription);
	}

	@Test
	public void searchByStatus() {
		String description = "StatusTest_" + UUID.randomUUID();
		Building building = buildingDAO.create(description);
		building.setStatus(RoomStatus.inactive);
		buildingDAO.update(building);
		dbInstance.commitAndCloseSession();

		SearchBuildingParameters activeOnly = new SearchBuildingParameters();
		activeOnly.setStatus(List.of(RoomStatus.active));
		List<Building> activeResults = buildingDAO.search(activeOnly);
		Assertions.assertThat(activeResults)
				.extracting(Building::getDescription)
				.doesNotContain(description);

		SearchBuildingParameters inactiveOnly = new SearchBuildingParameters();
		inactiveOnly.setStatus(List.of(RoomStatus.inactive));
		List<Building> inactiveResults = buildingDAO.search(inactiveOnly);
		Assertions.assertThat(inactiveResults)
				.extracting(Building::getDescription)
				.contains(description);
	}

	@Test
	public void softDelete() {
		Building building = buildingDAO.create("ToDelete_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		int rows = buildingDAO.delete(building);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, rows);
		Building reloaded = buildingDAO.loadByKey(building);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(RoomStatus.deleted, reloaded.getStatus());
	}

	@Test
	public void count() {
		String uniqueDescription = "CountTest_" + UUID.randomUUID();
		buildingDAO.create(uniqueDescription);
		dbInstance.commitAndCloseSession();

		SearchBuildingParameters params = new SearchBuildingParameters();
		params.setSearchString(uniqueDescription);
		long count = buildingDAO.count(params);
		Assert.assertEquals(1L, count);
	}

	@Test
	public void addAndRemoveOrganisation() {
		Building building = buildingDAO.create("OrgTest_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Organisation org = organisationService.getDefaultOrganisation();
		buildingDAO.addOrganisation(building, org);
		dbInstance.commitAndCloseSession();

		List<Organisation> orgs = buildingDAO.getOrganisations(building);
		Assertions.assertThat(orgs).isNotEmpty();
		Assertions.assertThat(orgs).extracting(Organisation::getKey).contains(org.getKey());

		buildingDAO.removeOrganisation(building, org);
		dbInstance.commitAndCloseSession();

		List<Organisation> orgsAfterRemove = buildingDAO.getOrganisations(building);
		Assertions.assertThat(orgsAfterRemove).extracting(Organisation::getKey).doesNotContain(org.getKey());
	}
}
