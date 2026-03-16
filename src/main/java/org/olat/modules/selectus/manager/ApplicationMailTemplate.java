/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.mail.MailAttachment;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ApplicationMailTemplate {
	
	public static final String DEFAULT_TEMPLATE = "def";
	
	private final Long key;
	private final String name;
	private final String label;
	private final Locale locale;
	private String subjectTemplate;
	private String bodyTemplate;
	private MailAttachment letterTemplate;
	private final VelocityContext context;

	/**
	 * Constructor for a mail using a template
	 * 
	 * @param subjectTemplate Template for mail subject. Must not be NULL
	 * @param bodyTemplate Template for mail body. Must not be NULL
	 * @param attachments File array for mail attachments. Can be NULL
	 */
	public ApplicationMailTemplate(Long key, String name, String label,
			String subjectTemplate, String bodyTemplate, MailAttachment letterTemplate,
			Locale locale) {
		this.subjectTemplate = subjectTemplate;
		this.bodyTemplate = bodyTemplate;
		this.letterTemplate = letterTemplate;
		context = new VelocityContext();
		this.locale = locale;
		this.key = key;
		this.name = name;
		this.label = label;
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}

	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return mail subject template as string
	 */
	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	/**
	 * @return mail body template as string
	 */
	public String getBodyTemplate() {
		return bodyTemplate;
	}

	/**
	 * @param bodyTemplate Set body template
	 */
	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
	}

	/**
	 * @param subjectTemplate Set subject template
	 */
	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}
	
	public MailAttachment getLetterTemplate() {
		return letterTemplate;
	}
	
	public void setLetterTemplate(MailAttachment letterTemplate) {
		this.letterTemplate = letterTemplate;
	}

	/**
	 * Method that puts all necessary variables for those templates into the give
	 * velocity context. This method must match all variables used in the subject
	 * and body template.
	 * 
	 * @param context The context where to put the variables
	 * @param recipient The current identity which will get the email
	 */
	public abstract void putVariablesInMailContext(VelocityContext context, ApplicationShort app, List<? extends ApplicationShort> appList,
			Reference reference, Identity member, List<ApplicationFeedback> feedbacks, ApplicationsFeedbackConfiguration configuration,
			Position position);
	
	public void addToContext(String name, String value) {
		context.put(name, value);
	}
	
	public VelocityContext getContext() {
		return context;
	}
}