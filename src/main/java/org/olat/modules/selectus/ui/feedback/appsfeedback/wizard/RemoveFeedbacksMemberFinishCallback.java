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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 7 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveFeedbacksMemberFinishCallback implements StepRunnerCallback {
	
	private final Translator translator;
	private final RemoveMembersContext feedbackMembersContext;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public RemoveFeedbacksMemberFinishCallback(RemoveMembersContext feedbackMembersContext, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.feedbackMembersContext = feedbackMembersContext;
		this.translator = translator;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		List<Identity> selectedMembers = feedbackMembersContext.getSelectedMembers();
		List<ApplicationFeedback> feedbacks = feedbackMembersContext.getFeedbacks();
		for(ApplicationFeedback feedback:feedbacks) {
			Identity id = feedback.getIdentity();
			if(selectedMembers.contains(id)) {
				deleteFeedback(feedback, ureq.getIdentity());
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void deleteFeedback(ApplicationFeedback feedback, Identity doer) {
		feedback = feedbackService.getApplicationFeedback(feedback);
		String before = auditService.toAuditXml(feedback);
		
		Identity member = feedback.getIdentity();
		Application application = feedback.getApplication();
		
		String messageI18n = "audit.log.member.feedback.remove.member";
		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(member, translator.getLocale()),		// 0
			salutationGenerator.getTitleFullname(application, translator.getLocale()),		// 1
			application.getId().toString(),													// 2
			feedbackMembersContext.getPosition().getMLShortTitle(translator.getLocale())	// 3
		};
		auditService.auditFeedbackLog(Action.remove, before, null, messageI18n, messageArgs,
				translator, feedbackMembersContext.getPosition(), application, null, doer);
		
		feedbackService.deleteApplicationFeedback(feedback);
	}
}
