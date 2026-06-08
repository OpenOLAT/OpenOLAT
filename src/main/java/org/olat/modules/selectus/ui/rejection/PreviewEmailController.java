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

import static org.olat.modules.selectus.manager.ApplicationMailTemplate.DEFAULT_TEMPLATE;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.mail.LetterMediaResource;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewEmailController extends FormBasicController {

	private StaticTextElement bodyEl;
	private StaticTextElement subjectEl;

	private Reference reference;
	private final Position position;
	private final Application application;
	private final List<Application> applicationList;
	private final String templateBody;
	private final String templateSubject;
	private final MailAttachment templateLetter;
	private final Identity secretary;
	private final Identity headOfCommittee;
	private final Identity member;
	private final List<ApplicationFeedback> feedbacks;
	private final ApplicationsFeedbackConfiguration feedbackConfig;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PreviewEmailController(UserRequest ureq, WindowControl wControl,
			String templateSubject, String templateBody, MailAttachment templateLetter,
			Position position, Application application, Reference reference,
			ApplicationsFeedbackConfiguration feedbackConfig, Identity secretary,
			Identity headOfCommittee, Translator translator) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(PreviewEmailController.class, translator.getLocale(),
				Util.createPackageTranslator(RecruitingHelper.class, getLocale(), translator)));
		this.position = position;
		this.reference = reference;
		this.application = application;
		this.applicationList = null;
		this.secretary = secretary;
		this.headOfCommittee = headOfCommittee;
		this.templateBody = templateBody;
		this.templateSubject = templateSubject;
		this.templateLetter = templateLetter;
		this.feedbacks = null;
		this.feedbackConfig = feedbackConfig;
		this.member = null;
		initForm(ureq);
		preview();
	}
	
	
	public PreviewEmailController(UserRequest ureq, WindowControl wControl,
			String templateSubject, String templateBody, MailAttachment templateLetter,
			Position position, List<Application> applicationList, Reference reference,
			ApplicationsFeedbackConfiguration feedbackConfig, Identity secretary,
			Identity headOfCommittee, Translator translator) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(PreviewEmailController.class, translator.getLocale(),
				Util.createPackageTranslator(RecruitingHelper.class, getLocale(), translator)));
		this.position = position;
		this.reference = reference;
		this.application = null;
		this.applicationList = applicationList;
		this.secretary = secretary;
		this.headOfCommittee = headOfCommittee;
		this.templateBody = templateBody;
		this.templateSubject = templateSubject;
		this.templateLetter = templateLetter;
		this.feedbacks = null;
		this.feedbackConfig = feedbackConfig;
		this.member = null;
		initForm(ureq);
		preview();
	}
	
	public PreviewEmailController(UserRequest ureq, WindowControl wControl,
			String templateSubject, String templateBody, MailAttachment templateLetter,
			Position position, Identity member, List<Application> applicationList,
			List<ApplicationFeedback> feedbacks, ApplicationsFeedbackConfiguration feedbackConfig,
			Identity secretary, Identity headOfCommittee, Translator translator) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(PreviewEmailController.class, getLocale(),
				Util.createPackageTranslator(RecruitingHelper.class, getLocale(), translator)));
		this.position = position;
		this.reference = null;
		this.application = null;
		this.applicationList = applicationList;
		this.secretary = secretary;
		this.headOfCommittee = headOfCommittee;
		this.templateBody = templateBody;
		this.templateSubject = templateSubject;
		this.templateLetter = templateLetter;
		this.member = member;
		this.feedbacks = feedbacks;
		this.feedbackConfig = feedbackConfig;
		initForm(ureq);
		preview();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		subjectEl = uifactory.addStaticTextElement("preview.subject", "", formLayout);
		bodyEl = uifactory.addStaticTextElement("preview.body", "", formLayout);
		
		if(recruitingModule.isMailLetterEnabled() && templateLetter != null
				&& StringHelper.containsNonWhitespace(templateLetter.getContentToPdf())) {
			String mapperUri = registerCacheableMapper(ureq, null, new PreviewMapper());
			String url = mapperUri + "/preview.pdf?test=" + CodeHelper.getForeverUniqueID();
			ExternalLinkItem link = uifactory.addExternalLink("letter", url, "_blank", formLayout);
			link.setName(templateLetter.getFilename());
			link.setIconLeftCSS("o_icon o_filetype_pdf");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}

	private void preview() {
		SubjectAndBody subjectBody = generatePreview();
		String subject = subjectBody.getSubject();
		subjectEl.setValue(subject);	
		subjectEl.setVisible(StringHelper.containsNonWhitespace(subject));
		String content = subjectBody.getBody();
		if(StringHelper.containsNonWhitespace(content) && !StringHelper.isHtml(content)) {
			content = Formatter.escWithBR(content).toString();
		}
		bodyEl.setValue(content);
	}
	
	private SubjectAndBody generatePreview() {
		SubjectAndBody subjectAndBody = new SubjectAndBody(templateSubject, templateBody, templateLetter);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, DEFAULT_TEMPLATE, DEFAULT_TEMPLATE,
				templateSubject, templateBody, templateLetter, headOfCommittee, secretary,
				subjectAndBody, salutationGenerator, getTranslator());
		
		MailerSender sender = recruitingService.createMailSender();
		MailerResult mailerResult = new MailerResult();
		return sender.createWithContext(application, applicationList, reference, member, feedbacks, feedbackConfig, position,
				template, mailerResult);
	}
	
	private class PreviewMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			SubjectAndBody subjectBody = generatePreview();
			if(subjectBody.getLetter() == null) {
				return new NotFoundMediaResource();
			}
			String letter = subjectBody.getLetter().getContentToPdf();
			return new LetterMediaResource(letter);
		}
	}
}
