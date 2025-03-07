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
package org.olat.resource.accesscontrol.model;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 4 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchReservationParameters {
	
	private final List<OLATResource> resources;
	private Collection<? extends IdentityRef> identities;
	private boolean withConfirmationDate;
	private Boolean confirmationByUser;
	
	public SearchReservationParameters(List<OLATResource> resources) {
		this.resources = resources;
	}
	
	public List<OLATResource> getResources() {
		return resources;
	}

	public Collection<? extends IdentityRef> getIdentities() {
		return identities;
	}

	public void setIdentities(Collection<? extends IdentityRef> identities) {
		this.identities = identities;
	}

	public boolean isWithConfirmationDate() {
		return withConfirmationDate;
	}

	public void setWithConfirmationDate(boolean withConfirmationDate) {
		this.withConfirmationDate = withConfirmationDate;
	}

	public Boolean getConfirmationByUser() {
		return confirmationByUser;
	}

	public void setConfirmationByUser(Boolean confirmationByUser) {
		this.confirmationByUser = confirmationByUser;
	}
}
