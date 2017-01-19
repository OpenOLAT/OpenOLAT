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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date:  Jan 27, 2006
 * @author gnaegi
 * @author matthai
 * <pre>
 * Description:
 * Form to configure the users system roles and the basic type 
 * of the user account (user or guest).
 */

public class SystemRolesAndRightsForm extends FormBasicController {
	
	private SingleSelection AnonymousRE;
	private SelectionElement RolesSE;
	private SpacerElement rolesSep;
	private SpacerElement sysSep;
	private SingleSelection statusRE;
	private MultipleSelectionElement sendLoginDeniedEmailCB;
	
	private Identity identity;
	private final boolean iAmOlatAdmin;
	private final boolean isAdmin, isUserManager, isAuthor, isGroupManager, isPoolManager, isGuestOnly, isInstitutionalResourceManager;
	private final boolean canGuestsByConfig, canAuthorsByConfig, canGroupmanagersByConfig, canPoolmanagersByConfig, canInstitutionalResourceManagerByConfig, canStatus;

	private List<String> statusKeys, statusValues;
	private List<String> roleKeys, roleValues;
	
	private static final String KUSER   = "isUserManager";
	private static final String KGROUP  = "isGroupManager";
	private static final String KPOOL   = "isPoolManager";
	private static final String KAUTHOR = "isAuthor";
	private static final String KADMIN  = "isAdmin";
	private static final String KRESMAN = "isInstitutionalResourcemanager";
	
	@Autowired
	private BaseSecurity secMgr;
	
	public SystemRolesAndRightsForm(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl);
		
		this.identity = identity;
		iAmOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();

