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

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge04SummaryStep extends BasicStep {
	public CreateBadge04SummaryStep(UserRequest ureq, CreateBadgeClassWizardContext createContext) {
		super(ureq);
		setI18nTitleAndDescr("form.summary", null);
		if (createContext.showRecipientsStep()) {
			setNextStep(new CreateBadge05RecipientsStep(ureq, createContext));
		} else {
			setNextStep(Step.NOSTEP);
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadge04SummaryForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "summary_step");
	}

	private class CreateBadge04SummaryForm extends StepFormBasicController {

		private final String mediaUrl;
		private CreateBadgeClassWizardContext createContext;

		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadge04SummaryForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

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
		protected void formFinish(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if (createContext.getMode() == CreateBadgeClassWizardContext.Mode.create) {
				flc.contextPut("createMode", true);
				setSvg();
			} else {
				flc.contextPut("createMode", false);
				flc.contextPut("img", mediaUrl + "/" + createContext.getBadgeClass().getImage());
			}

			BadgeClass badgeClass = createContext.getBadgeClass();

			uifactory.addStaticTextElement("name", "form.name", badgeClass.getName(), formLayout);
			uifactory.addStaticTextElement("version", "form.version", badgeClass.getVersion(), formLayout);
			uifactory.addStaticTextElement("language", "form.language", badgeClass.getLanguage(), formLayout);
			uifactory.addStaticTextElement("description", "form.description", badgeClass.getDescription(),formLayout);
			uifactory.addStaticTextElement("expires", "form.badge.expiry", createExpiryString(badgeClass), formLayout);

			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			uifactory.addStaticTextElement("form.criteria.description", null, badgeCriteria.getDescription(), formLayout);

			buildConditionsFromContext(formLayout);
		}

		private void buildConditionsFromContext(FormItemContainer formLayout) {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			flc.contextPut("showConditions", badgeCriteria.isAwardAutomatically());

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
			if (templateKey == CreateBadgeClassWizardContext.OWN_BADGE_KEY) {
				try {
					String svg = new String(Files.readAllBytes(createContext.getTemporaryBadgeImageFile().toPath()), "UTF8");
					flc.contextPut("svg", svg);
				} catch (IOException e) {
					logError("Invalid image file", e);
					flc.contextRemove("svg");
				}
				return;
			}

			String backgroundColorId = createContext.getBackgroundColorId();
			String title = createContext.getTitle();
			String svg = openBadgesManager.getTemplateSvgImageWithSubstitutions(templateKey, backgroundColorId, title);
			if (StringHelper.containsNonWhitespace(svg)) {
				flc.contextPut("svg", svg);
			} else {
				flc.contextRemove("svg");
			}
		}

		private class BadgeClassMediaFileMapper implements Mapper {

			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
				if (classFileLeaf != null) {
					return new VFSMediaResource(classFileLeaf);
				}
				return new NotFoundMediaResource();
			}
		}
	}
}
