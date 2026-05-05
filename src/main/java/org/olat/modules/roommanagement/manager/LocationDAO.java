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
import org.olat.modules.roommanagement.Location;
import org.olat.modules.roommanagement.LocationRef;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.LocationImpl;
import org.olat.modules.roommanagement.model.LocationToOrganisationImpl;
import org.olat.modules.roommanagement.model.SearchLocationParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 4 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class LocationDAO {

	@Autowired
	private DB dbInstance;

	public Location create(String name) {
		LocationImpl location = new LocationImpl();
		location.setCreationDate(new Date());
		location.setLastModified(location.getCreationDate());
		location.setName(name);
		location.setStatus(RoomStatus.active);
		dbInstance.getCurrentEntityManager().persist(location);
		return location;
	}

	public Location update(Location location) {
		location.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(location);
	}

	public Location loadByKey(LocationRef ref) {
		if (ref == null || ref.getKey() == null) return null;
		return dbInstance.getCurrentEntityManager().find(LocationImpl.class, ref.getKey());
	}

	public Location loadByExternalId(String externalId) {
		if (!StringHelper.containsNonWhitespace(externalId)) return null;
		List<Location> locations = dbInstance.getCurrentEntityManager()
				.createQuery("select l from rmlocation l where l.externalId=:externalId", Location.class)
				.setParameter("externalId", externalId)
				.getResultList();
		return locations == null || locations.isEmpty() ? null : locations.get(0);
	}

	public List<Location> search(SearchLocationParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select l from rmlocation l");
		appendSearchWhere(sb, params);
		sb.append(" order by l.name asc");

		TypedQuery<Location> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Location.class);
		applySearchParameters(query, params);
		return query.getResultList();
	}

	public long count(SearchLocationParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(l) from rmlocation l");
		appendSearchWhere(sb, params);

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class);
		applySearchParameters(query, params);
		Long count = query.getSingleResult();
		return count == null ? 0L : count;
	}

	private void appendSearchWhere(QueryBuilder sb, SearchLocationParameters params) {
		if (StringHelper.containsNonWhitespace(params.getSearchString())) {
			sb.and().append("(lower(l.name) like :searchString or lower(l.externalId) like :searchString or lower(l.externalRef) like :searchString)");
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("l.status in (:statusList)");
		}
		if (params.getOrganisations() != null && !params.getOrganisations().isEmpty()) {
			sb.and().append("exists (select 1 from rmlocationtoorganisation lto where lto.location=l and lto.organisation.key in (:orgKeys))");
		}
	}

	private void applySearchParameters(TypedQuery<?> query, SearchLocationParameters params) {
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
	}

	public int delete(LocationRef ref) {
		if (ref == null || ref.getKey() == null) return 0;
		return dbInstance.getCurrentEntityManager()
				.createQuery("update rmlocation l set l.status=:deleted where l.key=:key")
				.setParameter("deleted", RoomStatus.deleted.name())
				.setParameter("key", ref.getKey())
				.executeUpdate();
	}

	public void addOrganisation(Location location, Organisation organisation) {
		LocationToOrganisationImpl lto = new LocationToOrganisationImpl();
		lto.setCreationDate(new Date());
		lto.setLocation(location);
		lto.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(lto);
	}

	public void removeOrganisation(Location location, Organisation organisation) {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from rmlocationtoorganisation lto where lto.location.key=:locationKey and lto.organisation.key=:orgKey")
				.setParameter("locationKey", location.getKey())
				.setParameter("orgKey", organisation.getKey())
				.executeUpdate();
	}

	public List<Organisation> getOrganisations(LocationRef ref) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select lto.organisation from rmlocationtoorganisation lto where lto.location.key=:locationKey", Organisation.class)
				.setParameter("locationKey", ref.getKey())
				.getResultList();
	}
}
