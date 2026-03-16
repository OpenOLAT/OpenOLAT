/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RecruitingAuditLog extends CreateInfo {
	
	public Long getKey();
	
	public Action getActionEnum();
	
	public ActionTarget getTargetEnum();
	
	public String getBefore();
	
	public String getAfter();
	
	public String getMessage();
	
	public String getMessageI18n();
	
	public String[] getMessageValues();
	
	public Long getPositionKey();
	
	public Long getApplicationKey();
	
	public Long getCommentKey();
	
	public Identity getIdentity();
	
	
	public enum Action {
		add,
		update,
		changeConfiguration,
		changeStatus,
		committeeReminder,
		withdraw,
		revertWithdraw,
		onhold,
		revertOnhold,
		rejected,
		revertRejected,
		noteligible,
		revertNoteligible,
		granted,
		revertGranted,
		delete,
		remove,
		sendMail,
		accepted,
		declined,
		reset,
		deactivated,
		reactivated,
		comment,
		hired,
		revertHired,
		copy
	}
	
	public enum ActionTarget {
		position,
		committee,
		application,
		rating,
		review,
		comment,
		referee,
		referenceLetter,
		expert,
		expertOpinion,
		comparativeExpert,
		comparativeAssessment,
		categories,
		assignment,
		publicFeedback,
		publicFeedbackLink,
		memberFeedback,
		memberFeedbackMgmt,
		decision
	}

}
