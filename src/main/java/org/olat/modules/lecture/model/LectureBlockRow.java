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

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRow implements LectureBlockRef {
	
	private final String teachers;
	private final boolean iamTeacher;
	private final String entryExternalRef;
	private final String entryDisplayname;
	private final LectureBlock lectureBlock;
	private boolean assessmentMode;
	
	private FormLink toolsLink;
	
	private DateChooser dateChooser;
	private MultipleSelectionElement teacherChooser;
	private TextElement locationElement;
	private TextElement titleElement;
	
	public LectureBlockRow(LectureBlock lectureBlock, String entryDisplayname, String externalRef,
			String teachers, boolean iamTeacher, boolean assessmentMode) {
		this.lectureBlock = lectureBlock;
		this.teachers = teachers;
		this.iamTeacher = iamTeacher;
		this.entryExternalRef = externalRef;
		this.entryDisplayname = entryDisplayname;
		this.assessmentMode = assessmentMode;
	}
	
	@Override
	public Long getKey() {
		return lectureBlock.getKey();
	}
	
	public boolean isIamTeacher() {
		return iamTeacher;
	}
	
	public String getEntryExternalRef() {
		return entryExternalRef;
	}
	
	public String getEntryDisplayname() {
		return entryDisplayname;
	}

	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public String getTeachers() {
		return teachers;
	}
	
	public boolean isAssessmentMode() {
		return assessmentMode;
	}
	
	public void setAssessmentMode(boolean assessmentMode) {
		this.assessmentMode = assessmentMode;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public DateChooser getDateChooser() {
		return dateChooser;
	}
	
	public void setDateChooser(DateChooser dateChooser) {
		this.dateChooser = dateChooser;
	}
	
	public MultipleSelectionElement getTeacherChooser() {
		return teacherChooser;
	}
	
	public void setTeacherChooser(MultipleSelectionElement teacherChooser) {
		this.teacherChooser = teacherChooser;
	}
	
	public TextElement getLocationElement() {
		return locationElement;
	}
	
	public void setLocationElement(TextElement locationElement) {
		this.locationElement = locationElement;
	}
	
	public TextElement getTitleElement() {
		return titleElement;
	}
	
	public void setTitleElement(TextElement titleElement) {
		this.titleElement = titleElement;
	}
}
