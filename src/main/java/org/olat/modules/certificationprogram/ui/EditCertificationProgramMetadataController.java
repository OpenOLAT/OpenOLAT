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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCertificationProgramMetadataController extends FormBasicController {
	
	private TextElement identifierEl;
	private TextElement displayNameEl;
	private ObjectSelectionElement organisationsEl;
	
	private CertificationProgram certificationProgram;
	private List<Organisation> certificationProgramOrganisations;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCertificationProgramMetadataController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl);
		this.certificationProgram = certificationProgram;
		initForm(ureq);
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String displayName = certificationProgram.getDisplayName();
		displayNameEl = uifactory.addTextElement("certification.program.name", "certification.program.name", 255, displayName, formLayout);
		displayNameEl.setMandatory(true);
		
		String identifier = certificationProgram.getIdentifier();
		identifierEl = uifactory.addTextElement("certification.program.identifier", "certification.program.identifier", 128, identifier, formLayout);
		
		if(organisationModule.isEnabled()) {
			initFormOrganisations(formLayout, ureq.getUserSession());
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void initFormOrganisations(FormItemContainer formLayout, UserSession usess) {
		Roles roles = usess.getRoles();
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(
				certificationProgramService.getOrganisations(certificationProgram),
				() -> organisationService.getOrganisations(getIdentity(), roles,
						OrganisationRoles.administrator, OrganisationRoles.curriculummanager));
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "certification.admin.access", formLayout,
				getWindowControl(), true, organisationSource);
		organisationsEl.setVisible(organisationModule.isEnabled());
		organisationsEl.setMandatory(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= true;
		} else if(displayNameEl.getValue().length() > 255) {
			displayNameEl.setErrorKey("form.error.toolong", "255");
			allOk &= true;
		}
		
		identifierEl.clearError();
		if(identifierEl.getValue() != null && identifierEl.getValue().length() > 128) {
			identifierEl.setErrorKey("form.error.toolong", "128");
			allOk &= true;
		}
		
		if(organisationsEl != null) {
			organisationsEl.clearError();
			if(organisationsEl.getSelectedKeys().isEmpty()) {
				organisationsEl.setErrorKey("form.legende.mandatory");
				allOk &= true;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		certificationProgram = certificationProgramService.getCertificationProgram(certificationProgram);
		
		certificationProgram.setDisplayName(displayNameEl.getValue());
		certificationProgram.setIdentifier(identifierEl.getValue());
		List<Organisation> organisations = getSelectedOrganisations();
		certificationProgram = certificationProgramService.updateCertificationProgram(certificationProgram, organisations);
		dbInstance.commitAndCloseSession();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	public List<Organisation> getSelectedOrganisations() {
		if(organisationsEl == null || !organisationsEl.isVisible()) {
			return certificationProgramOrganisations;
		}
		
		return organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
	}
}
