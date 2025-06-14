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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

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
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.image.ImageFormItem;
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
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadge01ImageStep extends BasicStep {

	private final CreateBadgeClassWizardContext createBadgeClassContext;

	public CreateBadge01ImageStep(UserRequest ureq, CreateBadgeClassWizardContext createBadgeClassContext) {
		super(ureq);
		this.createBadgeClassContext = createBadgeClassContext;
		setI18nTitleAndDescr("form.image", null);
		if (createBadgeClassContext.needsCustomization()) {
			setNextStep(new CreateBadge02CustomizationStep(ureq, createBadgeClassContext));
		} else if (createBadgeClassContext.showCriteriaStep()) {
			setNextStep(new CreateBadge03CriteriaStep(ureq, createBadgeClassContext));
		} else {
			setNextStep(new CreateBadge04DetailsStep(ureq, createBadgeClassContext));
		}
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		runContext.put(CreateBadgeClassWizardContext.KEY, createBadgeClassContext);
		return new CreateBadgeImageForm(ureq, wControl, form, runContext);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		if (createBadgeClassContext.showStartingPointStep(getIdentity())) {
			return super.getInitialPrevNextFinishConfig();
		}
		return PrevNextFinishConfig.NEXT;
	}

	private class CreateBadgeImageForm extends StepFormBasicController {
		public final static String OWN_BADGE_IDENTIFIER = "ownBadge";
		public final static String CURRENT_IMAGE_IDENTIFIER = "currentImage";

		private CreateBadgeClassWizardContext createContext;
		private List<TagInfo> tagInfos;
		private Set<Long> selectedTagKeys;
		private List<Card> cards;
		private String templateMediaUrl;
		private String badgeClassMediaUrl;
		private FileElement fileEl;
		private File tmpImageFile;
		private ImageFormItem imageEl;

		@Autowired
		OpenBadgesManager openBadgesManager;

		public CreateBadgeImageForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "image_step");

			if (runContext.get(CreateBadgeClassWizardContext.KEY) instanceof CreateBadgeClassWizardContext createBadgeClassWizardContext) {
				createContext = createBadgeClassWizardContext;
			}

			initForm(ureq);

			flc.getFormItemComponent().addListener(this);
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if ("select".equals(event.getCommand())) {
				String templateKeyString = ureq.getParameter("templateKey");
				if (templateKeyString != null) {
					long templateKey = Long.parseLong(templateKeyString);
					doSelectTemplate(ureq, templateKey);
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
			super.formInnerEvent(ureq, source, event);
			if (source == fileEl) {
				doSetTmpFile();
			}
		}

		private void doSetTmpFile() {
			if (validateUploadedFile()) {
				File uploadFile = fileEl.getUploadFile();
				String uploadFileName = fileEl.getUploadFileName();
				String tmpFileName = UUID.randomUUID() + "." + FileUtils.getFileSuffix(uploadFileName);
				if (tmpImageFile != null) {
					tmpImageFile.delete();
					tmpImageFile = null;
				}
				tmpImageFile = new File(WebappHelper.getTmpDir(), tmpFileName);
				try {
					Files.copy(uploadFile.toPath(), tmpImageFile.toPath());
					imageEl.setMedia(tmpImageFile);
					flc.contextPut("showUploadedImage", true);
					flc.contextPut("svgImage", OpenBadgesFactory.isSvgFileName(tmpImageFile.getPath()));
				} catch (IOException e) {
					logError("", e);
				}
			}
		}

		private boolean validateUploadedFile() {
			boolean allOk = true;

			if (fileEl == null) {
				return allOk;
			}

			File uploadFile = fileEl.getUploadFile();
			fileEl.clearError();
			if (uploadFile != null && uploadFile.exists()) {
				String fileName = fileEl.getUploadFileName().toLowerCase();
				String suffix = FileUtils.getFileSuffix(fileName);
				if (!"svg".equalsIgnoreCase(suffix) && !"png".equalsIgnoreCase(suffix)) {
					fileEl.setErrorKey("template.upload.unsupported");
					allOk &= false;
				}
			} else {
				fileEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}

			return allOk;
		}

		private void doSelectTemplate(UserRequest ureq, long templateKey) {
			createContext.setTemplateVariables(Set.of());
			createContext.setSelectedTemplateImage(null);

			if (templateKey == CreateBadgeClassWizardContext.OWN_BADGE_KEY) {
				createContext.setSelectedTemplateKey(CreateBadgeClassWizardContext.OWN_BADGE_KEY);
				flc.contextPut("selectedTemplateKey", CreateBadgeClassWizardContext.OWN_BADGE_KEY);
				flc.contextPut("showOwnBadgeSection", true);
				updateSteps(ureq);
				return;
			}
			
			if (templateKey == CreateBadgeClassWizardContext.CURRENT_BADGE_IMAGE_KEY) {
				createContext.setSelectedTemplateKey(CreateBadgeClassWizardContext.CURRENT_BADGE_IMAGE_KEY);
				flc.contextPut("selectedTemplateKey", CreateBadgeClassWizardContext.CURRENT_BADGE_IMAGE_KEY);
				flc.contextPut("showOwnBadgeSection", false);
				updateSteps(ureq);
				return;
			}

			flc.contextPut("showOwnBadgeSection", false);

			BadgeTemplate template = openBadgesManager.getTemplate(templateKey);
			createContext.setSelectedTemplateKey(template.getKey());
			String image = template.getImage();
			createContext.setSelectedTemplateImage(image);
			createContext.setTemplateVariables(openBadgesManager.getTemplateSvgSubstitutionVariables(image));
			flc.contextPut("selectedTemplateKey", templateKey);
			updateSteps(ureq);
		}

		private void updateSteps(UserRequest ureq) {
			if (createContext.needsCustomization()) {
				setNextStep(new CreateBadge02CustomizationStep(ureq, createContext));
			} else if (createContext.showCriteriaStep()) {
				setNextStep(new CreateBadge03CriteriaStep(ureq, createContext));
			} else {
				setNextStep(new CreateBadge04DetailsStep(ureq, createContext));
			}
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			super.event(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);

			if (CreateBadgeClassWizardContext.OWN_BADGE_KEY.equals(createContext.getSelectedTemplateKey())) {
				allOk &= validateUploadedFile();
			}

			return allOk;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			if (createContext.getSelectedTemplateKey() == null) {
				return;
			}

			if (CreateBadgeClassWizardContext.OWN_BADGE_KEY.equals(createContext.getSelectedTemplateKey())) {
				createContext.setTemporaryBadgeImageFile(tmpImageFile);
				createContext.setTargetBadgeImageFileName(fileEl.getUploadFileName());
				if (createContext.showCriteriaStep()) {
					setNextStep(new CreateBadge03CriteriaStep(ureq, createContext));
				} else {
					setNextStep(new CreateBadge04DetailsStep(ureq, createContext));
				}
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
				return;
			}

			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			flc.contextPut("chooseTemplate", createContext.getSelectedTemplateKey() == null);

			templateMediaUrl = registerMapper(ureq, new BadgeTemplateImageMapper());
			badgeClassMediaUrl = registerMapper(ureq, new BadgeClassImageMapper());

			imageEl = new ImageFormItem(ureq.getUserSession(), "form.image.gfx");
			formLayout.add(imageEl);
			if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
				imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
			}

			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image", formLayout);
			fileEl.addActionListener(FormEvent.ONCHANGE);

			initCards();
			initCategories();
			
			if (createBadgeClassContext.isEditWithVersion()) {
				if (createBadgeClassContext.getSelectedTemplateKey() == null) {
					doSelectTemplate(ureq, CreateBadgeClassWizardContext.CURRENT_BADGE_IMAGE_KEY);
				}
			}
		}

		private void initCards() {
			Translator translator = OpenBadgesUIFactory.getTranslator(createBadgeClassContext.getLocale());

			BadgeTemplate.Scope scope = createContext.getCourseResourcableId() != null ? BadgeTemplate.Scope.course : BadgeTemplate.Scope.global;

			Card ownBadgeCard = new Card(CreateBadgeClassWizardContext.OWN_BADGE_KEY, translate("badge.own"), 
					"", 120, 66, OWN_BADGE_IDENTIFIER, List.of(), this, "");

			Card currentBadgeImageCard = null;
			if (createBadgeClassContext.isEditWithVersion()) {
				OpenBadgesManager.BadgeClassWithSize badgeClassWithSize = openBadgesManager.getBadgeClassWithSize(createBadgeClassContext.getBadgeClass().getKey());
				Size targetSize = badgeClassWithSize.fitIn(120, 66);
				currentBadgeImageCard = new Card(CreateBadgeClassWizardContext.CURRENT_BADGE_IMAGE_KEY, 
						translate("no.changes"), 
						badgeClassMediaUrl + "/" + createBadgeClassContext.getBadgeClass().getImage(), 
						targetSize.getWidth(), targetSize.getHeight(), CURRENT_IMAGE_IDENTIFIER, List.of(), this, "o_current");
			}
			
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
								templateMediaUrl + "/" + (previewImage != null ? previewImage : image),
								targetSize.getWidth(), targetSize.getHeight(),
								template.template().getIdentifier(),
								tags,
								this, "");
					})
					.toList();
			List<Card> combinedCards = new ArrayList<>();
			if (currentBadgeImageCard != null) {
				combinedCards.add(currentBadgeImageCard);
			}
			combinedCards.add(ownBadgeCard);
			combinedCards.addAll(cards);
			flc.contextPut("cards", combinedCards);
		}

		public record Card(Long key, String name, String imageSrc, int width, int height, String identifier, List<Tag> tags,
						   CreateBadgeImageForm form, String cssClass) {
			public boolean isVisible() {
				if (OWN_BADGE_IDENTIFIER.equals(identifier)) {
					return true;
				}
				if (form.selectedTagKeys == null || form.selectedTagKeys.isEmpty()) {
					return true;
				}
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

		private class BadgeTemplateImageMapper implements Mapper {

			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf templateLeaf = openBadgesManager.getTemplateVfsLeaf(relPath);
				if (templateLeaf != null) {
					return new VFSMediaResource(templateLeaf);
				}
				return new NotFoundMediaResource();
			}
		}
		
		private class BadgeClassImageMapper implements Mapper {
			
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSLeaf classLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
				if (classLeaf != null) {
					return new VFSMediaResource(classLeaf);
				}
				return new NotFoundMediaResource();
			}
		}
	}
}
