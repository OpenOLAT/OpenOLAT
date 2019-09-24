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
package org.olat.modules.curriculum.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSearchParameters {
	
	private String searchString;
	private Identity elementOwner;
	private Identity managerIdentity;
	private Identity principalIdentity;
	private List<? extends OrganisationRef> organisations;
	
	private boolean withDeleted = false;

	public List<? extends OrganisationRef> getOrganisations() {
		if(organisations == null) {
			organisations = new ArrayList<>();
		}
		return organisations;
	}

	public void setOrganisations(List<? extends OrganisationRef> organisations) {
		this.organisations = organisations;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Identity getCurriculumAdmin() {
		return managerIdentity;
	}

	/**
	 * Set for curriculum owners and managers
	 * 
	 * @param managerIdentity
	 */
	public void setCurriculumAdmin(Identity identity) {
		managerIdentity = identity;
	}
	
	public Identity getCurriculumPrincipal() {
		return principalIdentity;
	}
	
	public void setCurriculumPrincipal(Identity identity) {
		principalIdentity = identity;
	}

	public Identity getElementOwner() {
		return elementOwner;
	}

	/**
	 * Set for repository entry owners and curriculum elements owners
	 * 
	 * @param elementOwner
	 */
	public void setElementOwner(Identity identity) {
		elementOwner = identity;
	}

	public boolean isWithDeleted() {
		return withDeleted;
	}

	public void setWithDeleted(boolean withDeleted) {
		this.withDeleted = withDeleted;
	}
}
