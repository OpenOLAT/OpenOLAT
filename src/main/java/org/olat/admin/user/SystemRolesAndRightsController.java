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
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
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
	private FormLayoutContainer rolesCont;
	private FormLink addToOrganisationButton;
	private MultipleSelectionElement sendLoginDeniedEmailEl;
	private final List<MultipleSelectionElement> rolesEls = new ArrayList<>();

	private 	List<String> statusKeys;
	private 	List<String> statusValues;
	
	private Identity editedIdentity;
	private List<Organisation> organisations;
	
	private final Roles managerRoles;
	private final List<Organisation> manageableOrganisations;
	

	private CloseableModalController cmc;
	private SelectOrganisationController selectOrganisationCtrl;
	
	@Autowired
	private DB dbInstance;
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
	public SystemRolesAndRightsController(WindowControl wControl, UserRequest ureq, Identity identity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.editedIdentity = identity;
		organisations = organisationService.getOrganisations(editedIdentity, OrganisationRoles.values());

		managerRoles = ureq.getUserSession().getRoles();
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), managerRoles,
				OrganisationRoles.administrator, OrganisationRoles.usermanager);
		
		initStatusKeysAndValues();
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

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean iAmOlatAdmin = managerRoles.isOLATAdmin();
		
		// anonymous
		FormLayoutContainer anonymousCont = FormLayoutContainer.createDefaultFormLayout("anonc", getTranslator());
		formLayout.add(anonymousCont);
		
		anonymousEl = uifactory.addRadiosVertical(
				"anonymous", "rightsForm.isAnonymous", anonymousCont, 
				new String[]{"true", "false"},
				new String[]{translate("rightsForm.isAnonymous.true"), translate("rightsForm.isAnonymous.false")}
		);
		uifactory.addSpacerElement("syssep", anonymousCont, false);
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS.booleanValue()) {
			anonymousEl.addActionListener(FormEvent.ONCLICK);
		} else {
			anonymousCont.setVisible(false);
		}
		
		// roles
		rolesCont = FormLayoutContainer.createDefaultFormLayout("rolesc", getTranslator());
		formLayout.add(rolesCont);
		
		initFormRoles();
		
		FormLayoutContainer statusCont = FormLayoutContainer.createDefaultFormLayout("statusc", getTranslator());
		formLayout.add(statusCont);
		
		statusEl = uifactory.addRadiosVertical(
				"status", "rightsForm.status", statusCont,
				statusKeys.toArray(new String[statusKeys.size()]),
				statusValues.toArray(new String[statusKeys.size()])
		);
		statusEl.addActionListener(FormEvent.ONCHANGE);
		sendLoginDeniedEmailEl = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", statusCont, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailEl.setLabel(null, null);
		
		rolesSep.setVisible(iAmOlatAdmin);
		statusEl.setVisible(iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER.booleanValue());
		sendLoginDeniedEmailEl.setVisible(false);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		statusCont.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
	}

	private void initFormRoles() {
		for(Organisation organisation:organisations) {
			initFormRoles(rolesCont, organisation);
		}
		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);
		upgradeableToOrganisations.removeAll(organisations);
		if(!upgradeableToOrganisations.isEmpty()) {
			addToOrganisationButton = uifactory.addFormLink("rightsForm.add.to.organisation", rolesCont, Link.BUTTON);
		}

		rolesSep = uifactory.addSpacerElement("rolesSep", rolesCont, false);
	}
	
	private void initFormRoles(FormItemContainer formLayout, Organisation organisation) {
		boolean iAmAdmin = managerRoles.isOLATAdmin() || managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator); 
		
		List<String> roleKeys = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();
		
		roleKeys.add(OrganisationRoles.invitee.name());
		roleValues.add(translate("rightsForm.isInvitee"));

		roleKeys.add(OrganisationRoles.user.name());
		roleValues.add(translate("rightsForm.isUser"));

		roleKeys.add(OrganisationRoles.coach.name());
		roleValues.add(translate("rightsForm.isCoach"));

		if (iAmAdmin) {
			roleKeys.add(OrganisationRoles.usermanager.name());
			roleValues.add(translate("rightsForm.isUsermanager"));
		}
		
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.groupmanager.name());
			roleValues.add(translate("rightsForm.isGroupmanager"));
		}

		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.poolmanager.name());
			roleValues.add(translate("rightsForm.isPoolmanager"));
		}
		
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_CURRICULUMMANAGERS.booleanValue()) {
			roleKeys.add(OrganisationRoles.curriculummanager.name());
			roleValues.add(translate("rightsForm.isCurriculummanager"));
		}

		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS.booleanValue()) {
			roleKeys.add(OrganisationRoles.author.name());
			roleValues.add(translate("rightsForm.isAuthor"));
		}

		if (iAmAdmin) {
			roleKeys.add(OrganisationRoles.administrator.name());
			roleValues.add(translate("rightsForm.isAdmin"));
		}

		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER.booleanValue()) {
			roleKeys.add(OrganisationRoles.learnresourcemanager.name());
			String iname = editedIdentity.getUser().getProperty("institutionalName", null);
			roleValues.add(
				iname != null
				? translate("rightsForm.isInstitutionalResourceManager.institution",iname)
				: translate("rightsForm.isInstitutionalResourceManager")
			);
		}
		
		MultipleSelectionElement rolesEl;
		if(organisations.size() == 1) {
			rolesEl = uifactory.addCheckboxesVertical(
					"roles-" + organisation.getKey(), "rightsForm.roles", formLayout,
					roleKeys.toArray(new String[roleKeys.size()]),
					roleValues.toArray(new String[roleValues.size()]), 1);
		} else {
			rolesEl = uifactory.addCheckboxesHorizontal(
					"roles-" + organisation.getKey(), "rightsForm.roles", formLayout,
					roleKeys.toArray(new String[roleKeys.size()]),
					roleValues.toArray(new String[roleValues.size()]));
		}
		if(organisation.getParent() != null) {
			rolesEl.setLabel("rightsForm.roles.for", new String[] { organisation.getDisplayName() });
		}
		rolesEl.setUserObject(new RolesElement(roleKeys, organisation, rolesEl));
		rolesEl.setEnabled(iAmAdmin || managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager));
		
		rolesEls.add(rolesEl);
	}
	
	private void update() {
		Roles editedRoles = securityManager.getRoles(editedIdentity);
		if(editedRoles.isGuestOnly()) {
			anonymousEl.select("true", true);
		} else {
			anonymousEl.select("false", true);
		}
		
		for(MultipleSelectionElement rolesEl:rolesEls) {
			RolesElement wrapper = (RolesElement)rolesEl.getUserObject();
			update(wrapper, editedRoles.getRoles(wrapper.getOrganisation()));
		}
	}
	
	private void updateRoles() {
		if(rolesSep != null) {
			rolesCont.remove(rolesSep);
		}
		if(addToOrganisationButton != null) {
			rolesCont.remove(addToOrganisationButton);
		}
		for(MultipleSelectionElement roleEl:rolesEls) {
			rolesCont.remove(roleEl);
		}
		initFormRoles();
		update();
	}
	
	private void update(RolesElement wrapper, RolesByOrganisation editedRoles) {
		wrapper.setRole(OrganisationRoles.user, editedRoles.isUser());
		wrapper.setRole(OrganisationRoles.invitee, editedRoles.isInvitee());
	
		wrapper.setRole(OrganisationRoles.coach, editedRoles.isCoach());
		wrapper.setRole(OrganisationRoles.author, editedRoles.isAuthor());

		wrapper.setRole(OrganisationRoles.usermanager, editedRoles.isUserManager());
		wrapper.setRole(OrganisationRoles.groupmanager, editedRoles.isGroupManager());
		wrapper.setRole(OrganisationRoles.learnresourcemanager, editedRoles.isLearnResourceManager());
		wrapper.setRole(OrganisationRoles.poolmanager, editedRoles.isPoolManager());
		wrapper.setRole(OrganisationRoles.curriculummanager, editedRoles.isCurriculumManager());
		
		wrapper.setRole(OrganisationRoles.administrator, editedRoles.isAdministrator());
		if(editedRoles.isAdministrator()) {
			statusEl.setEnabled(false);
		}
		
		setStatus(editedIdentity.getStatus());
		wrapper.getRolesEl().setVisible(!isAnonymous());
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

	private Integer getStatus() {
		return new Integer(statusEl.getSelectedKey());
	}
	
	public boolean getSendLoginDeniedEmail() {
		return sendLoginDeniedEmailEl.isSelected(0);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(selectOrganisationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doAddIdentityToOrganisation(selectOrganisationCtrl.getSelectedOrganisation());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectOrganisationCtrl);
		removeAsListenerAndDispose(cmc);
		selectOrganisationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addToOrganisationButton == source) {
			doAddToOrganisation(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddToOrganisation(UserRequest ureq) {
		if(selectOrganisationCtrl != null) return;
		
		List<Organisation> upgradeableToOrganisations = new ArrayList<>(manageableOrganisations);
		upgradeableToOrganisations.removeAll(organisations);
		selectOrganisationCtrl = new SelectOrganisationController(ureq, getWindowControl(), upgradeableToOrganisations);
		listenTo(selectOrganisationCtrl);
		
		String title = translate("rightsForm.add.to.organisation");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectOrganisationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	

	private void doAddIdentityToOrganisation(Organisation organisation) {
		organisationService.addMember(organisation, editedIdentity, OrganisationRoles.user);
		dbInstance.commit();
		organisations = organisationService.getOrganisations(editedIdentity, OrganisationRoles.values());
		
		updateRoles();
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
		Roles editorRoles = usess.getRoles();
		boolean iAmOlatAdmin = editorRoles.isOLATAdmin();
	
		Roles editedRoles = securityManager.getRoles(editedIdentity);
		
		// 1) general user type - anonymous or user
		// anonymous users
		boolean isAnonymous = editedRoles.isGuestOnly();
		if (iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS.booleanValue()) {
			isAnonymous = anonymousEl.getSelectedKey().equals("true");
		}
		
		if(isAnonymous) {
			saveAnonymousData(ureq);
		} else {
			for(MultipleSelectionElement rolesEl:rolesEls) {
				if(rolesEl.isEnabled()) {
					RolesElement wrapper = (RolesElement)rolesEl.getUserObject();
					saveOrganisationRolesFormData(wrapper, editedRoles, editorRoles);
				}
			}
		}
		
		if ((iAmOlatAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_STATUS.booleanValue()) &&  !editedIdentity.getStatus().equals(getStatus()) ) {			
			int oldStatus = editedIdentity.getStatus();
			String oldStatusText = (oldStatus == Identity.STATUS_PERMANENT ? "permanent" : (oldStatus == Identity.STATUS_ACTIV ? "active" : (oldStatus == Identity.STATUS_LOGIN_DENIED ? "login_denied" : (oldStatus == Identity.STATUS_DELETED ? "deleted" : "unknown"))));
			int newStatus = getStatus();
			String newStatusText = (newStatus == Identity.STATUS_PERMANENT ? "permanent" : (newStatus == Identity.STATUS_ACTIV ? "active" : (newStatus == Identity.STATUS_LOGIN_DENIED ? "login_denied"	 : (newStatus == Identity.STATUS_DELETED ? "deleted" : "unknown"))));
			if(oldStatus != newStatus && newStatus == Identity.STATUS_LOGIN_DENIED && getSendLoginDeniedEmail()) {
				userBulkChangeManager.sendLoginDeniedEmail(editedIdentity);
			}
			
			editedIdentity = securityManager.saveIdentityStatus(editedIdentity, newStatus);
			logAudit("User::" + getIdentity().getKey() + " changed account status for user::" + editedIdentity.getKey() + " from::" + oldStatusText + " to::" + newStatusText, null);
		}
	}
	
	private void saveAnonymousData(UserRequest ureq) {
		organisationService.setAsGuest(editedIdentity);
		dbInstance.commit();
		organisations = organisationService.getOrganisations(editedIdentity, OrganisationRoles.values());
		updateRoles();
	}
	
	private void saveOrganisationRolesFormData(RolesElement wrapper, Roles editedRoles, Roles editorRoles) {
		
		Organisation organisation = wrapper.getOrganisation();
		boolean iAmUserManager = editorRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager);
		boolean iAmAdmin = editorRoles.isOLATAdmin() || editorRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator);
		
		RolesByOrganisation editedOrganisationRoles = editedRoles.getRoles(wrapper.getOrganisation());

		// 2) system roles
		boolean invitee = wrapper.getRole(OrganisationRoles.invitee);
		boolean user = wrapper.getRole(OrganisationRoles.user);
		boolean coach = wrapper.getRole(OrganisationRoles.coach);

		// group manager
		boolean groupManager = editedOrganisationRoles.hasRole(OrganisationRoles.groupmanager);
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS.booleanValue()) {
			groupManager = wrapper.getRole(OrganisationRoles.groupmanager);
		}
		// pool manager
		boolean poolmanager = editedOrganisationRoles.hasRole(OrganisationRoles.poolmanager);
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			poolmanager = wrapper.getRole(OrganisationRoles.poolmanager);
		}
		// curriculum manager
		boolean curriculummanager = editedOrganisationRoles.hasRole(OrganisationRoles.curriculummanager);
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS.booleanValue()) {
			curriculummanager = wrapper.getRole(OrganisationRoles.curriculummanager);
		}
		// author
		boolean author = editedOrganisationRoles.hasRole(OrganisationRoles.author);
		if (iAmAdmin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS.booleanValue()) {
			author = wrapper.getRole(OrganisationRoles.author);
		}
		// user manager, only allowed by admin
		boolean usermanager = editedOrganisationRoles.hasRole(OrganisationRoles.usermanager);
		if (iAmAdmin) {
			usermanager = wrapper.getRole(OrganisationRoles.usermanager);
		}
	 	// institutional resource manager, only allowed by admin
		boolean learnresourcemanager = editedOrganisationRoles.hasRole(OrganisationRoles.learnresourcemanager);
		if (iAmUserManager || iAmAdmin) {
			learnresourcemanager = wrapper.getRole(OrganisationRoles.learnresourcemanager);
		}
		// system administrator, only allowed by admin
		boolean admin = editedOrganisationRoles.hasRole(OrganisationRoles.administrator);
		if (iAmAdmin) {
			admin = wrapper.getRole(OrganisationRoles.administrator);
		}
		
		RolesByOrganisation updatedRoles = RolesByOrganisation.roles(wrapper.getOrganisation(),
				false, invitee, user, coach, author,
				groupManager, poolmanager, curriculummanager,
				usermanager, learnresourcemanager, admin);
		securityManager.updateRoles(getIdentity(), editedIdentity, updatedRoles);
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}
	
	private class RolesElement {
		
		private final List<String> roleKeys;
		private final Organisation organisation;
		private final MultipleSelectionElement rolesEl;
		
		public RolesElement(List<String> roleKeys, Organisation organisation, MultipleSelectionElement rolesEl) {
			this.roleKeys = roleKeys;
			this.rolesEl = rolesEl;
			this.organisation = organisation;
		}
		
		public MultipleSelectionElement getRolesEl() {
			return rolesEl;
		}
		
		public Organisation getOrganisation() {
			return organisation;
		}
		
		private boolean getRole(OrganisationRoles k) {
			return roleKeys.contains(k.name()) && rolesEl.getSelectedKeys().contains(k.name());
		}
		
		private void setRole(OrganisationRoles k, boolean enabled) {
			if(roleKeys.contains(k.name()) && enabled) {
				rolesEl.select(k.name(), enabled);
			}
		}
	}
}
