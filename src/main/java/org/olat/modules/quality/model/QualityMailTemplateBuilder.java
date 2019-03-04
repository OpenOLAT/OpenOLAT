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
				context.put("firstname", blankIfNull(firstname));
				context.put("lastname", blankIfNull(lastname));
				context.put("start", blankIfNull(start));
				context.put("deadline", blankIfNull(deadline));
				context.put("topictype", blankIfNull(topictype));
				context.put("topic", blankIfNull(topic));
				context.put("title", blankIfNull(title));
				context.put("previousTitle", blankIfNull(previousTitle));
				context.put("seriePosition", blankIfNull(seriePosition));
				context.put("context", blankIfNull(surveyContext));
				context.put("url", blankIfNull(url));
				context.put("invitation", blankIfNull(invitation));
				context.put("result", blankIfNull(result));
			}
		};
		return mailTempl;
	}

}
