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
package org.olat.course.assessment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.core.id.Persistable;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

/**
 * 
 * Initial date: 17 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentmodetocurriculumelement")
@Table(name="o_as_mode_course_to_cur_el")
public class AssessmentModeToCurriculumElementImpl implements Persistable, AssessmentModeToCurriculumElement {

	private static final long serialVersionUID = -2236435048542112653L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@ManyToOne(targetEntity=AssessmentModeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessment_mode_id", nullable=false, insertable=true, updatable=false)
	private AssessmentMode assessmentMode;
	
	@ManyToOne(targetEntity=CurriculumElementImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_cur_element_id", nullable=false, insertable=true, updatable=false)
	private CurriculumElement curriculumElement;
	

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}
	
	public void setAssessmentMode(AssessmentMode assessmentMode) {
		this.assessmentMode = assessmentMode;
	}

	@Override
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 6170178 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssessmentModeToCurriculumElementImpl) {
			AssessmentModeToCurriculumElementImpl rel = (AssessmentModeToCurriculumElementImpl)obj;
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
