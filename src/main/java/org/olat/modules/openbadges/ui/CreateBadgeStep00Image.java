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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
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

	public CreateBadgeStep00Image(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.image", null);
		setNextStep(new CreateBadgeStep01Customization(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadgeStep00Form(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadgeStep00Form extends StepFormBasicController {

		private TextElement nameEl;
		private TextAreaElement descriptionEl;
		private SingleSelection expiration;
		private SelectionValues expirationKV;
		private FormLayoutContainer validityContainer;
		private IntegerElement validityInput;
		private SingleSelection validityUnit;
		private SelectionValues validityUnitKV;


		@Autowired
		OpenBadgesManager openBadgesManager;

		public CreateBadgeStep00Form(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "image_step");

			expirationKV = new SelectionValues();
			expirationKV.add(SelectionValues.entry(Expiration.never.name(), translate("form.never")));
			expirationKV.add(SelectionValues.entry(Expiration.validFor.name(), translate("form.valid")));

			validityUnitKV = new SelectionValues();
			validityUnitKV.add(SelectionValues.entry(TimeUnit.day.name(), translate("form.time.day")));
			validityUnitKV.add(SelectionValues.entry(TimeUnit.week.name(), translate("form.time.week")));
			validityUnitKV.add(SelectionValues.entry(TimeUnit.month.name(), translate("form.time.month")));
			validityUnitKV.add(SelectionValues.entry(TimeUnit.year.name(), translate("form.time.year")));

			initForm(ureq);

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if ("select".equals(event.getCommand())) {
				String templateKeyString = ureq.getParameter("templateKey");
				if (templateKeyString != null) {
					long templateKey = Long.parseLong(templateKeyString);
					doSelectTemplate(templateKey);
				}
			}
			super.event(ureq, source, event);
		}

		private void doSelectTemplate(long templateKey) {
			BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
			nameEl.setValue(template.getName());
			descriptionEl.setValue(template.getDescription());
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			super.event(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String mediaUrl = registerMapper(ureq, new BadgeImageMapper());
			List<Card> cards = openBadgesManager.getTemplatesWithSizes().stream()
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

			nameEl = uifactory.addTextElement("form.name", 80, "", formLayout);
			nameEl.setMandatory(true);
			nameEl.setElementCssClass("o_test_css_class");

			descriptionEl = uifactory.addTextAreaElement("form.description", "form.description",
					512, 2, 80, false, false, "", formLayout);

			expiration = uifactory.addRadiosVertical("form.expiration", formLayout, expirationKV.keys(),
					expirationKV.values());
			expiration.addActionListener(FormEvent.ONCHANGE);

			validityContainer = FormLayoutContainer.createButtonLayout("validity", getTranslator());
			validityContainer.setElementCssClass("o_inline_cont");
			validityContainer.setLabel("form.validity.period", null);
			validityContainer.setMandatory(true);
			validityContainer.setRootForm(mainForm);
			formLayout.add(validityContainer);

			validityInput = uifactory.addIntegerElement("timelapse", null, 0, validityContainer);
			validityInput.setDisplaySize(4);

			validityUnit = uifactory.addDropdownSingleselect("timelapse.unit", null, validityContainer,
					validityUnitKV.keys(), validityUnitKV.values(), null);
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
