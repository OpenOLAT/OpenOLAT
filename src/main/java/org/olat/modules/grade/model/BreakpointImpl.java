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
package org.olat.modules.grade.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;

/**
 * 
 * Initial date: 25 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gradebreakpoint")
@Table(name="o_gr_breakpoint")
public class BreakpointImpl implements Breakpoint, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = 169608891173973806L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="g_value", nullable=true, insertable=true, updatable=true)
	private BigDecimal value;
	@Column(name="g_grade", nullable=true, insertable=true, updatable=true)
	private String grade;
	@Column(name="g_best_to_lowest", nullable=true, insertable=true, updatable=true)
	private Integer bestToLowest;
	
	@ManyToOne(targetEntity=GradeScaleImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_grade_scale", nullable=false, insertable=true, updatable=false)
	private GradeScale gradeScale;

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public BigDecimal getValue() {
		return value;
	}

	@Override
	public void setValue(BigDecimal value) {
		this.value = value;
	}

	@Override
	public String getGrade() {
		return grade;
	}

	@Override
	public void setGrade(String grade) {
		this.grade = grade;
	}

	@Override
	public Integer getBestToLowest() {
		return bestToLowest;
	}

	@Override
	public void setBestToLowest(Integer bestToLowest) {
		this.bestToLowest = bestToLowest;
	}

	@Override
	public GradeScale getGradeScale() {
		return gradeScale;
	}

	public void setGradeScale(GradeScale gradeScale) {
		this.gradeScale = gradeScale;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BreakpointImpl other = (BreakpointImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
