/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications;

import java.util.Date;

import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;

/**
 * 
 * Initial date: 22 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuditLogRow {
	
	private final Long key;
	private final Date creationDate;
	private final Action action;
	private final ActionTarget target;
	private final String message;
	private final String messageI18n;
	private final String[] messageValues;
	
	private final boolean read;
	
	private final Long positionKey;
	private final Long applicationKey;
	private final Long commentKey;
	
	private final Long identityKey;
	private final String identityFullName;
	
	public AuditLogRow(String identityFullName, RecruitingAuditLogLight log, boolean read) {
		this.key = log.getKey();
		this.read = read;
		this.creationDate = log.getCreationDate();
		this.action = log.getActionEnum();
		this.target = log.getTargetEnum();
		this.message = log.getMessage();
		this.messageI18n = log.getMessageI18n();
		this.messageValues = log.getMessageValues();
		this.positionKey = log.getPositionKey();
		this.applicationKey = log.getApplicationKey();
		this.commentKey = log.getCommentKey();
		
		if(log.getIdentity() != null) {
			identityKey = log.getIdentity().getKey();
		} else {
			identityKey = null;
		}
		this.identityFullName = identityFullName;
	}
	
	public Long getKey() {
		return key;
	}
	
	public boolean isRead() {
		return read;
	}
	
	public String getIdentityFullName() {
		return identityFullName;
	}
	
	public Date getTime() {
		return creationDate;
	}
	
	public Long getPositionKey() {
		return positionKey;
	}
	
	public Long getApplicationKey() {
		return applicationKey;
	}
	
	public Long getCommentKey() {
		return commentKey;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public String getMessage() {
		return message;
	}
	
	public String getMessageI18n() {
		return messageI18n;
	}
	
	public String[] getMessageValues() {
		return messageValues;
	}
	
	public Action getAction() {
		return action;
	}
	
	public ActionTarget getTarget() {
		return target;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();		
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AuditLogRow) {
			AuditLogRow row = (AuditLogRow)obj;
			return key.equals(row.key);
		}
		return false;
	}
}
