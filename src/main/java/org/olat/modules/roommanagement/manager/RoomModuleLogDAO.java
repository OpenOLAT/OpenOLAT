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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.BuildingRef;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomBookingRef;
import org.olat.modules.roommanagement.RoomModuleLog;
import org.olat.modules.roommanagement.RoomModuleLogAction;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.model.RoomModuleLogImpl;
import org.olat.modules.roommanagement.model.RoomModuleLogSearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class RoomModuleLogDAO {

	@Autowired
	private DB dbInstance;

	public RoomModuleLog createLog(RoomModuleLogAction action,
			String beforeStatus, String beforeValue,
			String afterStatus, String afterValue,
			Building ctxBuilding, Room ctxRoom,
			RoomBooking ctxBooking, LectureBlock ctxLectureBlock,
			Identity doer) {
		RoomModuleLogImpl log = new RoomModuleLogImpl();
		log.setCreationDate(new Date());
		log.setAction(action);
		log.setBeforeStatus(beforeStatus);
		log.setBefore(beforeValue);
		log.setAfterStatus(afterStatus);
		log.setAfter(afterValue);
		log.setBuilding(ctxBuilding);
		log.setRoom(ctxRoom);
		log.setBooking(ctxBooking);
		log.setLectureBlock(ctxLectureBlock);
		log.setDoer(doer);
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}

	public List<RoomModuleLog> loadLogs(RoomModuleLogSearchParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log from rmmodulelog log");

		if (params.getBuilding() != null) {
			sb.and().append("log.building.key=:buildingKey");
		}
		if (params.getRoom() != null) {
			sb.and().append("log.room.key=:roomKey");
		}
		if (params.getFrom() != null) {
			sb.and().append("log.creationDate >= :from");
		}
		if (params.getTo() != null) {
			sb.and().append("log.creationDate <= :to");
		}
		sb.append(" order by log.creationDate desc");

		TypedQuery<RoomModuleLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RoomModuleLog.class);
		if (params.getBuilding() != null) {
			query.setParameter("buildingKey", params.getBuilding().getKey());
		}
		if (params.getRoom() != null) {
			query.setParameter("roomKey", params.getRoom().getKey());
		}
		if (params.getFrom() != null) {
			query.setParameter("from", params.getFrom());
		}
		if (params.getTo() != null) {
			query.setParameter("to", params.getTo());
		}
		return query.getResultList();
	}

	public void nullBuildingRef(BuildingRef building) {
		dbInstance.getCurrentEntityManager()
				.createQuery("update rmmodulelog l set l.building = null where l.building.key = :key")
				.setParameter("key", building.getKey())
				.executeUpdate();
	}

	public void nullRoomRef(RoomRef room) {
		dbInstance.getCurrentEntityManager()
				.createQuery("update rmmodulelog l set l.room = null where l.room.key = :key")
				.setParameter("key", room.getKey())
				.executeUpdate();
	}

	public void nullBookingRef(RoomBookingRef booking) {
		dbInstance.getCurrentEntityManager()
				.createQuery("update rmmodulelog l set l.booking = null where l.booking.key = :key")
				.setParameter("key", booking.getKey())
				.executeUpdate();
	}

	public void nullLectureBlockRef(LectureBlockRef lb) {
		dbInstance.getCurrentEntityManager()
				.createQuery("update rmmodulelog l set l.lectureBlock = null where l.lectureBlock.key = :key")
				.setParameter("key", lb.getKey())
				.executeUpdate();
	}

	public List<Identity> loadDoers(RoomRef room) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select distinct doer from rmmodulelog log inner join log.doer doer inner join fetch doer.user doerUser where log.room.key=:roomKey", Identity.class)
				.setParameter("roomKey", room.getKey())
				.getResultList();
	}
}
