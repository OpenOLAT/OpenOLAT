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

import org.olat.core.id.OrganisationRef;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationChangeEvent extends MultiUserEvent {

	private static final long serialVersionUID = -6494263081500759931L;
	
	public static final String ORGANISATION_CREATED = "organisation.created";
	public static final String ORGANISATION_DELETED = "organisation.deleted";
	public static final String ORGANISATION_CHANGED = "organisation.changed";

	private final Long organisationKey;
	
	public OrganisationChangeEvent(String command, OrganisationRef organisationRef) {
		super(command);
		this.organisationKey = organisationRef.getKey();
	}
	
	public static OrganisationChangeEvent organisationCreated(OrganisationRef organisationRef) {
		return new OrganisationChangeEvent(ORGANISATION_CREATED, organisationRef);
	}
	
	public static OrganisationChangeEvent organisationDeleted(OrganisationRef organisationRef) {
		return new OrganisationChangeEvent(ORGANISATION_DELETED, organisationRef);
	}
	
	public static OrganisationChangeEvent organisationChanged(OrganisationRef organisationRef) {
		return new OrganisationChangeEvent(ORGANISATION_CHANGED, organisationRef);
	}

	public Long getOrganisationKey() {
		return organisationKey;
	}

}
