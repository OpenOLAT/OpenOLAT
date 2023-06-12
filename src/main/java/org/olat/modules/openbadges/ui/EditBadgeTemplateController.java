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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
	private ImageFormItem imageEl;
	private FileElement fileEl;
	private TextElement nameEl;
	private TextAreaElement descriptionEl;
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

		String name = badgeTemplate != null ? badgeTemplate.getName() : "";
		nameEl = uifactory.addTextElement("form.name", 80, name, formLayout);
		nameEl.setMandatory(true);
		nameEl.setElementCssClass("o_test_css_class");

		String description = badgeTemplate != null ? badgeTemplate.getDescription() : "";
		descriptionEl = uifactory.addTextAreaElement("form.description", "form.description",
				512, 2, 80, false, false, description, formLayout);

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
	protected void formOK(UserRequest ureq) {
		if (badgeTemplate == null) {
			File templateFile = fileEl.getUploadFile();
			String targetFileName = fileEl.getUploadFileName();
			if (templateFile != null) {
				badgeTemplate = openBadgesManager.createTemplate(nameEl.getValue(), templateFile, targetFileName,
						descriptionEl.getValue(), scopeEl.getSelectedKeys(), getIdentity());
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
		}
	}

	private boolean validateTemplateFile() {
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
}
