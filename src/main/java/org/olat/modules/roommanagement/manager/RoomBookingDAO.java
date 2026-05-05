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

import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomBookingRef;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.model.RoomBookingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class RoomBookingDAO {

	@Autowired
	private DB dbInstance;

	public RoomBooking create(Room room, LectureBlock lectureBlock, Date startDate, Date endDate) {
		RoomBookingImpl booking = new RoomBookingImpl();
		booking.setCreationDate(new Date());
		booking.setLastModified(booking.getCreationDate());
		booking.setRoom(room);
		booking.setLectureBlock(lectureBlock);
		booking.setStartDate(startDate);
		booking.setEndDate(endDate);
		dbInstance.getCurrentEntityManager().persist(booking);
		return booking;
	}

	public RoomBooking update(RoomBooking booking) {
		booking.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(booking);
	}

	public List<RoomBooking> getBookingsForRoom(RoomRef room, Date from, Date to) {
		String query = "select b from rmroombooking b where b.room.key=:roomKey";
		if (from != null) {
			query += " and b.endDate >= :from";
		}
		if (to != null) {
			query += " and b.startDate <= :to";
		}
		query += " order by b.startDate asc";

		var typedQuery = dbInstance.getCurrentEntityManager().createQuery(query, RoomBooking.class)
				.setParameter("roomKey", room.getKey());
		if (from != null) {
			typedQuery.setParameter("from", from);
		}
		if (to != null) {
			typedQuery.setParameter("to", to);
		}
		return typedQuery.getResultList();
	}

	public List<RoomBooking> getBookingsForLectureBlock(LectureBlockRef lectureBlock) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select b from rmroombooking b where b.lectureBlock.key=:lbKey order by b.startDate asc", RoomBooking.class)
				.setParameter("lbKey", lectureBlock.getKey())
				.getResultList();
	}

	/**
	 * Finds all bookings for a given room that have a hard overlap with the specified start and end dates. 
	 * A hard overlap is defined as the new booking's time interval intersecting with the time intervals 
	 * of existing bookings such that neither booking can coexist in the same room during the same timeframe.
	 *
	 * @param room       The room reference for which the overlapping bookings are to be found.
	 * @param start      The start date and time of the interval being checked.
	 * @param end        The end date and time of the interval being checked.
	 * @param excludeKey An optional unique identifier for a booking to exclude from the overlap check.
	 * @return A list of {@code RoomBooking} objects that have a hard overlap with the specified interval.
	 */
	public List<RoomBooking> findHardOverlapping(RoomRef room, Date start, Date end, Long excludeKey) {
		String query = "select b from rmroombooking b" +
				" where b.room.key=:roomKey" +
				" and (:excludeKey is null or b.key <> :excludeKey)" +
				" and b.startDate < :endDate" +
				" and b.endDate > :startDate";
		return dbInstance.getCurrentEntityManager().createQuery(query, RoomBooking.class)
				.setParameter("roomKey", room.getKey())
				.setParameter("excludeKey", excludeKey)
				.setParameter("startDate", start)
				.setParameter("endDate", end)
				.getResultList();
	}

	/**
	 * Finds all bookings for a given room that overlap with a given buffer-enveloped time interval.
	 * The method evaluates whether any existing booking's actual interval intersects the specified time range.
	 * This ensures that bookings overlapping with the buffered envelope of a candidate interval are identified.
	 *
	 * @param room       The room reference for which the overlapping bookings are to be found.
	 * @param start      The start date of the candidate time interval.
	 * @param end        The end date of the candidate time interval.
	 * @param sPrime     The adjusted start date of the candidate interval considering the buffer.
	 * @param ePrime     The adjusted end date of the candidate interval considering the buffer.
	 * @param excludeKey The key of a booking to exclude from the search, or null if no exclusion is required.
	 * @return A list of {@code RoomBooking} objects that overlap with the specified buffered time interval.
	 */
	public List<RoomBooking> findBufferOverlapping(RoomRef room, Date start, Date end,
			Date sPrime, Date ePrime, Long excludeKey) {
		// Buffer-envelope of existing booking: [b.startDate - b.bufferBefore, b.endDate + b.bufferAfter]
		String query = "select b from rmroombooking b" +
				" where b.room.key=:roomKey" +
				" and (:excludeKey is null or b.key <> :excludeKey)" +
				" and b.startDate < :ePrime" +
				" and b.endDate > :sPrime" +
				" and not (b.startDate < :endDate and b.endDate > :startDate)";
		return dbInstance.getCurrentEntityManager().createQuery(query, RoomBooking.class)
				.setParameter("roomKey", room.getKey())
				.setParameter("excludeKey", excludeKey)
				.setParameter("sPrime", sPrime)
				.setParameter("ePrime", ePrime)
				.setParameter("startDate", start)
				.setParameter("endDate", end)
				.getResultList();
	}

	public int delete(RoomBookingRef ref) {
		if (ref == null || ref.getKey() == null) return 0;
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from rmroombooking b where b.key=:key")
				.setParameter("key", ref.getKey())
				.executeUpdate();
	}

	public int deleteForLectureBlock(LectureBlockRef lectureBlock) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from rmroombooking b where b.lectureBlock.key=:lbKey")
				.setParameter("lbKey", lectureBlock.getKey())
				.executeUpdate();
	}

	public int copyBookings(LectureBlock source, LectureBlock target) {
		List<RoomBooking> sourceBookings = getBookingsForLectureBlock(source);
		if (sourceBookings.isEmpty()) return 0;
		int count = 0;
		for (RoomBooking src : sourceBookings) {
			RoomBookingImpl copy = new RoomBookingImpl();
			copy.setCreationDate(new Date());
			copy.setLastModified(copy.getCreationDate());
			copy.setRoom(src.getRoom());
			copy.setLectureBlock(target);
			copy.setStartDate(src.getStartDate());
			copy.setEndDate(src.getEndDate());
			copy.setBufferBefore(src.getBufferBefore());
			copy.setBufferAfter(src.getBufferAfter());
			dbInstance.getCurrentEntityManager().persist(copy);
			count++;
		}
		return count;
	}
}
