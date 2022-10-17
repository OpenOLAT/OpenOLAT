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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.PerformanceClass;

/**
 * 
 * Initial date: 21 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gradeperformanceclass")
@Table(name="o_gr_performance_class")
public class PerformanceClassImpl implements PerformanceClass, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = 7502962284527950849L;

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
	
	@Column(name="g_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="g_best_to_lowest", nullable=false, insertable=true, updatable=true)
	private int bestToLowest;
	@Column(name="g_passed", nullable=false, insertable=true, updatable=true)
	private boolean passed;
	
	@ManyToOne(targetEntity=GradeSystemImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_grade_system", nullable=false, insertable=true, updatable=false)
	private GradeSystem gradeSystem;
	
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
	public int getBestToLowest() {
		return bestToLowest;
	}
	
	@Override
	public void setBestToLowest(int bestToLowest) {
		this.bestToLowest = bestToLowest;
	}
	
	@Override
	public boolean isPassed() {
		return passed;
	}
	
	@Override
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	
	@Override
	public GradeSystem getGradeSystem() {
		return gradeSystem;
	}

	public void setGradeSystem(GradeSystem gradeSystem) {
		this.gradeSystem = gradeSystem;
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
		PerformanceClassImpl other = (PerformanceClassImpl) obj;
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

	@Override
	public int compareTo(PerformanceClass o) {
		return getBestToLowest() - o.getBestToLowest();
	}
	
}
