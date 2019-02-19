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
package org.olat.user.ui.role;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditRelationRoleController extends FormBasicController {
	
	private TextElement roleEl;
	private MultipleSelectionElement rightsEl;
	
	private RelationRole relationRole;
	private List<RelationRight> rights;
	
	@Autowired
	private IdentityRelationshipService identityRelationsService;

	public EditRelationRoleController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}
	
	public EditRelationRoleController(UserRequest ureq, WindowControl wControl, RelationRole relationRole) {
		super(ureq, wControl, Util.createPackageTranslator(UserModule.class, ureq.getLocale()));
		rights = identityRelationsService.getAvailableRights();
		this.relationRole = relationRole;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = relationRole == null ? null : relationRole.getRole();
		roleEl = uifactory.addTextElement("role.name", 128, name, formLayout);
		roleEl.setEnabled(!RelationRoleManagedFlag.isManaged(relationRole, RelationRoleManagedFlag.name));
		
		String[] rightKeys = new String[rights.size()];
		String[] rightValues = new String[rights.size()];
		for(int i=rights.size(); i-->0; ) {
			rightKeys[i] = rights.get(i).getRight();
			rightValues[i] = identityRelationsService.getTranslatedName(rights.get(i), getLocale());
		}
		rightsEl = uifactory.addCheckboxesVertical("role.rights", formLayout, rightKeys, rightValues, 2);
		rightsEl.setEnabled(!RelationRoleManagedFlag.isManaged(relationRole, RelationRoleManagedFlag.rights));
		if(relationRole != null) {
			Set<RelationRoleToRight> roleToRights = relationRole.getRights();
			for(RelationRoleToRight roleToRight:roleToRights) {
				String right = roleToRight.getRight().getRight();
				for(String rightKey:rightKeys) {
					if(rightKey.equals(right)) {
						rightsEl.select(rightKey, true);
					}
				}
			}	
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		roleEl.clearError();
		if(!StringHelper.containsNonWhitespace(roleEl.getValue())) {
			roleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selectedRightKeys = rightsEl.getSelectedKeys();
		List<RelationRight> selectedRights = rights.stream()
				.filter(r -> selectedRightKeys.contains(r.getRight())).collect(Collectors.toList());
		if(relationRole == null) {
			identityRelationsService.createRole(roleEl.getValue(), selectedRights);
		} else {
			identityRelationsService.updateRole(relationRole, selectedRights);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
