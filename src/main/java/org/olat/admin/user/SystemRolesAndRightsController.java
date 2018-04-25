/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jan 27, 2006
 * @author gnaegi
 * <pre>
 * Description:
 * Controller that is used to manipulate the users system roles and rights. When calling
 * this controller make sure the user who calls the controller meets the following 
 * criterias:
 * - user is system administrator
 * or
 * - user tries not to modify a system administrator or user administrator
 * - user tries not to modify an author if author rights are not enabled for user managers
 * - user tries not to modify a group manager if group manager rights are not enabled for user managers 
 * - user tries not to modify a guest if guest rights are not enabled for user managers 
 * 
 * Usually this controller is called by the UserAdminController that takes care of all this. 
 * There should be no need to use it anywhere else.
 */
public class SystemRolesAndRightsController extends FormBasicController {
	
	private SpacerElement rolesSep;
	private SingleSelection statusEl;
	private SingleSelection anonymousEl;
	private MultipleSelectionElement rolesEl;
	private MultipleSelectionElement sendLoginDeniedEmailEl;

	private 	List<String> roleKeys;
	private 	List<String> roleValues;
	private 	List<String> statusKeys;
	private 	List<String> statusValues;
	
	private Identity editedIdentity;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserBulkChangeManager userBulkChangeManager;
	
	/**
	 * Constructor for a controller that lets you edit the users system roles and rights.
	 * @param wControl
	 * @param ureq
	 * @param identity identity to be edited
	 */
	public SystemRolesAndRightsController(WindowControl wControl, UserRequest ureq, Identity identity){
		super(ureq, wControl);
		this.editedIdentity = identity;
		
		initStatusKeysAndValues();
		initRolesKeysAndValues(ureq);
		initForm(ureq);
		update();
	}
	
	private void initStatusKeysAndValues() {
		statusKeys = new ArrayList<>(4);
		statusKeys.add(Integer.toString(Identity.STATUS_ACTIV));
		statusKeys.add(Integer.toString(Identity.STATUS_PERMANENT));
		statusKeys.add(Integer.toString(Identity.STATUS_LOGIN_DENIED));

		statusValues = new ArrayList<>(4);
		statusValues.add(translate("rightsForm.status.activ"));
		statusValues.add(translate("rightsForm.status.permanent"));
		statusValues.add(translate("rightsForm.status.login_denied"));
		
		if (editedIdentity.getStatus() == Identity.STATUS_DELETED) {
			statusKeys.add(Integer.toString(Identity.STATUS_DELETED));
			statusValues.add(translate("rightsForm.status.deleted"));
		}
	}
	
