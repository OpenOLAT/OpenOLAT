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
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBadgeClassController extends FormBasicController {

	private final BadgeClass badgeClass;
	private StaticTextElement uuidEl;
	private TextElement versionEl;
	private ImageFormItem imageEl;
	private FileElement fileEl;
	private TextElement nameEl;
	private TextAreaElement descriptionEl;
	private TextAreaElement criteriaEl;
	private TextAreaElement issuerEl;
	private TextElement tagsEl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public EditBadgeClassController(UserRequest ureq, WindowControl wControl, BadgeClass badgeClass) {
		super(ureq, wControl);
		this.badgeClass = badgeClass;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String uuid = badgeClass != null ? badgeClass.getUuid() :
				UUID.randomUUID().toString().replace("-", "");
		uuidEl = uifactory.addStaticTextElement("form.uuid", uuid, formLayout);

		String version = badgeClass != null ? badgeClass.getVersion() : "1.0";
		versionEl = uifactory.addTextElement("form.version", 16, version, formLayout);

		if (badgeClass == null) {
			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.image", formLayout);
		} else {
			imageEl = new ImageFormItem(ureq.getUserSession(), "form.image");
			imageEl.setMedia(openBadgesManager.getBadgeClassVfsLeaf(badgeClass.getImage()));
			if (imageEl.getComponent() instanceof ImageComponent imageComponent) {
				imageComponent.setMaxWithAndHeightToFitWithin(80, 80);
			}
			formLayout.add(imageEl);
		}

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

		String tags = badgeClass != null ? badgeClass.getTags() : "";
		tagsEl = uifactory.addTextElement("class.tags", 128, tags, formLayout);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String submitLabelKey = badgeClass != null ? "save" : "class.add";
		uifactory.addFormSubmitButton(submitLabelKey, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (badgeClass == null) {
			File uploadedFile = fileEl.getUploadFile();
			String targetFileName = fileEl.getUploadFileName();
			if (uploadedFile != null) {
				openBadgesManager.createBadgeClass(uuidEl.getValue(), versionEl.getValue(), uploadedFile, targetFileName,
						nameEl.getValue(), descriptionEl.getValue(), criteriaEl.getValue(), issuerEl.getValue(),
						tagsEl.getValue(), getIdentity());
			}
		} else {
			badgeClass.setVersion(versionEl.getValue());
			badgeClass.setName(nameEl.getValue());
			badgeClass.setDescription(descriptionEl.getValue());
			badgeClass.setCriteria(criteriaEl.getValue());
			badgeClass.setIssuer(issuerEl.getValue());
			badgeClass.setTags(tagsEl.getValue());
			openBadgesManager.updateBadgeClass(badgeClass);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileEl) {
			validateImageFile();
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
}
