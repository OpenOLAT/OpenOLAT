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
package org.olat.user.ui.identity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.user.UserModule;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRolesController extends StepFormBasicController {
	
	private MultipleSelectionElement relationRoleEl;
	
	private List<RelationRole> availableRoles;
	
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	
	public RelationRolesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(UserModule.class, getLocale(), getTranslator()));
		availableRoles = identityRelationsService.getAvailableRoles();
		initForm (ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] roleKeys = new String[availableRoles.size()];
		String[] roleValues = new String[availableRoles.size()];
		
		for(int i=availableRoles.size(); i-->0; ) {
			roleKeys[i] = availableRoles.get(i).getKey().toString();
			roleValues[i] = RelationRolesAndRightsUIFactory.getTranslatedRole(getTranslator(), availableRoles.get(i));
		}
		relationRoleEl = uifactory.addCheckboxesVertical("relation.roles", formLayout, roleKeys, roleValues, 2);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		relationRoleEl.clearError();
		if(relationRoleEl.getSelectedKeys().isEmpty()) {
			relationRoleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Long> selectedKeys = relationRoleEl.getSelectedKeys().stream()
				.map(Long::valueOf)
				.collect(Collectors.toSet());
		List<RelationRole> selectedRoles = availableRoles.stream()
				.filter(role -> selectedKeys.contains(role.getKey()))
				.collect(Collectors.toList());

		addToRunContext("relationRoles", selectedRoles);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
