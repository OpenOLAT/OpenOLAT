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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomModuleLog;
import org.olat.modules.roommanagement.RoomModuleLogAction;
import org.olat.modules.roommanagement.model.RoomModuleLogSearchParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomModuleLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private LocationDAO locationDAO;
	@Autowired
	private RoomDAO roomDAO;
	@Autowired
	private RoomModuleLogDAO roomModuleLogDAO;

	private Location createLocation() {
		return locationDAO.create("LogLoc_" + UUID.randomUUID());
	}

	private Room createRoom(Location loc) {
		return roomDAO.create(loc, "LogRoom_" + UUID.randomUUID());
	}

	@Test
	public void createLog() {
		Location loc = createLocation();
		Room room = createRoom(loc);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-log-doer");
		dbInstance.commitAndCloseSession();

		RoomModuleLog log = roomModuleLogDAO.createLog(
				RoomModuleLogAction.room_create,
				null, null,
				"active", "<room/>",
				loc, room, null, null,
				doer);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(log.getKey());
		Assert.assertEquals(RoomModuleLogAction.room_create, log.getAction());
	}

	@Test
	public void loadLogs_byRoom() {
		Location loc = createLocation();
		Room room = createRoom(loc);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-log-room");
		dbInstance.commitAndCloseSession();

		roomModuleLogDAO.createLog(RoomModuleLogAction.room_update,
				"active", "<before/>", "active", "<after/>",
				loc, room, null, null, doer);
		dbInstance.commitAndCloseSession();

		RoomModuleLogSearchParameters params = new RoomModuleLogSearchParameters();
		params.setRoom(room);
		List<RoomModuleLog> logs = roomModuleLogDAO.loadLogs(params);
		Assertions.assertThat(logs)
				.extracting(l -> l.getRoom().getKey())
				.contains(room.getKey());
	}

	@Test
	public void loadLogs_byDateRange() {
		Location loc = createLocation();
		Room room = createRoom(loc);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-log-date");
		dbInstance.commitAndCloseSession();

		roomModuleLogDAO.createLog(RoomModuleLogAction.location_create,
				null, null, "active", "<loc/>",
				loc, null, null, null, doer);
		dbInstance.commitAndCloseSession();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -5);
		Date from = cal.getTime();
		cal.add(Calendar.MINUTE, 10);
		Date to = cal.getTime();

		RoomModuleLogSearchParameters params = new RoomModuleLogSearchParameters();
		params.setLocation(loc);
		params.setFrom(from);
		params.setTo(to);
		List<RoomModuleLog> logs = roomModuleLogDAO.loadLogs(params);
		Assert.assertFalse("Should find at least one log in date range", logs.isEmpty());
	}

	@Test
	public void loadDoers() {
		Location loc = createLocation();
		Room room = createRoom(loc);
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("rm-log-doer2");
		dbInstance.commitAndCloseSession();

		roomModuleLogDAO.createLog(RoomModuleLogAction.room_create,
				null, null, "active", "<room/>",
				loc, room, null, null, doer);
		dbInstance.commitAndCloseSession();

		List<Identity> doers = roomModuleLogDAO.loadDoers(room);
		Assertions.assertThat(doers)
				.extracting(Identity::getKey)
				.contains(doer.getKey());
	}
}
