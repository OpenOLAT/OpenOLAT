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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBadgeTemplateController extends FormBasicController {

	private BadgeTemplate badgeTemplate;
	private StaticTextElement identifierEl;
	private ImageFormItem imageEl;
	private StaticTextElement imageInfoEl;
	private FileElement fileEl;
	private File tmpImageFile;
	private FormLayoutContainer nameCont;
	private StaticTextElement nameEl;
	private FormLink nameLink;
	private CloseableModalController cmc;
	private SingleKeyTranslatorController templateNameTranslatorCtrl;
	private FormLayoutContainer descriptionCont;
	private TextAreaElement descriptionEl;
	private StaticTextElement descriptionSpacerEl;
	private FormLink descriptionLink;
	private SingleKeyTranslatorController templateDescriptionTranslatorCtrl;
	private TagSelection categoriesEl;
	private MultipleSelectionElement scopeEl;
	private SelectionValues scopeKV;
	private List<? extends TagInfo> categories;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public EditBadgeTemplateController(UserRequest ureq, WindowControl wControl, BadgeTemplate badgeTemplate) {
		super(ureq, wControl);
		this.badgeTemplate = badgeTemplate;

		scopeKV = new SelectionValues();
		for (BadgeTemplate.Scope scope : BadgeTemplate.Scope.values()) {
			scopeKV.add(SelectionValues.entry(scope.name(), translate("template.scope." + scope.name())));
		}

		categories = openBadgesManager.getCategories(badgeTemplate, null);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_badge_template_edit");
		String identifier = badgeTemplate != null ? badgeTemplate.getIdentifier() :
				OpenBadgesUIFactory.createIdentifier();
		identifierEl = uifactory.addStaticTextElement("form.identifier", identifier, formLayout);

		imageEl = new ImageFormItem(ureq.getUserSession(), "form.image.other");
		imageEl.showLabel(true);
		imageEl.setLabel("form.image.other", null);
		if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
			imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
		}
		imageEl.setVisible(false);
		formLayout.add(imageEl);

		imageInfoEl = uifactory.addStaticTextElement("form.imageInfo", null,
				"", formLayout);
		imageInfoEl.setVisible(false);

		if (badgeTemplate == null) {
			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image", formLayout);
			fileEl.setMandatory(true);
			fileEl.addActionListener(FormEvent.ONCHANGE);
		} else {
			String image = badgeTemplate.getImage();
			String previewImage = openBadgesManager.getTemplateSvgPreviewImage(image);
			imageEl.setMedia(openBadgesManager.getTemplateVfsLeaf(previewImage != null ? previewImage : image));
			if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
				imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
			}
			formLayout.add(imageEl);
			imageEl.setVisible(true);

			openBadgesManager.getTemplateSvgPreviewImage(badgeTemplate.getImage());
			Set<String> substitutionVariables = openBadgesManager.getTemplateSvgSubstitutionVariables(badgeTemplate.getImage());
			if (!substitutionVariables.isEmpty()) {
				String imageInfo = badgeTemplate.getImage() + " (" +
						translate("form.imageInfo.substitutionVariables", String.join(", ", substitutionVariables)) +
						")";
				imageInfoEl.setValue(imageInfo);
			} else {
				imageInfoEl.setValue(badgeTemplate.getImage());
			}
			imageInfoEl.setVisible(true);

			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image.other", formLayout);
			fileEl.addActionListener(FormEvent.ONCHANGE);
		}

		nameCont = FormLayoutContainer.createButtonLayout("nameCont", getTranslator());
		nameCont.setLabel("form.name", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		nameCont.setMandatory(true);
		formLayout.add(nameCont);

		String name = OpenBadgesUIFactory.translateTemplateName(getTranslator(), identifier);
		nameEl = uifactory.addStaticTextElement("name", name, nameCont);

		nameLink = uifactory.addFormLink("form.translation", nameCont);

		String description = OpenBadgesUIFactory.translateTemplateDescription(getTranslator(), identifier);

		descriptionCont = FormLayoutContainer.createButtonLayout("descriptionCont", getTranslator());
		descriptionCont.setLabel("form.description", null);
		descriptionCont.setElementCssClass(StringHelper.containsNonWhitespace(description) ? "o_description" : "o_inline_cont");
		descriptionCont.setRootForm(mainForm);
		formLayout.add(descriptionCont);

		descriptionEl = uifactory.addTextAreaElement("description", null,
				-1, 3, 80, true, false, description, descriptionCont);
		descriptionEl.setEnabled(false);
		descriptionEl.setVisible(StringHelper.containsNonWhitespace(description));

		descriptionSpacerEl = uifactory.addStaticTextElement("descriptionSpacer", "-", descriptionCont);
		descriptionSpacerEl.setVisible(!StringHelper.containsNonWhitespace(description));

		descriptionLink = uifactory.addFormLink("descriptionLink", "form.translation", null, descriptionCont, Link.LINK);

		categoriesEl = uifactory.addTagSelection("form.categories", "form.categories", formLayout,
				getWindowControl(), categories);

		scopeEl = uifactory.addCheckboxesVertical("form.scope", formLayout, scopeKV.keys(), scopeKV.values(), 1);
		if (badgeTemplate != null) {
			badgeTemplate.getScopesAsCollection().forEach(s -> scopeEl.select(s, true));
		}
		scopeEl.select("a", true);
		scopeEl.setMandatory(true);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String submitLabelKey = badgeTemplate != null ? "save" : "template.upload";
		uifactory.addFormSubmitButton(submitLabelKey, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateTemplateFile();

		nameCont.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue()) ||
				translate(OpenBadgesUIFactory.getTemplateNameFallbackKey()).equals(nameEl.getValue())) {
			nameCont.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		scopeEl.clearError();
		if (!scopeEl.isAtLeastSelected(1)) {
			scopeEl.setErrorKey("alert");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (badgeTemplate == null) {
			File templateFile = fileEl.getUploadFile();
			String targetFileName = fileEl.getUploadFileName();
			if (templateFile != null) {
				badgeTemplate = openBadgesManager.createTemplate(identifierEl.getValue(), nameEl.getValue(),
						templateFile, targetFileName, descriptionEl.getValue(), scopeEl.getSelectedKeys(), getIdentity());
			}
		} else {
			badgeTemplate.setName(nameEl.getValue());
			badgeTemplate.setDescription(descriptionEl.getValue());
			badgeTemplate.setScopesAsCollection(scopeEl.getSelectedKeys());
			if (fileEl.getUploadFile() != null) {
				File templateFile = fileEl.getUploadFile();
				String targetFileName = fileEl.getUploadFileName();
				openBadgesManager.updateTemplate(badgeTemplate, templateFile, targetFileName, getIdentity());
			} else {
				openBadgesManager.updateTemplate(badgeTemplate);
			}
		}

		openBadgesManager.updateCategories(badgeTemplate, null, categoriesEl.getDisplayNames());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileEl) {
			validateTemplateFile();
			updateImage();
		} else if (source == nameLink) {
			doTranslateName(ureq);
		} else if (source == descriptionLink) {
			doTranslateDescription(ureq);
		}
	}

	private void updateImage() {
		if (fileEl.getUploadFile() == null) {
			return;
		}

		if (fileEl.hasError()) {
			return;
		}

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
		} catch (IOException e) {
			logError("", e);
		}

		imageEl.setVisible(true);
		if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
			imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
		}

		imageInfoEl.setVisible(false);
	}

	private void doTranslateName(UserRequest ureq) {
		if (guardModalController(templateNameTranslatorCtrl)) {
			return;
		}

		String i18nKey = OpenBadgesUIFactory.getTemplateNameI18nKey(identifierEl.getValue());

		templateNameTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), i18nKey, OpenBadgesUIFactory.class);
		listenTo(templateNameTranslatorCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				templateNameTranslatorCtrl.getInitialComponent(), true, translate("form.name"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doTranslateDescription(UserRequest ureq) {
		if (guardModalController(templateDescriptionTranslatorCtrl)) {
			return;
		}

		String i18nKey = OpenBadgesUIFactory.getTemplateDescriptionI18nKey(identifierEl.getValue());

		templateDescriptionTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(),
				i18nKey, OpenBadgesUIFactory.class, SingleKeyTranslatorController.InputType.TEXT_AREA, null);
		listenTo(templateDescriptionTranslatorCtrl);

		cmc =  new CloseableModalController(getWindowControl(), translate("close"),
				templateDescriptionTranslatorCtrl.getInitialComponent(), true, translate("form.description"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == templateNameTranslatorCtrl) {
			String translation = OpenBadgesUIFactory.translateTemplateName(getTranslator(), identifierEl.getValue());
			nameEl.setValue(translation);
			validateFormLogic(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (source == templateDescriptionTranslatorCtrl) {
			String translation = OpenBadgesUIFactory.translateTemplateDescription(getTranslator(),
					identifierEl.getValue());
			descriptionEl.setValue(translation);
			descriptionEl.setVisible(StringHelper.containsNonWhitespace(translation));
			descriptionSpacerEl.setVisible(!StringHelper.containsNonWhitespace(translation));
			descriptionCont.setElementCssClass(StringHelper.containsNonWhitespace(translation) ? null : "o_inline_cont");
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(templateNameTranslatorCtrl);
		removeAsListenerAndDispose(templateDescriptionTranslatorCtrl);
		cmc = null;
		templateNameTranslatorCtrl = null;
		templateDescriptionTranslatorCtrl = null;
	}

	private boolean validateTemplateFile() {
		boolean allOk = true;

		if (fileEl == null || !fileEl.isMandatory()) {
			return allOk;
		}

		File templateFile = fileEl.getUploadFile();
		fileEl.clearError();
		if (templateFile != null && templateFile.exists()) {
			String fileName = fileEl.getUploadFileName().toLowerCase();
			String suffix = FileUtils.getFileSuffix(fileName);
			if (!"svg".equalsIgnoreCase(suffix) && !"png".equalsIgnoreCase(suffix)) {
				fileEl.setErrorKey("template.upload.unsupported");
				allOk &= false;
			}
		} else {
			fileEl.setErrorKey("form.legende.mandatory");
		}

		return allOk;
	}
}
