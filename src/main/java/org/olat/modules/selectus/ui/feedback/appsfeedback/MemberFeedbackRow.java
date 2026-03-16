/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberFeedbackRow {
	
	private final FormLink editLink;
	private final ApplicationFeedback feedback;
	private final Application application;
	private final Position position;
	private final OrganisationUnit organisationUnit;
	private final ApplicationsFeedbackConfiguration config;
	
	public MemberFeedbackRow(ApplicationFeedback feedback, FormLink editLink) {
		this.feedback = feedback;
		this.config = feedback.getConfiguration();
		this.editLink = editLink;
		this.application = feedback.getApplication();
		this.position = application.getPosition();
		this.organisationUnit = position.getOrganisationUnit();
	}

	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public Application getApplication() {
		return application;
	}

	public Position getPosition() {
		return position;
	}

	public OrganisationUnit getOrganisationUnit() {
		return organisationUnit;
	}
	
	public Date getDeadline() {
		Date deadline = feedback.getDeadline();
		if(deadline == null) {
			deadline = config.getDeadline();
		}
		return deadline;
	}
	
	public boolean hasComment() {
		return StringHelper.containsNonWhitespace(feedback.getComment());
	}

	public FormLink getEditLink() {
		return editLink;
	}

}
