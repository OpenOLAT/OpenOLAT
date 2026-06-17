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

import java.util.Locale;

import org.olat.core.commons.persistence.DB;
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

/**
 * 
 * Initial date: 23 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeactivateReferenceController extends FormBasicController {

	private Reference reference;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ConfirmDeactivateReferenceController(UserRequest ureq, WindowControl wControl, Reference reference) {
		super(ureq, wControl, "confirm_deactivate", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.reference = reference;
	
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String refereeName = salutationGenerator.getTitleLastName(reference, getLocale());
			String message = translate("reference.management.deactivate.text", refereeName);
			layoutCont.contextPut("message", message);
		}
		
		uifactory.addFormSubmitButton("reference.management.deactivate", "reference.management.deactivate", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Send notifications and deactivated status
		MailerResult result = new MailerResult();
		Application application = reference.getApplication();
		Position position = application.getPosition();
		
		if(reference.getReferenceStatus() == ReferenceStatus.notSent) {
			reference.setReferenceStatus(ReferenceStatus.deactivated);
			reference = recruitingService.updateReference(reference);
		} else {
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			Identity secretary = recruitingService.getSecretary(position);
			
			Locale locale = I18nModule.getDefaultLocale();
			Translator translator = Util.createPackageTranslator(PositionController.class, locale);

			ApplicationMailTemplate template = ReferenceHelper.referenceDeactivationTemplate(headOfCommittee, secretary,
					position, application, reference, salutationGenerator, translator);
			reference = recruitingService.sendRefereeMail(reference, application, null, position, template, ReferenceStatus.deactivated, false, result);
		}
		
		// Log
		logDeactivation(position, application, reference);
		dbInstance.commitAndCloseSession();

		if(!result.isSuccessful()) {
			analyseResults(result);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void analyseResults(MailerResult mailerResult) {
		if(mailerResult.getReturnCode() == MailerResult.OK) {
			showInfo("rejection.mail.send.success");
		} else if(mailerResult.getFailedAddresses().isEmpty()) {
			showError("rejection.mail.send.error", reference.getEmail());
		} else {
			String error = mailerResult.getFailedAddresses().size() == 1 ?"error.mail.send.invalid.address" : "error.mail.send.invalid.addresses";
			showError(error, new String[] { reference.getEmail(), mailerResult.failedAddressesToString() });
		}
	}
	
	private void logDeactivation(Position position, Application application, Reference ref) {
		ActionTarget target = null;
		String messageI18n = "";
		if(ref.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.applicant.expert.deactivation";
		} else if(ref.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.applicant.referee.deactivation";
		}
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(ref, getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		auditService.auditRefereeLog(Action.deactivated, target, null, null, messageI18n, messageArgs, getTranslator(), position, application, ref, getIdentity());
	}
}
