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
package org.olat.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.quota.QuotaConstants;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithLazyQuotaCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.group.model.SearchBusinessGroupParams;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class GroupfoldersWebDAVMergeSource extends WebDAVMergeSource {
	
	private final CollaborationManager collaborationManager;

	public GroupfoldersWebDAVMergeSource(Identity identity, CollaborationManager collaborationManager) {
		super(identity);
		this.collaborationManager = collaborationManager;
	}
	
	@Override
	protected List<VFSContainer> loadMergedContainers() {
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);

		Set<Long> addedGroupKeys = new HashSet<>();
		Set<String> addedGroupNames = new HashSet<>();
		List<VFSContainer> containers = new ArrayList<>();
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, false);
		params.addTools(CollaborationTools.TOOL_FOLDER);
		List<BusinessGroup> tutorGroups = bgs.findBusinessGroups(params, null, 0, -1);
		for (BusinessGroup group : tutorGroups) {
			addContainer(group, addedGroupKeys, addedGroupNames, containers, true);
		}

		SearchBusinessGroupParams paramsParticipants = new SearchBusinessGroupParams(getIdentity(), false, true);
		paramsParticipants.addTools(CollaborationTools.TOOL_FOLDER);
		List<BusinessGroup> participantsGroups = bgs.findBusinessGroups(paramsParticipants, null, 0, -1);
		for (BusinessGroup group : participantsGroups) {
			addContainer(group, addedGroupKeys, addedGroupNames, containers, false);
		}
		return containers;
	}
	
	private void addContainer(BusinessGroup group, Set<Long> addedGroupKeys, Set<String> addedGroupNames,
			List<VFSContainer> containers, boolean isOwner) {
		if(addedGroupKeys.contains(group.getKey())) {
			return;
		}
		String name = nameIdentifier(group, addedGroupNames);
		if(name == null) {
			return;
		}

		VFSContainer grpContainer = getGroupContainer(name, group, isOwner);
		// add container
		addContainerToList(grpContainer, containers);
		addedGroupKeys.add(group.getKey());
	}
	
	private String nameIdentifier(BusinessGroup group, Set<String> addedGroupNames) {
		String name = group.getName();
		if (addedGroupNames.contains(name)) {
			// attach a serial to the group name to avoid duplicate mount points...
			int serial = 1;
			int serialMax = 100;
			while (addedGroupNames.contains(name + serial) && serial < serialMax) {
				serial++;
			}
			if (serial == serialMax) {
				return null; // continue without adding mount point
			}
			name = name + serial;
		}
		addedGroupNames.add(name);
		return name;
	}
	
	private VFSContainer getGroupContainer(String name, BusinessGroup group, boolean isOwner) {
		String folderPath = collaborationManager.getFolderRelPath(group);
		// create container and set quota
		VFSContainer localImpl = VFSManager.olatRootContainer(folderPath, this);
		//already done in OlatRootFolderImpl localImpl.getBasefile().mkdirs(); // lazy initialize dirs
		String containerName = RequestUtil.normalizeFilename(name);
		NamedContainerImpl grpContainer = new GroupNamedContainer(containerName, localImpl);

		boolean writeAccess;
		boolean readOnly = BusinessGroupStatusEnum.isReadOnly(group);
		if(readOnly) {
			writeAccess = false;
		} else if (!isOwner) {
			// check if participants have read/write access
			int folderAccess = CollaborationTools.FOLDER_ACCESS_ALL;
			Long lFolderAccess = collaborationManager.lookupFolderAccess(group);
			if (lFolderAccess != null) {
				folderAccess = lFolderAccess.intValue();
			}
			writeAccess = (folderAccess == CollaborationTools.FOLDER_ACCESS_ALL);
		} else {
			writeAccess = true;
		}
		
		VFSSecurityCallback secCallback;
		if(writeAccess) {
			SubscriptionContext sc = new SubscriptionContext(group, "toolfolder");
			secCallback = new FullAccessWithLazyQuotaCallback(folderPath, QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, sc);
		} else {
			secCallback = new ReadOnlyCallback();
		}
		grpContainer.setLocalSecurityCallback(secCallback);
		return grpContainer;
	}
	
	private static class GroupNamedContainer extends NamedContainerImpl {
		
		public GroupNamedContainer(String containerName, VFSContainer container) {
			super(containerName, container);
		}

		@Override
		public boolean exists() {
			return true;
		}
	}
}
