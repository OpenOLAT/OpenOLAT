package org.olat.course.member;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchMembersParams {
	private boolean repoOwners;
	private boolean repoTutors;
	private boolean repoParticipants;
	private boolean groupTutors;
	private boolean groupParticipants;
	private boolean groupWaitingList;
	
	public SearchMembersParams() {
		//
	}
	
	public SearchMembersParams(boolean repoOwners, boolean repoTutors, boolean repoParticipants,
			boolean groupTutors, boolean groupParticipants, boolean groupWaitingList) {
		this.repoOwners = repoOwners;
		this.repoTutors = repoTutors;
		this.repoParticipants = repoParticipants;
		this.groupTutors = groupTutors;
		this.groupParticipants = groupParticipants;
		this.groupWaitingList = groupWaitingList;
	}
	
	public boolean isRepoOwners() {
		return repoOwners;
	}
	
	public void setRepoOwners(boolean repoOwners) {
		this.repoOwners = repoOwners;
	}
	
	public boolean isRepoTutors() {
		return repoTutors;
	}
	
	public void setRepoTutors(boolean repoTutors) {
		this.repoTutors = repoTutors;
	}
	
	public boolean isRepoParticipants() {
		return repoParticipants;
	}
	
	public void setRepoParticipants(boolean repoParticipants) {
		this.repoParticipants = repoParticipants;
	}
	
	public boolean isGroupTutors() {
		return groupTutors;
	}
	
	public void setGroupTutors(boolean groupTutors) {
		this.groupTutors = groupTutors;
	}
	
	public boolean isGroupParticipants() {
		return groupParticipants;
	}
	
	public void setGroupParticipants(boolean groupParticipants) {
		this.groupParticipants = groupParticipants;
	}
	
	public boolean isGroupWaitingList() {
		return groupWaitingList;
	}
	
	public void setGroupWaitingList(boolean groupWaitingList) {
		this.groupWaitingList = groupWaitingList;
	}
}