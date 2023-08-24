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
package org.olat.modules.cemedia.model;

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

import org.olat.core.id.Persistable;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;

/**
 * 
 * Initial date: 31 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="mediatotaxonomylevel")
@Table(name="o_media_to_tax_level")
public class MediaToTaxonomyLevelImpl implements MediaToTaxonomyLevel, Persistable {
	
	private static final long serialVersionUID = -9080912363463284822L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=MediaImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_media", nullable=false, insertable=true, updatable=false)
	private Media media;

	@ManyToOne(targetEntity=TaxonomyLevelImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_taxonomy_level", nullable=false, insertable=true, updatable=false)
	private TaxonomyLevel taxonomyLevel;

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
	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 24347268 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MediaToTaxonomyLevelImpl rel) {
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
