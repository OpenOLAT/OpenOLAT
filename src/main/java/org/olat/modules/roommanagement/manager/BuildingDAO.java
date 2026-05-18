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
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Building;
import org.olat.modules.roommanagement.BuildingRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.BuildingImpl;
import org.olat.modules.roommanagement.model.BuildingToOrganisationImpl;
import org.olat.modules.roommanagement.model.SearchBuildingParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BuildingDAO {

	@Autowired
	private DB dbInstance;

	public Building create(String description) {
		BuildingImpl building = new BuildingImpl();
		building.setCreationDate(new Date());
		building.setLastModified(building.getCreationDate());
		building.setDescription(description);
		building.setStatus(RoomStatus.active);
		dbInstance.getCurrentEntityManager().persist(building);
		return building;
	}

	public Building update(Building building) {
		building.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(building);
	}

	public Building loadByKey(BuildingRef ref) {
		if (ref == null || ref.getKey() == null) return null;
		return dbInstance.getCurrentEntityManager().find(BuildingImpl.class, ref.getKey());
	}

	public Building loadByExternalId(String externalId) {
		if (!StringHelper.containsNonWhitespace(externalId)) return null;
		List<Building> buildings = dbInstance.getCurrentEntityManager()
				.createQuery("select b from rmbuilding b where b.externalId=:externalId", Building.class)
				.setParameter("externalId", externalId)
				.getResultList();
		return buildings == null || buildings.isEmpty() ? null : buildings.get(0);
	}

	public List<Building> search(SearchBuildingParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select b from rmbuilding b");
		appendSearchWhere(sb, params);
		sb.append(" order by b.description asc");

		TypedQuery<Building> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Building.class);
		applySearchParameters(query, params);
		return query.getResultList();
	}

	public long count(SearchBuildingParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(b) from rmbuilding b");
		appendSearchWhere(sb, params);

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);
		applySearchParameters(query, params);
		Long count = query.getSingleResult();
		return count == null ? 0L : count;
	}

	private void appendSearchWhere(QueryBuilder sb, SearchBuildingParameters params) {
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			sb.and().append("(lower(b.description) like :searchString or lower(b.externalId) like :searchString or lower(b.externalRef) like :searchString)");
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("b.status in (:statusList)");
		}
		if (params.getOrganisations() != null && !params.getOrganisations().isEmpty()) {
			sb.and().append("exists (select 1 from rmbuildingtoorganisation bto where bto.building=b and bto.organisation.key in (:orgKeys))");
		}
		if (params.getIdentity() != null) {
			// Org-scoped visibility: open-to-all (no org links) OR identity has administrator/user role in a linked org
			sb.and().append("(")
			  .append(" not exists (select 1 from rmbuildingtoorganisation bto2 where bto2.building=b)")
			  .append(" or exists (")
			  .append("   select 1 from rmbuildingtoorganisation bto3")
			  .append("   inner join bto3.organisation o")
			  .append("   inner join o.group og")
			  .append("   inner join og.members m")
			  .append("   where bto3.building=b")
			  .append("     and m.identity.key=:identityKey")
			  .append("     and m.role in ('administrator','user')")
			  .append(" )")
			  .append(")");
		}
	}

	private void applySearchParameters(TypedQuery<?> query, SearchBuildingParameters params) {
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			query.setParameter("searchString", "%" + params.getSearchString().toLowerCase() + "%");
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			List<String> statusNames = params.getStatus().stream().map(RoomStatus::name).collect(Collectors.toList());
			query.setParameter("statusList", statusNames);
		}
		if (params.getOrganisations() != null && !params.getOrganisations().isEmpty()) {
			List<Long> orgKeys = params.getOrganisations().stream()
					.map(OrganisationRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("orgKeys", orgKeys);
		}
		if (params.getIdentity() != null) {
			query.setParameter("identityKey", params.getIdentity().getKey());
		}
	}

	public int delete(BuildingRef ref) {
		if (ref == null || ref.getKey() == null) return 0;
		return dbInstance.getCurrentEntityManager()
				.createQuery("update rmbuilding b set b.status=:deleted where b.key=:key")
				.setParameter("deleted", RoomStatus.deleted.name())
				.setParameter("key", ref.getKey())
				.executeUpdate();
	}

	public void addOrganisation(Building building, Organisation organisation) {
		BuildingToOrganisationImpl bto = new BuildingToOrganisationImpl();
		bto.setCreationDate(new Date());
		bto.setBuilding(building);
		bto.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(bto);
	}

	public void removeOrganisation(Building building, Organisation organisation) {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from rmbuildingtoorganisation bto where bto.building.key=:buildingKey and bto.organisation.key=:orgKey")
				.setParameter("buildingKey", building.getKey())
				.setParameter("orgKey", organisation.getKey())
				.executeUpdate();
	}

	public List<Organisation> getOrganisations(BuildingRef ref) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select bto.organisation from rmbuildingtoorganisation bto where bto.building.key=:buildingKey", Organisation.class)
				.setParameter("buildingKey", ref.getKey())
				.getResultList();
	}
}
