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

import jakarta.persistence.Transient;

import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAccessUpgrade {

	public static final int DELETED = 0;
	public static final int ACC_OWNERS = 1; // limit access to owners
	public static final int ACC_OWNERS_AUTHORS = 2; // limit access to owners and authors
	public static final int ACC_USERS = 3; // limit access to owners, authors and users
	public static final int ACC_USERS_GUESTS = 4; // no limits

	private Long key;
	
	private int access;
	private boolean canCopy;
	private boolean canReference;
	private boolean canLaunch;
	private boolean canDownload;
	private boolean membersOnly;
	private int statusCode;

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public boolean getCanCopy() {
		return canCopy;
	}

	public void setCanCopy(boolean b) {
		canCopy = b;
	}

	public boolean getCanReference() {
		return canReference;
	}
	
	public void setCanReference(boolean b) {
		canReference = b;
	}

	public boolean getCanDownload() {
		return canDownload;
	}
	
	public void setCanDownload(boolean b) {
		canDownload = b;
	}

	public boolean getCanLaunch() {
		return canLaunch;
	}

	public void setCanLaunch(boolean b) {
		canLaunch = b;
	}
	
	public int getAccess() {
		return access;
	}
	
	public void setAccess(int i) {
		access = i;
	}

	public boolean isMembersOnly() {
		return membersOnly;
	}

	public void setMembersOnly(boolean membersOnly) {
		this.membersOnly = membersOnly;
	}
	
	@Transient
	public RepositoryEntryAccessUpgradeStatus getStatus() {
		return new RepositoryEntryAccessUpgradeStatus(getStatusCode());
	}

	@Override
	public int hashCode() {
		return key == null ? 293485 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntry) {
			RepositoryEntryAccessUpgrade re = (RepositoryEntryAccessUpgrade)obj;
			return key != null && key.equals(re.key);
		}
		return false;
	}
}
