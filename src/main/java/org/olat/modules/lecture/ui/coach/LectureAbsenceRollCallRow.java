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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureAbsenceRollCallRow {

	private final Identity identity;
	private final LectureBlockRollCall rollCall;
	private final LectureBlock lectureBlock;
	private final AbsenceNotice absenceNotice;
	private final RepositoryEntry entry;
	private final String teachers;
	
	private Boolean authorized;
	
	private FormLink noticeLink;
	
	public LectureAbsenceRollCallRow(LectureBlock lectureBlock, RepositoryEntry entry, LectureBlockRollCall rollCall,
			AbsenceNotice absenceNotice, String teachers) {
		this.rollCall = rollCall;
		this.identity = rollCall.getIdentity();
		this.entry = entry;
		this.lectureBlock = lectureBlock;
		this.absenceNotice = absenceNotice;
		this.teachers = teachers;
	}
	
	public LectureBlockRollCall getRollCall() {
		return rollCall;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public String getTeachers() {
		return teachers;
	}

	public FormLink getNoticeLink() {
		return noticeLink;
	}
	
	public void setNoticeLink(FormLink noticeLink) {
		this.noticeLink = noticeLink;
	}

	public Boolean getAuthorized() {
		return authorized;
	}

	public void setAuthorized(Boolean authorized) {
		this.authorized = authorized;
	}
}
