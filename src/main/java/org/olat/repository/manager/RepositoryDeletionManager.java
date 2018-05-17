/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.manager;

import java.io.File;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryDeletionModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Initial Date:  Mar 31, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
@Service("repositoryDeletionManager")
public class RepositoryDeletionManager implements UserDataDeletable {

	private static final OLog log = Tracing.createLoggerFor(RepositoryDeletionManager.class);

	public static final String SEND_DELETE_EMAIL_ACTION = "sendDeleteEmail";

	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryDeletionModule deletionModule;

	/**
	 * Remove identity as owner and initial author. Used in user-deletion.
	 * If there is no other owner and/or author, the olat-administrator (defined in olat.properties) will be added as owner.
	 *   
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		// Remove as owner
		Identity adminIdentity = deletionModule.getAdminUserIdentity();
		List<RepositoryEntry> ownedRepoEntries = repositoryManager.queryByOwner(identity);
		for (RepositoryEntry repositoryEntry: ownedRepoEntries) {
			repositoryService.removeRole(identity, repositoryEntry, GroupRoles.owner.name());
			if (adminIdentity != null && repositoryService.countMembers(repositoryEntry, GroupRoles.owner.name()) == 0) {
				// This group has no owner anymore => add OLAT-Admin as owner
				repositoryService.addRole(deletionModule.getAdminUserIdentity(), repositoryEntry, GroupRoles.owner.name());
				log.info("Delete user-data, add Administrator-identity as owner of repositoryEntry=" + repositoryEntry.getDisplayname());
			}
		}
		// Remove as initial author
		List<RepositoryEntry> repoEntries = repositoryManager.queryByInitialAuthor(identity.getName());
		if(!repoEntries.isEmpty()) {
			String replacement = "administrator";
			if(adminIdentity != null) {
				replacement = adminIdentity.getName();
			}
			for (RepositoryEntry repositoryEntry: repoEntries) {
				repositoryEntry.setInitialAuthor(replacement);
				repositoryService.update(repositoryEntry);
				log.info("Delete user-data, add Administrator-identity as initial-author of repositoryEntry=" + repositoryEntry.getDisplayname());
			}
		}
		log.debug("All owner and initial-author entries in repository deleted for identity=" + identity);
	}
}