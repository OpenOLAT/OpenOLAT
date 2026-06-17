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
package org.olat.modules.selectus.ui.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplateEditController extends FormBasicController {

	private TextElement nameEl;
	private Link variablesButton;
	private List<FormLink> previewButtons = new ArrayList<>(2);
	private List<TextElement> subjectLanguagesEl = new ArrayList<>(2);
	private List<RichTextElement> bodyLanguagesEl = new ArrayList<>(2);
	
	private Type type;
	private String name;
	private Position position;
	private Identity headOfCommittee;
	private PositionMailTemplate updatedTemplate;
	private final List<Locale> positionLanguages;
	private final PositionMailTemplateRow templateRow;

	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionMailTemplateEditController(UserRequest ureq, WindowControl wControl,
			Position position, PositionMailTemplateRow templateRow, Type type, String name) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		this.type = templateRow == null ? type : templateRow.getType();
		this.name = name;
		this.position = position;
		this.templateRow = templateRow;
		positionLanguages = recruitingModule.getPositionLocales(position);
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}
	
	public PositionMailTemplate getUpdatedTemplate() {
		return updatedTemplate;
	}
	
	public String getTemplateName() {
		return nameEl.getValue();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		nameEl = uifactory.addTextElement("template.name", "template.name", 255, name, formLayout);
		nameEl.setMandatory(true);
		nameEl.setEnabled(templateRow == null || !templateRow.isSystemTemplate());
		
		UserSession usess = ureq.getUserSession();
		
		for(Locale locale:positionLanguages) {
			initSubjectForm(formLayout, locale);
			initBodyForm(formLayout, locale, usess);
		}
		
		FormLayoutContainer variablesCont = FormLayoutContainer.createBareBoneFormLayout("links", getTranslator());
		formLayout.add(variablesCont);
		for(Locale locale:positionLanguages) {
			initPreviewForm(variablesCont, locale);
		}
		
		String page = velocity_root + "/variable_link.html";
		FormLayoutContainer subCont = uifactory.addCustomFormLayout("cusvar", null, page, variablesCont);
		subCont.setDomReplacementWrapperRequired(false);
		variablesButton = LinkFactory.createLink("edit.template.variables", subCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initSubjectForm(FormItemContainer formLayout, Locale templateLocale) {
		String lang = templateLocale.getLanguage();
		String subject = getSubject(templateLocale);
		TextElement subjectEl = uifactory.addTextElement("subject_" + lang, "edit.subject", 256, subject, formLayout);
		subjectEl.setMandatory(true);
		subjectEl.setUserObject(templateLocale);
		if(positionLanguages.size() > 1) {
			subjectEl.setLabel("edit.subject_ml", new String[]{ lang });
			subjectEl.setElementCssClass("o_sel_template_subject_" + lang);
		} else {
			subjectEl.setElementCssClass("o_sel_template_subject");
		}
		subjectLanguagesEl.add(subjectEl);
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
	
	private String getSubject(Locale templateLocale) {
		if(templateRow != null && templateRow.getMailTemplate() != null
				&& StringHelper.containsNonWhitespace(templateRow.getMailTemplate().getSubject(templateLocale))) {
			return templateRow.getMailTemplate().getSubject(templateLocale);	
		}
		
		if(templateRow != null && templateRow.isSystemTemplate()) {
			SubjectAndBody subjectAndBody = mailService.rejectionTemplate(position, templateRow.getId(), getHeadOfCommittee(), templateLocale);
			return toHtml(subjectAndBody.getSubject());
		}
		return null;
	}
	
	private String getBody(Locale templateLocale) {
		if(templateRow != null && templateRow.getMailTemplate() != null
				&& StringHelper.containsNonWhitespace(templateRow.getMailTemplate().getBody(templateLocale))) {
			return templateRow.getMailTemplate().getBody(templateLocale);	
		}
		
		if(templateRow != null && templateRow.isSystemTemplate()) {
			SubjectAndBody subjectAndBody = mailService.rejectionTemplate(position, templateRow.getId(), getHeadOfCommittee(), templateLocale);
			return toHtml(subjectAndBody.getBody());
		}
		return null;
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}
	
	public Identity getHeadOfCommittee() {
		if(headOfCommittee == null) {
			headOfCommittee = recruitingService.getHeadOfCommittee(position);
		}
		return headOfCommittee;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= RecruitingHelper.validateTextElement(nameEl, 255, true, new OWASPAntiSamyXSSFilter());
		for(TextElement subjectEl:subjectLanguagesEl) {
			allOk &= RecruitingHelper.validateTextElement(subjectEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		for(RichTextElement bodyEl:bodyLanguagesEl) {
			allOk &= RecruitingHelper.validateRichTextElement(bodyEl, 32000, true, new OWASPAntiSamyXSSFilter());
		}
		return allOk;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
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
		if(previewButtons.contains(source) && source.getUserObject() instanceof Locale) {
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
		
		PositionMailTemplate template = null;
		if(templateRow == null || templateRow.getMailTemplate() == null) {
			String id = (templateRow != null && templateRow.isSystemTemplate()) ? templateRow.getId() : Long.toString(CodeHelper.getForeverUniqueID());
			template = mailService.createTemplate(position, id, nameEl.getName());
		} else {
			template = mailService.getTemplate(templateRow.getMailTemplate());
		}

		template.setName(nameEl.getValue());
		for(TextElement subjectEl:subjectLanguagesEl) {
			template.setSubject(subjectEl.getValue(), (Locale)subjectEl.getUserObject());
		}
		for(TextElement bodyEl:bodyLanguagesEl) {
			template.setBody(bodyEl.getValue(), (Locale)bodyEl.getUserObject());
		}
		
		updatedTemplate = mailService.updateTemplate(template);
		if(templateRow != null) {
			templateRow.setMailTemplate(updatedTemplate);
		}
		dbInstance.commit();
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public void doOpenVariables(UserRequest ureq) {
		final boolean withApplicantDashboardUrl = type == Type.custom || type == Type.system;
		
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
		Identity head = getHeadOfCommittee();
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
		for(TextElement subjectEl:subjectLanguagesEl) {
			if(locale.equals(subjectEl.getUserObject())) {
				subject = subjectEl.getValue();
			}
		}
		for(TextElement bodyEl:bodyLanguagesEl) {
			if(locale.equals(bodyEl.getUserObject())) {
				body = bodyEl.getValue();
			}
		}
		
		MailAttachment letter = null;
		if(templateRow != null && templateRow.getMailTemplate()  != null
				&& StringHelper.containsNonWhitespace(templateRow.getMailTemplate().getLetter())) {
			String letterConfiguration = templateRow.getMailTemplate().getLetter();
			letter = mailService.toAttachment(letterConfiguration, app, locale);
		}
		Translator localeTranslator = Util.createPackageTranslator(PositionMailTemplateEditController.class, locale);
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, app, null, null, secretary, head, localeTranslator) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
