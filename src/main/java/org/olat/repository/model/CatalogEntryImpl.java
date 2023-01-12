/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;

/**
 * Description: <br>
 * Implementation of CatalogEntry
 * 
 * @see org.olat.repository.CatalogEntry
 * @author Felix Jost
 */
@Entity(name="catalogentry")
@Table(name="o_catentry")
public class CatalogEntryImpl implements CatalogEntry {

	private static final long serialVersionUID = 2834235462805397562L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name = "name", unique = false, nullable = false)
	private String name;
	
	@Column(name = "short_title", unique = false, nullable = true)
	private String shortTitle;
	
	@Column(name = "style", unique = false, nullable = true)
	private String styleString;
	
	@Column(name = "description", unique = false, nullable = true)
	private String description;
	
	@Column(name = "externalurl", unique = false, nullable = true)
	private String externalURL;
	
	@ManyToOne(targetEntity = RepositoryEntry.class, optional = true)
	@JoinColumn(name = "fk_repoentry", nullable = true, insertable = true, updatable = true)
	private RepositoryEntry repositoryEntry;
	
	@ManyToOne(targetEntity = CatalogEntryImpl.class, optional = true)
	@JoinColumn(name = "parent_id", nullable = true, insertable = true, updatable = true)
	private CatalogEntry parent;
	
	@OneToMany(targetEntity = CatalogEntryImpl.class, mappedBy = "parent", fetch = FetchType.LAZY)
	@OrderColumn(name = "order_index")
	private List<CatalogEntry> children;

	@ManyToOne(targetEntity = SecurityGroupImpl.class, optional = true)
	@JoinColumn(name = "fk_ownergroup", nullable = true, insertable = true, updatable = true)
	private SecurityGroup ownerGroup;
	
	@Column(name = "type", unique = false, nullable = false)
	private int type;
	
	@GeneratedValue
	@Column(name = "order_index", updatable = false, insertable = false)
	private Integer position;

	@Column(name = "add_entry_position", unique = false, nullable = true)
	private Integer addEntryPosition;

	@Column(name = "add_category_position", unique = false, nullable = true)
	private Integer addCategoryPosition;

	
	public CatalogEntryImpl() {
	// for hibernate
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
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		if (name.length() > 100)
			throw new AssertException("CatalogEntry: Name is limited to 100 characters.");
		this.name = name;
	}
	
	@Override 
	public String getShortTitle() {
		return shortTitle;
	}
	
	@Override 
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public Integer getEntryAddPosition() {
		return addEntryPosition;
	}

	@Override
	public void setEntryAddPosition(Integer addEntryPosition) {
		this.addEntryPosition = addEntryPosition;
	}

	@Override
	public Integer getCategoryAddPosition() {
		return addCategoryPosition;
	}

	@Override
	public void setCategoryAddPosition(Integer addCategoryPosition) {
		this.addCategoryPosition = addCategoryPosition;
	}

	public String getStyleString() {
		return styleString;
	}

	public void setStyleString(String styleString) {
		this.styleString = styleString;
	}
	
	@Override
	public Style getStyle() {
		return StringHelper.containsNonWhitespace(styleString) ? Style.valueOf(styleString) : Style.tiles;
	}

	@Override
	public void setStyle(Style style) {
		if(style == null) {
			styleString = null;
		} else {
			styleString = style.name();
		}
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	@Override
	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String getExternalURL() {
		return externalURL;
	}

	@Override
	public void setExternalURL(String externalURL) {
		this.externalURL = externalURL;
	}

	@Override
	public CatalogEntry getParent() {
		return parent;
	}

	@Override
	public void setParent(CatalogEntry parent) {
		this.parent = parent;
	}

	@Override
	public List<CatalogEntry> getChildren() {
		if(children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "cat:" + getName() + "=" + super.toString();
	}

	@Override
	public String getResourceableTypeName() {
		return this.getClass().getName();
	}

	@Override
	public Long getResourceableId() {
		Long k = getKey();
		if (k == null) throw new AssertException("no key yet!");
		return k;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -7759 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CatalogEntry entry) {
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return this.equals(persistable);
	}
}