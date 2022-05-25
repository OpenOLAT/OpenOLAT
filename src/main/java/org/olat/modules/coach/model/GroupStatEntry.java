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

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Dummy bean to transport statistic values about group
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupStatEntry {

	private final Long groupKey;
	private final String groupName;
	private int countCourses = 0;
	private int countStudents = 0;
	private int countDistinctStudents = 0;
	private int countPassed = 0;
	private int countFailed = 0;
	private int countNotAttempted = 0;
	private int countScore = 0;
	private Float averageScore;
	private double sumScore = 0.0d;
	private int initialLaunch = 0;
	
	private Set<Long> repoIds = new HashSet<>();
	
	public GroupStatEntry(Long groupKey, String groupName) {
		this.groupKey = groupKey;
		this.groupName = groupName;
	}
	
	public Long getGroupKey() {
		return groupKey;
	}

	public String getGroupName() {
		return groupName;
	}

	public Set<Long> getRepoIds() {
		return repoIds;
	}

	public void setRepoIds(Set<Long> repoIds) {
		this.repoIds = repoIds;
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
		return countDistinctStudents;
	}
	
	public void setCountDistinctStudents(int countDistinctStudents) {
		this.countDistinctStudents = countDistinctStudents;
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
	
	public int getCountScore() {
		return countScore;
	}

	public void setCountScore(int countScore) {
		this.countScore = countScore;
	}

	public Float getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(Float averageScore) {
		this.averageScore = averageScore;
	}

	public double getSumScore() {
		return sumScore;
	}

	public void setSumScore(double sumScore) {
		this.sumScore = sumScore;
	}

	public int getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(int initialLaunch) {
		this.initialLaunch = initialLaunch;
	}
}
