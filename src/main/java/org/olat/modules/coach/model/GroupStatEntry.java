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
 * Dummy bean to transport statistic values about group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupStatEntry {
	// s.repoKey, 
	private Long groupKey;
	private String groupName;
	private int countCourses;
	private int countStudents;
	private int countPassed;
	private int countFailed;
	private int countNotAttempted;
	private Float averageScore;
	private int initialLaunch;
	
	public Long getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(Long groupKey) {
		this.groupKey = groupKey;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getCountCourses() {
		return countCourses;
	}

	public void setCountCourses(int countCourses) {
		this.countCourses = countCourses;
	}

	public int getCountStudents() {
		return countStudents;
	}
	
	public void setCountStudents(int countStudents) {
		this.countStudents = countStudents;
	}
	
	public int getCountDistinctStudents() {
		return countStudents;
	}
	
	public void setCountDistinctStudents(int countStudents) {
		this.countStudents = countStudents;
	}
	
	public int getCountPassed() {
		return countPassed;
	}
	
	public void setCountPassed(int countPassed) {
		this.countPassed = countPassed;
	}
	
	public int getCountFailed() {
		return countFailed;
	}
	
	public void setCountFailed(int countFailed) {
		this.countFailed = countFailed;
	}
	
	public int getCountNotAttempted() {
		return countNotAttempted;
	}
	
	public void setCountNotAttempted(int countNotAttempted) {
		this.countNotAttempted = countNotAttempted;
	}
	
	public Float getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(Float averageScore) {
		this.averageScore = averageScore;
	}

	public int getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(int initialLaunch) {
		this.initialLaunch = initialLaunch;
	}
}
