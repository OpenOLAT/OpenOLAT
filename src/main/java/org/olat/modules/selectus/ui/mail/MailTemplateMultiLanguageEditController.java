/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.ApplyToApplicationMainController;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;

/**
 * This controller edit the mail templates for confirmation and confirmation duplicate.
 * 
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailTemplateMultiLanguageEditController extends FormBasicController {

	private FormLink variablesButton;
	private List<FormLink> previewButtons = new ArrayList<>(2);
	private List<RichTextElement> bodyLanguagesEl = new ArrayList<>(2);
	
	private Position position;
	private Identity headOfCommittee;
	private final List<Locale> positionLanguages;
	private final PositionMailTemplateRow templateRow;

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
	
	public MailTemplateMultiLanguageEditController(UserRequest ureq, WindowControl wControl,
			Position position, PositionMailTemplateRow templateRow) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.position = position;
		this.templateRow = templateRow;
		positionLanguages = recruitingModule.getPositionLocales(position);
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		UserSession usess = ureq.getUserSession();
		
		for(Locale locale:positionLanguages) {
			initBodyForm(formLayout, locale, usess);
		}
		
		FormLayoutContainer variablesCont = FormLayoutContainer.createBareBoneFormLayout("links", getTranslator());
		formLayout.add(variablesCont);
		for(Locale locale:positionLanguages) {
			initPreviewForm(variablesCont, locale);
		}

		variablesButton = uifactory.addFormLink("edit.template.variables", variablesCont, Link.LINK);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initBodyForm(FormItemContainer formLayout, Locale templateLocale, UserSession usess) {
		String lang = templateLocale.getLanguage();
		String description = getBody(templateLocale);
		RichTextElement descriptionElement = uifactory.addRichTextElementForStringData("body_" + lang, "edit.body", description, 20, 60,
				false, null, null, formLayout, usess, getWindowControl());
		descriptionElement.getEditorConfiguration().setRelativeUrls(false);
		descriptionElement.getEditorConfiguration().setRemoveScriptHost(false);
		descriptionElement.getEditorConfiguration().setPathInStatusBar(false);
		descriptionElement.setMandatory(true);
		descriptionElement.setMaxLength(7000);
		descriptionElement.setUserObject(templateLocale);
		if(positionLanguages.size() > 1) {
			descriptionElement.setLabel("edit.body_ml", new String[]{ lang });
			descriptionElement.setElementCssClass("o_sel_template_body_" + lang);
		} else {
			descriptionElement.setElementCssClass("o_sel_template_body");
		}
		bodyLanguagesEl.add(descriptionElement);
	}
	
	private void initPreviewForm(FormItemContainer formLayout, Locale templateLocale) {
		FormLink previewLink = uifactory.addFormLink("edit.template.preview-" + templateLocale, "edit.template.preview", null, formLayout, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.setUserObject(templateLocale);
		if(positionLanguages.size() > 1) {
			previewLink.setI18nKey("edit.template.preview_ml", new String[] { templateLocale.getLanguage() });
		}
		previewButtons.add(previewLink);
	}
	
	private String getBody(Locale templateLocale) {
		String body = null;
		if(templateRow.getType() == Type.confirmationApplication) {
			body = position.getApplicationConfirmationMailTemplate(templateLocale);
			if(!StringHelper.containsNonWhitespace(body)) {
				body = ApplyToApplicationMainController.getDefaultMailTemplate(templateLocale, false);
			}
		} else if(templateRow.getType() == Type.confirmationApplicationWithRefereeManagement) {
			body = position.getApplicationConfirmationWithRefereeManagementMailTemplate(templateLocale);
			if(!StringHelper.containsNonWhitespace(body)) {
				body = ApplyToApplicationMainController.getDefaultMailTemplate(templateLocale, true);
			}
		} else if(templateRow.getType() == Type.confirmationApplicationDuplicate) {
			body = position.getApplicationConfirmationDuplicateMailTemplate(templateLocale);
			if(!StringHelper.containsNonWhitespace(body)) {
				body = ApplyToApplicationMainController.getDefaultMailTemplateDuplicate(templateLocale);
			}
		}
		return toHtml(body);
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}
	
	public Identity getHeadOfcommittee() {
		if(headOfCommittee == null) {
			headOfCommittee = recruitingService.getHeadOfCommittee(position);
		}
		return headOfCommittee;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(RichTextElement bodyEl:bodyLanguagesEl) {
			allOk &= RecruitingHelper.validateRichTextElement(bodyEl, 32000, true, new OWASPAntiSamyXSSFilter());
		}
		return allOk;
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
		removeControllerListener(cmc);
		mailPreviewCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		} else if(previewButtons.contains(source) && source.getUserObject() instanceof Locale) {
			doOpenPreview(ureq, (Locale)source.getUserObject());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
			position = recruitingService.savePosition(position);
		}
		
		String before = auditService.toAuditXml(position);
		
		for(TextElement bodyEl:bodyLanguagesEl) {
			Locale positionLocale = (Locale)bodyEl.getUserObject();
			if(templateRow.getType() == Type.confirmationApplication) {
				position.setApplicationConfirmationMailTemplate(bodyEl.getValue(), positionLocale);
				position = recruitingService.savePosition(position);
			} else if(templateRow.getType() == Type.confirmationApplicationWithRefereeManagement) {
				position.setApplicationConfirmationWithRefereeManagementMailTemplate(bodyEl.getValue(), positionLocale);
				position = recruitingService.savePosition(position);
			} else if(templateRow.getType() == Type.confirmationApplicationDuplicate) {
				position.setApplicationConfirmationDuplicateMailTemplate(bodyEl.getValue(), positionLocale);
				position = recruitingService.savePosition(position);
			}
		}
		
		getLogger().info(Tracing.M_AUDIT, "Update position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}

		dbInstance.commit();
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public void doOpenVariables(UserRequest ureq) {
		final boolean withApplicantDashboardUrl = templateRow.getType() == Type.confirmationApplicationWithRefereeManagement;
		
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, true, false, withApplicantDashboardUrl, null, null, true, true, false);
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
	
	private void doOpenPreview(UserRequest ureq, Locale locale) {
		Identity head = getHeadOfcommittee();
		if(head == null) {
			head = ReferenceHelper.generateDummyHeadOfCommittee();
		}
		Identity secretary = recruitingService.getSecretary(position);
		if(secretary == null) {
			secretary = ReferenceHelper.generateDummySecretary();
		}
		
		Application app = ReferenceHelper.generateDummyApplication(position);
		
		String subject = "";
		String body = "";
		for(TextElement bodyEl:bodyLanguagesEl) {
			if(locale.equals(bodyEl.getUserObject())) {
				body = bodyEl.getValue();
			}
		}
		
		String rawLetterConfiguration = null;
		if(templateRow.getType() == Type.confirmationApplication) {
			rawLetterConfiguration = position.getApplicationConfirmationMailLetter();
		} else if(templateRow.getType() == Type.confirmationApplicationWithRefereeManagement) {
			rawLetterConfiguration = position.getApplicationConfirmationWithRefereeManagementMailLetter();
		} else if(templateRow.getType() == Type.confirmationApplicationDuplicate) {
			rawLetterConfiguration = position.getApplicationConfirmationDuplicateMailLetter();
		}
		
		MailAttachment letter = null;
		if(StringHelper.containsNonWhitespace(rawLetterConfiguration)) {
			letter = mailService.toAttachment(rawLetterConfiguration, app, locale);
		}
		
		Translator localeTranslator = Util.createPackageTranslator(MailTemplateMultiLanguageEditController.class, locale);
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, app, null, null, secretary, head, localeTranslator) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
