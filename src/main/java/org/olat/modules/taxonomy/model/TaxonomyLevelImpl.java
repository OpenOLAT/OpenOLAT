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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * 
 * Initial date: 19 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ctaxonomylevel")
@Table(name="o_tax_taxonomy_level")
@NamedQueries({
	@NamedQuery(name="loadTaxonomyLevelsByKey", query="select level from ctaxonomylevel as level left join fetch level.parent parent left join fetch level.type type inner join fetch level.taxonomy taxonomy where level.key=:levelKey")
	
})
public class TaxonomyLevelImpl implements Persistable, ModifiedInfo, TaxonomyLevel {

	private static final long serialVersionUID = 7873564059919461651L;

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
	
	@Column(name="t_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="t_i18n_suffix", nullable=true, insertable=true, updatable=true)
	private String i18nSuffix;
	@Column(name="t_displayname", nullable=true, insertable=true, updatable=true)
	private String displayName;
	@Column(name="t_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="t_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="t_sort_order", nullable=true, insertable=true, updatable=true)
	private Integer sortOrder;
	
	@Column(name="t_directory_path", nullable=true, insertable=true, updatable=true)
	private String directoryPath;
	@Column(name="t_media_path", nullable=true, insertable=true, updatable=true)
	private String mediaPath;

	@Column(name="t_m_path_keys", nullable=true, insertable=true, updatable=true)
	private String materializedPathKeys;
	@Column(name="t_m_path_identifiers", nullable=true, insertable=true, updatable=true)
	private String materializedPathIdentifiers;

	@Column(name="t_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Column(name="t_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@ManyToOne(targetEntity=TaxonomyImpl.class)
	@JoinColumn(name="fk_taxonomy", nullable=true, insertable=true, updatable=true)
	private Taxonomy taxonomy;
	
	@ManyToOne(targetEntity=TaxonomyLevelImpl.class)
	@JoinColumn(name="fk_parent", nullable=true, insertable=true, updatable=true)
	private TaxonomyLevel parent;
	
	@ManyToOne(targetEntity=TaxonomyLevelTypeImpl.class)
	@JoinColumn(name="fk_type", nullable=true, insertable=true, updatable=true)
	private TaxonomyLevelType type;
	
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
	public String getResourceableTypeName() {
		return "TaxonomyLevel";
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getI18nSuffix() {
		return i18nSuffix;
	}

	public void setI18nSuffix(String i18nSuffix) {
		this.i18nSuffix = i18nSuffix;
	}

	@Deprecated
	public String getDisplayName() {
		return displayName;
	}

	@Deprecated
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Deprecated
	public String getDescription() {
		return description;
	}

	@Deprecated
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public Integer getSortOrder() {
		return sortOrder;
	}

	@Override
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

	@Override
	public String getMaterializedPathKeys() {
		return materializedPathKeys;
	}

	public void setMaterializedPathKeys(String materializedPathKeys) {
		this.materializedPathKeys = materializedPathKeys;
	}

	@Override
	public String getMaterializedPathIdentifiers() {
		return materializedPathIdentifiers;
	}

	public void setMaterializedPathIdentifiers(String materializedPathIdentifiers) {
		this.materializedPathIdentifiers = materializedPathIdentifiers;
	}
	
	@Override
	public String getMaterializedPathIdentifiersWithoutSlash() {
		String keysWithoutSlash = materializedPathIdentifiers;
		
		if (materializedPathIdentifiers.endsWith("/") && materializedPathIdentifiers.length() > 1) {
			keysWithoutSlash = keysWithoutSlash.substring(0, keysWithoutSlash.length() - 1);
		}
		
		if (materializedPathIdentifiers.startsWith("/") && materializedPathIdentifiers.length() > 1) {
			keysWithoutSlash = keysWithoutSlash.substring(1, keysWithoutSlash.length());
		}
		
		keysWithoutSlash = keysWithoutSlash.replace("/", " / ");
		
		return keysWithoutSlash;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public TaxonomyLevelManagedFlag[] getManagedFlags() {
		return TaxonomyLevelManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(TaxonomyLevelManagedFlag[] flags) {
		managedFlagsString = TaxonomyLevelManagedFlag.toString(flags);
	}

	@Override
	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}

	@Override
	public TaxonomyLevel getParent() {
		return parent;
	}

	public void setParent(TaxonomyLevel parent) {
		this.parent = parent;
	}

	@Override
	public TaxonomyLevelType getType() {
		return type;
	}

	@Override
	public void setType(TaxonomyLevelType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return key == null ? 1961331 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyLevelImpl) {
			TaxonomyLevelImpl level = (TaxonomyLevelImpl)obj;
			return getKey() != null && getKey().equals(level.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
