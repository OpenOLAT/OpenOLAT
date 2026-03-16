/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackRow {
	
	private final PublicFeedback feedback;
	private final FormLink deleteLink;
	private final String feedbackNumber;
	private boolean open = true;
	
	private final Locale locale;
	
	public PublicFeedbackRow(PublicFeedback feedback, FormLink deleteLink, int feedbackNumber, Locale locale) {
		this.locale = locale;
		this.feedback = feedback;
		this.deleteLink = deleteLink;
		this.feedbackNumber = Integer.toString(feedbackNumber);
	}
	
	public String getId() {
		return feedback.getKey().toString();
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public String getFeedbackNumber() {
		return feedbackNumber;
	}
	
	public Date getLastModified() {
		return feedback.getLastModified();
	}
	
	public String getEmail() {
		return feedback.getEmail();
	}
	
	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(feedback.getFirstName())) {
			sb.append(feedback.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(feedback.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(feedback.getLastName());
		}
		return sb.toString();
	}
	
	public String getFeedbackDate() {
		return DateCellRenderer.format(feedback.getCreationDate(), locale);
	}
	
	public String getFeedbackTime() {
		return Formatter.getInstance(locale).formatTimeShort(feedback.getCreationDate());
	}
	
	public String getComment() {
		StringBuilder comment = Formatter.escWithBR(feedback.getComment());
		return comment == null ? "" : comment.toString();
	}
	
	public PublicFeedback getFeedback() {
		return feedback;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}
}
