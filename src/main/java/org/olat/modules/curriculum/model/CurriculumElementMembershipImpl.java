/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.model;

import org.olat.modules.curriculum.CurriculumElementMembership;

/**
 * 
 * Initial date: 8 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMembershipImpl implements CurriculumElementMembership {
	
	private final Long identityKey;
	private final Long curriculumElementKey;
	
	private boolean curriculumElementOwner;
	private boolean repositoryEntryOwner;
	private boolean coach;
	private boolean participant;
	private boolean masterCoach;

	public CurriculumElementMembershipImpl(Long identityKey, Long curriculumElementKey) {
		this.identityKey = identityKey;
		this.curriculumElementKey = curriculumElementKey;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	@Override
	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}
	
	@Override
	public boolean hasMembership() {
		return repositoryEntryOwner || coach || participant || curriculumElementOwner;
	}

	@Override
	public boolean isRepositoryEntryOwner() {
		return repositoryEntryOwner;
	}

	public void setRepositoryEntryOwner(boolean repositoryEntryOwner) {
		this.repositoryEntryOwner = repositoryEntryOwner;
	}

	@Override
	public boolean isCoach() {
		return coach;
	}

	public void setCoach(boolean coach) {
		this.coach = coach;
	}

	@Override
	public boolean isParticipant() {
		return participant;
	}

	public void setParticipant(boolean participant) {
		this.participant = participant;
	}

	@Override
	public boolean isCurriculumElementOwner() {
		return curriculumElementOwner;
	}

	public void setCurriculumElementOwner(boolean curriculumElementOwner) {
		this.curriculumElementOwner = curriculumElementOwner;
	}

	@Override
	public boolean isMasterCoach() {
		return masterCoach;
	}
	
	public void setMasterCoach(boolean masterCoach) {
		this.masterCoach = masterCoach;
	}

	@Override
	public int hashCode() {
		return (identityKey == null ? 792515 : identityKey.hashCode())
				+ (curriculumElementKey == null ? 12121 : curriculumElementKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementMembershipImpl) {
			CurriculumElementMembershipImpl el = (CurriculumElementMembershipImpl)obj;
			return identityKey != null && identityKey.equals(el.identityKey)
					&& curriculumElementKey != null && curriculumElementKey.equals(el.curriculumElementKey);
		}
		return false;
	}
}
