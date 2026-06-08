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
package org.olat.modules.selectus.manager;

import java.util.List;
import java.util.Map;

import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Initial date: 7 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionStatusFilters {
	private final boolean committee;
	private final boolean organisation;
	private final List<PositionStatus> filtered;
	private final List<PositionStatus> committeeFiltered;
	private final Map<PositionRole, List<PositionStatus>> filterPerRole;
	
	public PositionStatusFilters(boolean committee, List<PositionStatus> filtered, List<PositionStatus> committeeFiltered,
			Map<PositionRole, List<PositionStatus>> filterPerRole, boolean organisation) {
		this.committee = committee;
		this.filtered = filtered;
		this.filterPerRole = filterPerRole;
		this.organisation = organisation;
		this.committeeFiltered = committeeFiltered;
	}

	public boolean isCommittee() {
		return committee;
	}

	public boolean isOrganisation() {
		return organisation;
	}

	public List<PositionStatus> getFiltered() {
		return filtered;
	}
	
	public List<PositionStatus> getCommitteeFiltered() {
		return committeeFiltered;
	}

	public Map<PositionRole, List<PositionStatus>> getFilterPerRole() {
		return filterPerRole;
	}
	
	public List<PositionStatus> getFilterPerRole(PositionRole role) {
		List<PositionStatus> status = null;
		if(filterPerRole != null && filterPerRole.containsKey(role)) {
			status = filterPerRole.get(role);
		}
		return status;
	}
	
	public boolean hasFilterPerRole(PositionRole role) {
		boolean has = false;
		if(filterPerRole != null && filterPerRole.containsKey(role)) {
			has = !filterPerRole.get(role).isEmpty();
		}
		return has;
	}
}
