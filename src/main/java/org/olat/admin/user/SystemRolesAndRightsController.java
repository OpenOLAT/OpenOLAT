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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.util.Formatter;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserModule;
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
	
	private StaticTextElement lastLoginEl;
	private StaticTextElement inactivationDateEl;
	private StaticTextElement reactivationDateEl;
	private StaticTextElement daysInactivationEl;
	private StaticTextElement daysDeletionEl;
	
	private SpacerElement rolesSep;
	private SingleSelection statusEl;
	private SingleSelection anonymousEl;
	private DateChooser expirationDateEl;
	private FormLayoutContainer rolesCont;
	private FormLink addToOrganisationButton;
	private MultipleSelectionElement sendLoginDeniedEmailEl;
	private final List<MultipleSelectionElement> rolesEls = new ArrayList<>();

	private int counter = 0;
	private SelectionValues statusKeys;
	
	/**
	 * The roles without inheritance
	 */
	private Roles editedRoles;
	private Identity editedIdentity;
	private List<Organisation> organisations;
	
	private final Roles managerRoles;
	private final List<Organisation> manageableOrganisations;
	

	private CloseableModalController cmc;
	private SelectOrganisationController selectOrganisationCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserModule userModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
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
		
		editedRoles = securityManager.getRoles(editedIdentity, false);
		editedRoles.getOrganisations();
		
		organisations = new ArrayList<>();
		for(OrganisationRef organisation: editedRoles.getOrganisations()) {
			organisations.add(organisationService.getOrganisation(organisation));
		}

		managerRoles = ureq.getUserSession().getRoles();
		manageableOrganisations = organisationService.getOrganisations(getIdentity(), managerRoles,
				OrganisationRoles.administrator, OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		
		initStatusKeysAndValues();
		initForm(ureq);
		update();
	}
	
	private void initStatusKeysAndValues() {
		statusKeys = new SelectionValues();
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_ACTIV), translate("rightsForm.status.activ")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PERMANENT), translate("rightsForm.status.permanent")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PENDING), translate("rightsForm.status.pending")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_INACTIVE), translate("rightsForm.status.inactive")));
		statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_LOGIN_DENIED), translate("rightsForm.status.login_denied")));
		if (editedIdentity.getStatus() != null && editedIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
			statusKeys.add(SelectionValues.entry(Integer.toString(Identity.STATUS_DELETED), translate("rightsForm.status.deleted")));
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean iAmAdmin = managerRoles.isManagerOf(OrganisationRoles.administrator, editedRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, editedRoles);
		boolean iAmUserManager = managerRoles.isManagerOf(OrganisationRoles.usermanager, editedRoles);
	
		// anonymous
		FormLayoutContainer anonymousCont = FormLayoutContainer.createDefaultFormLayout("anonc", getTranslator());
		formLayout.add(anonymousCont);
		
		anonymousEl = uifactory.addRadiosVertical(
				"anonymous", "rightsForm.guest", anonymousCont, 
				new String[]{"true", "false"},
				new String[]{translate("role.guest.true"), translate("role.guest.false")}
		);
		uifactory.addSpacerElement("syssep", anonymousCont, false);
		if (iAmAdmin) {
			anonymousEl.addActionListener(FormEvent.ONCLICK);
		} else {
			anonymousCont.setVisible(false);
		}
		
		// roles
		rolesCont = FormLayoutContainer.createDefaultFormLayout("rolesc", getTranslator());
		formLayout.add(rolesCont);
		
		initFormRoles();
		initFormStatus(ureq, formLayout, iAmAdmin, iAmUserManager);
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
	
	private void initFormStatus(UserRequest ureq, FormItemContainer formLayout, boolean iAmAdmin, boolean iAmUserManager) {
		FormLayoutContainer statusCont = FormLayoutContainer.createDefaultFormLayout("statusc", getTranslator());
		formLayout.add(statusCont);
		
		// status
		statusEl = uifactory.addRadiosVertical("status", "rightsForm.status", statusCont, statusKeys.keys(), statusKeys.values());
		statusEl.addActionListener(FormEvent.ONCHANGE);
		sendLoginDeniedEmailEl = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", statusCont, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailEl.setLabel(null, null);
		
		rolesSep.setVisible(iAmAdmin);
		statusEl.setVisible(iAmAdmin || iAmUserManager);
		sendLoginDeniedEmailEl.setVisible(false);
		
		// life cycle information
		expirationDateEl = uifactory.addDateChooser("rightsForm.expiration.date", null, statusCont);
		lastLoginEl = uifactory.addStaticTextElement("rightsForm.last.login", "", statusCont);
		inactivationDateEl = uifactory.addStaticTextElement("rightsForm.inactivation.date", "", statusCont);
		reactivationDateEl = uifactory.addStaticTextElement("rightsForm.reactivation.date", "", statusCont);
		daysInactivationEl = uifactory.addStaticTextElement("rightsForm.days.inactivation", "", statusCont);
		daysDeletionEl = uifactory.addStaticTextElement("rightsForm.days.deletion", "", statusCont);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		statusCont.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
	}
	
	private void initFormRoles(FormItemContainer formLayout, Organisation organisation) {
		boolean admin = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator)
				|| managerRoles.isSystemAdmin(); 
		boolean userManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager); 
		boolean rolesManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.rolesmanager); 
		
		List<String> roleKeys = new ArrayList<>();
		List<String> roleValues = new ArrayList<>();

		roleKeys.add(OrganisationRoles.invitee.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.invitee.name())));

		roleKeys.add(OrganisationRoles.user.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.user.name())));

		roleKeys.add(OrganisationRoles.author.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.author.name())));

		roleKeys.add(OrganisationRoles.usermanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.usermanager.name())));

		roleKeys.add(OrganisationRoles.rolesmanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.rolesmanager.name())));

		roleKeys.add(OrganisationRoles.groupmanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.groupmanager.name())));

		roleKeys.add(OrganisationRoles.poolmanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.poolmanager.name())));

		roleKeys.add(OrganisationRoles.curriculummanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.curriculummanager.name())));

		roleKeys.add(OrganisationRoles.lecturemanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.lecturemanager.name())));

		roleKeys.add(OrganisationRoles.qualitymanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.qualitymanager.name())));

		roleKeys.add(OrganisationRoles.linemanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.linemanager.name())));

		roleKeys.add(OrganisationRoles.learnresourcemanager.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.learnresourcemanager.name())));

		roleKeys.add(OrganisationRoles.principal.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.principal.name())));

		roleKeys.add(OrganisationRoles.administrator.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.administrator.name())));

		roleKeys.add(OrganisationRoles.sysadmin.name());
		roleValues.add(translate("role.".concat(OrganisationRoles.sysadmin.name())));

		MultipleSelectionElement rolesEl = uifactory.addCheckboxesHorizontal(
					"roles_" + (++counter), "rightsForm.roles", formLayout,
					roleKeys.toArray(new String[roleKeys.size()]),
					roleValues.toArray(new String[roleValues.size()]));
		if(organisations.size() > 1 || !organisation.getIdentifier().equals(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER)) {
			rolesEl.setLabel("rightsForm.roles.for", new String[] { organisation.getDisplayName() });
		}
		rolesEl.setUserObject(new RolesElement(roleKeys, organisation, rolesEl));
		rolesEl.addActionListener(FormEvent.ONCHANGE);
		
		if(admin) {
			rolesEl.setEnabled(new HashSet<>(roleKeys), true);
		} else if(userManager) {
			Set<String> enabled = new HashSet<>();
			enabled.add(OrganisationRoles.invitee.name());
			enabled.add(OrganisationRoles.user.name());
			enabled.add(OrganisationRoles.author.name());
			rolesEl.setEnabled(enabled, true);
			Set<String> disabled = new HashSet<>(roleKeys);
			disabled.removeAll(enabled);
			rolesEl.setEnabled(disabled, false);
		} else if(rolesManager) {
			Set<String> enabled = new HashSet<>();
			enabled.add(OrganisationRoles.invitee.name());
			enabled.add(OrganisationRoles.user.name());
			enabled.add(OrganisationRoles.author.name());
			enabled.add(OrganisationRoles.curriculummanager.name());
			enabled.add(OrganisationRoles.groupmanager.name());
			enabled.add(OrganisationRoles.learnresourcemanager.name());
			enabled.add(OrganisationRoles.lecturemanager.name());
			enabled.add(OrganisationRoles.linemanager.name());
			enabled.add(OrganisationRoles.poolmanager.name());
			enabled.add(OrganisationRoles.qualitymanager.name());
			enabled.add(OrganisationRoles.rolesmanager.name());
			enabled.add(OrganisationRoles.usermanager.name());
			rolesEl.setEnabled(enabled, true);
			Set<String> disabled = new HashSet<>(roleKeys);
			disabled.removeAll(enabled);
			rolesEl.setEnabled(disabled, false);
		} else {
			rolesEl.setEnabled(new HashSet<>(), false);
		}
		
		rolesEls.add(rolesEl);
	}
	
	private void update() {
		editedRoles = securityManager.getRoles(editedIdentity, false);
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
		rolesEls.clear();
		initFormRoles();
		update();
	}
	
	private void update(RolesElement wrapper, RolesByOrganisation editedRolesByOrg) {
		for(OrganisationRoles role:OrganisationRoles.values()) {
			boolean hasRole = editedRolesByOrg != null && editedRolesByOrg.hasRole(role);
			wrapper.setRole(role, hasRole);
		}
		wrapper.saveSelectedRoles();
		
		if(editedRolesByOrg != null && editedRolesByOrg.hasSomeRoles(OrganisationRoles.administrator, OrganisationRoles.sysadmin, OrganisationRoles.rolesmanager)) {
			statusEl.setEnabled(false);
		}

		setStatus(editedIdentity.getStatus());
		wrapper.getRolesEl().setVisible(!isAnonymous());
		rolesSep.setVisible(!isAnonymous());
		
		expirationDateEl.setDate(editedIdentity.getExpirationDate());
		expirationDateEl.setVisible(!editedRoles.isSystemAdmin() && !editedRoles.isAdministrator());
		
		Formatter formatter = Formatter.getInstance(getLocale());
		String lastLogin = formatter.formatDateAndTime(editedIdentity.getLastLogin());
		lastLoginEl.setValue(lastLogin == null ? "" : lastLogin);
		
		Date inactivationDate = editedIdentity.getInactivationDate();
		inactivationDateEl.setValue(formatter.formatDate(inactivationDate));
		inactivationDateEl.setVisible(inactivationDate != null);
		
		Date reactivationDate = editedIdentity.getReactivationDate();
		reactivationDateEl.setValue(formatter.formatDate(reactivationDate));
		reactivationDateEl.setVisible(reactivationDate != null);
		
		Date now = new Date();
		long daysBeforeDeactivation = userLifecycleManager.getDaysUntilDeactivation(editedIdentity, now);
		daysInactivationEl.setValue(Long.toString(daysBeforeDeactivation));
		daysInactivationEl.setVisible(userModule.isUserAutomaticDeactivation()
				&& (editedIdentity.getStatus().equals(Identity.STATUS_ACTIV)
						|| editedIdentity.getStatus().equals(Identity.STATUS_PENDING)
						|| editedIdentity.getStatus().equals(Identity.STATUS_LOGIN_DENIED)));
		
		long daysBeforeDeletion = userLifecycleManager.getDaysUntilDeletion(editedIdentity, now);
		daysDeletionEl.setValue(Long.toString(daysBeforeDeletion));
		daysDeletionEl.setVisible(userModule.isUserAutomaticDeletion() && editedIdentity.getInactivationDate() != null);
	}
	
	private void setStatus(Integer status) {
		String statusStr = status.toString();
		for(String statusKey:statusKeys.keys()) {
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
		return Integer.valueOf(statusEl.getSelectedKey());
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if(rolesEls.isEmpty()) {
			allOk &= false;
		} else {
			rolesEls.get(0).clearError();
			
			int numOfRoles = 0;
			Set<String> allSelectedRoles = new HashSet<>();
			for(MultipleSelectionElement rolesEl:rolesEls) {
				Collection<String> selectedRoles = rolesEl.getSelectedKeys();
				numOfRoles += selectedRoles.size();
				allSelectedRoles.addAll(selectedRoles);
			}
			
			if(numOfRoles == 0) {
				rolesEls.get(0).setErrorKey("error.roles.atleastone", null);
				allOk &= false;
			} else if(!allSelectedRoles.contains(OrganisationRoles.invitee.name()) && !allSelectedRoles.contains(OrganisationRoles.user.name())) {
				Roles currentRoles = securityManager.getRoles(editedIdentity, false);
				List<OrganisationRef> userOrgs = currentRoles.getOrganisationsWithRole(OrganisationRoles.user)
						.stream().map(OrganisationRefImpl::new).collect(Collectors.toList());
				List<OrganisationRef> inviteeOrgs = currentRoles.getOrganisationsWithRole(OrganisationRoles.invitee)
						.stream().map(OrganisationRefImpl::new).collect(Collectors.toList());
				Set<OrganisationRef> allOrgRefs = new HashSet<>();
				allOrgRefs.addAll(userOrgs);
				allOrgRefs.addAll(inviteeOrgs);
				
				for(MultipleSelectionElement rolesEl:rolesEls) {
					RolesElement rolesElement = (RolesElement)rolesEl.getUserObject();
					allOrgRefs.remove(new OrganisationRefImpl(rolesElement.getOrganisation()));
				}
				
				if(allOrgRefs.isEmpty()) {
					rolesEls.get(0).setErrorKey("error.roles.atleastone.userorinvitee", null);
					allOk &= false;
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addToOrganisationButton == source) {
			doAddToOrganisation(ureq);
		} else if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement el = (MultipleSelectionElement)source;
			if(el.getUserObject() instanceof RolesElement) {
				((RolesElement)el.getUserObject()).checkInvitee();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddToOrganisation(UserRequest ureq) {
		if(guardModalController(selectOrganisationCtrl)) return;
		
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
		saveFormData();
		update();
	}

	/**
	 * Persist form data in database. User needs to logout / login to activate changes. A bit tricky here
	 * is that only form elements should be gettet that the user is allowed to manipulate. See also the 
	 * comments in SystemRolesAndRightsForm. 
	 * @param myIdentity
	 * @param form
	 */
	private void saveFormData() {
		boolean admin = managerRoles.isAdministrator() || managerRoles.isRolesManager();
	
		editedRoles = securityManager.getRoles(editedIdentity, false);
		
		// 1) general user type - anonymous or user
		// anonymous users
		boolean isAnonymous = editedRoles.isGuestOnly();
		if (admin) {
			isAnonymous = anonymousEl.getSelectedKey().equals("true");
		}
		
		if(isAnonymous) {
			saveAnonymousData();
		} else {
			for(MultipleSelectionElement rolesEl:rolesEls) {
				if(rolesEl.isEnabled()) {
					saveOrganisationRolesFormData((RolesElement)rolesEl.getUserObject());
				}
			}
		}
		
		if ((admin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_STATUS.booleanValue()) &&  !editedIdentity.getStatus().equals(getStatus()) ) {			
			Integer oldStatus = editedIdentity.getStatus();
			String oldStatusText = userBulkChangeManager.getStatusText(oldStatus);
			Integer newStatus = getStatus();
			String newStatusText = userBulkChangeManager.getStatusText(newStatus);
			if(!oldStatus.equals(newStatus) && Identity.STATUS_LOGIN_DENIED.equals(newStatus) && getSendLoginDeniedEmail()) {
				userBulkChangeManager.sendLoginDeniedEmail(editedIdentity);
			}
			editedIdentity = securityManager.saveIdentityStatus(editedIdentity, newStatus, getIdentity());
			logAudit("User::" + getIdentity().getKey() + " changed account status for user::" + editedIdentity.getKey() + " from::" + oldStatusText + " to::" + newStatusText);
		}
		
		if(expirationDateEl.isVisible()) {
			editedIdentity = securityManager.saveIdentityExpirationDate(editedIdentity, expirationDateEl.getDate(), getIdentity());
		}
	}
	
	private void saveAnonymousData() {
		organisationService.setAsGuest(editedIdentity);
		dbInstance.commit();
		organisations = organisationService.getOrganisations(editedIdentity, OrganisationRoles.values());
		updateRoles();
	}
	
	private void saveOrganisationRolesFormData(RolesElement wrapper) {
		Organisation organisation = wrapper.getOrganisation();
		boolean iAmUserManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.usermanager);
		boolean iAmRolesManager = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.rolesmanager);
		boolean iAmAdmin = managerRoles.hasRoleInParentLine(organisation, OrganisationRoles.administrator)
				|| managerRoles.isSystemAdmin();

		// 2) system roles
		List<OrganisationRoles> rolesToAdd = new ArrayList<>();
		List<OrganisationRoles> rolesToRemove = new ArrayList<>();
		wrapper.commit(OrganisationRoles.invitee, rolesToAdd, rolesToRemove);
		wrapper.commit(OrganisationRoles.user, rolesToAdd, rolesToRemove);

		// author
		if (iAmAdmin || iAmUserManager) {
			wrapper.commit(OrganisationRoles.author, rolesToAdd, rolesToRemove);
		}
		
		// managers
		if (iAmAdmin || iAmRolesManager) {
			wrapper.commit(OrganisationRoles.groupmanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.poolmanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.curriculummanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.linemanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.qualitymanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.lecturemanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.usermanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.rolesmanager, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.learnresourcemanager, rolesToAdd, rolesToRemove);
		}

		// administration roles, only allowed by administrator
		if (iAmAdmin) {
			wrapper.commit(OrganisationRoles.principal, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.administrator, rolesToAdd, rolesToRemove);
			wrapper.commit(OrganisationRoles.sysadmin, rolesToAdd, rolesToRemove);
		}

		RolesByOrganisation editedOrganisationRoles = editedRoles.getRoles(wrapper.getOrganisation());
		if(editedOrganisationRoles == null) {
			editedOrganisationRoles = new RolesByOrganisation(wrapper.getOrganisation(), OrganisationRoles.EMPTY_ROLES);
		}
		RolesByOrganisation updatedRoles = RolesByOrganisation.enhance(editedOrganisationRoles, rolesToAdd, rolesToRemove);
		securityManager.updateRoles(getIdentity(), editedIdentity, updatedRoles);
	}
	
	private class RolesElement {
		
		private final List<String> roleKeys;
		private final Organisation organisation;
		private final MultipleSelectionElement rolesEl;
		private List<String> selectedRoles = new ArrayList<>();
		
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
		
		public void commit(OrganisationRoles k, List<OrganisationRoles> rolesToAdd, List<OrganisationRoles> rolesToRemove) {
			if(roleKeys.contains(k.name())) {
				if(getRole(k)) {
					rolesToAdd.add(k);
				} else {
					rolesToRemove.add(k);
				}
			}
		}
		
		private boolean getRole(OrganisationRoles k) {
			return roleKeys.contains(k.name()) && rolesEl.getSelectedKeys().contains(k.name());
		}
		
		private void setRole(OrganisationRoles k, boolean enabled) {
			if(roleKeys.contains(k.name()) && enabled) {
				rolesEl.select(k.name(), enabled);
			}
		}
		
		public void checkInvitee() {
			Collection<String> keys = rolesEl.getSelectedKeys();
			if(selectedRoles.contains(OrganisationRoles.invitee.name())) {
				if(keys.contains(OrganisationRoles.invitee.name()) && keys.size() > 1)  {
					rolesEl.uncheckAll();
					for(String role:keys) {
						if(!OrganisationRoles.invitee.name().equals(role)) {
							rolesEl.select(role, true);
						}
					}
				}
			} else if(keys.contains(OrganisationRoles.invitee.name())) {
				rolesEl.uncheckAll();
				rolesEl.select(OrganisationRoles.invitee.name(), true);
			}
			saveSelectedRoles();
		}

		public void saveSelectedRoles() {
			selectedRoles = new ArrayList<>(rolesEl.getSelectedKeys());
		}
	}
}
