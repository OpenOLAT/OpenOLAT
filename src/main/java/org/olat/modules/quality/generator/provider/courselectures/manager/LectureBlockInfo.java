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
package org.olat.modules.quality.generator.provider.courselectures.manager;

import java.util.Date;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockInfo {
	
	private final Long lectureBlockKey;
	private final Long teacherKey;
	private final Long courseRepoKey;
	private final Date lectureEndDate;
	private final Long lecturesTotal;
	private final Long firstLecture;
	private final Long lastLecture;

	public LectureBlockInfo(Long lectureBlockKey, Long teacherKey, Long courseRepoKey, Date lectureEndDate,
			Long lecturesTotal, Long firstLecture, Long lastLecture) {
		this.lectureBlockKey = lectureBlockKey;
		this.teacherKey = teacherKey;
		this.courseRepoKey = courseRepoKey;
		this.lectureEndDate = lectureEndDate;
		this.lecturesTotal = lecturesTotal;
		this.firstLecture = firstLecture != null ? 1 + firstLecture: 1;
		this.lastLecture = lastLecture != null ? 1 + lastLecture: 1;
	}

	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public Long getTeacherKey() {
		return teacherKey;
	}

	public Long getCourseRepoKey() {
		return courseRepoKey;
	}

	public Date getLectureEndDate() {
		return lectureEndDate;
	}

	public Long getLecturesTotal() {
		return lecturesTotal;
	}

	public Long getFirstLecture() {
		return firstLecture;
	}

	public Long getLastLecture() {
		return lastLecture;
	}

}
