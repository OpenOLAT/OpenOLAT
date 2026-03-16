/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.RecruitingAuditLog;

/**
 * 
 * Initial date: 20 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ApplicationStatus {
	active(null, null),
	onhold(RecruitingAuditLog.Action.onhold, RecruitingAuditLog.Action.revertOnhold),
	withdrawn(RecruitingAuditLog.Action.withdraw, RecruitingAuditLog.Action.revertWithdraw),
	rejected(RecruitingAuditLog.Action.rejected, RecruitingAuditLog.Action.revertRejected),
	noteligible(RecruitingAuditLog.Action.noteligible, RecruitingAuditLog.Action.revertNoteligible),
	granted(RecruitingAuditLog.Action.granted, RecruitingAuditLog.Action.revertGranted),
	hired(RecruitingAuditLog.Action.hired, RecruitingAuditLog.Action.revertHired)
	;
	
	private final RecruitingAuditLog.Action action;
	private final RecruitingAuditLog.Action revertAction;
	
	private ApplicationStatus(RecruitingAuditLog.Action action, RecruitingAuditLog.Action revertAction) {
		this.action = action;
		this.revertAction = revertAction;
	}
	
	public RecruitingAuditLog.Action action() {
		return action;
	}
	
	public RecruitingAuditLog.Action revertAction() {
		return revertAction;
	}
	
	public static final boolean valid(String val) {
		for(ApplicationStatus status:values()) {
			if(status.name().equals(val)) {
				return true;
			}
		}
		return false;
	}
	
	public static final ApplicationStatus[] valueOfArray(String status) {
		ApplicationStatus[] statusEnum;
		if(StringHelper.containsNonWhitespace(status)) {
			String[] statusArr = status.split("[,]");
			statusEnum = new ApplicationStatus[statusArr.length];
			for(int i=statusArr.length; i-->0; ) {
				statusEnum[i] = ApplicationStatus.valueOf(statusArr[i]);
			}
		} else {
			statusEnum = new ApplicationStatus[0];
		}
		return statusEnum;
	}
	
	public static final List<String> toNameList(ApplicationStatus... applicationStatus) {
		List<String> statusList = new ArrayList<>();
		if(applicationStatus != null && applicationStatus.length > 0) {
			for(ApplicationStatus status:applicationStatus) {
				statusList.add(status.name());
			}
		}
		return statusList;
	}
}
