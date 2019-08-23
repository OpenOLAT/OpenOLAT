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

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
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
public class CatalogEntryImpl extends PersistentObject implements CatalogEntry {

	private static final long serialVersionUID = 2834235462805397562L;
	private String name;
	private String styleString;
	private String description;
	private String externalURL;
	private RepositoryEntry repositoryEntry;
	private CatalogEntry parent;

	private SecurityGroup ownerGroup;
	private int type;

	public CatalogEntryImpl() {
	// for hibernate
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setName(java.lang.String)
	 */
	public void setName(String name) {
		if (name.length() > 255)
			throw new AssertException("CatalogEntry: Name is limited to 255 characters.");
		this.name = name;
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

	/**
	 * @see org.olat.repository.CatalogEntry#getRepositoryEntry()
	 */
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setRepositoryEntry(org.olat.repository.RepositoryEntry)
	 */
	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getOwnerGroup()
	 */
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setOwnerGroup(org.olat.basesecurity.SecurityGroup)
	 */
	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setType(int)
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getExternalURL()
	 */
	public String getExternalURL() {
		return externalURL;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setExternalURL(java.lang.String)
	 */
	public void setExternalURL(String externalURL) {
		this.externalURL = externalURL;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#getParent()
	 */
	public CatalogEntry getParent() {
		return parent;
	}

	/**
	 * @see org.olat.repository.CatalogEntry#setParent(org.olat.repository.CatalogEntry)
	 */
	public void setParent(CatalogEntry parent) {
		this.parent = parent;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	public String toString() {
		return "cat:" + getName() + "=" + super.toString();
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return this.getClass().getName();
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		Long key = getKey();
		if (key == null) throw new AssertException("no key yet!");
		return key;
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
}