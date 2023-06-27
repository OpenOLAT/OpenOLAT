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
import java.util.List;

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
import org.olat.core.util.StringHelper;
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
	private FileElement fileEl;
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
		String identifier = badgeTemplate != null ? badgeTemplate.getIdentifier() :
				OpenBadgesUIFactory.createIdentifier();
		identifierEl = uifactory.addStaticTextElement("form.identifier", identifier, formLayout);

		if (badgeTemplate == null) {
			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image", formLayout);
			fileEl.setMandatory(true);
		} else {
			imageEl = new ImageFormItem(ureq.getUserSession(), "form.image");
			imageEl.setMedia(openBadgesManager.getTemplateVfsLeaf(badgeTemplate.getImage()));
			if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
				imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
			}
			formLayout.add(imageEl);
		}

		nameCont = FormLayoutContainer.createButtonLayout("nameCont", getTranslator());
		nameCont.setLabel("form.name", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		nameCont.setMandatory(true);
		formLayout.add(nameCont);

		String name = OpenBadgesUIFactory.translateTemplateName(getTranslator(), identifier, "form.template.name.placeholder");
		nameEl = uifactory.addStaticTextElement("name", name, nameCont);

		nameLink = uifactory.addFormLink("form.translation", nameCont);

		descriptionCont = FormLayoutContainer.createButtonLayout("descriptionCont", getTranslator());
		descriptionCont.setLabel("form.description", null);
		descriptionCont.setElementCssClass("o_inline_cont");
		descriptionCont.setRootForm(mainForm);
		formLayout.add(descriptionCont);

		String description = OpenBadgesUIFactory.translateTemplateDescription(getTranslator(), identifier);

		descriptionEl = uifactory.addTextAreaElement("description", null,
				-1, -1, -1, true, false, description, descriptionCont);
		descriptionEl.setEnabled(false);
		descriptionEl.setVisible(StringHelper.containsNonWhitespace(description));

		descriptionLink = uifactory.addFormLink("descriptionLink", "form.translation", null, descriptionCont, Link.LINK);

		descriptionSpacerEl = uifactory.addStaticTextElement("descriptionSpacer", "&nbsp;", descriptionCont);

		categoriesEl = uifactory.addTagSelection("form.categories", "form.categories", formLayout,
				getWindowControl(), categories);

		scopeEl = uifactory.addCheckboxesVertical("form.scope", formLayout, scopeKV.keys(), scopeKV.values(), 1);
		if (badgeTemplate != null) {
			badgeTemplate.getScopesAsCollection().forEach(s -> scopeEl.select(s, true));
		}
		scopeEl.select("a", true);

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

		nameCont.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue()) || translate("form.template.name.placeholder").equals(nameEl.getValue())) {
			nameCont.setErrorKey("form.legende.mandatory");
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
			openBadgesManager.updateTemplate(badgeTemplate);
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
		} else if (source == nameLink) {
			doTranslateName(ureq);
		} else if (source == descriptionLink) {
			doTranslateDescription(ureq);
		}
	}

	private void doTranslateName(UserRequest ureq) {
		if (guardModalController(templateNameTranslatorCtrl)) {
			return;
		}

		String i18nKey = OpenBadgesUIFactory.getTemplateNameI18nKey(badgeTemplate.getIdentifier());

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

		String i18nKey = OpenBadgesUIFactory.getTemplateDescriptionI18nKey(badgeTemplate.getIdentifier());

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
			String translation = OpenBadgesUIFactory.translateTemplateName(getTranslator(),
					badgeTemplate.getIdentifier(), "form.template.name.placeholder");
			nameEl.setValue(translation);
			validateFormLogic(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (source == templateDescriptionTranslatorCtrl) {
			String translation = OpenBadgesUIFactory.translateTemplateDescription(getTranslator(),
					badgeTemplate.getIdentifier());
			descriptionEl.setValue(translation);
			descriptionEl.setVisible(StringHelper.containsNonWhitespace(translation));
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

		File templateFile = fileEl.getUploadFile();
		fileEl.clearError();
		if (templateFile != null && templateFile.exists()) {
			String fileName = fileEl.getUploadFileName().toLowerCase();
			if (fileName.toLowerCase().endsWith(".png")) {
				allOk = validatePng(templateFile);
			} else if (fileName.toLowerCase().endsWith(".svg")) {
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
}
