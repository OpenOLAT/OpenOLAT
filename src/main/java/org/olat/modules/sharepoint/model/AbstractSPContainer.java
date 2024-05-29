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
import java.util.List;

import org.olat.core.util.vfs.AbstractVirtualContainer;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSExternalItem;
import org.olat.core.util.vfs.VFSExternalMetadata;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSAllItemsFilter;
import org.olat.modules.sharepoint.SharePointHelper;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ThumbnailSet;
import com.microsoft.graph.models.User;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSPContainer extends AbstractVirtualContainer implements VFSExternalItem {
	
	private final VFSContainer parentContainer;
	protected final SharePointDAO sharePointDao;
	protected final TokenCredential tokenProvider;
	protected final List<String> exclusionsLabels;

	public AbstractSPContainer(VFSContainer parentContainer, String name,
			SharePointDAO sharePointDao, List<String> exclusionsLabels,
			TokenCredential tokenProvider) {
		super(name);
		this.parentContainer = parentContainer;
		this.sharePointDao = sharePointDao;
		this.tokenProvider = tokenProvider;
		this.exclusionsLabels = exclusionsLabels;
	}
	
	protected List<VFSItem> toVFS(MicrosoftDrive drive, List<MicrosoftDriveItem> driveItems) {
		List<VFSItem> items = new ArrayList<>();
		if(driveItems == null || driveItems.isEmpty()) return items;
		
		for(MicrosoftDriveItem item:driveItems) {
			if(SharePointDAO.accept(item, exclusionsLabels)) {
				if(item.directory()) {
					items.add(new DriveItemContainer(this, drive, item,
							sharePointDao, exclusionsLabels, tokenProvider));
				} else {
					DriveItemMetadata metadata = toMetadata(item);
					items.add(new DriveItemLeaf(this, drive, item, metadata, sharePointDao, tokenProvider));
				}
			}
		}
		return items;
	}
	
	protected DriveItemMetadata toMetadata(MicrosoftDriveItem item) {
		DriveItem driveItem = item.driveItem();
		
		DriveItemMetadata metadata = new DriveItemMetadata();

		metadata.setFilename(driveItem.getName());
		metadata.setDirectory(driveItem.getFolder() != null);
		metadata.setCreationDate(SharePointHelper.toDate(driveItem.getCreatedDateTime()));
		metadata.setFileLastModified(SharePointHelper.toDate(driveItem.getLastModifiedDateTime()));
		metadata.setLastModified(SharePointHelper.toDate(driveItem.getLastModifiedDateTime()));
		
		metadata.setUrl(driveItem.getWebUrl());
		metadata.setUri(driveItem.getWebUrl());
		metadata.setUuid(driveItem.getId());
		
		
		User createdBy = driveItem.getCreatedByUser();
		if(createdBy != null) {
			String givenName = createdBy.getGivenName();
			String surName = createdBy.getSurname();
			
			System.out.println(givenName + " " + surName);
		}
		
		ThumbnailSet thumbnails = item.thumbnails();
		if(thumbnails != null) {
			if(thumbnails.getLarge() != null) {
				metadata.setLargeThumbnailUrl(thumbnails.getLarge().getUrl());
			}
			if(thumbnails.getMedium() != null) {
				metadata.setThumbnailUrl(thumbnails.getMedium().getUrl());
			}
		}
	
		if(driveItem.getSize() != null) {
			metadata.setFileSize(driveItem.getSize());
		}

		return metadata;
	}

	@Override
	public VFSExternalMetadata getMetaInfo() {
		return null;
	}
	
	@Override
	public List<VFSItem> getItems() {
		return getItems(VFSAllItemsFilter.ACCEPT_ALL);
	}

	@Override
	public boolean isInPath(String path) {
		return false;
	}

	@Override
	public VFSItem resolve(String path) {
		return VFSManager.resolveFile(this, path);
	}
	
	@Override
	public VFSContainer getParentContainer() {
		return parentContainer;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		//
	}
	
	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isHidden() {
		return false;
	}
	
	@Override
	public VFSStatus canWrite() {
		return VFSStatus.NO;
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return parentContainer == null ? null : parentContainer.getLocalSecurityCallback();
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		//
	}
}
