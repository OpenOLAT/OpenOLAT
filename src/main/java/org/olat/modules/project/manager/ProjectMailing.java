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
package org.olat.modules.project.manager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 Dez 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
@Service
public class ProjectMailing {

	private static final Logger log = Tracing.createLoggerFor(ProjectMailing.class);
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	
	public ProjectMailTemplate createMemberAddTemplate(Identity sender, ProjProject project, ProjectBCFactory bcFactory) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(sender.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		String subject = translator.translate("mail.member.add.subject");
		String body = translator.translate("mail.member.add.body");
		return new ProjectMailTemplate(subject, body, sender, project, bcFactory, locale);
	}
	
	public class ProjectMailTemplate extends MailTemplate {
		
		private static final String PROJECT_TITLE = "projectTitle";
		private static final String PROJECT_EXTERNAL_REF = "projectRef";
		private static final String PROJECT_URL = "projectUrl";
		private static final String SENDER_FIRST_NAME = "senderFirstName";
		private static final String SENDER_LAST_NAME = "senderLastName";
		private static final String SENDER_DISPLAYNAME = "senderDisplayName";
		private static final String SENDER_EMAIL = "senderEmail";
		private static final String ROLES_ADD = "rolesAdd";
		
		private final Locale locale;
		
		private final Identity sender;
		private final ProjProject project;
		private final ProjectBCFactory bcFactory;
		private Set<ProjectRole> rolesAdd;
		
		public ProjectMailTemplate(String subject, String body, Identity sender, ProjProject project, ProjectBCFactory bcFactory, Locale locale) {
			super(subject, body, null);
			this.sender = sender;
			this.project = project;	
			this.bcFactory = bcFactory;
			this.locale = locale;
		}
		
		public void setRolesAdd(Set<ProjectRole> rolesAdd) {
			this.rolesAdd = rolesAdd;
		}
		
		@Override
		public Collection<String> getVariableNames() {
			Set<String> variableNames = new HashSet<>();
			variableNames.addAll(getStandardIdentityVariableNames());
			variableNames.add(PROJECT_TITLE);
			variableNames.add(PROJECT_EXTERNAL_REF);
			variableNames.add(PROJECT_URL);
			variableNames.add(ROLES_ADD);
			variableNames.add(SENDER_FIRST_NAME);
			variableNames.add(SENDER_LAST_NAME);
			variableNames.add(SENDER_DISPLAYNAME);
			variableNames.add(SENDER_EMAIL);
			return variableNames;
		}
		
		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity identity) {
			fillContextWithStandardIdentityValues(context, identity, locale);
			
			putVariablesInMailContext(context, SENDER_FIRST_NAME, StringHelper.escapeHtml(sender.getUser().getProperty(UserConstants.FIRSTNAME, locale)));
			putVariablesInMailContext(context, SENDER_LAST_NAME, StringHelper.escapeHtml(sender.getUser().getProperty(UserConstants.LASTNAME, locale)));
			putVariablesInMailContext(context, SENDER_DISPLAYNAME, userManager.getUserDisplayName(sender));
			putVariablesInMailContext(context, SENDER_EMAIL, userManager.getUserDisplayEmail(sender, locale));
			
			putVariablesInMailContext(context, PROJECT_TITLE, StringHelper.escapeHtml(project.getTitle()));
			putVariablesInMailContext(context, PROJECT_EXTERNAL_REF, StringHelper.blankIfNull(StringHelper.escapeHtml(project.getExternalRef())));
			putVariablesInMailContext(context, PROJECT_URL, bcFactory.getProjectUrl(project));
			
			if (rolesAdd != null) {
				Translator translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
				String translatedRoles = rolesAdd.stream()
						.map(role -> ProjectUIFactory.translateRoleInSentence(translator, role))
						.collect(Collectors.joining(", "));
				putVariablesInMailContext(context, ROLES_ADD, translatedRoles);
			}
		}
	}

	public ProjInvitationMailTemplate getInvitationTemplate(ProjProject project, Identity actor) {
		String subjectKey = "mail.invitation.subject";
		String bodyKey = "mail.invitation.body";
		return createInvitationMailTemplate(project, actor, subjectKey, bodyKey);
	}

	private ProjInvitationMailTemplate createInvitationMailTemplate(ProjProject project, Identity actor, String subjectKey,
			String bodyKey) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
		Translator trans = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey);

		return new ProjInvitationMailTemplate(subject, body, project, actor, locale);
	}

	public static class ProjInvitationMailTemplate extends MailTemplate {

		private static final String PROJECT_TITLE = "projectTitle";
		private static final String PROJECT_DESCRIPTION = "projectDescription";
		private static final String PROJECT_URL = "projectUrl";

		private final ProjProject project;
		private final Identity actor;
		private final Locale locale;
		private String url;

		public ProjInvitationMailTemplate(String subject, String body, ProjProject project, Identity actor,
				Locale locale) {
			super(subject, body, null);
			this.actor = actor;
			this.locale = locale;
			this.project = project;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public Collection<String> getVariableNames() {
			Set<String> variableNames = new HashSet<>();
			variableNames.addAll(getStandardIdentityVariableNames());
			variableNames.add(PROJECT_TITLE);
			variableNames.add(PROJECT_DESCRIPTION);
			variableNames.add(PROJECT_URL);
			return variableNames;
		}

		@Override
		public void putVariablesInMailContext(VelocityContext context, Identity identity) {
			fillContextWithStandardIdentityValues(context, actor, locale);

			String title = project.getTitle();
			context.put(PROJECT_TITLE, title);
			context.put(PROJECT_TITLE.toLowerCase(), title);

			String description = StringHelper.containsNonWhitespace(project.getDescription())
					? FilterFactory.getHtmlTagAndDescapingFilter().filter(project.getDescription())
					: "";
			context.put(PROJECT_DESCRIPTION, description);
			context.put(PROJECT_DESCRIPTION.toLowerCase(), description);

			if (!StringHelper.containsNonWhitespace(url)) {
				url = ProjectBCFactory.createFactory(project).getProjectUrl(project);
			}
			context.put(PROJECT_URL, url);
			context.put(PROJECT_URL.toLowerCase(), url);
		}

	}

	public void send(Identity doer, Identity recipient, ProjectMailTemplate template) {
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(null, recipient, template, doer, null, result);
		if (bundle != null) {
			result = mailManager.sendMessage(bundle);
			if (result.isSuccessful()) {
				log.debug("Project e-mail from {} to {} successfully sent.", doer, recipient);
			} else {
				log.warn("Project e-mail from {} to {} failed!", doer, recipient);
			}
		}
		
	}
}