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
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ctaxonomyleveltype")
@Table(name="o_tax_taxonomy_level_type")
@NamedQuery(name="loadTaxonomyLevelTypeByKey", query="select type from ctaxonomyleveltype type inner join fetch type.taxonomy taxonomy where type.key=:typeKey")
public class TaxonomyLevelTypeImpl implements Persistable, ModifiedInfo, TaxonomyLevelType {

	private static final long serialVersionUID = -4154176752303740382L;

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
	@Column(name="t_displayname", nullable=true, insertable=true, updatable=true)
	private String displayName;
	@Column(name="t_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="t_external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="t_managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="t_css_class", nullable=true, insertable=true, updatable=true)
	private String cssClass;
	@Column(name="t_visible", nullable=false, insertable=true, updatable=true)
	private boolean visible;

	@Column(name="t_library_docs", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryEnabled;
	@Column(name="t_library_manage", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryManagerCompetenceEnabled;
	@Column(name="t_library_teach_read", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryTeachCompetenceReadEnabled;
	@Column(name="t_library_teach_readlevels", nullable=false, insertable=true, updatable=true)
	private int documentsLibraryTeachCompetenceReadParentLevels = 0;
	@Column(name="t_library_teach_write", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryTeachCompetenceWriteEnabled;
	@Column(name="t_library_have_read", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryHaveCompetenceReadEnabled;
	@Column(name="t_library_target_read", nullable=false, insertable=true, updatable=true)
	private boolean documentsLibraryTargetCompetenceReadEnabled;
	@Column(name="t_allow_as_competence", nullable=false, insertable=true, updatable=true)
	private boolean allowedAsCompetence;
	@Column(name="t_allow_as_subject", nullable=false, insertable=true, updatable=true)
	private boolean allowedAsSubject;
	
	@ManyToOne(targetEntity=TaxonomyImpl.class)
	@JoinColumn(name="fk_taxonomy", nullable=true, insertable=true, updatable=true)
	private Taxonomy taxonomy;
	
	@OneToMany(targetEntity=TaxonomyLevelTypeToTypeImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_type")
	public Set<TaxonomyLevelTypeToType> allowedSubTypes;

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
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
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
	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public TaxonomyLevelTypeManagedFlag[] getManagedFlags() {
		return TaxonomyLevelTypeManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(TaxonomyLevelTypeManagedFlag[] flags) {
		managedFlagsString = TaxonomyLevelTypeManagedFlag.toString(flags);
	}

	@Override
	public String getCssClass() {
		return cssClass;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isDocumentsLibraryEnabled() {
		return documentsLibraryEnabled;
	}

	@Override
	public void setDocumentsLibraryEnabled(boolean documentsLibraryEnabled) {
		this.documentsLibraryEnabled = documentsLibraryEnabled;
	}

	@Override
	public boolean isDocumentsLibraryManageCompetenceEnabled() {
		return documentsLibraryManagerCompetenceEnabled;
	}

	@Override
	public void setDocumentsLibraryManageCompetenceEnabled(boolean enabled) {
		documentsLibraryManagerCompetenceEnabled = enabled;
	}

	@Override
	public boolean isDocumentsLibraryTeachCompetenceReadEnabled() {
		return documentsLibraryTeachCompetenceReadEnabled;
	}

	@Override
	public void setDocumentsLibraryTeachCompetenceReadEnabled(boolean documentsLibraryTeachCompetenceReadEnabled) {
		this.documentsLibraryTeachCompetenceReadEnabled = documentsLibraryTeachCompetenceReadEnabled;
	}

	@Override
	public int getDocumentsLibraryTeachCompetenceReadParentLevels() {
		return documentsLibraryTeachCompetenceReadParentLevels;
	}

	@Override
	public void setDocumentsLibraryTeachCompetenceReadParentLevels(int documentsLibraryTeachCompetenceReadParentLevels) {
		this.documentsLibraryTeachCompetenceReadParentLevels = documentsLibraryTeachCompetenceReadParentLevels;
	}

	@Override
	public boolean isDocumentsLibraryTeachCompetenceWriteEnabled() {
		return documentsLibraryTeachCompetenceWriteEnabled;
	}

	@Override
	public void setDocumentsLibraryTeachCompetenceWriteEnabled(boolean documentsLibraryTeachCompetenceWriteEnabled) {
		this.documentsLibraryTeachCompetenceWriteEnabled = documentsLibraryTeachCompetenceWriteEnabled;
	}

	@Override
	public boolean isDocumentsLibraryHaveCompetenceReadEnabled() {
		return documentsLibraryHaveCompetenceReadEnabled;
	}

	@Override
	public void setDocumentsLibraryHaveCompetenceReadEnabled(boolean documentsLibraryHaveCompetenceReadEnabled) {
		this.documentsLibraryHaveCompetenceReadEnabled = documentsLibraryHaveCompetenceReadEnabled;
	}

	@Override
	public boolean isDocumentsLibraryTargetCompetenceReadEnabled() {
		return documentsLibraryTargetCompetenceReadEnabled;
	}

	@Override
	public void setDocumentsLibraryTargetCompetenceReadEnabled(boolean documentsLibraryTargetCompetenceReadEnabled) {
		this.documentsLibraryTargetCompetenceReadEnabled = documentsLibraryTargetCompetenceReadEnabled;
	}

	@Override
	public Set<TaxonomyLevelTypeToType> getAllowedTaxonomyLevelSubTypes() {
		if(allowedSubTypes == null) {
			allowedSubTypes = new HashSet<>();
		}
		return allowedSubTypes;
	}

	public void setAllowedTaxonomyLevelSubTypes(Set<TaxonomyLevelTypeToType> allowedSubTypes) {
		this.allowedSubTypes = allowedSubTypes;
	}

	@Override
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}
	
	@Override
	public boolean isAllowedAsCompetence() {
		return allowedAsCompetence;
	}
	
	@Override
	public void setAllowedAsCompetence(boolean allowedAsCompetence) {
		this.allowedAsCompetence = allowedAsCompetence;
	}
	
	@Override
	public boolean isAllowedAsSubject() {
		return allowedAsSubject;
	}
	
	@Override
	public void setAllowedAsSubject(boolean allowedAsSubject) {
		this.allowedAsSubject = allowedAsSubject;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 234379 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyLevelTypeImpl) {
			TaxonomyLevelTypeImpl type = (TaxonomyLevelTypeImpl)obj;
			return getKey() != null && getKey().equals(type.getKey());
		}
		return false	;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
