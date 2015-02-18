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
package org.olat.resource.references;

import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceInfos {
	
	private final boolean orphan;
	private final boolean owner;
	private final boolean managed;
	private final RepositoryEntry entry;
	
	public ReferenceInfos(RepositoryEntry entry, boolean orphan, boolean owner, boolean managed) {
		this.entry = entry;
		this.orphan = orphan;
		this.owner = owner;
		this.managed = managed;
	}
	
	public boolean isOrphan() {
		return orphan;
	}
	
	/**
	 * @return true if the user has the permission to delete this entry
	 */
	public boolean isOwner() {
		return owner;
	}
	
	public boolean isManaged() {
		return managed;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}
}
