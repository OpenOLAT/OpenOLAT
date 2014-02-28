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

package org.olat.group.area;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> Initial Date: Aug 23, 2004
 * 
 * @author gnaegi
 */
public class BGAreaImpl extends PersistentObject implements BGArea {

	private static final long serialVersionUID = 4452153442327716546L;
	private String name;
	private String description;
	private OLATResource resource;

	/**
	 * Constructor used for Hibernate instanciation.
	 */
	protected BGAreaImpl() {
	// nothing to do
	}
	
	BGAreaImpl(String name, String description, OLATResource resource) {
		setName(name);
		setResource(resource);
		setDescription(description);
	}

	/**
	 * @see org.olat.group.area.BGArea#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see org.olat.group.area.BGArea#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	/**
	 * @see org.olat.group.area.BGArea#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.olat.group.area.BGArea#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "name=" + name + "::" + super.toString();
	}

	/**
	 * @see org.olat.core.gui.ShortName#getShortName()
	 */
	public String getShortName() {
		return getName();
	}
	
	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGArea) {
			BGArea area = (BGArea)obj;
			return getKey() != null && getKey().equals(area.getKey());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getKey() ==null ? 568301 : getKey().hashCode();
	}

}
