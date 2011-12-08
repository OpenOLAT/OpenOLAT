/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
* 
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik
* 
* Author Marcel Karras (toka@freebits.de)
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;

import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * 
 * Description:<br>
 * This helper class provides the functionality to send a notification mail
 * after succesfully finalizing the course creation wizard.
 * 
 * <P>
 * @author Marcel Karras (toka@freebits.de)
 */
public class CourseCreationMailHelper {

	/**
	 * Get the success info message.
	 *
	 * @param ureq user request
	 * @return info message
	 */
	public static final String getSuccessMessageString(final UserRequest ureq) {
		final Translator translator = Util.createPackageTranslator(CourseCreationMailHelper.class, ureq.getLocale());
		return translator.translate("coursecreation.success");
	}

	/**
	 * Sent notification mail for signalling that course creation was successful.
	 *
	 * @param ureq user request
	 * @param config course configuration object
	 * @return mailer result object
	 */
	public static final MailerResult sentNotificationMail(final UserRequest ureq, final CourseCreationConfiguration config) {
		Translator translator = Util.createPackageTranslator(CourseCreationMailHelper.class, ureq.getLocale());
		Tracing.createLoggerFor(CourseCreationMailHelper.class).info("Course creation with wizard finished. [User: " + ureq.getIdentity().getName() + "] [Course name: " + config.getCourseTitle() + "]");
		String subject = translator.translate("mail.subject", new String[] {config.getCourseTitle()});
		String body = translator.translate("mail.body.0", new String[] {config.getCourseTitle()});
		body += translator.translate("mail.body.1");
		body += translator.translate("mail.body.2", new String[] {config.getExtLink()});
		body += translator.translate("mail.body.3");
		body += translator.translate("mail.body.4");

		int counter = 1;
		if (config.isCreateSinglePage()) {
			body += translator.translate("mail.body.4.2", new String[] {Integer.toString(++counter)});
		}
		if (config.isCreateEnrollment()) {
			body += translator.translate("mail.body.4.3", new String[] {Integer.toString(++counter)});
		}
		if (config.isCreateDownloadFolder()) {
			body += translator.translate("mail.body.4.4", new String[] {Integer.toString(++counter)});
		}
		if (config.isCreateForum()) {
			body += translator.translate("mail.body.4.5", new String[] {Integer.toString(++counter)});
		}
		if (config.isCreateContactForm()) {
			body += translator.translate("mail.body.4.6", new String[] {Integer.toString(++counter)});
		}
		body += translator.translate("mail.body.5");
		body += translator.translate("mail.body.6");
		body += translator.translate("mail.body.greetings");

		MailTemplate template = new MailTemplate(subject, body, null) {
			@Override
			@SuppressWarnings("unused") 
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// nothing to do
			}
		};
		//fxdiff VCRP-16: intern mail system
		return MailerWithTemplate.getInstance().sendRealMail(ureq.getIdentity(), template);
	}

}
