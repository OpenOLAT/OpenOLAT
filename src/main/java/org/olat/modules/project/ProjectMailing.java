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
package org.olat.modules.project;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.project.ui.ProjectUIFactory;

/**
 * 
 * Initial date: 5 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class ProjectMailing {
	
	public static ProjProjectMailTemplate getInvitationTemplate(ProjProject project, Identity actor) {
		String subjectKey = "mail.invitation.subject";
		String bodyKey = "mail.invitation.body";
		return createMailTemplate(project, actor, subjectKey, bodyKey);
	}
	
	private static ProjProjectMailTemplate createMailTemplate(ProjProject project, Identity actor, String subjectKey, String bodyKey) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(actor.getUser().getPreferences().getLanguage());
		Translator trans = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		String subject = trans.translate(subjectKey);
		String body = trans.translate(bodyKey);
		
		return new ProjProjectMailTemplate(project, subject, body, locale);
	}
	
	public static class ProjProjectMailTemplate extends MailTemplate {
		
		private static final String PROJECT_TITLE = "projectTitle";
		private static final String PROJECT_DESCRIPTION = "projectDescription";
		private static final String PROJECT_URL = "projectUrl";
		
		private final ProjProject project;
		private final Locale locale;
		private String url;
		
		public ProjProjectMailTemplate(ProjProject project, String subject, String body, Locale locale) {
			super(subject, body, null);	
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
			fillContextWithStandardIdentityValues(context, identity, locale);
			
			String title = project.getTitle();
			context.put(PROJECT_TITLE, title);
			context.put(PROJECT_TITLE.toLowerCase(), title);
			
			String description = StringHelper.containsNonWhitespace(project.getDescription())
					? FilterFactory.getHtmlTagAndDescapingFilter().filter(project.getDescription())
					: "";
			context.put(PROJECT_DESCRIPTION, description);
			context.put(PROJECT_DESCRIPTION.toLowerCase(), description);
			
			if (!StringHelper.containsNonWhitespace(url)) {
				url = Settings.getServerContextPathURI() + "/url/Projects/0/Project/" + project.getKey();
			}
			context.put(PROJECT_URL, url);
			context.put(PROJECT_URL.toLowerCase(), url);
		}
		
	}
}