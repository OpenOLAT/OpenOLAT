/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.teacher;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.Reference;
import org.olat.modules.lecture.ui.LectureListDetailsController;

/**
 * 
 * Initial date: 2 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockTeachersRow implements LectureBlockRef {

	private final LectureBlock lectureBlock;
	private final LectureBlockRow lectureBlockRow;
	
	private MultipleSelectionElement[] teachersEl;
	private LectureListDetailsController detailsCtrl;
	
	public LectureBlockTeachersRow(LectureBlockRow lectureBlockRow) {
		lectureBlock = lectureBlockRow.getLectureBlock();
		this.lectureBlockRow = lectureBlockRow;
	}

	@Override
	public Long getKey() {
		return lectureBlock.getKey();
	}
	
	public String getTitle() {
		return lectureBlock.getTitle();
	}
	
	public String getExternalId() {
		return lectureBlock.getExternalId();
	}
	
	public String getExternalRef() {
		return lectureBlock.getExternalRef();
	}
	
	public Date getStartDate() {
		return lectureBlock.getStartDate();
	}
	
	public Date getEndDate() {
		return lectureBlock.getEndDate();
	}
	
	public int getPlannedLecturesNumber() {
		return lectureBlock.getPlannedLecturesNumber();
	}
	
	public boolean isCompulsory() {
		return lectureBlock.isCompulsory();
	}
	
	public long getNumOfParticipants() {
		return lectureBlockRow.getNumOfParticipants();
	}
	
	public Reference getEntry() {
		return lectureBlockRow.getEntry();
	}
	
	public Reference getCurriculumElement() {
		return lectureBlockRow.getCurriculumElement();
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public LectureBlockRow getLectureBlockRow() {
		return lectureBlockRow;
	}
	
	public MultipleSelectionElement getTeacherEl(int col) {
		if(teachersEl != null && col >= 0 && col < teachersEl.length) {
			return teachersEl[col];
		}
		return null;
	}
	
	public void selectTeacher(int col, boolean val) {
		MultipleSelectionElement el = getTeacherEl(col);
		if(el != null) {
			el.select("on", val);
		}
	}
	
	public MultipleSelectionElement[] getTeachersEl() {
		return teachersEl;
	}

	public void setTeachersEl(MultipleSelectionElement[] teachersEl) {
		this.teachersEl = teachersEl;
	}

	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public LectureListDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(LectureListDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
