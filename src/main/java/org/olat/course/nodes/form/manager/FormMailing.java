/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.form.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.form.ui.FormParticipationController;
import org.olat.course.nodes.form.ui.FormRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FormMailing {
	
	private static final Logger log = Tracing.createLoggerFor(FormMailing.class);
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	
	public MailTemplate getConfirmationTemplate(RepositoryEntry courseEntry, CourseNode courseNode, Identity recipient,
			EvaluationFormSession session) {
		return createMailTemplate("mail.confirmation.subject", "mail.confirmation.body", courseEntry, courseNode, recipient, session);
	}
	
	private MailTemplate createMailTemplate(String subjectKey, String bodyKey, RepositoryEntry courseEntry,
			CourseNode courseNode, Identity recipient, EvaluationFormSession session) {
		final String courseUrl = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey();
		final String courseNodeUrl = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey()
				+ "/CourseNode/" + courseNode.getIdent();
		
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(recipient.getUser().getPreferences().getLanguage());
		Translator trans = Util.createPackageTranslator(FormRunController.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey);
		
		return new MailTemplate(subject, body, null) {
			
			private static final String USER_DISPLAY_NAME = "userDisplayName";
			private static final String COURSE_URL = "courseUrl";
			private static final String COURSE_NAME = "courseName";
			private static final String COURSE_DESCRIPTION = "courseDescription";
			private static final String COURSE_NODE_URL = "courseNodeUrl";
			private static final String COURSE_NODE_SHORT_TITLE = "courseNodeShortName";
			private static final String COURSE_NODE_LONG_TITLE = "courseNodeShortTitle";
			private static final String PARTICIPATION_DATE = "participationDate";
			private static final String PARTICIPATION_TIME = "participationTime";
			
			@Override
			public Collection<String> getVariableNames() {
				Set<String> variableNames = new HashSet<>();
				variableNames.addAll(getStandardIdentityVariableNames());
				variableNames.add(USER_DISPLAY_NAME);
				variableNames.add(COURSE_URL);
				variableNames.add(COURSE_NAME);
				variableNames.add(COURSE_DESCRIPTION);
				variableNames.add(COURSE_NODE_URL);
				variableNames.add(COURSE_NODE_SHORT_TITLE);
				variableNames.add(COURSE_NODE_LONG_TITLE);
				if (session != null) {
					variableNames.add(PARTICIPATION_DATE);
					variableNames.add(PARTICIPATION_TIME);
				}
				return variableNames;
			}
			
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				fillContextWithStandardIdentityValues(context, identity, locale);
				context.put(USER_DISPLAY_NAME, userManager.getUserDisplayName(identity.getKey()));
				context.put(COURSE_URL, courseUrl);
				context.put(COURSE_NAME, courseEntry.getDisplayname());
				context.put(COURSE_DESCRIPTION, courseEntry.getDescription());
				context.put(COURSE_NODE_URL, courseNodeUrl);
				context.put(COURSE_NODE_SHORT_TITLE, courseNode.getShortTitle());
				context.put(COURSE_NODE_LONG_TITLE, courseNode.getLongTitle());
				if (session != null) {
					Formatter f = Formatter.getInstance(locale);
					context.put(PARTICIPATION_DATE, f.formatDate(session.getSubmissionDate()));
					context.put(PARTICIPATION_TIME, f.formatTime(session.getSubmissionDate()));
				}
			}
		};
	}
	
	public void sendEmail(MailTemplate template, RepositoryEntry courseEntry, CourseNode courseNode, Identity recipient) {
		MailContext context = new MailContextImpl("[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]");
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, recipient, template, null, null, result);
		if(bundle != null) {
			mailManager.sendMessage(bundle);
			if (!result.isSuccessful()) {
				log.warn("E-mail in form course element not sent: {}, course node {}, {}", courseEntry, courseNode.getIdent(), recipient);
			}
		}
	}
	
	public void deleteTempDir(MailTemplate mailTemplate) {
		if (mailTemplate.getAttachmentsTmpDir() != null) {
			FileUtils.deleteDirsAndFiles(mailTemplate.getAttachmentsTmpDir(), true, true);
		}
	}
	
	public void addFormPdfAttachment(MailTemplate mailTemplate, CourseNode courseNode, UserCourseEnvironment coachedCourseEnv) {
		if (pdfModule.isEnabled()) {
			File attachmentsTmpDir = FileUtils.createTempDir("formattachment", null, null);
			mailTemplate.setAttachmentsTmpDir(attachmentsTmpDir);
			File formPdf = new File(attachmentsTmpDir, "form.pdf");
			addFormPdfAttachment(mailTemplate, courseNode, coachedCourseEnv, formPdf);
		}
	}
	
	private void addFormPdfAttachment(MailTemplate mailTemplate, CourseNode courseNode, UserCourseEnvironment coachedCourseEnv, File formPdf) {
		ControllerCreator controllerCreator = (lureq, lwControl) -> new FormParticipationController(lureq, lwControl,
				courseNode, coachedCourseEnv);
		try (OutputStream out = new FileOutputStream(formPdf)) {
			WindowControl bwControl = new WindowControlMocker();
			Identity identity = coachedCourseEnv.getIdentityEnvironment().getIdentity();
			pdfService.convert(identity, controllerCreator, bwControl, out);
			mailTemplate.setAttachments(new File[] {formPdf});
		} catch (IOException e) {
			log.error("Error while generating form pdf! Path: " + formPdf.getAbsolutePath(), e);
		}
	}
	
}
