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

import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Better caching, done in the first place for the list
 * of courses in BGMailHelper
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGRepositoryEntryShortImpl implements Persistable, RepositoryEntryShort {
	
	private static final long serialVersionUID = -1620571782903602033L;

	private Long key;
	private String displayname;
	
	public BGRepositoryEntryShortImpl() {
		//
	}
	
	public BGRepositoryEntryShortImpl(Long key, String displayname) {
		setKey(key);
		this.displayname = displayname;
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public String getDisplayname() {
		return displayname;
	}

	@Override
	public String getResourceType() {
		return null;
	}
	
	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return null;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2939985 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BGRepositoryEntryShortImpl re) {
			return getKey() != null && getKey().equals(re.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
