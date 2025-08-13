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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;

/**
 * Search per default on course with lecture configured.
 * 
 * Initial date: 27 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlockSearchParameters {
	
	private String searchString;
	private Date startDate;
	private Date endDate;
	private IdentityRef teacher;
	private IdentityRef manager;
	private IdentityRef participant;
	private IdentityRef masterCoach;
	private List<? extends IdentityRef> teachersList;
	private RepositoryEntry entry;
	private Boolean withTeachers;
	private List<CurriculumRef> curriculums;
	private String curriculumElementPath;
	private CurriculumElementRef curriculumElement;
	private List<LectureRollCallStatus> rollCallStatus;
	private List<LectureBlockStatus> lectureBlockStatus;
	private List<LectureBlockVirtualStatus> virtualStatus;
	private List<LectureBlockRef> lectureBlocks;
	private boolean inSomeCurriculum = false;
	
	private boolean configuredEntry = true;
	private List<TaxonomyLevelRef> taxonomyLevels;

	public LecturesBlockSearchParameters() {
		//
	}
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Boolean getWithTeachers() {
		return withTeachers;
	}

	public void setWithTeachers(Boolean withTeachers) {
		this.withTeachers = withTeachers;
	}

	public List<LectureRollCallStatus> getRollCallStatus() {
		return rollCallStatus;
	}
	
	public List<LectureBlockVirtualStatus> getVirtualStatus() {
		return virtualStatus;
	}
	
	public void setVirtualStatus(List<LectureBlockVirtualStatus> status) {
		this.virtualStatus = status;
	}

	public void addRollCallStatus(LectureRollCallStatus... status) {
		if(rollCallStatus == null) {
			rollCallStatus = new ArrayList<>();
		}
		if(status != null && status.length > 0) {
			for(LectureRollCallStatus s:status) {
				if(s != null) {
					rollCallStatus.add(s);
				}
			}
		}
	}

	public List<LectureBlockStatus> getLectureBlockStatus() {
		return lectureBlockStatus;
	}

	public void addLectureBlockStatus(LectureBlockStatus... status) {
		if(lectureBlockStatus == null) {
			lectureBlockStatus = new ArrayList<>();
		}
		if(status != null && status.length > 0) {
			for(LectureBlockStatus s:status) {
				if(s != null) {
					lectureBlockStatus.add(s);
				}
			}
		}
	}

	/**
	 * Identity which want to access the data (for permission restrictions)
	 * @return
	 */
	public IdentityRef getManager() {
		return manager;
	}

	/**
	 * Identity which want to access the data (for permission restrictions)
	 * 
	 * @param identity
	 */
	public void setManager(IdentityRef manager) {
		this.manager = manager;
	}

	public IdentityRef getMasterCoach() {
		return masterCoach;
	}

	public void setMasterCoach(IdentityRef masterCoach) {
		this.masterCoach = masterCoach;
	}

	public IdentityRef getTeacher() {
		return teacher;
	}

	public void setTeacher(IdentityRef teacher) {
		this.teacher = teacher;
	}

	public IdentityRef getParticipant() {
		return participant;
	}

	public void setParticipant(IdentityRef participant) {
		this.participant = participant;
	}
	
	public void setViewAs(IdentityRef identity, LectureRoles role) {
		switch(role) {
			case lecturemanager: setManager(identity); break;
			case mastercoach: setMasterCoach(identity); break;
			case teacher: setTeacher(identity); break;
			case participant:
			default: setParticipant(identity);
		}
	}

	public List<? extends IdentityRef> getTeachersList() {
		return teachersList;
	}

	public void setTeachersList(List<? extends IdentityRef> teachersList) {
		this.teachersList = teachersList;
	}

	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}

	public void setRepositoryEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public boolean isLectureConfiguredRepositoryEntry() {
		return configuredEntry;
	}

	public void setLectureConfiguredRepositoryEntry(boolean enabled) {
		this.configuredEntry = enabled;
	}

	public List<CurriculumRef> getCurriculums() {
		return curriculums;
	}

	public void setCurriculums(List<? extends CurriculumRef> curriculums) {
		this.curriculums = new ArrayList<>(curriculums);
	}

	public CurriculumElementRef getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElementRef curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	public String getCurriculumElementPath() {
		return curriculumElementPath;
	}

	public void setCurriculumElementPath(String curriculumElementPath) {
		this.curriculumElementPath = curriculumElementPath;
	}

	public boolean isInSomeCurriculum() {
		return inSomeCurriculum;
	}

	public void setInSomeCurriculum(boolean inSomeCurriculum) {
		this.inSomeCurriculum = inSomeCurriculum;
	}

	public List<LectureBlockRef> getLectureBlocks() {
		return lectureBlocks;
	}

	public void setLectureBlocks(List<? extends LectureBlockRef> lectureBlocks) {
		this.lectureBlocks = new ArrayList<>(lectureBlocks);
	}

	public void setTaxonomyLevels(List<TaxonomyLevelRef> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	public List<TaxonomyLevelRef> getTaxonomyLevels() {
		return taxonomyLevels;
	}
}
