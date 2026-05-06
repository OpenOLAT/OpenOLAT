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
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LocationDAO locationDAO;
	@Autowired
	private RoomDAO roomDAO;
	@Autowired
	private OrganisationService organisationService;

	private Location createLocation() {
		Location loc = locationDAO.create("TestLoc_" + UUID.randomUUID());
		dbInstance.getCurrentEntityManager().flush();
		return loc;
	}

	@Test
	public void createAndLoad() {
		Location loc = createLocation();
		Room room = roomDAO.create(loc, "Room A_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Room reloaded = roomDAO.loadByKey(room);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(room.getKey(), reloaded.getKey());
		Assert.assertEquals(RoomStatus.active, reloaded.getStatus());
	}

	@Test
	public void update() {
		Location loc = createLocation();
		Room room = roomDAO.create(loc, "UpdateRoom_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		room.setSeats(30);
		room.setDescription("Nice room");
		Room updated = roomDAO.update(room);
		dbInstance.commitAndCloseSession();

		Room reloaded = roomDAO.loadByKey(updated);
		Assert.assertEquals(Integer.valueOf(30), reloaded.getSeats());
		Assert.assertEquals("Nice room", reloaded.getDescription());
	}

	@Test
	public void loadByExternalId() {
		String extId = "room-ext-" + UUID.randomUUID();
		Location loc = createLocation();
		Room room = roomDAO.create(loc, "ExtRoom");
		room.setExternalId(extId);
		roomDAO.update(room);
		dbInstance.commitAndCloseSession();

		Room found = roomDAO.loadByExternalId(extId);
		Assert.assertNotNull(found);
		Assert.assertEquals(extId, found.getExternalId());
	}

	@Test
	public void loadByExternalRefIfUnique() {
		String extRef = "ref-" + UUID.randomUUID();
		Location loc = createLocation();
		Room room = roomDAO.create(loc, "RefRoom");
		room.setExternalRef(extRef);
		roomDAO.update(room);
		dbInstance.commitAndCloseSession();

		Room found = roomDAO.loadByExternalRefIfUnique(extRef);
		Assert.assertNotNull(found);
		Assert.assertEquals(extRef, found.getExternalRef());
	}

	@Test
	public void softDelete() {
		Location loc = createLocation();
		Room room = roomDAO.create(loc, "DeleteRoom_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		int rows = roomDAO.delete(room);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, rows);
		Room reloaded = roomDAO.loadByKey(room);
		Assert.assertEquals(RoomStatus.deleted, reloaded.getStatus());
	}

	@Test
	public void getRoomsForLocation() {
		Location loc = createLocation();
		Room r1 = roomDAO.create(loc, "RoomForLoc_A_" + UUID.randomUUID());
		Room r2 = roomDAO.create(loc, "RoomForLoc_B_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		List<Room> rooms = roomDAO.getRoomsForLocation(loc);
		Assertions.assertThat(rooms)
				.extracting(Room::getKey)
				.contains(r1.getKey(), r2.getKey());
	}

	@Test
	public void search_openToAll_visibleForAnyIdentity() {
		// A location with no org links is visible to any identity
		Location loc = createLocation();
		String uniqueName = "OpenRoom_" + UUID.randomUUID();
		roomDAO.create(loc, uniqueName);
		dbInstance.commitAndCloseSession();

		Identity anyUser = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-open-user");
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setSearchString(uniqueName);
		params.setIdentity(anyUser);
		List<Room> rooms = roomDAO.search(params);

		Assertions.assertThat(rooms)
				.extracting(Room::getName)
				.contains(uniqueName);
	}

	@Test
	public void search_orgScoped_visibleOnlyForMember() {
		Organisation org = organisationService.createOrganisation(
				"RmTestOrg_" + UUID.randomUUID(), UUID.randomUUID().toString(), null,
				organisationService.getDefaultOrganisation(), null, null);
		Location loc = createLocation();
		locationDAO.addOrganisation(loc, org);
		String uniqueName = "OrgRoom_" + UUID.randomUUID();
		roomDAO.create(loc, uniqueName);
		dbInstance.commitAndCloseSession();

		// Member with "user" role
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-member");
		organisationService.addMember(org, member, OrganisationRoles.user, null);
		dbInstance.commitAndCloseSession();

		// Non-member
		Identity nonMember = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-nonmember");
		dbInstance.commitAndCloseSession();

		SearchRoomParameters paramsNonMember = new SearchRoomParameters();
		paramsNonMember.setSearchString(uniqueName);
		paramsNonMember.setIdentity(nonMember);
		List<Room> nonMemberRooms = roomDAO.search(paramsNonMember);
		Assertions.assertThat(nonMemberRooms)
				.extracting(Room::getName)
				.doesNotContain(uniqueName);
	}

	@Test
	public void search_byLocation() {
		Location loc1 = createLocation();
		Location loc2 = createLocation();
		String name1 = "SearchByLoc_A_" + UUID.randomUUID();
		String name2 = "SearchByLoc_B_" + UUID.randomUUID();
		roomDAO.create(loc1, name1);
		roomDAO.create(loc2, name2);
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setLocation(loc1);
		List<Room> rooms = roomDAO.search(params);
		Assertions.assertThat(rooms).extracting(Room::getName).contains(name1).doesNotContain(name2);
	}

	@Test
	public void search_deletedRooms_excluded() {
		Location loc = createLocation();
		String name = "DeletedRoom_" + UUID.randomUUID();
		Room room = roomDAO.create(loc, name);
		dbInstance.commitAndCloseSession();
		roomDAO.delete(room);
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setSearchString(name);
		List<Room> rooms = roomDAO.search(params);
		Assertions.assertThat(rooms).extracting(Room::getName).doesNotContain(name);
	}
}
