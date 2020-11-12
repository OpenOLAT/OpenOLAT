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
package org.olat.modules.quality.model;

import static org.olat.core.util.StringHelper.blankIfNull;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailTemplate;

/**
 * 
 * Initial date: 31.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityMailTemplateBuilder {
	
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String START = "start";
	private static final String DEADLINE = "deadline";
	private static final String TOPICTYPE = "topictype";
	private static final String TOPIC = "topic";
	private static final String TITLE = "title";
	private static final String PREVIOUS_TITLE = "previousTitle";
	private static final String SERIE_POSITION = "seriePosition";
	private static final String CONTEXT = "context";
	private static final String URL = "url";
	private static final String INVITATION = "invitation";
	private static final String RESULT = "result";
	
	private final Formatter formatter;
	
	private final String subject;
	private final String body;
	private String firstname;
	private String lastname;
	private String start;
	private String deadline;
	private String topictype;
	private String topic;
	private String title;
	private String previousTitle;
	private String seriePosition;
	private String surveyContext;
	private String url;
	private String invitation;
	private String result;
	private File reportPdf;
	
	public static QualityMailTemplateBuilder builder(String subject, String body, Locale locale) {
		return new QualityMailTemplateBuilder(subject, body, locale);
	}

	private QualityMailTemplateBuilder(String subject, String body, Locale locale) {
		this.subject = subject;
		this.body = body;
		this.formatter = Formatter.getInstance(locale);
	}
	
	public QualityMailTemplateBuilder withExecutor(User user) {
		this.firstname = user.getProperty(UserConstants.FIRSTNAME, null);
		this.lastname = user.getProperty(UserConstants.LASTNAME, null);
		return this;
	}
	
	public QualityMailTemplateBuilder withStart(Date start) {
		this.start = formatter.formatDateAndTime(start);
		return this;
	}
	
	public QualityMailTemplateBuilder withDeadline(Date deadline) {
		this.deadline = formatter.formatDateAndTime(deadline);
		return this;
	}
	
	public QualityMailTemplateBuilder withTopicType(String topicType) {
		this.topictype = topicType;
		return this;
	}
	
	public QualityMailTemplateBuilder withTopic(String topic) {
		this.topic = topic;
		return this;
	}
	
	public QualityMailTemplateBuilder withTitle(String title) {
		this.title = title;
		return this;
	}
	
	public QualityMailTemplateBuilder withPreviousTitle(String previousTitle) {
		this.previousTitle = previousTitle;
		return this;
	}
	
	public QualityMailTemplateBuilder withSeriePosition(String seriePosition) {
		this.seriePosition = seriePosition;
		return this;
	}
	
	public QualityMailTemplateBuilder withContext(String context) {
		this.surveyContext = context;
		return this;
	}
	
	public QualityMailTemplateBuilder withUrl(String url) {
		this.url = url;
		return this;
	}
	
	public QualityMailTemplateBuilder withInvitation(Date invitation) {
		this.invitation = formatter.formatDateAndTime(invitation);
		return this;
	}
	
	public QualityMailTemplateBuilder withResult(String result) {
		this.result = result;
		return this;
	}
	
	public QualityMailTemplateBuilder withReportPfd(File reportPdf) {
		this.reportPdf = reportPdf;
		return this;
	}
	
	public MailTemplate build() {
		File[] attachments = reportPdf != null? new File[] { reportPdf }: null;
		
		MailTemplate mailTempl = new MailTemplate(subject, body, attachments ) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {	
				context.put(FIRST_NAME, blankIfNull(firstname));
				context.put("firstname", blankIfNull(firstname));
				context.put(LAST_NAME, blankIfNull(lastname));
				context.put("lastname", blankIfNull(lastname));
				context.put(START, blankIfNull(start));
				context.put(DEADLINE, blankIfNull(deadline));
				context.put(TOPICTYPE, blankIfNull(topictype));
				context.put(TOPIC, blankIfNull(topic));
				context.put(TITLE, blankIfNull(title));
				context.put(PREVIOUS_TITLE, blankIfNull(previousTitle));
				context.put(SERIE_POSITION, blankIfNull(seriePosition));
				context.put(CONTEXT, blankIfNull(surveyContext));
				context.put(URL, blankIfNull(url));
				context.put(INVITATION, blankIfNull(invitation));
				context.put(RESULT, blankIfNull(result));
			}
		};
		return mailTempl;
	}

}
