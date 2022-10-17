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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.Rounding;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gradesystem")
@Table(name="o_gr_grade_system")
public class GradeSystemImpl implements GradeSystem, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -594382150645203845L;

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

	@Column(name="g_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="g_predefined", nullable=false, insertable=true, updatable=false)
	private boolean predefined;
	@Enumerated(EnumType.STRING)
	@Column(name="g_type", nullable=false, insertable=true, updatable=true)
	private GradeSystemType type;
	@Column(name="g_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="g_has_passed", nullable=false, insertable=true, updatable=true)
	private boolean hasPassed;
	@Enumerated(EnumType.STRING)
	@Column(name="g_resolution", nullable=true, insertable=true, updatable=true)
	private NumericResolution resolution;
	@Enumerated(EnumType.STRING)
	@Column(name="g_rounding", nullable=true, insertable=true, updatable=true)
	private Rounding rounding;
	@Column(name="g_best_grade", nullable=true, insertable=true, updatable=true)
	private Integer bestGrade;
	@Column(name="g_lowest_grade", nullable=true, insertable=true, updatable=true)
	private Integer lowestGrade;
	@Column(name="g_cut_value", nullable=true, insertable=true, updatable=true)
	private BigDecimal cutValue;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
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
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public boolean isPredefined() {
		return predefined;
	}

	public void setPredefined(boolean predefined) {
		this.predefined = predefined;
	}

	@Override
	public GradeSystemType getType() {
		return type;
	}

	@Override
	public void setType(GradeSystemType type) {
		this.type = type;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean hasPassed() {
		return hasPassed;
	}

	@Override
	public void setPassed(boolean passed) {
		this.hasPassed = passed;
	}

	@Override
	public NumericResolution getResolution() {
		return resolution;
	}

	@Override
	public void setResolution(NumericResolution resolution) {
		this.resolution = resolution;
	}

	@Override
	public Rounding getRounding() {
		return rounding;
	}

	@Override
	public void setRounding(Rounding rounding) {
		this.rounding = rounding;
	}

	@Override
	public Integer getBestGrade() {
		return bestGrade;
	}

	@Override
	public void setBestGrade(Integer bestGrade) {
		this.bestGrade = bestGrade;
	}

	@Override
	public Integer getLowestGrade() {
		return lowestGrade;
	}

	@Override
	public void setLowestGrade(Integer lowestGrade) {
		this.lowestGrade = lowestGrade;
	}

	@Override
	public BigDecimal getCutValue() {
		return cutValue;
	}

	@Override
	public void setCutValue(BigDecimal cutValue) {
		this.cutValue = cutValue;
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
		GradeSystemImpl other = (GradeSystemImpl) obj;
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
