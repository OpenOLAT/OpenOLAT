/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationFeedback  extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public String getComment();
	
	public void setComment(String comment);
	
	public Date getCommentDate();

	public void setCommentDate(Date commentDate);

	public Date getDeadline();

	public void setDeadline(Date deadline);
	
	public ReferenceStatus getReferenceStatus();
	
	public void setReferenceStatus(ReferenceStatus status);
	
	public Date getRequest();

	public void setRequest(Date request);

	public Date getLastReminder();

	public void setLastReminder(Date lastReminder);
	
	public Application getApplication();
	
	public Identity getIdentity();
	
	public ApplicationsFeedbackConfiguration getConfiguration();
	
}
