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
package org.olat.gui.control;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

/**
 * 
 * <h3>Description:</h3>
 * AutoCreator for the FrentixTopNavController which allow to configure
 * an impressum or not, and the search or not
 * 
 * <p>
 * Initial Date:  25 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class TopNavCreator extends AutoCreator {
	
	private boolean impressum;
	private boolean search;
	private String internalSiteSoftKey;
	private boolean searchOnlyHasInternalSiteMember;
	
	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		boolean canSearch = canSearch(ureq.getIdentity());
		return new OlatTopNavController(ureq, wControl, impressum, canSearch);
	}
	
	public boolean isImpressum() {
		return impressum;
	}

	public void setImpressum(boolean impressum) {
		this.impressum = impressum;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}
	
	public String getInternalSiteSoftKey() {
		return internalSiteSoftKey;
	}

	public void setInternalSiteSoftKey(String internalSiteSoftKey) {
		this.internalSiteSoftKey = internalSiteSoftKey;
	}

	public boolean isSearchOnlyHasInternalSiteMember() {
		return searchOnlyHasInternalSiteMember;
	}

	public void setSearchOnlyHasInternalSiteMember(
			boolean searchOnlyHasInternalSiteMember) {
		this.searchOnlyHasInternalSiteMember = searchOnlyHasInternalSiteMember;
	}

	public boolean canSearch(Identity identity) {
		boolean canSearch = search;
		if(isSearchOnlyHasInternalSiteMember()) {
			String softKey = getInternalSiteSoftKey();
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(softKey, false);
			if(repoEntry != null) {
				RepositoryService contextManager = CoreSpringFactory.getImpl(RepositoryService.class);
				canSearch = contextManager.isMember(identity, repoEntry);
			}
		}
		return canSearch;
	}
}