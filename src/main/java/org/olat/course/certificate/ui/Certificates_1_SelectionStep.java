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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
//TODO uh delete?
public class Certificates_1_SelectionStep extends BasicStep {
	
	private final boolean hasAssessableNodes;
	private final RepositoryEntry courseEntry;
	private final List<AssessedIdentityWrapper> datas;
	
	public Certificates_1_SelectionStep(UserRequest ureq, RepositoryEntry courseEntry,
			List<AssessedIdentityWrapper> datas, boolean hasAssessableNodes) {
		super(ureq);
		this.datas = datas;
		this.courseEntry = courseEntry;
		this.hasAssessableNodes = hasAssessableNodes;
		setNextStep(new Certificates_2_OverviewStep(ureq, hasAssessableNodes));
		setI18nTitleAndDescr("certificates.wizard.select", "certificates.wizard.select");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		CertificatesSelectionController selectCtrl = new CertificatesSelectionController(ureq, wControl, form, runContext,
				courseEntry, datas, hasAssessableNodes);
		return selectCtrl;
	}
}
