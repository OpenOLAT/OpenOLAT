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
package org.olat.modules.roommanagement.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.restapi.RestConnection;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 18 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomManagementWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private RoomManagementService roomManagementService;

	private static IdentityWithLogin admin;
	private static IdentityWithLogin author;
	private static IdentityWithLogin plainUser;

	@Before
	public void setUp() {
		if (admin == null) {
			admin = JunitTestHelper.createAndPersistRndAdmin("rm-rest-admin");
			author = JunitTestHelper.createAndPersistRndAuthor("rm-rest-author");
			plainUser = JunitTestHelper.createAndPersistRndUser("rm-rest-user");
			dbInstance.commitAndCloseSession();
		}
		if (!roomManagementModule.isEnabled()) {
			roomManagementModule.setEnabled(true);
		}
	}

	@Test
	public void getBuildings_sysadmin_returnsAll()
	throws IOException, URISyntaxException {
		String uniqueName = "TestBldSysAdmin_" + UUID.randomUUID();
		Building building = roomManagementService.createBuilding(uniqueName, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.queryParam("search", uniqueName).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		BuildingVO[] vos = conn.parse(response, BuildingVO[].class);
		Assert.assertNotNull(vos);
		boolean found = Arrays.stream(vos).anyMatch(v -> uniqueName.equals(v.getName()));
		Assert.assertTrue("Expected to find building with name " + uniqueName, found);

		Assert.assertNotNull(building);
	}

	@Test
	public void getBuildings_plainUser_returns403()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(plainUser);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings").build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getBuilding_returns200()
	throws IOException, URISyntaxException {
		String uniqueName = "TestBldDetail_" + UUID.randomUUID();
		Building building = roomManagementService.createBuilding(uniqueName, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.path(building.getKey().toString()).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		BuildingVO vo = conn.parse(response, BuildingVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(building.getKey(), vo.getKey());
	}

	@Test
	public void getBuilding_unknownKey_returns404()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.path("9999999").build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getBuilding_notInScope_returns404()
	throws IOException, URISyntaxException {
		Organisation orgB = organisationService.createOrganisation(
				"OrgB-" + UUID.randomUUID(), "OrgB-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());

		Building building = roomManagementService.createBuilding("ScopedBldOrgB_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.updateBuilding(building, List.of(orgB), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		IdentityWithLogin author2 = JunitTestHelper.createAndPersistRndAuthor("rm-rest-author2");
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(author2);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.path(building.getKey().toString()).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getBuildings_author_scopedToOwnOrg()
	throws IOException, URISyntaxException {
		Organisation orgC = organisationService.createOrganisation(
				"OrgC-" + UUID.randomUUID(), "OrgC-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());
		Organisation orgD = organisationService.createOrganisation(
				"OrgD-" + UUID.randomUUID(), "OrgD-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());

		IdentityWithLogin author3 = JunitTestHelper.createAndPersistRndAuthor("rm-rest-author3");
		organisationService.addMember(orgC, author3.getIdentity(), OrganisationRoles.user, admin.getIdentity());

		String prefix = "ScopedBld_" + UUID.randomUUID() + "_";
		Building buildingInOrgC = roomManagementService.createBuilding(prefix + "OrgC", admin.getIdentity());
		roomManagementService.updateBuilding(buildingInOrgC, List.of(orgC), admin.getIdentity());

		Building buildingInDefaultOrg = roomManagementService.createBuilding(prefix + "Default", admin.getIdentity());

		Building buildingInOnlyOtherOrg = roomManagementService.createBuilding(prefix + "OrgD", admin.getIdentity());
		roomManagementService.updateBuilding(buildingInOnlyOtherOrg, List.of(orgD), admin.getIdentity());

		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(author3);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.queryParam("search", prefix).queryParam("pageSize", 200).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		BuildingVO[] vos = conn.parse(response, BuildingVO[].class);
		Assert.assertNotNull(vos);

		List<Long> keys = Arrays.stream(vos).map(BuildingVO::getKey).toList();
		Assert.assertTrue("buildingInOrgC should be visible", keys.contains(buildingInOrgC.getKey()));
		Assert.assertTrue("buildingInDefaultOrg should be visible", keys.contains(buildingInDefaultOrg.getKey()));
		Assert.assertFalse("buildingInOnlyOtherOrg should not be visible", keys.contains(buildingInOnlyOtherOrg.getKey()));
	}

	@Test
	public void getBuildings_paged()
	throws IOException, URISyntaxException {
		String prefix = "PagedBld_" + UUID.randomUUID() + "_";
		roomManagementService.createBuilding(prefix + "A", admin.getIdentity());
		roomManagementService.createBuilding(prefix + "B", admin.getIdentity());
		roomManagementService.createBuilding(prefix + "C", admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("buildings")
				.queryParam("pageSize", 2)
				.queryParam("search", prefix)
				.build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		BuildingVO[] vos = conn.parse(response, BuildingVO[].class);
		Assert.assertNotNull(vos);
		Assert.assertEquals(2, vos.length);

		String totalCount = response.getFirstHeader("X-Total-Count").getValue();
		Assert.assertEquals("3", totalCount);
	}

	@Test
	public void getRooms_sysadmin_returnsAll()
	throws IOException, URISyntaxException {
		Building building = roomManagementService.createBuilding("BldForRoomSysAdmin_" + UUID.randomUUID(), admin.getIdentity());
		String uniqueName = "TestRoomSysAdmin_" + UUID.randomUUID();
		Room room = roomManagementService.createRoom(building, uniqueName, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.queryParam("search", uniqueName).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomVO[] vos = conn.parse(response, RoomVO[].class);
		Assert.assertNotNull(vos);
		boolean found = Arrays.stream(vos).anyMatch(v -> room.getKey().equals(v.getKey()));
		Assert.assertTrue("Expected to find room with key " + room.getKey(), found);
	}

	@Test
	public void getRooms_planUser_returns403()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(plainUser);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms").build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getRoom_returns200()
	throws IOException, URISyntaxException {
		Building building = roomManagementService.createBuilding("BldForRoomDetail_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "TestRoomDetail_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.path(room.getKey().toString()).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomVO vo = conn.parse(response, RoomVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(room.getKey(), vo.getKey());
	}

	@Test
	public void getRoom_unknownKey_returns404()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.path("9999999").build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getRoom_notInScope_returns404()
	throws IOException, URISyntaxException {
		Organisation orgRoom = organisationService.createOrganisation(
				"OrgRoom-" + UUID.randomUUID(), "OrgRoom-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());

		Building building = roomManagementService.createBuilding("BldForScopedRoom_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.updateBuilding(building, List.of(orgRoom), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "ScopedRoom_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		IdentityWithLogin outsideAuthor = JunitTestHelper.createAndPersistRndAuthor("rm-rest-outside-author");
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(outsideAuthor);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.path(room.getKey().toString()).build();
		HttpGet method = conn.createGet(request, "application/json", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
	}

	@Test
	public void getRooms_adminInfoMasked_forNonAdmin()
	throws IOException, URISyntaxException {
		Building building = roomManagementService.createBuilding("BldForAdminInfo_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomAdminInfo_" + UUID.randomUUID(), admin.getIdentity());
		room.setAdminInfo("secret-admin-info");
		roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection adminConn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.path(room.getKey().toString()).build();
		HttpGet adminMethod = adminConn.createGet(request, "application/json", true);
		HttpResponse adminResponse = adminConn.execute(adminMethod);
		Assert.assertEquals(200, adminResponse.getStatusLine().getStatusCode());
		RoomVO adminVo = adminConn.parse(adminResponse, RoomVO.class);
		Assert.assertNotNull(adminVo);
		Assert.assertNotNull("Admin should see adminInfo", adminVo.getAdminInfo());

		RestConnection authorConn = new RestConnection(author);
		HttpGet authorMethod = authorConn.createGet(request, "application/json", true);
		HttpResponse authorResponse = authorConn.execute(authorMethod);
		Assert.assertEquals(200, authorResponse.getStatusLine().getStatusCode());
		RoomVO authorVo = authorConn.parse(authorResponse, RoomVO.class);
		Assert.assertNotNull(authorVo);
		Assert.assertNull("Author should NOT see adminInfo", authorVo.getAdminInfo());
	}

	@Test
	public void getRooms_externalIdMasked_forNonAdmin()
	throws IOException, URISyntaxException {
		Building building = roomManagementService.createBuilding("BldForExtId_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomExtId_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("ext-id-secret-" + UUID.randomUUID());
		roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection adminConn = new RestConnection(admin);
		URI request = UriBuilder.fromUri(getContextURI()).path("rm").path("rooms")
				.path(room.getKey().toString()).build();
		HttpGet adminMethod = adminConn.createGet(request, "application/json", true);
		HttpResponse adminResponse = adminConn.execute(adminMethod);
		Assert.assertEquals(200, adminResponse.getStatusLine().getStatusCode());
		RoomVO adminVo = adminConn.parse(adminResponse, RoomVO.class);
		Assert.assertNotNull(adminVo);
		Assert.assertNotNull("Admin should see externalId", adminVo.getExternalId());

		RestConnection authorConn = new RestConnection(author);
		HttpGet authorMethod = authorConn.createGet(request, "application/json", true);
		HttpResponse authorResponse = authorConn.execute(authorMethod);
		Assert.assertEquals(200, authorResponse.getStatusLine().getStatusCode());
		RoomVO authorVo = authorConn.parse(authorResponse, RoomVO.class);
		Assert.assertNotNull(authorVo);
		Assert.assertNull("Author should NOT see externalId", authorVo.getExternalId());
	}
}
