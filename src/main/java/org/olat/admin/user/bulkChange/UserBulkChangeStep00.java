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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.ProfileFormController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.GenericUnique127CharTextPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

public /**
 * Description:<br>
 * First Step in bulk change wizard using flexiForm
 * 
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author rhaag
 */
class UserBulkChangeStep00 extends BasicStep {
	
	static final String usageIdentifyer = UserBulkChangeStep00.class.getCanonicalName();
	static final String usageIdentifyerForAllProperties = ProfileFormController.class.getCanonicalName();
	private List<Identity> identitiesToEdit;
	private static VelocityEngine velocityEngine;
	private boolean isAdministrativeUser;
	private boolean isOLATAdmin;

	static {
		// init velocity engine
		Properties p = null;
		try {
			velocityEngine = new VelocityEngine();
			p = new Properties();
			p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			p.setProperty("runtime.log.logsystem.log4j.category", "syslog");
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p.toString());
		}
	}

	public UserBulkChangeStep00(UserRequest ureq, List<Identity> toEdit) {
		super(ureq);
		this.identitiesToEdit = toEdit;
		setI18nTitleAndDescr("step0.description", null);
		setNextStep(new UserBulkChangeStep01(ureq));
		Roles roles = ureq.getUserSession().getRoles();
		isOLATAdmin = roles.isOLATAdmin();
		isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getInitialPrevNextFinishConfig()
	 */
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext,
	 *      org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new UserBulkChangeStepForm00(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}

	private final class UserBulkChangeStepForm00 extends StepFormBasicController {
		private FormLayoutContainer textContainer;
		private List<UserPropertyHandler> userPropertyHandlers;
		private FormItem formitem;
		private List<MultipleSelectionElement> checkBoxes;
		private List<FormItem> formItems;
		
		@Autowired
		private UserBulkChangeManager ubcMan;

		public UserBulkChangeStepForm00(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			UserManager um = UserManager.getInstance();
			setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// process changed attributes
			int i = 0;
			HashMap<String, String> attributeChangeMap = new HashMap<String, String>();
			for (Iterator<MultipleSelectionElement> iterator = checkBoxes.iterator(); iterator.hasNext();) {
				MultipleSelectionElement checkbox = iterator.next();
				if (checkbox.isSelected(0)) {
					FormItem formItem = formItems.get(i);
					// first get the values from the hardcoded items
					if (formItem.getName().equals(UserBulkChangeManager.LANG_IDENTIFYER)) {
						SingleSelection selectField = (SingleSelection) formItem;
						attributeChangeMap.put(UserBulkChangeManager.LANG_IDENTIFYER, selectField.getSelectedKey());						
					} else if (formItem.getName().equals(UserBulkChangeManager.PWD_IDENTIFYER)) {
						TextElement propertyField = (TextElement) formItem;
						attributeChangeMap.put(UserBulkChangeManager.PWD_IDENTIFYER, propertyField.getValue());						
					}					
					// second get the values from all configured user properties
					else {
						// find corresponding user property handler
						for (UserPropertyHandler propertyHanlder : userPropertyHandlers) {
							if (propertyHanlder.getName().equals(formItem.getName())) {
								String inputText;
								if (formItem instanceof DateChooser) {
									// special case: don't use getStringValue, this would encode
									// the date with the date formatter, use raw text input value
									// instead
									DateChooser dateItem = (DateChooser) formItem;	
									inputText = dateItem.getValue();
								} else {
									inputText = propertyHanlder.getStringValue(formItem);
								}

								attributeChangeMap.put(formItem.getName(), inputText);
							}
						}
					}
				}
				i++;
			}
			addToRunContext("attributeChangeMap", attributeChangeMap);
			addToRunContext("identitiesToEdit", identitiesToEdit);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean validChange = true;
			UserManager um = UserManager.getInstance();

			// loop through and check if any checkbox has been selected
			int i = 0;
			
			for (Iterator<MultipleSelectionElement> iterator = checkBoxes.iterator(); iterator.hasNext();) {
				MultipleSelectionElement checkbox = iterator.next();
				if (checkbox.isSelected(0)) {
					Context vcContext = ubcMan.getDemoContext(getTranslator());
					validChange = true;
					FormItem formItem = formItems.get(i);
					if (formItem instanceof TextElement) {
						TextElement propertyField = (TextElement) formItem;
						String inputFieldValue = propertyField.getValue();


						// check validity of velocity-variables by using default values from
						// userproperties
						inputFieldValue = inputFieldValue.replace("$", "$!");
						String evaluatedInputFieldValue = ubcMan.evaluateValueWithUserContext(inputFieldValue, vcContext);

						// check user property configuration
						for (UserPropertyHandler handler : userPropertyHandlers) {
							if (handler.getName().equals(formItem.getName())) {
								// first check on mandatoryness
								if (um.isMandatoryUserProperty(usageIdentifyer, handler) && ! StringHelper.containsNonWhitespace(evaluatedInputFieldValue)) {
									formItem.setErrorKey("form.name." + handler.getName() + ".error.empty", null);				
									return false;									
								}
								// second check on property content
								ValidationError valicationError = new ValidationError();
								if (! handler.isValidValue(null, evaluatedInputFieldValue, valicationError, ureq.getLocale())) {
									formItem.setErrorKey(valicationError.getErrorKey(), valicationError.getArgs());
									return false;
								}
								// else validation was ok, reset previous errors
								formItem.clearError();
							}
						}

						// special case: check password-syntax:
						if (propertyField.getName().equals("password")) {
							if (!um.syntaxCheckOlatPassword(evaluatedInputFieldValue)) {
								propertyField.setErrorKey("error.password", new String[] { evaluatedInputFieldValue });
								return false;
							}
						}

						// already done by form.visitAll -> validate():
						// //check all other types according to its FormItem type
						// propertyField.validate(validationResults);
						//						
						// //set value back to inputValue in order to process input in later
						// steps
						// propertyField.setValue(origInputFieldValue);
					}
				}
				i++;
			} // for

			addToRunContext("validChange", new Boolean(validChange));
			return true;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			MultipleSelectionElement checkbox;
			checkBoxes = new ArrayList<MultipleSelectionElement>();
			formItems = new ArrayList<FormItem>();

			setFormTitle("title");
			// text description of this Step
			textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step0.html");
			formLayout.add(textContainer);
			textContainer.contextPut("userPropertyHandlers", UserManager.getInstance().getUserPropertyHandlersFor(
					usageIdentifyerForAllProperties, isAdministrativeUser));

			Set<FormItem> targets;
			// Main layout is a vertical layout without left side padding. To format
			// the checkboxes properly we need a default layout for the remaining form
			// elements
			FormItemContainer innerFormLayout = FormLayoutContainer.createDefaultFormLayout("innerFormLayout", getTranslator());
			formLayout.add(innerFormLayout);

			// add input field for password
			Boolean canChangePwd = BaseSecurityModule.USERMANAGER_CAN_MODIFY_PWD;
			if (canChangePwd.booleanValue() || isOLATAdmin) {
				checkbox = uifactory.addCheckboxesHorizontal("checkboxPWD", "form.name.pwd", innerFormLayout, new String[] { "changePWD" }, new String[] { "" });
				checkbox.select("changePWD", false);
				checkbox.addActionListener(FormEvent.ONCLICK);
				formitem = uifactory.addTextElement(UserBulkChangeManager.PWD_IDENTIFYER, "password", 127, null, innerFormLayout);
				TextElement formEl = (TextElement) formitem;
				formEl.setDisplaySize(35);
				formitem.setLabel(null, null);
				targets = new HashSet<FormItem>();
				targets.add(formitem);
				RulesFactory.createHideRule(checkbox, null, targets, innerFormLayout);
				RulesFactory.createShowRule(checkbox, "changePWD", targets, innerFormLayout);
				checkBoxes.add(checkbox);
				formItems.add(formitem);
			}

			// add SingleSelect for language
			Map<String, String> locdescs = I18nManager.getInstance().getEnabledLanguagesTranslated();
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
			checkbox = uifactory.addCheckboxesHorizontal("checkboxLang", "form.name.language", innerFormLayout, new String[] { "changeLang" }, new String[] { "" });
			checkbox.select("changeLang", false);
			checkbox.addActionListener(FormEvent.ONCLICK);
			formitem = uifactory.addDropdownSingleselect(UserBulkChangeManager.LANG_IDENTIFYER, innerFormLayout, languageKeys, languageValues, null);
			formitem.setLabel(null, null);
			targets = new HashSet<FormItem>();
			targets.add(formitem);
			RulesFactory.createHideRule(checkbox, null, targets, innerFormLayout);
			RulesFactory.createShowRule(checkbox, "changeLang", targets, innerFormLayout);
			checkBoxes.add(checkbox);
			formItems.add(formitem);

			// add checkboxes/formitems for userProperties defined in
			// src/serviceconfig/org/olat/_spring/olat_userconfig.xml -> Key:
			// org.olat.admin.user.bulkChange.UserBulkChangeStep00
			List<UserPropertyHandler> userPropHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
			userPropertyHandlers = new ArrayList<UserPropertyHandler>();
			for (int i = 0; i < userPropHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler = userPropHandlers.get(i);
				//accept only no-unique properties
				if(!(userPropertyHandler instanceof GenericUnique127CharTextPropertyHandler)) {
					userPropertyHandlers.add(userPropertyHandler);

					checkbox = uifactory.addCheckboxesHorizontal("checkbox" + i, "form.name." + userPropertyHandler.getName(), innerFormLayout,
							new String[] { "change" + userPropertyHandler.getName() }, new String[] { "" });
					checkbox.select("change" + userPropertyHandler.getName(), false);
					checkbox.addActionListener(FormEvent.ONCLICK);
	
					formitem = userPropertyHandler.addFormItem(getLocale(), null, usageIdentifyer, isAdministrativeUser, innerFormLayout);
					formitem.setLabel(null, null);
	
					targets = new HashSet<FormItem>();
					targets.add(formitem);
					
					RulesFactory.createHideRule(checkbox, null, targets, innerFormLayout);
					RulesFactory.createShowRule(checkbox, "change" + userPropertyHandler.getName(), targets, innerFormLayout);
	
					checkBoxes.add(checkbox);
					formItems.add(formitem);
				}
			}
		}
	}
}