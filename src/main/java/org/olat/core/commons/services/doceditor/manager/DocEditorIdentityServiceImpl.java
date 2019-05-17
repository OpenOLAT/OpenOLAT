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
package org.olat.core.commons.services.doceditor.manager;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.services.doceditor.DocEditorIdentityService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocEditorIdentityServiceImpl implements DocEditorIdentityService {

	private static final Logger log = Tracing.createLoggerFor(DocEditorIdentityServiceImpl.class);
	
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private UserManager userManager;

	@Override
	public String getGlobalIdentityId(Identity identity) {
		return getGlobalUserIdPrefix() + identity.getName();
	}

	@Override
	public Identity getIdentity(String globalIdenityId) {
		try {
			String username = globalIdenityId.substring(getGlobalUserIdPrefix().length());
			return securityManager.findIdentityByNameCaseInsensitive(username);
		} catch (NumberFormatException e) {
			log.warn("Try to load identity with global unique id " + globalIdenityId, e);
		}
		return null;
	}

	private String getGlobalUserIdPrefix() {
		return "openolat." + WebappHelper.getInstanceId() + ".";
	}

	@Override
	public String getUserDisplayName(Identity identity) {
		return userManager.getUserDisplayName(identity);
	}

	@Override
	public boolean isCoach(Identity identity) {
		SearchIdentityParams params = new SearchIdentityParams();
		params.setIdentityKeys(Collections.singletonList(identity.getKey()));
		params.setRepositoryEntryRole(GroupRoles.coach);
		List<Identity> identities = securityManager.getIdentitiesByPowerSearch(params , 0, 1);
		return !identities.isEmpty();
	}

}
