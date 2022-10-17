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

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.model.ContactView;
import org.olat.group.model.ContactViewExtended;
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
	
	public int countContacts(IdentityRef identity) {
		Collection<Long> contactSet = getDistinctGroupOwnersParticipants(identity);
		contactSet.remove(identity.getKey());
		return contactSet.size();
	}
	
	public Collection<Long> getDistinctGroupOwnersParticipants(IdentityRef me) {
		List<Long> contactList = getMembersForCount(me);
		return new HashSet<>(contactList);
	}
	
	private List<Long> getMembersForCount(IdentityRef me) {
		StringBuilder sb = new StringBuilder();
		sb.append("select contact.identity.key from businessgroup bgroup ")
		  .append(" inner join bgroup.baseGroup baseGroup")
		  .append(" inner join baseGroup.members contact")
		  .append(" inner join baseGroup.members me on (me.identity.key=:identKey)")
		  .append(" where (bgroup.ownersVisibleIntern=true and contact.role='coach')")
		  .append("  or (bgroup.participantsVisibleIntern=true and contact.role='participant')");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("identKey", me.getKey())
				.getResultList();
	}
	
	public List<ContactViewExtended> getContactWithExtendedInfos(Identity me) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv from ").append(ContactViewExtended.class.getName()).append(" memv ")
		  .append(" where memv.meKey=:identKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ContactViewExtended.class)
				.setParameter("identKey", me.getKey())
				.getResultList();
	}

	public List<ContactViewExtended> getGroupOwners(Identity me) {
		return getMembers(me, GroupRoles.coach.name());
	}
	
	public List<ContactViewExtended> getParticipants(Identity me) {
		return getMembers(me, GroupRoles.participant.name());
	}
	
	private List<ContactViewExtended> getMembers(Identity me, String role) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv from ").append(ContactViewExtended.class.getName()).append(" memv ")
		  .append(" where memv.meKey=:identKey and memv.role=:role");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ContactViewExtended.class)
				.setParameter("identKey", me.getKey())
				.setParameter("role", role)
				.getResultList();
	}

	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct identity from ").append(ContactView.class.getName()).append(" as contact ")
		  .append(" inner join contact.identity as identity ")
		  .append(" where contact.meKey=:identKey")
		  .append(" order by identity.name");

		TypedQuery<Identity> db = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("identKey", identity.getKey())
				.setFirstResult(firstResult);
		if(maxResults > 0) {
			db.setMaxResults(maxResults + 1);
		}
		List<Identity> contacts = db.getResultList();
		if(!contacts.remove(identity) && maxResults > 0 && contacts.size() > maxResults) {
			contacts.remove(contacts.size() - 1);
		}
		return contacts;
	}
}
