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
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.LocationRef;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.RoomImpl;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class RoomDAO {

	@Autowired
	private DB dbInstance;

	public Room create(Location location, String name) {
		RoomImpl room = new RoomImpl();
		room.setCreationDate(new Date());
		room.setLastModified(room.getCreationDate());
		room.setName(name);
		room.setStatus(RoomStatus.active);
		room.setLocation(location);
		dbInstance.getCurrentEntityManager().persist(room);
		return room;
	}

	public Room update(Room room) {
		((RoomImpl) room).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(room);
	}

	public Room loadByKey(RoomRef ref) {
		if (ref == null || ref.getKey() == null) return null;
		return dbInstance.getCurrentEntityManager().find(RoomImpl.class, ref.getKey());
	}

	public Room loadByExternalId(String externalId) {
		if (!StringHelper.containsNonWhitespace(externalId)) return null;
		List<Room> rooms = dbInstance.getCurrentEntityManager()
				.createQuery("select r from rmroom r where r.externalId=:externalId", Room.class)
				.setParameter("externalId", externalId)
				.getResultList();
		return rooms == null || rooms.isEmpty() ? null : rooms.get(0);
	}

	public Room loadByExternalRefIfUnique(String externalRef) {
		if (!StringHelper.containsNonWhitespace(externalRef)) return null;
		List<Room> rooms = dbInstance.getCurrentEntityManager()
				.createQuery("select r from rmroom r where r.externalRef=:externalRef", Room.class)
				.setParameter("externalRef", externalRef)
				.getResultList();
		return rooms != null && rooms.size() == 1 ? rooms.get(0) : null;
	}

	public List<Room> search(SearchRoomParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct r from rmroom r")
		  .append(" inner join r.location loc");
		appendSearchWhere(sb, params);
		sb.append(" order by r.name asc");

		TypedQuery<Room> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Room.class);
		applySearchParameters(query, params);
		return query.getResultList();
	}

	public long count(SearchRoomParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct r) from rmroom r")
		  .append(" inner join r.location loc");
		appendSearchWhere(sb, params);

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);
		applySearchParameters(query, params);
		Long count = query.getSingleResult();
		return count == null ? 0L : count.longValue();
	}

	private void appendSearchWhere(QueryBuilder sb, SearchRoomParameters params) {
		// Always filter out deleted rooms and their deleted locations
		sb.and().append("r.status <> :deletedStatus")
		  .and().append("loc.status <> :deletedStatus");

		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("r.status in (:statusList)");
		}
		if (params.getLocation() != null) {
			sb.and().append("r.location.key=:locationKey");
		}
		if (params.getMinSeats() != null) {
			sb.and().append("r.seats >= :minSeats");
		}
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			sb.and().append("(lower(r.name) like :searchString or lower(r.externalId) like :searchString or lower(r.externalRef) like :searchString)");
		}
		if (params.getAvailableFrom() != null && params.getAvailableTo() != null) {
			sb.and().append("not exists (select 1 from rmroombooking b where b.room=r and b.startDate < :availTo and b.endDate > :availFrom)");
		}
		if (params.getIdentity() != null) {
			// Org-scoped visibility: open-to-all (no org links) OR identity has administrator/user role in a linked org
			sb.and().append("(")
			  .append(" not exists (select 1 from rmlocationtoorganisation lto where lto.location=loc)")
			  .append(" or exists (")
			  .append("   select 1 from rmlocationtoorganisation lto2")
			  .append("   inner join lto2.organisation o")
			  .append("   inner join o.group og")
			  .append("   inner join og.members m")
			  .append("   where lto2.location=loc")
			  .append("     and m.identity.key=:identityKey")
			  .append("     and m.role in ('administrator','user')")
			  .append(" )")
			  .append(")");
		}
	}

	private void applySearchParameters(TypedQuery<?> query, SearchRoomParameters params) {
		query.setParameter("deletedStatus", RoomStatus.deleted.name());
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			List<String> statusNames = params.getStatus().stream().map(RoomStatus::name).collect(Collectors.toList());
			query.setParameter("statusList", statusNames);
		}
		if (params.getLocation() != null) {
			query.setParameter("locationKey", params.getLocation().getKey());
		}
		if (params.getMinSeats() != null) {
			query.setParameter("minSeats", params.getMinSeats());
		}
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			query.setParameter("searchString", "%" + params.getSearchString().toLowerCase() + "%");
		}
		if (params.getAvailableFrom() != null && params.getAvailableTo() != null) {
			query.setParameter("availFrom", params.getAvailableFrom());
			query.setParameter("availTo", params.getAvailableTo());
		}
		if (params.getIdentity() != null) {
			query.setParameter("identityKey", params.getIdentity().getKey());
		}
	}

	public int delete(RoomRef ref) {
		if (ref == null || ref.getKey() == null) return 0;
		return dbInstance.getCurrentEntityManager()
				.createQuery("update rmroom r set r.status=:deleted where r.key=:key")
				.setParameter("deleted", RoomStatus.deleted.name())
				.setParameter("key", ref.getKey())
				.executeUpdate();
	}

	public List<Room> getRoomsForLocation(LocationRef location) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select r from rmroom r where r.location.key=:locationKey and r.status <> :deleted order by r.name asc", Room.class)
				.setParameter("locationKey", location.getKey())
				.setParameter("deleted", RoomStatus.deleted.name())
				.getResultList();
	}
}
