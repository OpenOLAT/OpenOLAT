package org.olat.course.nodes.gta.ui;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailTemplate;

import java.util.Date;
import java.util.Locale;

public class GTAAssessmentMailTemplate extends MailTemplate {

	private final Identity identity;
	private final Translator translator;
	private final String taskName;

	public GTAAssessmentMailTemplate(String subject, String body, String taskName, Identity identity, Translator translator) {
		super(subject, body, null);
		this.translator = translator;
		this.identity = identity;
		this.taskName = taskName;
	}

	@Override
	public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
		Locale locale = translator.getLocale();
		//compatibility with the old TA
		context.put("login", identity.getName());
		context.put("first", identity.getUser().getProperty(UserConstants.FIRSTNAME, locale));
		context.put("firstName", identity.getUser().getProperty(UserConstants.FIRSTNAME, locale));
		context.put("last", identity.getUser().getProperty(UserConstants.LASTNAME, locale));
		context.put("lastName", identity.getUser().getProperty(UserConstants.LASTNAME, locale));
		context.put("email", identity.getUser().getProperty(UserConstants.EMAIL, locale));
		context.put("title", taskName);
		// format all dates using Formatter
		Date now = new Date();
		Formatter f = Formatter.getInstance(locale);
		context.put("date", f.formatDate(now));
		context.put("time", f.formatTime(now));
	}
}
