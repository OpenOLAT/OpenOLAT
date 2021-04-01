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
package org.olat.modules.taxonomy.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ctaxonomycompetence")
@Table(name="o_tax_taxonomy_competence")
public class TaxonomyCompetenceImpl implements Persistable, ModifiedInfo, TaxonomyCompetence {

	private static final long serialVersionUID = -1382950544549489153L;

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
	
	@Column(name="t_type", nullable=false, insertable=true, updatable=false)
	private String type;

	@Column(name="t_achievement", nullable=true, insertable=true, updatable=true)
	private BigDecimal achievement;
	@Column(name="t_reliability", nullable=true, insertable=true, updatable=true)
	private BigDecimal reliability;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_expiration_date", nullable=true, insertable=true, updatable=true)
	private Date expiration;
	@Column(name="t_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="t_source_text", nullable=true, insertable=true, updatable=true)
	private String sourceText;
	@Column(name="t_source_url", nullable=true, insertable=true, updatable=true)
	private String sourceUrl;
	@Enumerated(EnumType.STRING)
	@Column(name="t_link_location", nullable=false, insertable=true, updatable=false)
	private TaxonomyCompetenceLinkLocations linkLocation;
	
	@ManyToOne(targetEntity=TaxonomyLevelImpl.class)
	@JoinColumn(name="fk_level", nullable=false, insertable=true, updatable=false)
	private TaxonomyLevel taxonomyLevel;
	
	@ManyToOne(targetEntity=IdentityImpl.class)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	

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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public TaxonomyCompetenceTypes getCompetenceType() {
		if(StringHelper.containsNonWhitespace(type)) {
			return TaxonomyCompetenceTypes.valueOf(type);
		}
		return null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public BigDecimal getAchievement() {
		return achievement;
	}

	public void setAchievement(BigDecimal achievement) {
		this.achievement = achievement;
	}

	@Override
	public BigDecimal getReliability() {
		return reliability;
	}

	public void setReliability(BigDecimal reliability) {
		this.reliability = reliability;
	}

	@Override
	public Date getExpiration() {
		return expiration;
	}

	@Override
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getSourceText() {
		return sourceText;
	}

	public void setSourceText(String sourceText) {
		this.sourceText = sourceText;
	}

	@Override
	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public TaxonomyCompetenceLinkLocations getLinkLocation() {
		return linkLocation;
	}
	
	public void setLinkLocation(TaxonomyCompetenceLinkLocations linkLocation) {
		this.linkLocation = linkLocation;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 62350616 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyCompetenceImpl) {
			TaxonomyCompetenceImpl competence = (TaxonomyCompetenceImpl)obj;
			return getKey() != null && getKey().equals(competence.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
