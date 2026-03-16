/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.OrganisationUnitMembership;
import org.olat.modules.selectus.model.OrganisationUnitMembershipImpl;

/**
 * 
 * Initial date: 16 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationUnitMembershipDAO {
	
	@Autowired
	private DB dbInstance;
	
	public OrganisationUnitMembership createAndPersistMembership(Identity identity, OrganisationUnit organisationUnit) {
		OrganisationUnitMembershipImpl membership = new OrganisationUnitMembershipImpl();
		membership.setCreationDate(new Date());
		membership.setLastModified(membership.getCreationDate());
		membership.setIdentity(identity);
		membership.setOrganisationUnit(organisationUnit);
		dbInstance.getCurrentEntityManager().persist(membership);
		return membership;
	}
	
	public OrganisationUnitMembership loadByKey(Long key) {
		String q = "select membership from rorganisationunitmember membership where membership.key=:membershipKey";
		
		List<OrganisationUnitMembership> memberships = dbInstance.getCurrentEntityManager()
			.createQuery(q, OrganisationUnitMembership.class)
			.setParameter("membershipKey", key)
			.getResultList();
		return memberships == null || memberships.isEmpty() ? null : memberships.get(0);
	}
	
	public List<OrganisationUnitMembership> findMemberships(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select membership from rorganisationunitmember membership")
		  .append(" inner join fetch membership.organisationUnit orgUnit")
		  .append(" where membership.identity.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), OrganisationUnitMembership.class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	public boolean isMemberOfOrganisationUnit(IdentityRef identity, OrganisationUnit organisationUnit) {
		StringBuilder sb = new StringBuilder();
		sb.append("select membership.key from rorganisationunitmember membership")
		  .append(" inner join membership.organisationUnit orgUnit")
		  .append(" where membership.identity.key=:identityKey and membership.organisationUnit.key=:organisationUnitKey");
		
		List<Long> memberships = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("organisationUnitKey", organisationUnit.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return memberships == null || memberships.isEmpty() || memberships.get(0) == null ? false : memberships.get(0).longValue() > 0l;
	}
	
	public void removeMembership(OrganisationUnitMembership membership) {
		if(membership.getKey() == null) return;// not saved, nothing to do
		
		OrganisationUnitMembershipImpl reloadMembership = dbInstance.getCurrentEntityManager()
				.getReference(OrganisationUnitMembershipImpl.class, membership.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadMembership);
	}
	
	public int updateMembership(List<OrganisationUnit> oldUnits, OrganisationUnit newUnit) {
		if(oldUnits == null || oldUnits.isEmpty() || newUnit == null) return 0;//nothing to do
		
		List<Long> oldUnitKeys = PersistenceHelper.toKeys(oldUnits);
		StringBuilder sb = new StringBuilder();
		sb.append("update rorganisationunitmember membership set membership.organisationUnit.key=:newUnitKey")
		  .append(" where membership.organisationUnit.key in (:oldUnitKeys)")
		  .append(" and not exists (select otherMembership.key from rorganisationunitmember otherMembership")
		  .append("  where otherMembership.organisationUnit.key=:newUnitKey and otherMembership.identity.key=membership.identity.key")
		  .append(")");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString())
			.setParameter("newUnitKey", newUnit.getKey())
			.setParameter("oldUnitKeys", oldUnitKeys)
			.executeUpdate();
	}
	
	public int deleteMembership(OrganisationUnit unit) {
		if(unit == null) return 0;//nothing to do
		
		String q = "delete rorganisationunitmember membership where membership.organisationUnit.key=:unitKey";
		return dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("unitKey", unit.getKey())
			.executeUpdate();
	}
}