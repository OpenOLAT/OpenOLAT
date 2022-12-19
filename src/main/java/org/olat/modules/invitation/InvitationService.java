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
package org.olat.modules.invitation;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Invitation;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.invitation.model.InvitationWithBusinessGroup;
import org.olat.modules.invitation.model.InvitationWithRepositoryEntry;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.portfolio.Binder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface InvitationService {
	
	/**
	 * 
	 * @return A invitation which is NOT persisted on the database
	 */
	public Invitation createInvitation(InvitationTypeEnum type);
	
	public Identity createIdentityFrom(Invitation invitation, Locale locale);
	
	
	public long countInvitations();
	
	public boolean hasInvitations(String token);
	
	public boolean hasInvitations(RepositoryEntryRef entry);
	
	public boolean hasInvitations(BusinessGroupRef businessGroup);
	
	public Invitation getInvitation(Invitation invitation);
	
	public Invitation getInvitationByKey(Long key);
	
	public Invitation findInvitation(String token);

	public Invitation findInvitation(Binder binder, IdentityRef identity);
	
	public List<Invitation> findInvitations(Identity identity);

	/**
	 * 
	 * @param searchParams The search parameters
	 * @param followToBusinessGroups true look up the invitations of business groups
	 * 	linked to the entries, false limit strictly to invitations on repository entries
	 * @return A list of invitations with their repository entries
	 */
	public List<InvitationWithRepositoryEntry> findInvitationsWithEntries(SearchInvitationParameters searchParams, boolean followToBusinessGroups);
	
	public List<InvitationWithBusinessGroup> findInvitationsWithBusinessGroups(SearchInvitationParameters searchParams);

	public List<Invitation> findInvitations(RepositoryEntryRef entry, SearchInvitationParameters searchParams);
	
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup, SearchInvitationParameters searchParams);
	
	/**
	 * Find an invitation for the same resource, same invitee and same list of roles.
	 * 
	 * @param email The email of the invitee
	 * @param roles The roles for invitation
	 * @param group The security group
	 * @return An invitation if found or null
	 */
	public Invitation findSimilarInvitation(InvitationTypeEnum type, String email, List<String> roles, Group group);
	
	public Identity getOrCreateIdentityAndPersistInvitation(Invitation invitation, Group group, Locale locale, Identity doer);
	
	public Invitation update(Invitation invitation, String firstName, String lastName, String email);
	
	/**
	 * End the registration process and accept the invitation, create the membership...
	 * 
	 * @param invitation The invitation
	 * @param identity The identity which accepts the invitation
	 */
	public void acceptInvitation(Invitation invitation, Identity identity);
	
	public void inactivateInvitations(RepositoryEntryRef entry, IdentityRef identity);
	
	/**
	 * Link the identity to the invitation.
	 * 
	 * @param invitation The invitation
	 * @param identity The identity
	 * @return The merged identity
	 */
	public Invitation update(Invitation invitation, Identity identity);
	
	/**
	 * Merge the invitation.
	 * 
	 * @param invitation The invitation to update
	 * @return The merged invitation
	 */
	public Invitation update(Invitation invitation);
	
	/**
	 * @param invitation The invitation
	 * @return A direct URL with the invitation attached
	 */
	public String toUrl(Invitation invitation);
	
	public String toUrl(Invitation invitation, RepositoryEntry repositoryEntry);
	
	public String toUrl(Invitation invitation, BusinessGroup businessGroup);
	
	public String toUrl(Invitation invitation, Binder binder);
	

	public void deleteInvitation(Invitation invitation);

}
