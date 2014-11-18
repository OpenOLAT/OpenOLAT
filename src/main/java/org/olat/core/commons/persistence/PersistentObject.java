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
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * Needs the primary key called 'key' in hibernate and 'creationDate' 
 * 
 * @author Andreas Ch. Kapp
 *
 */
@SuppressWarnings("unused")
public abstract class PersistentObject implements CreateInfo, Persistable {

	private Long key = null;
	private int version = 0;
	protected Date creationDate;
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}
	
	/**
	 * For Hibernate only
	 * @param version
	 */
	private void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return Long
	 */
	public Long getKey() {
		return key;
	}

	/**
	 * for hibernate only
	 * @param key
	 */
	protected void setKey(Long key) {
		this.key = key;
	}
	
	
	/**
	 * @see org.olat.core.commons.persistence.Persistable#equalsByPersistableKey(org.olat.core.commons.persistence.Persistable)
	 */
	public boolean equalsByPersistableKey(Persistable persistable) {
		if (this.getKey().compareTo(persistable.getKey()) == 0)
			return true;
		else
			return false;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "key:"+key+"="+super.toString();
	}

}
