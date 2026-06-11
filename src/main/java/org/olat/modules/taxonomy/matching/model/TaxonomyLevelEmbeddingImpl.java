/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.matching.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.model.TaxonomyImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;

/**
 * JPA entity for a stored embedding vector of a taxonomy level in one locale.
 * The actual vector column (t_vector / t_vector_json) is handled via native SQL
 * in {@link org.olat.modules.taxonomy.matching.manager.TaxonomyLevelEmbeddingDAO}.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Entity(name = "ctaxonomylevelembedding")
@Table(name = "o_tax_level_embedding")
public class TaxonomyLevelEmbeddingImpl implements Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Enumerated(EnumType.STRING)
	@Column(name = "t_text_variant", nullable = false, insertable = true, updatable = false)
	private TaxonomyEmbeddingTextVariant textVariant;

	@Column(name = "t_locale", nullable = false, insertable = true, updatable = false)
	private String locale;

	@Column(name = "t_embedding_text", nullable = false, insertable = true, updatable = true)
	private String embeddingText;

	@Column(name = "t_model_id", nullable = false, insertable = true, updatable = true)
	private String modelId;

	@Column(name = "t_model_version", nullable = true, insertable = true, updatable = true)
	private String modelVersion;

	@ManyToOne(targetEntity = TaxonomyLevelImpl.class)
	@JoinColumn(name = "fk_level", nullable = false, insertable = true, updatable = false)
	private TaxonomyLevel level;

	@ManyToOne(targetEntity = TaxonomyImpl.class)
	@JoinColumn(name = "fk_taxonomy", nullable = false, insertable = true, updatable = false)
	private Taxonomy taxonomy;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public TaxonomyEmbeddingTextVariant getTextVariant() {
		return textVariant;
	}

	public void setTextVariant(TaxonomyEmbeddingTextVariant textVariant) {
		this.textVariant = textVariant;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getEmbeddingText() {
		return embeddingText;
	}

	public void setEmbeddingText(String embeddingText) {
		this.embeddingText = embeddingText;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public TaxonomyLevel getLevel() {
		return level;
	}

	public void setLevel(TaxonomyLevel level) {
		this.level = level;
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof TaxonomyLevelEmbeddingImpl other) {
			return key != null && key.equals(other.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return key == null ? super.hashCode() : key.hashCode();
	}
}
