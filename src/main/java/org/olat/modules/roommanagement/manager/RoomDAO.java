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
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.BuildingRef;
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

	public Room create(Building building, String description) {
		RoomImpl room = new RoomImpl();
		room.setCreationDate(new Date());
		room.setLastModified(room.getCreationDate());
		room.setDescription(description);
		room.setStatus(RoomStatus.active);
		room.setBuilding(building);
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
		  .append(" inner join r.building bld");
		appendSearchWhere(sb, params);
		sb.append(" order by r.description asc");

		TypedQuery<Room> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Room.class);
		applySearchParameters(query, params);
		if (params.getFirstResult() > 0) {
			query.setFirstResult(params.getFirstResult());
		}
		if (params.getMaxResults() > 0) {
			query.setMaxResults(params.getMaxResults());
		}
		return query.getResultList();
	}

	public long count(SearchRoomParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct r) from rmroom r")
		  .append(" inner join r.building bld");
		appendSearchWhere(sb, params);

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);
		applySearchParameters(query, params);
		Long count = query.getSingleResult();
		return count == null ? 0L : count.longValue();
	}

	private void appendSearchWhere(QueryBuilder sb, SearchRoomParameters params) {
		// Always filter out deleted rooms and their deleted buildings
		sb.and().append("r.status <> :deletedStatus")
		  .and().append("bld.status <> :deletedStatus");

		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("r.status in (:statusList)");
		}
		if (params.getBuilding() != null) {
			sb.and().append("r.building.key=:buildingKey");
		}
		if (StringHelper.containsNonWhitespace(params.getExactExternalId())) {
			sb.and().append("r.externalId=:exactExternalId");
		}
		if (StringHelper.containsNonWhitespace(params.getExactExternalRef())) {
			sb.and().append("r.externalRef=:exactExternalRef");
		}
		if (params.getMinSeats() != null) {
			sb.and().append("r.seats >= :minSeats");
		}
		if (params.getMaxSeats() != null) {
			sb.and().append("r.seats <= :maxSeats");
		}
		if (params.getOrganisationKey() != null) {
			sb.and().append("exists (select 1 from rmbuildingtoorganisation bto3 where bto3.building=bld and bto3.organisation.key=:organisationKey)");
		}
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			sb.and().append("(lower(r.description) like :searchString or lower(r.externalId) like :searchString or lower(r.externalRef) like :searchString)");
		}
		if (params.getAvailableFrom() != null && params.getAvailableTo() != null) {
			sb.and().append("not exists (select 1 from rmroombooking b where b.room=r and b.startDate < :availTo and b.endDate > :availFrom)");
		}
		if (params.getIdentity() != null) {
			// Org-scoped visibility: open-to-all (no org links) OR identity is a member of a linked org (any role)
			sb.and().append("(")
			  .append(" not exists (select 1 from rmbuildingtoorganisation bto where bto.building=bld)")
			  .append(" or exists (")
			  .append("   select 1 from rmbuildingtoorganisation bto2")
			  .append("   inner join bto2.organisation o")
			  .append("   inner join o.group og")
			  .append("   inner join og.members m")
			  .append("   where bto2.building=bld")
			  .append("     and m.identity.key=:identityKey")
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
		if (params.getBuilding() != null) {
			query.setParameter("buildingKey", params.getBuilding().getKey());
		}
		if (StringHelper.containsNonWhitespace(params.getExactExternalId())) {
			query.setParameter("exactExternalId", params.getExactExternalId());
		}
		if (StringHelper.containsNonWhitespace(params.getExactExternalRef())) {
			query.setParameter("exactExternalRef", params.getExactExternalRef());
		}
		if (params.getMinSeats() != null) {
			query.setParameter("minSeats", params.getMinSeats());
		}
		if (params.getMaxSeats() != null) {
			query.setParameter("maxSeats", params.getMaxSeats());
		}
		if (params.getOrganisationKey() != null) {
			query.setParameter("organisationKey", params.getOrganisationKey());
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

	public List<Room> getRoomsForBuilding(BuildingRef building) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select r from rmroom r where r.building.key=:buildingKey and r.status <> :deleted order by r.description asc", Room.class)
				.setParameter("buildingKey", building.getKey())
				.setParameter("deleted", RoomStatus.deleted.name())
				.getResultList();
	}
}
