/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.log;

import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;

/**
 * 
 * Initial date: 27 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationPermission {
	
	private final ActionTarget target;
	private final Action action;

	public NotificationPermission(ActionTarget target, Action action) {
		this.target = target;
		this.action = action;
	}

	public ActionTarget getTarget() {
		return target;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		return target.hashCode() + action.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof NotificationPermission) {
			NotificationPermission perm = (NotificationPermission)obj;
			return target.equals(perm.target) && action.equals(perm.action);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append(target.name()).append(".").append(action.name());
		return sb.toString();
	}
}
