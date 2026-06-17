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
package org.olat.modules.selectus.ui.committee.assignment;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;

/**
 * 
 * Initial date: 28 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveAssignmentStepCallback implements StepRunnerCallback {
	
	private AssignmentsData data;
	private final Translator translator;
	private List<ApplicationLight> applications;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private AssignmentService assignmentService;
	
	public RemoveAssignmentStepCallback(List<ApplicationLight> applications, AssignmentsData data, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.data = data;
		this.translator = translator;
		this.applications = applications;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		List<Identity> assignees = data.getAssigneeList();
		assignmentService.removeAssignments(data.getPosition(), applications, assignees, ureq.getIdentity(), translator);
		dbInstance.commit();
		
		if(data.getMailTemplate() != null) {
			MailerResult result = new MailerResult();
			RecruitingMailTemplate mailTemplate = data.getMailTemplate();
			recruitingService.sendAssignmentNotificationMail(data.getPosition(), assignees, mailTemplate, result);
			if(result.getReturnCode() == MailerResult.OK) {
				wControl.setInfo(translator.translate("assignments.mail.send.success"));
			} else {
				String error = result.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
				wControl.setInfo(translator.translate(error, result.failedAddressesToString()));
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
}
