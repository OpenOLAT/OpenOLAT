/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSExternalMetadata;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.modules.sharepoint.PermissionsDelegate;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.DriveItem;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DriveItemContainer extends AbstractSPContainer {
	
	private final VFSStatus writeFlag;
	private final MicrosoftDrive drive;
	private final MicrosoftDriveItem driveItem;
	
	private boolean initialized = false;
	private final List<VFSItem> items = new ArrayList<>();
	
	public DriveItemContainer(VFSContainer parentContainer,
			MicrosoftDrive drive, MicrosoftDriveItem driveItem, VFSStatus writeFlag,
			SharePointDAO sharePointDao, List<String> exclusionsLabels,
			PermissionsDelegate permissionsDelegate, TokenCredential tokenProvider) {
		super(parentContainer, driveItem.name(), sharePointDao, exclusionsLabels, permissionsDelegate, tokenProvider);
		this.drive = drive;
		this.driveItem = driveItem;
		this.writeFlag = writeFlag;
	}

	public VFSStatus getWriteFlag() {
		return writeFlag;
	}
	
	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			items.clear();
			
			List<MicrosoftDriveItem> childrenItems = sharePointDao.getDriveItems(drive.drive(), driveItem.driveItem(), tokenProvider);
			items.addAll(toVFS(drive, childrenItems));
			initialized = true;
		}
		return items;
	}
	
	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		List<MicrosoftDriveItem> foundItems = sharePointDao.searchDriveItems(drive.drive(), "", tokenProvider);
		return toVFS(drive, foundItems);
	}
	
	@Override
	public VFSStatus canMeta() {
		return VFSStatus.NO;
	}
	
	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}
	
	@Override
	public VFSExternalMetadata getMetaInfo() {
		return null;
	}

	@Override
	public String getRelPath() {
		return driveItem.name();
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if(vfsItem instanceof DriveItemContainer driveContainer) {
			return this == driveContainer;
		}
		return false;
	}
	
	@Override
	public VFSStatus canWrite() {
		return writeFlag == null ? VFSStatus.NO : writeFlag;
	}
	
	@Override
	public VFSSuccess copyFrom(VFSItem vfsItem, Identity savedBy) {
		List<DriveItem> parentLine = getParentLine();
		return upload(parentLine, vfsItem);
	}
	
	private VFSSuccess upload(List<DriveItem> parentLine, VFSItem vfsItem) {
		if(vfsItem instanceof VFSLeaf leaf) {
			return upload(parentLine, leaf);
		}
		
		VFSSuccess allOk = VFSSuccess.SUCCESS;
		if(vfsItem instanceof VFSContainer container) {
			DriveItem directoryItem = sharePointDao.createDirectory(drive.drive(), parentLine, container.getName(), tokenProvider);
			List<DriveItem> directoryParentLine = new ArrayList<>(parentLine);
			directoryParentLine.add(directoryItem);
			
			List<VFSItem> children = container.getItems();
			for(VFSItem child:children) {
				VFSSuccess uploadStatus = upload(directoryParentLine, child);
				allOk = mergeSuccessStatus(allOk, uploadStatus);
			}
		}
		return allOk;
	}
	
	private VFSSuccess mergeSuccessStatus(VFSSuccess currentStatus, VFSSuccess newStatus) {
		if(currentStatus == VFSSuccess.SUCCESS && newStatus == VFSSuccess.SUCCESS) {
			return VFSSuccess.SUCCESS;
		}
		if(newStatus != VFSSuccess.SUCCESS) {
			return newStatus;
		}
		return currentStatus;
	}
	
	private VFSSuccess upload(List<DriveItem> parentLine, VFSLeaf leaf) {
		DriveItem uploadedItem = sharePointDao.uploadLargeFile(drive.drive(), parentLine, leaf, leaf.getName(), tokenProvider);
		return uploadedItem == null ? VFSSuccess.ERROR_FAILED : VFSSuccess.SUCCESS;
	}
	
	private List<DriveItem> getParentLine() {
		List<DriveItem> parentLine = new ArrayList<>();
		for(VFSContainer container=this; container.getParentContainer() != null; container=container.getParentContainer()) {
			if(container instanceof DriveItemContainer itemContainer) {
				parentLine.add(itemContainer.driveItem.driveItem());
			}
		}
		if(parentLine.size() > 1) {
			Collections.reverse(parentLine);
		}
		return parentLine;
	}
}
