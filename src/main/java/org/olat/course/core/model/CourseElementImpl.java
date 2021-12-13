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
package org.olat.course.core.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.core.CourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="courseelement")
@Table(name="o_course_element")
public class CourseElementImpl implements Persistable, ModifiedInfo, CreateInfo, CourseElement {

	private static final long serialVersionUID = -8028624433304128556L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="c_type", nullable=false, insertable=true, updatable=false)
	private String type;
	@Column(name="c_short_title", nullable=false, insertable=true, updatable=true)
	private String shortTitle;
	@Column(name="c_long_title", nullable=false, insertable=true, updatable=true)
	private String longTitle;
	
	@Column(name="c_assesseable", nullable=false, insertable=true, updatable=true)
	private boolean assesseable;
	@Enumerated(EnumType.STRING)
	@Column(name="c_score_mode", nullable=false, insertable=true, updatable=true)
	private Mode scoreMode;
	@Enumerated(EnumType.STRING)
	@Column(name="c_passed_mode", nullable=false, insertable=true, updatable=true)
	private Mode passedMode;
	@Column(name="c_cut_value", nullable=true, insertable=true, updatable=true)
	private BigDecimal cutValue;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry repositoryEntry;
	@Column(name="c_subident", nullable=false, insertable=true, updatable=false)
	private String subIdent;
	
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
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String getShortTitle() {
		return shortTitle;
	}
	
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	
	@Override
	public String getLongTitle() {
		return longTitle;
	}
	
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}
	
	@Override
	public boolean isAssesseable() {
		return assesseable;
	}
	
	public void setAssesseable(boolean assesseable) {
		this.assesseable = assesseable;
	}
	
	@Override
	public Mode getScoreMode() {
		return scoreMode;
	}

	public void setScoreMode(Mode scoreMode) {
		this.scoreMode = scoreMode;
	}

	@Override
	public Mode getPassedMode() {
		return passedMode;
	}

	public void setPassedMode(Mode passedMode) {
		this.passedMode = passedMode;
	}

	@Override
	public BigDecimal getCutValue() {
		return cutValue;
	}
	
	public void setCutValue(BigDecimal cutValue) {
		this.cutValue = cutValue;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
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
		CourseElementImpl other = (CourseElementImpl) obj;
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
