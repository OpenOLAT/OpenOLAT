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
import org.olat.core.id.IdentityLifecycle;
import org.olat.core.id.Organisation;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 29 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityPropertiesRow extends UserPropertiesRow implements IdentityLifecycle {
	
	private final Integer status;
	private final Date lastLogin;
	private final Date creationDate;
	private final Date inactivationDate;
	private final Date reactivationDate;
	private final Date expirationDate;
	private final Date deletionEmailDate;
	
	private List<Organisation> organisations;

	public IdentityPropertiesRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		status = identity.getStatus();
		lastLogin = identity.getLastLogin();
		creationDate = identity.getCreationDate();
		inactivationDate = identity.getInactivationDate();
		reactivationDate = identity.getReactivationDate();
		expirationDate = identity.getExpirationDate();
		deletionEmailDate = identity.getDeletionEmailDate();
	}
	
	public IdentityPropertiesRow(Long identityKey, Date creationDate, Date lastLogin, Integer status,
			Date inactivationDate, Date reactivationDate, Date expirationDate, Date deletionEmailDate,
			List<UserPropertyHandler> userPropertyHandlers, String[] identityProps, Locale locale) {
		super(identityKey, userPropertyHandlers, identityProps, locale);
		this.status = status;
		this.creationDate = creationDate;
		this.lastLogin = lastLogin;	
		this.inactivationDate = inactivationDate;
		this.reactivationDate = reactivationDate;
		this.expirationDate = expirationDate;
		this.deletionEmailDate = deletionEmailDate;
	}
	
	@Override
	public Integer getStatus() {
		return status;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getLastLogin() {
		return lastLogin;
	}

	@Override
	public Date getInactivationDate() {
		return inactivationDate;
	}

	@Override
	public Date getReactivationDate() {
		return reactivationDate;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	@Override
	public Date getDeletionEmailDate() {
		return deletionEmailDate;
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
	}
	
}
