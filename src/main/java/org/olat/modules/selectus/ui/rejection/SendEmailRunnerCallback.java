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
package org.olat.modules.selectus.ui.rejection;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 7 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendEmailRunnerCallback implements StepRunnerCallback {
	
	private final Identity identity;
	private final Translator translator;
	private final EmailVariables emailVar;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	public SendEmailRunnerCallback(Identity identity, EmailVariables emailVar,
			RecruitingPositionSecurityCallback secCallback, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.identity = identity;
		this.emailVar = emailVar;
		this.translator = translator;
		this.secCallback = secCallback;
	}
	
	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		String templateName = emailVar.getTemplateName();
		List<ApplicationLight> mailsApps = emailVar.getSelectedApps();
		if(mailsApps.isEmpty()) {
			// do nothing
		} else if(mailsApps.size() == 1 || !emailVar.hasSelectedTemplateLetter()) {
			// do it synchronous
			Map<ApplicationLight, MailerResult> mailerResults = sendEmail(templateName, mailsApps);
			runContext.put("mailerResults", mailerResults);
			runContext.put("asyncMailer", Boolean.FALSE);
		} else {
			taskExecutorManager.execute(() -> {
				Map<ApplicationLight, MailerResult> results = sendEmail(templateName, mailsApps);
				sendConfirmationEmail(results);
			});
			runContext.put("mailerResults", Collections.emptyMap());
			runContext.put("asyncMailer", Boolean.TRUE);
		}
		dbInstance.commitAndCloseSession();

		ApplicationStatus status = emailVar.getApplicationStatus();
		if(status != null) {
			for(ApplicationLight mailApp:mailsApps) {
				Application app = recruitingService.getApplicationByKey(mailApp.getKey());
				
				String before = auditService.toAuditXml(app);
				
				ApplicationStatus currentStatus = app.getApplicationStatus();
				app.setApplicationStatus(status);
				Date statusDate = emailVar.getApplicationStatusDate();
				switch(status) {
					case onhold: app.setOnholdDate(statusDate); break;
					case withdrawn: app.setWithdrawnDate(statusDate); break;
					case rejected: app.setRejectedDate(statusDate); break;
					case noteligible: app.setNotEligibleDate(statusDate); break;
					case granted: app.setGrantedDate(statusDate); break;
					case hired: app.setHiredDate(statusDate); break;
					default: break;
				}
				
				if(StringHelper.containsNonWhitespace(emailVar.getApplicationStatusComment())) {
					if(StringHelper.containsNonWhitespace(app.getStatusComment())) {
						app.setStatusComment(app.getStatusComment() + "\n" + emailVar.getApplicationStatusComment());
					} else {
						app.setStatusComment(emailVar.getApplicationStatusComment());
					}
				}

				app = recruitingService.saveTempApplication(app, false);

				String after  = auditService.toAuditXml(app);
				if(currentStatus != status) {
					if(status == ApplicationStatus.active) {
						String messageI18n = "audit.log.application.revert.".concat(currentStatus.name());
						String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(app, ureq.getLocale()), app.getId().toString() };
						auditService.auditApplicationLog(currentStatus.revertAction(), ActionTarget.application, before, after,
								messageI18n, messageArgs, translator, app.getPosition(), app, ureq.getIdentity());
					} else {
						String messageI18n = "audit.log.application.".concat(status.name());
						String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(app, ureq.getLocale()), app.getId().toString() };
						auditService.auditApplicationLog(status.action(), ActionTarget.application, before, after,
								messageI18n, messageArgs, translator, app.getPosition(), app, ureq.getIdentity());
					} 
				}
			}
		}
		
		dbInstance.commitAndCloseSession();

		if(emailVar.getCategoriesToAdd() != null && !emailVar.getCategoriesToAdd().isEmpty()) {
			for(ApplicationLight mailApp:mailsApps) {
				Application app = recruitingService.getApplicationByKey(mailApp.getKey());
				taggingService.addCategories(app, emailVar.getCategoriesToAdd(), secCallback.canEditApplicationAdministrativeCategories(),
						emailVar.getPosition(), ureq.getIdentity(), ureq.getLocale());
			}
		}

		return mailsApps.isEmpty() ? StepsMainRunController.DONE_UNCHANGED : StepsMainRunController.DONE_MODIFIED;
	}
	
	private void sendConfirmationEmail(Map<ApplicationLight, MailerResult> mailerResults) {
		int countError = 0;
		for(MailerResult result:mailerResults.values()) {
			if(result.getReturnCode() != MailerResult.OK) {
				countError++;
			}
		}
		
		if(countError > 0) {
			Position position = emailVar.getPosition();
			Locale locale = translator.getLocale();
			Formatter formatter = Formatter.getInstance(locale);
			
			String[] args = new String[] {
				identity.getUser().getProperty(UserConstants.FIRSTNAME, locale),	// 0
				identity.getUser().getProperty(UserConstants.LASTNAME, locale),		// 1
				PositionMLHelper.getPositionMLTitle(position, locale),				// 2
				formatter.formatDateAndTime(new Date())	
			};
			
			String subject = translator.translate("error.async.mail.subject", args);
			String body = translator.translate("error.async.mail.body", args);
			String mail = identity.getUser().getProperty(UserConstants.EMAIL, locale);
			recruitingService.sendMail(mail, subject, body);
		}
	}
	
	private Map<ApplicationLight, MailerResult> sendEmail(String templateName, List<ApplicationLight> mailsApps) {
		Map<ApplicationLight, MailerResult> mailerResults = new HashMap<>();
		for(ApplicationLight mailApp:mailsApps) {
			String language = mailApp.getLanguage();
			Locale locale = recruitingModule.getPositionLocale(language);
			ApplicationMailTemplate template = emailVar.getTemplate(templateName, locale);
			MailerResult result = new MailerResult();
			recruitingService.sendRejectionMail(emailVar.getPosition(), mailApp, template, result);
			mailerResults.put(mailApp, result);
		}
		return mailerResults;
	}
}
