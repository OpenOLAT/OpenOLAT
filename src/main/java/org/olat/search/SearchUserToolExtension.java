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
package org.olat.search;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolCategory;
import org.olat.admin.user.tools.UserToolExtension;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 29.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchUserToolExtension extends UserToolExtension {
	
	private boolean search;
	private String internalSiteSoftKey;
	private boolean searchOnlyHasInternalSiteMember;
	
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

	public void setSearchOnlyHasInternalSiteMember(boolean searchOnlyHasInternalSiteMember) {
		this.searchOnlyHasInternalSiteMember = searchOnlyHasInternalSiteMember;
	}
	
	@Override
	public boolean isShortCutOnly() {
		return true;
	}

	@Override
	public String getShortCutCssId() {
		return "o_navbar_search_opener";
	}
	
	@Override
	public String getShortCutCssClass() {
		return "dropdown";
	}

	@Override
	public UserToolCategory getUserToolCategory() {
		return UserToolCategory.search;
	}

	@Override
	public String getUniqueExtensionID() {
		return "org.olat.home.HomeMainController:org.olat.gui.control.SearchUserToolExtension";
	}

	@Override
	public UserTool createUserTool(UserRequest ureq, WindowControl wControl, Locale locale) {
		boolean canSearch = isEnabled();
		if(canSearch && isSearchOnlyHasInternalSiteMember()) {
			String softKey = getInternalSiteSoftKey();
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(softKey, false);
			if(repoEntry != null) {
				RepositoryService contextManager = CoreSpringFactory.getImpl(RepositoryService.class);
				canSearch = contextManager.isMember(ureq.getUserSession().getIdentity(), repoEntry);
			}
		}
		return canSearch ? new SearchUserTool(wControl) : null;
	}

	@Override
	public boolean isEnabled() {
		return search && super.isEnabled();
	}
}