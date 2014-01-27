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
import org.olat.group.model.ContactOwnerView;
import org.olat.group.model.ContactParticipantView;
import org.olat.group.model.ContactKeyOwnerView;
import org.olat.group.model.ContactKeyParticipantView;
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
		List<Long> owners = getMembersForCount(me, ContactKeyOwnerView.class);
		List<Long> participants = getMembersForCount(me, ContactKeyParticipantView.class);
		Set<Long> contacts = new HashSet<Long>(participants);
		contacts.addAll(owners);
		return contacts;
	}
	
	private List<Long> getMembersForCount(Identity me, Class<?> cl) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv.identityKey from ").append(cl.getName()).append(" memv ")
		  .append(" where exists (")
		  .append("   select ownerSgmi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi")
		  .append("     where ownerSgmi.securityGroup=memv.ownerSecGroupKey and ownerSgmi.identity.key=:identKey")
		  .append(" ) or exists (")
		  .append("   select partSgmi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi")
		  .append("     where memv.participantSecGroupKey=partSgmi.securityGroup and partSgmi.identity.key=:identKey")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("identKey", me.getKey())
				.getResultList();
	}

	public List<ContactOwnerView> getGroupOwners(Identity me) {
		return getMembers(me, ContactOwnerView.class);
	}
	
	public List<ContactParticipantView> getParticipants(Identity me) {
		return getMembers(me, ContactParticipantView.class);
	}
	
	private <U> List<U> getMembers(Identity me, Class<U> cl) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv from ").append(cl.getName()).append(" memv ")
		  .append(" where exists (")
		  .append("   select ownerSgmi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi")
		  .append("     where memv.ownerSecGroupKey=ownerSgmi.securityGroup and ownerSgmi.identity.key=:identKey")
		  .append(" ) or exists (")
		  .append("   select partSgmi from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi")
		  .append("     where memv.participantSecGroupKey=partSgmi.securityGroup and partSgmi.identity.key=:identKey")
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
		     .append("    select bg1.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg1 where bg1.ownersVisibleIntern=true")
		     .append("      and bg1.ownerGroup in (select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg3.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg3 where bg3.ownersVisibleIntern=true")
		     .append("      and bg3.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg2.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg2 where bg2.participantsVisibleIntern=true")
		     .append("      and bg2.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg4.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg4 where bg4.participantsVisibleIntern=true")
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
