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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="evaluationformparticipation")
@Table(name="o_eva_form_participation")
public class EvaluationFormParticipationImpl implements EvaluationFormParticipation, Persistable {

	private static final long serialVersionUID = -4172370350430158356L;
	
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
	
	@Embedded
    @AttributeOverrides( {
    	@AttributeOverride(name="type", column = @Column(name="e_identifier_type") ),
    	@AttributeOverride(name="key", column = @Column(name="e_identifier_key") )
    })
	private EvaluationFormParticipationIdentifier identifier;
	@Column(name="e_anonymous", nullable=false, insertable=true, updatable=true)
	private boolean anonymous;
	@Enumerated(EnumType.STRING)
	@Column(name="e_status", nullable=false, insertable=true, updatable=true)
	private EvaluationFormParticipationStatus status;

	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_executor", nullable=true, insertable=true, updatable=false)
	private Identity executor;
	@ManyToOne(targetEntity=EvaluationFormSurveyImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_survey", nullable=false, insertable=true, updatable=false)
	private EvaluationFormSurvey survey;

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
	public EvaluationFormSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(EvaluationFormSurvey survey) {
		this.survey = survey;
	}

	@Override
	public EvaluationFormParticipationIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(EvaluationFormParticipationIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public boolean isAnonymous() {
		return anonymous;
	}

	@Override
	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	@Override
	public EvaluationFormParticipationStatus getStatus() {
		return status;
	}

	public void setStatus(EvaluationFormParticipationStatus status) {
		this.status = status;
	}

	@Override
	public Identity getExecutor() {
		return executor;
	}

	public void setExecutor(Identity executor) {
		this.executor = executor;
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
		EvaluationFormParticipationImpl other = (EvaluationFormParticipationImpl) obj;
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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EvaluationFormParticipationImpl [key=");
		builder.append(key);
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", anonymous=");
		builder.append(anonymous);
		builder.append(", executor=");
		builder.append(executor);
		builder.append(", survey=");
		builder.append(survey);
		builder.append("]");
		return builder.toString();
	}

}
