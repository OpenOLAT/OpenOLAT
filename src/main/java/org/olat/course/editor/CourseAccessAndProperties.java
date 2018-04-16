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
package org.olat.course.editor;

import java.util.List;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
/**
 * 
 * @author fkiefer
 *
 */
public class CourseAccessAndProperties {
	private RepositoryEntry repoEntry;
	private RepositoryEntryAllowToLeaveOptions setting;
	private int access;
	private boolean membersOnly;
	private boolean canCopy;
	private boolean canReference;
	private boolean canDownload;	
	private Boolean confirmationEmail;
	
	private List<OfferAccess> offerAccess;
	private List<Offer> deletedOffer;
	
	public CourseAccessAndProperties(RepositoryEntry re) {
		this.repoEntry = re;
	}
	
	public CourseAccessAndProperties(RepositoryEntry re, RepositoryEntryAllowToLeaveOptions setting, int access,
			boolean membersOnly, boolean canCopy, boolean canReference, boolean canDownload) {
		this.repoEntry = re;
		this.setting = setting;
		this.access = access;
		this.membersOnly = membersOnly;
		this.canCopy = canCopy;
		this.canReference = canReference;
		this.canDownload = canDownload;
	}	
	
	public List<OfferAccess> getOfferAccess() {
		return offerAccess;
	}

	public void setOfferAccess(List<OfferAccess> offerAccess) {
		this.offerAccess = offerAccess;
	}

	public List<Offer> getDeletedOffer() {
		return deletedOffer;
	}

	public void setDeletedOffer(List<Offer> deletedOffer) {
		this.deletedOffer = deletedOffer;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repoEntry;
	}

	public void setRepositoryEntry(RepositoryEntry re) {
		this.repoEntry = re;
	}

	public RepositoryEntryAllowToLeaveOptions getSetting() {
		return setting;
	}

	public void setSetting(RepositoryEntryAllowToLeaveOptions setting) {
		this.setting = setting;
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}

	public boolean isMembersOnly() {
		return membersOnly;
	}

	public void setMembersOnly(boolean membersOnly) {
		this.membersOnly = membersOnly;
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

	public Boolean getConfirmationEmail() {
		return confirmationEmail;
	}

	public void setConfirmationEmail(Boolean confirmationEmail) {
		this.confirmationEmail = confirmationEmail;
	}
}