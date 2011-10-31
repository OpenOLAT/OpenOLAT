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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */


package org.olat.course.nodes.info;

import java.text.DateFormat;
import java.util.List;

import org.olat.commons.info.manager.MailFormatter;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.notifications.NotificationHelper;

/**
 * 
 * Description:<br>
 * Format the email send after the creation of an info message in a course
 * 
 * <P>
 * Initial Date:  24 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendMailFormatterForCourse implements MailFormatter {
	
	private final String courseTitle;
	private final String businessPath;
	private final Translator translator;
	
	public SendMailFormatterForCourse(String courseTitle, String businessPath, Translator translator) {
		this.courseTitle = courseTitle;
		this.translator = translator;
		this.businessPath = businessPath;
	}

	@Override
	public String getSubject(InfoMessage msg) {
		return msg.getTitle();
	}

	@Override
	public String getBody(InfoMessage msg) {
		BusinessControlFactory bCF = BusinessControlFactory.getInstance(); 
		List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
		String busPath = NotificationHelper.getBusPathStringAsURIFromCEList(ceList); 

		String author =	msg.getAuthor().getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + msg.getAuthor().getUser().getProperty(UserConstants.LASTNAME, null);
		String date = DateFormat.getDateInstance(DateFormat.MEDIUM, translator.getLocale()).format(msg.getCreationDate());
		String link =	Settings.getServerContextPathURI() + "/url/" + busPath;
		return translator.translate("mail.body", new String[]{courseTitle, author, date, msg.getMessage(), link});
	}
}
