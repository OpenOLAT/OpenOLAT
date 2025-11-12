/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemToOrganisation;
import org.olat.modules.creditpoint.ui.component.ExpirationFormItem;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreditPointSystemEditController extends FormBasicController {
	
	private static final String ROLES_RESTRICTIONS_NONE_KEY = "none";
	private static final String ROLES_RESTRICTIONS_MANAGERS_KEY = "managers";
	
	private TextElement nameEl;
	private TextElement labelEl;
	private FormToggle expirationEl;
	private FormToggle organisationsEnableEl;
	private ExpirationFormItem defaultExpirationEl;
	private SingleSelection rolesRestrictionsEl;
	private ObjectSelectionElement organisationsEl;
	
	private CreditPointSystem creditPointSystem;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private OrganisationService organisationService;
	
	public CreditPointSystemEditController(UserRequest ureq, WindowControl wControl, CreditPointSystem creditPointSystem) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.creditPointSystem = creditPointSystem;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer nameCont = uifactory.addDefaultFormLayout("name.and.validity", null, formLayout);
		nameCont.setFormTitle(translate("edit.name.validity.title"));
		
		String name = creditPointSystem == null ? null : creditPointSystem.getName();
		nameEl = uifactory.addTextElement("system.name", 255, name, nameCont);
		nameEl.setMandatory(true);
		
		String label = creditPointSystem == null ? null : creditPointSystem.getLabel();
		labelEl = uifactory.addTextElement("system.label", 16, label, nameCont);
		labelEl.setMandatory(true);
		
		expirationEl = uifactory.addToggleButton("validity.period", "validity.period", translate("on"), translate("off"), nameCont);
		expirationEl.toggle(creditPointSystem != null && creditPointSystem.getDefaultExpiration() != null && creditPointSystem.getDefaultExpirationUnit() != null);

		defaultExpirationEl = new ExpirationFormItem("system.default.expiration", false, getTranslator());
		defaultExpirationEl.setLabel("system.default.expiration", null);
		if(creditPointSystem != null && creditPointSystem.getDefaultExpiration() != null) {
			defaultExpirationEl.setValue(creditPointSystem.getDefaultExpiration().toString());
			defaultExpirationEl.setType(creditPointSystem.getDefaultExpirationUnit());
		}
		defaultExpirationEl.setVisible(expirationEl.isOn());
		nameCont.add(defaultExpirationEl);
		
		FormLayoutContainer restrictionsCont = uifactory.addDefaultFormLayout("restrictions", null, formLayout);
		restrictionsCont.setFormTitle(translate("edit.restrictions.title"));
		restrictionsCont.setFormInfo(translate("edit.restrictions.infos"));
		
		organisationsEnableEl = uifactory.addToggleButton("organisations.restrictions.enabled", "organisations.restrictions.enabled", translate("on"), translate("off"), restrictionsCont);
		organisationsEnableEl.toggle(creditPointSystem != null && creditPointSystem.isOrganisationsRestrictions());
		organisationsEnableEl.setVisible(organisationModule.isEnabled());
		
		Roles roles = ureq.getUserSession().getRoles();
		List<Organisation> selectedOrganisations = creditPointSystem != null
				? creditPointSystem.getOrganisations().stream()
						.map(CreditPointSystemToOrganisation::getOrganisation)
						.toList()
				: List.of();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				selectedOrganisations,
				() -> organisationService.getOrganisations(getIdentity(), roles,
						OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager));
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "organisations.restrictions", restrictionsCont,
				getWindowControl(), true, organisationSource);
		organisationsEl.setVisible(organisationModule.isEnabled() && organisationsEnableEl.isOn());
		organisationsEl.setMandatory(true);

		SelectionValues rolesRestrictionsPK = new SelectionValues();
		rolesRestrictionsPK.add(SelectionValues.entry(ROLES_RESTRICTIONS_NONE_KEY, translate("roles.restrictions.none"), null, null, null, true));
		rolesRestrictionsPK.add(SelectionValues.entry(ROLES_RESTRICTIONS_MANAGERS_KEY, translate("roles.restrictions.managers"), null, "o_icon o_icon_lock", null, true));
		rolesRestrictionsEl = uifactory.addRadiosHorizontal("roles.restrictions", "roles.restrictions", restrictionsCont,
				rolesRestrictionsPK.keys(), rolesRestrictionsPK.values());
		String rolesRestrictionsKey = creditPointSystem != null && creditPointSystem.isRolesRestrictions()
				? ROLES_RESTRICTIONS_MANAGERS_KEY
				: ROLES_RESTRICTIONS_NONE_KEY;
		rolesRestrictionsEl.select(rolesRestrictionsKey, true);
		
		FormLayoutContainer buttonsCont = uifactory.addDefaultFormLayout("buttons.wrapper", null, formLayout);
		FormLayoutContainer buttonsSubCont = uifactory.addButtonsFormLayout("buttons", null, buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsSubCont);
		uifactory.addFormCancelButton("cancel", buttonsSubCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		labelEl.clearError();
		if(!StringHelper.containsNonWhitespace(labelEl.getValue())) {
			labelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		defaultExpirationEl.clearError();
		if(expirationEl.isOn()) {
			if(defaultExpirationEl.isEmpty()) {
				defaultExpirationEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(defaultExpirationEl.getValue() != null) {
				int val = defaultExpirationEl.getValue().intValue();
				if(val <= 0) {
					defaultExpirationEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(expirationEl == source) {
			defaultExpirationEl.setVisible(expirationEl.isOn());
		} else if(organisationsEnableEl == source) {
			organisationsEl.setVisible(organisationsEnableEl.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = nameEl.getValue();
		String label = labelEl.getValue();
		
		boolean expiration = expirationEl.isOn();
		Integer defaultExpiration = expiration ? defaultExpirationEl.getValue() : null;
		CreditPointExpirationType defaultExpirationType = expiration ? defaultExpirationEl.getType() : null;
		boolean organisationsRestrictions = organisationsEnableEl.isOn();
		boolean rolesRestrictions = rolesRestrictionsEl.isOneSelected()
				&& ROLES_RESTRICTIONS_MANAGERS_KEY.equals(rolesRestrictionsEl.getSelectedKey());
		
		if(creditPointSystem == null) {
			creditPointSystem = creditPointService.createCreditPointSystem(name, label,
					defaultExpiration, defaultExpirationType,
					rolesRestrictions, organisationsRestrictions);
		} else {
			creditPointSystem.setName(name);
			creditPointSystem.setLabel(label);
			creditPointSystem.setDefaultExpiration(defaultExpiration);
			creditPointSystem.setDefaultExpirationUnit(defaultExpirationType);
			creditPointSystem.setRolesRestrictions(rolesRestrictions);
			creditPointSystem.setOrganisationsRestrictions(organisationsRestrictions);
			creditPointSystem = creditPointService.updateCreditPointSystem(creditPointSystem);
		}
		
		List<Organisation> organisations = getSelectedOrganisations();
		creditPointService.updateCreditPointSystemOrganisations(creditPointSystem, organisations);
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public List<Organisation> getSelectedOrganisations() {
		if(organisationsEnableEl.isVisible() && !organisationsEl.getSelectedKeys().isEmpty()) {
			List<OrganisationRefImpl> refs = organisationsEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(OrganisationRefImpl::new).toList();
			return organisationService.getOrganisation(refs);
		}
		return new ArrayList<>();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
