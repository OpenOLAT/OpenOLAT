/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.NamedGroupImpl;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SecurityGroupDAO implements UserDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(SecurityGroupDAO.class);
	
	@Autowired
	private DB dbInstance;

	public SecurityGroup createAndPersistSecurityGroup() {
		SecurityGroupImpl sgi = new SecurityGroupImpl();
		dbInstance.saveObject(sgi);
		return sgi;
	}
	
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
		if (secGroup == null) {
			throw new AssertException("getIdentitiesOfSecurityGroup: ERROR secGroup was null !!");
		} 
		return getIdentitiesOfSecurityGroup(secGroup, 0, -1);
	}
	
	public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup, int firstResult, int maxResults) {
		if (secGroup == null) {
			throw new AssertException("getIdentitiesOfSecurityGroup: ERROR secGroup was null !!");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
	   .append(" inner join sgmsi.identity identity ")
	   .append(" inner join fetch  identity.user user ")
			.append(" where sgmsi.securityGroup=:secGroup");

		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("secGroup", secGroup);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<SecurityGroup> getSecurityGroupsForIdentity(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select sgi from ").append(SecurityGroupImpl.class.getName()).append(" as sgi, ")
		  .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append(" where sgmsi.securityGroup=sgi and sgmsi.identity.key=:identityKey");

		return dbInstance.getCurrentEntityManager()
	  		.createQuery(sb.toString(), SecurityGroup.class)
	  		.setParameter("identityKey", identity.getKey())
	  		.getResultList();
	}

	public List<Identity> getIdentitiesOfSecurityGroups(List<SecurityGroup> secGroups) {
		if (secGroups == null || secGroups.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(identity) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append(" inner join sgmsi.identity identity ")
		  .append(" inner join fetch  identity.user user ")
		  .append(" where sgmsi.securityGroup in (:secGroups)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("secGroups", secGroups)
				.getResultList();
	}

	/**
	 * @param secGroup
	 * @return a List of Object[] with the array[0] = Identity, array[1] =
	 *         addedToGroupTimestamp
	 */
	public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup) {
	   StringBuilder sb = new StringBuilder();
	   sb.append("select ii, sgmsi.lastModified from ").append(IdentityImpl.class.getName()).append(" as ii")
	     .append(" inner join fetch ii.user as iuser, ")
	     .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi")
	     .append(" where sgmsi.securityGroup=:secGroup and sgmsi.identity = ii");
	 
	   return dbInstance.getCurrentEntityManager()
				 .createQuery(sb.toString(), Object[].class)
				 .setParameter("secGroup", secGroup)
				 .getResultList();
	}
	
	/**
	 * use only if really needed. Normally better use
	 * isIdentityPermittedOnResourceable!
	 * 
	 * @param identity
	 * @param secGroup
	 * @return true if the identity is in the group
	 */
	public boolean isIdentityInSecurityGroup(Identity identity, SecurityGroup secGroup) {
		if (secGroup == null || identity == null) return false;
		String queryString = "select sgmsi.key from org.olat.basesecurity.SecurityGroupMembershipImpl as sgmsi where sgmsi.identity.key=:identitykey and sgmsi.securityGroup.key=:securityGroupKey";

		List<Long> membership = dbInstance.getCurrentEntityManager()
			.createQuery(queryString, Long.class)
			.setParameter("identitykey", identity.getKey())
			.setParameter("securityGroupKey", secGroup.getKey())
			.setHint("org.hibernate.cacheable", Boolean.TRUE)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return membership != null && !membership.isEmpty() && membership.get(0) != null;
	}

	/**
	 * @param secGroup
	 * @return The number of members in the security group
	 */
	public int countIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
		DB db = dbInstance;
		String q = "select count(sgm) from org.olat.basesecurity.SecurityGroupMembershipImpl sgm where sgm.securityGroup = :group";
		List<Long> counts = db.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.setParameter("group", secGroup)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return counts.get(0).intValue();
	}

	public SecurityGroup findSecurityGroupByName(String securityGroupName) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select sgi from ").append(NamedGroupImpl.class.getName()).append(" as ngroup ")
		  .append(" inner join ngroup.securityGroup sgi")
		  .append(" where ngroup.groupName=:groupName");

		List<SecurityGroup> group = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SecurityGroup.class)
				.setParameter("groupName", securityGroupName)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		int size = group.size();
		if (size == 0) return null;
		if (size != 1) throw new AssertException("non unique name in namedgroup: " + securityGroupName);
		return group.get(0);
	}

	/**
	 * @param identity
	 * @param secGroup
	 */
	public void addIdentityToSecurityGroup(Identity identity, SecurityGroup secGroup) {
		SecurityGroupMembershipImpl sgmsi = new SecurityGroupMembershipImpl();
		sgmsi.setIdentity(identity);
		sgmsi.setSecurityGroup(secGroup);
		sgmsi.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().persist(sgmsi);
	}

	/**
	 * Removes the identity from this security group or does nothing if the
	 * identity is not in the group at all.
	 * 
	 * @param identity
	 * @param secGroup
	 */
	public boolean removeIdentityFromSecurityGroup(Identity identity, SecurityGroup secGroup) {
		return removeIdentityFromSecurityGroups(Collections.singletonList(identity), Collections.singletonList(secGroup));
	}

	/**
	 * Remove an Identity
	 * @param identity
	 * @param secGroups
	 * @return
	 */
	public boolean removeIdentityFromSecurityGroups(List<Identity> identities, List<SecurityGroup> secGroups) {
		if(identities == null || identities.isEmpty()) return true;//nothing to do
		if(secGroups == null || secGroups.isEmpty()) return true;//nothing to do
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as msi ")
		  .append("  where msi.identity.key in (:identityKeys) and msi.securityGroup.key in (:secGroupKeys)");
		
		List<Long> identityKeys = new ArrayList<>();
		for(Identity identity:identities) {
			identityKeys.add(identity.getKey());
		}
		List<Long> secGroupKeys = new ArrayList<>();
		for(SecurityGroup secGroup:secGroups) {
			secGroupKeys.add(secGroup.getKey());
		}
		int rowsAffected = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("identityKeys", identityKeys)
				.setParameter("secGroupKeys", secGroupKeys)
				.executeUpdate();
		return rowsAffected > 0;
	}
	
	/**
	 * Change the last modificaiton date of the membership
	 * @param identity
	 * @param secGroups
	 */
	public void touchMembership(Identity identity, List<SecurityGroup> secGroups) {
		if (secGroups == null || secGroups.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select sgmsi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmsi ")
		  .append("where sgmsi.identity.key=:identityKey and sgmsi.securityGroup in (:securityGroups)");
		
		List<ModifiedInfo> infos = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ModifiedInfo.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("securityGroups", secGroups)
				.getResultList();
		
		for(ModifiedInfo info:infos) {
			info.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(info);
		}
	}

	/**
	 * Removes the group with all the identities contained in it, the identities
	 * itself are of course not deleted.
	 * 
	 * @param secGroup
	 */
	public void deleteSecurityGroup(SecurityGroup secGroup) {
		// we do not use hibernate cascade="delete", but implement our own (to be
		// sure to understand our code)
		StringBuilder sb = new StringBuilder();
		sb.append("select secGroup from ").append(SecurityGroupImpl.class.getName()).append(" as secGroup ")
		  .append("where secGroup.key=:securityGroupKey");
		List<SecurityGroup> reloadedSecGroups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), SecurityGroup.class)
				.setParameter("securityGroupKey", secGroup.getKey())
				.getResultList();
		if(reloadedSecGroups.size() == 1) {
			secGroup = reloadedSecGroups.get(0);
			
			// 1) delete associated users (need to do it manually, hibernate knows
			// nothing about
			// the membership, modeled manually via many-to-one and not via set)
			dbInstance.getCurrentEntityManager()
				.createQuery("delete from org.olat.basesecurity.SecurityGroupMembershipImpl where securityGroup=:securityGroup")
				.setParameter("securityGroup", secGroup)
				.executeUpdate();
			// 2) delete all policies
	
			dbInstance.getCurrentEntityManager()
				.createQuery("delete from org.olat.basesecurity.PolicyImpl where securityGroup=:securityGroup")
				.setParameter("securityGroup", secGroup)
				.executeUpdate();
			// 3) delete security group
			dbInstance.getCurrentEntityManager()
				.remove(secGroup);
		}
	}

	@Override
	public int deleteUserDataPriority() {
		return 10;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// Remove legacy security group memberships
		List<SecurityGroup> securityGroups = getSecurityGroupsForIdentity(identity);
		for (SecurityGroup secGroup : securityGroups) {
			removeIdentityFromSecurityGroup(identity, secGroup);
			log.info("Removing identity::" + identity.getKey() + " from security group::" + secGroup.getKey()
					+ ", resourceableTypeName::" + secGroup.getResourceableTypeName() + ", resourceableId"
					+ secGroup.getResourceableId());
		}
	}
}
