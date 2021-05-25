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
 * Statistics centered around a user.
 * 
 * Initial date: 13 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockStatistics {
	
	private final Long repoKey;
	private final String displayName;
	private final String externalRef;
	private final Long identityKey;
	private final Long lectureBlockKey;
	
	private long totalLectureBlocks = 0l;
	private long totalEffectiveLectures = 0l;
	private long totalPersonalPlannedLectures = 0l;
	
	private long totalAttendedLectures = 0l;
	private long totalAbsentLectures = 0l;
	private long totalAuthorizedAbsentLectures = 0l;
	private long totalDispensationLectures = 0l;
	private double attendanceRate;

	private final boolean calculateRate;
	private final double requiredRate;
	
	public LectureBlockStatistics(Long identityKey, Long lectureBlockKey, Long repoKey, String displayName, String externalRef,
			boolean calculateRate, double requiredRate) {
		this.repoKey = repoKey;
		this.displayName = displayName;
		this.externalRef = externalRef;
		this.identityKey = identityKey;
		this.lectureBlockKey = lectureBlockKey;
		this.calculateRate = calculateRate;
		this.requiredRate = requiredRate;
	}

	public Long getRepoKey() {
		return repoKey;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getExternalRef() {
		return externalRef;
	}
	
	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public boolean isCalculateRate() {
		return calculateRate;
	}

	public double getRequiredRate() {
		return requiredRate;
	}
	
	/**
	 * The number of lectures the user has followed (or was absent)
	 * or will follow in the future.
	 */
	public long getTotalPersonalPlannedLectures() {
		return totalPersonalPlannedLectures;
	}
	
	public void addTotalPersonalPlannedLectures(long lectures) {
		if(lectures > 0) {
			totalPersonalPlannedLectures += lectures;
		}
	}

	/**
	 * The number of lectures the user has followed (or was absent).
	 */
	public long getTotalEffectiveLectures() {
		return totalEffectiveLectures;
	}

	public void addTotalEffectiveLectures(long lectures) {
		if(lectures > 0) {
			totalEffectiveLectures += lectures;
		}
	}

	public long getTotalAttendedLectures() {
		return totalAttendedLectures;
	}

	public void addTotalAttendedLectures(long lectures) {
		if(lectures > 0) {
			totalAttendedLectures += lectures;
		}
	}

	public long getTotalAbsentLectures() {
		return totalAbsentLectures;
	}

	public void addTotalAbsentLectures(long lectures) {
		if(lectures > 0) {
			totalAbsentLectures += lectures;
		}
	}
	
	public long getTotalAuthorizedAbsentLectures() {
		return totalAuthorizedAbsentLectures;
	}
	
	public void addTotalAuthorizedAbsentLectures(long lectures) {
		if(lectures > 0) {
			totalAuthorizedAbsentLectures += lectures;
		}
	}
	
	public long getTotalDispensationLectures() {
		return totalDispensationLectures;
	}

	public void addTotalDispensationLectures(long lectures) {
		if(lectures > 0) {
			totalDispensationLectures = lectures;
		}
	}

	public long getTotalLectureBlocks() {
		return totalLectureBlocks;
	}

	public void addTotalLectureBlocks(long lectures) {
		if(lectures > 0) {
			totalLectureBlocks += lectures;
		}
	}

	public double getAttendanceRate() {
		return attendanceRate;
	}
	
	public void setAttendanceRate(double rate) {
		this.attendanceRate = rate;
	}
}
