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

import java.util.Date;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupMembershipInfos {
	
	private final Long identityKey;
	private final Long businessGroupKey;
	private final String businessGroupName;
	private final String role;
	private final Date creationDate;
	private final Date lastModified;
	
	public BusinessGroupMembershipInfos(Long identityKey, Long businessGroupKey, String businessGroupName,
			String role, Date creationDate, Date lastModified) {
		this.identityKey = identityKey;
		this.businessGroupKey = businessGroupKey;
		this.businessGroupName = businessGroupName;
		this.role = role;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public Long getBusinessGroupKey() {
		return businessGroupKey;
	}

	public String getBusinessGroupName() {
		return businessGroupName;
	}
	
	public String getRole() {
		return role;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}
}
