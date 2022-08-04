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
package org.olat.modules.lecture.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 3 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "lectureBlockRollCallVO")
public class LectureBlockStatisticsVO {

	private Long identityKey;
	
	private long totalLectureBlocks = 0l;
	private long totalEffectiveLectures = 0l;
	private long totalPersonalPlannedLectures = 0l;
	
	private long totalAttendedLectures = 0l;
	private long totalAbsentLectures = 0l;
	private long totalAuthorizedAbsentLectures = 0l;
	private long totalDispensationLectures = 0l;
	private double attendanceRate;
	
	public LectureBlockStatisticsVO() {
		//
	}
	
	public static final LectureBlockStatisticsVO valueOf(LectureBlockStatistics stats) {
		LectureBlockStatisticsVO vo = new LectureBlockStatisticsVO();
		vo.setIdentityKey(stats.getIdentityKey());
		vo.setTotalLectureBlocks(stats.getTotalLectureBlocks());
		vo.setTotalEffectiveLectures(stats.getTotalEffectiveLectures());
		vo.setTotalPersonalPlannedLectures(stats.getTotalPersonalPlannedLectures());
		vo.setTotalAttendedLectures(stats.getTotalAttendedLectures());
		vo.setTotalAbsentLectures(stats.getTotalAbsentLectures());
		vo.setTotalAuthorizedAbsentLectures(stats.getTotalAuthorizedAbsentLectures());
		vo.setTotalDispensationLectures(stats.getTotalDispensationLectures());
		vo.setAttendanceRate(stats.getAttendanceRate());
		return vo;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public long getTotalLectureBlocks() {
		return totalLectureBlocks;
	}

	public void setTotalLectureBlocks(long totalLectureBlocks) {
		this.totalLectureBlocks = totalLectureBlocks;
	}

	public long getTotalEffectiveLectures() {
		return totalEffectiveLectures;
	}

	public void setTotalEffectiveLectures(long totalEffectiveLectures) {
		this.totalEffectiveLectures = totalEffectiveLectures;
	}

	public long getTotalPersonalPlannedLectures() {
		return totalPersonalPlannedLectures;
	}

	public void setTotalPersonalPlannedLectures(long totalPersonalPlannedLectures) {
		this.totalPersonalPlannedLectures = totalPersonalPlannedLectures;
	}

	public long getTotalAttendedLectures() {
		return totalAttendedLectures;
	}

	public void setTotalAttendedLectures(long totalAttendedLectures) {
		this.totalAttendedLectures = totalAttendedLectures;
	}

	public long getTotalAbsentLectures() {
		return totalAbsentLectures;
	}

	public void setTotalAbsentLectures(long totalAbsentLectures) {
		this.totalAbsentLectures = totalAbsentLectures;
	}

	public long getTotalAuthorizedAbsentLectures() {
		return totalAuthorizedAbsentLectures;
	}

	public void setTotalAuthorizedAbsentLectures(long totalAuthorizedAbsentLectures) {
		this.totalAuthorizedAbsentLectures = totalAuthorizedAbsentLectures;
	}

	public long getTotalDispensationLectures() {
		return totalDispensationLectures;
	}

	public void setTotalDispensationLectures(long totalDispensationLectures) {
		this.totalDispensationLectures = totalDispensationLectures;
	}

	public double getAttendanceRate() {
		return attendanceRate;
	}

	public void setAttendanceRate(double attendanceRate) {
		this.attendanceRate = attendanceRate;
	}
}
