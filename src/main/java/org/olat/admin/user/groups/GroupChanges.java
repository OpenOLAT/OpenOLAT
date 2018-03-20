package org.olat.admin.user.groups;

import java.util.List;

/**
 * 
 * Initial date: 20 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupChanges {

	private boolean validChange;
	private List<Long> ownerGroups;
	private List<Long> participantGroups;
	
	public boolean isValidChange() {
		return validChange;
	}
	
	public void setValidChange(boolean validChange) {
		this.validChange = validChange;
	}
	
	public List<Long> getOwnerGroups() {
		return ownerGroups;
	}

	public void setOwnerGroups(List<Long> ownerGroups) {
		this.ownerGroups = ownerGroups;
	}

	public List<Long> getParticipantGroups() {
		return participantGroups;
	}

	public void setParticipantGroups(List<Long> participantGroups) {
		this.participantGroups = participantGroups;
	}

}
