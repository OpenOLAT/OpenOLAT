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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.hibernate.stat.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.RestConnection;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for {@link RoomManagementWebService#getBookings(String, jakarta.servlet.http.HttpServletRequest)},
 * the batch room-booking search across a list of lecture blocks ({@code GET /rm/bookings}).
 *
 * Initial date: 8 Sep 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomManagementWebServiceBookingsTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private RoomManagementService roomManagementService;

	private static IdentityWithLogin admin;
	private static IdentityWithLogin plainUser;

	@Before
	public void setUp() {
		if (admin == null) {
			admin = JunitTestHelper.createAndPersistRndAdmin("rm-bk-admin");
			plainUser = JunitTestHelper.createAndPersistRndUser("rm-bk-user");
			dbInstance.commitAndCloseSession();
		}
		if (!roomManagementModule.isEnabled()) {
			roomManagementModule.setEnabled(true);
		}
	}

	private LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlock block = lectureService.createLectureBlock(entry);
		block.setStartDate(new Date());
		block.setEndDate(new Date());
		block.setTitle("Bookings test block " + UUID.randomUUID());
		block.setPlannedLecturesNumber(4);
		return lectureService.save(block, null);
	}

	private URI buildBookingsUri(IdentityWithLogin actingAs, String lectureBlockKeys) throws URISyntaxException {
		UriBuilder builder = UriBuilder.fromUri(getContextURI()).path("rm").path("bookings");
		if (lectureBlockKeys != null) {
			builder = builder.queryParam("lectureBlockKeys", lectureBlockKeys);
		}
		return builder.build();
	}

	// ---------- Happy path / grouping / order ----------

	@Test
	public void getBookings_threeBlocks_fiveBookings_groupedByLectureBlockKey()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldHappy_" + UUID.randomUUID(), admin.getIdentity());

		LectureBlock block1 = createLectureBlock(entry);
		LectureBlock block2 = createLectureBlock(entry);
		LectureBlock block3 = createLectureBlock(entry);

		Room room1 = roomManagementService.createRoom(building, "RoomHappy1_" + UUID.randomUUID(), admin.getIdentity());
		Room room2a = roomManagementService.createRoom(building, "RoomHappy2a_" + UUID.randomUUID(), admin.getIdentity());
		Room room2b = roomManagementService.createRoom(building, "RoomHappy2b_" + UUID.randomUUID(), admin.getIdentity());
		Room room3a = roomManagementService.createRoom(building, "RoomHappy3a_" + UUID.randomUUID(), admin.getIdentity());
		Room room3b = roomManagementService.createRoom(building, "RoomHappy3b_" + UUID.randomUUID(), admin.getIdentity());

		roomManagementService.bookRoom(room1, block1, block1.getStartDate(), block1.getEndDate(), 0, 0, admin.getIdentity());
		// block2 and block3 are each booked in two rooms at once -- the case the old single-room GET used to hide
		roomManagementService.bookRoom(room2a, block2, block2.getStartDate(), block2.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(room2b, block2, block2.getStartDate(), block2.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(room3a, block3, block3.getStartDate(), block3.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(room3b, block3, block3.getStartDate(), block3.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		String keysParam = block3.getKey() + "," + block1.getKey() + "," + block2.getKey();
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertNotNull(vos);
		Assert.assertEquals(5, vos.length);

		List<RoomBookingVO> block2Bookings = List.of(vos).stream()
				.filter(v -> block2.getKey().equals(v.getLectureBlockKey()))
				.toList();
		Assert.assertEquals("Block2 has two rooms, must yield two entries with the same lectureBlockKey",
				2, block2Bookings.size());

		// Order: lectureBlock.key asc, startDate asc, room.externalRef asc -- even though the
		// query param listed the keys as 3,1,2
		List<Long> lectureBlockKeyOrder = List.of(vos).stream().map(RoomBookingVO::getLectureBlockKey).toList();
		List<Long> sorted = new ArrayList<>(lectureBlockKeyOrder);
		sorted.sort(Long::compareTo);
		Assert.assertEquals(sorted, lectureBlockKeyOrder);
	}

	@Test
	public void getBookings_order_stableAcrossMixedInputOrder()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldOrder_" + UUID.randomUUID(), admin.getIdentity());

		LectureBlock block = createLectureBlock(entry);
		Room roomB = roomManagementService.createRoom(building, "RoomOrderB_" + UUID.randomUUID(), admin.getIdentity());
		roomB.setExternalRef("B_" + UUID.randomUUID());
		roomB = roomManagementService.updateRoom(roomB, admin.getIdentity());
		Room roomA = roomManagementService.createRoom(building, "RoomOrderA_" + UUID.randomUUID(), admin.getIdentity());
		roomA.setExternalRef("A_" + UUID.randomUUID());
		roomA = roomManagementService.updateRoom(roomA, admin.getIdentity());

		// Same start/end date on the same block, so the tie-break must fall to room.externalRef
		roomManagementService.bookRoom(roomB, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(roomA, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, String.valueOf(block.getKey()));
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertEquals(2, vos.length);
		Assert.assertEquals(List.of(roomA.getKey(), roomB.getKey()),
				List.of(vos).stream().map(RoomBookingVO::getRoomKey).toList());
	}

	// ---------- Parameter validation ----------

	@Test
	public void getBookings_missingParameter_returns400()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, null);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_blankParameter_returns400()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, "   ");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_fullyUnparsableParameter_returns400()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, "abc");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_partiallyUnparsableParameter_returnsBookingsOfValidKeys()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldPartial_" + UUID.randomUUID(), admin.getIdentity());
		LectureBlock block1 = createLectureBlock(entry);
		LectureBlock block2 = createLectureBlock(entry);
		Room room1 = roomManagementService.createRoom(building, "RoomPartial1_" + UUID.randomUUID(), admin.getIdentity());
		Room room2 = roomManagementService.createRoom(building, "RoomPartial2_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room1, block1, block1.getStartDate(), block1.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(room2, block2, block2.getStartDate(), block2.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		String keysParam = block1.getKey() + ",abc," + block2.getKey();
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertEquals(2, vos.length);
	}

	// ---------- Unknown / duplicate keys ----------

	@Test
	public void getBookings_unknownKey_ignoredSilently()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldUnknown_" + UUID.randomUUID(), admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Room room = roomManagementService.createRoom(building, "RoomUnknown_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		String keysParam = block.getKey() + ",999999999";
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertEquals(1, vos.length);
	}

	@Test
	public void getBookings_duplicateKey_doesNotDuplicateRows()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldDup_" + UUID.randomUUID(), admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Room room = roomManagementService.createRoom(building, "RoomDup_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		String keysParam = block.getKey() + "," + block.getKey();
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertEquals(1, vos.length);
	}

	// ---------- Cap ----------

	@Test
	public void getBookings_500Keys_pass()
	throws IOException, URISyntaxException {
		List<Long> keys = new ArrayList<>();
		for (long i = 1; i <= 500; i++) {
			keys.add(i);
		}
		String keysParam = keys.stream().map(String::valueOf).collect(Collectors.joining(","));

		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_501Keys_returns400TooMany()
	throws IOException, URISyntaxException {
		List<Long> keys = new ArrayList<>();
		for (long i = 1; i <= 501; i++) {
			keys.add(i);
		}
		String keysParam = keys.stream().map(String::valueOf).collect(Collectors.joining(","));

		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, keysParam);
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("lectureBlockKeys.tooMany", body.get("code"));
		Assert.assertEquals(500, ((Number) body.get("max")).intValue());
	}

	// ---------- Empty result ----------

	@Test
	public void getBookings_validKeysWithoutBookings_returnsEmptyArray()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, String.valueOf(block.getKey()));
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(response, RoomBookingVO[].class);
		Assert.assertNotNull(vos);
		Assert.assertEquals(0, vos.length);
	}

	// ---------- Module ----------

	@Test
	public void getBookings_moduleDisabled_returns404()
	throws IOException, URISyntaxException {
		roomManagementModule.setEnabled(false);
		try {
			RestConnection conn = new RestConnection(admin);
			URI request = buildBookingsUri(admin, "1");
			HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

			Assert.assertEquals(404, response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		} finally {
			roomManagementModule.setEnabled(true);
		}
	}

	// ---------- Roles ----------

	@Test
	public void getBookings_administrator_isAuthorised()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(admin);
		URI request = buildBookingsUri(admin, "1");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_lectureManager_isAuthorised()
	throws IOException, URISyntaxException {
		IdentityWithLogin lectureManager = JunitTestHelper.createAndPersistRndUser("rm-bk-lm-" + UUID.randomUUID());
		organisationService.addMember(organisationService.getDefaultOrganisation(),
				lectureManager.getIdentity(), OrganisationRoles.lecturemanager, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(lectureManager);
		URI request = buildBookingsUri(lectureManager, "1");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_author_returns403()
	throws IOException, URISyntaxException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("rm-bk-author-" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(author);
		URI request = buildBookingsUri(author, "1");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_learnResourceManager_returns403()
	throws IOException, URISyntaxException {
		IdentityWithLogin lrm = JunitTestHelper.createAndPersistRndUser("rm-bk-lrm-" + UUID.randomUUID());
		organisationService.addMember(organisationService.getDefaultOrganisation(),
				lrm.getIdentity(), OrganisationRoles.learnresourcemanager, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(lrm);
		URI request = buildBookingsUri(lrm, "1");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getBookings_plainUser_returns403()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(plainUser);
		URI request = buildBookingsUri(plainUser, "1");
		HttpResponse response = conn.execute(conn.createGet(request, "application/json", true));

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	// ---------- Visibility ----------

	@Test
	public void getBookings_foreignOrgBuilding_omittedForOrgAdmin_returnedForSysAdmin()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);

		Organisation foreignOrg = organisationService.createOrganisation(
				"OrgForeign-" + UUID.randomUUID(), "OrgForeign-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldForeign_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.updateBuilding(building, List.of(foreignOrg), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomForeign_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());

		Organisation ownOrg = organisationService.createOrganisation(
				"OrgOwn-" + UUID.randomUUID(), "OrgOwn-" + UUID.randomUUID(), "",
				null, null, admin.getIdentity());
		IdentityWithLogin orgAdmin = JunitTestHelper.createAndPersistRndUser("rm-bk-orgadmin-" + UUID.randomUUID());
		organisationService.addMember(ownOrg, orgAdmin.getIdentity(), OrganisationRoles.administrator, admin.getIdentity());

		IdentityWithLogin sysAdmin = JunitTestHelper.createAndPersistRndUser("rm-bk-sysadmin-" + UUID.randomUUID());
		organisationService.addMember(organisationService.getDefaultOrganisation(),
				sysAdmin.getIdentity(), OrganisationRoles.sysadmin, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		URI request = buildBookingsUri(orgAdmin, String.valueOf(block.getKey()));

		RestConnection orgAdminConn = new RestConnection(orgAdmin);
		HttpResponse orgAdminResponse = orgAdminConn.execute(orgAdminConn.createGet(request, "application/json", true));
		Assert.assertEquals(200, orgAdminResponse.getStatusLine().getStatusCode());
		RoomBookingVO[] orgAdminVos = orgAdminConn.parse(orgAdminResponse, RoomBookingVO[].class);
		Assert.assertEquals("Booking in a foreign-org-restricted building must be omitted for an org admin",
				0, orgAdminVos.length);

		RestConnection sysAdminConn = new RestConnection(sysAdmin);
		HttpResponse sysAdminResponse = sysAdminConn.execute(sysAdminConn.createGet(request, "application/json", true));
		Assert.assertEquals(200, sysAdminResponse.getStatusLine().getStatusCode());
		RoomBookingVO[] sysAdminVos = sysAdminConn.parse(sysAdminResponse, RoomBookingVO[].class);
		Assert.assertEquals("A system administrator must see the booking regardless of the building's organisation",
				1, sysAdminVos.length);
	}

	// ---------- Query count ----------

	/**
	 * The plan's own claim that this pattern mirrors "existing performance tests
	 * in org.olat.test" does not hold -- no such precedent exists anywhere in
	 * this codebase (verified by grepping for {@code getPrepareStatementCount}
	 * and Hibernate {@code Statistics} usage before writing this test). The
	 * capability is real, though: {@link DB#getStatistics()} already exposes the
	 * underlying Hibernate {@link Statistics}, with {@code hibernate.generate_statistics}
	 * enabled in {@code persistence.xml}. Because this is a REST-level test (an
	 * embedded Undertow server in the same JVM/Spring context, not an isolated
	 * DAO call), the raw statement count also includes framework-level queries
	 * (authentication, role lookups) unrelated to the booking search. Asserting
	 * an exact count would be fragile, so this asserts what the DAO's fetch-joined
	 * query (Phase 2) actually guarantees: the statement count does not scale
	 * with the number of lecture block keys.
	 */
	@Test
	public void getBookings_20Keys_doesNotScaleWithKeyCount()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		Building building = roomManagementService.createBuilding("BldQueryCount_" + UUID.randomUUID(), admin.getIdentity());

		List<Long> keys = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			LectureBlock block = createLectureBlock(entry);
			Room room = roomManagementService.createRoom(building, "RoomQueryCount" + i + "_" + UUID.randomUUID(), admin.getIdentity());
			roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
			keys.add(block.getKey());
		}
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		Statistics statistics = dbInstance.getStatistics();
		Assert.assertNotNull("Hibernate statistics must be enabled (hibernate.generate_statistics)", statistics);

		// Baseline: the identical request shape (same identity, same code path,
		// same connection/session) with a single key. The REST pipeline's own
		// per-request overhead (authentication, role lookups) is not under test
		// here and must not be mistaken for the booking query itself.
		URI oneKeyRequest = buildBookingsUri(admin, String.valueOf(keys.get(0)));
		statistics.clear();
		HttpResponse oneKeyResponse = conn.execute(conn.createGet(oneKeyRequest, "application/json", true));
		Assert.assertEquals(200, oneKeyResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(oneKeyResponse.getEntity());
		long statementsForOneKey = statistics.getPrepareStatementCount();

		String keysParam = keys.stream().map(String::valueOf).collect(Collectors.joining(","));
		URI twentyKeysRequest = buildBookingsUri(admin, keysParam);
		statistics.clear();
		HttpResponse twentyKeysResponse = conn.execute(conn.createGet(twentyKeysRequest, "application/json", true));
		Assert.assertEquals(200, twentyKeysResponse.getStatusLine().getStatusCode());
		RoomBookingVO[] vos = conn.parse(twentyKeysResponse, RoomBookingVO[].class);
		Assert.assertEquals(20, vos.length);
		long statementsForTwentyKeys = statistics.getPrepareStatementCount();

		// The bookings query is a single `in (:lbKeys)` statement regardless of key
		// count (Phase 2's fetch-joined DAO query), so going from 1 to 20 keys must
		// not add anywhere near 19 more statements the way an N+1 pattern would.
		long delta = statementsForTwentyKeys - statementsForOneKey;
		Assert.assertTrue("Expected the statement count not to scale with key count (1 key: "
				+ statementsForOneKey + ", 20 keys: " + statementsForTwentyKeys + ")", delta < 10);
	}
}
