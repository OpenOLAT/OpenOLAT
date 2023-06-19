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

import java.io.IOException;
import java.nio.file.Files;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadgeStep01Customization extends BasicStep {
	public CreateBadgeStep01Customization(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.customization", null);
		setNextStep(new CreateBadgeStep02Details(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadgeStep01Form(ureq, wControl, form, runContext);
	}

	private class CreateBadgeStep01Form extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private ColorPickerElement backgroundColor;

		@Autowired
		private ColorService colorService;
		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadgeStep01Form(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "customize_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		public void back() {
			super.back();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			backgroundColor = uifactory.addColorPickerElement("backgroundColor", "var.background",
					formLayout, colorService.getColors());
			backgroundColor.addActionListener(FormEvent.ONCHANGE);
			if (createContext.getBackgroundColorId() != null) {
				backgroundColor.setColor(createContext.getBackgroundColorId());
			} else {
				backgroundColor.setColor(colorService.getColors().get(0));
			}

			setSvg();
		}

		private void setSvg() {
			Long templateKey = createContext.getSelectedTemplateKey();
			String courseTitle = createContext.getCourse().getCourseTitle();
			if (templateKey != null) {
				BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
				VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(template.getImage());
				if (templateLeaf instanceof LocalFileImpl localFile) {
					String svg;
					try {
						svg = new String(Files.readAllBytes(localFile.getBasefile().toPath()), "UTF8");
						svg = svg.replace("$title", courseTitle);
						svg = svg.replace("$background", openBadgesManager.getColorAsRgb(backgroundColor.getColor().getId()));
						flc.contextPut("svg", svg);
					} catch (IOException e) {
						flc.contextRemove("svg");
					}
				}
			} else {
				flc.contextRemove("svg");
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == backgroundColor) {
				createContext.setBackgroundColorId(backgroundColor.getColor().getId());
				setSvg();
			}
			super.formInnerEvent(ureq, source, event);
		}
	}
}
