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
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchLocationParameters;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LocationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LocationDAO locationDAO;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void createAndLoad() {
		Location location = locationDAO.create("Test Location " + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Location reloaded = locationDAO.loadByKey(location);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(location.getKey(), reloaded.getKey());
		Assert.assertEquals(RoomStatus.active, reloaded.getStatus());
	}

	@Test
	public void update() {
		Location location = locationDAO.create("Update Test " + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		location.setName("Updated Name");
		location.setAddress("Updated Address");
		Location updated = locationDAO.update(location);
		dbInstance.commitAndCloseSession();

		Location reloaded = locationDAO.loadByKey(updated);
		Assert.assertEquals("Updated Name", reloaded.getName());
		Assert.assertEquals("Updated Address", reloaded.getAddress());
	}

	@Test
	public void loadByExternalId() {
		String extId = "ext-" + UUID.randomUUID();
		Location location = locationDAO.create("Ext ID Test");
		location.setExternalId(extId);
		locationDAO.update(location);
		dbInstance.commitAndCloseSession();

		Location found = locationDAO.loadByExternalId(extId);
		Assert.assertNotNull(found);
		Assert.assertEquals(extId, found.getExternalId());
	}

	@Test
	public void searchByName() {
		String uniqueName = "UniqueLocName_" + UUID.randomUUID();
		locationDAO.create(uniqueName);
		dbInstance.commitAndCloseSession();

		SearchLocationParameters params = new SearchLocationParameters();
		params.setSearchString(uniqueName.substring(0, 14));
		List<Location> results = locationDAO.search(params);

		Assertions.assertThat(results)
				.extracting(Location::getName)
				.contains(uniqueName);
	}

	@Test
	public void searchByStatus() {
		String name = "StatusTest_" + UUID.randomUUID();
		Location location = locationDAO.create(name);
		location.setStatus(RoomStatus.inactive);
		locationDAO.update(location);
		dbInstance.commitAndCloseSession();

		SearchLocationParameters activeOnly = new SearchLocationParameters();
		activeOnly.setStatus(List.of(RoomStatus.active));
		List<Location> activeResults = locationDAO.search(activeOnly);
		Assertions.assertThat(activeResults)
				.extracting(Location::getName)
				.doesNotContain(name);

		SearchLocationParameters inactiveOnly = new SearchLocationParameters();
		inactiveOnly.setStatus(List.of(RoomStatus.inactive));
		List<Location> inactiveResults = locationDAO.search(inactiveOnly);
		Assertions.assertThat(inactiveResults)
				.extracting(Location::getName)
				.contains(name);
	}

	@Test
	public void softDelete() {
		Location location = locationDAO.create("ToDelete_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		int rows = locationDAO.delete(location);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, rows);
		Location reloaded = locationDAO.loadByKey(location);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(RoomStatus.deleted, reloaded.getStatus());
	}

	@Test
	public void count() {
		String uniqueName = "CountTest_" + UUID.randomUUID();
		locationDAO.create(uniqueName);
		dbInstance.commitAndCloseSession();

		SearchLocationParameters params = new SearchLocationParameters();
		params.setSearchString(uniqueName);
		long count = locationDAO.count(params);
		Assert.assertEquals(1L, count);
	}

	@Test
	public void addAndRemoveOrganisation() {
		Location location = locationDAO.create("OrgTest_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Organisation org = organisationService.getDefaultOrganisation();
		locationDAO.addOrganisation(location, org);
		dbInstance.commitAndCloseSession();

		List<Organisation> orgs = locationDAO.getOrganisations(location);
		Assertions.assertThat(orgs).isNotEmpty();
		Assertions.assertThat(orgs).extracting(Organisation::getKey).contains(org.getKey());

		locationDAO.removeOrganisation(location, org);
		dbInstance.commitAndCloseSession();

		List<Organisation> orgsAfterRemove = locationDAO.getOrganisations(location);
		Assertions.assertThat(orgsAfterRemove).extracting(Organisation::getKey).doesNotContain(org.getKey());
	}
}
