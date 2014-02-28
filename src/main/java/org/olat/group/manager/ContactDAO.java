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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.model.ContactKeyView;
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
	
	public Collection<Long> getDistinctGroupOwnersParticipants(Identity me) {
		List<Long> participants = getMembersForCount(me);
		List<Long> owners = getMembersForCount(me);
		
		Set<Long> contacts = new HashSet<Long>(participants);
		contacts.addAll(owners);
		return contacts;
	}
	
	private List<Long> getMembersForCount(Identity me) {
		StringBuilder sb = new StringBuilder();
		sb.append("select memv.identityKey from ").append(ContactKeyView.class.getName()).append(" memv ")
		  .append(" where memv.meKey=:identKey");
		
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
		//return getMembers(me, ContactOwnerView.class);
		return getMembers(me, GroupRoles.coach.name());
	}
	
	public List<ContactViewExtended> getParticipants(Identity me) {
		//return getMembers(me, ContactParticipantView.class);
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
			query.append("select distinct identity from ").append(ContactView.class.getName()).append(" as contact ");
		} else {
			query.append("select distinct identity.key from ").append(ContactView.class.getName()).append(" as contact ");
			     
		}
		query.append(" inner join contact.identity as identity ");
		query.append(" where contact.meKey=:identKey");
		if(Identity.class.equals(resultClass)) {
			query.append(" order by identity.name");
		}

		TypedQuery<T> db = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		db.setParameter("identKey", identity.getKey());
		return db;
	}
}
