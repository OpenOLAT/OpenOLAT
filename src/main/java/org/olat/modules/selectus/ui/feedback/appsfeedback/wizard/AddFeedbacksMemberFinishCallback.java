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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.util.mail.MailerResult;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMember;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMemberStatus;
import org.olat.modules.selectus.ui.committee.wizard.MembersController;

/**
 * 
 * Initial date: 23 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddFeedbacksMemberFinishCallback implements StepRunnerCallback {

	private final Translator translator;
	private final FeedbackMembersContext feedbackMembersContext;

	@Autowired
	private UserManager userManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public AddFeedbacksMemberFinishCallback(FeedbackMembersContext feedbackMembersContext, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		
		this.feedbackMembersContext = feedbackMembersContext;
		this.translator = translator;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		
		List<ApplicationLight> apps = feedbackMembersContext.getSelectedApps();
		List<Identity> members = new ArrayList<>();
		List<CommitteeMember> selectedMembers = feedbackMembersContext.getMembers();
		for(CommitteeMember selectedMember:selectedMembers) {
			if(selectedMember.getStatus() == CommitteeMemberStatus.ok) {
				Identity identity = selectedMember.getIdentity();
				if(identity instanceof TransientIdentity tIdentity) {
					identity = createMember(tIdentity, ureq.getIdentity());
				}
				members.add(identity);
			}
		}
		
		Date deadline = feedbackMembersContext.getDeadline();
		List<ApplicationFeedback> feedbacks = feedbackService.addFeedbacksMembers(apps, members, deadline, feedbackMembersContext.getConfiguration());
		DBFactory.getInstance().commit();
		
		for(ApplicationFeedback feedback:feedbacks) {
			logAddMember(feedback, ureq.getIdentity());
		}
		DBFactory.getInstance().commit();

		if(feedbackMembersContext.isSendMail() && !feedbacks.isEmpty()) {
			MailerResult allResult = new MailerResult();
			// send mails
			for(Identity member:members) {
				List<ApplicationFeedback> membersFeedback = feedbacks.stream()
						.filter(feedback -> feedback.getIdentity().equals(member))
						.collect(Collectors.toList());
				if(membersFeedback.isEmpty()) {
					continue;
				}

				List<ApplicationFeedback> mailedFeedbacks = recruitingService.sendFeedbackContactMail(member, membersFeedback,
						null, feedbackMembersContext.getConfiguration(), apps, feedbackMembersContext.getPosition(),
						feedbackMembersContext.getMailTemplate(), allResult);
				if(mailedFeedbacks.size() == 1) {
					ApplicationFeedback feedback = mailedFeedbacks.get(0);
					logSendMail(member, feedback.getApplication(), feedback, ureq.getIdentity());
				} else {
					logSendMail(member, feedbacks.size(), ureq.getIdentity());
				}
				DBFactory.getInstance().commit();
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void logAddMember(ApplicationFeedback feedback, Identity doer) {
		Identity member = feedback.getIdentity();
		Application application = feedback.getApplication();
		String messageI18n = "audit.log.member.feedback.add.member";

		String appName = salutationGenerator.getTitleFullname(application, translator.getLocale());
		String appId = application.getId() == null ? "" : application.getId().toString();
		String reviewer = RecruitingHelper.formatFullName(member);
		String[] messageArgs = new String[] { appName, appId, reviewer };

		auditService.auditFeedbackMemberLog(Action.add, null, null, messageI18n, messageArgs,
				translator, feedbackMembersContext.getPosition(), application, feedback, doer);
	}
	
	private void logSendMail(Identity member, int numOfApps, Identity doer) {
		String messageI18n = "audit.log.member.feedback.send.bulk.email";
		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(member, translator.getLocale()),
			feedbackMembersContext.getPosition().getMLShortTitle(translator.getLocale()),
			Integer.toString(numOfApps)
		};
		auditService.auditFeedbackMemberLog(Action.sendMail, null, null, messageI18n, messageArgs,
				translator, feedbackMembersContext.getPosition(), null, null, doer);
	}
	
	private void logSendMail(Identity member, Application application, ApplicationFeedback feedback, Identity doer) {
		String applicationName = salutationGenerator.getTitleFullname(application, translator.getLocale());
		String applicationId = application.getId().toString();
		String messageI18n = "audit.log.member.feedback.send.email";

		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(member, translator.getLocale()),
			applicationName,
			applicationId,
			feedbackMembersContext.getPosition().getMLShortTitle(translator.getLocale())
		};
		auditService.auditFeedbackMemberLog(Action.sendMail, null, null, messageI18n, messageArgs,
				translator, feedbackMembersContext.getPosition(), application, feedback, doer);
	}
	
	private Identity createMember(TransientIdentity tIdentity, Identity doer) {
		// Create new user and identity and put user to users group
		// Create transient user without firstName,lastName, email
		User newUser = userManager.createUser(null, null, null);
		// Now add data from user fields (firstName,lastName and email are mandatory)
		List<UserPropertyHandler> handlers = userManager.getUserPropertyHandlersFor(MembersController.formIdentifyer, true);
		for (UserPropertyHandler handler:handlers) {
			String value = tIdentity.getProperty(handler.getName());
			newUser.setProperty(handler.getName(), value);
		}
		Organisation organisation = organisationService.getDefaultOrganisation();
		return recruitingService.createCommitteeIdentity(tIdentity.getName(), newUser, tIdentity.isLdap(), tIdentity.isAzure(), null, organisation, doer);
	}
}
