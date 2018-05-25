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
package org.olat.repository.model;

import java.util.Date;

/**
 * 
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembershipInfos {
	
	private final Long identityKey;
	private final Long repositoryEntryKey;
	private final String displayName;
	private final String role;
	private final Date creationDate;
	private final Date initialLaunch;
	private final Date recentLaunch;
	private final Long visit;
	
	public MembershipInfos(Long identityKey, Long repositoryEntryKey, String displayName,
			String role, Date creationDate, Date initialLaunch, Date recentLaunch, Long visit) {
		this.identityKey = identityKey;
		this.repositoryEntryKey = repositoryEntryKey;
		this.displayName = displayName;
		this.role = role;
		this.creationDate = creationDate;
		this.initialLaunch = initialLaunch;
		this.recentLaunch = recentLaunch;
		this.visit = visit;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getRole() {
		return role;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public Date getInitialLaunch() {
		return initialLaunch;
	}

	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public Long getVisit() {
		return visit;
	}
}
