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
package org.olat.modules.lecture.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.RoomModuleLog;
import org.olat.modules.roommanagement.RoomModuleLogAction;
import org.olat.modules.roommanagement.model.RoomModuleLogSearchParameters;
import org.olat.modules.roommanagement.restapi.RoomBookingVO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.RestConnection;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 19 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LectureBlockWebServiceRoomBookingTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private RoomManagementService roomManagementService;

	private static IdentityWithLogin admin;
	private static IdentityWithLogin plainUser;

	@Before
	public void setUp() {
		if (admin == null) {
			admin = JunitTestHelper.createAndPersistRndAdmin("rm-lb-admin");
			plainUser = JunitTestHelper.createAndPersistRndUser("rm-lb-user");
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
		block.setTitle("Test block " + UUID.randomUUID());
		block.setPlannedLecturesNumber(4);
		return lectureService.save(block, null);
	}

	private URI buildRoomUri(RepositoryEntry entry, LectureBlock block) throws URISyntaxException {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks")
				.path(block.getKey().toString()).path("room").build();
	}

	/**
	 * Wraps the given bookings in a JSON array and sends them as the body of a
	 * {@code PUT .../room}. An empty varargs list produces {@code []}, which
	 * clears all bookings of the block.
	 */
	private HttpResponse putBookings(RestConnection conn, URI uri, RoomBookingVO... vos)
	throws IOException, URISyntaxException {
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vos);
		return conn.execute(method);
	}

	private int countLogsForRoom(Room room) {
		RoomModuleLogSearchParameters logParams = new RoomModuleLogSearchParameters();
		logParams.setRoom(room);
		return roomManagementService.searchLogs(logParams).size();
	}

	// ---------- GET .../room ----------

	@Test
	public void getRoom_noBooking_returnsEmptyArray()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> bookings = conn.parseList(response, RoomBookingVO.class);
		Assert.assertNotNull(bookings);
		Assert.assertTrue(bookings.isEmpty());
	}

	@Test
	public void getRoom_withBooking_returnsVO_withBuffers()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldGetRoom_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomGetRoom_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("get-ext-id-" + UUID.randomUUID());
		room.setExternalRef("get-ext-ref-" + UUID.randomUUID());
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		Date startDate = new Date();
		Date endDate = new Date();
		roomManagementService.bookRoom(room, block, startDate, endDate, 10, 15, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> bookings = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(1, bookings.size());
		RoomBookingVO vo = bookings.get(0);
		Assert.assertEquals(Integer.valueOf(10), vo.getBufferBeforeMin());
		Assert.assertEquals(Integer.valueOf(15), vo.getBufferAfterMin());
		Assert.assertEquals(room.getKey(), vo.getRoomKey());
		Assert.assertEquals(room.getExternalId(), vo.getExternalId());
		Assert.assertEquals(room.getExternalRef(), vo.getExternalRef());
	}

	@Test
	public void getRoom_threeBookings_returnsAllInStableOrder()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldThree_" + UUID.randomUUID(), admin.getIdentity());

		Room roomB = roomManagementService.createRoom(building, "RoomThreeB_" + UUID.randomUUID(), admin.getIdentity());
		roomB.setExternalRef("B_" + UUID.randomUUID());
		roomB = roomManagementService.updateRoom(roomB, admin.getIdentity());

		Room roomA = roomManagementService.createRoom(building, "RoomThreeA_" + UUID.randomUUID(), admin.getIdentity());
		roomA.setExternalRef("A_" + UUID.randomUUID());
		roomA = roomManagementService.updateRoom(roomA, admin.getIdentity());

		Room roomC = roomManagementService.createRoom(building, "RoomThreeC_" + UUID.randomUUID(), admin.getIdentity());
		roomC.setExternalRef("C_" + UUID.randomUUID());
		roomC = roomManagementService.updateRoom(roomC, admin.getIdentity());

		// Same start/end date for all three, so the query must tie-break on externalRef
		roomManagementService.bookRoom(roomB, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(roomA, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(roomC, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);

		HttpResponse response1 = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		List<RoomBookingVO> first = conn.parseList(response1, RoomBookingVO.class);

		HttpResponse response2 = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response2.getStatusLine().getStatusCode());
		List<RoomBookingVO> second = conn.parseList(response2, RoomBookingVO.class);

		Assert.assertEquals(3, first.size());
		Assert.assertEquals(List.of(roomA.getKey(), roomB.getKey(), roomC.getKey()),
				first.stream().map(RoomBookingVO::getRoomKey).toList());
		Assert.assertEquals(
				first.stream().map(RoomBookingVO::getRoomKey).toList(),
				second.stream().map(RoomBookingVO::getRoomKey).toList());
	}

	@Test
	public void getRoom_moduleDisabled_returns404()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		roomManagementModule.setEnabled(false);
		try {
			RestConnection conn = new RestConnection(admin);
			URI uri = buildRoomUri(entry, block);
			HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));

			Assert.assertEquals(404, response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		} finally {
			roomManagementModule.setEnabled(true);
		}
	}

	@Test
	public void getRoom_callerWithoutBlockEditRights_returns403()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(plainUser);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	// ---------- PUT .../room : add ----------

	@Test
	public void putRoom_byInternalKey_createsBooking()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutKey_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomPutKey_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(room.getKey());
		bookingVO.setBufferBeforeMin(5);
		bookingVO.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(room.getKey(), result.get(0).getRoomKey());
	}

	@Test
	public void putRoom_byExternalId_createsBooking()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutExtId_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomPutExtId_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("ext-" + UUID.randomUUID());
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalId(room.getExternalId());
		bookingVO.setBufferBeforeMin(5);
		bookingVO.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(room.getKey(), result.get(0).getRoomKey());
	}

	@Test
	public void putRoom_byUniqueExternalRef_createsBooking()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutExtRef_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomPutExtRef_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalRef("unique-ref-" + UUID.randomUUID());
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalRef(room.getExternalRef());
		bookingVO.setBufferBeforeMin(5);
		bookingVO.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(room.getKey(), result.get(0).getRoomKey());
	}

	@Test
	public void putRoom_emptyBlock_twoRooms_createsBothBookings()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldTwoNew_" + UUID.randomUUID(), admin.getIdentity());
		Room room1 = roomManagementService.createRoom(building, "RoomTwoNew1_" + UUID.randomUUID(), admin.getIdentity());
		Room room2 = roomManagementService.createRoom(building, "RoomTwoNew2_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo1 = new RoomBookingVO();
		vo1.setRoomKey(room1.getKey());
		RoomBookingVO vo2 = new RoomBookingVO();
		vo2.setRoomKey(room2.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo1, vo2);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(2, result.size());
		List<Long> roomKeys = result.stream().map(RoomBookingVO::getRoomKey).toList();
		Assert.assertTrue(roomKeys.contains(room1.getKey()));
		Assert.assertTrue(roomKeys.contains(room2.getKey()));
	}

	@Test
	public void putRoom_mixedIdentifierTypes_createsAllBookings()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldMixed_" + UUID.randomUUID(), admin.getIdentity());

		Room roomByKey = roomManagementService.createRoom(building, "RoomMixedKey_" + UUID.randomUUID(), admin.getIdentity());

		Room roomByExtId = roomManagementService.createRoom(building, "RoomMixedExtId_" + UUID.randomUUID(), admin.getIdentity());
		roomByExtId.setExternalId("mixed-ext-id-" + UUID.randomUUID());
		roomByExtId = roomManagementService.updateRoom(roomByExtId, admin.getIdentity());

		Room roomByExtRef = roomManagementService.createRoom(building, "RoomMixedExtRef_" + UUID.randomUUID(), admin.getIdentity());
		roomByExtRef.setExternalRef("mixed-ext-ref-" + UUID.randomUUID());
		roomByExtRef = roomManagementService.updateRoom(roomByExtRef, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO voKey = new RoomBookingVO();
		voKey.setRoomKey(roomByKey.getKey());
		RoomBookingVO voExtId = new RoomBookingVO();
		voExtId.setExternalId(roomByExtId.getExternalId());
		RoomBookingVO voExtRef = new RoomBookingVO();
		voExtRef.setExternalRef(roomByExtRef.getExternalRef());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, voKey, voExtId, voExtRef);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(3, result.size());
		List<Long> roomKeys = result.stream().map(RoomBookingVO::getRoomKey).toList();
		Assert.assertTrue(roomKeys.contains(roomByKey.getKey()));
		Assert.assertTrue(roomKeys.contains(roomByExtId.getKey()));
		Assert.assertTrue(roomKeys.contains(roomByExtRef.getKey()));
	}

	// ---------- PUT .../room : keep / no-op ----------

	@Test
	public void putRoom_sameArrayTwice_isNoOp_noNewLogRows()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldNoOp_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomNoOp_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo = new RoomBookingVO();
		vo.setRoomKey(room.getKey());
		vo.setBufferBeforeMin(5);
		vo.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);

		HttpResponse response1 = putBookings(conn, uri, vo);
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		EntityUtils.consume(response1.getEntity());
		int logCountAfterFirst = countLogsForRoom(room);

		HttpResponse response2 = putBookings(conn, uri, vo);
		Assert.assertEquals(200, response2.getStatusLine().getStatusCode());
		EntityUtils.consume(response2.getEntity());
		int logCountAfterSecond = countLogsForRoom(room);

		Assert.assertEquals("An identical PUT must not write a new log row", logCountAfterFirst, logCountAfterSecond);
	}

	// ---------- PUT .../room : replace ----------

	@Test
	public void putRoom_replaceTwoRoomsWithTwoOtherRooms()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldRepl2to2_" + UUID.randomUUID(), admin.getIdentity());
		Room oldRoom1 = roomManagementService.createRoom(building, "RoomRepl2to2Old1_" + UUID.randomUUID(), admin.getIdentity());
		Room oldRoom2 = roomManagementService.createRoom(building, "RoomRepl2to2Old2_" + UUID.randomUUID(), admin.getIdentity());
		Room newRoom1 = roomManagementService.createRoom(building, "RoomRepl2to2New1_" + UUID.randomUUID(), admin.getIdentity());
		Room newRoom2 = roomManagementService.createRoom(building, "RoomRepl2to2New2_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(oldRoom1, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(oldRoom2, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo1 = new RoomBookingVO();
		vo1.setRoomKey(newRoom1.getKey());
		RoomBookingVO vo2 = new RoomBookingVO();
		vo2.setRoomKey(newRoom2.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo1, vo2);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		List<Long> roomKeys = result.stream().map(RoomBookingVO::getRoomKey).toList();
		Assert.assertEquals(2, result.size());
		Assert.assertTrue(roomKeys.contains(newRoom1.getKey()));
		Assert.assertTrue(roomKeys.contains(newRoom2.getKey()));
		Assert.assertFalse(roomKeys.contains(oldRoom1.getKey()));
		Assert.assertFalse(roomKeys.contains(oldRoom2.getKey()));
	}

	@Test
	public void putRoom_replaceTwoRoomsWithOne()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldRepl2to1_" + UUID.randomUUID(), admin.getIdentity());
		Room oldRoom1 = roomManagementService.createRoom(building, "RoomRepl2to1Old1_" + UUID.randomUUID(), admin.getIdentity());
		Room oldRoom2 = roomManagementService.createRoom(building, "RoomRepl2to1Old2_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(oldRoom1, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(oldRoom2, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo1 = new RoomBookingVO();
		vo1.setRoomKey(oldRoom1.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo1);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(oldRoom1.getKey(), result.get(0).getRoomKey());
	}

	@Test
	public void putRoom_replaceOneRoomWithThree()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldRepl1to3_" + UUID.randomUUID(), admin.getIdentity());
		Room oldRoom = roomManagementService.createRoom(building, "RoomRepl1to3Old_" + UUID.randomUUID(), admin.getIdentity());
		Room newRoom1 = roomManagementService.createRoom(building, "RoomRepl1to3New1_" + UUID.randomUUID(), admin.getIdentity());
		Room newRoom2 = roomManagementService.createRoom(building, "RoomRepl1to3New2_" + UUID.randomUUID(), admin.getIdentity());
		Room newRoom3 = roomManagementService.createRoom(building, "RoomRepl1to3New3_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(oldRoom, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo1 = new RoomBookingVO();
		vo1.setRoomKey(newRoom1.getKey());
		RoomBookingVO vo2 = new RoomBookingVO();
		vo2.setRoomKey(newRoom2.getKey());
		RoomBookingVO vo3 = new RoomBookingVO();
		vo3.setRoomKey(newRoom3.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo1, vo2, vo3);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		List<Long> roomKeys = result.stream().map(RoomBookingVO::getRoomKey).toList();
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(roomKeys.contains(newRoom1.getKey()));
		Assert.assertTrue(roomKeys.contains(newRoom2.getKey()));
		Assert.assertTrue(roomKeys.contains(newRoom3.getKey()));
		Assert.assertFalse(roomKeys.contains(oldRoom.getKey()));
	}

	@Test
	public void putRoom_replacesExistingBooking()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutReplace_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomPutReplace_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		// First PUT
		RoomBookingVO bookingVO1 = new RoomBookingVO();
		bookingVO1.setRoomKey(room.getKey());
		bookingVO1.setBufferBeforeMin(5);
		bookingVO1.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response1 = putBookings(conn, uri, bookingVO1);

		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		List<RoomBookingVO> result1 = conn.parseList(response1, RoomBookingVO.class);
		Assert.assertEquals(1, result1.size());
		Long bookingKey = result1.get(0).getKey();

		// Second PUT with different buffer
		RoomBookingVO bookingVO2 = new RoomBookingVO();
		bookingVO2.setRoomKey(room.getKey());
		bookingVO2.setBufferBeforeMin(99);
		bookingVO2.setBufferAfterMin(10);

		HttpResponse response2 = putBookings(conn, uri, bookingVO2);
		Assert.assertEquals(200, response2.getStatusLine().getStatusCode());
		EntityUtils.consume(response2.getEntity());

		// GET to verify updated buffer
		HttpGet getMethod = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		List<RoomBookingVO> getResult = conn.parseList(getResponse, RoomBookingVO.class);
		Assert.assertEquals(1, getResult.size());
		Assert.assertEquals(Integer.valueOf(99), getResult.get(0).getBufferBeforeMin());
		Assert.assertEquals(bookingKey, getResult.get(0).getKey());
	}

	// ---------- PUT .../room : remove ----------

	@Test
	public void putRoom_emptyArray_clearsAllBookings()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldClearPut_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomClearPut_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertTrue(result.isEmpty());
		Assert.assertTrue(roomManagementService.getBookings(block).isEmpty());
	}

	// ---------- PUT .../room : atomicity ----------

	@Test
	public void putRoom_atomicity_unknownRoomInMiddle_abortsWithoutWrites()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldAtomic_" + UUID.randomUUID(), admin.getIdentity());
		Room roomA = roomManagementService.createRoom(building, "RoomAtomicA_" + UUID.randomUUID(), admin.getIdentity());
		Room roomB = roomManagementService.createRoom(building, "RoomAtomicB_" + UUID.randomUUID(), admin.getIdentity());
		Room roomC = roomManagementService.createRoom(building, "RoomAtomicC_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(roomA, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(roomB, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		int logCountBefore = countLogsForRoom(roomA) + countLogsForRoom(roomB) + countLogsForRoom(roomC);
		Assert.assertEquals(2, roomManagementService.getBookings(block).size());

		RoomBookingVO voA = new RoomBookingVO();
		voA.setRoomKey(roomA.getKey());
		RoomBookingVO voB = new RoomBookingVO();
		voB.setRoomKey(roomB.getKey());
		RoomBookingVO voUnknown = new RoomBookingVO();
		voUnknown.setRoomKey(999999999L);
		RoomBookingVO voC = new RoomBookingVO();
		voC.setRoomKey(roomC.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, voA, voB, voUnknown, voC);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		List<RoomBooking> bookingsAfter = roomManagementService.getBookings(block);
		Assert.assertEquals(2, bookingsAfter.size());
		List<Long> roomKeysAfter = bookingsAfter.stream().map(b -> b.getRoom().getKey()).toList();
		Assert.assertTrue(roomKeysAfter.contains(roomA.getKey()));
		Assert.assertTrue(roomKeysAfter.contains(roomB.getKey()));

		int logCountAfter = countLogsForRoom(roomA) + countLogsForRoom(roomB) + countLogsForRoom(roomC);
		Assert.assertEquals("An aborted PUT must not write any log row", logCountBefore, logCountAfter);
	}

	// ---------- PUT .../room : duplicates ----------

	@Test
	public void putRoom_duplicateRoomKeyTwice_returns409()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldDup_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomDup_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo1 = new RoomBookingVO();
		vo1.setRoomKey(room.getKey());
		RoomBookingVO vo2 = new RoomBookingVO();
		vo2.setRoomKey(room.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo1, vo2);

		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.duplicate", body.get("code"));
		Assert.assertEquals(room.getKey().intValue(), ((Number) body.get("room")).intValue());
	}

	@Test
	public void putRoom_duplicateRoomKeyAndMatchingExternalId_returns409()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldDupExt_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomDupExt_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("dup-ext-" + UUID.randomUUID());
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO byKey = new RoomBookingVO();
		byKey.setRoomKey(room.getKey());
		RoomBookingVO byExtId = new RoomBookingVO();
		byExtId.setExternalId(room.getExternalId());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, byKey, byExtId);

		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.duplicate", body.get("code"));
	}

	// ---------- PUT .../room : errors ----------

	@Test
	public void putRoom_byAmbiguousExternalRef_returns422()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutAmbig_" + UUID.randomUUID(), admin.getIdentity());
		String sharedRef = "ambiguous-" + UUID.randomUUID();
		Room room1 = roomManagementService.createRoom(building, "RoomAmbig1_" + UUID.randomUUID(), admin.getIdentity());
		room1.setExternalRef(sharedRef);
		roomManagementService.updateRoom(room1, admin.getIdentity());
		Room room2 = roomManagementService.createRoom(building, "RoomAmbig2_" + UUID.randomUUID(), admin.getIdentity());
		room2.setExternalRef(sharedRef);
		roomManagementService.updateRoom(room2, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalRef(sharedRef);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(422, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.ambiguousExternalRef", body.get("code"));
		Assert.assertEquals(2, ((Number) body.get("matches")).intValue());
	}

	@Test
	public void putRoom_noIdentifier_returns400()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setBufferBeforeMin(5);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_unknownExternalId_returns404()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalId("nonexistent-ext-id-" + UUID.randomUUID());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_unknownRoomKey_returns404()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(999999999L);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_inactiveRoom_byKey_returns409()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldInactiveKey_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomInactiveKey_" + UUID.randomUUID(), admin.getIdentity());
		room.setStatus(RoomStatus.inactive);
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(room.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.inactive", body.get("code"));
	}

	@Test
	public void putRoom_inactiveRoom_byExternalId_returns409()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldInactiveExtId_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomInactiveExtId_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("inactive-ext-" + UUID.randomUUID());
		room.setStatus(RoomStatus.inactive);
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalId(room.getExternalId());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.inactive", body.get("code"));
	}

	@Test
	public void putRoom_inactiveRoom_byExternalRef_returns409()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldInactiveExtRef_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomInactiveExtRef_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalRef("inactive-ref-" + UUID.randomUUID());
		room.setStatus(RoomStatus.inactive);
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalRef(room.getExternalRef());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.inactive", body.get("code"));
	}

	// ---------- PUT .../room : buffers ----------

	@Test
	public void putRoom_absentBuffersOnCreate_defaultToZero()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldZeroCreate_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomZeroCreate_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo = new RoomBookingVO();
		vo.setRoomKey(room.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getBufferBeforeMin());
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getBufferAfterMin());
	}

	@Test
	public void putRoom_absentBuffersOnUpdate_keepsStoredValue()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldKeepBuf_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomKeepBuf_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 5, 10, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		// No buffers set on the VO -- must keep the stored 5/10
		RoomBookingVO vo = new RoomBookingVO();
		vo.setRoomKey(room.getKey());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(Integer.valueOf(5), result.get(0).getBufferBeforeMin());
		Assert.assertEquals(Integer.valueOf(10), result.get(0).getBufferAfterMin());
	}

	@Test
	public void putRoom_explicitZeroBuffer_setsZero()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldExplicitZero_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomExplicitZero_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 5, 10, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO vo = new RoomBookingVO();
		vo.setRoomKey(room.getKey());
		vo.setBufferBeforeMin(0);
		vo.setBufferAfterMin(0);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, vo);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(response, RoomBookingVO.class);
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getBufferBeforeMin());
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getBufferAfterMin());
	}

	// ---------- PUT .../room : round trip ----------

	@Test
	public void putRoom_roundTripOfGetBody_isNoOp_noNewLogRows()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldRoundTrip_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomRoundTrip_" + UUID.randomUUID(), admin.getIdentity());
		room.setExternalId("rt-ext-id-" + UUID.randomUUID());
		room.setExternalRef("rt-ext-ref-" + UUID.randomUUID());
		room = roomManagementService.updateRoom(room, admin.getIdentity());
		roomManagementService.bookRoom(room, block, block.getStartDate(), block.getEndDate(), 5, 10, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);

		HttpResponse getResponse = conn.execute(conn.createGet(uri, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		List<RoomBookingVO> body = conn.parseList(getResponse, RoomBookingVO.class);
		Assert.assertEquals(1, body.size());

		int logCountBefore = countLogsForRoom(room);

		HttpResponse putResponse = putBookings(conn, uri, body.toArray(new RoomBookingVO[0]));
		Assert.assertEquals(200, putResponse.getStatusLine().getStatusCode());
		List<RoomBookingVO> result = conn.parseList(putResponse, RoomBookingVO.class);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(room.getKey(), result.get(0).getRoomKey());
		Assert.assertEquals(Integer.valueOf(5), result.get(0).getBufferBeforeMin());
		Assert.assertEquals(Integer.valueOf(10), result.get(0).getBufferAfterMin());

		int logCountAfter = countLogsForRoom(room);
		Assert.assertEquals("A round-tripped GET body sent as PUT must not write a new log row",
				logCountBefore, logCountAfter);
	}

	// ---------- PUT .../room : managed flag / access ----------

	@Test
	public void putRoom_onBlockWithManagedFlagRoom_stillSucceeds()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		block.setManagedFlagsString("room");
		block = lectureService.save(block, null);
		Building building = roomManagementService.createBuilding("BldManaged_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomManaged_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(room.getKey());
		bookingVO.setBufferBeforeMin(5);
		bookingVO.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_callerWithoutBlockEditRights_returns403()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldPutNoRights_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomPutNoRights_" + UUID.randomUUID(), admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(room.getKey());
		bookingVO.setBufferBeforeMin(5);
		bookingVO.setBufferAfterMin(10);

		RestConnection conn = new RestConnection(plainUser);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = putBookings(conn, uri, bookingVO);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	// ---------- DELETE .../room ----------

	@Test
	public void deleteRoom_existingBooking_returns204_andLogs()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldDel_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomDel_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, new Date(), new Date(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(204, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		// Verify log entry
		RoomModuleLogSearchParameters logParams = new RoomModuleLogSearchParameters();
		logParams.setRoom(room);
		List<RoomModuleLog> logs = roomManagementService.searchLogs(logParams);
		boolean hasDeleteLog = logs.stream().anyMatch(l -> l.getAction() == RoomModuleLogAction.booking_delete);
		Assert.assertTrue("Expected a booking_delete log entry", hasDeleteLog);
	}

	@Test
	public void deleteRoom_multipleBookings_clearsAll()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldDelMulti_" + UUID.randomUUID(), admin.getIdentity());
		Room room1 = roomManagementService.createRoom(building, "RoomDelMulti1_" + UUID.randomUUID(), admin.getIdentity());
		Room room2 = roomManagementService.createRoom(building, "RoomDelMulti2_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room1, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		roomManagementService.bookRoom(room2, block, block.getStartDate(), block.getEndDate(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpResponse response = conn.execute(conn.createDelete(uri, MediaType.APPLICATION_JSON));

		Assert.assertEquals(204, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		Assert.assertTrue(roomManagementService.getBookings(block).isEmpty());
	}

	@Test
	public void deleteRoom_noBooking_returns204()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(204, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void deleteRoom_callerWithoutBlockEditRights_returns403()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldDelNoRights_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomDelNoRights_" + UUID.randomUUID(), admin.getIdentity());
		roomManagementService.bookRoom(room, block, new Date(), new Date(), 0, 0, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(plainUser);
		URI uri = buildRoomUri(entry, block);
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
}
