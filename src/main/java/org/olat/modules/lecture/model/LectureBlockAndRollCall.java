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

import java.util.Date;

import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 29 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAndRollCall {
	
	private final String entryDisplayname;
	
	private final Long lectureBlockKey;
	private final String lectureBlockTitle;
	private final int plannedLectures;
	private final int effectiveLectures;
	private final Date startDate;
	
	private final Long rollCallKey;
	private final int lecturesAbsentNumber;
	private final int lecturesAttendedNumber;
	private final Boolean lecturesAuthorizedAbsent;
	
	private String coach;
	
	public LectureBlockAndRollCall(String entryDisplayname, LectureBlock lectureBlock, LectureBlockRollCall rollCall) {
		this.entryDisplayname = entryDisplayname;
		
		startDate = lectureBlock.getStartDate();
		lectureBlockKey = lectureBlock.getKey();
		lectureBlockTitle = lectureBlock.getTitle();
		plannedLectures = lectureBlock.getPlannedLecturesNumber();
		effectiveLectures = lectureBlock.getEffectiveLecturesNumber();
		
		if(rollCall == null) {
			rollCallKey = null;
			lecturesAttendedNumber = 0;
			lecturesAbsentNumber = 0;
			lecturesAuthorizedAbsent = null;
		} else {
			rollCallKey = rollCall.getKey();
			lecturesAttendedNumber = rollCall.getLecturesAttendedNumber();
			lecturesAbsentNumber = rollCall.getLecturesAbsentNumber();
			lecturesAuthorizedAbsent = rollCall.getAbsenceAuthorized();
		}
	}

	public Date getDate() {
		return startDate;
	}
	
	public String getEntryDisplayname() {
		return entryDisplayname;
	}
	
	public LectureBlockRef getLectureBlockRef() {
		return new LectureBlockRefImpl(lectureBlockKey);
	}
	
	public String getLectureBlockTitle() {
		return lectureBlockTitle;
	}
	
	public boolean isRollCalled() {
		return rollCallKey != null;
	}
	
	public int getLecturesAttendedNumber() {
		return lecturesAttendedNumber;
	}
	
	public int getLecturesAbsentNumber() {
		return lecturesAbsentNumber;
	}
	
	public Boolean getLecturesAuthorizedAbsent() {
		return lecturesAuthorizedAbsent;
	}

	public int getPlannedLecturesNumber() {
		return plannedLectures;
	}
	
	public int getEffectiveLecturesNumber() {
		return effectiveLectures;
	}
	
	public String getCoach() {
		return coach;
	}
	
	public void setCoach(String coach) {
		this.coach = coach;
	}
}
