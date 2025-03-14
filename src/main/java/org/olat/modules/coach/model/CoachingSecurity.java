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
package org.olat.modules.coach.model;

/**
 * 
 * Initial date: 14 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingSecurity {
	
	private final boolean masterCoach;
	private final boolean coach;
	private final boolean teacher;
	private final boolean isUserRelationSource;
	private final boolean lineManager;
	private final boolean educationManager;

	public CoachingSecurity(boolean masterCoach, boolean coach, boolean teacher, boolean isUserRelationSource, 
							boolean lineManager, boolean educationManager) {
		this.masterCoach = masterCoach;
		this.coach = coach;
		this.teacher = teacher;
		this.isUserRelationSource = isUserRelationSource;
		this.lineManager = lineManager;
		this.educationManager = educationManager;
	}

	public boolean isMasterCoachForLectures() {
		return masterCoach;
	}

	public boolean isCoach() {
		return coach;
	}

	public boolean isTeacher() {
		return teacher;
	}

	public boolean isUserRelationSource() {
		return isUserRelationSource;
	}

	public boolean isLineManager() {
		return lineManager;
	}

	public boolean isEducationManager() {
		return educationManager;
	}
}
