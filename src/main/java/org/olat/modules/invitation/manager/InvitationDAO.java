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
package org.olat.modules.invitation.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationEntry;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is only for e-Portfolio. For wider useage, need a refactor of the datamodel
 * and of process and workflow. Don't be afraid of reference ot e-Portfolio datamodel
 * here.
 * 
 * 
 * Initial date: 25.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service(value="invitationDao")
public class InvitationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Invitation createInvitation(InvitationTypeEnum type) {
		InvitationImpl invitation = new InvitationImpl();
		invitation.setToken(UUID.randomUUID().toString());
		invitation.setType(type);
		return invitation;
	}
	
	public Invitation persist(Invitation invitation) {
		if(invitation.getKey() != null) {
			invitation = dbInstance.getCurrentEntityManager().merge(invitation);
		} else {
			dbInstance.getCurrentEntityManager().persist(invitation);
		}
		return invitation;
	}
	
	public Invitation update(Invitation invitation) {
		return dbInstance.getCurrentEntityManager().merge(invitation);
	} 
	
	/**
	 * Is the invitation linked to any valid policies
	 * @param token
	 * @return
	 */
	public boolean hasInvitations(String token) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation.key from binvitation as invitation")
		  .append(" inner join invitation.baseGroup as baseGroup")
		  .append(" where invitation.token=:token")
		  .append(" and (exists (select binder.key from pfbinder as binder")
		  .append("   where binder.baseGroup.key=baseGroup.key")
		  .append(" ) or exists (select relGroup.key from repoentrytogroup as relGroup")
		  .append("   where relGroup.group.key=baseGroup.key")
		  .append(" ) or exists (select bgi.key from businessgroup as bgi")
		  .append("   where bgi.baseGroup.key=baseGroup.key")
		  .append(" ))");

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("token", token);
		
		List<Long> keys = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" where invitation.key=:key");

		List<Invitation> invitations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Invitation.class)
			.setParameter("key", key)
			.getResultList();
	    return invitations.isEmpty() ? null : invitations.get(0);
	}
	
	/**
	 * 
	 * Warning! The E-mail is used in this case as a foreign key to match
	 * the identity and the invitation on a base group which can have several
	 * identities.
	 * 
	 * @param group
	 * @param identity
	 * @return
	 */
	public Invitation findInvitation(Group group, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join bGroup.members as members")
		  .append(" inner join members.identity as identity")
		  .append(" inner join identity.user as user")
		  .append(" where bGroup.key=:groupKey and identity.key=:inviteeKey and members.role=:role and invitation.mail=user.email");

		List<Invitation> invitations = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("groupKey", group.getKey())
				  .setParameter("inviteeKey", identity.getKey())
				  .setParameter("role", GroupRoles.invitee.name())
				  .getResultList();
		if(invitations.isEmpty()) return null;
		return invitations.get(0);
	}
	
	public List<InvitationEntry> findInvitations(Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation, v from binvitation as invitation")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" left join repoentrytogroup as reToGroup on (bGroup.key = reToGroup.group.key and reToGroup.defaultGroup=true)")
		  .append(" left join reToGroup.entry as v ")
		  .append(" left join fetch v.olatResource as vResource ")
		  .append(" left join bGroup.members as members")
		  .append(" where invitation.identity.key=:inviteeKey or (invitation.identity.key is null and invitation.mail=:email)");

		List<Object[]> raws = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Object[].class)
				  .setParameter("inviteeKey", identity.getKey())
				  .setParameter("email", identity.getUser().getEmail())
				  .getResultList();
		List<InvitationEntry> invitations = new ArrayList<>(raws.size());
		for(Object[] raw:raws) {
			Invitation invitation = (Invitation)raw[0];
			RepositoryEntry entry = (RepositoryEntry)raw[1];
			invitations.add(new InvitationEntry(invitation, entry));
		}
		return invitations;
	}
	
	public List<Invitation> findInvitations(RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join repoentrytogroup as reToGroup on (bGroup.key = reToGroup.group.key)")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("reToGroup.entry.key=:repositoryEntryKey and reToGroup.defaultGroup=true");

		return dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("repositoryEntryKey", entry.getKey())
				  .getResultList();
	}
	
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join businessgroup as bgi on (bGroup.key = bgi.baseGroup.key)")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("bgi.key=:businessGroupKey");

		return dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("businessGroupKey", businessGroup.getKey())
				  .getResultList();
	}
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitation(String token) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" left join fetch invitation.identity ident")
		  .append(" where invitation.token=:token");

		List<Invitation> invitations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Invitation.class)
			.setParameter("token", token)
			.getResultList();
	    return invitations.isEmpty() ? null : invitations.get(0);
	}
	
	
	/**
	 * the number of invitations
	 * @return
	 */
	public long countInvitations() {
		String sb = "select count(invitation) from binvitation as invitation";
		Number invitations = dbInstance.getCurrentEntityManager()
				.createQuery(sb, Number.class)
				.getSingleResult();		
		return invitations == null ? 0l : invitations.longValue();
	}
	
	/**
	 * Check if the identity has an invitation, valid or not
	 * @param identity
	 * @return
	 */
	public boolean isInvitee(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(invitation) from binvitation as invitation")
		  .append(" inner join invitation.baseGroup as baseGroup ")
		  .append(" inner join baseGroup.members as members")
		  .append(" inner join members.identity as ident")
		  .append(" where ident.key=:identityKey");
		  
		Number invitations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Number.class)
			.setParameter("identityKey", identity.getKey())
			.getSingleResult();
	    return invitations != null && invitations.intValue() > 0;
	}
	
	/**
	 * Delete an invitation
	 * @param invitation
	 */
	public void deleteInvitation(Invitation invitation) {
		if(invitation == null || invitation.getKey() == null) return;
		
		Invitation refInvitation = dbInstance.getCurrentEntityManager()
			.getReference(InvitationImpl.class, invitation.getKey());
		dbInstance.getCurrentEntityManager().remove(refInvitation);
	}
	
	public int deleteInvitation(Group group) {
		if(group == null || group.getKey() == null) return 0;
		
		String delete = "delete from binvitation as invitation where invitation.baseGroup.key=:groupKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(delete)
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
	}
}
