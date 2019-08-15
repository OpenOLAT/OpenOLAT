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

import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.repository.RepositoryEntry;

/**
 * The statistics are centered around a lecture block.
 * 
 * Initial date: 6 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockBlockStatistics {
	
	private final LectureBlock lectureBlock;
	private final RepositoryEntry entry;
	
	private final long numOfParticipants;
	private int numOfPresents = 0;
	private int numOfAbsents = 0;
	
	private int numOfAbsenceAuthorized = 0;
	private int numOfAbsenceUnauthorized = 0;
	private int numOfNoticeOfAbsenceAuthorized = 0;
	private int numOfNoticeOfAbsenceUnauthorized = 0;
	private int numOfDispensationAuthorized = 0;
	private int numOfDispensationUnauthorized = 0;
	
	public LectureBlockBlockStatistics(LectureBlock lectureBlock, long numOfParticipants) {
		this.lectureBlock = lectureBlock;
		this.entry = lectureBlock.getEntry();
		this.numOfParticipants = numOfParticipants;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public long getNumOfParticipants() {
		return numOfParticipants;
	}

	public int getNumOfAbsents() {
		return numOfAbsents;
	}
	
	public int getNumOfPresents() {
		return numOfPresents;
	}

	public int getNumOfAbsenceAuthorized() {
		return numOfAbsenceAuthorized;
	}

	public int getNumOfAbsenceUnauthorized() {
		return numOfAbsenceUnauthorized;
	}

	public int getNumOfNoticeOfAbsenceAuthorized() {
		return numOfNoticeOfAbsenceAuthorized;
	}

	public int getNumOfNoticeOfAbsenceUnauthorized() {
		return numOfNoticeOfAbsenceUnauthorized;
	}

	public int getNumOfDispensationAuthorized() {
		return numOfDispensationAuthorized;
	}

	public int getNumOfDispensationUnauthorized() {
		return numOfDispensationUnauthorized;
	}

	public void aggregate(LectureBlockRollCall rollCall, AbsenceNotice notice) {
		if(notice != null) {
			if(notice.getNoticeType() == AbsenceNoticeType.absence) {
				if(notice.getAbsenceAuthorized() && notice.getAbsenceAuthorized().booleanValue()) {
					numOfAbsenceAuthorized++;
				} else {
					numOfAbsenceUnauthorized++;
				}
			} else if(notice.getNoticeType() == AbsenceNoticeType.notified) {
				if(notice.getAbsenceAuthorized() && notice.getAbsenceAuthorized().booleanValue()) {
					numOfNoticeOfAbsenceAuthorized++;
				} else {
					numOfNoticeOfAbsenceUnauthorized++;
				}
			} else if(notice.getNoticeType() == AbsenceNoticeType.dispensation) {
				if(notice.getAbsenceAuthorized() && notice.getAbsenceAuthorized().booleanValue()) {
					numOfDispensationAuthorized++;
				} else {
					numOfDispensationUnauthorized++;
				}
			}
			numOfAbsents++;
		} else if(rollCall != null) {
			if(rollCall.getLecturesAbsentNumber() > 0) {
				if(rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue()) {
					numOfAbsenceAuthorized++;
				} else {
					numOfAbsenceUnauthorized++;
				}
				numOfAbsents++;
			} else {
				numOfPresents++;
			}
		}
	}
}
