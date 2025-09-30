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
package org.olat.modules.curriculum.ui;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.manager.CurriculumImportHandler;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCurriculumController extends FormBasicController {
	
	private FileElement uploadFileEl;
	private TextElement displayNameEl;
	private ObjectSelectionElement organisationEl;
	
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumImportHandler curriculumImportHandler;
	
	public ImportCurriculumController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "curriculum.file", formLayout);
		uploadFileEl.limitToMimeType(Collections.singleton("application/zip"), "error.mimetype", new String[]{ "ZIP" });
		uploadFileEl.setMandatory(true);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);
		
		displayNameEl = uifactory.addTextElement("curriculum.display.name", 255, "", formLayout);
		displayNameEl.setMandatory(true);
		
		Roles roles = ureq.getUserSession().getRoles();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				List.of(),
				() -> organisationService.getOrganisations(getIdentity(), roles,
						OrganisationRoles.administrator, OrganisationRoles.curriculummanager));
		organisationEl = uifactory.addObjectSelectionElement("organisations", "curriculum.organisation", formLayout,
				getWindowControl(), false, organisationSource);
		organisationEl.setVisible(organisationModule.isEnabled());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("import.curriculum", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		uploadFileEl.clearError();
		if(uploadFileEl.getUploadFile() == null) {
			uploadFileEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			validateFormItem(ureq, uploadFileEl);
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(uploadFileEl == source) {
			if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())
					&& uploadFileEl.getUploadFile() != null) {
				String name = curriculumImportHandler.getCurriculumName(uploadFileEl.getUploadFile());
				displayNameEl.setValue(name);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File archive = uploadFileEl.getUploadFile();
		
		Organisation organisation;
		if (organisationEl.getSelectedKey() != null) {
			organisation = organisationService.getOrganisation(OrganisationSelectionSource.toRef(organisationEl.getSelectedKey()));
		} else {
			organisation = organisationService.getDefaultOrganisation();
		}
		String curriculumName = displayNameEl.getValue();
		curriculumImportHandler.importCurriculum(archive, curriculumName, organisation, getIdentity(), getLocale());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
