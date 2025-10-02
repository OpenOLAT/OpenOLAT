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

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureModule;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCurriculumController extends FormBasicController {
	
	private static final String GUIPREF_KEY_CURRICULUM_ORG = "curriculum.organisation";

	private RichTextElement descriptionEl;
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private ObjectSelectionElement organisationEl;
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
		formLayout.setElementCssClass("o_sel_curriculum_form");
		
		if(curriculum != null) {
			String key = curriculum.getKey().toString();
			uifactory.addStaticTextElement("curriculum.key", key, formLayout);
			String externalId = curriculum.getExternalId();
			uifactory.addStaticTextElement("curriculum.external.id", externalId, formLayout);
		}

		String displayName = curriculum == null ? "" : curriculum.getDisplayName();
		displayNameEl = uifactory.addTextElement("curriculum.display.name", "curriculum.display.name", 255, displayName, formLayout);
		displayNameEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.displayName) && secCallback.canEditCurriculum(curriculum));
		displayNameEl.setElementCssClass("o_sel_curriculum_displayname");
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = curriculum == null ? "" : curriculum.getIdentifier();
		identifierEl = uifactory.addTextElement("curriculum.identifier", "curriculum.identifier", 255, identifier, formLayout);
		identifierEl.setElementCssClass("o_sel_curriculum_identifier");
		identifierEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.identifier) && secCallback.canEditCurriculum(curriculum));
		identifierEl.setMandatory(true);
		
		initFormOrganisations(ureq, formLayout);
		
		SelectionValues enabledKeysValues = new SelectionValues();
		enabledKeysValues.add(SelectionValues.entry("on", translate("type.lectures.enabled.enabled")));
		enabledKeysValues.add(SelectionValues.entry("off", translate("type.lectures.enabled.disabled")));
		lecturesEnabledEl = uifactory.addRadiosHorizontal("type.lectures.enabled", formLayout, enabledKeysValues.keys(), enabledKeysValues.values());
		lecturesEnabledEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.lectures) && secCallback.canEditCurriculum(curriculum));
		lecturesEnabledEl.setVisible(lectureModule.isEnabled());
		if(curriculum != null && curriculum.isLecturesEnabled()) {
			lecturesEnabledEl.select("on", true);
		} else {
			lecturesEnabledEl.select("off", true);
		}
		
		String description = curriculum == null ? "" : curriculum.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("curriculum.description", "curriculum.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.description) && secCallback.canEditCurriculum(curriculum));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		if(secCallback.canEditCurriculum(curriculum)) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initFormOrganisations(UserRequest ureq, FormItemContainer formLayout) {
		Roles roles = ureq.getUserSession().getRoles();
		List<Organisation> managedOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.curriculummanager);
				
		Organisation currentOrganisation = null;
		if (curriculum != null) {
			currentOrganisation = curriculum.getOrganisation();
		}
		if (currentOrganisation == null) {
			String organisationKey = getDefaultOrganisationKey(ureq);
			if (organisationKey != null) {
				currentOrganisation =managedOrganisations.stream()
						.filter(organisation -> organisationKey.equals(organisation.getKey().toString()))
						.findFirst()
						.orElseGet(null);
			}
		}
		List<Organisation> currentOrganisations = currentOrganisation != null? List.of(currentOrganisation): List.of();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				currentOrganisations,
				() -> managedOrganisations);
		organisationEl = uifactory.addObjectSelectionElement("organisations", "curriculum.organisation", formLayout,
				getWindowControl(), false, organisationSource);
		organisationEl.setElementCssClass("o_sel_curriculum_organisation");
		organisationEl.setMandatory(true);
		organisationEl.setVisible(organisationModule.isEnabled());
	}
	
	private String getDefaultOrganisationKey(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			Object pref = guiPrefs.get(EditCurriculumController.class, GUIPREF_KEY_CURRICULUM_ORG);
			if (pref instanceof String key) {
				return key;
			}
		}
		return null;
	}
	
	private static void setDefaultOrganisationKey(UserRequest ureq, String key) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(EditCurriculumController.class, GUIPREF_KEY_CURRICULUM_ORG, key);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextElement(displayNameEl, 64, true);
		allOk &= validateTextElement(identifierEl, 64, true);
		allOk &= validateOrg();
		
		return allOk;
	}
	
	private boolean validateOrg() {
		boolean allOk = true;

		organisationEl.clearError();
		if (organisationEl.isVisible() && organisationEl.getSelectedKey() == null) {
			organisationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val) && mandatory) {
			el.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
			el.setErrorKey("input.toolong", Integer.toString(maxLength));
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Organisation organisation = null;
		if (organisationEl.isVisible() && organisationEl.getSelectedKey() != null) {
			OrganisationRef organisationRef = OrganisationSelectionSource.toRef(organisationEl.getSelectedKey());
			organisation = organisationService.getOrganisation(organisationRef);
			if (curriculum == null && organisation != null) {
				setDefaultOrganisationKey(ureq, organisation.getKey().toString());
			}
		}
		if (organisation == null) {
			organisation = organisationService.getDefaultOrganisation();
		}
		
		boolean lecturesEnabled = lecturesEnabledEl.isOneSelected() && "on".equals(lecturesEnabledEl.getSelectedKey());
		
		if(curriculum == null) {
			//create a new one
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
		if (curriculum != null) {
			if (curriculum.getOrganisation() != null) {
				organisationEl.select(curriculum.getOrganisation().getKey().toString());
			}
		}

		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}