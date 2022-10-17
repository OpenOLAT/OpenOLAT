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
package org.olat.modules.quality.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.olat.modules.forms.model.jpa.EvaluationFormSessionImpl;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityContextToCurriculum;
import org.olat.modules.quality.QualityContextToCurriculumElement;
import org.olat.modules.quality.QualityContextToOrganisation;
import org.olat.modules.quality.QualityContextToTaxonomyLevel;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qualitycontext")
@Table(name="o_qual_context")
public class QualityContextImpl implements QualityContext, Persistable {

	private static final long serialVersionUID = 6372826226016520068L;
	
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
	
	@Enumerated(EnumType.STRING)
	@Column(name="q_role", nullable=true, insertable=true, updatable=true)
	private QualityContextRole role;
	@Column(name="q_location", nullable=true, insertable=true, updatable=true)
	private String location;
	
	@ManyToOne(targetEntity=QualityDataCollectionImpl.class)
	@JoinColumn(name="fk_data_collection", nullable=false, insertable=true, updatable=false)
	private QualityDataCollection dataCollection;
	@ManyToOne(targetEntity=EvaluationFormParticipationImpl.class)
	@JoinColumn(name="fk_eva_participation", nullable=true, insertable=true, updatable=true)
	private EvaluationFormParticipation evaluationFormParticipation;
	@ManyToOne(targetEntity=EvaluationFormSessionImpl.class)
	@JoinColumn(name="fk_eva_session", nullable=true, insertable=true, updatable=true)
	private EvaluationFormSession evaluationFormSession;
	@ManyToOne(targetEntity=RepositoryEntry.class)
	@JoinColumn(name="fk_audience_repository", nullable=true, insertable=true, updatable=true)
	private RepositoryEntry audienceRepositoryEntry;
	@ManyToOne(targetEntity=CurriculumElementImpl.class)
	@JoinColumn(name="fk_audience_cur_element", nullable=true, insertable=true, updatable=true)
	private CurriculumElement audienceCurriculumElement;
	
	@OneToMany(targetEntity=QualityContextToCurriculumImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_context")
	private Set<QualityContextToCurriculum> contextToCurriculum;
	@OneToMany(targetEntity=QualityContextToCurriculumElementImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_context")
	private Set<QualityContextToCurriculumElement> contextToCurriculumElement;
	@OneToMany(targetEntity=QualityContextToOrganisationImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_context")
	private Set<QualityContextToOrganisation> contextToOrganisation;
	@OneToMany(targetEntity=QualityContextToTaxonomyLevelImpl.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_context")
	private Set<QualityContextToTaxonomyLevel> contextToTaxonomyLevel;
	

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
	public QualityContextRole getRole() {
		return role;
	}

	public void setRole(QualityContextRole role) {
		this.role = role;
	}

	@Override
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public QualityDataCollection getDataCollection() {
		return dataCollection;
	}

	public void setDataCollection(QualityDataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}

	@Override
	public EvaluationFormParticipation getEvaluationFormParticipation() {
		return evaluationFormParticipation;
	}

	public void setEvaluationFormParticipation(EvaluationFormParticipation evaluationFormParticipation) {
		this.evaluationFormParticipation = evaluationFormParticipation;
	}

	@Override
	public EvaluationFormSession getEvaluationFormSession() {
		return evaluationFormSession;
	}

	public void setEvaluationFormSession(EvaluationFormSession evaluationFormSession) {
		this.evaluationFormSession = evaluationFormSession;
	}

	@Override
	public RepositoryEntry getAudienceRepositoryEntry() {
		return audienceRepositoryEntry;
	}

	public void setAudienceRepositoryEntry(RepositoryEntry audienceRepositoryEntry) {
		this.audienceRepositoryEntry = audienceRepositoryEntry;
	}

	@Override
	public CurriculumElement getAudienceCurriculumElement() {
		return audienceCurriculumElement;
	}

	public void setAudienceCurriculumElement(CurriculumElement audienceCurriculumElement) {
		this.audienceCurriculumElement = audienceCurriculumElement;
	}

	@Override
	public Set<QualityContextToCurriculum> getContextToCurriculum() {
		if (contextToCurriculum == null) {
			contextToCurriculum = new HashSet<>();
		}
		return contextToCurriculum;
	}

	public void setContextToCurriculum(Set<QualityContextToCurriculum> contextToCurriculum) {
		this.contextToCurriculum = contextToCurriculum;
	}

	@Override
	public Set<QualityContextToCurriculumElement> getContextToCurriculumElement() {
		if (contextToCurriculumElement == null) {
			contextToCurriculumElement = new HashSet<>();
		}
		return contextToCurriculumElement;
	}

	public void setContextToCurriculumElement(Set<QualityContextToCurriculumElement> contextToCurriculumElement) {
		this.contextToCurriculumElement = contextToCurriculumElement;
	}

	@Override
	public Set<QualityContextToOrganisation> getContextToOrganisation() {
		if (contextToOrganisation == null) {
			contextToOrganisation = new HashSet<>();
		}
		return contextToOrganisation;
	}

	public void setContextToOrganisation(Set<QualityContextToOrganisation> contextToOrganisation) {
		this.contextToOrganisation = contextToOrganisation;
	}

	@Override
	public Set<QualityContextToTaxonomyLevel> getContextToTaxonomyLevel() {
		if (contextToTaxonomyLevel == null) {
			contextToTaxonomyLevel = new HashSet<>();
		}
		return contextToTaxonomyLevel;
	}
	
	public void setContextToTaxonomyLevel(Set<QualityContextToTaxonomyLevel> contextToTaxonomyLevel) {
		this.contextToTaxonomyLevel = contextToTaxonomyLevel;
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
		QualityContextImpl other = (QualityContextImpl) obj;
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
		builder.append("QualityContextImpl [key=");
		builder.append(key);
		builder.append(", dataCollectionKey=");
		builder.append(dataCollection != null? dataCollection.getKey(): "");
		builder.append(", evaluationFormParticipationKey=");
		builder.append(evaluationFormParticipation != null? evaluationFormParticipation.getKey(): "");
		builder.append(", repositoryEntryKey=");
		builder.append(audienceRepositoryEntry != null? audienceRepositoryEntry.getKey(): "");
		builder.append(", curriculumElementKey=");
		builder.append(audienceCurriculumElement != null? audienceCurriculumElement.getKey(): "");
		builder.append("]");
		return builder.toString();
	}

}
