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
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeTemplate;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBadgeClassController extends FormBasicController {

	private BadgeClass badgeClass;
	private boolean templateDecisionMade;
	private StaticTextElement uuidEl;

	private SingleSelection badgeTemplateDropdown;
	private SelectionValues badgeTemplateKV;
	private BadgeTemplate badgeTemplate;
	private FormLayoutContainer badgeTemplateButtonsContainer;
	private FormLink useTemplateButton;
	private FormLink doNotUseTemplateButton;
	private TextElement versionEl;
	private TextElement languageEl;
	private ImageFormItem imageEl;
	private FileElement fileEl;
	private File tmpImageFile;
	private TextElement nameEl;
	private TextAreaElement descriptionEl;
	private TextAreaElement criteriaEl;
	private TextAreaElement issuerEl;
	private TagSelection categoriesEl;
	private List<? extends TagInfo> categories;
	private FormSubmit submitButton;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public EditBadgeClassController(UserRequest ureq, WindowControl wControl, BadgeClass badgeClass) {
		super(ureq, wControl);
		this.badgeClass = badgeClass;

		templateDecisionMade = badgeClass != null;

		badgeTemplateKV = new SelectionValues();
		for (BadgeTemplate badgeTemplate : openBadgesManager.getTemplates()) {
			badgeTemplateKV.add(SelectionValues.entry(badgeTemplate.getKey().toString(), badgeTemplate.getName()));
		}

		categories = openBadgesManager.getCategories(null	, badgeClass);

		initForm(ureq);
		updateUi();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String uuid = badgeClass != null ? badgeClass.getUuid() :
				UUID.randomUUID().toString().replace("-", "");
		uuidEl = uifactory.addStaticTextElement("form.uuid", uuid, formLayout);

		badgeTemplateDropdown = uifactory.addDropdownSingleselect("form.template", formLayout, badgeTemplateKV.keys(), badgeTemplateKV.values());
		badgeTemplateButtonsContainer = FormLayoutContainer.createButtonLayout("form.use.template.buttons", getTranslator());
		badgeTemplateButtonsContainer.setRootForm(mainForm);
		formLayout.add(badgeTemplateButtonsContainer);
		useTemplateButton = uifactory.addFormLink("form.use.template", badgeTemplateButtonsContainer, Link.BUTTON);
		doNotUseTemplateButton = uifactory.addFormLink("form.do.not.use.template", badgeTemplateButtonsContainer, Link.BUTTON);

		String version = badgeClass != null ? badgeClass.getVersion() : "1.0";
		versionEl = uifactory.addTextElement("form.version", 16, version, formLayout);
		versionEl.setMandatory(true);

		String language = badgeClass != null && badgeClass.getLanguage() != null ? badgeClass.getLanguage() : getLocale().getLanguage();
		languageEl = uifactory.addTextElement("form.language", 16, language, formLayout);

		imageEl = new ImageFormItem(ureq.getUserSession(), "form.image.gfx");
		formLayout.add(imageEl);

		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image", formLayout);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);

		String name = badgeClass != null ? badgeClass.getName() : "";
		nameEl = uifactory.addTextElement("form.name", 128, name, formLayout);
		nameEl.setMandatory(true);
		nameEl.setElementCssClass("o_test_css_class");

		String description = badgeClass != null ? badgeClass.getDescription() : "";
		descriptionEl = uifactory.addTextAreaElement("form.description", "form.description",
				512, 2, 80, false, false, description, formLayout);
		descriptionEl.setMandatory(true);

		String criteria = badgeClass != null ? badgeClass.getCriteria() :  "";
		criteriaEl = uifactory.addTextAreaElement("class.criteria", "class.criteria",
				512, 2, 80, false, false, criteria, formLayout);

		String issuer = badgeClass != null ? badgeClass.getIssuer() : "";
		issuerEl = uifactory.addTextAreaElement("class.issuer", "class.issuer",
				512, 2, 80, false, false, issuer, formLayout);

		categoriesEl = uifactory.addTagSelection("form.categories", "form.categories", formLayout,
				getWindowControl(), categories);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String submitLabelKey = badgeClass != null ? "save" : "class.add";
		submitButton = uifactory.addFormSubmitButton(submitLabelKey, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	void updateUi() {
		badgeTemplateDropdown.setVisible(!templateDecisionMade);
		badgeTemplateButtonsContainer.setVisible(!templateDecisionMade);

		versionEl.setVisible(templateDecisionMade);
		languageEl.setVisible(templateDecisionMade);
		imageEl.setVisible(templateDecisionMade);
		fileEl.setVisible(templateDecisionMade);
		if (badgeClass != null) {
			if (fileEl.getUploadFile() != null) {
				imageEl.setMedia(fileEl.getUploadFile());
			} else {
				imageEl.setMedia(openBadgesManager.getBadgeClassVfsLeaf(badgeClass.getImage()));
			}
			fileEl.setLabel("form.image.other", null);
			fileEl.setMandatory(false);
		} else {
			fileEl.setLabel("form.image", null);
			fileEl.setMandatory(true);
			if (fileEl.getUploadFile() != null) {
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
			} else if (badgeTemplate != null) {
				imageEl.setMedia(openBadgesManager.getTemplateVfsLeaf(badgeTemplate.getImage()));
				fileEl.setLabel("form.image.other", null);
				fileEl.setMandatory(false);
			} else {
				imageEl.setVisible(false);
			}
		}
		if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
			imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
		}
		nameEl.setVisible(templateDecisionMade);
		descriptionEl.setVisible(templateDecisionMade);
		criteriaEl.setVisible(templateDecisionMade);
		issuerEl.setVisible(templateDecisionMade);
		categoriesEl.setVisible(templateDecisionMade);

		submitButton.setVisible(templateDecisionMade);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (badgeClass == null) {
			File imageFile = fileEl.getUploadFile();
			String targetFileName = fileEl.getUploadFileName();
			if (imageFile == null && badgeTemplate != null) {
				VFSLeaf imageLeaf = openBadgesManager.getTemplateVfsLeaf(badgeTemplate.getImage());
				if (imageLeaf instanceof LocalFileImpl imageLocalFile) {
					imageFile = imageLocalFile.getBasefile();
					targetFileName = imageLeaf.getName();
				}
			}
			if (imageFile != null) {
				String salt = "badgeClass" + Math.abs(uuidEl.getValue().hashCode());
				badgeClass = openBadgesManager.createBadgeClass(uuidEl.getValue(), versionEl.getValue(),
						languageEl.getValue(), imageFile, targetFileName,
						nameEl.getValue(), descriptionEl.getValue(), criteriaEl.getValue(), salt, issuerEl.getValue(),
						getIdentity());
			}
		} else {
			badgeClass.setVersion(versionEl.getValue());
			badgeClass.setLanguage(languageEl.getValue());
			badgeClass.setName(nameEl.getValue());
			badgeClass.setDescription(descriptionEl.getValue());
			badgeClass.setCriteria(criteriaEl.getValue());
			badgeClass.setIssuer(issuerEl.getValue());
			openBadgesManager.updateBadgeClass(badgeClass);
		}

		openBadgesManager.updateCategories(null, badgeClass, categoriesEl.getDisplayNames());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileEl) {
			if (validateImageFile()) {
				updateUi();
			}
		} else if (source == useTemplateButton) {
			templateDecisionMade = true;
			doUseTemplate();
			updateUi();
		} else if (source == doNotUseTemplateButton) {
			templateDecisionMade = true;
			updateUi();
		}
	}

	private void doUseTemplate() {
		if (badgeTemplateDropdown.getSelectedKey() != null) {
			Long badgeTemplateKey = Long.parseLong(badgeTemplateDropdown.getSelectedKey());
			badgeTemplate = openBadgesManager.getTemplate(badgeTemplateKey);
			nameEl.setValue(badgeTemplate.getName());
			descriptionEl.setValue(badgeTemplate.getDescription());
			Set<Tag> selectedTags = openBadgesManager
					.getCategories(badgeTemplate, null)
					.stream()
					.filter(TagInfo::isSelected)
					.collect(Collectors.toSet());
			categoriesEl.setSelectedTags(selectedTags);
		}
	}

	private boolean validateImageFile() {
		boolean allOk = true;

		File templateFile = fileEl.getUploadFile();
		fileEl.clearError();
		if (templateFile != null && templateFile.exists()) {
			String fileName = fileEl.getUploadFileName().toLowerCase();
			if (fileName.endsWith(".png")) {
				allOk = validatePng(templateFile);
			} else if (fileName.endsWith(".svg")) {
				allOk = validateSvg(templateFile);
			} else {
				fileEl.setErrorKey("template.upload.unsupported");
				allOk &= false;
			}
		}

		return allOk;
	}

	private boolean validatePng(File templateFile) {
		return true;
	}

	private boolean validateSvg(File templateFile) {
		return true;
	}

	@Override
	protected void doDispose() {
		if (tmpImageFile != null) {
			tmpImageFile.delete();
			tmpImageFile = null;
		}
		super.doDispose();
	}
}
