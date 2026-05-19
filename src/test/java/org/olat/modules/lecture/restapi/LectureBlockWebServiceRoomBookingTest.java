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
import java.io.UnsupportedEncodingException;
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
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
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

	@Test
	public void getRoom_noBooking_returns204()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(204, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void getRoom_withBooking_returnsVO_withBuffers()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		Building building = roomManagementService.createBuilding("BldGetRoom_" + UUID.randomUUID(), admin.getIdentity());
		Room room = roomManagementService.createRoom(building, "RoomGetRoom_" + UUID.randomUUID(), admin.getIdentity());
		Date startDate = new Date();
		Date endDate = new Date();
		roomManagementService.bookRoom(room, block, startDate, endDate, 10, 15, admin.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO vo = conn.parse(response, RoomBookingVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(10, vo.getBufferBeforeMin());
		Assert.assertEquals(15, vo.getBufferAfterMin());
		Assert.assertEquals(room.getKey(), vo.getRoomKey());
	}

	@Test
	public void putRoom_byInternalKey_createsBooking()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO result = conn.parse(response, RoomBookingVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(room.getKey(), result.getRoomKey());
	}

	@Test
	public void putRoom_byExternalId_createsBooking()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO result = conn.parse(response, RoomBookingVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(room.getKey(), result.getRoomKey());
	}

	@Test
	public void putRoom_byUniqueExternalRef_createsBooking()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RoomBookingVO result = conn.parse(response, RoomBookingVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(room.getKey(), result.getRoomKey());
	}

	@Test
	public void putRoom_byAmbiguousExternalRef_returns422()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(422, response.getStatusLine().getStatusCode());
		Map<?, ?> body = conn.parse(response, Map.class);
		Assert.assertEquals("room.ambiguousExternalRef", body.get("code"));
		Assert.assertEquals(2, ((Number) body.get("matches")).intValue());
	}

	@Test
	public void putRoom_noIdentifier_returns400()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setBufferBeforeMin(5);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_unknownExternalId_returns404()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setExternalId("nonexistent-ext-id-" + UUID.randomUUID());

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_unknownRoomKey_returns404()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin.getIdentity());
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		RoomBookingVO bookingVO = new RoomBookingVO();
		bookingVO.setRoomKey(999999999L);

		RestConnection conn = new RestConnection(admin);
		URI uri = buildRoomUri(entry, block);
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_replacesExistingBooking()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method1 = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method1, bookingVO1);
		HttpResponse response1 = conn.execute(method1);

		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		RoomBookingVO result1 = conn.parse(response1, RoomBookingVO.class);
		Assert.assertNotNull(result1);
		Long bookingKey = result1.getKey();

		// Second PUT with different buffer
		RoomBookingVO bookingVO2 = new RoomBookingVO();
		bookingVO2.setRoomKey(room.getKey());
		bookingVO2.setBufferBeforeMin(99);
		bookingVO2.setBufferAfterMin(10);

		HttpPut method2 = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method2, bookingVO2);
		HttpResponse response2 = conn.execute(method2);

		Assert.assertEquals(200, response2.getStatusLine().getStatusCode());
		EntityUtils.consume(response2.getEntity());

		// GET to verify updated buffer
		HttpGet getMethod = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		RoomBookingVO getResult = conn.parse(getResponse, RoomBookingVO.class);
		Assert.assertNotNull(getResult);
		Assert.assertEquals(99, getResult.getBufferBeforeMin());
		Assert.assertEquals(bookingKey, getResult.getKey());
	}

	@Test
	public void putRoom_onBlockWithManagedFlagRoom_stillSucceeds()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

	@Test
	public void putRoom_callerWithoutBlockEditRights_returns403()
	throws IOException, URISyntaxException, UnsupportedEncodingException {
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
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, bookingVO);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}

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
