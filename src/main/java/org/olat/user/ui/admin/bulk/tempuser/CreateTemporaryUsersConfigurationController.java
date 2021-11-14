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
package org.olat.user.ui.admin.bulk.tempuser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateTemporaryUsersConfigurationController extends StepFormBasicController {
	
	private TextElement numOfUsersEl;
	private TextElement usernamePrefixEl;
	private TextElement firstNamePrefixEl;
	private TextElement lastNamePrefixEl;
	private DateChooser expirationDateEl;
	private SingleSelection organisationEl;
	
	private final CreateTemporaryUsers createTemporaryUsers;
	
	@Autowired
	private OrganisationService organisationService;
	
	public CreateTemporaryUsersConfigurationController(UserRequest ureq, WindowControl wControl, Form form,
			CreateTemporaryUsers createTemporaryUsers, StepsRunContext stepsRunContext) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_DEFAULT, "");
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		this.createTemporaryUsers = createTemporaryUsers;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.rolesmanager, OrganisationRoles.usermanager);
		List<Organisation> organisationList = new ArrayList<>(organisations);
		Collections.sort(organisationList, new OrganisationNameComparator(getLocale()));
		
		SelectionValues values = new SelectionValues();
		for(Organisation organisation:organisationList) {
			values.add(SelectionValues.entry(organisation.getKey().toString(), organisation.getDisplayName()));
		}
		if(values.isEmpty()) {
			Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
			values.add(SelectionValues.entry(defaultOrganisation.getKey().toString(), defaultOrganisation.getDisplayName()));
		}
		organisationEl = uifactory.addDropdownSingleselect("organisation", formLayout, values.keys(), values.values());
		if(createTemporaryUsers.getOrganisation() != null
				&& values.containsKey(createTemporaryUsers.getOrganisation().getKey().toString())) {
			organisationEl.select(createTemporaryUsers.getOrganisation().getKey().toString(), true);
		} else {
			organisationEl.select(values.keys()[0], true);
		}
		organisationEl.setVisible(values.size() > 1);	
		
		numOfUsersEl = uifactory.addTextElement("num.users", 8, "", formLayout);
		numOfUsersEl.setMandatory(true);
		usernamePrefixEl = uifactory.addTextElement("users.username.prefix", 32, "", formLayout);
		usernamePrefixEl.setMandatory(true);
		firstNamePrefixEl = uifactory.addTextElement("users.firstname.prefix", 32, "", formLayout);
		firstNamePrefixEl.setMandatory(true);
		lastNamePrefixEl = uifactory.addTextElement("users.lastname.prefix", 32, "", formLayout);
		lastNamePrefixEl.setMandatory(true);
		expirationDateEl = uifactory.addDateChooser("users.expiration", null, formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateInteger(numOfUsersEl);
		allOk &= validateMandatory(usernamePrefixEl);
		allOk &= validateMandatory(firstNamePrefixEl);
		allOk &= validateMandatory(lastNamePrefixEl);
		return allOk;
	}
	

	private boolean validateMandatory(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		String val = el.getValue();
		if(!StringHelper.containsNonWhitespace(val)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}
		
		return allOk;
	}
	
	private boolean validateInteger(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		String val = el.getValue();
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				int value = Integer.parseInt(val);
				if(value < 1) {
					el.setErrorKey("form.error.nointeger", null);
					allOk = false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("form.error.nointeger", null);
				allOk = false;
			}
		} else {
			el.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		}

		return allOk;
	}
	
	private Organisation getSelectedOrganisation() {
		if(organisationEl.isOneSelected()) {
			String selectedKey = organisationEl.getSelectedKey();
			return organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(selectedKey)));
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Organisation organisation = getSelectedOrganisation();
		createTemporaryUsers.setOrganisation(organisation);
		
		createTemporaryUsers.setNumberOfUsers(Integer.parseInt(numOfUsersEl.getValue()));
		createTemporaryUsers.setUsernamePrefix(usernamePrefixEl.getValue());
		createTemporaryUsers.setFirstNamePrefix(firstNamePrefixEl.getValue());
		createTemporaryUsers.setLastNamePrefix(lastNamePrefixEl.getValue());
		createTemporaryUsers.setExpirationDate(expirationDateEl.getDate());
		createTemporaryUsers.generateUsers();

		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
