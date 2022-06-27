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
package org.olat.repository.wizard;

import java.util.List;

import org.olat.core.id.Organisation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController.OfferAccessWithOrganisation;
/**
 * 
 * @author fkiefer
 *
 */
import org.olat.resource.accesscontrol.ui.AccessConfigurationController.OfferWithOrganisation;
public class AccessAndProperties {
	private RepositoryEntry repoEntry;
	private RepositoryEntryAllowToLeaveOptions setting;
	private RepositoryEntryStatusEnum status;
	private boolean publicVisible;
	private boolean canCopy;
	private boolean canReference;
	private boolean canDownload;
	private List<Organisation> organisations;
	
	private List<OfferAccessWithOrganisation> offerAccess;
	private List<OfferWithOrganisation> openAccess;
	private Offer guestOffer;

	public void setRepositoryEntry(RepositoryEntry repoEntry) {
		this.repoEntry = repoEntry;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repoEntry;
	}

	public RepositoryEntryAllowToLeaveOptions getSetting() {
		return setting;
	}

	public void setSetting(RepositoryEntryAllowToLeaveOptions setting) {
		this.setting = setting;
	}

	public RepositoryEntryStatusEnum getStatus() {
		return status;
	}

	public void setStatus(RepositoryEntryStatusEnum status) {
		this.status = status;
	}

	public boolean isPublicVisible() {
		return publicVisible;
	}

	public void setPublicVisible(boolean publicVisible) {
		this.publicVisible = publicVisible;
	}

	public boolean isCanCopy() {
		return canCopy;
	}

	public void setCanCopy(boolean canCopy) {
		this.canCopy = canCopy;
	}

	public boolean isCanReference() {
		return canReference;
	}

	public void setCanReference(boolean canReference) {
		this.canReference = canReference;
	}

	public boolean isCanDownload() {
		return canDownload;
	}

	public void setCanDownload(boolean canDownload) {
		this.canDownload = canDownload;
	}
	
	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
	}

	
	public List<OfferAccessWithOrganisation> getOfferAccess() {
		return offerAccess;
	}

	public void setOfferAccess(List<OfferAccessWithOrganisation> offerAccess) {
		this.offerAccess = offerAccess;
	}

	public List<OfferWithOrganisation> getOpenAccess() {
		return openAccess;
	}

	public void setOpenAccess(List<OfferWithOrganisation> openAccess) {
		this.openAccess = openAccess;
	}

	public Offer getGuestOffer() {
		return guestOffer;
	}

	public void setGuestOffer(Offer guestOffer) {
		this.guestOffer = guestOffer;
	}
	
}