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
package org.olat.modules.sharedfolder;

import static org.olat.modules.sharedfolder.SharedFolderWebDAVProvider.readOnlyCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.ACService;


/**
 * Delivery the shared folders configuered in SharedFolderWebDAVProvider
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SharedFolderWebDAVMergeSource extends WebDAVMergeSource {
	
	private static final Logger log = Tracing.createLoggerFor(SharedFolderWebDAVMergeSource.class);

	private final List<String> publiclyReadableFolders;
	
	public SharedFolderWebDAVMergeSource(Identity identity, List<String> publiclyReadableFolders) {
		super(identity);
		this.publiclyReadableFolders = publiclyReadableFolders;
	}

	@Override
	protected List<VFSContainer> loadMergedContainers() {
		SharedFolderManager sfm = SharedFolderManager.getInstance();
		RepositoryManager repoManager = RepositoryManager.getInstance();
		List<VFSContainer> containers = new ArrayList<>();
		Set<Long> addedEntries = new HashSet<>();
		List<OrganisationRef> offerOrganisations = CoreSpringFactory.getImpl(ACService.class).getOfferOrganisations(getIdentity());
		
		List<RepositoryEntry> ownerEntries = repoManager.queryByMembership(getIdentity(), true, true, false, SharedFolderFileResource.TYPE_NAME);
		for (RepositoryEntry entry : ownerEntries) {
			if(entry != null && !addedEntries.contains(entry.getKey())) {
				VFSContainer container = sfm.getNamedSharedFolder(entry, true);
				if(container != null) {
					addContainerToList(container, containers);
					addedEntries.add(entry.getKey());
				}
			}
		}
		
		List<RepositoryEntry> participantEntries = repoManager.queryByMembership(getIdentity(), false, false, true, SharedFolderFileResource.TYPE_NAME);
		for (RepositoryEntry entry : participantEntries) {
			addReadonlyFolder(entry, sfm, addedEntries, containers);
		}

		// see /webapp/WEB-INF/classes/org/olat/core/commons/services/webdav/webdavContext.xml
		if (publiclyReadableFolders != null && !publiclyReadableFolders.isEmpty()) {
			// Temporarily save added entries. This is needed to make sure not to add an entry twice.
			
			String firstItem = publiclyReadableFolders.get(0);
			// If the first value in the list is '*', list all resource folders.
			if (firstItem != null && firstItem.equals("*")) {
				// fake role that represents normally logged in user
				SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(getIdentity(), Roles.userRoles(), SharedFolderFileResource.TYPE_NAME);
				params.setOfferOrganisations(offerOrganisations);
				params.setOfferValidAt(new Date());
				List<RepositoryEntry> allEntries = repoManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
				for (RepositoryEntry entry : allEntries) {
					addReadonlyFolder(entry, sfm, addedEntries, containers);
				}
			} else {
				// only list the specified folders
				List<Long> publiclyReadableFoldersKeys = getSharedKeys();	
				List<RepositoryEntry> entries = repoManager.lookupRepositoryEntries(publiclyReadableFoldersKeys);
				for (RepositoryEntry entry:entries) {
					if (entry.getEntryStatus() == RepositoryEntryStatusEnum.published
							|| entry.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
						// add folder (which is a repo entry) to root container if not present
						addReadonlyFolder(entry, sfm, addedEntries, containers);
					} else {
						log.warn("Access denied on entry::" + entry.getKey());
					}
				}
			}
		}

		return containers;
	}

	private void addReadonlyFolder(RepositoryEntry entry, SharedFolderManager sfm,
			Set<Long> addedEntries, List<VFSContainer> containers) {
		//
		if (!addedEntries.contains(entry.getKey())) {
			// add the entry (readonly)
			VFSContainer folder = sfm.getNamedSharedFolder(entry, true);
			if(folder != null) {
				folder.setLocalSecurityCallback(readOnlyCallback);
				addContainerToList(folder, containers);
				addedEntries.add(entry.getKey());
			}
		}
	}
	
	
	private List<Long> getSharedKeys() {
		List<Long> publiclyReadableFoldersKeys = new ArrayList<>();
		for (String folder : publiclyReadableFolders) {
			try {
				Long repoKey = Long.parseLong(folder);
				publiclyReadableFoldersKeys.add(repoKey);
			} catch (NumberFormatException e) {
				// Invalid id name
				log.warn("The list item::" + folder + " of publiclyReadableFolders is invalid. Should be repsitoryEntryId or '*'.", e);
			}
		}
		return publiclyReadableFoldersKeys;	
	}
}
