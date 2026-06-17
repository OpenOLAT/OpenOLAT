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
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  29 aug. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DataProtectionStepController extends StepFormBasicController {
	
	private MultipleSelectionElement acceptTermsEl;
	
	private final Position position;
	private final OrganisationUnit organisationSettings;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;

	public DataProtectionStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "dataprotection");
		setTranslator(Util.createPackageTranslator(RecruitingMainController.class, getLocale(), getTranslator()));
		flc.getComponent().setTranslator(getTranslator());
		
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		position = app.getPosition();
		organisationSettings = recruitingService.getOrganisationUnit(position);
		initForm(ureq);
	}
	
	public DataProtectionStepController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "dataprotection");
		setTranslator(Util.createPackageTranslator(RecruitingMainController.class, getLocale(), getTranslator()));
		this.position = position;
		organisationSettings = recruitingService.getOrganisationUnit(position);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] keys = new String[]{"yes"};
		String[] values = new String[]{ translate("apply_application.acceptTerms") };
		acceptTermsEl = uifactory.addCheckboxesHorizontal("acceptTerms", "", formLayout, keys, values);	
		acceptTermsEl.setElementCssClass("o_sel_accept_terms");
		
		String[] i18nArguments = new String[] {
			recruitingModule.getStaffMail(position, organisationSettings),
			recruitingModule.getStaffMail(),
			getOrganisationUtilMail(),
			Settings.createServerURI(),
		};
		formLayout.contextPut("i18nArguments", i18nArguments);
		formLayout.contextPut("positionTitle", position.getMLTitle(getLocale()));
	}
	
	private String getOrganisationUtilMail() {
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		return recruitingModule.getStaffMail(position, organisationSettings);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acceptTermsEl.clearError();
		if(!acceptTermsEl.isMultiselect() || !acceptTermsEl.isSelected(0)) {
			acceptTermsEl.setErrorKey("apply_application.acceptTerms.error");
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Application app = (Application)getFromRunContext(WizardConstants.APPLICATION);
		commitChanges(app);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	public void commitChanges(Application app) {
		app.setAcceptTerms(acceptTermsEl.isAtLeastSelected(1));
	}
}
