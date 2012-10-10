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
package org.olat.core.dispatcher.mapper.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PersistedMapper extends PersistentObject implements ModifiedInfo {

	private static final long serialVersionUID = 7297417374497607347L;
	
	private String mapperId;
	private String originalSessionId;
	private String xmlConfiguration;
	private Date lastModified;
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getMapperId() {
		return mapperId;
	}
	
	public void setMapperId(String mapperId) {
		this.mapperId = mapperId;
	}
	
	public String getOriginalSessionId() {
		return originalSessionId;
	}
	
	public void setOriginalSessionId(String originalSessionId) {
		this.originalSessionId = originalSessionId;
	}
	
	public String getXmlConfiguration() {
		return xmlConfiguration;
	}
	
	public void setXmlConfiguration(String xmlConfiguration) {
		this.xmlConfiguration = xmlConfiguration;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -7526 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PersistedMapper) {
			PersistedMapper m = (PersistedMapper)obj;
			return getKey() != null && getKey().equals(m.getKey());
		}
		return false;
	}
}