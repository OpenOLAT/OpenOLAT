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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentitiesTableDataModel;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesWizardController extends BasicController {
	
	private Link startButton;
	private StepsMainRunController wizardCtrl;
	
	private final boolean hasAssessableNodes;
	private final RepositoryEntry courseEntry;
	private final AssessedIdentitiesTableDataModel dataModel;
	
	@Autowired
	private CertificatesManager certificatesManager;
	
	public CertificatesWizardController(UserRequest ureq, WindowControl wControl,
			AssessedIdentitiesTableDataModel dataModel, RepositoryEntry courseEntry, boolean hasAssessableNodes) {
		super(ureq, wControl);
		
		this.dataModel = dataModel;
		this.courseEntry = courseEntry;
		this.hasAssessableNodes = hasAssessableNodes;
		
		startButton = LinkFactory.createButton("generate.certificate", null, this);
		startButton.setTranslator(getTranslator());
		putInitialPanel(startButton);
		getInitialComponent().setSpanAsDomReplaceable(true);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(startButton == source) {
			doStartWizard(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(wizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(wizardCtrl);
				wizardCtrl = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void doStartWizard(UserRequest ureq) {
		List<AssessedIdentityWrapper> datas = dataModel.getObjects();
		Certificates_1_SelectionStep start = new Certificates_1_SelectionStep(ureq, courseEntry, datas, hasAssessableNodes);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				@SuppressWarnings("unchecked")
				List<CertificateInfos> assessedIdentitiesInfos = (List<CertificateInfos>)runContext.get("infos");
				if(assessedIdentitiesInfos != null && assessedIdentitiesInfos.size() > 0) {
					doGenerateCertificates(assessedIdentitiesInfos);
					return StepsMainRunController.DONE_MODIFIED;
				}
				return StepsMainRunController.DONE_UNCHANGED;
			}
		};
		
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("certificates.wizard.title"), "o_sel_certificates_wizard");
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doGenerateCertificates(List<CertificateInfos> assessedIdentitiesInfos) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Long templateKey = course.getCourseConfig().getCertificateTemplate();
		CertificateTemplate template = null;
		if(templateKey != null) {
			template = certificatesManager.getTemplateById(templateKey);
		}
		
		certificatesManager.generateCertificates(assessedIdentitiesInfos, courseEntry, template, true);
	}
}