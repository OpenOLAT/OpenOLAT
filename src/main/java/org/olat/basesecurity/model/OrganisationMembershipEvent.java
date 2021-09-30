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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 15 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationMembershipEvent extends MultiUserEvent {

	private static final long serialVersionUID = 353880982543124020L;
	
	public static final String IDENTITY_ADDED = "organisation.identity.added";
	public static final String IDENTITY_REMOVED = "organisation.identity.removed";

	private final Long organisationKey;
	private final Long identityKey;
	
	public OrganisationMembershipEvent(String command, OrganisationRef organisationRef, IdentityRef identityRef) {
		super(command);
		this.organisationKey = organisationRef.getKey();
		this.identityKey = identityRef.getKey();
	}
	
	public static OrganisationMembershipEvent identityAdded(OrganisationRef organisationRef, IdentityRef identityRef) {
		return new OrganisationMembershipEvent(IDENTITY_ADDED, organisationRef, identityRef);
	}
	
	public static OrganisationMembershipEvent identityRemoved(OrganisationRef organisationRef, IdentityRef identityRef) {
		return new OrganisationMembershipEvent(IDENTITY_REMOVED, organisationRef, identityRef);
	}

	public Long getOrganisationKey() {
		return organisationKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

}
