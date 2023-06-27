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
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge02DetailsStep extends BasicStep {
	public CreateBadge02DetailsStep(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.details", null);
		setNextStep(new CreateBadge03CriteriaStep(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge02DetailsForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_VERTICAL, null);
	}

	private class CreateBadge02DetailsForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private TextElement nameEl;
		private TextAreaElement descriptionEl;
		private SingleSelection expiration;
		private final SelectionValues expirationKV;
		private FormLayoutContainer validityContainer;
		private IntegerElement validityTimelapseEl;
		private SingleSelection validityTimelapseUnitEl;
		private final SelectionValues validityTimelapseUnitKV;

		@Autowired
		OpenBadgesManager openBadgesManager;

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
			boolean allOk = super.validateFormLogic(ureq);

			nameEl.clearError();
			if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
				nameEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}

			validityContainer.clearError();
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
			badgeClass.setDescription(descriptionEl.getValue());
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

			nameEl = uifactory.addTextElement("form.name", 80, badgeClass.getName(), formLayout);
			nameEl.setMandatory(true);
			nameEl.setElementCssClass("o_test_css_class");

			descriptionEl = uifactory.addTextAreaElement("form.description", "form.description",
					512, 2, 80, false, false,
					badgeClass.getDescription(), formLayout);

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
