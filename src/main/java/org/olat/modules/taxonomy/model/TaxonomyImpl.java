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
import jakarta.persistence.FetchType;
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

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyManagedFlag;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ctaxonomy")
@Table(name="o_tax_taxonomy")
@NamedQueries({
		@NamedQuery(name="loadTaxonomyByKey", query="select taxonomy from ctaxonomy taxonomy inner join fetch taxonomy.group as bGroup where taxonomy.key=:taxonomyKey"),
		@NamedQuery(name="loadAllTaxonomy", query="select taxonomy from ctaxonomy taxonomy inner join fetch taxonomy.group as bGroup")
})
public class TaxonomyImpl implements Persistable, Taxonomy {

	private static final long serialVersionUID = -8728887923293798226L;

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

	@Column(name="t_directory_path", nullable=true, insertable=true, updatable=true)
	private String directoryPath;
	@Column(name="t_directory_lost_found_path", nullable=true, insertable=true, updatable=true)
	private String directoryLostFoundPath;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group", nullable=false, insertable=true, updatable=false)
	private Group group;

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
		return "Taxonomy";
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
	public TaxonomyManagedFlag[] getManagedFlags() {
		return TaxonomyManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public void setManagedFlags(TaxonomyManagedFlag[] flags) {
		managedFlagsString = TaxonomyManagedFlag.toString(flags);
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public String getDirectoryLostFoundPath() {
		return directoryLostFoundPath;
	}

	public void setDirectoryLostFoundPath(String directoryLostFoundPath) {
		this.directoryLostFoundPath = directoryLostFoundPath;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		return key == null ? 816587 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TaxonomyImpl) {
			TaxonomyImpl taxonomy = (TaxonomyImpl)obj;
			return getKey() != null && getKey().equals(taxonomy.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
