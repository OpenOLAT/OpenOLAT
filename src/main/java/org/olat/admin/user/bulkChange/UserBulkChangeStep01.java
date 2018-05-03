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
package org.olat.admin.user.bulkChange;

import java.util.Map;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * first step: select attributes, which should be changed
 * 
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class UserBulkChangeStep01 extends BasicStep {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final UserBulkChanges userBulkChanges;

	public UserBulkChangeStep01(UserRequest ureq, UserBulkChanges userBulkChanges) {
		super(ureq);
		this.userBulkChanges = userBulkChanges;
		setI18nTitleAndDescr("step1.description", null);
		setNextStep(new UserBulkChangeStep01a(ureq, userBulkChanges));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new UserBulkChangeStepForm01(ureq, windowControl, form, stepsRunContext);
	}

	private final class UserBulkChangeStepForm01 extends StepFormBasicController {

		private MultipleSelectionElement chkAuthor;
		private SingleSelection setAuthor;
		private MultipleSelectionElement chkUserManager;
		private SingleSelection setUserManager;
		private MultipleSelectionElement chkGroupManager;
		private SingleSelection setGroupManager;
		private MultipleSelectionElement chkPoolManager;
		private SingleSelection setPoolManager;
		private MultipleSelectionElement chkInstitutionManager;
		private SingleSelection setInstitutionManager;
		private MultipleSelectionElement chkAdmin;
		private SingleSelection setAdmin;
		private MultipleSelectionElement chkStatus;
		private SingleSelection setStatus;
		private MultipleSelectionElement sendLoginDeniedEmail;
		
		@Autowired
		private UserManager userManager;

		public UserBulkChangeStepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			//nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {
			boolean validChange = userBulkChanges.isValidChange();
			Map<OrganisationRoles, String> roleChangeMap = userBulkChanges.getRoleChangeMap();
			if (chkUserManager != null && chkUserManager.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.usermanager, setUserManager.getSelectedKey());
				validChange = true;
			}

			if (chkGroupManager!=null && chkGroupManager.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.groupmanager, setGroupManager.getSelectedKey());
				validChange = true;
			}

			if (chkAuthor!=null && chkAuthor.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.author, setAuthor.getSelectedKey());
				validChange = true;
			}
			
			if (chkPoolManager!=null && chkPoolManager.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.poolmanager, setPoolManager.getSelectedKey());
				validChange = true;
			}
			
			if (chkInstitutionManager!=null && chkInstitutionManager.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.learnresourcemanager, setInstitutionManager.getSelectedKey());
				validChange = true;
			}

			if (chkAdmin!=null && chkAdmin.isAtLeastSelected(1)) {
				roleChangeMap.put(OrganisationRoles.administrator, setAdmin.getSelectedKey());
				validChange = true;
			}

			if (chkStatus!=null && chkStatus.isAtLeastSelected(1)) {
				userBulkChanges.setStatus(Integer.parseInt(setStatus.getSelectedKey()));
				// also check dependent send-email checkbox
				if (sendLoginDeniedEmail != null) {
					userBulkChanges.setSendLoginDeniedEmail(sendLoginDeniedEmail.isSelected(0));				
				}
				validChange = true;
			}
			
			userBulkChanges.setValidChange(validChange);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(chkUserManager == source) {
				setUserManager.setVisible(chkUserManager.isAtLeastSelected(1));
			} else if(chkGroupManager == source) {
				setGroupManager.setVisible(chkGroupManager.isAtLeastSelected(1));
			} else if(chkAuthor == source) {
				setAuthor.setVisible(chkAuthor.isAtLeastSelected(1));
			} else if(chkPoolManager == source) {
				setPoolManager.setVisible(chkPoolManager.isAtLeastSelected(1));
			} else if(chkInstitutionManager == source) {
				setInstitutionManager.setVisible(chkInstitutionManager.isAtLeastSelected(1));
			} else if(chkAdmin == source) {
				setAdmin.setVisible(chkAdmin.isAtLeastSelected(1));
			} else if(chkStatus == source || setStatus == source) {
				setStatus.setVisible(chkStatus.isAtLeastSelected(1));
				boolean loginDenied = chkStatus.isAtLeastSelected(1) && setStatus.isOneSelected()
						&& Integer.toString(Identity.STATUS_LOGIN_DENIED).equals(setStatus.getSelectedKey());
				sendLoginDeniedEmail.setVisible(loginDenied);
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			// always true, because no changes are required
			return true;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("step1.title");

			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step1.html");
			formLayout.add(textContainer);
			String[] addremove = new String[] { "add", "remove" };
			String[] addremoveTranslated = new String[] { translate("role.add"), translate("role.remove") };

			// Main layout is a vertical layout without left side padding. To format
			// the checkboxes properly we need a default layout for the remaining form
			// elements
			FormItemContainer innerFormLayout = FormLayoutContainer.createDefaultFormLayout("innerFormLayout", getTranslator());
			formLayout.add(innerFormLayout);

			// check user rights:
			Roles roles = ureq.getUserSession().getRoles();
			// usermanager:
			if (roles.isOLATAdmin() || roles.isUserManager()) {
				chkUserManager = uifactory.addCheckboxesHorizontal("Usermanager", "table.role.useradmin", innerFormLayout, onKeys, onValues);
				chkUserManager.select("Usermanager", false);
				chkUserManager.addActionListener(FormEvent.ONCLICK);

				setUserManager = uifactory.addDropdownSingleselect("setUserManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setUserManager.setVisible(false);
			}

			// groupmanager
			if (roles.isOLATAdmin() || roles.isGroupManager()) {
				chkGroupManager = uifactory.addCheckboxesHorizontal("Groupmanager", "table.role.groupadmin", innerFormLayout, onKeys, onValues);
				chkGroupManager.select("Groupmanager", false);
				chkGroupManager.addActionListener(FormEvent.ONCLICK);

				setGroupManager = uifactory.addDropdownSingleselect("setGroupManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setGroupManager.setVisible(false);
			}

			// author
			if (roles.isOLATAdmin() || roles.isAuthor()) {
				chkAuthor = uifactory.addCheckboxesHorizontal("Author", "table.role.author", innerFormLayout, onKeys, onValues);
				chkAuthor.select("Author", false);
				chkAuthor.addActionListener(FormEvent.ONCLICK);

				setAuthor = uifactory.addDropdownSingleselect("setAuthor", null, innerFormLayout, addremove, addremoveTranslated, null);
				setAuthor.setVisible(false);
			}
			
			//pool manager
			if (roles.isOLATAdmin() || roles.isPoolAdmin()) {
				chkPoolManager = uifactory.addCheckboxesHorizontal("PoolManager", "table.role.poolManager", innerFormLayout, onKeys, onValues);
				chkPoolManager.select("Author", false);
				chkPoolManager.addActionListener(FormEvent.ONCLICK);

				setPoolManager = uifactory.addDropdownSingleselect("setPoolManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setPoolManager.setVisible(false);
			}
			
			// learn resource manager
			if (roles.isOLATAdmin() || roles.isLearnResourceManager()) {
				chkInstitutionManager = uifactory.addCheckboxesHorizontal("InsitutionManager", "table.role.institutionManager", innerFormLayout, onKeys, onValues);
				chkInstitutionManager.select("Author", false);
				chkInstitutionManager.addActionListener(FormEvent.ONCLICK);

				setInstitutionManager = uifactory.addDropdownSingleselect("setInstitutionManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setInstitutionManager.setVisible(false);
			}
			
			// sysadmin
			if (roles.isOLATAdmin()) {
				chkAdmin = uifactory.addCheckboxesHorizontal("Admin", "table.role.admin", innerFormLayout, onKeys, onValues);
				chkAdmin.select("Admin", false);
				chkAdmin.addActionListener(FormEvent.ONCLICK);

				setAdmin = uifactory.addDropdownSingleselect("setAdmin",null, innerFormLayout, addremove, addremoveTranslated, null);
				setAdmin.setVisible(false);
			}

			// status
			if (roles.isOLATAdmin()) {
				chkStatus = uifactory.addCheckboxesHorizontal("Status", "table.role.status", innerFormLayout, onKeys, onValues);
				chkStatus.select("Status", false);
				chkStatus.addActionListener(FormEvent.ONCLICK);

				// Pay attention: if status changes in Identity-statics this
				// may lead to missing status
				// implement methods in SystemRolesAndRightsController.java
				setTranslator(Util.createPackageTranslator(SystemRolesAndRightsController.class, getLocale()));
				String[] statusKeys = {
						Integer.toString(Identity.STATUS_ACTIV),
						Integer.toString(Identity.STATUS_PERMANENT),
						Integer.toString(Identity.STATUS_LOGIN_DENIED)
					};
				String[] statusValues = {
						translate("rightsForm.status.activ"),
						translate("rightsForm.status.permanent"),
						translate("rightsForm.status.login_denied")
					};

				setStatus = uifactory.addDropdownSingleselect("setStatus",null, innerFormLayout, statusKeys, statusValues, null);
				setStatus.setVisible(false);
				setStatus.addActionListener(FormEvent.ONCHANGE);

				sendLoginDeniedEmail = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", innerFormLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
				sendLoginDeniedEmail.setLabel(null, null);
				sendLoginDeniedEmail.setVisible(false);
			}
		}
	}
}
