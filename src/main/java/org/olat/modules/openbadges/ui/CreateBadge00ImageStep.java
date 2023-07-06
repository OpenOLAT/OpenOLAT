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

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.model.TagInfoImpl;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.i18n.I18nManager;
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
public class CreateBadge00ImageStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge00ImageStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.image", null);
		setNextStep(new CreateBadge02DetailsStep(ureq, createBadgeClassContext));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadge00ImageForm(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadge00ImageForm extends StepFormBasicController {

		private CreateBadgeClassWizardContext createContext;
		private List<TagInfo> tagInfos;
		private Set<Long> selectedTagKeys;
		private List<Card> cards;
		private SingleSelection templateLanguageDropdown;
		private SelectionValues templateLanguageKV;
		private String mediaUrl;

		@Autowired
		OpenBadgesManager openBadgesManager;
		@Autowired
		I18nManager i18nManager;

		public CreateBadge00ImageForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "image_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			templateLanguageKV = openBadgesManager.getTemplateTranslationLanguages(getLocale());

			initForm(ureq);

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if ("select".equals(event.getCommand())) {
				String templateKeyString = ureq.getParameter("templateKey");
				if (templateKeyString != null) {
					long templateKey = Long.parseLong(templateKeyString);
					doSelectTemplate(templateKey);
				}
				String tagKeyString = ureq.getParameter("tagKey");
				if (tagKeyString != null) {
					long tagKey = Long.parseLong(tagKeyString);
					tagInfos = tagInfos.stream().map(t -> {
						boolean selected = t.isSelected();
						Long key = t.getKey();
						if (tagKey == key) {
							selected = !selected;
						}
						return (TagInfo) new TagInfoImpl(key, t.getCreationDate(), t.getDisplayName(), t.getCount(),
								selected);
					}).toList();
					selectedTagKeys = tagInfos.stream().filter(TagInfo::isSelected).map(TagRef::getKey).collect(Collectors.toSet());
					flc.contextPut("tagInfos", tagInfos);
				}
			}
			super.event(ureq, source, event);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == templateLanguageDropdown) {
				doSelectLanguage();
			}
			super.formInnerEvent(ureq, source, event);
		}

		private void doSelectLanguage() {
			BadgeClass badgeClass = createContext.getBadgeClass();
			badgeClass.setLanguage(templateLanguageDropdown.getSelectedKey());
			initCards();
		}

		private void doSelectTemplate(long templateKey) {
			BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
			createContext.setSelectedTemplateKey(template.getKey());
			String image = template.getImage();
			createContext.setSelectedTemplateImage(image);
			createContext.setTemplateVariables(openBadgesManager.getTemplateSvgSubstitutionVariables(image));
			flc.contextPut("selectedTemplateKey", templateKey);
		}

		private Card findCard(long templateKey) {
			return cards.stream().filter(c -> c.key == templateKey).findFirst().orElse(null);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			super.event(ureq, source, event);
		}

		@Override
		protected void formNext(UserRequest ureq) {
			if (createContext.getSelectedTemplateKey() == null) {
				return;
			}

			BadgeTemplate template = openBadgesManager.getTemplate(createContext.getSelectedTemplateKey());
			String languageKey = templateLanguageDropdown.getSelectedKey();
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			Translator translator = OpenBadgesUIFactory.getTranslator(locale);
			BadgeClass badgeClass = createContext.getBadgeClass();
			badgeClass.setName(OpenBadgesUIFactory.translateTemplateName(translator, template.getIdentifier()));
			badgeClass.setDescription(OpenBadgesUIFactory.translateTemplateDescription(translator, template.getIdentifier()));

			if (createContext.needsCustomization()) {
				setNextStep(new CreateBadge01CustomizationStep(ureq, createContext));
			} else {
				setNextStep(new CreateBadge02DetailsStep(ureq, createContext));
			}
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("chooseTemplate", createContext.getSelectedTemplateKey() == null);

			templateLanguageDropdown = uifactory.addDropdownSingleselect("form.template.language", formLayout,
					templateLanguageKV.keys(), templateLanguageKV.values());
			templateLanguageDropdown.addActionListener(FormEvent.ONCHANGE);
			templateLanguageDropdown.select(templateLanguageKV.keys()[0], true);
			doSelectLanguage();

			mediaUrl = registerMapper(ureq, new BadgeImageMapper());

			initCards();
			initCategories();
		}

		private void initCards() {
			String languageKey = templateLanguageDropdown.getSelectedKey();
			Locale locale = i18nManager.getLocaleOrNull(languageKey);
			Translator translator = OpenBadgesUIFactory.getTranslator(locale);

			BadgeTemplate.Scope scope = createContext.getCourse() != null ? BadgeTemplate.Scope.course : BadgeTemplate.Scope.global;

			cards = openBadgesManager.getTemplatesWithSizes(scope).stream()
					.map(template -> {
						Size targetSize = template.fitIn(120, 66);
						String name = OpenBadgesUIFactory.translateTemplateName(translator, template.template().getIdentifier());
						String image = template.template().getImage();
						String previewImage = openBadgesManager.getTemplateSvgPreviewImage(template.template().getImage());
						List<Tag> tags = openBadgesManager
								.getCategories(template.template(), null)
								.stream()
								.filter(TagInfo::isSelected)
								.map(ti -> (Tag)ti)
								.toList();
						return new Card(
								template.template().getKey(),
								name,
								mediaUrl + "/" + (previewImage != null ? previewImage : image),
								targetSize.getWidth(), targetSize.getHeight(),
								template.template().getIdentifier(),
								tags,
								this);
					})
					.toList();
			flc.contextPut("cards", cards);
		}

		public record Card(Long key, String name, String imageSrc, int width, int height, String identifier, List<Tag> tags,
						   CreateBadge00ImageForm form) {
			public boolean isVisible() {
				for (Tag tag : tags) {
					if (form.selectedTagKeys.contains(tag.getKey())) {
						return true;
					}
				}
				return false;
			}
		}

		private void initCategories() {
			Set<Long> usedTagKeys = new HashSet<>();
			for (Card card : cards) {
				for (Tag tag : card.tags()) {
					usedTagKeys.add(tag.getKey());
				}
			}
			tagInfos = openBadgesManager.readBadgeCategoryTags().stream()
					.filter(ti -> usedTagKeys.contains(ti.getKey())).collect(Collectors.toList());
			selectedTagKeys = tagInfos.stream().filter(TagInfo::isSelected).map(TagRef::getKey).collect(Collectors.toSet());
			flc.contextPut("tagInfos", tagInfos);
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
	}
}
