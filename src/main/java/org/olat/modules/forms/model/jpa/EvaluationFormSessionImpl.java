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
package org.olat.modules.forms.model.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.model.PageBodyImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="evaluationformsession")
@Table(name="o_eva_form_session")
public class EvaluationFormSessionImpl implements EvaluationFormSession, Persistable {

	private static final long serialVersionUID = 1311363640999376608L;

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
	
	@Column(name="e_status", nullable=false, insertable=true, updatable=true)
	private String status;
	
	@Column(name="e_submission_date", nullable=true, insertable=true, updatable=true)
	private Date submissionDate;
	@Column(name="e_first_submission_date", nullable=true, insertable=true, updatable=true)
	private Date firstSubmissionDate;
	@Column(name="e_email", nullable=true, insertable=true, updatable=true)
	private String email;
	@Column(name="e_firstname", nullable=true, insertable=true, updatable=true)
	private String firstname;
	@Column(name="e_lastname", nullable=true, insertable=true, updatable=true)
	private String lastname;
	@Column(name="e_age", nullable=true, insertable=true, updatable=true)
	private String age;
	@Column(name="e_gender", nullable=true, insertable=true, updatable=true)
	private String gender;
	@Column(name="e_org_unit", nullable=true, insertable=true, updatable=true)
	private String orgUnit;
	@Column(name="e_study_subject", nullable=true, insertable=true, updatable=true)
	private String studySubject;
	
	@ManyToOne(targetEntity=EvaluationFormSurveyImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_survey", nullable=true, insertable=true, updatable=true)
	private EvaluationFormSurvey survey;
	@OneToOne(targetEntity=EvaluationFormParticipationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_participation", nullable=true, insertable=true, updatable=true)
	private EvaluationFormParticipation participation;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	
	@ManyToOne(targetEntity=PageBodyImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_page_body", nullable=true, insertable=true, updatable=false)
	private PageBody pageBody;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_form_entry", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry formEntry;

	
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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Transient
	@Override
	public EvaluationFormSessionStatus getEvaluationFormSessionStatus() {
		if(StringHelper.containsNonWhitespace(status)) {
			return EvaluationFormSessionStatus.valueOf(status);
		}
		return null;
	}

	public void setEvaluationFormSessionStatus(EvaluationFormSessionStatus sessionStatus) {
		if(sessionStatus == null) {
			status = null;
		} else {
			status = sessionStatus.name();
		}
	}

	@Override
	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	@Override
	public Date getFirstSubmissionDate() {
		return firstSubmissionDate;
	}

	public void setFirstSubmissionDate(Date firstSubmissionDate) {
		this.firstSubmissionDate = firstSubmissionDate;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@Override
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@Override
	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	@Override
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	@Override
	public String getStudySubject() {
		return studySubject;
	}

	public void setStudySubject(String studySubject) {
		this.studySubject = studySubject;
	}

	@Override
	public EvaluationFormSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(EvaluationFormSurvey survey) {
		this.survey = survey;
	}

	@Override
	public EvaluationFormParticipation getParticipation() {
		return participation;
	}

	public void setParticipation(EvaluationFormParticipation participation) {
		this.participation = participation;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public PageBody getPageBody() {
		return pageBody;
	}

	public void setPageBody(PageBody pageBody) {
		this.pageBody = pageBody;
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	public void setFormEntry(RepositoryEntry formEntry) {
		this.formEntry = formEntry;
	}

	@Override
	public int hashCode() {
		return key == null ? -765972 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EvaluationFormSessionImpl) {
			EvaluationFormSessionImpl session = (EvaluationFormSessionImpl)obj;
			return getKey() != null && getKey().equals(session.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
