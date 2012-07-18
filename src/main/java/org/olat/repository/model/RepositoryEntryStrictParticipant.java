package org.olat.repository.model;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryStrictParticipant extends PersistentObject {

	private static final long serialVersionUID = 3795108974268603941L;
	
	private Long repoParticipantKey;
	private Long groupParticipantKey;

	public Long getRepoParticipantKey() {
		return repoParticipantKey;
	}
	
	public void setRepoParticipantKey(Long repoParticipantKey) {
		this.repoParticipantKey = repoParticipantKey;
	}
	
	public Long getGroupParticipantKey() {
		return groupParticipantKey;
	}
	
	public void setGroupParticipantKey(Long groupParticipantKey) {
		this.groupParticipantKey = groupParticipantKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3768 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		return false;
	}
}
