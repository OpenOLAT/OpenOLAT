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
package org.olat.admin.user.bulkChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * first step: select attributes, which should be changed
 * 
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
class UserBulkChangeStep01 extends BasicStep {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final UserBulkChanges userBulkChanges;

	public UserBulkChangeStep01(UserRequest ureq, UserBulkChanges userBulkChanges) {
		super(ureq);
		this.userBulkChanges = userBulkChanges;
		setI18nTitleAndDescr("step1.description", null);
		setNextStep(new UserBulkChangeStep01a(ureq, userBulkChanges));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new UserBulkChangeStepForm01(ureq, windowControl, form, stepsRunContext);
	}

	private final class UserBulkChangeStepForm01 extends StepFormBasicController {

		private SingleSelection organisationEl;
		private final List<RoleChange> roleChanges = new ArrayList<>();
		
		private int counter = 0;
		private final String[] addremove;
		private final String[] addremoveTranslated;
		
		@Autowired
		private UserManager userManager;
		@Autowired
		private OrganisationService organisationService;

		public UserBulkChangeStepForm01(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			// use custom translator with fallback to user properties translator
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
			flc.setTranslator(getTranslator());
			
			addremove = new String[] { "add", "remove" };
			addremoveTranslated = new String[] { translate("role.add"), translate("role.remove") };
			
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			boolean validChange = userBulkChanges.isValidChange();
			Map<OrganisationRoles, String> roleChangeMap = userBulkChanges.getRoleChangeMap();
			for(RoleChange change:roleChanges) {
				if(change.set().isOneSelected()) {
					roleChangeMap.put(change.role(), change.set().getSelectedKey());
					validChange = true;
				}
			}
			
			if(organisationEl != null && organisationEl.isOneSelected()) {
				Long organisationkey = Long.valueOf(organisationEl.getSelectedKey());
				userBulkChanges.setOrganisation(new OrganisationRefImpl(organisationkey));
			}

			userBulkChanges.setValidChange(validChange);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
			if(source instanceof MultipleSelectionElement check
					&& check.getUserObject() instanceof RoleChange change) {
				change.set().setVisible(change.check().isAtLeastSelected(1));
			} else if(source instanceof SingleSelection actionEl
					&& actionEl.getUserObject() instanceof RoleChange change) {
				if(OrganisationRoles.user.equals(change.role())) {
					if(actionEl.isOneSelected() && "remove".equals(actionEl.getSelectedKey())) {
						actionEl.setWarningKey("warning.remove.user");
					} else {
						actionEl.setWarningKey(null);
					}
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			// always true, because no changes are required
			return true;
		}
		
		private void initRole(OrganisationRoles role, FormItemContainer formLayout) {
			MultipleSelectionElement chkRole = uifactory.addCheckboxesHorizontal("rolechk_" + (++counter), "table.role." + role.name(), formLayout, onKeys, onValues);
			chkRole.addActionListener(FormEvent.ONCHANGE);
			chkRole.setElementCssClass("o_sel_role_" + role.name());

			SingleSelection setRole = uifactory.addDropdownSingleselect("roleset_" + (++counter), null, formLayout, addremove, addremoveTranslated, null);
			setRole.setElementCssClass("o_sel_role_" + role.name());
			setRole.addActionListener(FormEvent.ONCHANGE);
			setRole.setVisible(false);
			
			RoleChange change = new RoleChange(chkRole, setRole, role);
			roleChanges.add(change);
			chkRole.setUserObject(change);
			setRole.setUserObject(change);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("step1.description");
			setFormInfo("step1.content", new String[] { Integer.toString(userBulkChanges.getNumOfIdentitiesToEdit()) });

			// Main layout is a vertical layout without left side padding. To format
			// the checkboxes properly we need a default layout for the remaining form
			// elements
			FormItemContainer innerFormLayout = uifactory.addDefaultFormLayout("innerFormLayout", null, formLayout);
			innerFormLayout.setElementCssClass("o_sel_user_roles");
			
			if(userBulkChanges.getOrganisation() == null) {
				SelectionValues orgKeyValues = getOrganisationKeyValues(ureq);
				if(!orgKeyValues.isEmpty()) {
					String[] keys = orgKeyValues.keys();
					organisationEl = uifactory.addDropdownSingleselect("organisations", "organisations", innerFormLayout,
							keys, orgKeyValues.values());
					organisationEl.select(keys[0], true);
				}
			} else {
				Organisation organisation = organisationService.getOrganisation(userBulkChanges.getOrganisation());
				if(organisation != null) {
					uifactory.addStaticTextElement("organisations", organisation.getDisplayName(), innerFormLayout);
				}
			}

			// check user rights:
			Roles roles = ureq.getUserSession().getRoles();
			if(roles.isUserManager() || roles.isRolesManager() || roles.isAdministrator()) {
				initRole(OrganisationRoles.user, innerFormLayout);
				initRole(OrganisationRoles.author, innerFormLayout);
			}
			if(roles.isRolesManager() || roles.isAdministrator()) {
				OrganisationRoles[] roleArr = new OrganisationRoles[] {
						OrganisationRoles.usermanager, OrganisationRoles.rolesmanager,
						OrganisationRoles.groupmanager, OrganisationRoles.learnresourcemanager,
						OrganisationRoles.poolmanager, OrganisationRoles.curriculummanager,
						OrganisationRoles.lecturemanager, OrganisationRoles.qualitymanager,
						OrganisationRoles.projectmanager, OrganisationRoles.linemanager,
						OrganisationRoles.principal, OrganisationRoles.administrator
				};

				for(OrganisationRoles role:roleArr) {
					initRole(role, innerFormLayout);
				}
			}
		}
		
		private SelectionValues getOrganisationKeyValues(UserRequest ureq) {
			Roles roles = ureq.getUserSession().getRoles();
			List<Organisation> organisations = organisationService.getOrganisations(getIdentity(), roles,
					OrganisationRoles.administrator, OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
			if(organisations.size() > 1) {
				Collections.sort(organisations, new OrganisationNameComparator(getLocale()));
			}
			SelectionValues organisationKeyValues = new SelectionValues();
			for(Organisation organisation:organisations) {
				organisationKeyValues.add(SelectionValues.entry(organisation.getKey().toString(), organisation.getDisplayName()));
			}
			return organisationKeyValues;
		}
	}
	
	private record RoleChange(MultipleSelectionElement check, SingleSelection set, OrganisationRoles role) {
		//
	}
}
