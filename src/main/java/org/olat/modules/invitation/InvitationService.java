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
import org.olat.modules.invitation.model.InvitationEntry;
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
	
	public Invitation findInvitation(String token);

	public Invitation findInvitation(Binder binder, IdentityRef identity);

	public List<InvitationEntry> findInvitations(Identity identity);

	public List<Invitation> findInvitations(RepositoryEntryRef entry);
	
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup);
	
	public Identity getOrCreateIdentityAndPersistInvitation(Invitation invitation, Group group, Locale locale);
	
	public Invitation update(Invitation invitation, String firstName, String lastName, String email);
	
	/**
	 * End the registration process and accept the invitation, create the membership...
	 * 
	 * @param invitation The invitation
	 * @param identity The identity which accepts the invitation
	 */
	public void acceptInvitation(Invitation invitation, Identity identity);
	
	/**
	 * Link the identity to the invitation.
	 * 
	 * @param invitation The invitation
	 * @param identity The identity
	 * @return The merged identity
	 */
	public Invitation update(Invitation invitation, Identity identity);
	
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
