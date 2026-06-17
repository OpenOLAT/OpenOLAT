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
package org.olat.modules.selectus.ui.reference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 7 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendReferencesEmailRunnerCallback implements StepRunnerCallback {
	
	private final InvitationVariables invitationVar;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public SendReferencesEmailRunnerCallback(InvitationVariables invitationVar) {
		CoreSpringFactory.autowireObject(this);
		this.invitationVar = invitationVar;
	}
	
	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		Date submissionDeadline = invitationVar.getSubmissionDeadline();
		List<Reference> references = invitationVar.getSelectedReferences();
		Position position = invitationVar.getPosition();
		Translator translator = Util.createPackageTranslator(PositionReferenceListController.class, ureq.getLocale(),
				Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		Map<Reference, MailerResult> mailerResults = new HashMap<>();
		for(Reference reference:references) {
			// reload references
			reference = recruitingService.getReferenceById(reference.getKey());
			if(reference == null) {
				continue;
			}
			
			MailerResult result = new MailerResult();
			ApplicationMailTemplate template = null;
			Application application = reference.getApplication();
			List<Application> applicationsList = null;
			if(reference.getReferenceType() == ReferenceType.expert) {
				template = invitationVar.getExpertTemplate();
			} else if(reference.getReferenceType() == ReferenceType.recommendation) {
				template = invitationVar.getRecommendationTemplate();
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				template = invitationVar.getComparativeExpertTemplate();
				applicationsList = recruitingService.getReferenceToApplicationsList(reference);
			}
			
			if(submissionDeadline != null) {
				reference.setSubmissionDeadline(submissionDeadline);
			}
			
			reference = recruitingService.sendRefereeMail(reference, application, applicationsList, position,
					template, ReferenceStatus.sentAwaiting, false, result);
			mailerResults.put(reference, result);
			logSendMail(position, reference, application, applicationsList, ureq.getIdentity(), translator);
		}
		
		runContext.put("mailerResults", mailerResults);
		return references.isEmpty() ? StepsMainRunController.DONE_UNCHANGED : StepsMainRunController.DONE_MODIFIED;
	}
	
	private void logSendMail(Position position, Reference reference, Application application, List<Application> applicationsList,
			Identity identity, Translator translator) {
		
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.send.email";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.send.email";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = "audit.log.comparative.expert.send.email";
		}
		
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(reference, translator.getLocale()),
			salutationGenerator.getTitleFullname(application, applicationsList, translator.getLocale()),
			RecruitingHelper.formatIDs(application, applicationsList)
		};
		auditService.auditRefereeLog(Action.sendMail, target, null, null, messageI18n, messageArgs, translator, position, application, reference, identity);
	}
}
