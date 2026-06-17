/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitImpl;
import org.olat.modules.selectus.model.PositionRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.TypedQuery;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationUnitDAO {
	
	@Autowired
	private DB dbInstance;
	
	public OrganisationUnit createOrganisationUnit(Organisation organisation) {
		OrganisationUnitImpl unit = new OrganisationUnitImpl();
		unit.setCreationDate(new Date());
		unit.setLastModified(unit.getCreationDate());
		unit.setOrganisation(organisation);
		return unit;
	}
	
	public OrganisationUnit save(OrganisationUnit unit) {
		if(unit.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(unit);
		} else {
			unit = dbInstance.getCurrentEntityManager().merge(unit);
		}
		return unit;
	}
	
	public OrganisationUnit loadOrganisationUnitByOrganisation(OrganisationRef organisation) {
		String query = """
				select orgunit from rorganisationunit orgunit
				inner join fetch orgunit.organisation as org
				where org.key=:organisationKey
				order by orgunit.creationDate asc""";
		List<OrganisationUnit> units = dbInstance.getCurrentEntityManager()
			.createQuery(query, OrganisationUnit.class)
			.setParameter("organisationKey", organisation.getKey())
			.getResultList();
		return units == null || units.isEmpty() ? null : units.get(0);
	}
	
	public OrganisationUnit loadOrganisationUnitByPosition(PositionRef position) {
		String query = """
				select orgunit from rposition as pos
				inner join pos.organisation as org
				inner join rorganisationunit orgunit on (orgunit.organisation.key=org.key)
				where pos.key=:positionKey
				order by orgunit.creationDate asc""";
		List<OrganisationUnit> units = dbInstance.getCurrentEntityManager()
			.createQuery(query, OrganisationUnit.class)
			.setParameter("positionKey", position.getKey())
			.getResultList();
		return units == null || units.isEmpty() ? null : units.get(0);
	}
	
	public OrganisationUnit loadOrganisationUnitByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select orgunit from rorganisationunit orgunit where orgunit.key=:unitKey");
		
		List<OrganisationUnit> units = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), OrganisationUnit.class)
			.setParameter("unitKey", key)
			.getResultList();
		return units == null || units.isEmpty() ? null : units.get(0);
	}
	
	public List<OrganisationUnit> findAllOrganisationUnits() {
		StringBuilder sb = new StringBuilder();
		sb.append("select orgunit from rorganisationunit orgunit");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), OrganisationUnit.class)
			.getResultList();
	}
	
	public boolean isOrganisationUnitNamesInUse(String name,  OrganisationUnit current) {
		if(!StringHelper.containsNonWhitespace(name)) return false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select orgUnit.key from rorganisationunit orgUnit")
		  .append(" where (lower(orgUnit.name)=:name or lower(orgUnit.nameDe)=:name)");
		if(current != null) {
			sb.append(" and orgUnit.key!=:currentKey");
		}

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class);
		if(StringHelper.containsNonWhitespace(name)) {
			query.setParameter("name", name.toLowerCase());
		}
		if(current != null) {
			query.setParameter("currentKey", current.getKey());
		}
		List<Long> keys = query.setFirstResult(0).setMaxResults(1).getResultList();
		return keys == null || keys.isEmpty() || keys.get(0)== null ? false : keys.get(0) > 0;
	}
	
	public int deleteOrganisationUnit(OrganisationUnit unit) {
		if(unit == null) return 0;//nothing to do
		
		String q = "delete rorganisationunit orgUnit where orgUnit.key=:unitKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("unitKey", unit.getKey())
			.executeUpdate();
	}
}
