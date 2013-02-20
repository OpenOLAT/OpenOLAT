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
package org.olat.group.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.model.BusinessGroupOwnerViewImpl;
import org.olat.group.model.BusinessGroupParticipantViewImpl;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Manage the "contacts"
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ContactDAO {

	@Autowired
	private DB dbInstance;
	
	
	public Collection<Long> getDistinctGroupOwnersParticipants(Identity me) {
		List<Long> owners = getMembersForCount(me, BusinessGroupOwnerViewImpl.class);
		List<Long> participants = getMembersForCount(me, BusinessGroupParticipantViewImpl.class);
		Set<Long> contacts = new HashSet<Long>(participants);
		contacts.addAll(owners);
		return contacts;
	}
	
	private List<Long> getMembersForCount(Identity me, Class<?> cl) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(memv.identityKey) from ").append(cl.getName()).append(" memv ")
		  .append(" where memv.ownerSecGroupKey in (")
		  .append("   select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey")
		  .append(" ) or memv.participantSecGroupKey in (")
		  .append("   select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("identKey", me.getKey())
				.getResultList();
	}

	public List<BusinessGroupOwnerViewImpl> getGroupOwners(Identity me) {
		return getMembers(me, BusinessGroupOwnerViewImpl.class);
	}
	
	public List<BusinessGroupParticipantViewImpl> getParticipants(Identity me) {
		return getMembers(me, BusinessGroupParticipantViewImpl.class);
	}
	
	private <U> List<U> getMembers(Identity me, Class<U> cl) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv from ").append(cl.getName()).append(" memv ")
		  .append(" where memv.ownerSecGroupKey in (")
		  .append("   select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey")
		  .append(" ) or memv.participantSecGroupKey in (")
		  .append("   select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), cl)
				.setParameter("identKey", me.getKey())
				.getResultList();
	}
	

	public int countContacts(Identity identity) {
		List<Long> result = createContactsQuery(identity, Long.class).getResultList();
		result.remove(identity.getKey());//not always a contact of myself with this query
		return result.size();
	}

	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		TypedQuery<Identity> query = createContactsQuery(identity, Identity.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults + 1);
		}
		List<Identity> contacts = query.getResultList();
		if(!contacts.remove(identity) && maxResults > 0 && contacts.size() > maxResults) {
			contacts.remove(contacts.size() - 1);
		}
		return contacts;
	}
	
	private <T> TypedQuery<T> createContactsQuery(Identity identity, Class<T> resultClass) {
		StringBuilder query = new StringBuilder();
		if(Identity.class.equals(resultClass)) {
			query.append("select distinct identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		} else {
			query.append("select distinct identity.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		}
		query.append(" inner join sgmi.identity as identity ")
		     .append(" inner join sgmi.securityGroup as secGroup ")
		     .append(" where ")
		     .append("  secGroup in (")
		     .append("    select bg1.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg1,").append(Property.class.getName()).append(" as prop where prop.grp=bg1 and prop.name='displayMembers' and prop.longValue in (1,3,5,7)")
		     .append("      and bg1.ownerGroup in (select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg3.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg3,").append(Property.class.getName()).append(" as prop where prop.grp=bg3 and prop.name='displayMembers' and prop.longValue in (1,3,5,7)")
		     .append("      and bg3.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg2.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg2,").append(Property.class.getName()).append(" as prop where prop.grp=bg2 and prop.name='displayMembers' and prop.longValue in (2,3,6,7)")
		     .append("      and bg2.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg4.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg4,").append(Property.class.getName()).append(" as prop where prop.grp=bg4 and prop.name='displayMembers' and prop.longValue in (2,3,6,7)")
		     .append("      and bg4.ownerGroup in (select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey)")
		     .append("  )");
		if(Identity.class.equals(resultClass)) {
			query.append("order by identity.name");
		}

		TypedQuery<T> db = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		db.setParameter("identKey", identity.getKey());
		return db;
	}



}
