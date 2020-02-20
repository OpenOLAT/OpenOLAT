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

package org.olat.repository;

import java.util.List;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

/**
 * Description: <br>
 * A CatalogEntry represents either a category or an alias to a linked
 * repository entry. The distinction is made by using the <code>TYPE_XXX</code>
 * attributes. <br>
 * Further the catalog entries together build up the catalog, which is a tree
 * structure. This tree structure is implemented by having each catalog entry
 * pointing to its parent.
 * 
 * @author Felix Jost
 */
public interface CatalogEntry extends CatalogEntryRef, CreateInfo, Persistable, OLATResourceable {
	/**
	 * define a catalog entry as a node <code>TYPE_NODE</code>
	 */
	public final static int TYPE_NODE = 0;
	/**
	 * define a catalog entry as a leaf <code>TYPE_LEAF</code>
	 */
	public final static int TYPE_LEAF = 1;

	/**
	 * getter
	 * 
	 * @return String
	 */
	public String getDescription();

	/**
	 * setter
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * getter
	 * 
	 * @return String
	 */
	public String getName();

	/**
	 * setter
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * getter
	 * 
	 * @return RepositoryEntry
	 */
	public RepositoryEntry getRepositoryEntry();

	/**
	 * setter
	 * 
	 * @param repositoryEntry
	 */
	public void setRepositoryEntry(RepositoryEntry repositoryEntry);

	/**
	 * getter
	 * 
	 * @return SecurityGroup
	 */
	public SecurityGroup getOwnerGroup();

	/**
	 * getter
	 * 
	 * @param ownerGroup
	 */
	public void setOwnerGroup(SecurityGroup ownerGroup);
	
	public Style getStyle();
	
	public void setStyle(Style style);

	/**
	 * getter
	 * 
	 * @see CatalogEntry#TYPE_LEAF
	 * @see CatalogEntry#TYPE_NODE
	 * @return int
	 */
	public int getType();

	/**
	 * setter
	 * 
	 * @see CatalogEntry#TYPE_LEAF
	 * @see CatalogEntry#TYPE_NODE
	 * @param type
	 */
	public void setType(int type);

	/**
	 * getter for an external URL pointer of ths catalogentry
	 * 
	 * @return String
	 */
	public String getExternalURL();

	/**
	 * setter for an external URL pointer of ths catalogentry
	 * 
	 * @param externalURL
	 */
	public void setExternalURL(String externalURL);

	/**
	 * parent node of this catalog entry
	 * 
	 * @param parent
	 */
	public void setParent(CatalogEntry parent);

	/**
	 * parent node of this catalog entry
	 * 
	 * @return CatalogEntry
	 */
	public CatalogEntry getParent();
	
	/**
	 * children of this catalog entry
	 *
	 * @return List<CatalogEntry>
	 */
	public List<CatalogEntry> getChildren();
	
	/**
	 * get position of entry
	 * 
	 * @return
	 */
	public Integer getPosition();
	
	
	public enum OrderBy {
		name,
	}
	
	public enum Style {
		tiles,
		list,
		compact,
		
	}
}