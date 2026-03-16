/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssigneeRow {
	
	private final Identity identity;
	private final PositionRole positionRole;
	
	private int numOfRatings;
	private int numOfAssignments;
	
	public AssigneeRow(Identity identity, PositionRole positionRole) {
		this.identity = identity;
		this.positionRole = positionRole;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public Long getIdentityKey() {
		return identity.getKey();
	}

	public PositionRole getPositionRole() {
		return positionRole;
	}

	public int getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(int numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public int getNumOfAssignments() {
		return numOfAssignments;
	}

	public void setNumOfAssignments(int numOfAssignments) {
		this.numOfAssignments = numOfAssignments;
	}
}
