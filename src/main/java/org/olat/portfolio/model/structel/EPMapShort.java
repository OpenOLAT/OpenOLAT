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
package org.olat.portfolio.model.structel;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.resource.OLATResource;

/**
 * This is a help mapping to prevent loading too much from the database
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPMapShort extends PersistentObject {

	private static final long serialVersionUID = 3093838342982364478L;
	
	private String title;
	private Long sourceMapKey;
	private OLATResource olatResource;
	private SecurityGroup ownerGroup;
	
	public EPMapShort() {
		//
	}

	
	public Long getSourceMapKey() {
		return sourceMapKey;
	}
	
	public void setSourceMapKey(Long sourceMapKey) {
		this.sourceMapKey = sourceMapKey;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public OLATResource getOlatResource() {
		return olatResource;
	}
	
	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}
	
	@Override
	public String toString() {
		return "EPMapShort[" + super.toString() + "]";
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 98759 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EPMapShort) {
			EPMapShort map = (EPMapShort)obj;
			return getKey() != null && getKey().equals(map.getKey());
		}
		return false;
	}
}
