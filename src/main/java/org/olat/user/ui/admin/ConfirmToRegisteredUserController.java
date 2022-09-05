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
package org.olat.user.ui.admin;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.model.InvitationEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmToRegisteredUserController extends FormBasicController {
	
	private final Identity identityToModify;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private OrganisationService organisationService;
	
	public ConfirmToRegisteredUserController(UserRequest ureq, WindowControl wControl, Identity identityToModify) {
		super(ureq, wControl, "confirm_conversion");
		this.identityToModify = identityToModify;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String[] i18nArgs = new String[] {
					identityToModify.getUser().getFirstName(),
					identityToModify.getUser().getLastName(),
					identityToModify.getUser().getEmail(),
					userManager.getUserDisplayName(identityToModify)
			};
			String message = translate("convert.invitee.to.user.text", i18nArgs);
			layoutCont.contextPut("message", message);
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("convert.invitee.to.user", formLayout);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Roles roles = securityManager.getRoles(identityToModify);
		
		// Remove all invitee memberships (only on the default organization but who knows)
		List<OrganisationRef> organisations = roles.getOrganisationsWithRole(OrganisationRoles.invitee);
		for(OrganisationRef organisation:organisations) {
			List<String> rolesAsString = securityManager.getRolesAsString(identityToModify, organisation);
			List<OrganisationRoles> rolesList = OrganisationRoles.toValues(rolesAsString);
			rolesList.remove(OrganisationRoles.invitee);
			
			RolesByOrganisation updatedRoles = new RolesByOrganisation(organisation, rolesList);
			securityManager.updateRoles(getIdentity(), identityToModify, updatedRoles);
		}
		
		// Add the user membership to the default organization
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<String> rolesAsString = securityManager.getRolesAsString(identityToModify, defaultOrganisation);
		if(!rolesAsString.contains(OrganisationRoles.user.name())) {
			List<OrganisationRoles> rolesList = OrganisationRoles.toValues(rolesAsString);
			rolesList.add(OrganisationRoles.user);
			
			RolesByOrganisation updatedRoles = new RolesByOrganisation(defaultOrganisation, rolesList);
			securityManager.updateRoles(getIdentity(), identityToModify, updatedRoles);
		}
		
		List<InvitationEntry> invitationEntries = invitationService.findInvitations(identityToModify);
		for(InvitationEntry invitationEntry:invitationEntries) {
			Invitation invitation = invitationEntry.getInvitation();
			invitation.setStatus(InvitationStatusEnum.inactive);
			invitationService.update(invitation);
		}
		dbInstance.commit();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
