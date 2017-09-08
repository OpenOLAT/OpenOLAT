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

import java.util.HashMap;
import java.util.Map;

import org.olat.admin.user.SystemRolesAndRightsController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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

	boolean canCreateOLATPassword;
	static final String usageIdentifyer = UserBulkChangeStep01.class.getCanonicalName();
	TextElement textAreaElement;

	public UserBulkChangeStep01(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("step1.description", null);
		setNextStep(new UserBulkChangeStep01a(ureq));
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getInitialPrevNextFinishConfig()
	 */
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext,
	 *      org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new UserBulkChangeStepForm01(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}

	private final class UserBulkChangeStepForm01 extends StepFormBasicController {

		private FormLayoutContainer textContainer;

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
		
		private Identity identity;
		
		@Autowired
		private UserManager userManager;
		@Autowired
		private BaseSecurity securityManager;

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
			Boolean validChange = (Boolean) getFromRunContext("validChange");
			Map<String, String> roleChangeMap = new HashMap<>();

			if (chkUserManager!=null && chkUserManager.getSelectedKeys().contains("Usermanager")) {
				roleChangeMap.put(Constants.GROUP_USERMANAGERS, setUserManager.getSelectedKey());
				validChange = true;
			}

			if (chkGroupManager!=null && chkGroupManager.getSelectedKeys().contains("Groupmanager")) {
				roleChangeMap.put(Constants.GROUP_GROUPMANAGERS, setGroupManager.getSelectedKey());
				validChange = true;
			}

			if (chkAuthor!=null && chkAuthor.getSelectedKeys().contains("Author")) {
				roleChangeMap.put(Constants.GROUP_AUTHORS, setAuthor.getSelectedKey());
				validChange = true;
			}
			
			if (chkPoolManager!=null && chkPoolManager.getSelectedKeys().contains("PoolManager")) {
				roleChangeMap.put(Constants.GROUP_POOL_MANAGER, setPoolManager.getSelectedKey());
				validChange = true;
			}
			
			if (chkInstitutionManager!=null && chkInstitutionManager.getSelectedKeys().contains("InstitutionManager")) {
				roleChangeMap.put(Constants.GROUP_INST_ORES_MANAGER, setInstitutionManager.getSelectedKey());
				validChange = true;
			}

			if (chkAdmin!=null && chkAdmin.getSelectedKeys().contains("Admin")) {
				roleChangeMap.put(Constants.GROUP_ADMIN, setAdmin.getSelectedKey());
				validChange = true;
			}

			if (chkStatus!=null && chkStatus.getSelectedKeys().contains("Status")) {
				roleChangeMap.put("Status", setStatus.getSelectedKey());
				// also check dependent send-email checkbox
				if (sendLoginDeniedEmail!=null) {
					roleChangeMap.put("sendLoginDeniedEmail", Boolean.toString(sendLoginDeniedEmail.isSelected(0)));					
				}
				validChange = true;
			}

			addToRunContext("roleChangeMap", roleChangeMap);
			addToRunContext("validChange", validChange);

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

			textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step1.html");
			formLayout.add(textContainer);
			String[] addremove = new String[] { "add", "remove" };
			String[] addremoveTranslated = new String[] { translate("role.add"), translate("role.remove") };

			// Main layout is a vertical layout without left side padding. To format
			// the checkboxes properly we need a default layout for the remaining form
			// elements
			FormItemContainer innerFormLayout = FormLayoutContainer.createDefaultFormLayout("innerFormLayout", getTranslator());
			formLayout.add(innerFormLayout);

			// check user rights:
			boolean iAmOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
			identity = ureq.getIdentity();
			
			// get user system roles groups from security manager
			SecurityGroup adminGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
			boolean isAdmin = securityManager.isIdentityInSecurityGroup(identity, adminGroup);
			SecurityGroup userManagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
			boolean isUserManager = securityManager.isIdentityInSecurityGroup(identity, userManagerGroup);
			SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
			boolean isAuthor = securityManager.isIdentityInSecurityGroup(identity, authorGroup);
			SecurityGroup groupmanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
			boolean isGroupManager = securityManager.isIdentityInSecurityGroup(identity, groupmanagerGroup);
			SecurityGroup poolManagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_POOL_MANAGER);
			boolean isPoolManager = securityManager.isIdentityInSecurityGroup(identity, poolManagerGroup);
			SecurityGroup insitutionManagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
			boolean isInstitutionManager = securityManager.isIdentityInSecurityGroup(identity, insitutionManagerGroup);

			// usermanager:
			if (isAdmin || isUserManager || iAmOlatAdmin) {
				chkUserManager = uifactory.addCheckboxesHorizontal("Usermanager", "table.role.useradmin", innerFormLayout, new String[] { "Usermanager" }, new String[] { "" });
				chkUserManager.select("Usermanager", false);
				chkUserManager.addActionListener(FormEvent.ONCLICK);

				setUserManager = uifactory.addDropdownSingleselect("setUserManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setUserManager.setVisible(false);
			}

			// groupmanager
			if (isAdmin || isGroupManager || iAmOlatAdmin) {
				chkGroupManager = uifactory.addCheckboxesHorizontal("Groupmanager", "table.role.groupadmin", innerFormLayout, new String[] { "Groupmanager" }, new String[] { "" });
				chkGroupManager.select("Groupmanager", false);
				chkGroupManager.addActionListener(FormEvent.ONCLICK);

				setGroupManager = uifactory.addDropdownSingleselect("setGroupManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setGroupManager.setVisible(false);
			}

			// author
			if (isAdmin || isAuthor || iAmOlatAdmin) {
				chkAuthor = uifactory.addCheckboxesHorizontal("Author", "table.role.author", innerFormLayout, new String[] { "Author" }, new String[] { "" });
				chkAuthor.select("Author", false);
				chkAuthor.addActionListener(FormEvent.ONCLICK);

				setAuthor = uifactory.addDropdownSingleselect("setAuthor", null, innerFormLayout, addremove, addremoveTranslated, null);
				setAuthor.setVisible(false);
			}
			
			//pool manager
			if (isAdmin || isPoolManager || iAmOlatAdmin) {
				chkPoolManager = uifactory.addCheckboxesHorizontal("PoolManager", "table.role.poolManager", innerFormLayout, new String[] { "PoolManager" }, new String[] { "" });
				chkPoolManager.select("Author", false);
				chkPoolManager.addActionListener(FormEvent.ONCLICK);

				setPoolManager = uifactory.addDropdownSingleselect("setPoolManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setPoolManager.setVisible(false);
			}
			
			//
			if (isAdmin || isInstitutionManager || iAmOlatAdmin) {
				chkInstitutionManager = uifactory.addCheckboxesHorizontal("InsitutionManager", "table.role.institutionManager", innerFormLayout, new String[] { "InstitutionManager" }, new String[] { "" });
				chkInstitutionManager.select("Author", false);
				chkInstitutionManager.addActionListener(FormEvent.ONCLICK);

				setInstitutionManager = uifactory.addDropdownSingleselect("setInstitutionManager", null, innerFormLayout, addremove, addremoveTranslated, null);
				setInstitutionManager.setVisible(false);
			}
			
			// sysadmin
			if (isAdmin || iAmOlatAdmin) {
				chkAdmin = uifactory.addCheckboxesHorizontal("Admin", "table.role.admin", innerFormLayout, new String[] { "Admin" }, new String[] { "" });
				chkAdmin.select("Admin", false);
				chkAdmin.addActionListener(FormEvent.ONCLICK);

				setAdmin = uifactory.addDropdownSingleselect("setAdmin",null, innerFormLayout, addremove, addremoveTranslated, null);
				setAdmin.setVisible(false);
			}

			// status
			if (isAdmin || iAmOlatAdmin) {
				chkStatus = uifactory.addCheckboxesHorizontal("Status", "table.role.status", innerFormLayout, new String[] { "Status" }, new String[] { "" });
				chkStatus.select("Status", false);
				chkStatus.addActionListener(FormEvent.ONCLICK);

				// TODO: RH: pay attention: if status changes in Identity-statics this
				// may lead to missing status
				// implement methods in SystemRolesAndRightsController.java
				setTranslator(Util.createPackageTranslator(SystemRolesAndRightsController.class, getLocale()));
				String[] statusKeys = { Integer.toString(Identity.STATUS_ACTIV), Integer.toString(Identity.STATUS_PERMANENT),
						Integer.toString(Identity.STATUS_LOGIN_DENIED) };
				String[] statusValues = { translate("rightsForm.status.activ"), translate("rightsForm.status.permanent"),
						translate("rightsForm.status.login_denied") };

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
