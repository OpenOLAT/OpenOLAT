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

/**
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureStatistics {
	
	private final Long repoKey;
	private final String displayName;
	
	private long totalLectureBlocks;
	private long totalPlannedLectures = 0l;
	private long totalAttendedLectures = 0l;
	private long totalAbsentLectures = 0l;
	
	public LectureStatistics(Long repoKey, String displayName) {
		this.repoKey = repoKey;
		this.displayName = displayName;
	}

	public Long getRepoKey() {
		return repoKey;
	}

	public String getDisplayName() {
		return displayName;
	}

	public long getTotalPlannedLectures() {
		return totalPlannedLectures;
	}
	
	public void addTotalPlannedLectures(long lectures) {
		totalPlannedLectures += lectures;
	}

	public long getTotalAttendedLectures() {
		return totalAttendedLectures;
	}
	
	public void addTotalAttendedLectures(long lectures) {
		totalAttendedLectures += lectures;
	}

	public long getTotalAbsentLectures() {
		return totalAbsentLectures;
	}
	
	public void addTotalAbsentLectures(long lectures) {
		totalAbsentLectures += lectures;
	}

	public long getTotalLectureBlocks() {
		return totalLectureBlocks;
	}

	public void addTotalLectureBlocks(long lectures) {
		totalLectureBlocks += lectures;
	}
	
	
}
