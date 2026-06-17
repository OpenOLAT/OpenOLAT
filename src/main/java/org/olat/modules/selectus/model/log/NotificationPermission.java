/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
