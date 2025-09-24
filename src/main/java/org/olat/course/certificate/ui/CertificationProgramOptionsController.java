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
package org.olat.course.certificate.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.run.RunMainController;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.ui.EditCertificationProgramController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramOptionsController extends FormBasicController {
	
	private CertificationProgram certificationProgram;
	
	@Autowired
	private CertificationProgramService certificationProgramService;
	

	public CertificationProgramOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util.createPackageTranslator(RunMainController.class, ureq.getLocale(),
				Util.createPackageTranslator(EditCertificationProgramController.class, ureq.getLocale())));

		List<CertificationProgram> certificationsPrograms = certificationProgramService.getCertificationPrograms(entry);
		if(certificationsPrograms.size() == 1) {
			certificationProgram = certificationsPrograms.get(0);
		}

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.certificate.program.title");
		if(certificationProgram != null) {
			InfoPanelItem infosPanel = uifactory.addInfoPanel("infos", null, formLayout);
			infosPanel.setTitle(translate("info.certification.program.title"));
			infosPanel.setInformations(translate("info.certification.program.text", certificationProgram.getDisplayName()));
			infosPanel.setPersistedStatusId(ureq, "course-certification-program-options-v1");
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {

		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		
	}

	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		//
	}


	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}