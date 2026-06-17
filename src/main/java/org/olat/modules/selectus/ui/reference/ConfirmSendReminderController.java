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

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
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
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 23 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmSendReminderController extends FormBasicController {
	
	private Reference reference;
	private final Position position;
	private final Application application;

	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ConfirmSendReminderController(UserRequest ureq, WindowControl wControl, Position position, Application application, Reference reference) {
		super(ureq, wControl, "confirm_reminder", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.reference = reference;
		this.position = position;
		this.application = application;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;

			String message = translate("reference.management.confirm.reminder.text",
					salutationGenerator.getTitleLastName(reference, getLocale()));
			layoutCont.contextPut("message", message);
		}
		
		uifactory.addFormSubmitButton("send.reminder", "send.reminder", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		reference = recruitingService.getReferenceById(reference.getKey());
		if(reference == null) {
			showWarning("warning.reference.deleted");
		} else {
			MailerResult result = new MailerResult();
			Application app = reference.getApplication();
			List<Application> appsList = recruitingService.getReferenceToApplicationsList(reference);
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			Identity secretary = recruitingService.getSecretary(position);
			
			Locale locale = I18nModule.getDefaultLocale();
			Translator translator = Util.createPackageTranslator(PositionController.class, locale);
			
			ApplicationMailTemplate template = ReferenceHelper.referenceReminderTemplate(headOfCommittee, secretary, position,
					app, appsList, reference, salutationGenerator, translator);
			reference = recruitingService.sendRefereeMail(reference, app, appsList, position,
					template, ReferenceStatus.sentAwaiting, true, result);
			if(result.getReturnCode() == MailerResult.OK) {
				showInfo("reference.mail.send.success");
				logSendMail(reference, app, appsList);
			} else if(result.getFailedAddresses().isEmpty()) {
				showError("rejection.mail.send.error", reference.getEmail());
			} else {
				String error = result.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
				showError(error, new String[] { reference.getEmail(), result.failedAddressesToString() });
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void logSendMail(Reference ref, Application app, List<Application> appsList) {
		ActionTarget target = null;
		String messageI18n = "";
		if(ref.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.applicant.expert.send.reminder";
		} else if(ref.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.applicant.referee.send.reminder";
		}
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(ref, getLocale()),
			salutationGenerator.getTitleFullname(app, appsList, getLocale()),
			RecruitingHelper.formatIDs(app, appsList)
		};
		auditService.auditRefereeLog(Action.sendMail, target, null, null, messageI18n, messageArgs, getTranslator(), position, application, ref, getIdentity());
	}
}
