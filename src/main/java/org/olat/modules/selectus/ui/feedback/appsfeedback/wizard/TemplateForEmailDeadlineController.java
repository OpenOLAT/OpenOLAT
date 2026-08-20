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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
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
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemplateForEmailDeadlineController extends StepFormBasicController {
	
	private static final String[] sendKeys = new String[] { "send" };
	
	private Link variablesButton;
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	private MultipleSelectionElement sendEmailEl;
	private DateChooser feedbackDeadlineEl;

	private final FeedbackMembersContext feedbacksContext;

	@Autowired
	private RecruitingService erFrontendManager;
	
	public TemplateForEmailDeadlineController(UserRequest ureq, WindowControl wControl, FeedbackMembersContext feedbacksContext,
			StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.template.mail.description");
		formLayout.setElementCssClass("o_sel_mail_to_template");
		
		String[] sendValues = new String[] { translate("apps.feedback.send.mail.check") };
		sendEmailEl = uifactory.addCheckboxesHorizontal("apps.feedback.send.mail", formLayout, sendKeys, sendValues);
		sendEmailEl.addActionListener(FormEvent.ONCHANGE);
		sendEmailEl.select(sendKeys[0], true);
	
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		String subject = template.getSubjectTemplate();
		subjectEl = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 256, subject, formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);
		
		String htmlBody = toHtml(template.getBodyTemplate());
		bodyEl = uifactory.addRichTextElementForStringData("bodyElem", "mailtemplateform.body", htmlBody, 20, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		bodyEl.getEditorConfiguration().setPathInStatusBar(false);
		bodyEl.setMandatory(true);
		bodyEl.setMaxLength(7000);
		
		String page = velocity_root + "/variable_link.html";
		FormLayoutContainer subCont = uifactory.addCustomFormLayout("cusvar", null, page, formLayout);
		subCont.setDomReplacementWrapperRequired(false);
		variablesButton = LinkFactory.createLink("edit.template.variables", subCont.getFormItemComponent(), listener);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));

		Date feedbackDeadline = feedbacksContext.getDeadline();
		feedbackDeadlineEl = uifactory.addDateChooser("edit.public.feedback.deadline", feedbackDeadline, formLayout);
		feedbackDeadlineEl.setMandatory(true);
	}
	
	private void updateGUI() {
		boolean sendMail = this.sendEmailEl.isAtLeastSelected(1);
		subjectEl.setVisible(sendMail);
		bodyEl.setVisible(sendMail);
		variablesButton.setVisible(sendMail);
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= checkSubjectSize(subjectEl, subjectEl.getMaxLength());
		allOk &= checkBodyElement(bodyEl);
		
		feedbackDeadlineEl.clearError();
		if(feedbackDeadlineEl.getDate() == null) {
			feedbackDeadlineEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= RecruitingHelper.validateYearElement(feedbackDeadlineEl);
		}

		return allOk;
	}
	
	private boolean checkSubjectSize(TextElement element, int size) {
		boolean ok = true;

		element.clearError();
		String value = element.getValue();
		if(sendEmailEl.isAtLeastSelected(1)) {
			if(!StringHelper.containsNonWhitespace(value)) {
				element.setErrorKey("form.legende.mandatory");
				ok &= false;
			} else if(StringHelper.containsNonWhitespace(value) && value.length() >= size) {
				element.setErrorKey("error.subject.max.length", new String[] { Integer.toString(size) });
				ok &= false;
			}
		}
		
		return ok;
	}
	
	private boolean checkBodyElement(TextElement element) {
		boolean ok = true;
		element.clearError();
		String value = element.getValue();
		if(sendEmailEl.isAtLeastSelected(1)) {
			if(!StringHelper.containsNonWhitespace(value)) {
				element.setErrorKey("form.legende.mandatory");
				ok &= false;
			} else if(!checkTemplate(element)) {
				ok &= false;
			}
		}
		
		return ok;
	}
	
	private boolean checkTemplate(TextElement element) {
		if(feedbacksContext.getSelectedApps().isEmpty()) {
			return true;
		}
		
		Position position = feedbacksContext.getPosition();
		List<ApplicationLight> mailApps = feedbacksContext.getSelectedApps();
		ApplicationLight mailApp = mailApps != null && mailApps.size() == 1 ? mailApps.get(0) : null;
		
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		ApplicationsFeedbackConfiguration feedbackConfig = feedbacksContext.getConfiguration();
		ApplicationFeedback dummyFeedback = FeedbackHelper.generateDummyFeedback(feedbackConfig, feedbackDeadlineEl.getDate());
		List<ApplicationFeedback> dummyFeedbacks = Collections.singletonList(dummyFeedback);
		Identity member = FeedbackHelper.generateDummyMember();

		VariablesValidationContext context = new VariablesValidationContext();
		template.putVariablesInMailContext(context, mailApp, mailApps, null, member, dummyFeedbacks, feedbackConfig, position);
		
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
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendEmailEl == source) {
			updateGUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		template.setSubjectTemplate(subjectEl.getValue());
		template.setBodyTemplate(bodyEl.getValue());
		
		feedbacksContext.setSendMail(sendEmailEl.isAtLeastSelected(1));
		
		Date deadline = feedbackDeadlineEl.getDate();
		feedbacksContext.setDeadline(deadline);

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			boolean app = feedbacksContext.getSelectedApps().size() == 1;
			boolean appList = !feedbacksContext.getSelectedApps().isEmpty();
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					feedbacksContext.getPosition(), app, appList, false, null, feedbacksContext.getConfiguration(),
					true, true, false);
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
}