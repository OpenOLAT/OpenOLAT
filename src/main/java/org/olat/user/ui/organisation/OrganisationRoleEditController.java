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
package org.olat.user.ui.organisation;

import java.util.List;

import org.olat.admin.privacy.PrivacyAdminController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.RightProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.user.UserModule;
import org.olat.user.ui.role.EditRelationRoleController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationRoleEditController extends FormBasicController {

	private MultipleSelectionElement rightsEl;

	private final Organisation organisation;
	private final OrganisationRoles organisationRole;
	private final List<RightProvider> allRights;
	private final List<RightProvider> selectedRights;
	private final String roleName;

	@Autowired
	OrganisationService organisationService;

	public OrganisationRoleEditController(UserRequest ureq, WindowControl wControl, Organisation organisation, OrganisationRoles organisationRole) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(UserModule.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditRelationRoleController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(PrivacyAdminController.class, getLocale(), getTranslator()));

		this.organisation = organisation;
		this.organisationRole = organisationRole;
		this.roleName = translate("admin.props." + organisationRole.name() + "s");
		this.allRights = organisationService.getAllOrganisationRights(organisationRole);
		this.selectedRights = organisationService.getGrantedOrganisationRights(organisation, organisationRole);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("role.identifier", "role.identifier", roleName, formLayout);

		String[] rightKeys = new String[allRights.size()];
		String[] rightValues = new String[allRights.size()];
		String[] cssClasses = new String[allRights.size()];

		for(int i = 0; i < allRights.size(); i++) {
			rightKeys[i] = allRights.get(i).getRight();
			rightValues[i] = allRights.get(i).getTranslatedName(getLocale());
			cssClasses[i] = allRights.get(i).getParent() != null ? (allRights.get(i).getParent().getParent() != null ? "o_checkbox_indented level_2" : "o_checkbox_indented") : null;
		}

		rightsEl = uifactory.addCheckboxesVertical("role.rights", "role.rights", formLayout, rightKeys, rightValues, cssClasses, null,1);
		rightsEl.addActionListener(FormEvent.ONCLICK);
		selectedRights.stream().map(RightProvider::getRight).forEach(right -> rightsEl.select(right, true));

		checkDependentRights();

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == rightsEl) {
			checkDependentRights();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		organisationService.setGrantedOrganisationRights(organisation, organisationRole, rightsEl.getSelectedKeys());

		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void checkDependentRights() {
		for (RightProvider right:allRights) {
			if (right.getParent() != null) {
				int parentIndex = allRights.indexOf(right.getParent());
				if (rightsEl.isSelected(parentIndex)) {
					rightsEl.setEnabled(right.getRight(), true);
				} else {
					rightsEl.setEnabled(right.getRight(), false);
					rightsEl.select(right.getRight(), false);
				}
			}
		}
	}
}
