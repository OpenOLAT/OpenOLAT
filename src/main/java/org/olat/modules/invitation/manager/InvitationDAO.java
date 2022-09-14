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
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationWithRepositoryEntry;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.modules.invitation.model.InvitationWithBusinessGroup;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.propertyhandlers.UserPropertyHandler;
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
		invitation.setStatus(InvitationStatusEnum.active);
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
	
	public boolean hasInvitations(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation.key from binvitation as invitation")
		  .append(" inner join invitation.baseGroup as bGroup")
		  .append(" inner join repoentrytogroup as reToGroup on (bGroup.key = reToGroup.group.key and reToGroup.defaultGroup=true)")
		  .append(" where reToGroup.entry.key=:repositoryEntryKey");

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", entry.getKey());
		
		List<Long> keys = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public boolean hasInvitations(BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation.key from binvitation as invitation")
		  .append(" inner join invitation.baseGroup as baseGroup")
		  .append(" inner join businessgroup as bgi on (baseGroup.key = bgi.baseGroup.key)")
		  .append(" where bgi.key=:businessGroupKey");

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("businessGroupKey", businessGroup.getKey());
		
		List<Long> keys = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	/**
	 * Find an invitation by its primary key.
	 * 
	 * @param key The primary key
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
	
	/**
	 * 
	 * @param entry The repository entry
	 * @param searchParams
	 * @return List of invitations strictly on the repository entry.
	 */
	public List<Invitation> findInvitations(Identity identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append(" invitation.identity.key=:inviteeKey or (invitation.identity.key is null and invitation.mail=:email)");

		return dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("inviteeKey", identity.getKey())
				  .setParameter("email", identity.getUser().getEmail())
				  .getResultList();
	}
	
	public List<Invitation> findInvitations(InvitationTypeEnum type, String email, Group group) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup baseGroup")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("baseGroup.key=:groupKey")
		  .and().append("invitation.type=:type")
		  .and().append("lower(invitation.mail)=:email");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Invitation.class)
				.setParameter("groupKey", group.getKey())
				.setParameter("email", email.toLowerCase())
				.setParameter("type", type)
				.getResultList();
	}
	
	public List<InvitationWithRepositoryEntry> findInvitationsWithRepositoryEntries(SearchInvitationParameters searchParams, boolean followToBusinessGroups) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation, v from binvitation as invitation")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join repoentrytogroup as reToGroup on (bGroup.key = reToGroup.group.key").append(" and reToGroup.defaultGroup=true", !followToBusinessGroups).append(")")
		  .append(" inner join reToGroup.entry as v ")
		  .append(" inner join fetch v.olatResource as vResource ")
		  .append(" ").append("inner", "left", searchParams.getIdentityKey() != null).append(" join fetch invitation.identity ident")
		  .append(" ").append("inner", "left", searchParams.getIdentityKey() != null).append(" join fetch ident.user as identUser");
		appendInvitationsSearchParametersToQuery(sb, searchParams);
		String[] userParams = appendInvitationsSearchFull(sb, searchParams.getUserPropertyHandlers(), searchParams.getSearchString(), false, true);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Object[].class);
		appendInvitationsParametersToQuery(query, searchParams);
		appendinvitationsSearchToQuery(userParams, query);
		
		List<Object[]> raws = query.getResultList();
		List<InvitationWithRepositoryEntry> invitations = new ArrayList<>(raws.size());
		for(Object[] raw:raws) {
			Invitation invitation = (Invitation)raw[0];
			RepositoryEntry entry = (RepositoryEntry)raw[1];
			invitations.add(new InvitationWithRepositoryEntry(invitation, entry));
		}
		return invitations;
	}
	
	public List<InvitationWithBusinessGroup> findInvitationsWitBusinessGroups(SearchInvitationParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation, bgi from binvitation as invitation")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join businessgroup as bgi on (bGroup.key = bgi.baseGroup.key)")
		  .append(" ").append("inner", "left", searchParams.getIdentityKey() != null).append(" join fetch invitation.identity ident")
		  .append(" ").append("inner", "left", searchParams.getIdentityKey() != null).append(" join fetch ident.user as identUser");
		appendInvitationsSearchParametersToQuery(sb, searchParams);
		String[] params = appendInvitationsSearchFull(sb, searchParams.getUserPropertyHandlers(), searchParams.getSearchString(), true, false);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Object[].class);
		appendInvitationsParametersToQuery(query, searchParams);
		appendinvitationsSearchToQuery(params, query);
		
		List<Object[]> raws = query.getResultList();
		List<InvitationWithBusinessGroup> invitations = new ArrayList<>(raws.size());
		for(Object[] raw:raws) {
			Invitation invitation = (Invitation)raw[0];
			BusinessGroup businessGroup = (BusinessGroup)raw[1];
			invitations.add(new InvitationWithBusinessGroup(invitation, businessGroup));
		}
		return invitations;
	}
	
	/**
	 * 
	 * @param entry The repository entry
	 * @param searchParams
	 * @return List of invitations strictly on the repository entry.
	 */
	public List<Invitation> findInvitations(RepositoryEntryRef entry, SearchInvitationParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join repoentrytogroup as reToGroup on (bGroup.key = reToGroup.group.key)")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("reToGroup.entry.key=:repositoryEntryKey and reToGroup.defaultGroup=true");
		appendInvitationsSearchParametersToQuery(sb, searchParams);
		String[] userParams = appendInvitationsSearchFull(sb, searchParams.getUserPropertyHandlers(), searchParams.getSearchString(), false, false);

		TypedQuery<Invitation> query = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("repositoryEntryKey", entry.getKey());
		appendInvitationsParametersToQuery(query, searchParams);
		appendinvitationsSearchToQuery(userParams, query);
		return query.getResultList();
	}
	
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup, SearchInvitationParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" inner join businessgroup as bgi on (bGroup.key = bgi.baseGroup.key)")
		  .append(" left join fetch invitation.identity ident")
		  .append(" left join fetch ident.user as identUser")
		  .where().append("bgi.key=:businessGroupKey");
		appendInvitationsSearchParametersToQuery(sb, searchParams);
		String[] userParams = appendInvitationsSearchFull(sb, searchParams.getUserPropertyHandlers(), searchParams.getSearchString(), true, false);
		
		TypedQuery<Invitation> query = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("businessGroupKey", businessGroup.getKey());
		appendInvitationsParametersToQuery(query, searchParams);
		appendinvitationsSearchToQuery(userParams, query);
		return query.getResultList();
	}
	
	private void appendInvitationsSearchParametersToQuery(QueryBuilder sb, SearchInvitationParameters searchParams) {
		if(searchParams.getStatus() != null) {
			sb.and().append("invitation.status=:status");
		}
		
		if(searchParams.getIdentityKey() != null) {
			sb.and().append("ident.key=:identityKey");
		}
		
		if(searchParams.getType() != null) {
			sb.and().append("invitation.type=:type");
		}
	}
	
	private void appendInvitationsParametersToQuery(TypedQuery<?> query, SearchInvitationParameters searchParams) {
		if(searchParams.getStatus() != null) {
			query.setParameter("status", searchParams.getStatus());
		}

		if(searchParams.getIdentityKey() != null) {
			query.setParameter("identityKey", searchParams.getIdentityKey());
		}
		
		if(searchParams.getType() != null) {
			query.setParameter("type", searchParams.getType());
		}
	}
	
	private String[] appendInvitationsSearchFull(QueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers,
			String search, boolean withBusinessGroups, boolean withRepositoryEntry) {
		String[] searchArr = null;

		if(StringHelper.containsNonWhitespace(search)) {
			String dbVendor = dbInstance.getDbVendor();
			searchArr = search.split(" ");

			sb.and().append(" (");
			boolean start = true;
			for(int i=0; i<searchArr.length; i++) {
				String searchParam = "search" + i;
				
				for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
					start = appendOr(sb, start);
					String searchAttr = "identUser.".concat(userPropertyHandler.getName());
					PersistenceHelper.appendFuzzyLike(sb, searchAttr, searchParam, dbVendor);
				}
				
				if(withBusinessGroups) {
					start = appendOr(sb, start);
					PersistenceHelper.appendFuzzyLike(sb, "bgi.name", searchParam, dbVendor);
				}
				
				if(withRepositoryEntry) {
					start = appendOr(sb, start);
					PersistenceHelper.appendFuzzyLike(sb, "v.displayname", searchParam, dbVendor);
				}
			}
			sb.append(")");
		}
		return searchArr;
	}
	
	private boolean appendOr(QueryBuilder sb, boolean start) {
		if(start) {
			start = false;
		} else {
			sb.append(" or ");
		}
		return start;
	}
	
	private void appendinvitationsSearchToQuery(String[] searchArr, TypedQuery<?> query) {
		if(searchArr != null) {
			for(int i=searchArr.length; i-->0; ) {
				query.setParameter("search" + i, PersistenceHelper.makeFuzzyQueryString(searchArr[i]));
			}
		}
	}
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitationByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" left join fetch invitation.identity ident")
		  .append(" where invitation.key=:key");

		List<Invitation> invitations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Invitation.class)
			.setParameter("key", key)
			.getResultList();
	    return invitations.isEmpty() ? null : invitations.get(0);
	}
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitationByToken(String token) {
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
	
	public int deleteInvitation(IdentityRef identity) {
		if(identity == null || identity.getKey() == null) return 0;
		
		String delete = "delete from binvitation as invitation where invitation.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(delete)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
}
