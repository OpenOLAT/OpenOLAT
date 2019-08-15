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
package org.olat.modules.lecture.model;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesMemberSearchParameters {
	
	private IdentityRef manager;
	private IdentityRef masterCoach;
	private IdentityRef teacher;

	private String searchString;
	private RepositoryEntryRef repositoryEntry;
	private CurriculumElement curriculumElement;
	
	/**
	 * Identity which want to access the data (for permission restrictions)
	 * @return
	 */
	public IdentityRef getManager() {
		return manager;
	}

	/**
	 * Identity which want to access the data (for permission restrictions)
	 * 
	 * @param identity
	 */
	public void setManager(IdentityRef manager) {
		this.manager = manager;
	}

	public IdentityRef getTeacher() {
		return teacher;
	}

	public void setTeacher(IdentityRef teacher) {
		this.teacher = teacher;
	}

	public IdentityRef getMasterCoach() {
		return masterCoach;
	}

	public void setMasterCoach(IdentityRef masterCoach) {
		this.masterCoach = masterCoach;
	}
	
	public void setViewAs(IdentityRef identity, LectureRoles role) {
		switch(role) {
			case lecturemanager: setManager(identity); break;
			case mastercoach: setMasterCoach(identity); break;
			case teacher:
			default: setTeacher(identity); break;
		}
	}

	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public RepositoryEntryRef getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntryRef repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}
}
