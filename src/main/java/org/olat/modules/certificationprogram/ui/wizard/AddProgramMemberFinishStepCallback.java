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
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationCoordinator.RequestMode;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddProgramMemberFinishStepCallback implements StepRunnerCallback {
	
	private final AddProgramMembersContext membersContext;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationCoordinator certificationCoordinator;
	
	public AddProgramMemberFinishStepCallback(AddProgramMembersContext membersContext) {
		CoreSpringFactory.autowireObject(this);
		this.membersContext = membersContext;
	}
	
	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		Date issuedDate = membersContext.getIssuedDate();
		List<UserToCertify> userToCertifyList = membersContext.getSelectedIdentities();
		CertificationProgram certificationProgram = membersContext.getProgram();
		for(UserToCertify userToCertify:userToCertifyList) {
			Identity identity = userToCertify.identity();
			certificationCoordinator.generateCertificate(identity, certificationProgram, issuedDate,
					RequestMode.COACH, CertificationProgramMailType.certificate_issued, ureq.getIdentity());
		}
		dbInstance.commit();
		return StepsMainRunController.DONE_MODIFIED;
	}
}
