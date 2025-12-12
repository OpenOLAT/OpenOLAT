/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityOrganisationsRow;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserRow extends UserPropertiesRow implements IdentityOrganisationsRow {
	
	private final Identity identity;
	private UserMembershipStatus membershipStatus;
	private List<OrganisationWithParents> organisations;
	
	public UserRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
	}
	
	public UserRow(Identity identity, UserMembershipStatus status, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
		this.membershipStatus = status;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public List<OrganisationWithParents> getOrganisations() {
		return organisations;
	}

	@Override
	public void setOrganisations(List<OrganisationWithParents> organisations) {
		this.organisations = organisations;
	}

	public UserMembershipStatus getMembershipStatus() {
		return membershipStatus;
	}

	public void setMembershipStatus(UserMembershipStatus membershipStatus) {
		this.membershipStatus = membershipStatus;
	}

}
