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
package org.olat.user.ui.organisation;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationAdminConfigrationController extends FormBasicController {
	
	private FormToggle enableEl;
	private FormLayoutContainer emailDomainCont;
	private FormToggle emailDomainEnableEl;
	private FormLayoutContainer legalFolderCont;
	private FormToggle legalFolderEnableEl;
	private FormLayoutContainer statusCont;
	private FormLink moveRolesLink;


	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OrganisationAdminConfigrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer organisationCont = FormLayoutContainer.createDefaultFormLayout("organisations", getTranslator());
		organisationCont.setElementCssClass("o_sel_org_admin_configuration");
		organisationCont.setFormTitle(translate("organisation.configuration"));
		organisationCont.setFormInfo(translate("organisation.configuration.help"));
		organisationCont.setFormInfoHelp("manual_admin/administration/Modules_Organisations/");
		organisationCont.setRootForm(mainForm);
		formLayout.add(organisationCont);
		
		enableEl = uifactory.addToggleButton("organisation.admin.enabled", "organisation.admin.enabled", translate("on"), translate("off"), organisationCont);
		enableEl.toggle(organisationModule.isEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);
		
		emailDomainCont = FormLayoutContainer.createDefaultFormLayout("emailDomain", getTranslator());
		emailDomainCont.setFormTitle(translate("organisation.email.domains"));
		emailDomainCont.setFormInfo(translate("organisation.email.domains.help"));
		emailDomainCont.setFormInfoHelp("manual_admin/administration/Modules_Organisations/#e-mail_domain_mapping");
		emailDomainCont.setElementCssClass("o_block_top");
		emailDomainCont.setRootForm(mainForm);
		formLayout.add(emailDomainCont);
		
		emailDomainEnableEl = uifactory.addToggleButton("email.domain", "organisation.admin.email.domain.enabled", translate("on"), translate("off"), emailDomainCont);
		emailDomainEnableEl.toggle(organisationModule.isEmailDomainEnabled());
		emailDomainEnableEl.addActionListener(FormEvent.ONCHANGE);

		legalFolderCont = FormLayoutContainer.createDefaultFormLayout("legalFolder", getTranslator());
		legalFolderCont.setFormTitle(translate("organisation.legal.folder"));
		legalFolderCont.setElementCssClass("o_block_top");
		legalFolderCont.setRootForm(mainForm);
		formLayout.add(legalFolderCont);
		
		legalFolderEnableEl = uifactory.addToggleButton("legal.folder", "organisation.admin.legal.folder.enabled", translate("on"), translate("off"), legalFolderCont);
		legalFolderEnableEl.toggle(organisationModule.isLegalFolderEnabled());
		legalFolderEnableEl.addActionListener(FormEvent.ONCHANGE);

		initStatusCont();
	}

	private void initStatusCont() {
		if (flc.hasFormComponent(statusCont)) {
			flc.remove(statusCont);
		}
		statusCont = null;
		statusCont = FormLayoutContainer.createDefaultFormLayout("status", getTranslator());
		statusCont.setFormTitle(translate("organisation.status.title"));
		statusCont.setElementCssClass("o_block_top");
		statusCont.setRootForm(mainForm);
		flc.add(statusCont);

		// Show current default organisation
		String defaultOrgName = organisationService.getDefaultOrganisation().getDisplayName();
		uifactory.addStaticTextElement("organisation.default.label", "organisation.default.label", defaultOrgName, statusCont);

		// Check for multiple default orgs
		if (organisationService.hasMultipleOrganisationsWithSameId(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER)) {
			statusCont.setFormWarning(translate("organisation.status.multiple.default.error"));
		}

		// Check global roles outside default org
		if (!organisationService.getGlobalRolesOutsideDefaultIdentities().isEmpty()) {
			statusCont.setFormWarning(translate("organisation.status.roles.warning", String.valueOf(organisationService.getGlobalRolesOutsideDefaultIdentities().size())));

			moveRolesLink = uifactory.addFormLink("organisation.status.roles.move", statusCont, Link.BUTTON);
			moveRolesLink.setIconLeftCSS("o_icon o_icon-arrows-up-to-line");
		}
	}

	private void updateUI() {
		emailDomainCont.setVisible(enableEl.isOn());
		legalFolderCont.setVisible(enableEl.isOn());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			organisationModule.setEnabled(enableEl.isOn());
			updateUI();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(emailDomainEnableEl == source) {
			organisationModule.setEmailDomainEnabled(emailDomainEnableEl.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(legalFolderEnableEl == source) {
			organisationModule.setLegalFolderEnabled(legalFolderEnableEl.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (moveRolesLink == source) {
			if (organisationService.moveGlobalRolesToDefault(getIdentity())) {
				showInfo("organisation.status.move.success", organisationService.getDefaultOrganisation().getDisplayName());
				initStatusCont();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}
