/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.user;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Dez 20, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChangeOrganisationController extends FormBasicController {

	private final List<OrganisationEmailDomain> matchingMailDomains;
	private final String changedEmail;
	private final Identity identityToModify;


	private FormSubmit submitButton;
	private MultipleSelectionElement confirmEl;
	private SingleSelection orgSelection;


	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BaseSecurity securityManager;

	public ChangeOrganisationController(UserRequest ureq, WindowControl wControl,
										List<OrganisationEmailDomain> matchingMailDomains,
										String changedEmail, Identity identityToModify) {
		super(ureq, wControl, "confirm_org_change");
		this.matchingMailDomains = matchingMailDomains;
		this.changedEmail = changedEmail;
		this.identityToModify = identityToModify;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String desc;
		if (matchingMailDomains.size() == 1) {
			String newOrgName = matchingMailDomains.get(0).getOrganisation().getDisplayName();
			desc = translate("change.org.desc.single", newOrgName, changedEmail);
			setFormTranslatedDescription(desc);
		} else {
			desc = translate("change.org.desc.multi", changedEmail);

			SelectionValues orgValues = new SelectionValues();
			matchingMailDomains.forEach(domain ->
					orgValues.add(SelectionValues.entry(
							domain.getOrganisation().getKey().toString(),
							domain.getOrganisation().getDisplayName()
					))
			);

			orgSelection = uifactory.addDropdownSingleselect("changeDropdown", "change.org.label", formLayout, orgValues.keys(), orgValues.values());
			orgSelection.enableNoneSelection(translate("change.org.label.select"));
			orgSelection.setMandatory(true);
			flc.contextPut("changeDropDown", orgSelection);
		}
		uifactory.addStaticTextElement("desc", null, desc, formLayout);

		if (formLayout instanceof FormLayoutContainer formLayoutCont) {
			buildWarnMessage(formLayoutCont);
		}

		confirmEl = uifactory.addCheckboxesHorizontal("confirmation", formLayout, new String[]{translate("change.org.confirm")}, new String[]{translate("change.org.confirm.desc")});
		confirmEl.addActionListener(FormEvent.ONCHANGE);

		submitButton = uifactory.addFormSubmitButton("confirm.email.in.process", formLayout);
		submitButton.setEnabled(false);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void buildWarnMessage(FormLayoutContainer formLayoutCont) {
		Roles roles = securityManager.getRoles(identityToModify);

		if (!roles.getOrganisations().isEmpty()) {
			formLayoutCont.contextPut("hasRoles", true);
			String msg = translate("change.org.warning.desc", WebappHelper.getMailConfig("mailSupport"));
			formLayoutCont.contextPut("msg", msg);

			formLayoutCont.contextPut("rolesDesc", translate("change.org.removing.roles"));

			ChangeOrgRoleRenderer roleRenderer = new ChangeOrgRoleRenderer();
			String renderedOrgRoles = roleRenderer.renderRolesAsHtml(roles, securityManager, organisationService, identityToModify, getLocale());
			formLayoutCont.contextPut("roles", renderedOrgRoles);
		} else {
			formLayoutCont.contextRemove("hasRoles");
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// edge case, submitting without checking confirmEl is not possible
		// because submit is disabled until confirmEl is selected
		// double check in case someone altered the html
		confirmEl.clearError();
		if (!confirmEl.isAtLeastSelected(1)) {
			confirmEl.setErrorKey("change.org.confirm.error");
			allOk = false;
		}

		if (orgSelection != null) {
			orgSelection.clearError();
			if (!orgSelection.isOneSelected()) {
				orgSelection.setErrorKey("change.org.selection.error");
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == confirmEl) {
			submitButton.setEnabled(confirmEl.isAtLeastSelected(1));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new ChangeMailEvent(ChangeMailEvent.CHANGED_ORG_EVENT, changedEmail));
	}


	public SingleSelection getOrgSelection() {
		return orgSelection;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
