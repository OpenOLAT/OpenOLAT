/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.list;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 26 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMemberRow {

	private final boolean canRate;
	private final Identity identity;
	private final PositionRole role;

	private int numOfRatings;
	private int numOfAssignments;
	private int numOfAssignedRatings;
	
	public CommitteeMemberRow(Identity identity, PositionRole role, boolean canRate) {
		this.identity = identity;
		this.role = role;
		this.canRate = canRate;
	}
	
	public String getKey() {
		return identity.getKey().toString();
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public PositionRole getRole() {
		return role;
	}
	
	public boolean isCanRate() {
		return canRate;
	}

	public int getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(int numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public int getNumOfAssignedRatings() {
		return numOfAssignedRatings;
	}

	public void setNumOfAssignedRatings(int numOfAssignedRatings) {
		this.numOfAssignedRatings = numOfAssignedRatings;
	}

	public int getNumOfAssignments() {
		return numOfAssignments;
	}

	public void setNumOfAssignments(int numOfAssignments) {
		this.numOfAssignments = numOfAssignments;
	}
}
