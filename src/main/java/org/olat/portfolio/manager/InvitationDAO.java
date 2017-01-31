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
package org.olat.portfolio.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.TypedQuery;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.portfolio.model.InvitationImpl;
import org.olat.user.UserManager;
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
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	/**
	 * Create and persist an invitation with its security group and security token.
	 * @return
	 */
	public Invitation createAndPersistInvitation() {
		Group group = groupDao.createGroup();
		
		InvitationImpl invitation = new InvitationImpl();
		invitation.setToken(UUID.randomUUID().toString());
		invitation.setBaseGroup(group);
		dbInstance.getCurrentEntityManager().persist(invitation);
		return invitation;
	}
	
	public Invitation update(Invitation invitation) {
		return dbInstance.getCurrentEntityManager().merge(invitation);
	}
	
	public Identity createIdentityFrom(Invitation invitation, Locale locale) {
		String tempUsername = UUID.randomUUID().toString();
		User user = userManager.createAndPersistUser(invitation.getFirstName(), invitation.getLastName(), invitation.getMail());
		user.getPreferences().setLanguage(locale.toString());
		Identity invitee = securityManager.createAndPersistIdentity(tempUsername, user, null, null, null);
		groupDao.addMembershipTwoWay(invitation.getBaseGroup(), invitee, GroupRoles.invitee.name());
		return invitee;
	}
	
	/**
	 * Is the invitation linked to any valid policies
	 * @param token
	 * @param atDate
	 * @return
	 */
	public boolean hasInvitations(String token, Date atDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(relation) from structuretogroup as relation ")
		  .append(" inner join relation.group as baseGroup")
	      .append(" where exists (select invitation.key from binvitation as invitation where ")
	      .append("  invitation.baseGroup=baseGroup and invitation.token=:token")
	      .append(" )");
		  
		if(atDate != null) {
			sb.append(" and (relation.validFrom is null or relation.validFrom<=:date)")
			  .append(" and (relation.validTo is null or relation.validTo>=:date)");
		}

		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("token", token);
		if(atDate != null) {
		  	query.setParameter("date", atDate);
		}
		  
		Number counter = query.getSingleResult();
	    return counter == null ? false : counter.intValue() > 0;
	}
	
	/**
	 * Find an invitation by its security group
	 * @param secGroup
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitation(Group group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
		  .append(" where bGroup=:group");

		List<Invitation> invitations = dbInstance.getCurrentEntityManager()
				  .createQuery(sb.toString(), Invitation.class)
				  .setParameter("group", group)
				  .getResultList();
		if(invitations.isEmpty()) return null;
		return invitations.get(0);
	}
	
	/**
	 * Find an invitation by its security token
	 * @param token
	 * @return The invitation or null if not found
	 */
	public Invitation findInvitation(String token) {
		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from binvitation as invitation ")
		  .append(" inner join fetch invitation.baseGroup bGroup")
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
	    return invitations == null ? false : invitations.intValue() > 0;
	}
	
	/**
	 * Delete an invitation
	 * @param invitation
	 */
	public void deleteInvitation(Invitation invitation) {
		//fxdiff: FXOLAT-251: nothing persisted, nothing to delete
		if(invitation == null || invitation.getKey() == null) return;
		dbInstance.getCurrentEntityManager().remove(invitation);
	}
	
	/**
	 * Clean up old invitation and set to deleted temporary users
	 */
	public void cleanUpInvitations() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Date currentTime = cal.getTime();
		cal.add(Calendar.HOUR, -6);
		Date dateLimit = cal.getTime();

		StringBuilder sb = new StringBuilder();
		sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ")
		  .append(" inner join invitation.baseGroup baseGroup ")
		  .append(" where invitation.creationDate<:dateLimit")//someone can create an invitation but not add it to a policy within millisecond
		  .append(" and not exists (")
		  //select all valid policies from this security group
		  .append("  select policy.group from structuretogroup as policy ")
		  .append("   where policy.group=baseGroup ")
		  .append("   and (policy.validFrom is null or policy.validFrom<=:currentDate)")
		  .append("   and (policy.validTo is null or policy.validTo>=:currentDate)")
		  .append(" )");

		List<Invitation> oldInvitations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Invitation.class)
				.setParameter("currentDate", currentTime)
				.setParameter("dateLimit", dateLimit)
				.getResultList();
		
		if(oldInvitations.isEmpty()) {
			return;
		}
	  
		SecurityGroup olatUserSecGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
		for(Invitation invitation:oldInvitations) {
			List<Identity> identities = groupDao.getMembers(invitation.getBaseGroup(), GroupRoles.invitee.name());
			//normally only one identity
			for(Identity identity:identities) {
				if(identity.getStatus().compareTo(Identity.STATUS_VISIBLE_LIMIT) >= 0) {
	  			//already deleted
				} else if(securityManager.isIdentityInSecurityGroup(identity, olatUserSecGroup)) {
	  			//out of scope
				} else {
	  			//delete user
					UserDeletionManager.getInstance().deleteIdentity(identity);
				}
			}
			dbInstance.getCurrentEntityManager().remove(invitation);
			dbInstance.commit();
		}
	}
}
