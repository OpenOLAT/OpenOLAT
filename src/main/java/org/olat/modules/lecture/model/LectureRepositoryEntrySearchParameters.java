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
import org.olat.modules.lecture.ui.LectureRoles;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositoryEntrySearchParameters {
	
	private String searchString;
	private IdentityRef teacher;
	private IdentityRef masterCoach;
	private IdentityRef manager;
	
	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;
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

	public IdentityRef getManager() {
		return manager;
	}

	public void setManager(IdentityRef manager) {
		this.manager = manager;
	}

	public void setViewAs(IdentityRef identity, LectureRoles role) {
		switch(role) {
			case lecturemanager: setManager(identity); break;
			case mastercoach: setMasterCoach(identity); break;
			default: setTeacher(identity); break;
		}
	}
	
	

}
