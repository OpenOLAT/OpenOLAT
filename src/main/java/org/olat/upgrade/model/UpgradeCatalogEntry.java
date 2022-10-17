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
package org.olat.upgrade.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;

/* 
 * Date: 13 Feb 2020<br>
 * @author Alexander Boeckle
 */

@Entity(name = "upgradecatalogentry")
@Table(name = "o_catentry")
public class UpgradeCatalogEntry {

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
	
	@Column(name = "name", unique = false, nullable = false, length = 100)
	private String name;
	
	@Column(name = "parent_id", nullable = true, insertable = false, updatable = false)
	private Long parent;
	
	@Column(name = "type", unique = false, nullable = false)
	private int type;
	
	@Column(name = "order_index")
	private Integer position;
	
	@ManyToOne(targetEntity = RepositoryEntry.class, optional = true)
	@JoinColumn(name = "fk_repoentry", nullable = true, insertable = true, updatable = true)
	private RepositoryEntry repositoryEntry;
	
	public UpgradeCatalogEntry() {
	// for hibernate
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns parent id
	 * @return
	 */
	public Long getParent() {
		return parent;
	}
	
	/**
	 * @see org.olat.repository.CatalogEntry#getPosition()
	 */
	public Integer getPosition() {
		return position;
	}
	
	/**
	 * Set the order index
	 * 
	 * @param position
	 */
	public void setPosition(Integer position) {
		this.position = position;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	@Override
	public String toString() {
		return "Typ: " + getType() + " Position: " + getPosition() + " Name: " + getName() + "\n";
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
		if(obj instanceof CatalogEntry) {
			CatalogEntry entry = (CatalogEntry)obj;
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}

	public Long getKey() {
		return this.key;
	}
}