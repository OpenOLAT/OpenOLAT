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
package org.olat.upgrade.model;

import java.util.Set;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.resource.OLATResource;

/**
 * Needed to upgrade the maps
 * 
 * Initial date: 28.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPMapUpgrade extends PersistentObject {

	private static final long serialVersionUID = 9041327840189041360L;

	private SecurityGroup ownerGroup;
	private OLATResource olatResource;
	private Set<EPMapUpgradeToGroupRelation> groups;
	 
	public OLATResource getOlatResource() {
		return olatResource;
	}

	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	public Set<EPMapUpgradeToGroupRelation> getGroups() {
		return groups;
	}

	public void setGroups(Set<EPMapUpgradeToGroupRelation> groups) {
		this.groups = groups;
	}

	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -9544 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof EPMapUpgrade) {
			EPMapUpgrade map = (EPMapUpgrade)obj;
			return getKey() != null && getKey().equals(map.getKey());
		}
		return false;
	}
}
