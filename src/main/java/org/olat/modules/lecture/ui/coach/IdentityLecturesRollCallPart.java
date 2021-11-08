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
package org.olat.modules.lecture.ui.coach;

import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.modules.lecture.ui.component.RollCallItem;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityLecturesRollCallPart implements RollCallItem {
	
	private final boolean participate;
	private final LectureBlock lectureBlock;
	private final LectureBlockRollCall rollCall;
	private LectureBlockRollCallStatusItem statusItem;
	
	public IdentityLecturesRollCallPart(LectureBlock lectureBlock, boolean participate, LectureBlockRollCall rollCall) {
		this.rollCall = rollCall;
		this.participate = participate;
		this.lectureBlock = lectureBlock;
	}
	
	public boolean isParticipate() {
		return participate;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	public AbsenceNotice getAbsenceNotice() {
		return null;// not implemented
	}

	@Override
	public LectureBlockRollCall getRollCall() {
		return rollCall;
	}

	@Override
	public int getPlannedLecturesNumber() {
		return lectureBlock.getCalculatedLecturesNumber();
	}

	@Override
	public int getLecturesAttendedNumber() {
		int attended = rollCall == null ? 0 : rollCall.getLecturesAttendedNumber();
		if(attended < 0) {
			attended = 0;
		}
		return attended;
	}

	@Override
	public int getLecturesAbsentNumber() {
		int absences = rollCall == null ? 0 : rollCall.getLecturesAbsentNumber();
		if(absences < 0) {
			absences = 0;
		}
		return absences;
	}

	public LectureBlockRollCallStatusItem getStatusItem() {
		return statusItem;
	}

	public void setStatusItem(LectureBlockRollCallStatusItem statusItem) {
		this.statusItem = statusItem;
	}

}
