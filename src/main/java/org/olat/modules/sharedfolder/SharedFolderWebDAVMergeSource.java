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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;


/**
 * Delivery the shared folders configuered in SharedFolderWebDAVProvider
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SharedFolderWebDAVMergeSource extends MergeSource {
	
	private OLog log = Tracing.createLoggerFor(SharedFolderWebDAVMergeSource.class);

	private boolean init = false;
	private long loadTime;
	private final Identity identity;
	private final List<String> publiclyReadableFolders;
	
	public SharedFolderWebDAVMergeSource(Identity identity, List<String> publiclyReadableFolders) {
		super(null, "root");
		this.identity = identity;
		this.publiclyReadableFolders = publiclyReadableFolders;
	}

	@Override
	public List<VFSItem> getItems() {
		if(!init) {
			init();
		}
		return super.getItems();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!init  || (System.currentTimeMillis() - loadTime) > 60000) {
			init();
		}
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		if(init) {
			return super.resolve(path);
		}

		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) {
			return this;
		}
		
		String childName = VFSManager.extractChild(path);
		RepositoryManager repoManager = RepositoryManager.getInstance();
		
		//lookup in my shared folders
		List<RepositoryEntry> ownerEntries = repoManager.queryByOwner(identity, SharedFolderFileResource.TYPE_NAME);
		for (RepositoryEntry re : ownerEntries) {
			String name = RequestUtil.normalizeFilename(re.getDisplayname());
			if(childName.equals(name)) {
				VFSContainer shared = getSharedContainer(re, false);
				String nextPath = path.substring(childName.length() + 1);
				return shared.resolve(nextPath);
			}	
		}
		
		if (publiclyReadableFolders != null && publiclyReadableFolders.size() > 0) {
			String firstItem = publiclyReadableFolders.get(0);
			// If the first value in the list is '*', list all resource folders.
			if (firstItem != null && firstItem.equals("*")) {
				// fake role that represents normally logged in user
				Roles registeredUserRole = new Roles(false, false, false, false, false, false, false);
				List<String> types = Collections.singletonList(SharedFolderFileResource.TYPE_NAME);
				List<RepositoryEntry> allEntries = repoManager.queryByTypeLimitAccess(identity, types, registeredUserRole);
				for (RepositoryEntry re : allEntries) {
					String name = RequestUtil.normalizeFilename(re.getDisplayname());
					if(childName.equals(name)) {
						VFSContainer shared = getSharedContainer(re, true);
						String nextPath = path.substring(childName.length() + 1);
						return shared.resolve(nextPath);
					}	
				}
			} else {
				// only list the specified folders
				List<Long> publiclyReadableFoldersKeys = getSharedKeys();	
				List<RepositoryEntry> entries = repoManager.lookupRepositoryEntries(publiclyReadableFoldersKeys);
				for (RepositoryEntry re:entries) {
					String name = RequestUtil.normalizeFilename(re.getDisplayname());
					if (childName.equals(name) && 
							(re.getAccess() >= RepositoryEntry.ACC_USERS || (re.getAccess() == RepositoryEntry.ACC_OWNERS && re.isMembersOnly()))) {
						
						VFSContainer shared = getSharedContainer(re, true);
						String nextPath = path.substring(childName.length() + 1);
						return shared.resolve(nextPath);
					}
				}
			}
		}
		
		return super.resolve(path);
	}
	
	private VFSContainer getSharedContainer(RepositoryEntry re, boolean readOnly) {
		SharedFolderManager sfm = SharedFolderManager.getInstance();
		VFSContainer shared = sfm.getNamedSharedFolder(re, true);
		if(readOnly) {
			shared.setLocalSecurityCallback(readOnlyCallback);
		}
		return shared;
	}
	
	@Override
	protected void init() {
		super.init();

		SharedFolderManager sfm = SharedFolderManager.getInstance();
		RepositoryManager repoManager = RepositoryManager.getInstance();
		List<VFSContainer> containers = new ArrayList<>();
		Set<Long> addedEntries = new HashSet<>();
		List<RepositoryEntry> ownerEntries = repoManager.queryByOwner(identity, SharedFolderFileResource.TYPE_NAME);
		for (RepositoryEntry entry : ownerEntries) {
			VFSContainer container = sfm.getNamedSharedFolder(entry, true);
			addContainerToList(container, containers);
			addedEntries.add(entry.getKey());
		}

		// see /olat3/webapp/WEB-INF/olat_extensions.xml
		if (publiclyReadableFolders != null && publiclyReadableFolders.size() > 0) {
			// Temporarily save added entries. This is needed to make sure not to add an entry twice.
			
			String firstItem = publiclyReadableFolders.get(0);
			// If the first value in the list is '*', list all resource folders.
			if (firstItem != null && firstItem.equals("*")) {
				// fake role that represents normally logged in user
				Roles registeredUserRole = new Roles(false, false, false, false, false, false, false);
				List<String> types = Collections.singletonList(SharedFolderFileResource.TYPE_NAME);
				List<RepositoryEntry> allEntries = repoManager.queryByTypeLimitAccess(identity, types, registeredUserRole);
				for (RepositoryEntry entry : allEntries) {
					addReadonlyFolder(entry, sfm, addedEntries, containers);
				}
			} else {
				// only list the specified folders
				List<Long> publiclyReadableFoldersKeys = getSharedKeys();	
				List<RepositoryEntry> entries = repoManager.lookupRepositoryEntries(publiclyReadableFoldersKeys);
				for (RepositoryEntry entry:entries) {
					if (entry.getAccess() >= RepositoryEntry.ACC_USERS || (entry.getAccess() == RepositoryEntry.ACC_OWNERS && entry.isMembersOnly())) {
						// add folder (which is a repo entry) to root container if not present
						addReadonlyFolder(entry, sfm, addedEntries, containers);
					} else {
						log.warn("Access denied on entry::" + entry.getKey(), null);
					}
				}
			}
		}

		setMergedContainers(containers);
		loadTime = System.currentTimeMillis();
		init = true;
	}

	private void addReadonlyFolder(RepositoryEntry entry, SharedFolderManager sfm,
			Set<Long> addedEntries, List<VFSContainer> containers) {
		//
		if (!addedEntries.contains(entry.getKey())) {
			// add the entry (readonly)
			VFSContainer folder = sfm.getNamedSharedFolder(entry, true);
			folder.setLocalSecurityCallback(readOnlyCallback);
			addContainerToList(folder, containers);
			addedEntries.add(entry.getKey());
		}
	}
	
	
	private List<Long> getSharedKeys() {
		List<Long> publiclyReadableFoldersKeys = new ArrayList<Long>();
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
