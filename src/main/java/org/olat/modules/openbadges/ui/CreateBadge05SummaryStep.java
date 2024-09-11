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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.MarkdownElement;
import org.olat.core.gui.components.image.ImageFormItem;
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
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.repository.RepositoryEntry;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge05SummaryStep extends BasicStep {
	public CreateBadge05SummaryStep(UserRequest ureq, CreateBadgeClassWizardContext createContext) {
		super(ureq);
		setI18nTitleAndDescr("form.summary", null);
		if (createContext.showRecipientsStep()) {
			setNextStep(new CreateBadge06RecipientsStep(ureq, createContext));
		} else {
			setNextStep(Step.NOSTEP);
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateBadgeSummaryForm(ureq, wControl, form, runContext, FormBasicController.LAYOUT_CUSTOM, "summary_step");
	}

	private class CreateBadgeSummaryForm extends StepFormBasicController {

		private final String mediaUrl;
		private CreateBadgeClassWizardContext createContext;
		private ImageFormItem imageEl;
		private final String tmpSvgFileName;

		@Autowired
		private OpenBadgesManager openBadgesManager;

		public CreateBadgeSummaryForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout, String customLayoutPageName) {
			super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			assert createContext != null;
			tmpSvgFileName = createContext.getBadgeClass().getUuid() + ".svg";
			removeTemporarySvgFile();

			mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

			initForm(ureq);
		}

		private void removeTemporarySvgFile() {
			File tmpSvgFile = new File(WebappHelper.getTmpDir(), tmpSvgFileName);
			if (tmpSvgFile.exists()) {
				tmpSvgFile.delete();
			}
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
				imageEl = new ImageFormItem(ureq.getUserSession(), "form.image");
				formLayout.add(imageEl);

				flc.contextPut("createMode", true);
				if (createContext.selectedTemplateIsSvg() || createContext.ownFileIsSvg()) {
					setSvg();
				} else if (createContext.selectedTemplateIsPng() || createContext.ownFileIsPng()) {
					setPng();
				}
			} else {
				flc.contextPut("createMode", false);
				flc.contextPut("img", mediaUrl + "/" + createContext.getBadgeClass().getImage());
			}

			BadgeClass badgeClass = createContext.getBadgeClass();

			uifactory.addStaticTextElement("name", "form.name", badgeClass.getNameWithScan(), formLayout);
			uifactory.addStaticTextElement("version", "form.version", badgeClass.getVersionWithScan(), formLayout);
			uifactory.addStaticTextElement("language", "form.language", badgeClass.getLanguage(), formLayout);
			MarkdownElement descriptionEl = uifactory.addMarkdownElement("description", "form.description", badgeClass.getDescriptionWithScan(), formLayout);
			descriptionEl.setElementCssClass("o_badge_class_description");
			descriptionEl.setEnabled(false);
			uifactory.addStaticTextElement("expires", "form.badge.expiry", createExpiryString(badgeClass), formLayout);

			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			MarkdownElement criteriaDescriptionEl = uifactory.addMarkdownElement("form.criteria.description",
					null, badgeCriteria.getDescriptionWithScan(), formLayout);
			criteriaDescriptionEl.setElementCssClass("o_badge_criteria_description");
			criteriaDescriptionEl.setEnabled(false);

			buildConditionsFromContext();

			if (!badgeCriteria.isAwardAutomatically()) {
				uifactory.addStaticTextElement("badge.issued.manually", null,
						translate("badge.issued.manually"), formLayout);
			}
		}

		private void buildConditionsFromContext() {
			BadgeCriteria badgeCriteria = createContext.getBadgeCriteria();

			flc.contextPut("showConditions", badgeCriteria.isAwardAutomatically());

			List<BadgeCondition> badgeConditions = badgeCriteria.getConditions();
			List<Object> conditions = new ArrayList<>();
			for (int i = 0; i < badgeConditions.size(); i++) {
				BadgeCondition badgeCondition = badgeConditions.get(i);
				Condition condition = new Condition(badgeCondition, i == 0, getTranslator(), createContext.getBadgeClass().getEntry());
				conditions.add(condition);
			}
			flc.contextPut("conditions", conditions);
		}

		public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator, RepositoryEntry courseEntry) {
			@Override
			public String toString() {
				return badgeCondition.toString(translator, courseEntry);
			}
		}

		private String createExpiryString(BadgeClass badgeClass) {
			if (!badgeClass.isValidityEnabled()) {
				return translate("form.never");
			} else {
				return badgeClass.getValidityTimelapse() +
						" " +
						translate("form.time." + badgeClass.getValidityTimelapseUnit().name());
			}
		}

		private void setSvg() {
			Long templateKey = createContext.getSelectedTemplateKey();
			if (Objects.equals(templateKey, CreateBadgeClassWizardContext.OWN_BADGE_KEY)) {
				try {
					String svg = Files.readString(createContext.getTemporaryBadgeImageFile().toPath());
					File tmpSvgFile = new File(WebappHelper.getTmpDir(), tmpSvgFileName);
					Files.writeString(tmpSvgFile.toPath(), svg);
					imageEl.setMedia(tmpSvgFile);
					imageEl.setVisible(true);
				} catch (IOException e) {
					logError("Invalid image file", e);
					imageEl.setVisible(false);
				}
				return;
			}

			String backgroundColorId = createContext.getBackgroundColorId();
			String title = createContext.getTitle();
			String svg = openBadgesManager.getTemplateSvgImageWithSubstitutions(templateKey, backgroundColorId, title);
			if (StringHelper.containsNonWhitespace(svg)) {
				File tmpSvgFile = new File(WebappHelper.getTmpDir(), tmpSvgFileName);
				try {
					Files.writeString(tmpSvgFile.toPath(), svg);
					imageEl.setMedia(tmpSvgFile);
					imageEl.setVisible(true);
				} catch (IOException e) {
					logError("", e);
					imageEl.setVisible(false);
				}
			} else {
				imageEl.setVisible(false);
			}
		}

		private void setPng() {
			Long templateKey = createContext.getSelectedTemplateKey();
			if (Objects.equals(templateKey, CreateBadgeClassWizardContext.OWN_BADGE_KEY)) {
				imageEl.setMedia(createContext.getTemporaryBadgeImageFile());
				return;
			}

			imageEl.setMedia(openBadgesManager.getTemplateVfsLeaf(createContext.getSelectedTemplateImage()));
		}

		@Override
		protected void doDispose() {
			removeTemporarySvgFile();
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
