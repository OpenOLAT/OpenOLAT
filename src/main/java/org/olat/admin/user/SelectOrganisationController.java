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
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.user.ui.organisation.element.OrgSelectorElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectOrganisationController extends FormBasicController {
	
	private OrgSelectorElement organisationEl;
	
	private final List<Organisation> organisations;
	private final Identity editedIdentity;
	
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;

	public SelectOrganisationController(UserRequest ureq, WindowControl wControl, List<Organisation> organisations, Identity editedIdentity) {
		super(ureq, wControl);
		this.organisations = new ArrayList<>(organisations);
		this.editedIdentity = editedIdentity;
		
		initForm(ureq);
		updateEmailDomainUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		organisationEl = uifactory.addOrgSelectorElement("select.organisation", formLayout,
				getWindowControl(), organisations);
		if (!organisations.isEmpty()) {
			organisationEl.setSelection(organisations.get(0).getKey());
			if (editedIdentity != null && organisationModule.isEmailDomainEnabled()) {
				organisationEl.addActionListener(FormEvent.ONCHANGE);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("add", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public Organisation getSelectedOrganisation() {
		Organisation organisation = null;
		if(organisationEl.isExactlyOneSelected()) {
			Long selection = organisationEl.getSingleSelection();
			for(Organisation org:organisations) {
				if(selection != null && selection.equals(org.getKey())) {
					organisation = org;
				}
			}
		}
		return organisation;
	}

	private void updateEmailDomainUI() {
		if (editedIdentity != null && organisationModule.isEmailDomainEnabled() && organisationEl.isExactlyOneSelected()) {
			List<OrganisationEmailDomain> emailDomains = organisationService.getEnabledEmailDomains(() -> organisationEl.getSingleSelection());
			boolean emailDomainAllowed = organisationService.isEmailDomainAllowed(emailDomains, editedIdentity.getUser().getEmail());
			if (!emailDomainAllowed) {
				organisationEl.setWarningKey("error.email.domain.not.allowed");
			} else {
				organisationEl.clearWarning();
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organisationEl) {
			updateEmailDomainUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationEl.clearError();
		if(!organisationEl.isExactlyOneSelected()) {
			organisationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
