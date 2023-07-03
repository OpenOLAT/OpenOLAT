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

import java.util.Set;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge01CustomizationStep extends BasicStep {
	public CreateBadge01CustomizationStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		setI18nTitleAndDescr("form.customization", null);
		setNextStep(new CreateBadge02DetailsStep(ureq, createBadgeClassContext));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge01ImageForm(ureq, wControl, form, runContext);
	}

	private class CreateBadge01ImageForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private ColorPickerElement backgroundColor;

		private TextElement titleEl;

		@Autowired
		private ColorService colorService;
		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadge01ImageForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "customize_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			initForm(ureq);
		}

		@Override
		protected void formNext(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		public void back() {
			super.back();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			Set<String> templateVariables = createContext.getTemplateVariables();
			if (templateVariables != null) {
				if (templateVariables.contains(OpenBadgesManager.VAR_BACKGROUND)) {
					backgroundColor = uifactory.addColorPickerElement("backgroundColor", "var.background",
							formLayout, colorService.getColors());
					backgroundColor.addActionListener(FormEvent.ONCHANGE);
					if (createContext.getBackgroundColorId() != null) {
						backgroundColor.setColor(createContext.getBackgroundColorId());
					} else {
						backgroundColor.setColor(colorService.getColors().get(0));
					}
				}
				if (templateVariables.contains(OpenBadgesManager.VAR_TITLE)) {
					String title = createContext.getTitle();
					titleEl = uifactory.addTextElement("title", "var.title", 24, title, formLayout);
					titleEl.addActionListener(FormEvent.ONCHANGE);
				}
			}

			setSvg();
		}

		private void setSvg() {
			Long templateKey = createContext.getSelectedTemplateKey();
			String backgroundColorId = createContext.getBackgroundColorId();
			String title = createContext.getTitle();
			String svg = openBadgesManager.getTemplateSvgImageWithSubstitutions(templateKey, backgroundColorId, title);
			if (StringHelper.containsNonWhitespace(svg)) {
				flc.contextPut("svg", svg);
			} else {
				flc.contextRemove("svg");
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == backgroundColor) {
				createContext.setBackgroundColorId(backgroundColor.getColor().getId());
				setSvg();
			} else if (source == titleEl) {
				createContext.setTitle(titleEl.getValue());
				setSvg();
			}
			super.formInnerEvent(ureq, source, event);
		}
	}
}
