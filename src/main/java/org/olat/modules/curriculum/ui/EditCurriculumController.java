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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumController extends FormBasicController {

	private RichTextElement descriptionEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private SingleSelection organisationEl;
	private SingleSelection lecturesEnabledEl;
	
	private Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	/**
	 * Create a new curriculum.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 */
	public EditCurriculumController(UserRequest ureq, WindowControl wControl, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		curriculum = null;
		this.secCallback = secCallback;
		initForm(ureq);
	}
	
	public EditCurriculumController(UserRequest ureq, WindowControl wControl, Curriculum curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		initForm(ureq);
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(curriculum != null) {
			String key = curriculum.getKey().toString();
			uifactory.addStaticTextElement("curriculum.key", key, formLayout);
			String externalId = curriculum.getExternalId();
			uifactory.addStaticTextElement("curriculum.external.id", externalId, formLayout);
		}
		
		String identifier = curriculum == null ? "" : curriculum.getIdentifier();
		identifierEl = uifactory.addTextElement("curriculum.identifier", "curriculum.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.identifier) && secCallback.canEditCurriculum());
		identifierEl.setMandatory(true);

		String displayName = curriculum == null ? "" : curriculum.getDisplayName();
		displayNameEl = uifactory.addTextElement("curriculum.displayName", "curriculum.displayName", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.displayName) && secCallback.canEditCurriculum());
		displayNameEl.setMandatory(true);
		
		initFormOrganisations(formLayout, ureq.getUserSession());
		
		SelectionValues enabledKeysValues = new SelectionValues();
		enabledKeysValues.add(SelectionValues.entry("on", translate("type.lectures.enabled.enabled")));
		enabledKeysValues.add(SelectionValues.entry("off", translate("type.lectures.enabled.disabled")));
		lecturesEnabledEl = uifactory.addRadiosHorizontal("type.lectures.enabled", formLayout, enabledKeysValues.keys(), enabledKeysValues.values());
		lecturesEnabledEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.lectures) && secCallback.canEditCurriculum());
		lecturesEnabledEl.setVisible(lectureModule.isEnabled());
		if(curriculum != null && curriculum.isLecturesEnabled()) {
			lecturesEnabledEl.select("on", true);
		} else {
			lecturesEnabledEl.select("off", true);
		}
		
		String description = curriculum == null ? "" : curriculum.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("curriculum.description", "curriculum.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.description) && secCallback.canEditCurriculum());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(secCallback.canEditCurriculum()) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.curriculummanager);
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		for(Organisation organisation:organisations) {
			keyList.add(organisation.getKey().toString());
			valueList.add(organisation.getDisplayName());
		}
		
		String selectedOrganisationKey = null;
		if(curriculum != null) {
			if(curriculum.getOrganisation() == null) {
				keyList.add(0, "");
				valueList.add(0, "-");
				selectedOrganisationKey = "";
			} else {
				selectedOrganisationKey = curriculum.getOrganisation().getKey().toString();
				if(!keyList.contains(selectedOrganisationKey)) {
					keyList.add(selectedOrganisationKey);
					valueList.add(curriculum.getOrganisation().getDisplayName());
				}
			}
		}

		organisationEl = uifactory.addDropdownSingleselect("curriculum.organisation", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]));
		organisationEl.setVisible(organisationModule.isEnabled());
		if(selectedOrganisationKey != null && keyList.contains(selectedOrganisationKey)) {
			organisationEl.select(selectedOrganisationKey, true);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextElement(displayNameEl, 64, true);
		allOk &= validateTextElement(identifierEl, 64, true);
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val) && mandatory) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//create a new one
		Organisation organisation;
		if(organisationEl != null && organisationEl.isVisible() && organisationEl.isOneSelected()) {
			if(StringHelper.isLong(organisationEl.getSelectedKey())) {
				organisation = organisationService
						.getOrganisation(new OrganisationRefImpl(Long.valueOf(organisationEl.getSelectedKey())));
			} else {
				organisation = null;
			}
		} else {
			organisation = organisationService.getDefaultOrganisation();
		}
		
		boolean lecturesEnabled = lecturesEnabledEl.isOneSelected() && "on".equals(lecturesEnabledEl.getSelectedKey());
		
		if(curriculum == null) {
			curriculum = curriculumService
					.createCurriculum(identifierEl.getValue(), displayNameEl.getValue(), descriptionEl.getValue(),
							lecturesEnabled, organisation);
			curriculumService.addMember(curriculum, getIdentity(), CurriculumRoles.curriculummanager);
		} else {
			curriculum = curriculumService.getCurriculum(curriculum);
			curriculum.setIdentifier(identifierEl.getValue());
			curriculum.setDisplayName(displayNameEl.getValue());
			curriculum.setDescription(descriptionEl.getValue());
			curriculum.setLecturesEnabled(lecturesEnabled);
			curriculum.setOrganisation(organisation);
			curriculum = curriculumService.updateCurriculum(curriculum);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}