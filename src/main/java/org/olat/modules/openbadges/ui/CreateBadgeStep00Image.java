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

import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadgeStep00Image extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadgeStep00Image(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.image", null);
		setNextStep(new CreateBadgeStep01Customization(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadgeStep00Form(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadgeStep00Form extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private List<Card> cards;
		private TextElement nameEl;
		private TextAreaElement descriptionEl;
		private SingleSelection expiration;
		private SelectionValues expirationKV;
		private FormLayoutContainer validityContainer;
		private IntegerElement validityTimelapseEl;
		private SingleSelection validityTimelapseUnitEl;
		private SelectionValues validityTimelapseUnitKV;


		@Autowired
		OpenBadgesManager openBadgesManager;

		public CreateBadgeStep00Form(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "image_step");

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

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == expiration) {
				updateUI();
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if ("select".equals(event.getCommand())) {
				String templateKeyString = ureq.getParameter("templateKey");
				if (templateKeyString != null) {
					long templateKey = Long.parseLong(templateKeyString);
					doSelectTemplate(templateKey);
					updateUI();
				}
			}
			super.event(ureq, source, event);
		}

		private void doSelectTemplate(long templateKey) {
			BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
			nameEl.setValue(template.getName());
			descriptionEl.setValue(template.getDescription());
			createContext.setSelectedTemplateKey(template.getKey());
			flc.contextPut("chooseTemplate", false);
			flc.contextPut("card", findCard(templateKey));
		}

		private Card findCard(long templateKey) {
			return cards.stream().filter(c -> c.key == templateKey).findFirst().orElse(null);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			super.event(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
				nameEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			if (Expiration.validFor.name().equals(expiration.getSelectedKey())) {
				if (!validityTimelapseEl.validateIntValue()) {
					validityContainer.setErrorKey("form.error.nointeger");
					allOk &= false;
				} else if (validityTimelapseEl.getIntValue() <= 0) {
					validityContainer.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			}
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
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
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("chooseTemplate", createContext.getSelectedTemplateKey() == null);

			BadgeClass badgeClass = createContext.getBadgeClass();

			String mediaUrl = registerMapper(ureq, new BadgeImageMapper());
			cards = openBadgesManager.getTemplatesWithSizes().stream()
					.map(template -> {
						Size targetSize = template.fitIn(120, 66);
						return new Card(
								template.template().getKey(),
								template.template().getName(),
								mediaUrl + "/" + template.template().getImage(),
								targetSize.getWidth(), targetSize.getHeight());
					})
					.toList();
			flc.contextPut("cards", cards);

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

		public record Card(Long key, String name, String imageSrc, int width, int height) {
		}

		private class BadgeImageMapper implements Mapper {

			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(relPath);
				if (templateLeaf != null) {
					return new VFSMediaResource(templateLeaf);
				}
				return new NotFoundMediaResource();
			}
		}

		private enum Expiration {
			never, validFor
		}

		private enum TimeUnit {
			day, week, month, year
		}
	}
}
