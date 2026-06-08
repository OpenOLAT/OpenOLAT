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

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.mail.LetterMediaResource;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewEmailsController extends StepFormBasicController {
	
	private FormLink nextButton;
	private FormLink previousButton;
	private SingleSelection mailListEl;
	private StaticTextElement bodyEl;
	private StaticTextElement subjectEl;
	private ExternalLinkItem previewLink;

	private String mapperUri;
	private String letterName;
	private final EmailVariables emailVar;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PreviewEmailsController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, EmailVariables emailVar) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "mail_preview");
		this.emailVar = emailVar;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousButton = uifactory.addFormLink("previous.mail", formLayout, Link.BUTTON);
		previousButton.setIconLeftCSS("o_icon o_icon_previous");
		nextButton = uifactory.addFormLink("next.mail", formLayout, Link.BUTTON);
		nextButton.setIconRightCSS("o_icon o_icon_next");
		
		SelectionValues appValues = new SelectionValues();
		List<ApplicationLight> apps = emailVar.getSelectedApps();
		for(ApplicationLight app:apps) {
			String fullName = salutationGenerator.getFullname(app, getLocale());
			appValues.add(SelectionValues.entry(app.getKey().toString(), fullName));
		}
		mailListEl = uifactory.addDropdownSingleselect("select.mail", null, formLayout, appValues.keys(), appValues.values(), null);
		mailListEl.setDomReplacementWrapperRequired(false);
		mailListEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer verticalCont = FormLayoutContainer.createVerticalFormLayout("mail", getTranslator());
		formLayout.add(verticalCont);
		subjectEl = uifactory.addStaticTextElement("preview.subject", "", verticalCont);
		bodyEl = uifactory.addStaticTextElement("preview.body", "", verticalCont);
		
		ApplicationMailTemplate template = emailVar.getTemplate(emailVar.getTemplateName(), getLocale());
		if(recruitingModule.isMailLetterEnabled() && template.getLetterTemplate() != null
				&& StringHelper.containsNonWhitespace(template.getLetterTemplate().getContentToPdf())) {
			letterName = template.getLetterTemplate().getFilename();
			mapperUri = registerCacheableMapper(ureq, null, new PreviewMapper());
			String url = mapperUri + "/preview.pdf?test=" + CodeHelper.getForeverUniqueID();
			previewLink = uifactory.addExternalLink("letter", url, "_blank", verticalCont);
			previewLink.setName(letterName);
			previewLink.setIconLeftCSS("o_icon o_filetype_pdf");
		}
		
		boolean navigation = apps.size() > 1;
		previousButton.setVisible(navigation);
		nextButton.setVisible(navigation);
		mailListEl.setVisible(navigation);
		boolean hasContent = !apps.isEmpty();
		subjectEl.setVisible(hasContent);
		bodyEl.setVisible(hasContent);
		if(hasContent) {
			select(0);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousButton == source) {
			select(mailListEl.getSelected() - 1);
		} else if(nextButton == source) {
			select(mailListEl.getSelected() + 1);
		} else if(mailListEl == source) {
			select(mailListEl.getSelected());
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	private void select(int index) {
		if(index < 0) {
			index = 0;
		} else if(index >= mailListEl.getSize()) {
			index = mailListEl.getSize() - 1;
		}
		String nextKey = mailListEl.getKey(index);
		mailListEl.select(nextKey, true);
		ApplicationLight app = selectApplication();
		
		if(previewLink != null) {
			String url = mapperUri + "/" + nextKey + ".pdf";
			previewLink.setName(RecruitingHelper.letterName(letterName, app));
			previewLink.setUrl(url);
		}
		
		if(index <= 0) {
			previousButton.setEnabled(false);
			nextButton.setEnabled(true);
		} else if(index >= mailListEl.getSize() - 1) {
			previousButton.setEnabled(true);
			nextButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
			nextButton.setEnabled(true);
		}
	}
	
	private ApplicationLight selectApplication() {
		String appKey = mailListEl.getSelectedKey();
		SubjectAndBody subjectBody = generateMail(appKey);
		if(subjectBody != null) {
			subjectEl.setValue(subjectBody.getSubject());
			String content = subjectBody.getBody();
			if(StringHelper.containsNonWhitespace(content) && !StringHelper.isHtml(content)) {
				content = Formatter.escWithBR(content).toString();
			}
			bodyEl.setValue(content);
		}
		return getApplication(appKey);
	}

	private ApplicationLight getApplication(String appKey) {
		return emailVar.getSelectedApps().stream()
			.filter(app -> app.getKey().toString().equals(appKey))
			.findFirst().orElse(null);
	}
	
	private SubjectAndBody generateMail(String appKey) {
		ApplicationLight mailApp = getApplication(appKey);
		if(mailApp == null) {
			return null;
		}

		Position position = emailVar.getPosition();
		String templateName = emailVar.getTemplateName();
		String language = mailApp.getLanguage();
		Locale locale = recruitingModule.getPositionLocale(language);
		ApplicationMailTemplate template = emailVar.getTemplate(templateName, locale);
		
		MailerSender sender = recruitingService.createMailSender();
		MailerResult mailerResult = new MailerResult();
		return sender.createWithContext(mailApp, null, null, null, null, null, position, template, mailerResult);
	}
	
	private class PreviewMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			int index = relPath.lastIndexOf('/');
			int pointIndex = relPath.lastIndexOf('.');
			if(index >= 0 && pointIndex > index) {
				String appKey = relPath.substring(index + 1, pointIndex);
				if(StringHelper.isLong(appKey)) {
					SubjectAndBody subjectBody = generateMail(appKey);
					if(subjectBody != null) {
						String letter = subjectBody.getLetter().getContentToPdf();
						String filename = subjectBody.getLetter().getFilename();
						return new LetterMediaResource(letter, filename);
					}
				}
			}
			return new NotFoundMediaResource();
		}
	}
}
