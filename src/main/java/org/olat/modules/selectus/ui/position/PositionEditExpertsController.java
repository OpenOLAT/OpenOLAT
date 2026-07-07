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

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
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
import org.olat.modules.selectus.MailService;
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
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;

/**
 * 
 * Initial date: 13.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditExpertsController extends FormBasicController implements PositionEditableController {
	
	private static final String[] enableKeys = new String[]{ "on" };
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private FormLink previewLink;
	private Link variablesButton;
	private MultipleSelectionElement staffCanAddExpertsEl;
	private TextElement expertDeadlineDayElement;
	private SingleSelection expertDeadlineMonthElement;
	private TextElement expertDeadlineYearElement;
	private FormLayoutContainer expertDeadlineContainer;
	private TextElement expertMailSubjectEl;
	private RichTextElement expertMailTemplateEl;
	
	private Position position;
	private final boolean readOnly;

	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionEditExpertsController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.experts");
		formLayout.setElementCssClass("o_sel_edit_position_recommendation_form");
		
		String[] enableValues = new String[]{ translate("enable") };
		
		staffCanAddExpertsEl = uifactory.addCheckboxesHorizontal("edit.staff.can.add.experts", formLayout, enableKeys, enableValues);
		staffCanAddExpertsEl.addActionListener(FormEvent.ONCHANGE);
		staffCanAddExpertsEl.setEnabled(!readOnly);
		if(position.isExpertRecommendationEnabled()) {
			staffCanAddExpertsEl.select(enableKeys[0], true);
		}
		
		//expert deadline container
		String pageDeadline = velocity_root + "/edit_deadline.html";
		expertDeadlineContainer = FormLayoutContainer.createCustomFormLayout("expert.deadline", getTranslator(), pageDeadline);
		expertDeadlineContainer.setRootForm(mainForm);
		expertDeadlineContainer.setLabel("edit.expert.deadline", null);
		expertDeadlineContainer.setMandatory(true);
		formLayout.add(expertDeadlineContainer);
		
		String day = "";
		String month= "0";
		String year = "";
		Date expertDeadline = position.getExpertRecommandationDeadline();
		if(expertDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(expertDeadline);
			day = Integer.toString(cal.get(Calendar.DATE));
			month = Integer.toString(cal.get(Calendar.MONTH));
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		expertDeadlineDayElement = uifactory.addTextElement("deadline.day", "", 2, day, expertDeadlineContainer);
		expertDeadlineDayElement.setDomReplacementWrapperRequired(false);
		expertDeadlineDayElement.setDisplaySize(2);
		expertDeadlineDayElement.setMandatory(true);
		expertDeadlineDayElement.setEnabled(!readOnly);
		
		expertDeadlineMonthElement = uifactory.addDropdownSingleselect("deadline.month", "", expertDeadlineContainer, monthKeys, monthValues, null);
		expertDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		expertDeadlineMonthElement.setMandatory(true);
		expertDeadlineMonthElement.select(month, true);
		expertDeadlineMonthElement.setEnabled(!readOnly);
		
		expertDeadlineYearElement = uifactory.addTextElement("deadline.year", "", 4, year, expertDeadlineContainer);
		expertDeadlineYearElement.setDomReplacementWrapperRequired(false);
		expertDeadlineYearElement.setDisplaySize(4);
		expertDeadlineYearElement.setMandatory(true);
		expertDeadlineYearElement.setEnabled(!readOnly);
		
		String subject = getExpertSubject();
		expertMailSubjectEl = uifactory.addTextElement("edit.subject.experts", "edit.subject.experts", "reference.subject", 255, subject, formLayout);
		
		String expertTemplate = getExpertTemplate();
		expertMailTemplateEl = uifactory.addRichTextElementForStringData("edit.template.experts", "reference.mail", expertTemplate, 18, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		expertMailTemplateEl.getEditorConfiguration().setRelativeUrls(false);
		expertMailTemplateEl.getEditorConfiguration().setRemoveScriptHost(false);
		expertMailTemplateEl.getEditorConfiguration().setPathInStatusBar(false);
		expertMailTemplateEl.setEnabled(!readOnly);
		
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
		
		updateGUI();
	}
	
	private void updateGUI() {
		boolean expertEnabled = staffCanAddExpertsEl.isAtLeastSelected(1);
		expertDeadlineContainer.setVisible(expertEnabled);
		expertMailSubjectEl.setVisible(expertEnabled);
		expertMailTemplateEl.setVisible(expertEnabled);
		variablesButton.setVisible(expertEnabled);
		previewLink.setVisible(expertEnabled);
	}
	
	private String getExpertSubject() {
		String expertSubject = position.getExpertRecommandationMailSubject();
		if(!StringHelper.containsNonWhitespace(expertSubject)) {
			expertSubject = translate("reference.expert.mail.subject");
		}
		return expertSubject;
	}
	
	private String getExpertTemplate() {
		String expertTemplate = position.getExpertRecommandationMailTemplate();
		if(!StringHelper.containsNonWhitespace(expertTemplate)) {
			expertTemplate = translate("reference.expert.mail.body");
		}
		return expertTemplate;
	}

	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		String template = getExpertTemplate();
		expertMailTemplateEl.setValue(template);
		String subject = getExpertSubject();
		expertMailSubjectEl.setValue(subject);
	}
	
	private Date getExpertDeadline() {
		String dayStr = expertDeadlineDayElement.getValue();
		String monthStr = expertDeadlineMonthElement.getSelectedKey();
		String yearStr = expertDeadlineYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDeadline(day, month, year, 0, 0);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDeadline(int day, int month, int year, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		expertDeadlineYearElement.clearError();
		if(staffCanAddExpertsEl.isAtLeastSelected(1)) {
			if(getExpertDeadline() == null) {
				expertDeadlineYearElement.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateYearElement(expertDeadlineYearElement);
			}
		}
		
		expertMailTemplateEl.clearError();
		if(StringHelper.containsNonWhitespace(expertMailTemplateEl.getValue())) {
			allOk &= checkTemplate(expertMailTemplateEl);
		}
		
		expertMailSubjectEl.clearError();
		if(StringHelper.containsNonWhitespace(expertMailSubjectEl.getValue())) {
			allOk &= checkTemplate(expertMailSubjectEl);
		}
		
		return allOk;
	}
	
	private boolean checkTemplate(TextElement element) {
		Application mailApp = ReferenceHelper.generateDummyApplication(position);
		Reference mailReference = ReferenceHelper.generateDummyReference();
		VariablesValidationContext context = new VariablesValidationContext();

		Identity headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		Identity secretary = ReferenceHelper.generateDummySecretary();
		String content = element.getValue();
		MailAttachment letter = mailService.toAttachment(position.getExpertRecommandationMailLetter(), mailApp, getLocale());

		SubjectAndBody subjectAndBody = new SubjectAndBody("", content, letter);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, "expert", "Expert",
				"", content, letter, headOfCommittee, secretary,
				subjectAndBody, salutationGenerator, getTranslator());

		template.putVariablesInMailContext(context, mailApp, null, mailReference, null, null, null, position);
		
		MailerSender sender = recruitingService.createMailSender();
		boolean allOk = sender.checkTemplate(context, content);
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
		return allOk;
	}
	
	private boolean validateYearElement(TextElement textEl) {
		boolean ok = true;
		if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
			try {
				int year = Integer.parseInt(textEl.getValue());
				if(year < 2010 || year > currentYear) {
					ok &= false;
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
			}
		}
		return ok;
	}
	
	

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mailPreviewCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(mailPreviewCtrl);
		removeAsListenerAndDispose(cmc);
		mailPreviewCtrl = null;
		cmc = null;
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
		
		boolean expertEnabled = staffCanAddExpertsEl.isAtLeastSelected(1);
		if(expertEnabled != position.isExpertRecommendationEnabled()) {
			logAudit("Experts " + (expertEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setExpertRecommendationEnabled(expertEnabled);
		if(expertEnabled) {
			position.setExpertRecommandationDeadline(getExpertDeadline());
			position.setExpertRecommandationMailSubject(expertMailSubjectEl.getValue());
			position.setExpertRecommandationMailTemplate(expertMailTemplateEl.getValue());
		} else {
			position.setExpertRecommandationDeadline(null);
			position.setExpertRecommandationMailSubject(null);
			position.setExpertRecommandationMailTemplate(null);
		}

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update referees / experts position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(staffCanAddExpertsEl == source) {
			updateGUI();
			markDirty();
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, true, false, false, ReferenceHelper.generateDummyReference(), null, true, true, false);
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
		Date deadline = getExpertDeadline();
		if(deadline != null) {
			reference.setSubmissionDeadline(deadline);
		}
		Application app = ReferenceHelper.generateDummyApplication(position);
		
		String subject = expertMailSubjectEl.getValue();
		String body = expertMailTemplateEl.getValue();
		String letterConfiguration = position.getExpertRecommandationMailLetter();
		MailAttachment letter = mailService.toAttachment(letterConfiguration, app, getLocale());
		
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, app, reference, null, secretary, headOfCommittee, getTranslator()) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}