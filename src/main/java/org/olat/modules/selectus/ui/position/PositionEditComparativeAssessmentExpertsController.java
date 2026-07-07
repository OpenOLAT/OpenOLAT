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
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;

/**
 * 
 * Initial date: 4 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditComparativeAssessmentExpertsController extends FormBasicController implements PositionEditableController {

	private FormLink previewLink;
	private Link variablesButton;
	private DateChooser deadlineEl;
	private TextElement mailSubjectEl;
	private RichTextElement mailTemplateEl;
	private MultipleSelectionElement staffCanAddComparativeExpertsEl;
	
	private Position position;
	private final boolean readOnly;
	
	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;
	
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
	
	public PositionEditComparativeAssessmentExpertsController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.readOnly = readOnly;
		this.position = position;
		
		initForm(ureq);
		updateGUI();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.comparative.experts");
		
		SelectionValues enablePK = new SelectionValues();
		enablePK.add(SelectionValues.entry("on", translate("enable")));
		
		staffCanAddComparativeExpertsEl = uifactory.addCheckboxesHorizontal("edit.staff.can.add.comparative.experts", formLayout, enablePK.keys(), enablePK.values());
		staffCanAddComparativeExpertsEl.addActionListener(FormEvent.ONCHANGE);
		staffCanAddComparativeExpertsEl.setEnabled(!readOnly);
		if(position.isComparativeAssessmentExpertEnabled()) {
			staffCanAddComparativeExpertsEl.select("on", true);
		}

		Date deadline = position.getComparativeAssessmentExpertDeadline();
		deadlineEl = uifactory.addDateChooser("edit.expert.deadline", "edit.expert.deadline", deadline, formLayout);
		deadlineEl.setMandatory(true);
		
		String subject = getComparativeAssessmentExpertSubject();
		mailSubjectEl = uifactory.addTextElement("edit.subject.experts", "edit.subject.experts", "reference.subject", 255, subject, formLayout);

		String expertTemplate = getComparativeAssessmentExpertTemplate();
		mailTemplateEl = uifactory.addRichTextElementForStringData("edit.template.experts", "reference.mail", expertTemplate, 18, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		mailTemplateEl.getEditorConfiguration().setRelativeUrls(false);
		mailTemplateEl.getEditorConfiguration().setRemoveScriptHost(false);
		mailTemplateEl.getEditorConfiguration().setPathInStatusBar(false);
		mailTemplateEl.setEnabled(!readOnly);
		
		String page = velocity_root + "/links.html";
		FormLayoutContainer variablesCont = FormLayoutContainer.createCustomFormLayout("links", getTranslator(), page);
		formLayout.add(variablesCont);
		
		variablesButton = LinkFactory.createLink("edit.template.variables", variablesCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		variablesButton.setVisible(!readOnly);
		
		previewLink = uifactory.addFormLink("edit.template.preview", variablesCont, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.setVisible(!readOnly);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private String getComparativeAssessmentExpertSubject() {
		String subject = position.getComparativeAssessmentExpertMailSubject();
		if(!StringHelper.containsNonWhitespace(subject)) {
			subject = translate("reference.comparative.expert.mail.subject", ReferenceHelper.getMailVariables());
		}
		return subject;
	}
	
	private String getComparativeAssessmentExpertTemplate() {
		String template = position.getComparativeAssessmentExpertMailTemplate();
		if(!StringHelper.containsNonWhitespace(template)) {
			template = translate("reference.comparative.expert.mail.body", ReferenceHelper.getMailVariables());
		}
		return template;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deadlineEl.clearError();
		if(staffCanAddComparativeExpertsEl.isAtLeastSelected(1)) {
			if(deadlineEl.getDate() == null) {
				deadlineEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateYearElement(deadlineEl);
			}
		}
		
		mailTemplateEl.clearError();
		if(StringHelper.containsNonWhitespace(mailTemplateEl.getValue())) {
			allOk &= checkTemplate(mailTemplateEl);
		}
		
		mailSubjectEl.clearError();
		if(StringHelper.containsNonWhitespace(mailSubjectEl.getValue())) {
			allOk &= checkTemplate(mailSubjectEl);
		}
		return allOk;
	}
	
	private boolean validateYearElement(DateChooser dateEl) {
		boolean ok = true;
		Date date = dateEl.getDate();
		if(date != null) {
			Calendar cal = Calendar.getInstance();
			int currentYear = cal.get(Calendar.YEAR) + 5;
			try {
				cal.setTime(date);
				int year = cal.get(Calendar.YEAR);
				if(year < 2010 || year > currentYear) {
					ok &= false;
					dateEl.setErrorKey("deadline.error", Integer.toString(currentYear));
				}
			} catch (NumberFormatException e) {
				ok =false;
				dateEl.setErrorKey("deadline.error", Integer.toString(currentYear));
			}
		}
		return ok;
	}
	
	private boolean checkTemplate(TextElement element) {
		Application mailApp = ReferenceHelper.generateDummyApplication(position);
		List<Application> mailApps = new ArrayList<>();
		mailApps.add(mailApp);
		
		Reference mailReference = ReferenceHelper.generateDummyReference();
		VariablesValidationContext context = new VariablesValidationContext();

		Identity headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		Identity secretary = ReferenceHelper.generateDummySecretary();
		String content = element.getValue();

		SubjectAndBody subjectAndBody = new SubjectAndBody("", content, null);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, "expert", "Expert",
				"", content, null, headOfCommittee, secretary,
				subjectAndBody, salutationGenerator, getTranslator());

		template.putVariablesInMailContext(context, null, mailApps, mailReference, null, null, null, position);
		
		MailerSender sender = recruitingService.createMailSender();
		boolean allOk = sender.checkTemplate(context, content);
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
		return allOk;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(staffCanAddComparativeExpertsEl == source) {
			updateGUI();
			markDirty();
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateGUI() {
		boolean expertEnabled = staffCanAddComparativeExpertsEl.isAtLeastSelected(1);
		deadlineEl.setVisible(expertEnabled);
		mailSubjectEl.setVisible(expertEnabled);
		mailTemplateEl.setVisible(expertEnabled);
		variablesButton.setVisible(expertEnabled);
		previewLink.setVisible(expertEnabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);
		
		boolean enabled = staffCanAddComparativeExpertsEl.isAtLeastSelected(1);
		if(enabled != position.isComparativeAssessmentExpertEnabled()) {
			logAudit("Comparative assessment experts " + (enabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setComparativeAssessmentExpertEnabled(enabled);
		if(enabled) {
			position.setComparativeAssessmentExpertDeadline(deadlineEl.getDate());
			position.setComparativeAssessmentExpertMailSubject(mailSubjectEl.getValue());
			position.setComparativeAssessmentExpertMailTemplate(mailTemplateEl.getValue());
		} else {
			position.setComparativeAssessmentExpertDeadline(null);
			position.setComparativeAssessmentExpertMailSubject(null);
			position.setComparativeAssessmentExpertMailTemplate(null);
		}

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update comparative assessment experts position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, false, true, false, ReferenceHelper.generateDummyReference(), null, true, true, false);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, mailVariablesCtrl);
			layoutCtr.addDisposableChildController(mailVariablesCtrl);
			return layoutCtr;
		};
		
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
	}

	private void doOpenPreview(UserRequest ureq) {
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		if(headOfCommittee == null) {
			headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		}
		Identity secretary = recruitingService.getSecretary(position);
		if(secretary == null) {
			secretary = ReferenceHelper.generateDummySecretary();
		}
		
		Reference reference = ReferenceHelper.generateDummyReference();
		reference.setReferenceType(ReferenceType.comparativeAssessmentExpert);
		Date deadline = deadlineEl.getDate();
		if(deadline != null) {
			reference.setSubmissionDeadline(deadline);
		}
		Application app = ReferenceHelper.generateDummyApplication(position);
		List<Application> appList = new ArrayList<>();
		appList.add(app);
		
		String subject = mailSubjectEl.getValue();
		String body = mailTemplateEl.getValue();
		
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, null,
				position, appList, reference, null, secretary, headOfCommittee, getTranslator()) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

}
