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
 * <p>
 */

package org.olat.modules.sharedfolder;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.servlets.WebDAVProvider;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider, Gregor Wassmann
 */
public class SharedFolderWebDAVProvider extends LogDelegator implements WebDAVProvider {
	private static List<String> publiclyReadableFolders;
	private static final VFSSecurityCallback readOnlyCallback = new ReadOnlyCallback();

	/**
	 * Spring setter.
	 * <p>
	 * In /olat3/webapp/WEB-INF/olat_extensions.xml the bean
	 * 'webdav_sharedfolders' has an optional property called
	 * 'publiclyReadableFolders':
	 * 
	 * <pre>
	 * &lt;property name=&quot;publiclyReadableFolders&quot;&gt;
	 *   &lt;list&gt;
	 *     &lt;value&gt;7045120&lt;/value&gt;
	 *     &lt;value&gt;{another repository entry key}&lt;/value&gt;
	 *   &lt;/list&gt;
	 * &lt;/property&gt;
	 * </pre>
	 * 
	 * It's a list of repositoryEntryKeys belonging to resource folders. These
	 * folders will then be displayed (in readonly mode) in WebDAV provided that
	 * the repository entry allows access from all users or guests.
	 * <p>
	 * Alternatively, use '*' as the first value in the list to indicate that all
	 * resource folders should be listed in WebDAV.
	 * 
	 * @param folders
	 */
	public void setPubliclyReadableFolders(List<String> repositoryEntryKeys) {
		publiclyReadableFolders = repositoryEntryKeys;
	}

	/**
	 * @see org.olat.commons.servlets.util.WebDAVProvider#getMountPoint()
	 */
	public String getMountPoint() {
		return "sharedfolders";
	}

	/**
	 * @see org.olat.commons.servlets.util.WebDAVProvider#getContainer(org.olat.core.id.Identity)
	 */
	public VFSContainer getContainer(Identity identity) {
		MergeSource rootContainer = new MergeSource(null, "root");

		SharedFolderManager sfm = SharedFolderManager.getInstance();
		RepositoryManager repoManager = RepositoryManager.getInstance();
		List<RepositoryEntry> ownerEntries = (List<RepositoryEntry>) repoManager.queryByOwner(identity, SharedFolderFileResource.TYPE_NAME);
		for (RepositoryEntry repoEntry : ownerEntries) {
			rootContainer.addContainer(sfm.getNamedSharedFolder(repoEntry));
		}

		// see /olat3/webapp/WEB-INF/olat_extensions.xml
		if (publiclyReadableFolders != null && publiclyReadableFolders.size() > 0) {
			// Temporarily save added entries. This is needed to make sure not to add
			// an
			// entry twice.
			List<RepositoryEntry> addedEntries = new ArrayList<RepositoryEntry>(ownerEntries);
			//
			String firstItem = publiclyReadableFolders.get(0);
			// If the first value in the list is '*', list all resource folders.
			if (firstItem != null && firstItem.equals("*")) {
				// fake role that represents normally logged in user
				Roles registeredUserRole = new Roles(false, false, false, false, false, false, false);
				List<RepositoryEntry> allEntries = (List<RepositoryEntry>) repoManager.queryByTypeLimitAccess(SharedFolderFileResource.TYPE_NAME,
						registeredUserRole);
				for (RepositoryEntry entry : allEntries) {
					addReadonlyFolder(rootContainer, entry, sfm, addedEntries);
				}
			} else {
				// only list the specified folders
				for (String folder : publiclyReadableFolders) {
					try {
						Long repoKey = Long.parseLong(folder);
						RepositoryEntry entry = repoManager.lookupRepositoryEntry(repoKey);
						if (entry != null) {
							if (entry.getAccess() >= RepositoryEntry.ACC_USERS) {
								// add folder (which is a repo entry) to root container if not
								// present
								addReadonlyFolder(rootContainer, entry, sfm, addedEntries);
							} else {
								logWarn("Access denied on entry::" + entry.getKey(), null);
							}
						} else {
							logWarn("The repsitoryEntryId::" + folder + " does not exist.", null);
						}
					} catch (NumberFormatException e) {
						// Invalid id name
						logWarn("The list item::" + folder + " of publiclyReadableFolders is invalid. Should be repsitoryEntryId or '*'.", e);
					}
				}
			}
		}
		return rootContainer;
	}

	// If there is a bean property 'publiclyReadableFolders' do the following:

	/**
	 * Outsourced helper method for adding an entry to the root container.
	 * 
	 * @param rootContainer
	 * @param sfm
	 * @param ownerEntries
	 * @param entry
	 */
	private void addReadonlyFolder(MergeSource rootContainer, RepositoryEntry entry, SharedFolderManager sfm,
			List<RepositoryEntry> addedEntries) {
		//
		if (addedEntries == null || !PersistenceHelper.listContainsObjectByKey(addedEntries, entry)) {
			// add the entry (readonly)
			VFSContainer folder = sfm.getNamedSharedFolder(entry);
			folder.setLocalSecurityCallback(readOnlyCallback);
			rootContainer.addContainer(folder);
			addedEntries.add(entry);
		}
	}

}
