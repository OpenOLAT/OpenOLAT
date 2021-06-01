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
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 13 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallAndCoach {
	
	private String coach;
	private final LectureBlock block;
	private final RepositoryEntry entry;
	private final AbsenceNotice absenceNotice;
	private final LectureBlockRollCall rollCall;
	
	public LectureBlockRollCallAndCoach(String coach, LectureBlock block, RepositoryEntry entry,
			LectureBlockRollCall rollCall, AbsenceNotice absenceNotice) {
		this.coach = coach;
		this.block = block;
		this.entry = entry;
		this.rollCall = rollCall;
		this.absenceNotice = absenceNotice;
	}

	public LectureBlock getLectureBlock() {
		return block;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}

	public LectureBlockRollCall getRollCall() {
		return rollCall;
	}

	public String getCoach() {
		return coach;
	}
	
	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}
	
}
