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

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.ui.LectureListDetailsController;

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
	private LectureBlock lectureBlock;
	private boolean assessmentMode;
	private long numOfParticipants;
	private final Reference curriculumElement;
	private final Reference entry;
	
	private FormLink toolsLink;
	private LectureListDetailsController detailsCtrl;
	
	private DateChooser dateChooser;
	private FormLink teacherChooserLink;
	private TextElement locationElement;
	private List<Identity> teachersList;
	
	public LectureBlockRow(LectureBlock lectureBlock, String entryDisplayname, String externalRef,
			String teachers, boolean iamTeacher, Reference curriculumElement, Reference entry,
			long numOfParticipants, boolean assessmentMode) {
		this.lectureBlock = lectureBlock;
		this.teachers = teachers;
		this.entry = entry;
		this.numOfParticipants = numOfParticipants;
		this.curriculumElement = curriculumElement;
		
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
	
	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public void setNumOfParticipants(long participants) {
		this.numOfParticipants = participants;
	}
	
	public String getTeachers() {
		return teachers;
	}
	
	public Reference getCurriculumElement() {
		return curriculumElement;
	}

	public Reference getEntry() {
		return entry;
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
	
	public FormLink getTeacherChooserLink() {
		return teacherChooserLink;
	}
	
	public void setTeacherChooserLink(FormLink teacherChooserLink) {
		this.teacherChooserLink = teacherChooserLink;
	}
	
	public TextElement getLocationElement() {
		return locationElement;
	}
	
	public void setLocationElement(TextElement locationElement) {
		this.locationElement = locationElement;
	}
	
	public List<Identity> getTeachersList() {
		return teachersList;
	}
	
	public void setTeachersList(List<Identity> teachersList) {
		this.teachersList = teachersList;
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
