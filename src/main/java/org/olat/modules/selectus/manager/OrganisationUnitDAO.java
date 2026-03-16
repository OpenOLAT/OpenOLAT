/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitImpl;

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
	
	public OrganisationUnit createOrganisationUnit() {
		OrganisationUnitImpl unit = new OrganisationUnitImpl();
		unit.setCreationDate(new Date());
		unit.setLastModified(unit.getCreationDate());
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
	
	public List<OrganisationUnit> findOrganisationUnits(IdentityRef identity, Roles roles) {
		boolean limitByMembership = (!roles.isAdministrator() && !roles.isSelectusManager());
		
		StringBuilder sb = new StringBuilder();
		sb.append("select orgUnit from rorganisationunit orgUnit");
		if(limitByMembership) {
			sb.append(" where orgUnit.key in (select orgMember.organisationUnit.key from rorganisationunitmember as orgMember where")
			  .append("  orgMember.identity.key=:identityKey")
			  .append(" )");
		}
		
		TypedQuery<OrganisationUnit> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), OrganisationUnit.class);
		if(limitByMembership) {
			query.setParameter("identityKey", identity.getKey());
		}
		return query.getResultList();
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
