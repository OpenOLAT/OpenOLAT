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
package org.olat.user.ui.admin;

import java.util.Date;

import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
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
public class UserAccountController extends FormBasicController {

	private StaticTextElement userTypeEl;
	private StaticTextElement lastLoginEl;
	private StaticTextElement creationDateEl;
	private StaticTextElement inactivationDateEl;
	private StaticTextElement reactivationDateEl;
	private StaticTextElement daysInactivationEl;
	private StaticTextElement daysDeletionEl;
	
	private FormLink inviteeToUserButton;
	
	private SingleSelection statusEl;
	private DateChooser expirationDateEl;
	private MultipleSelectionElement sendLoginDeniedEmailEl;

	private SelectionValues statusKeys;
	
	private CloseableModalController cmc;
	private ConfirmToRegisteredUserController confirmConversionCtrl;
	
	/**
	 * The roles without inheritance
	 */
	private Roles editedRoles;
	private Identity editedIdentity;
	private final Roles managerRoles;

	@Autowired
	private UserModule userModule;
	@Autowired
	private BaseSecurity securityManager;
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
	public UserAccountController(WindowControl wControl, UserRequest ureq, Identity identity) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UserAdminController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		this.editedIdentity = identity;
		
		editedRoles = securityManager.getRoles(editedIdentity, false);
		editedRoles.getOrganisations();

		managerRoles = ureq.getUserSession().getRoles();
		
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
	
		initUserTypeForm(formLayout);
		initFormStatus(formLayout, iAmAdmin, iAmUserManager);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
	}
	
	private void initUserTypeForm(FormItemContainer formLayout) {
		userTypeEl = uifactory.addStaticTextElement("rightsForm.user.type", "", formLayout);
		inviteeToUserButton = uifactory.addFormLink("rightsForm.invitee.to.user", formLayout, Link.BUTTON);
		
		uifactory.addSpacerElement("syssep", formLayout, false);
	}

	private void initFormStatus(FormItemContainer formLayout, boolean iAmAdmin, boolean iAmUserManager) {
		creationDateEl = uifactory.addStaticTextElement("rightsForm.creation.date", "", formLayout);
		lastLoginEl = uifactory.addStaticTextElement("rightsForm.last.login", "", formLayout);
		uifactory.addSpacerElement("datesep", formLayout, false);
		
		// status
		statusEl = uifactory.addRadiosVertical("status", "rightsForm.status", formLayout, statusKeys.keys(), statusKeys.values());
		statusEl.addActionListener(FormEvent.ONCHANGE);
		sendLoginDeniedEmailEl = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", formLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailEl.setLabel(null, null);
		
		statusEl.setVisible(iAmAdmin || iAmUserManager);
		sendLoginDeniedEmailEl.setVisible(false);
		
		// life cycle information
		expirationDateEl = uifactory.addDateChooser("rightsForm.expiration.date", null, formLayout);
		inactivationDateEl = uifactory.addStaticTextElement("rightsForm.inactivation.date", "", formLayout);
		reactivationDateEl = uifactory.addStaticTextElement("rightsForm.reactivation.date", "", formLayout);
		daysInactivationEl = uifactory.addStaticTextElement("rightsForm.days.inactivation", "", formLayout);
		daysDeletionEl = uifactory.addStaticTextElement("rightsForm.days.deletion", "", formLayout);
	}
	
	private void update() {
		editedRoles = securityManager.getRoles(editedIdentity, false);
		
		String userTypeI18n;
		if(editedRoles.isInvitee()) {
			userTypeI18n = "user.type.invitee";
		} else if(editedRoles.isGuestOnly()) {
			userTypeI18n = "user.type.guest";
		} else {
			userTypeI18n = "user.type.user";
		}
		userTypeEl.setValue(translate(userTypeI18n));
		inviteeToUserButton.setVisible(editedRoles.isInvitee());
		
		if(editedRoles.hasRole(OrganisationRoles.administrator)
				|| editedRoles.hasRole(OrganisationRoles.sysadmin)
				|| editedRoles.hasRole(OrganisationRoles.rolesmanager)) {
			statusEl.setEnabled(false);
		}

		setStatus(editedIdentity.getStatus());

		expirationDateEl.setDate(editedIdentity.getExpirationDate());
		expirationDateEl.setVisible(!editedRoles.isSystemAdmin() && !editedRoles.isAdministrator());
		
		Formatter formatter = Formatter.getInstance(getLocale());
		String lastLogin = formatter.formatDateAndTime(editedIdentity.getLastLogin());
		lastLoginEl.setValue(lastLogin == null ? "" : lastLogin);
		String creationDate = formatter.formatDateAndTime(editedIdentity.getCreationDate());
		creationDateEl.setValue(creationDate);
		
		Date inactivationDate = editedIdentity.getInactivationDate();
		inactivationDateEl.setValue(formatter.formatDate(inactivationDate));
		inactivationDateEl.setVisible(inactivationDate != null);
		
		Date reactivationDate = editedIdentity.getReactivationDate();
		reactivationDateEl.setValue(formatter.formatDate(reactivationDate));
		reactivationDateEl.setVisible(reactivationDate != null);

		daysInactivationEl.setVisible(userModule.isUserAutomaticDeactivation()
				&& (editedIdentity.getStatus().equals(Identity.STATUS_ACTIV)
						|| editedIdentity.getStatus().equals(Identity.STATUS_PENDING)
						|| editedIdentity.getStatus().equals(Identity.STATUS_LOGIN_DENIED)));
		daysDeletionEl.setVisible(userModule.isUserAutomaticDeletion() && editedIdentity.getInactivationDate() != null);
		
		if(!editedRoles.isGuestOnly() || inactivationDate != null || reactivationDate != null
				|| editedIdentity.getDeletionEmailDate() != null || editedIdentity.getExpirationDate() != null) {
			Date now = new Date();
			long daysBeforeDeactivation = userLifecycleManager.getDaysUntilDeactivation(editedIdentity, now);
			daysInactivationEl.setValue(Long.toString(daysBeforeDeactivation));

			long daysBeforeDeletion = userLifecycleManager.getDaysUntilDeletion(editedIdentity, now);
			daysDeletionEl.setValue(Long.toString(daysBeforeDeletion));
		}
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

	private Integer getStatus() {
		return Integer.valueOf(statusEl.getSelectedKey());
	}
	
	public boolean getSendLoginDeniedEmail() {
		return sendLoginDeniedEmailEl.isSelected(0);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		// nothing to do
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmConversionCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, new ReloadIdentityEvent());
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmConversionCtrl);
		removeAsListenerAndDispose(cmc);
		confirmConversionCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(inviteeToUserButton == source) {
			doConversionToUser(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveFormData();
		update();
		fireEvent(ureq, Event.CHANGED_EVENT);
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
		
		if ((admin || BaseSecurityModule.USERMANAGER_CAN_MANAGE_STATUS.booleanValue()) && !editedIdentity.getStatus().equals(getStatus())) {			
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
	
	private void doConversionToUser(UserRequest ureq) {
		if(guardModalController(confirmConversionCtrl)) return;
		
		// start edit workflow in dedicated quota edit controller
		removeAsListenerAndDispose(confirmConversionCtrl);
		confirmConversionCtrl = new ConfirmToRegisteredUserController(ureq, getWindowControl(), editedIdentity);
		listenTo(confirmConversionCtrl);
		
		String title = translate("convert.invitee.to.user.title");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmConversionCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
