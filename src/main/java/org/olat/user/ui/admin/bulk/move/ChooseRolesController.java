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
package org.olat.user.ui.admin.bulk.move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationMembershipStats;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseRolesController extends StepFormBasicController {
	
	private MultipleSelectionElement rolesEl;
	private SingleSelection targetOrganisationEl;
	
	private final UserBulkMove userBulkMove;
	private final List<Identity> identities;
	private final Organisation organisation;
	private Map<Long,Organisation> allOrganisationsMap;
	private final List<Organisation> targetOrganisations;
	private final List<OrganisationMembershipStats> statistics;
	
	@Autowired
	private OrganisationService organisationService;
	
	public ChooseRolesController(UserRequest ureq, WindowControl wControl, Form form, UserBulkMove userBulkMove, StepsRunContext stepsRunContext) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_DEFAULT, "");
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		
		this.userBulkMove = userBulkMove;
		identities = userBulkMove.getIdentities();
		organisation = userBulkMove.getOrganisation();
		List<IdentityRef> identityRefs = new ArrayList<>(identities);
		statistics = organisationService.getOrganisationStatistics(organisation, identityRefs);
		List<Organisation> allOrganisations = organisationService.getOrganisations();
		allOrganisationsMap = allOrganisations.stream()
				.collect(Collectors.toMap(Organisation::getKey, o -> o, (u, v) -> u));
		Roles roles = ureq.getUserSession().getRoles();
		targetOrganisations = organisationService.getOrganisations(getIdentity(), roles,
				OrganisationRoles.administrator, OrganisationRoles.principal,
				OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues rolesKeyValues = new SelectionValues();
		for(OrganisationMembershipStats stats:statistics) {
			if(stats.getRole() != null) {
				String roleKey = stats.getRole().name();
				String roleValue = translate("role.".concat(stats.getRole().name())) + " ( " + stats.getNumOfMembers() + " )";
				rolesKeyValues.add(SelectionValues.entry(roleKey, roleValue));
			}
		}
		rolesEl = uifactory.addCheckboxesVertical("roles", formLayout, rolesKeyValues.keys(), rolesKeyValues.values(), 1);
		
		uifactory.addStaticTextElement("source.organisation", organisation.getDisplayName(), formLayout);

		SelectionValues organisationsKeyValues = new SelectionValues();
		for(Organisation target:targetOrganisations) {
			String organisationKey = target.getKey().toString();
			String parentLine = organisationWithParentLine(target);
			organisationsKeyValues.add(SelectionValues.entry(organisationKey, parentLine));
		}
		organisationsKeyValues.sort(SelectionValues.VALUE_ASC);
		targetOrganisationEl = uifactory.addDropdownSingleselect("target.organisation", formLayout, organisationsKeyValues.keys(), organisationsKeyValues.values());
	}
	
	private String organisationWithParentLine(Organisation org) {
		StringBuilder parentLine = new StringBuilder(128);
		List<OrganisationRef> parentRefs = org.getParentLine();
		for(OrganisationRef parentRef:parentRefs) {
			Organisation parentOrg = allOrganisationsMap.get(parentRef.getKey());
			if(parentOrg != null) {
				parentLine.append(" / ").append(parentOrg.getDisplayName());
			}
		}
		parentLine.append(" / ").append(org.getDisplayName());
		return parentLine.toString();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		targetOrganisationEl.clearError();
		if(!targetOrganisationEl.isOneSelected()) {
			targetOrganisationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		rolesEl.clearError();
		if(rolesEl.getSelectedKeys().isEmpty()) {
			rolesEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String targetKey = targetOrganisationEl.getSelectedKey();
		if(StringHelper.isLong(targetKey)) {
			Organisation target = organisationService.getOrganisation(new OrganisationRefImpl(Long.valueOf(targetKey)));
			userBulkMove.setTargetOrganisation(target);
		}
		
		Collection<String> selectedRoles = rolesEl.getSelectedKeys();
		List<OrganisationRoles> roles = new ArrayList<>(selectedRoles.size());
		for(String selectedRole:selectedRoles) {
			if(OrganisationRoles.isValue(selectedRole)) {
				roles.add(OrganisationRoles.valueOf(selectedRole));
			}
		}
		userBulkMove.setRoles(roles);
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
