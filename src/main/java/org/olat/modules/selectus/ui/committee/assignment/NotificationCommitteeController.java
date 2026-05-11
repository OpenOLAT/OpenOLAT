/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationCommitteeController extends StepFormBasicController {
	
	private static final String[] notificationKeys = new String[] { "notify" };

	private FormLink previewLink;
	private Link variablesButton;
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	private MultipleSelectionElement notificationEl;
	
	private final Position position;
	private final AssignmentsData data;
	private final Identity secretary;
	private final Identity headOfCommittee;
	private RecruitingMailTemplate mailTemplate;
	
	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;
	
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public NotificationCommitteeController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form, AssignmentsData data) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.data = data;
		position = data.getPosition();
		
		secretary = recruitingService.getSecretary(position);
		headOfCommittee = recruitingService.getHeadOfCommittee(position);
		if(data.getMailTemplate() == null) {
			mailTemplate = ReferenceHelper.assignmentTemplate(headOfCommittee, secretary,
					data.getPosition(), salutationGenerator, getTranslator());
		} else {
			mailTemplate = data.getMailTemplate();
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("notification.title");
		String[] notificationValues = new String[] {
				translate("assignments.notification.notify")
		};
		notificationEl = uifactory.addCheckboxesVertical("assignments.notification", formLayout,
				notificationKeys, notificationValues, 1);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		if(data.getMailTemplate() != null) {
			notificationEl.select(notificationKeys[0], true);
		}
		
		String subject = mailTemplate.getSubjectTemplate();
		subjectEl = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, subject, formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);
		subjectEl.setVisible(notificationEl.isAtLeastSelected(1));

		String body = mailTemplate.getBodyTemplate();
		bodyEl = uifactory.addRichTextElementForStringData("bodyElem", "mailtemplateform.body", body, 24, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		bodyEl.getEditorConfiguration().setPathInStatusBar(false);
		bodyEl.setMandatory(true);
		bodyEl.setVisible(notificationEl.isAtLeastSelected(1));
		
		String page = velocity_root + "/links.html";
		FormLayoutContainer variablesCont = FormLayoutContainer.createCustomFormLayout("links", getTranslator(), page);
		formLayout.add(variablesCont);
		variablesButton = LinkFactory.createLink("edit.template.variables", variablesCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		variablesButton.setVisible(notificationEl.isAtLeastSelected(1));
		
		previewLink = uifactory.addFormLink("edit.template.preview", variablesCont, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.setVisible(notificationEl.isAtLeastSelected(1));
	}
	
	private void updateUI() {
		boolean notify = notificationEl.isAtLeastSelected(1);
		subjectEl.setVisible(notify);
		bodyEl.setVisible(notify);
		variablesButton.setVisible(notify);
		previewLink.setVisible(notify);
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateForm();
		return allOk;
	}
	
	private boolean validateForm() {
		boolean allOk = true;
		
		subjectEl.clearError();
		bodyEl.clearError();
		if(notificationEl.isAtLeastSelected(1)) {
			allOk &= RecruitingHelper.validateTextElement(subjectEl, 4000, true, new OWASPAntiSamyXSSFilter());
			allOk &= RecruitingHelper.validateTextElement(bodyEl, 128000, true, new OWASPAntiSamyXSSFilter());
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
		if(notificationEl == source) {
			updateUI();
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void back() {
		if(validateForm()) {
			commitData();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitData();
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	private void commitData() {
		if(notificationEl.isAtLeastSelected(1)) {
			String subjectTemplate = subjectEl.getValue();
			mailTemplate.setSubjectTemplate(subjectTemplate);
			String bodyTemplate = bodyEl.getValue();
			mailTemplate.setBodyTemplate(bodyTemplate);
			data.setMailTemplate(mailTemplate);
		} else {
			data.setMailTemplate(null);
		}
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, false, false, false, null, null, true, true, false);
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
		String subject = subjectEl.getValue();
		String body = bodyEl.getValue();
		MailAttachment letter = mailTemplate.getLetterTemplate();
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, null, null, null, null, secretary, headOfCommittee, getTranslator()) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
