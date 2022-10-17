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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.model.QualityGeneratorImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qualitydatacollection")
@Table(name="o_qual_data_collection")
public class QualityDataCollectionImpl implements QualityDataCollection, Persistable {
	
	private static final long serialVersionUID = 132872099156672583L;

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
	@Column(name="q_status", nullable=false, insertable=true, updatable=true)
	private QualityDataCollectionStatus status;
	@Column(name="q_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="q_start", nullable=true, insertable=true, updatable=true)
	private Date start;
	@Column(name="q_deadline", nullable=true, insertable=true, updatable=true)
	private Date deadline;
	
	@Column(name="q_topic_custom", nullable=true, insertable=true, updatable=true)
	private String topicCustom;
	@Enumerated(EnumType.STRING)
	@Column(name="q_topic_type", nullable=true, insertable=true, updatable=true)
	private QualityDataCollectionTopicType topicType;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="q_topic_fk_identity", nullable=true, insertable=true, updatable=true)
	private Identity topicIdentity;
	@ManyToOne(targetEntity=OrganisationImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="q_topic_fk_organisation", nullable=true, insertable=true, updatable=true)
	private Organisation topicOrganisation;
	@ManyToOne(targetEntity=CurriculumImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="q_topic_fk_curriculum", nullable=true, insertable=true, updatable=true)
	private Curriculum topicCurriculum;
	@ManyToOne(targetEntity=CurriculumElementImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="q_topic_fk_curriculum_element", nullable=true, insertable=true, updatable=true)
	private CurriculumElement topicCurriculumElement;
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="q_topic_fk_repository", nullable=true, insertable=true, updatable=true)
	private RepositoryEntry topicRepositoryEntry;
	
	@Column(name="q_generator_provider_key", nullable=true, insertable=true, updatable=true)
	private Long generatorProviderKey;
	
	@ManyToOne(targetEntity=QualityGeneratorImpl.class)
	@JoinColumn(name="fk_generator", nullable=true, insertable=true, updatable=false)
	private QualityGenerator generator;
	
	@Override
	public String getResourceableTypeName() {
		return QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME;
	}

	@Override
	public Long getResourceableId() {
		return key;
	}
	
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
	public QualityDataCollectionStatus getStatus() {
		return status;
	}

	public void setStatus(QualityDataCollectionStatus status) {
		this.status = status;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public void setStart(Date start) {
		this.start = start;
	}

	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	@Override
	public String getTopicCustom() {
		return topicCustom;
	}

	@Override
	public void setTopicCustom(String topicCustom) {
		this.topicCustom = topicCustom;
	}

	@Override
	public QualityDataCollectionTopicType getTopicType() {
		return topicType;
	}

	@Override
	public void setTopicType(QualityDataCollectionTopicType topicType) {
		this.topicType = topicType;
	}

	@Override
	public Identity getTopicIdentity() {
		return topicIdentity;
	}

	@Override
	public void setTopicIdentity(Identity topicIdentity) {
		this.topicIdentity = topicIdentity;
	}

	@Override
	public Organisation getTopicOrganisation() {
		return topicOrganisation;
	}

	@Override
	public void setTopicOrganisation(Organisation topicOrganisation) {
		this.topicOrganisation = topicOrganisation;
	}

	@Override
	public Curriculum getTopicCurriculum() {
		return topicCurriculum;
	}

	@Override
	public void setTopicCurriculum(Curriculum topicCurriculum) {
		this.topicCurriculum = topicCurriculum;
	}

	@Override
	public CurriculumElement getTopicCurriculumElement() {
		return topicCurriculumElement;
	}

	@Override
	public void setTopicCurriculumElement(CurriculumElement topicCurriculumElement) {
		this.topicCurriculumElement = topicCurriculumElement;
	}

	@Override
	public RepositoryEntry getTopicRepositoryEntry() {
		return topicRepositoryEntry;
	}

	@Override
	public void setTopicRepositoryEntry(RepositoryEntry topicRepositoryEntry) {
		this.topicRepositoryEntry = topicRepositoryEntry;
	}

	@Override
	public Long getGeneratorProviderKey() {
		return generatorProviderKey;
	}

	public void setGeneratorProviderKey(Long generatorProviderKey) {
		this.generatorProviderKey = generatorProviderKey;
	}

	@Override
	public QualityGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(QualityGenerator generator) {
		this.generator = generator;
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
		QualityDataCollectionImpl other = (QualityDataCollectionImpl) obj;
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
		builder.append("QualityDataCollectionImpl [key=");
		builder.append(key);
		builder.append(", title=");
		builder.append(title);
		builder.append("]");
		return builder.toString();
	}
}