	private void initRolesKeysAndValues(UserRequest ureq) {
		boolean iAmOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		
		roleKeys = new ArrayList<>();
		roleValues = new ArrayList<>();

		if (iAmOlatAdmin) {
			roleKeys.add(OrganisationRoles.usermanager.name());
			roleValues.add(translate("rightsForm.isUsermanager"));
		}
		
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.groupmanager.name());
			roleValues.add(translate("rightsForm.isGroupmanager"));
		}

		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.poolmanager.name());
			roleValues.add(translate("rightsForm.isPoolmanager"));
		}
		
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_CURRICULUMMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.curriculummanager.name());
			roleValues.add(translate("rightsForm.isCurriculummanager"));
		}

		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS.booleanValue()) {
			roleKeys.add(OrganisationRoles.author.name());
			roleValues.add(translate("rightsForm.isAuthor"));
		}

		if (iAmOlatAdmin) {
			roleKeys.add(OrganisationRoles.administrator.name());
			roleValues.add(translate("rightsForm.isAdmin"));
		}

		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER.booleanValue()) {
			roleKeys.add(OrganisationRoles.learnresourcemanager.name());
			String iname = editedIdentity.getUser().getProperty("institutionalName", null);
			roleValues.add(
				iname != null
				? translate("rightsForm.isInstitutionalResourceManager.institution",iname)
				: translate("rightsForm.isInstitutionalResourceManager")
			);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean iAmOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		
		anonymousEl = uifactory.addRadiosVertical(
				"anonymous", "rightsForm.isAnonymous", formLayout, 
				new String[]{"true", "false"},
				new String[]{translate("rightsForm.isAnonymous.true"), translate("rightsForm.isAnonymous.false")}
		);
		SpacerElement sysSep = uifactory.addSpacerElement("syssep", formLayout, false);
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS.booleanValue()) {
			anonymousEl.addActionListener(FormEvent.ONCLICK);
		} else {
			anonymousEl.setVisible(false);
			sysSep.setVisible(false);
		}
		
		rolesEl = uifactory.addCheckboxesVertical(
				"roles", "rightsForm.roles", formLayout,
				roleKeys.toArray(new String[roleKeys.size()]),
				roleValues.toArray(new String[roleValues.size()]), 1);
		rolesSep = uifactory.addSpacerElement("rolesSep", formLayout, false);
		
		statusEl = uifactory.addRadiosVertical(
				"status", "rightsForm.status", formLayout,
				statusKeys.toArray(new String[statusKeys.size()]),
				statusValues.toArray(new String[statusKeys.size()])
		);
		statusEl.addActionListener(FormEvent.ONCHANGE);
		sendLoginDeniedEmailEl = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", formLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailEl.setLabel(null, null);
		
		rolesSep.setVisible(iAmOlatAdmin);
		statusEl.setVisible(iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER.booleanValue());
		sendLoginDeniedEmailEl.setVisible(false);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	private void update() {
		Roles editedRoles = securityManager.getRoles(editedIdentity);

		if(editedRoles.isGuestOnly()) {
			anonymousEl.select("true", true);
		} else {
			anonymousEl.select("false", true);
		}
		
		setRole(OrganisationRoles.usermanager, editedRoles.isUserManager());
		setRole(OrganisationRoles.groupmanager, editedRoles.isGroupManager());
		setRole(OrganisationRoles.author, editedRoles.isAuthor());
		setRole(OrganisationRoles.administrator, editedRoles.isOLATAdmin());
		if(editedRoles.isOLATAdmin()) {
			statusEl.setEnabled(false);
		}
		setRole(OrganisationRoles.learnresourcemanager, editedRoles.isInstitutionalResourceManager());
		setRole(OrganisationRoles.poolmanager, editedRoles.isPoolAdmin());
		setRole(OrganisationRoles.curriculummanager, editedRoles.isCurriculumManager());
		setStatus(editedIdentity.getStatus());
		rolesEl.setVisible(!isAnonymous());
		rolesSep.setVisible(!isAnonymous());
	}
	
	private void setStatus(Integer status) {
		String statusStr = status.toString();
		for(String statusKey:statusKeys) {
			if(statusStr.equals(statusKey)) {
				statusEl.select(statusKey, true);
			}
		}
		statusEl.setEnabled(!Identity.STATUS_DELETED.equals(status));
	}
	
	public boolean isAnonymous() {
		return anonymousEl.getSelectedKey().equals("true");
	}
	
	private boolean getRole(OrganisationRoles k) {
		return roleKeys.contains(k.name()) && rolesEl.getSelectedKeys().contains(k.name());
	}
	
	private void setRole(OrganisationRoles k, boolean enabled) {
		if(roleKeys.contains(k.name()) && enabled) {
			rolesEl.select(k.name(), enabled);
		}
	}
	
	private Integer getStatus() {
		return new Integer(statusEl.getSelectedKey());
	}
	
	public boolean getSendLoginDeniedEmail() {
		return sendLoginDeniedEmailEl.isSelected(0);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveFormData(ureq);
		update();
	}

	/**
	 * Persist form data in database. User needs to logout / login to activate changes. A bit tricky here
	 * is that only form elements should be gettet that the user is allowed to manipulate. See also the 
	 * comments in SystemRolesAndRightsForm. 
	 * @param myIdentity
	 * @param form
	 */
	private void saveFormData(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		boolean iAmOlatAdmin = usess.getRoles().isOLATAdmin();
		boolean iAmUserManager = usess.getRoles().isUserManager();
		List<String> currentRoles = securityManager.getRolesAsString(editedIdentity);
		
		// 1) general user type - anonymous or user
		// anonymous users
		boolean isAnonymous = currentRoles.contains(OrganisationRoles.guest.name());
		Boolean canGuestsByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;	
		if (canGuestsByConfig.booleanValue() || iAmOlatAdmin) {
			isAnonymous = anonymousEl.getSelectedKey().equals("true");
		}
		
		// 2) system roles
		// group manager
		boolean groupManager = currentRoles.contains(OrganisationRoles.groupmanager.name());
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS.booleanValue()) {
			groupManager = getRole(OrganisationRoles.groupmanager);
		}
		// pool manager
		boolean poolmanager = currentRoles.contains(OrganisationRoles.poolmanager.name());
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			poolmanager = getRole(OrganisationRoles.poolmanager);
		}
		// curriculum manager
		boolean curriculummanager = currentRoles.contains(OrganisationRoles.curriculummanager.name());
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			curriculummanager = getRole(OrganisationRoles.curriculummanager);
		}
		// author
		boolean author = currentRoles.contains(OrganisationRoles.author.name());
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS.booleanValue()) {
			author = getRole(OrganisationRoles.author);
		}
		// user manager, only allowed by admin
		boolean usermanager = currentRoles.contains(OrganisationRoles.usermanager.name());
		if (iAmOlatAdmin) {
			usermanager = getRole(OrganisationRoles.usermanager);
		}
	 	// institutional resource manager, only allowed by admin
		boolean learnresourcemanager = currentRoles.contains(OrganisationRoles.learnresourcemanager.name());
		if (iAmUserManager || iAmOlatAdmin) {
			learnresourcemanager = getRole(OrganisationRoles.learnresourcemanager);
		}
		// system administrator, only allowed by admin
		boolean admin = currentRoles.contains(OrganisationRoles.administrator.name());
		if (iAmOlatAdmin) {
			admin = getRole(OrganisationRoles.administrator);
		}
		
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Roles updatedRoles = new Roles(false, admin, usermanager, groupManager, author, isAnonymous,
				learnresourcemanager, poolmanager, curriculummanager, false);//TODO roles
		securityManager.updateRoles(getIdentity(), editedIdentity, defOrganisation, updatedRoles);
		
		if ((iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_STATUS.booleanValue()) &&  !editedIdentity.getStatus().equals(getStatus()) ) {			
			int oldStatus = editedIdentity.getStatus();
			String oldStatusText = (oldStatus == Identity.STATUS_PERMANENT ? "permanent" : (oldStatus == Identity.STATUS_ACTIV ? "active" : (oldStatus == Identity.STATUS_LOGIN_DENIED ? "login_denied" : (oldStatus == Identity.STATUS_DELETED ? "deleted" : "unknown"))));
			int newStatus = getStatus();
			String newStatusText = (newStatus == Identity.STATUS_PERMANENT ? "permanent" : (newStatus == Identity.STATUS_ACTIV ? "active" : (newStatus == Identity.STATUS_LOGIN_DENIED ? "login_denied"	 : (newStatus == Identity.STATUS_DELETED ? "deleted" : "unknown"))));
			if(oldStatus != newStatus && newStatus == Identity.STATUS_LOGIN_DENIED && getSendLoginDeniedEmail()) {
				userBulkChangeManager.sendLoginDeniedEmail(editedIdentity);
			}
			
			editedIdentity = securityManager.saveIdentityStatus(editedIdentity, newStatus);
			logAudit("User::" + getIdentity().getName() + " changed account status for user::" + editedIdentity.getName() + " from::" + oldStatusText + " to::" + newStatusText, null);
		}
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}
}
