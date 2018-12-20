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
package org.olat.basesecurity.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 29 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityPropertiesRow extends UserPropertiesRow {
	
	private final Integer status;
	private final Date lastLogin;
	private final Date creationDate;
	
	public IdentityPropertiesRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		status = identity.getStatus();
		lastLogin = identity.getLastLogin();
		creationDate = identity.getCreationDate();
	}
	
	public IdentityPropertiesRow(Long identityKey, String identityName, Date creationDate, Date lastLogin, Integer status, String[] identityProps) {
		super(identityKey, identityName, identityProps);
		this.status = status;
		this.creationDate = creationDate;
		this.lastLogin = lastLogin;	
	}
	
	public Integer getStatus() {
		return status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastLogin() {
		return lastLogin;
	}
}
