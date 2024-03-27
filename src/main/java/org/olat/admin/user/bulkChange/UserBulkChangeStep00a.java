/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.admin.user.bulkChange;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.UserAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserBulkChangeStep00a extends BasicStep {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final UserBulkChanges userBulkChanges;
	
	public UserBulkChangeStep00a(UserRequest ureq, UserBulkChanges userBulkChanges) {
		super(ureq);
		this.userBulkChanges = userBulkChanges;
		setI18nTitleAndDescr("step0a.description", null);
		setNextStep(new UserBulkChangeStep01(ureq, userBulkChanges));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new UserBulkChangeStepForm01b(ureq, wControl, form, runContext);
	}

	private final class UserBulkChangeStepForm01b extends StepFormBasicController {

		private SingleSelection setStatus;
		private MultipleSelectionElement chkStatus;
		private MultipleSelectionElement languageCheckEl;
		private MultipleSelectionElement passwordCheckEl;
		private MultipleSelectionElement expirationDateEl;
		private MultipleSelectionElement sendLoginDeniedEmail;
		
		private final SyntaxValidator syntaxValidator;
		
		@Autowired
		private I18nManager i18nManager;
		@Autowired
		private OLATAuthManager olatAuthManager;
		
		public UserBulkChangeStepForm01b(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(Util.createPackageTranslator(UserAdminController.class, getLocale(), getTranslator()));
			// use custom translator with fallback to user properties translator
			flc.setTranslator(getTranslator());
			syntaxValidator = olatAuthManager.createPasswordSytaxValidator();

			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			boolean validChange = userBulkChanges.isValidChange();

			Map<String, String> attributeChangeMap = userBulkChanges.getAttributeChangeMap();
			attributeChangeMap.remove(UserBulkChangeManager.LANG_IDENTIFYER);
			attributeChangeMap.remove(UserBulkChangeManager.CRED_IDENTIFYER);
			userBulkChanges.setExpirationDate(null);
			userBulkChanges.setStatus(null);
			
			if (languageCheckEl.isSelected(0) && languageCheckEl.getUserObject() instanceof SingleSelection selectField)  {
				attributeChangeMap.put(UserBulkChangeManager.LANG_IDENTIFYER, selectField.getSelectedKey());
				validChange |= true;
			}
			if (passwordCheckEl.isSelected(0) && passwordCheckEl.getUserObject() instanceof TextElement propertyField)  {
				attributeChangeMap.put(UserBulkChangeManager.CRED_IDENTIFYER, propertyField.getValue());
				validChange |= true;
			}
			if (expirationDateEl.isSelected(0) && expirationDateEl.getUserObject() instanceof DateChooser dateEl)  {
				userBulkChanges.setExpirationDate(dateEl.getDate());
				validChange |= true;
			}
			if (chkStatus != null && chkStatus.isAtLeastSelected(1)) {
				userBulkChanges.setStatus(Integer.parseInt(setStatus.getSelectedKey()));
				// also check dependent send-email checkbox
				if (sendLoginDeniedEmail != null) {
					userBulkChanges.setSendLoginDeniedEmail(sendLoginDeniedEmail.isSelected(0));			
				}
				validChange |= true;
			}
			
			userBulkChanges.setValidChange(validChange);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean validChange = super.validateFormLogic(ureq);

			if (passwordCheckEl.isSelected(0)) {
				validChange = true;
				FormItem formItem = (FormItem)passwordCheckEl.getUserObject();
				if (formItem instanceof TextElement propertyField && propertyField.getName().equals("password")) {
					String password = propertyField.getValue();
					// No identity :-(
					ValidationResult validationResult = syntaxValidator.validate(password);
					if (!validationResult.isValid()) {
						String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
						propertyField.setErrorKey("error.password", descriptions);
						return false;
					}
				}
			}

			userBulkChanges.setValidChange(validChange);
			return validChange;
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(chkStatus == source || setStatus == source) {
				setStatus.setVisible(chkStatus.isAtLeastSelected(1));
				boolean loginDenied = chkStatus.isAtLeastSelected(1) && setStatus.isOneSelected()
						&& Integer.toString(Identity.STATUS_LOGIN_DENIED).equals(setStatus.getSelectedKey());
				sendLoginDeniedEmail.setVisible(loginDenied);
			} else if(source instanceof MultipleSelectionElement checkbox
					&& checkbox.getUserObject() instanceof FormItem item) {
				item.setVisible(checkbox.isAtLeastSelected(1));
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("step0a.description");
			setFormInfo("step0a.content", new String[] { Integer.toString(userBulkChanges.getNumOfIdentitiesToEdit()) });
			formLayout.setElementCssClass("o_sel_user_settings");
			
			// add input field for password
			passwordCheckEl = uifactory.addCheckboxesHorizontal("checkboxPWD", "form.name.pwd", formLayout, new String[] { "changePWD" }, new String[] { "" });
			passwordCheckEl.select("changePWD", false);
			passwordCheckEl.addActionListener(FormEvent.ONCLICK);
			TextElement passwordTextEl = uifactory.addTextElement(UserBulkChangeManager.CRED_IDENTIFYER, "password", null, 127, null, formLayout);
			passwordTextEl.setDisplaySize(35);
			passwordTextEl.setLabel(null, null);
			passwordTextEl.setVisible(false);
			passwordCheckEl.setUserObject(passwordTextEl);

			// add SingleSelect for language
			Map<String, String> locdescs = i18nManager.getEnabledLanguagesTranslated();
			Set<String> lkeys = locdescs.keySet();
			String[] languageKeys = new String[lkeys.size()];
			String[] languageValues = new String[lkeys.size()];
			// fetch languages
			int p = 0;
			for (Iterator<String> iter = lkeys.iterator(); iter.hasNext();) {
				String key = iter.next();
				languageKeys[p] = key;
				languageValues[p] = locdescs.get(key);
				p++;
			}
			languageCheckEl = uifactory.addCheckboxesHorizontal("checkboxLang", "form.name.language", formLayout, new String[] { "changeLang" }, new String[] { "" });
			languageCheckEl.select("changeLang", false);
			languageCheckEl.addActionListener(FormEvent.ONCLICK);
			SingleSelection languageSelectionEl = uifactory.addDropdownSingleselect(UserBulkChangeManager.LANG_IDENTIFYER, null, formLayout, languageKeys, languageValues, null);
			languageSelectionEl.setLabel(null, null);
			languageSelectionEl.setVisible(false);
			languageCheckEl.setUserObject(languageSelectionEl);
			
			// status
			Roles roles = ureq.getUserSession().getRoles();
			if (roles.isAdministrator() || roles.isRolesManager()) {
				chkStatus = uifactory.addCheckboxesHorizontal("Status", "table.role.status", formLayout, onKeys, onValues);
				chkStatus.select("Status", false);
				chkStatus.addActionListener(FormEvent.ONCLICK);

				SelectionValues statusPK = new SelectionValues();
				statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_ACTIV), translate("rightsForm.status.activ")));
				statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PERMANENT), translate("rightsForm.status.permanent")));
				statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_INACTIVE), translate("rightsForm.status.inactive")));
				statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_LOGIN_DENIED), translate("rightsForm.status.login_denied")));
				statusPK.add(SelectionValues.entry(Integer.toString(Identity.STATUS_PENDING), translate("rightsForm.status.pending")));
				
				setStatus = uifactory.addDropdownSingleselect("setStatus",null, formLayout, statusPK.keys(), statusPK.values(), null);
				setStatus.setVisible(false);
				setStatus.addActionListener(FormEvent.ONCHANGE);

				sendLoginDeniedEmail = uifactory.addCheckboxesHorizontal("rightsForm.sendLoginDeniedEmail", formLayout, new String[]{"y"}, new String[]{translate("rightsForm.sendLoginDeniedEmail")});
				sendLoginDeniedEmail.setLabel(null, null);
				sendLoginDeniedEmail.setVisible(false);
			}
			
			expirationDateEl = uifactory.addCheckboxesHorizontal("checkboxExpiration", "rightsForm.expiration.date", formLayout, new String[] { "changeExp" }, new String[] { "" });
			expirationDateEl.select("changeExp", false);
			expirationDateEl.addActionListener(FormEvent.ONCLICK);
			DateChooser expirationEl =  uifactory.addDateChooser(UserBulkChangeManager.EXPIRATION_IDENTIFYER, null, formLayout);
			expirationEl.setLabel(null, null);
			expirationEl.setVisible(false);
			expirationDateEl.setUserObject(expirationEl);
		}
	}
}