		// get user system roles groups from security manager
		SecurityGroup adminGroup = secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN);
		isAdmin = secMgr.isIdentityInSecurityGroup(identity, adminGroup);
		
		SecurityGroup userManagerGroup = secMgr.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
		isUserManager = secMgr.isIdentityInSecurityGroup(identity, userManagerGroup);

		SecurityGroup authorGroup = secMgr.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		isAuthor = secMgr.isIdentityInSecurityGroup(identity, authorGroup);
	
		SecurityGroup groupmanagerGroup = secMgr.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
		isGroupManager = secMgr.isIdentityInSecurityGroup(identity, groupmanagerGroup);
		
		SecurityGroup poolmanagerGroup = secMgr.findSecurityGroupByName(Constants.GROUP_POOL_MANAGER);
		isPoolManager = secMgr.isIdentityInSecurityGroup(identity, poolmanagerGroup);

		SecurityGroup isAnonymous = secMgr.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
		isGuestOnly = secMgr.isIdentityInSecurityGroup(identity, isAnonymous);
		
		SecurityGroup institutionalResourceManager = secMgr.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
		isInstitutionalResourceManager = secMgr.isIdentityInSecurityGroup(identity, institutionalResourceManager);
		
		statusKeys = new ArrayList<>(4);
		statusKeys.add(Integer.toString(Identity.STATUS_ACTIV));
		statusKeys.add(Integer.toString(Identity.STATUS_PERMANENT));
		statusKeys.add(Integer.toString(Identity.STATUS_LOGIN_DENIED));
		
		
		statusValues = new ArrayList<>(4);
		statusValues.add(translate("rightsForm.status.activ"));
		statusValues.add(translate("rightsForm.status.permanent"));
		statusValues.add(translate("rightsForm.status.login_denied"));
		
		if (identity.getStatus() == Identity.STATUS_DELETED) {
			statusKeys.add(Integer.toString(Identity.STATUS_DELETED));
			statusValues.add(translate("rightsForm.status.deleted"));
		}
		
		roleKeys = new ArrayList<>();
		roleValues = new ArrayList<>();

		canGuestsByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;	
		canAuthorsByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS;

		canPoolmanagersByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_POOLMANAGERS;		
		canGroupmanagersByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
		canInstitutionalResourceManagerByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER;
		canStatus = BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER;

		if (iAmOlatAdmin) {
			roleKeys.add(KUSER);
			roleValues.add(translate("rightsForm.isUsermanager"));
		}
		
		if (iAmOlatAdmin || canGroupmanagersByConfig) {
			roleKeys.add(KGROUP);
			roleValues.add(translate("rightsForm.isGroupmanager"));
		}

		if (iAmOlatAdmin || canPoolmanagersByConfig) {
			roleKeys.add(KPOOL);
			roleValues.add(translate("rightsForm.isPoolmanager"));
		}

		if (iAmOlatAdmin || canAuthorsByConfig) {
			roleKeys.add(KAUTHOR);
			roleValues.add(translate("rightsForm.isAuthor"));
		}

		if (iAmOlatAdmin) {
			roleKeys.add(KADMIN);
			roleValues.add(translate("rightsForm.isAdmin"));
		}

		if (iAmOlatAdmin || canInstitutionalResourceManagerByConfig) {
			roleKeys.add(KRESMAN);
			String iname = identity.getUser().getProperty("institutionalName", null);
			roleValues.add(
				iname != null
				? translate("rightsForm.isInstitutionalResourceManager.institution",iname)
				: translate("rightsForm.isInstitutionalResourceManager")
			);
		}

		initForm(ureq);
	}

	private void update () {
		setAnonymous(isGuestOnly);
		
		setUsermanager(isUserManager);
		setGroupmanager(isGroupManager);
		setAuthor(isAuthor);
		setAdmin(isAdmin);
		setInstitutionalResourceManager(isInstitutionalResourceManager);
		setPoolmanager(isPoolManager);
		
		setStatus(identity.getStatus());
		
		RolesSE.setVisible(!isAnonymous());
		rolesSep.setVisible(!isAnonymous());
	}
	
	protected boolean isAdmin() {
		return getRole(KADMIN);
	}

	public void setAdmin(boolean isAdmin) {
		setRole (KADMIN, isAdmin);
	}

	public boolean isAnonymous() {
		return AnonymousRE.getSelectedKey().equals("true");
	}

	private void setAnonymous(boolean isAnonymous) {
			AnonymousRE.select(isAnonymous ? "true":"false", true);
	}

	protected boolean isAuthor() {
		return getRole(KAUTHOR);
	}

	protected void setAuthor(boolean isAuthor) {
		setRole(KAUTHOR, isAuthor);
	}

	protected boolean isGroupmanager() {
		return getRole(KGROUP);
	}

	private void setGroupmanager(boolean isGroupmanager) {
		setRole(KGROUP, isGroupmanager);
	}
	
	protected boolean isPoolmanager() {
		return getRole(KPOOL);
	}

	private void setPoolmanager(boolean isPoolmanager) {
		setRole(KPOOL, isPoolmanager);
	}

	protected boolean isUsermanager() {
		return getRole(KUSER);
	}

	private void setUsermanager(boolean isUsermanager) {
		setRole(KUSER, isUsermanager);
	}
	
	private void setInstitutionalResourceManager(boolean isInstitutionalResourceManager) {
		setRole(KRESMAN, isInstitutionalResourceManager);
	}
	
	public boolean isInstitutionalResourceManager() {
		return getRole(KRESMAN);
	}
	
	protected Integer getStatus() {
		return new Integer(statusRE.getSelectedKey());
	}
	
	private void setStatus(Integer status) {
		String statusStr = status.toString();
		for(String statusKey:statusKeys) {
			if(statusStr.equals(statusKey)) {
				statusRE.select(statusKey, true);
			}
		}
		statusRE.setEnabled(!Identity.STATUS_DELETED.equals(status));
	}

	public boolean getSendLoginDeniedEmail() {
		return sendLoginDeniedEmailCB.isSelected(0);
	}
	
	private void setRole (String k, boolean tf) {
		if (roleKeys.contains(k)) RolesSE.select(k, tf); 
	}
	
	private boolean getRole (String k) {
		return roleKeys.contains(k) ? RolesSE.isSelected(roleKeys.indexOf(k)) : false;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == AnonymousRE) {
			RolesSE.setVisible(!isAnonymous());
			rolesSep.setVisible(!isAnonymous());
		} else if (source == statusRE && (iAmOlatAdmin || canStatus)) {
			sendLoginDeniedEmailCB.setVisible(statusRE.isSelected(2));
		}
		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		AnonymousRE = uifactory.addRadiosVertical(
				"anonymous", "rightsForm.isAnonymous", formLayout, 
				new String[]{"true", "false"},
				new String[]{translate("rightsForm.isAnonymous.true"), translate("rightsForm.isAnonymous.false")}
		);
		sysSep = uifactory.addSpacerElement("syssep", formLayout, false);
		if (iAmOlatAdmin || canGuestsByConfig) {
			AnonymousRE.addActionListener(FormEvent.ONCLICK);
		} else {
			AnonymousRE.setVisible(false);
			sysSep.setVisible(false);
		}
		
		RolesSE = uifactory.addCheckboxesVertical(
				"roles", "rightsForm.roles", formLayout,
				roleKeys.toArray(new String[roleKeys.size()]),
				roleValues.toArray(new String[roleValues.size()]), 1);
		rolesSep = uifactory.addSpacerElement("rolesSep", formLayout, false);
		
		statusRE = uifactory.addRadiosVertical(
				"status", "rightsForm.status", formLayout,
				statusKeys.toArray(new String[statusKeys.size()]),
				statusValues.toArray(new String[statusKeys.size()])
		);
		statusRE.addActionListener(FormEvent.ONCHANGE);
		sendLoginDeniedEmailCB = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", formLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
		sendLoginDeniedEmailCB.setLabel(null, null);
		
		rolesSep.setVisible(iAmOlatAdmin);
		statusRE.setVisible(iAmOlatAdmin || canStatus);
		if(isAdmin) {
			statusRE.setEnabled(false);
		}
		sendLoginDeniedEmailCB.setVisible(false);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		
		update();
	}

	@Override
	protected void doDispose() {
		//
	}

}
