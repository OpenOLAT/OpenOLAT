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

package org.olat.group;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.quota.QuotaConstants;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.servlets.WebDAVProvider;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
/**
 * 
 * Description:<br>
 */
public class GroupfoldersWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "groupfolders";

	public String getMountPoint() { return MOUNTPOINT; }

	public VFSContainer getContainer(Identity identity) {
		MergeSource cfRoot = new MergeSource(null, null);
		// collect buddy groups
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		QuotaManager qm = QuotaManager.getInstance();
		List<BusinessGroup> groups = bgm.findBusinessGroupsAttendedBy(null, identity, null);
		groups.addAll(bgm.findBusinessGroupsOwnedBy(null, identity, null));

		List<Long> addedGroupKeys = new ArrayList<Long>();
		List<String> addedGroupNames = new ArrayList<String>();
		for (BusinessGroup group : groups) {
			if (addedGroupKeys.contains(group.getKey())) continue; // check for duplicate groups
			CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
			if (tools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
				String name = group.getName();
				if (addedGroupNames.contains(name)) {
					// attach a serial to the group name to avoid duplicate mount points...
					int serial = 1;
					int serialMax = 100;
					while (addedGroupNames.contains(name + serial) && serial < serialMax) {
						serial++;
					}
					if (serial == serialMax) continue; // continue without adding mount point
					name = name + serial;
				}
				
				// create container and set quota
				OlatRootFolderImpl localImpl = new OlatRootFolderImpl(tools.getFolderRelPath(), cfRoot);
				localImpl.getBasefile().mkdirs(); // lazy initialize dirs
				NamedContainerImpl grpContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(name), localImpl);
				Quota q = qm.getCustomQuota(tools.getFolderRelPath());
				if (q == null) {
					Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
					q = QuotaManager.getInstance().createQuota(tools.getFolderRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
				}
				
				//fxdiff VCRP-8: collaboration tools folder access control
				boolean writeAccess;
				boolean isOwner = BaseSecurityManager.getInstance().isIdentityInSecurityGroup(identity, group.getOwnerGroup());
				if (!isOwner) {
					// check if participants have read/write access
					int folderAccess = CollaborationTools.FOLDER_ACCESS_ALL;
					Long lFolderAccess = tools.lookupFolderAccess();
					if (lFolderAccess != null) {
						folderAccess = lFolderAccess.intValue();
					}
					writeAccess = (folderAccess == CollaborationTools.CALENDAR_ACCESS_ALL);
				} else {
					writeAccess = true;
				}
				
				VFSSecurityCallback secCallback;
				if(writeAccess) {
					SubscriptionContext sc = new SubscriptionContext(group, "toolfolder");
					secCallback = new FullAccessWithQuotaCallback(q, sc);
				} else {
					secCallback = new ReadOnlyCallback();
				}
				grpContainer.setLocalSecurityCallback(secCallback);
				
				// add container
				cfRoot.addContainer(grpContainer);
				addedGroupKeys.add(group.getKey());
				addedGroupNames.add(name);
			}
		}
		return cfRoot;
	}

}
