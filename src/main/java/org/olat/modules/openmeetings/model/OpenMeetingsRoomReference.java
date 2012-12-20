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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephae.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsRoomReference extends PersistentObject implements ModifiedInfo {

	private static final long serialVersionUID = -1556626893809537080L;

	private Date lastModified;
	
	private BusinessGroup group;
	private String resourceTypeName;
	private Long resourceTypeId;
	private String subIdentifier;
	
	private Long roomId;
	private String config;
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public String getResourceTypeName() {
		return resourceTypeName;
	}
	
	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}
	
	public Long getResourceTypeId() {
		return resourceTypeId;
	}
	
	public void setResourceTypeId(Long resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}
	
	public String getSubIdentifier() {
		return subIdentifier;
	}
	
	public void setSubIdentifier(String subIdentifier) {
		this.subIdentifier = subIdentifier;
	}
	
	public long getRoomId() {
		return roomId;
	}
	
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	
	public String getConfig() {
		return config;
	}
	
	public void setConfig(String config) {
		this.config = config;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3945 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OpenMeetingsRoomReference) {
			OpenMeetingsRoomReference ref = (OpenMeetingsRoomReference)obj;
			return getKey() != null && getKey().equals(ref.getKey());
		}
		return false;
	}	
}