/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rejection;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.model.RejectionEmailLogFull;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.mail.LetterMediaResource;
import org.olat.modules.selectus.ui.resources.AttachmentMediaResource;

/**
 * The contact form is filled with the subject and content of the
 * mail in log. If they are empty, the template is used as a fallback.
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionRejectionResendController extends FormBasicController {

	private TextElement subjectEl;
	private TextElement bodyEl;
	private FormLink reloadLetterButton;
	private ExternalLinkItem letterLink;
	
	private final RejectionEmailLogFull log;
	private final ApplicationMailTemplate template;
	private final boolean showMultiLanguagesLabels;
	
	private boolean refreshLetter = false;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param log The precedent log of the email
	 * @param template The template fallback
	 */
	public PositionRejectionResendController(UserRequest ureq, WindowControl wControl,
			RejectionEmailLog log, ApplicationMailTemplate template) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.log = erFrontendManager.getFullLog(log);
		this.template = template;
		
		showMultiLanguagesLabels = !template.getLocale().equals(recruitingModule.getPositionDefaultLocale());
		
		initForm(ureq);
	}
	
	public RejectionEmailLogFull getLog() {
		return log;
	}

	public ApplicationMailTemplate getTemplate() {
		return template;
	}
	
	public boolean isRefreshLetter() {
		return refreshLetter;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("rejection.resend.description");
		
		String subject = log == null || !StringHelper.containsNonWhitespace(log.getMailSubject())
				? template.getSubjectTemplate() : log.getMailSubject();
		subjectEl = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, subject, formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);
		
		String body = log == null || !StringHelper.containsNonWhitespace(log.getMailContent())
				? template.getBodyTemplate() : log.getMailContent();		
		if(StringHelper.isHtml(body)) {
			bodyEl = uifactory.addRichTextElementForStringData("bodyElem", "mailtemplateform.body", body, 18, 60,
					false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		} else {
			bodyEl = uifactory.addTextAreaElement("bodyElem", "mailtemplateform.body", -1, 15, 60, true, false, false, body, formLayout);
		}
		bodyEl.setMandatory(true);
		
		if(showMultiLanguagesLabels) {
			String[] params = new String[] {template.getLocale().getLanguage() };
			subjectEl.setLabel("mailtemplateform.subject.ml", params);
			bodyEl.setLabel("mailtemplateform.body.ml", params);
		}
		
		Attachment attachment = log == null ? null : log.getLetter();
		if(recruitingModule.isMailLetterEnabled() && attachment != null) {
			String letterPage = velocity_root + "/letter_external_link.html";
			FormLayoutContainer letterCont = FormLayoutContainer.createCustomFormLayout("letter", getTranslator(), letterPage);
			formLayout.add(letterCont);
			letterCont.setLabel("letter.label", null);
			
			String mapperUri = registerCacheableMapper(ureq, null, new AttachmentMapper());
			String url = mapperUri + "/" + attachment.getName();
			letterLink = uifactory.addExternalLink("letter", url, "_blank", letterCont);
			letterLink.setName(attachment.getName());
			letterLink.setIconLeftCSS("o_icon o_filetype_pdf");
			
			reloadLetterButton = uifactory.addFormLink("letter.reload", letterCont);
			reloadLetterButton.setIconLeftCSS("o_icon o_icon_refresh");
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("send", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(reloadLetterButton == source) {
			doReloadLetter(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doReloadLetter(UserRequest ureq) {
		if(!refreshLetter) {
			refreshLetter = true;
			Attachment attachment = log.getLetter();
			String mapperUri = registerCacheableMapper(ureq, null, new PreviewMapper());
			String url = mapperUri + "/preview/" + attachment.getName();
			letterLink.setUrl(url);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String subjectTemplate = subjectEl.getValue();
		template.setSubjectTemplate(subjectTemplate);
		
		String bodyTemplate = bodyEl.getValue();
		template.setBodyTemplate(bodyTemplate);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private class AttachmentMapper implements Mapper {
		
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			Attachment attachment = log.getLetter();
			if(attachment == null) {
				return new NotFoundMediaResource();
			}
			return new AttachmentMediaResource(attachment);
		}
	}
	
	private class PreviewMapper implements Mapper {
		
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(template == null || template.getLetterTemplate() == null) {
				return new NotFoundMediaResource();
			}
			String letter = template.getLetterTemplate().getContentToPdf();
			return new LetterMediaResource(letter);
		}
	}
}