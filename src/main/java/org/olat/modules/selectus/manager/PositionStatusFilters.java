/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
	private final boolean organisationUnit;
	private final List<PositionStatus> filtered;
	private final List<PositionStatus> committeeFiltered;
	private final Map<PositionRole, List<PositionStatus>> filterPerRole;
	
	public PositionStatusFilters(boolean committee, List<PositionStatus> filtered, List<PositionStatus> committeeFiltered,
			Map<PositionRole, List<PositionStatus>> filterPerRole, boolean organisationUnit) {
		this.committee = committee;
		this.filtered = filtered;
		this.filterPerRole = filterPerRole;
		this.organisationUnit = organisationUnit;
		this.committeeFiltered = committeeFiltered;
	}

	public boolean isCommittee() {
		return committee;
	}

	public boolean isOrganisationUnit() {
		return organisationUnit;
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
