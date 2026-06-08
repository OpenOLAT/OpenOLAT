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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
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
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemplateForEmailController extends StepFormBasicController {

	private Link variablesButton;
	private SingleSelection templatesEl;
	private final List<TextElement> subjectEls = new ArrayList<>();
	private final List<TextElement> bodyEls = new ArrayList<>();

	private final SelectionValues templatesKeys;
	private final EmailVariables emailVar;
	private final Locale[] templatesLocale;
	private final boolean showMultiLanguagesLabels;
	
	private CloseableModalController cmc;
	private AttachmentWarningController attachmentWarningCtrl;
	
	private ApplicationLight soloApp;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public TemplateForEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, EmailVariables emailVar) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.emailVar = emailVar;
		
		Set<Locale> selectedLocales = new HashSet<>();
		List<ApplicationLight> selectedApps = emailVar.getSelectedApps();
		for(ApplicationLight selectedApp:selectedApps) {
			selectedLocales.add(recruitingModule.getPositionLocale(selectedApp.getLanguage()));
		}
		if(selectedLocales.isEmpty()) {
			selectedLocales.add(recruitingModule.getPositionDefaultLocale());
		}
		
		if(selectedApps.size() == 1) {
			soloApp = selectedApps.get(0);
		}
		
		templatesKeys = emailVar.getTemplatesKeyValues();
		templatesLocale = selectedLocales.toArray(new Locale[selectedLocales.size()]);
		showMultiLanguagesLabels = templatesLocale.length > 1
				|| !templatesLocale[0].equals(recruitingModule.getPositionDefaultLocale());

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.mail.template.description");
		formLayout.setElementCssClass("o_sel_rejection_mail_to_template");
		
		if(emailVar.getApplicationsGroups() != null && !emailVar.getApplicationsGroups().isEmpty()) {
			uifactory.addStaticTextElement("selected.rejections", getApplicationsGroups(), formLayout);
		}

		templatesEl = uifactory.addDropdownSingleselect("mailtemplateform.templates", formLayout, templatesKeys.keys(), templatesKeys.values(), null);
		if(!templatesKeys.isEmpty()) {
			templatesEl.select(templatesKeys.keys()[0], true);
			templatesEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		UserSession usess = ureq.getUserSession();
		
		for(Locale templateLocale:templatesLocale) {
			PrefillTemplate fill = prefill(templateLocale);
			
			TextElement subjectEl = uifactory.addTextElement("subjectElem-" + templateLocale,
					"mailtemplateform.subject", 256, fill.getSubject(), formLayout);
			subjectEl.setUserObject(templateLocale);
			subjectEl.setDisplaySize(60);
			subjectEl.setMandatory(true);
			subjectEls.add(subjectEl);
			
			String htmlBody = toHtml(fill.getBody());
			RichTextElement bodyEl = uifactory.addRichTextElementForStringData("bodyElem-" + templateLocale, "mailtemplateform.body", htmlBody, 20, 60,
					false, null, null, formLayout, usess, getWindowControl());
			bodyEl.getEditorConfiguration().setRelativeUrls(false);
			bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
			bodyEl.getEditorConfiguration().setPathInStatusBar(false);
			bodyEl.setMandatory(true);
			bodyEl.setMaxLength(7000);
			bodyEl.setUserObject(templateLocale);
			bodyEls.add(bodyEl);

			if(showMultiLanguagesLabels) {
				String[] params = new String[]{ templateLocale.getLanguage() };
				subjectEl.setLabel("mailtemplateform.subject.ml", params);
				bodyEl.setLabel("mailtemplateform.body.ml", params);
			}
		}
	
		String page = velocity_root + "/variable_link.html";
		FormLayoutContainer subCont = uifactory.addCustomFormLayout("cusvar", null, page, formLayout);
		subCont.setDomReplacementWrapperRequired(false);
		variablesButton = LinkFactory.createLink("edit.template.variables", subCont.getFormItemComponent(), listener);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}
	
	public String getApplicationsGroups() {
		StringBuilder sb = new StringBuilder();
		if(emailVar.getApplicationsGroups() != null && !emailVar.getApplicationsGroups().isEmpty()) {
			for(String group:emailVar.getApplicationsGroups()) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(group);
			}
		}
		return sb.toString();
	}
	
	public String getSelectedTemplate() {
		if(templatesEl.isOneSelected()) {
			return templatesEl.getSelectedKey();
		}
		return templatesKeys.isEmpty() ? ApplicationMailTemplate.DEFAULT_TEMPLATE : templatesKeys.keys()[0];
	}
	
	private PrefillTemplate prefill(Locale templateLocale) {
		ApplicationMailTemplate template = emailVar.getTemplate(getSelectedTemplate(), templateLocale);
		String subject = "";
		String body = "";
		if(template != null) {
			subject = template.getSubjectTemplate();
			body = template.getBodyTemplate();
		}
		
		if(soloApp != null) {
			MailerResult mailerResult = new MailerResult(); 
			SubjectAndBody subjectAndBody2 = erFrontendManager.createMailSender()
					.createWithContext(soloApp, null, null, null, null, null, emailVar.getPosition(), template, mailerResult);
			if(mailerResult.isSuccessful()) {
				subject = subjectAndBody2.getSubject();
				body = subjectAndBody2.getBody();
			}
		}
		String htmlBody = toHtml(body);
		return new PrefillTemplate(subject, htmlBody);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(TextElement subjectEl:subjectEls) {
			subjectEl.clearError();
			allOk &= checkElement(subjectEl);
			allOk &= checkSubjectSize(subjectEl, subjectEl.getMaxLength());
		}
		for(TextElement bodyEl: bodyEls) {
			bodyEl.clearError();
			allOk &= checkElement(bodyEl);
		}
		return allOk;
	}
	
	private boolean checkSubjectSize(TextElement element, int size) {
		boolean ok = true;
		
		String value = element.getValue();
		if(StringHelper.containsNonWhitespace(value) && value.length() >= size ) {
			element.setErrorKey("error.subject.max.length", new String[] { Integer.toString(size) });
			ok &= false;
		}
		
		return ok;
	}
	
	private boolean checkElement(TextElement element) {
		boolean ok = true;
		String value = element.getValue();
		if(!StringHelper.containsNonWhitespace(value)) {
			element.setErrorKey("form.legende.mandatory");
			ok &= false;
		} else if(!checkTemplate(element)) {
			ok &= false;
		}
		return ok;
	}
	
	private boolean checkTemplate(TextElement element) {
		if(emailVar.getSelectedApps().isEmpty()) {
			return true;
		}
		
		Position position = emailVar.getPosition();
		ApplicationLight mailApp = emailVar.getSelectedApps().get(0);
		
		String templateName = getSelectedTemplate();
		Locale locale = recruitingModule.getPositionLocale(mailApp.getLanguage());
		ApplicationMailTemplate template = emailVar.getTemplate(templateName, locale);

		VariablesValidationContext context = new VariablesValidationContext();
		template.putVariablesInMailContext(context, mailApp, null, null, null, null, null, position);
		
		MailerSender sender = erFrontendManager.createMailSender();
		boolean allOk = sender.checkTemplate(context, element.getValue());
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(attachmentWarningCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				formOK(ureq);
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(attachmentWarningCtrl);
		removeAsListenerAndDispose(cmc);
		attachmentWarningCtrl = null;
		cmc = null;
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
		if(templatesEl == source) {
			doSelectTemplate(templatesEl.getSelectedKey());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		if(emailVar.isShowAttachmentWarning()) {
			attachmentWarningCtrl = new AttachmentWarningController(ureq, getWindowControl(), emailVar);
			listenTo(attachmentWarningCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "c", attachmentWarningCtrl.getInitialComponent(), translate("rejection.quick.view"));
			cmc.activate();
			listenTo(cmc);
		} else {
			formOK(ureq);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		emailVar.setTemplateName(getSelectedTemplate());

		for(TextElement subjectEl:subjectEls) {
			Locale locale = (Locale)subjectEl.getUserObject();
			ApplicationMailTemplate template = emailVar.getTemplate(getSelectedTemplate(), locale);
			template.setSubjectTemplate(subjectEl.getValue());	
		}
		
		for(TextElement bodyEl: bodyEls) {
			Locale locale = (Locale)bodyEl.getUserObject();
			ApplicationMailTemplate template = emailVar.getTemplate(getSelectedTemplate(), locale);
			template.setBodyTemplate(bodyEl.getValue());
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doSelectTemplate(String templateName) {
		if(!StringHelper.containsNonWhitespace(templateName)) return;
		
		emailVar.setTemplateName(templateName);
		
		for(Locale templateLocale:templatesLocale) {
			PrefillTemplate fill = prefill(templateLocale);
			for(TextElement subjectEl:subjectEls) {
				if(templateLocale.equals(subjectEl.getUserObject())) {
					subjectEl.setValue(fill.getSubject());
				}
			}
			for(TextElement bodyEl:bodyEls) {
				if(templateLocale.equals(bodyEl.getUserObject())) {
					bodyEl.setValue(fill.getBody());
				}
			}
		}
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					emailVar.getPosition(), true, false, true, null, null, true, true, false);
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
	
	private class PrefillTemplate {
		private final String subject;
		private final String body;
		
		public PrefillTemplate(String subject, String body) {
			this.subject = subject;
			this.body = body;
		}
		
		public String getSubject() {
			return subject;
		}
		
		public String getBody() {
			return body;
		}
	}
}