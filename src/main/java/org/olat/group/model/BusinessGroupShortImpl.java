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
package org.olat.group.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;

/**
 * This a short summary of the business group without any
 * relation.<br>
 * !!!This class is IMMUTABLE
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupShortImpl extends PersistentObject implements BusinessGroupShort {

	private static final long serialVersionUID = -5404538852842562897L;
	
	private String name;
	private String managedFlagsString;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return BusinessGroupManagedFlag.toEnum(managedFlagsString);
	}

	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof BusinessGroupShortImpl) {
			BusinessGroupShortImpl bg = (BusinessGroupShortImpl)obj;
			return getKey() != null && getKey().equals(bg.getKey());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2901 : getKey().hashCode();
	}
}
