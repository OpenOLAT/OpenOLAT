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
package org.olat.user.ui.organisation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationEmailDomainController extends FormBasicController implements Controller {

	private ObjectSelectionElement organisationEl;
	private TextElement domainEl;
	private FormToggle enabledEl;
	private FormToggle subdomainsAllowedEl;

	private OrganisationEmailDomain emailDomain;
	private final List<Organisation> organisations;
	
	@Autowired
	private OrganisationService organisationService;

	protected OrganisationEmailDomainController(UserRequest ureq, WindowControl wControl, Organisation organisation,
			OrganisationEmailDomain emailDomain) {
		super(ureq, wControl);
		this.emailDomain = emailDomain;
		organisations = organisation != null? List.of(organisation): organisationService.getOrganisations();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Collection<? extends OrganisationRef> domainOrganisation = emailDomain != null? List.of(emailDomain.getOrganisation()): List.of();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				domainOrganisation,
				() -> organisations);
		organisationEl = uifactory.addObjectSelectionElement("organisations", "organisation.email.domain.organisation", formLayout,
				getWindowControl(), false, organisationSource);
		organisationEl.setMandatory(true);
		organisationEl.setEnabled(emailDomain == null && organisations.size() > 1);
		
		String domain = emailDomain != null? emailDomain.getDomain(): null;
		domainEl = uifactory.addTextElement("organisation.email.domain.domain", 255, domain, formLayout);
		domainEl.setMandatory(true);
		
		enabledEl = uifactory.addToggleButton("enabled", "organisation.email.domain.enabled", translate("on"), translate("off"), formLayout);
		enabledEl.setEnabled(emailDomain == null);
		if (emailDomain == null || emailDomain.isEnabled()) {
			enabledEl.toggleOn();
		}
		
		subdomainsAllowedEl = uifactory.addToggleButton("subdomainsAllowed",
				"organisation.email.domain.subdomains.allowed", translate("on"), translate("off"), formLayout);
		if (emailDomain != null && emailDomain.isSubdomainsAllowed()) {
			subdomainsAllowedEl.toggleOn();
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		organisationEl.clearError();
		if (organisationEl.isEnabled() && organisationEl.getSelectedKey() == null) {
			organisationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		domainEl.clearError();
		if (!StringHelper.containsNonWhitespace(domainEl.getValue())) {
			domainEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if (emailDomain == null || !Objects.equals(emailDomain.getDomain(), domainEl.getValue())) {
			if (organisationEl.getSelectedKey() != null) {
				OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
				searchParams.setOrganisations(OrganisationSelectionSource.toRefs(organisationEl.getSelectedKeys()));
				searchParams.setDomains(List.of(domainEl.getValue().toLowerCase()));
				if (!organisationService.getEmailDomains(searchParams).isEmpty()) {
					domainEl.setErrorKey("organisation.email.domain.error.duplicate");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (emailDomain == null) {
			Long organisationKey = Long.valueOf(organisationEl.getSelectedKey());
			Organisation organisation = organisations.stream().filter(org -> organisationKey.equals(org.getKey())).findFirst().get();
			emailDomain = organisationService.createOrganisationEmailDomain(organisation, domainEl.getValue());
		}
		
		emailDomain.setDomain(domainEl.getValue().toLowerCase());
		emailDomain.setEnabled(enabledEl.isOn());
		emailDomain.setSubdomainsAllowed(subdomainsAllowedEl.isOn());
		
		emailDomain = organisationService.updateOrganisationEmailDomain(emailDomain);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
