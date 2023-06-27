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
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge04SummaryStep extends BasicStep {
	public CreateBadge04SummaryStep(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("form.summary", null);
		setNextStep(new CreateBadge05RecipientsStep(ureq));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge04SummaryForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "summary_step");
	}

	private class CreateBadge04SummaryForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;

		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadge04SummaryForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);

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
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setSvg();

			BadgeClass badgeClass = createContext.getBadgeClass();

			uifactory.addStaticTextElement("name", "form.name", badgeClass.getName(), formLayout);
			uifactory.addStaticTextElement("language", "form.language", badgeClass.getLanguage(), formLayout);
			uifactory.addStaticTextElement("description", "form.description", badgeClass.getDescription(),formLayout);
			uifactory.addStaticTextElement("expires", "form.badge.expiry", createExpiryString(badgeClass), formLayout);

			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			uifactory.addStaticTextElement("form.criteria.description", null, badgeCriteria.getDescription(), formLayout);

			buildConditionsFromContext(formLayout);
		}

		private void buildConditionsFromContext(FormItemContainer formLayout) {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			if (!badgeCriteria.isAwardAutomatically()) {
				flc.contextPut("showCriteria", false);
				return;
			}

			flc.contextPut("showCriteria", true);

			List<BadgeCondition> badgeConditions = badgeCriteria.getConditions();
			List<Object> conditions = new ArrayList<>();
			for (int i = 0; i < badgeConditions.size(); i++) {
				BadgeCondition badgeCondition = badgeConditions.get(i);
				Condition condition = new Condition(badgeCondition, i == 0, getTranslator());
				conditions.add(condition);
			}
			flc.contextPut("conditions", conditions);
		}

		public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator) {
			@Override
			public String toString() {
				return badgeCondition.toString(translator);
			}
		}

		private String createExpiryString(BadgeClass badgeClass) {
			if (!badgeClass.isValidityEnabled()) {
				return translate("form.never");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(badgeClass.getValidityTimelapse());
				sb.append(" ");
				sb.append(translate("form.time." + badgeClass.getValidityTimelapseUnit().name()));
				return sb.toString();
			}
		}

		private void setSvg() {
			Long templateKey = createContext.getSelectedTemplateKey();
			String courseTitle = createContext.getCourse().getCourseTitle();
			String colorId = createContext.getBackgroundColorId();
			if (templateKey != null) {
				BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
				VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(template.getImage());
				if (templateLeaf instanceof LocalFileImpl localFile) {
					String svg;
					try {
						svg = new String(Files.readAllBytes(localFile.getBasefile().toPath()), "UTF8");
						svg = svg.replace("$title", courseTitle);
						svg = svg.replace("$background", openBadgesManager.getColorAsRgb(colorId));
						flc.contextPut("svg", svg);
					} catch (IOException e) {
						flc.contextRemove("svg");
					}
				}
			} else {
				flc.contextRemove("svg");
			}
		}
	}
}
