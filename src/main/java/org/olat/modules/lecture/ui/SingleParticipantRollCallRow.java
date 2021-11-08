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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.modules.lecture.ui.component.RollCallItem;

/**
 * 
 * Initial date: 30 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleParticipantRollCallRow implements RollCallRow, RollCallItem {
	
	private final int numOfLectures;
	private final LectureBlock lectureBlock;
	private final AbsenceNotice absenceNotice;
	private LectureBlockRollCall rollCall;
	private List<Identity> teachers;

	private FormLink allLink;
	private FormLink noticeLink;
	private FormLink reasonLink;
	private TextElement commentEl;
	private StaticTextElement numOfAbsencesEl;
	private MultipleSelectionElement[] checks;
	private MultipleSelectionElement authorizedAbsence;
	private FormLayoutContainer authorizedAbsenceCont;
	private LectureBlockRollCallStatusItem rollCallStatusEl;
	
	public SingleParticipantRollCallRow(LectureBlock lectureBlock, AbsenceNotice absenceNotice, int numOfLectures, List<Identity> teachers) {
		this.lectureBlock = lectureBlock;
		this.absenceNotice = absenceNotice;
		this.numOfLectures = numOfLectures;
		this.teachers = teachers;
	}
	
	public int getNumOfLectures() {
		return numOfLectures;
	}
	
	public String getEntryDisplayname() {
		return lectureBlock.getEntry().getDisplayname();
	}
	
	public String getEntryExternalRef() {
		return lectureBlock.getEntry().getExternalRef();
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}

	public List<Identity> getTeachers() {
		return teachers;
	}

	@Override
	public LectureBlockRollCall getRollCall() {
		return rollCall;
	}
	
	public void setRollCall(LectureBlockRollCall rollCall) {
		this.rollCall = rollCall;
	}
	
	@Override
	public int getPlannedLecturesNumber() {
		return getChecks().length;
	}

	@Override
	public int getLecturesAttendedNumber() {
		return getChecks().length - getLecturesAbsentNumber();
	}

	@Override
	public int getLecturesAbsentNumber() {
		int numOfChecks = getChecks().length;
		int absence = 0;
		for(int j=0; j<numOfChecks; j++) {
			if(getCheck(j).isAtLeastSelected(1)) {
				absence++;
			}
		}
		return absence;
	}

	@Override
	public MultipleSelectionElement[] getChecks() {
		return checks;
	}
	
	public int getIndexOfCheck(MultipleSelectionElement check) {
		if(checks != null && check != null) {
			for(int i=checks.length; i-->0; ) {
				if(checks[i] == check) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public MultipleSelectionElement getCheck(int pos) {
		if(checks != null && pos >= 0 && pos < checks.length) {
			return checks[pos];
		}
		return null;
	}

	public void setChecks(MultipleSelectionElement[] checks) {
		this.checks = checks;
	}

	public FormLink getAllLink() {
		return allLink;
	}

	public void setAllLink(FormLink allLink) {
		this.allLink = allLink;
	}

	public FormLink getReasonLink() {
		return reasonLink;
	}

	public void setReasonLink(FormLink reasonLink) {
		this.reasonLink = reasonLink;
	}

	public FormLink getNoticeLink() {
		return noticeLink;
	}

	public void setNoticeLink(FormLink noticeLink) {
		this.noticeLink = noticeLink;
	}

	@Override
	public MultipleSelectionElement getAuthorizedAbsence() {
		return authorizedAbsence;
	}

	public void setAuthorizedAbsence(MultipleSelectionElement authorizedAbsence) {
		this.authorizedAbsence = authorizedAbsence;
	}

	public FormLayoutContainer getAuthorizedAbsenceCont() {
		return authorizedAbsenceCont;
	}

	public void setAuthorizedAbsenceCont(FormLayoutContainer authorizedAbsenceCont) {
		this.authorizedAbsenceCont = authorizedAbsenceCont;
	}

	public TextElement getCommentEl() {
		return commentEl;
	}

	public void setCommentEl(TextElement commentEl) {
		this.commentEl = commentEl;
	}

	public LectureBlockRollCallStatusItem getRollCallStatusEl() {
		return rollCallStatusEl;
	}

	public void setRollCallStatusEl(LectureBlockRollCallStatusItem rollCallStatusEl) {
		this.rollCallStatusEl = rollCallStatusEl;
	}

	public StaticTextElement getNumOfAbsencesEl() {
		return numOfAbsencesEl;
	}

	public void setNumOfAbsencesEl(StaticTextElement numOfAbsencesEl) {
		this.numOfAbsencesEl = numOfAbsencesEl;
	}
	
	
	
	
}
