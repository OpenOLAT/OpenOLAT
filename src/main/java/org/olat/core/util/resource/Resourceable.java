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
package org.olat.core.util.resource;

import java.io.Serializable;

import org.olat.core.id.OLATResourceable;

import javax.annotation.Nullable;

/**
 * An implementation of the OLATresourceable which is serializable
 * 
 * 
 * Initial date: 19.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Resourceable implements OLATResourceable, Serializable, Cloneable {

	private static final long serialVersionUID = 4493480617698403988L;
	private String resourceableTypeName;
	private Long resourceableId;
	
	public Resourceable() {
		//
	}
	
	public Resourceable(String type, @Nullable Long key) {
		this.resourceableTypeName = type;
		this.resourceableId = key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return resourceableTypeName;
	}

	public void setResourceableTypeName(String resourceableTypeName) {
		this.resourceableTypeName = resourceableTypeName;
	}

	@Override
	public Long getResourceableId() {
		return resourceableId;
	}
	
	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}

	@Override
	public int hashCode() {
		return (resourceableId == null ? 2938 : resourceableId.hashCode()) + (resourceableTypeName == null ? 76678 : resourceableTypeName.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OLATResourceable) {
			OLATResourceable ores = (OLATResourceable)obj;
			return resourceableTypeName != null && resourceableTypeName.equals(ores.getResourceableTypeName())
					&& resourceableId != null && resourceableId.equals(ores.getResourceableId());
		}
		return false;
	}

	@Override
	public String toString() {
		return "resourceable[type=" + resourceableTypeName + ":id=" + resourceableId + "]" + super.toString();
	}

	@Override
	protected Resourceable clone() {
		return new Resourceable(resourceableTypeName, resourceableId);
	}
}
