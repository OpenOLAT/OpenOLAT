/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.feedback;

import java.util.Date;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 25 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientApplicationFeedback implements ApplicationFeedback {
	
	private Identity member;
	private Application application;
	private ApplicationsFeedbackConfiguration configuration;
	private Date deadline;
	
	public TransientApplicationFeedback(Identity member, Application application, ApplicationsFeedbackConfiguration configuration, Date deadline) {
		this.member = member;
		this.application = application;
		this.configuration = configuration;
		this.deadline = deadline;
	}

	@Override
	public Long getKey() {
		return null;
	}
	
	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public void setLastModified(Date date) {
		//
	}


	@Override
	public String getComment() {
		return null;
	}

	@Override
	public void setComment(String comment) {
		//
	}

	@Override
	public Date getCommentDate() {
		return null;
	}

	@Override
	public void setCommentDate(Date commentDate) {
		//
	}

	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public void setDeadline(Date deadline) {
		//
	}

	@Override
	public ReferenceStatus getReferenceStatus() {
		return null;
	}

	@Override
	public void setReferenceStatus(ReferenceStatus status) {
		//
	}

	@Override
	public Date getRequest() {
		return null;
	}

	@Override
	public void setRequest(Date request) {
		//
	}

	@Override
	public Date getLastReminder() {
		return null;
	}

	@Override
	public void setLastReminder(Date lastReminder) {
		//
	}

	@Override
	public Application getApplication() {
		return application;
	}

	@Override
	public Identity getIdentity() {
		return member;
	}

	@Override
	public ApplicationsFeedbackConfiguration getConfiguration() {
		return configuration;
	}
}
