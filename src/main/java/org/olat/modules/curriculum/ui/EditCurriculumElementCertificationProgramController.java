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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementCertificationProgramController extends FormBasicController {
	
	private FormToggle enableEl;
	private SingleSelection programsEl;
	private StaticTextElement creditPointsEl;
	
	private final boolean canEdit;
	private final CurriculumElement element;
	private final CertificationProgram certificationProgram;
	
	private final List<CertificationProgram> programsList;
	
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCurriculumElementCertificationProgramController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement element, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.element = element;
		canEdit = secCallback.canEditCurriculumElement(element);
		
		Organisation curriculumOrganisation = curriculum.getOrganisation();
		if(curriculumOrganisation == null || !organisationModule.isEnabled()) {
			curriculumOrganisation = organisationService.getDefaultOrganisation();
		}
		programsList = certificationProgramService.getCertificationPrograms(List.of(curriculumOrganisation));
		certificationProgram = certificationProgramService.getCertificationProgram(element);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		enableEl = uifactory.addToggleButton("certification.program.enable", "curriculum.element.certification.program", translate("on"), translate("off"), formLayout);
		enableEl.toggle(certificationProgram != null);
		
		SelectionValues programsPK = new SelectionValues();
		for(CertificationProgram program:programsList) {
			programsPK.add(SelectionValues.entry(program.getKey().toString(), program.getDisplayName()));
		}
		if(certificationProgram != null && !programsPK.containsKey(certificationProgram.getKey().toString())) {
			programsPK.add(SelectionValues.entry(certificationProgram.getKey().toString(), certificationProgram.getDisplayName()));
		}
		
		programsEl = uifactory.addDropdownSingleselect("certification.program", "certification.program", formLayout,
				programsPK.keys(), programsPK.values());
		programsEl.setEnabled(canEdit);
		if(canEdit) {
			programsEl.addActionListener(FormEvent.ONCHANGE);
		}
		if(certificationProgram != null && programsPK.containsKey(certificationProgram.getKey().toString())) {
			programsEl.select(certificationProgram.getKey().toString(), true);
		} else if(!programsPK.isEmpty()) {
			programsEl.select(programsPK.keys()[0], true);
		}
		
		creditPointsEl = uifactory.addStaticTextElement("certification.program.credit.points", "certification.program.credit.points", formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		programsEl.clearError();
		if(enableEl.isOn() && !programsEl.isOneSelected()) {
			programsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
			doUpdateCertificationProgram();
		} else if(programsEl == null) {
			updateUI();
			doUpdateCertificationProgram();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		programsEl.setVisible(enabled);
		
		CertificationProgram selectedProgram = getSelectedProgram();
		creditPointsEl.setVisible(enabled && selectedProgram != null);
		String points = "";
		if(selectedProgram != null) {
			points = CertificationHelper.creditPointsToString(selectedProgram);
		}
		creditPointsEl.setValue(points);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doUpdateCertificationProgram() {
		certificationProgramService.removeCurriculumElementToCertificationProgram(element);
		if(enableEl.isOn()) {
			CertificationProgram program = getSelectedProgram();
			if(program != null) {
				certificationProgramService.addCurriculumElementToCertificationProgram(program, element);
			}
		}
	}
	
	private CertificationProgram getSelectedProgram() {
		if(programsEl.isVisible() && programsEl.isOneSelected()) {
			String selectedKey = programsEl.getSelectedKey();
			return programsList.stream()
					.filter(program -> selectedKey.equals(program.getKey().toString()))
					.findFirst()
					.orElse(null);
		}
		return null;
	}
}
