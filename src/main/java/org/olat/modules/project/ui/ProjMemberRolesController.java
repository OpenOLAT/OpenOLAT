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
package org.olat.modules.project.ui;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjectRole;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberRolesController extends FormBasicController {

	private MultipleSelectionElement rolesEl;
	
	private final Set<ProjectRole> initialRoles;
	private final boolean ownerAllowed;

	public ProjMemberRolesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Set<ProjectRole> initialRoles, boolean ownerAllowed) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		this.initialRoles = initialRoles;
		this.ownerAllowed = ownerAllowed;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues rolesSV = new SelectionValues();
		Arrays.stream(ProjectRole.values())
				.forEach(role -> rolesSV.add(SelectionValues.entry(
						role.name(),
						ProjectUIFactory.translateRole(getTranslator(), role))));
		rolesSV.remove(ProjectRole.invitee.name());
		if (!ownerAllowed) {
			rolesSV.remove(ProjectRole.owner.name());
		}
		rolesEl = uifactory.addCheckboxesVertical("roles", formLayout, rolesSV.keys(), rolesSV.values(), 1);
		if (initialRoles != null && !initialRoles.isEmpty()) {
			initialRoles.stream()
					.filter(role -> rolesEl.getKeys().contains(role.name()))
					.forEach(role -> rolesEl.select(role.name(), true));
		}
	}

	public Set<ProjectRole> getRoles() {
		return rolesEl.getSelectedKeys().stream().map(ProjectRole::valueOf).collect(Collectors.toSet());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		rolesEl.clearError();
		if (rolesEl.getSelectedKeys().isEmpty()) {
			rolesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
