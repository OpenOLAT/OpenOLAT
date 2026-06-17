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
import org.olat.modules.roommanagement.Building;
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
	private BuildingDAO buildingDAO;
	@Autowired
	private RoomDAO roomDAO;
	@Autowired
	private OrganisationService organisationService;

	private Building createBuilding() {
		Building bld = buildingDAO.create("TestBld_" + UUID.randomUUID());
		dbInstance.getCurrentEntityManager().flush();
		return bld;
	}

	@Test
	public void createAndLoad() {
		Building bld = createBuilding();
		Room room = roomDAO.create(bld, "Room A_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		Room reloaded = roomDAO.loadByKey(room);
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(room.getKey(), reloaded.getKey());
		Assert.assertEquals(RoomStatus.active, reloaded.getStatus());
	}

	@Test
	public void update() {
		Building bld = createBuilding();
		Room room = roomDAO.create(bld, "UpdateRoom_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		room.setSeats(30);
		room.setRoomInfo("Nice room");
		Room updated = roomDAO.update(room);
		dbInstance.commitAndCloseSession();

		Room reloaded = roomDAO.loadByKey(updated);
		Assert.assertEquals(Integer.valueOf(30), reloaded.getSeats());
		Assert.assertEquals("Nice room", reloaded.getRoomInfo());
	}

	@Test
	public void loadByExternalId() {
		String extId = "room-ext-" + UUID.randomUUID();
		Building bld = createBuilding();
		Room room = roomDAO.create(bld, "ExtRoom");
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
		Building bld = createBuilding();
		Room room = roomDAO.create(bld, "RefRoom");
		room.setExternalRef(extRef);
		roomDAO.update(room);
		dbInstance.commitAndCloseSession();

		Room found = roomDAO.loadByExternalRefIfUnique(extRef);
		Assert.assertNotNull(found);
		Assert.assertEquals(extRef, found.getExternalRef());
	}

	@Test
	public void softDelete() {
		Building bld = createBuilding();
		Room room = roomDAO.create(bld, "DeleteRoom_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		int rows = roomDAO.delete(room);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, rows);
		Room reloaded = roomDAO.loadByKey(room);
		Assert.assertEquals(RoomStatus.deleted, reloaded.getStatus());
	}

	@Test
	public void getRoomsForBuilding() {
		Building bld = createBuilding();
		Room r1 = roomDAO.create(bld, "RoomForBld_A_" + UUID.randomUUID());
		Room r2 = roomDAO.create(bld, "RoomForBld_B_" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		List<Room> rooms = roomDAO.getRoomsForBuilding(bld);
		Assertions.assertThat(rooms)
				.extracting(Room::getKey)
				.contains(r1.getKey(), r2.getKey());
	}

	@Test
	public void search_openToAll_visibleForAnyIdentity() {
		// A building with no org links is visible to any identity
		Building bld = createBuilding();
		String uniqueDesc = "OpenRoom_" + UUID.randomUUID();
		roomDAO.create(bld, uniqueDesc);
		dbInstance.commitAndCloseSession();

		Identity anyUser = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-open-user");
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setSearchString(uniqueDesc);
		params.setIdentity(anyUser);
		List<Room> rooms = roomDAO.search(params);

		Assertions.assertThat(rooms)
				.extracting(Room::getDescription)
				.contains(uniqueDesc);
	}

	@Test
	public void search_orgScoped_visibleOnlyForMember() {
		Organisation org = organisationService.createOrganisation(
				"RmTestOrg_" + UUID.randomUUID(), UUID.randomUUID().toString(), null,
				organisationService.getDefaultOrganisation(), null, null);
		Building bld = createBuilding();
		buildingDAO.addOrganisation(bld, org);
		String uniqueDesc = "OrgRoom_" + UUID.randomUUID();
		roomDAO.create(bld, uniqueDesc);
		dbInstance.commitAndCloseSession();

		// Member with "user" role
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-member");
		organisationService.addMember(org, member, OrganisationRoles.user, null);
		dbInstance.commitAndCloseSession();

		// Non-member
		Identity nonMember = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-nonmember");
		dbInstance.commitAndCloseSession();

		SearchRoomParameters paramsNonMember = new SearchRoomParameters();
		paramsNonMember.setSearchString(uniqueDesc);
		paramsNonMember.setIdentity(nonMember);
		List<Room> nonMemberRooms = roomDAO.search(paramsNonMember);
		Assertions.assertThat(nonMemberRooms)
				.extracting(Room::getDescription)
				.doesNotContain(uniqueDesc);
	}

	@Test
	public void search_byBuilding() {
		Building bld1 = createBuilding();
		Building bld2 = createBuilding();
		String desc1 = "SearchByBld_A_" + UUID.randomUUID();
		String desc2 = "SearchByBld_B_" + UUID.randomUUID();
		roomDAO.create(bld1, desc1);
		roomDAO.create(bld2, desc2);
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setBuilding(bld1);
		List<Room> rooms = roomDAO.search(params);
		Assertions.assertThat(rooms).extracting(Room::getDescription).contains(desc1).doesNotContain(desc2);
	}

	@Test
	public void search_deletedRooms_excluded() {
		Building bld = createBuilding();
		String desc = "DeletedRoom_" + UUID.randomUUID();
		Room room = roomDAO.create(bld, desc);
		dbInstance.commitAndCloseSession();
		roomDAO.delete(room);
		dbInstance.commitAndCloseSession();

		SearchRoomParameters params = new SearchRoomParameters();
		params.setSearchString(desc);
		List<Room> rooms = roomDAO.search(params);
		Assertions.assertThat(rooms).extracting(Room::getDescription).doesNotContain(desc);
	}
}
