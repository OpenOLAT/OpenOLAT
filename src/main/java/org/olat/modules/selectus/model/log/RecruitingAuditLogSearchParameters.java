/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.log;

import java.util.Date;
import java.util.List;

import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;

/**
 * 
 * Initial date: 22 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogSearchParameters {

	private PositionRef position;
	private ActionTarget[] permittedTargets;
	private List<PositionNotificationsPermissions> permittedPositions;
	private ApplicationRef application;
	private RecruitingAuditLog.ActionTarget target;
	private Date from;
	private Date until;
	private boolean organisation;
	private boolean unreadOnly = false;

	public PositionRef getPosition() {
		return position;
	}

	public void setPosition(PositionRef position) {
		this.position = position;
	}

	public List<PositionNotificationsPermissions> getPermittedPositions() {
		return permittedPositions;
	}

	public void setPermittedPositions(List<PositionNotificationsPermissions> permittedPositions) {
		this.permittedPositions = permittedPositions;
	}

	public ActionTarget[] getPermittedTargets() {
		return permittedTargets;
	}

	public void setPermittedTargets(ActionTarget[] permittedTargets) {
		this.permittedTargets = permittedTargets;
	}

	public ApplicationRef getApplication() {
		return application;
	}

	public void setApplication(ApplicationRef application) {
		this.application = application;
	}

	public RecruitingAuditLog.ActionTarget getTarget() {
		return target;
	}
	
	public void setTarget(RecruitingAuditLog.ActionTarget target) {
		this.target = target;
	}
	
	public Date getFrom() {
		return from;
	}
	
	public void setFrom(Date from) {
		this.from = from;
	}
	
	public Date getUntil() {
		return until;
	}
	
	public void setUntil(Date until) {
		this.until = until;
	}

	public boolean isUnreadOnly() {
		return unreadOnly;
	}

	public void setUnreadOnly(boolean unreadOnly) {
		this.unreadOnly = unreadOnly;
	}

	public boolean isOrganisation() {
		return organisation;
	}

	public void setOrganisation(boolean organisation) {
		this.organisation = organisation;
	}
}
