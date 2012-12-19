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
	private String type;
	private Long key;
	
	public Resourceable() {
		//
	}
	
	public Resourceable(String type, Long key) {
		this.type = type;
		this.key = key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return type;
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	@Override
	public int hashCode() {
		return (key == null ? 2938 : key.hashCode()) + (type == null ? 76678 : type.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OLATResourceable) {
			OLATResourceable ores = (OLATResourceable)obj;
			return type != null && type.equals(ores.getResourceableTypeName())
					&& key != null && key.equals(ores.getResourceableId());
		}
		return false;
	}

	@Override
	public String toString() {
		return "resourceable[type=" + type + ":id=" + key + "]" + super.toString();
	}

	@Override
	protected Resourceable clone() {
		return new Resourceable(type, key);
	}
}
