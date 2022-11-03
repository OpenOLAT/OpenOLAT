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
package org.olat.admin.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Organisation;
import org.olat.core.id.UserConstants;
import org.olat.user.UserManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserSearchListProvider implements ListProvider {
	
	private static final int MAX_ENTRIES = 15;
	
	private final BaseSecurity securityManager;
	private final UserManager userManager;
	private final List<Organisation> searchableOrganisations;
	private final GroupRoles repositoryEntryRole;
	private final OrganisationRoles[] excludedRoles;
	
	public UserSearchListProvider(List<Organisation> searchableOrganisations) {
		this(searchableOrganisations, null, null);
	}
	
	public UserSearchListProvider(List<Organisation> searchableOrganisations, GroupRoles repositoryEntryRole, OrganisationRoles[] excludedRoles) {
		this.searchableOrganisations = searchableOrganisations;
		this.repositoryEntryRole = repositoryEntryRole;
		this.excludedRoles = excludedRoles;
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}
	
	public List<Organisation> getSearchableOrganisations() {
		return searchableOrganisations;
	}

	public GroupRoles getRepositoryEntryRole() {
		return repositoryEntryRole;
	}
	
	public OrganisationRoles[] getExcludedRoles() {
		return excludedRoles;
	}

	@Override
	public int getMaxEntries() {
		return MAX_ENTRIES;
	}

	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		Map<String, String> userProperties = new HashMap<>();
		// We can only search in mandatory User-Properties due to problems
		// with hibernate query with join and not existing rows
		userProperties.put(UserConstants.FIRSTNAME, searchValue);
		userProperties.put(UserConstants.LASTNAME, searchValue);
		userProperties.put(UserConstants.EMAIL, searchValue);
		// Search in all fileds -> non intersection search

		int maxEntries = MAX_ENTRIES;
		List<IdentityShort> res = securityManager.searchIdentityShort(searchValue, searchableOrganisations,
				repositoryEntryRole, excludedRoles, maxEntries);

		boolean hasMore = false;
		for (Iterator<IdentityShort> it_res = res.iterator(); (hasMore=it_res.hasNext()) && maxEntries > 0;) {
			maxEntries--;
			IdentityShort ident = it_res.next();
			String key = ident.getKey().toString();
			String displayKey = ident.getNickName();
			String displayText = userManager.getUserDisplayName(ident);
			receiver.addEntry(key, displayKey, displayText, "o_icon o_icon-fw " + CSSHelper.CSS_CLASS_USER);
		}
		if(hasMore){
			receiver.addEntry(".....",".....");
		}
	}
}
