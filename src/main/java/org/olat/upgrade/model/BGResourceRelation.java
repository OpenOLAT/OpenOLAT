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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGResourceRelation extends PersistentObject {

	private static final long serialVersionUID = 2215547264646107606L;
	
	private BusinessGroup group;
	private OLATResourceImpl resource;
	
	private BGResourceRelation() {
		//
	}
	
	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public OLATResourceImpl getResource() {
		return resource;
	}
	
	public void setResource(OLATResourceImpl resource) {
		this.resource = resource;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("bgToResource[resource=")
			.append(resource == null ? "" : resource.getKey()).append(":")
			.append("group=").append(group == null ? "" : group.getKey())
			.append("]");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 29061 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGResourceRelation) {
			BGResourceRelation rel = (BGResourceRelation)obj;
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return false;
	}
}