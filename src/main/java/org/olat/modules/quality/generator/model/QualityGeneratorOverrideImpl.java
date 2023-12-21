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
package org.olat.modules.quality.generator.model;

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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.model.QualityDataCollectionImpl;

/**
 * 
 * Initial date: 06.12.2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qualitygeneratoroverride")
@Table(name="o_qual_generator_override")
public class QualityGeneratorOverrideImpl implements QualityGeneratorOverride, Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = 250256899051580600L;
	
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
	
	@Column(name="q_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="q_start", nullable=true, insertable=true, updatable=true)
	private Date start;
	
	@Column(name="q_generator_provider_key", nullable=true, insertable=true, updatable=true)
	private Long generatorProviderKey;
	
	@ManyToOne(targetEntity=QualityGeneratorImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_generator", nullable=false, insertable=true, updatable=false)
	private QualityGenerator generator;
	@OneToOne(targetEntity=QualityDataCollectionImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_data_collection", nullable=true, insertable=true, updatable=true)
	private QualityDataCollection dataCollection;


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
	public Date getStart() {
		return start;
	}

	@Override
	public void setStart(Date start) {
		this.start = start;
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
	public QualityDataCollection getDataCollection() {
		return dataCollection;
	}

	@Override
	public void setDataCollection(QualityDataCollection dataCollection) {
		this.dataCollection = dataCollection;
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
		QualityGeneratorOverrideImpl other = (QualityGeneratorOverrideImpl) obj;
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
