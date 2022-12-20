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
package org.olat.modules.invitation.restapi;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 20 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag (name = "Invitations")
public abstract class AbstractInvitationsWebService {
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected InvitationModule invitationModule;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected InvitationService invitationService;
	
	
	protected Invitation createOrUpdateTemporaryInvitation(Invitation invitation, Group group, InvitationTypeEnum type,
			Integer expirationInHours, Date expirationDate, Locale locale, Identity doer) {
		Invitation similarInvitation = invitationService.findSimilarInvitation(type,
				invitation.getMail(), invitation.getRoleList(), group);
		
		Identity identity;
		if(similarInvitation == null) {
			identity = invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, locale, doer);
		} else {
			if(similarInvitation.getStatus() != InvitationStatusEnum.active) {
				similarInvitation.setStatus(InvitationStatusEnum.active);
				similarInvitation = invitationService.update(similarInvitation);
			}
			invitation = similarInvitation;
			identity = similarInvitation.getIdentity();
		}
		
		if(identity.getStatus().intValue() == Identity.STATUS_INACTIVE) {
			identity = securityManager.reactivatedIdentity(identity);
		}
		if(identity != null && !identity.getStatus().equals(Identity.STATUS_PERMANENT)
				&& (expirationInHours != null || expirationDate != null)) {
			if(expirationInHours != null) {
				expirationDate = DateUtils.addHours(new Date(), expirationInHours.intValue());
			}
			securityManager.saveIdentityExpirationDate(identity, expirationDate, doer);
		}
		return invitation;
	}
	
	protected Invitation createOrUpdateTemporaryInvitation(InvitationVO invitationVo, Group group, InvitationTypeEnum type,
			Locale locale, Identity doer) {
		
		Invitation invitation;
		if(invitationVo.getKey() == null) {
			List<String> roles = List.of(GroupRoles.participant.name());
			invitation = invitationService.createInvitation(type);

			invitation.setRegistration(invitationVo.getRegistration());
			invitation.setFirstName(invitationVo.getFirstName());
			invitation.setLastName(invitationVo.getLastName());
			invitation.setMail(invitationVo.getEmail());
			invitation.setRegistration(invitationVo.getRegistration() == null || invitationVo.getRegistration());
			invitation.setRoleList(roles);
			
			invitation = createOrUpdateTemporaryInvitation(invitation, group, type, null, invitationVo.getExpirationDate(), locale, doer);	
		} else {
			invitation = invitationService.getInvitationByKey(invitationVo.getKey());
			if(invitationVo.getRegistration() != null) {
				invitation.setRegistration(invitationVo.getRegistration());
			}
			if(StringHelper.containsNonWhitespace(invitationVo.getStatus())) {
				invitation.setStatus(InvitationStatusEnum.valueOf(invitationVo.getStatus()));
			}
			invitation = invitationService.update(invitation, invitationVo.getFirstName(), invitationVo.getLastName(), invitationVo.getEmail());
		}
		
		return invitation;
	}

}
