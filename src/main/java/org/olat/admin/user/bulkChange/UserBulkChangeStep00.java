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

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.velocity.context.Context;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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
import org.olat.core.util.StringHelper;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
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
	
	private static final String usageIdentifyer = UserBulkChangeStep00.class.getCanonicalName();
	private static final String usageIdentifyerForAllProperties = ProfileFormController.class.getCanonicalName();

	private List<Identity> identitiesToEdit;
	private final UserBulkChanges userBulkChanges;

	public UserBulkChangeStep00(UserRequest ureq, List<Identity> toEdit, UserBulkChanges userBulkChanges) {
		super(ureq);
		this.identitiesToEdit = toEdit;
		this.userBulkChanges = userBulkChanges;
		setI18nTitleAndDescr("step0.description", null);
		setNextStep(new UserBulkChangeStep00a(ureq, userBulkChanges));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new UserBulkChangeStepForm00(ureq, windowControl, form, stepsRunContext);
	}

	private final class UserBulkChangeStepForm00 extends StepFormBasicController {
		private List<UserPropertyHandler> userPropertyHandlers;
		private final List<MultipleSelectionElement> checkBoxes = new ArrayList<>();

		private final boolean isAdministrativeUser;
		private final SyntaxValidator syntaxValidator;
		
		@Autowired
		private UserManager userManager;
		@Autowired
		private UserBulkChangeManager ubcMan;
		@Autowired
		private BaseSecurityModule securityModule;
		@Autowired
		private OLATAuthManager olatAuthManager;
		
		public UserBulkChangeStepForm00(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
			this.syntaxValidator = olatAuthManager.createPasswordSytaxValidator();

			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// process changed attributes
			Map<String, String> attributeChangeMap = userBulkChanges.getAttributeChangeMap();
			for (MultipleSelectionElement checkbox:checkBoxes) {
				if (checkbox.isSelected(0)) {
					FormItem formItem = (FormItem)checkbox.getUserObject();
					// find corresponding user property handler
					for (UserPropertyHandler propertyHanlder : userPropertyHandlers) {
						if (propertyHanlder.getName().equals(formItem.getName())) {
							String inputText;
							if (formItem instanceof DateChooser dateItem) {
								// special case: don't use getStringValue, this would encode
								// the date with the date formatter, use raw text input value
								// instead
								inputText = dateItem.getValue();
							} else {
								inputText = propertyHanlder.getStringValue(formItem);
							}

							attributeChangeMap.put(formItem.getName(), inputText);
						}
					}
				}
			}
			
			userBulkChanges.setIdentitiesToEdit(identitiesToEdit);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean validChange = super.validateFormLogic(ureq);

			// loop through and check if any checkbox has been selected
			for (MultipleSelectionElement checkbox:checkBoxes) {
				if (checkbox.isSelected(0)) {
					Context vcContext = ubcMan.getDemoContext(getTranslator());
					validChange = true;
					FormItem formItem = (FormItem)checkbox.getUserObject();
					if (formItem instanceof TextElement propertyField) {
						String inputFieldValue = propertyField.getValue();

						// check validity of velocity-variables by using default values from
						// userproperties
						inputFieldValue = inputFieldValue.replace("$", "$!");
						String evaluatedInputFieldValue = ubcMan.evaluateValueWithUserContext(inputFieldValue, vcContext);

						// check user property configuration
						for (UserPropertyHandler handler : userPropertyHandlers) {
							if (handler.getName().equals(formItem.getName())) {
								// first check on mandatoryness
								if (userManager.isMandatoryUserProperty(usageIdentifyer, handler) && !StringHelper.containsNonWhitespace(evaluatedInputFieldValue)) {
									formItem.setErrorKey("form.name." + handler.getName() + ".error.empty");
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
				}
			} // for
			
			userBulkChanges.setValidChange(validChange);
			return validChange;
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(source instanceof MultipleSelectionElement checkbox
					&& checkbox.getUserObject() instanceof FormItem item) {
				item.setVisible(checkbox.isAtLeastSelected(1));
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("step0.description");
			// text description of this Step
			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step0.html");
			formLayout.add(textContainer);
			textContainer.contextPut("userPropertyHandlers", userManager
					.getUserPropertyHandlersFor(usageIdentifyerForAllProperties, isAdministrativeUser));
			textContainer.contextPut("message", translate("step0.content", Integer.toString(identitiesToEdit.size())));
			
			// Main layout is a vertical layout without left side padding. To format
			// the checkboxes properly we need a default layout for the remaining form
			// elements
			FormItemContainer innerFormLayout = FormLayoutContainer.createDefaultFormLayout("innerFormLayout", getTranslator());
			innerFormLayout.setElementCssClass("o_sel_user_attributes");
			formLayout.add(innerFormLayout);

			// add checkboxes/formitems for userProperties defined in
			// src/serviceconfig/org/olat/_spring/olat_userconfig.xml -> Key:
			// org.olat.admin.user.bulkChange.UserBulkChangeStep00
			List<UserPropertyHandler> userPropHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
			userPropertyHandlers = new ArrayList<>();
			for (int i = 0; i < userPropHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler = userPropHandlers.get(i);
				//accept only no-unique properties
				if(!(userPropertyHandler instanceof GenericUnique127CharTextPropertyHandler)) {
					userPropertyHandlers.add(userPropertyHandler);
					
					MultipleSelectionElement checkboxEl = uifactory.addCheckboxesHorizontal("checkbox" + i,
							"form.name." + userPropertyHandler.getName(), innerFormLayout,
							new String[] { "change" + userPropertyHandler.getName() }, new String[] { "" });
					checkboxEl.select("change" + userPropertyHandler.getName(), false);
					checkboxEl.addActionListener(FormEvent.ONCLICK);
					
					FormItem formItem = userPropertyHandler.addFormItem(getLocale(), null, usageIdentifyer, isAdministrativeUser, innerFormLayout);
					formItem.setLabel(null, null);
					formItem.setElementCssClass("o_sel_user_" + userPropertyHandler.getName());
					formItem.setVisible(false);
					checkboxEl.setUserObject(formItem);
					checkBoxes.add(checkboxEl);
				}
			}
		}
	}
}