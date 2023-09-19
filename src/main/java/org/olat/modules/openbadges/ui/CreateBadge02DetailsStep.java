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
package org.olat.modules.openbadges.ui;

import java.net.URL;

import org.json.JSONObject;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.v2.Constants;
import org.olat.modules.openbadges.v2.Profile;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge02DetailsStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge02DetailsStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.details", null);
		setNextStep(new CreateBadge03CriteriaStep(ureq, createBadgeClassContext));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadge02DetailsForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_VERTICAL, null);
	}

	private class CreateBadge02DetailsForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private TextElement nameEl;
		private TextElement versionEl;
		private TextAreaElement descriptionEl;
		private TextElement issuerNameEl;
		private TextElement issuerUrlEl;
		private TextElement issuerEmailEl;
		private Profile issuer;
		private SingleSelection expiration;
		private final SelectionValues expirationKV;
		private FormLayoutContainer validityContainer;
		private IntegerElement validityTimelapseEl;
		private SingleSelection validityTimelapseUnitEl;
		private final SelectionValues validityTimelapseUnitKV;

		private enum Expiration {
			never, validFor
		}

		private enum TimeUnit {
			day, week, month, year
		}

		public CreateBadge02DetailsForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			expirationKV = new SelectionValues();
			expirationKV.add(SelectionValues.entry(Expiration.never.name(), translate("form.never")));
			expirationKV.add(SelectionValues.entry(Expiration.validFor.name(), translate("form.valid")));

			validityTimelapseUnitKV = new SelectionValues();
			validityTimelapseUnitKV.add(SelectionValues.entry(TimeUnit.day.name(), translate("form.time.day")));
			validityTimelapseUnitKV.add(SelectionValues.entry(TimeUnit.week.name(), translate("form.time.week")));
			validityTimelapseUnitKV.add(SelectionValues.entry(TimeUnit.month.name(), translate("form.time.month")));
			validityTimelapseUnitKV.add(SelectionValues.entry(TimeUnit.year.name(), translate("form.time.year")));

			issuer = new Profile(new JSONObject(createContext.getBadgeClass().getIssuer()));

			initForm(ureq);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == expiration) {
				updateUI();
			}
			super.formInnerEvent(ureq, source, event);
		}

		private void updateUI() {
			BadgeClass badgeClass = createContext.getBadgeClass();

			if (Expiration.validFor.name().equals(expiration.getSelectedKey())) {
				validityContainer.setVisible(true);
				validityTimelapseEl.setIntValue(badgeClass.getValidityTimelapse());
				if (badgeClass.getValidityTimelapseUnit() != null) {
					validityTimelapseUnitEl.select(badgeClass.getValidityTimelapseUnit().name(), true);
				} else {
					validityTimelapseUnitEl.select(BadgeClass.BadgeClassTimeUnit.week.name(), true);
				}
			} else {
				validityContainer.setVisible(false);
			}
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			nameEl.clearError();
			versionEl.clearError();
			descriptionEl.clearError();
			issuerNameEl.clearError();
			issuerEmailEl.clearError();
			issuerUrlEl.clearError();
			validityContainer.clearError();

			boolean allOk = super.validateFormLogic(ureq);

			if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
				nameEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			if (!StringHelper.containsNonWhitespace(versionEl.getValue())) {
				versionEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			if (!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
				descriptionEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			if (!StringHelper.containsNonWhitespace(issuerNameEl.getValue())) {
				issuerNameEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			if (StringHelper.containsNonWhitespace(issuerEmailEl.getValue())) {
				if (!MailHelper.isValidEmailAddress(issuerEmailEl.getValue())) {
					issuerEmailEl.setErrorKey("form.email.invalid");
					allOk &= false;
				}
			}

			if (StringHelper.containsNonWhitespace(issuerUrlEl.getValue())) {
				try {
					new URL(issuerUrlEl.getValue());
				} catch (Exception e) {
					issuerUrlEl.setErrorKey("form.url.invalid");
					allOk &= false;
				}
			}

			if (Expiration.validFor.name().equals(expiration.getSelectedKey())) {
				if (!validityTimelapseEl.validateIntValue()) {
					validityContainer.setErrorKey("form.error.nointeger");
					allOk = false;
				} else if (validityTimelapseEl.getIntValue() <= 0) {
					validityContainer.setErrorKey("form.error.positive.integer");
					allOk = false;
				}
			}

			return allOk;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			BadgeClass badgeClass = createContext.getBadgeClass();
			badgeClass.setName(nameEl.getValue());
			badgeClass.setVersion(versionEl.getValue());
			badgeClass.setDescription(descriptionEl.getValue());
			issuer.setName(issuerNameEl.getValue());
			if (StringHelper.containsNonWhitespace(issuerUrlEl.getValue())) {
				issuer.setUrl(issuerUrlEl.getValue());
			}
			if (StringHelper.containsNonWhitespace(issuerEmailEl.getValue())) {
				issuer.setEmail(issuerEmailEl.getValue());
			}
			badgeClass.setIssuer(issuer.asJsonObject(Constants.TYPE_VALUE_ISSUER).toString());
			badgeClass.setValidityEnabled(Expiration.validFor.name().equals(expiration.getSelectedKey()));
			if (badgeClass.isValidityEnabled()) {
				badgeClass.setValidityTimelapse(validityTimelapseEl.getIntValue());
				badgeClass.setValidityTimelapseUnit(BadgeClass.BadgeClassTimeUnit.valueOf(validityTimelapseUnitEl.getSelectedKey()));
			} else {
				badgeClass.setValidityTimelapse(0);
				badgeClass.setValidityTimelapseUnit(null);
			}

			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("form.details");

			BadgeClass badgeClass = createContext.getBadgeClass();

			nameEl = uifactory.addTextElement("form.name", 80, OpenBadgesUIFactory.getName(badgeClass), formLayout);
			nameEl.setElementCssClass("o_sel_badge_name");
			nameEl.setMandatory(true);

			versionEl = uifactory.addTextElement("form.version", 24, OpenBadgesUIFactory.getVersion(badgeClass), formLayout);
			versionEl.setMandatory(true);

			descriptionEl = uifactory.addTextAreaElement("form.description", "form.description",
					512, 2, 80, false, false,
					OpenBadgesUIFactory.getDescription(badgeClass), formLayout);
			descriptionEl.setElementCssClass("o_sel_badge_description");
			descriptionEl.setMandatory(true);

			issuerNameEl = uifactory.addTextElement("class.issuer", 80, OpenBadgesUIFactory.getName(issuer), formLayout);
			issuerNameEl.setMandatory(true);

			String issuerUrl = issuer.getUrl() != null ? issuer.getUrl() : "";
			issuerUrlEl = uifactory.addTextElement("class.issuer.url", 128, issuerUrl, formLayout);

			String issuerEmail = issuer.getEmail() != null ? issuer.getEmail() : "";
			issuerEmailEl = uifactory.addTextElement("class.issuer.email", 128, issuerEmail, formLayout);

			expiration = uifactory.addRadiosVertical("form.expiration", formLayout, expirationKV.keys(),
					expirationKV.values());
			expiration.addActionListener(FormEvent.ONCHANGE);
			if (badgeClass.isValidityEnabled()) {
				expiration.select(Expiration.validFor.name(), true);
			} else {
				expiration.select(Expiration.never.name(), true);
			}

			validityContainer = FormLayoutContainer.createButtonLayout("validity", getTranslator());
			validityContainer.setElementCssClass("o_inline_cont");
			validityContainer.setLabel("form.validity.period", null);
			validityContainer.setMandatory(true);
			validityContainer.setRootForm(mainForm);
			formLayout.add(validityContainer);

			validityTimelapseEl = uifactory.addIntegerElement("timelapse", null, 0, validityContainer);
			validityTimelapseEl.setDisplaySize(4);

			validityTimelapseUnitEl = uifactory.addDropdownSingleselect("timelapse.unit", null, validityContainer,
					validityTimelapseUnitKV.keys(), validityTimelapseUnitKV.values(), null);

			if (badgeClass.isValidityEnabled()) {
				validityContainer.setVisible(true);
				validityTimelapseEl.setIntValue(badgeClass.getValidityTimelapse());
				if (badgeClass.getValidityTimelapseUnit() != null) {
					validityTimelapseUnitEl.select(badgeClass.getValidityTimelapseUnit().name(), true);
				} else {
					validityTimelapseUnitEl.select(BadgeClass.BadgeClassTimeUnit.week.name(), true);
				}
			} else {
				validityContainer.setVisible(false);
			}
		}
	}
}
