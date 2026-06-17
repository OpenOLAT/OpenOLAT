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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 30 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicantRefereeCreateController extends FormBasicController {

	private SingleSelection titleEl;
	private TextElement firstNameEl;
	private TextElement lastNameEl;
	private TextElement institutionEl;
	private TextElement emailEl;
	
	private Position position;
	private Application application;
	
	private CloseableModalController cmc;
	private ConfirmCreateReferenceController confirmCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicantRefereeCreateController(UserRequest ureq, WindowControl wControl,
			Position position, Application application) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		PersonTitle[] personTitles = recruitingModule.getReferencePersonTitles();
		String[] titleKeys = new String[personTitles.length + 1];
		String[] titleValues = new String[personTitles.length + 1];
		titleKeys[0] = "";
		titleValues[0] = "-";
		for(int i=personTitles.length; i-->0; ) {
			titleKeys[i+1] = personTitles[i].title();
			titleValues[i+1] = translate(personTitles[i].i18nKey());
		}
		
		titleEl = uifactory.addDropdownSingleselect("edit.reference.title", "edit.reference.title", formLayout, titleKeys, titleValues, null);
		titleEl.setDomReplacementWrapperRequired(false);
		titleEl.setMandatory(true);

		firstNameEl = uifactory.addTextElement("edit.reference.firstname", "edit.application.firstName", 255, null, formLayout);
		firstNameEl.setDomReplacementWrapperRequired(false);
		firstNameEl.setMandatory(true);

		lastNameEl = uifactory.addTextElement("edit.reference.lastname", "edit.application.lastName", 255, null, formLayout);
		lastNameEl.setDomReplacementWrapperRequired(false);
		lastNameEl.setMandatory(true);
		
		institutionEl = uifactory.addTextElement("edit.reference.institution", "edit.reference.institution", 255, null, formLayout);
		institutionEl.setMandatory(true);
		
		emailEl = uifactory.addTextElement("edit.reference.email", "edit.reference.email", 255, null, formLayout);
		emailEl.setMandatory(true);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doCreate(ureq);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= RecruitingHelper.validateTextElement(lastNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(firstNameEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateTextElement(institutionEl, 255, true, new OWASPAntiSamyXSSFilter());
		allOk &= RecruitingHelper.validateEmailElement(emailEl, 255, true, new OWASPAntiSamyXSSFilter());
		
		if(StringHelper.containsNonWhitespace(emailEl.getValue())
				&& recruitingService.hasReferenceWithEmail(application, null, emailEl.getValue())) {
			emailEl.setErrorKey("error.email.in.use");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(guardModalController(confirmCtrl)) return;
			
		confirmCtrl = new ConfirmCreateReferenceController(ureq, getWindowControl());
		listenTo(confirmCtrl);
			
		String title = translate("add.recommendation");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	public void doCreate(UserRequest ureq) {
		String title = null;
		if(titleEl.isOneSelected()) {
			title = titleEl.getSelectedKey();
		}
		
		Reference reference = recruitingService.addReference(title, firstNameEl.getValue(), lastNameEl.getValue(), institutionEl.getValue(), emailEl.getValue(),
			null, ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, application, null);

		dbInstance.commit();
		
		MailerResult result = new MailerResult();
		Identity secretary = recruitingService.getSecretary(position);
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);

		Locale locale = I18nModule.getDefaultLocale();
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		
		ApplicationMailTemplate template = ReferenceHelper.referenceTemplate(headOfCommittee, secretary, position, application,
				null, reference, salutationGenerator, translator);
		reference = recruitingService.sendRefereeMail(reference, application, null, position, template, ReferenceStatus.sentAwaiting, false, result);
		
		dbInstance.commit();
		
		String after = auditService.toAuditXml(reference);
		logChanges(reference, after);
		fireEvent(ureq, Event.DONE_EVENT);
		
		if(result.getReturnCode() == MailerResult.OK) {
			showInfo("reference.mail.send.success");
		} else {
			if(result.getFailedAddresses().isEmpty()) {
				showError("reference.mail.send.error", reference.getEmail());
			} else {
				String error = result.getFailedAddresses().size() == 1 ?"error.mail.send.invalid.address" : "error.mail.send.invalid.addresses";
				showError(error, new String[] { reference.getEmail(), result.failedAddressesToString() });
			}
			reference.setReferenceStatus(ReferenceStatus.notSent);
			recruitingService.updateReference(reference);
		}
	}
	
	private void logChanges(Reference reference, String after) {
		String messageI18n = "audit.log.applicant.referee.add";
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(reference, getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		
		auditService.auditRefereeLog(Action.add, ActionTarget.referee, null, after, messageI18n, messageArgs, getTranslator(), position, application, reference, getIdentity());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
