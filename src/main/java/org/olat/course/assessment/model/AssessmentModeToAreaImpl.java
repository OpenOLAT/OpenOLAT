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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaImpl;

/**
 * 
 * Initial date: 19.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseassessmentmodetoarea")
@Table(name="o_as_mode_course_to_area")
public class AssessmentModeToAreaImpl implements Persistable, AssessmentModeToArea {

	private static final long serialVersionUID = 1119811433224501751L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
			@Parameter(name="sequence_name", value="hibernate_unique_key"),
			@Parameter(name="force_table_use", value="true"),
			@Parameter(name="optimizer", value="legacy-hilo"),
			@Parameter(name="value_column", value="next_hi"),
			@Parameter(name="increment_size", value="32767"),
			@Parameter(name="initial_value", value="32767")
		})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@ManyToOne(targetEntity=AssessmentModeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessment_mode_id", nullable=false, insertable=true, updatable=false)
	private AssessmentMode assessmentMode;
	
	@ManyToOne(targetEntity=BGAreaImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_area_id", nullable=false, insertable=true, updatable=false)
	private BGArea area;

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	public void setAssessmentMode(AssessmentMode assessmentMode) {
		this.assessmentMode = assessmentMode;
	}

	@Override
	public BGArea getArea() {
		return area;
	}

	public void setArea(BGArea area) {
		this.area = area;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -6765682 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentModeToAreaImpl) {
			AssessmentModeToAreaImpl mode = (AssessmentModeToAreaImpl)obj;
			return getKey() != null && getKey().equals(mode.getKey());	
		}
		return false;
	}
}
