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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.UserEfficiencyStatement;

/**
 * 
 * Mapping used to save efficiency statement in import process where
 * the course / resource doesn't exists.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="effstatementstandalone")
@Table(name="o_as_eff_statement")
public class UserEfficiencyStatementStandalone implements Persistable, UserEfficiencyStatement, ModifiedInfo {

	private static final long serialVersionUID = 2996458434418813284L;
	
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
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="score", nullable=true, insertable=true, updatable=true)
	private Float score;
	@Column(name="grade", nullable=true, insertable=true, updatable=true)
	private String grade;
	@Column(name="grade_system_ident", nullable=true, insertable=true, updatable=true)
	private String gradeSystemIdent;
	@Column(name="performance_class_ident", nullable=true, insertable=true, updatable=true)
	private String performanceClassIdent;
	@Column(name="passed", nullable=true, insertable=true, updatable=true)
	private Boolean passed;
	@Column(name="total_nodes", nullable=true, insertable=true, updatable=true)
	private Integer totalNodes;
	@Column(name="attempted_nodes", nullable=true, insertable=true, updatable=true)
	private Integer attemptedNodes;
	@Column(name="passed_nodes", nullable=true, insertable=true, updatable=true)
	private Integer passedNodes;
	
	@Column(name="course_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="course_short_title", nullable=true, insertable=true, updatable=true)
	private String shortTitle;
	@Column(name="course_repo_key", nullable=true, insertable=true, updatable=true)
	private Long courseRepoKey;

	@Column(name="statement_xml", nullable=true, insertable=true, updatable=true)
	private String statementXml;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastusermodified", nullable=true, insertable=true, updatable=true)
	private Date lastUserModified;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastcoachmodified", nullable=true, insertable=true, updatable=true)
	private Date lastCoachModified;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, updatable=false)
	private Identity identity;
	@Column(name="fk_resource_id", nullable=true, insertable=true, updatable=true)
	private Long resourceKey;
	
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
	public Date getLastUserModified() {
		return lastUserModified;
	}

	public void setLastUserModified(Date lastUserModified) {
		this.lastUserModified = lastUserModified;
	}

	@Override
	public Date getLastCoachModified() {
		return lastCoachModified;
	}

	public void setLastCoachModified(Date lastCoachModified) {
		this.lastCoachModified = lastCoachModified;
	}

	@Override
	public Float getScore() {
		return score;
	}
		
	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	@Override
	public String getGradeSystemIdent() {
		return gradeSystemIdent;
	}

	public void setGradeSystemIdent(String gradeSystemIdent) {
		this.gradeSystemIdent = gradeSystemIdent;
	}

	@Override
	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	public void setPerformanceClassIdent(String performanceClassIdent) {
		this.performanceClassIdent = performanceClassIdent;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}
		
	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	@Override
	public Integer getTotalNodes() {
		return totalNodes;
	}

	public void setTotalNodes(Integer totalNodes) {
		this.totalNodes = totalNodes;
	}

	@Override
	public Integer getAttemptedNodes() {
		return attemptedNodes;
	}

	public void setAttemptedNodes(Integer attemptedNodes) {
		this.attemptedNodes = attemptedNodes;
	}

	@Override
	public Integer getPassedNodes() {
		return passedNodes;
	}

	public void setPassedNodes(Integer passedNodes) {
		this.passedNodes = passedNodes;
	}

	public String getStatementXml() {
		return statementXml;
	}

	public void setStatementXml(String statementXml) {
		this.statementXml = statementXml;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public Long getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(Long resourceKey) {
		this.resourceKey = resourceKey;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public Long getCourseRepoKey() {
		return courseRepoKey;
	}

	public void setCourseRepoKey(Long courseRepoKey) {
		this.courseRepoKey = courseRepoKey;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -82654 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserEfficiencyStatementStandalone) {
			UserEfficiencyStatementStandalone statement = (UserEfficiencyStatementStandalone)obj;
			return getKey() != null && getKey().equals(statement.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
