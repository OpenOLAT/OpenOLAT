/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;

/**
 * 
 * Initial date: 18 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RecruitingAuditLogLight extends CreateInfo {
	
	public Long getKey();
	
	public Action getActionEnum();
	
	public ActionTarget getTargetEnum();
	
	public String getMessage();
	
	public String getMessageI18n();
	
	public String[] getMessageValues();
	
	public Long getPositionKey();
	
	public Long getApplicationKey();
	
	public Long getCommentKey();
	
	public Identity getIdentity();
	
}
