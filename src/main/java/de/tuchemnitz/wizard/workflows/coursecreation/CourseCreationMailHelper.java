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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;

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
	
	private static final OLog log = Tracing.createLoggerFor(CourseCreationMailHelper.class);

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
		log.info("Course creation with wizard finished. [identity: " + ureq.getIdentity().getKey() + "] [Course name: " + config.getCourseTitle() + "]");
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
		String url = CoreSpringFactory.getImpl(HelpModule.class).getHelpProvider().getURL(ureq.getLocale(), "");
		body += translator.translate("mail.body.6", new String[]{ url });
		body += translator.translate("mail.body.greetings");
		
		MailBundle bundle = new MailBundle();
		bundle.setToId(ureq.getIdentity());
		bundle.setContent(subject, body);
		return CoreSpringFactory.getImpl(MailManager.class).sendExternMessage(bundle, null, false);
	}

}
