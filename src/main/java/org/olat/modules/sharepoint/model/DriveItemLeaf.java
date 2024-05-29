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

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSExternalLeaf;
import org.olat.core.util.vfs.VFSExternalMetadata;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSSuccess;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.modules.sharepoint.SharePointHelper;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DriveItemLeaf implements VFSLeaf, VFSExternalLeaf {
	
	private final MicrosoftDrive drive;
	private final MicrosoftDriveItem driveItem;
	private final DriveItemMetadata metadata;
	private final SharePointDAO sharePointDao;
	private final VFSContainer parentContainer;
	private final TokenCredential tokenProvider;
	
	public DriveItemLeaf(VFSContainer parentContainer,
			MicrosoftDrive drive, MicrosoftDriveItem driveItem, DriveItemMetadata metadata,
			SharePointDAO sharePointDao, TokenCredential tokenProvider) {
		this.drive = drive;
		this.driveItem = driveItem;
		this.metadata = metadata;
		if(metadata != null) {
			metadata.setItem(this);
		}
		this.parentContainer = parentContainer;
		this.sharePointDao = sharePointDao;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public VFSItem resolve(String path) {
		return this;
	}

	@Override
	public VFSSuccess restore(VFSContainer targetContainer) {
		return VFSSuccess.ERROR_FAILED;
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
	public String getRelPath() {
		return driveItem.name();
	}

	@Override
	public VFSSuccess rename(String newname) {
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess delete() {
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess deleteSilently() {
		return VFSSuccess.ERROR_FAILED;
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
	public VFSStatus canRename() {
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus canDelete() {
		return VFSStatus.NO;
	}

	@Override
	public String getName() {
		return driveItem.name();
	}

	@Override
	public long getLastModified() {
		long lastModified = -1;
		if(driveItem.driveItem().getLastModifiedDateTime() != null) {
			lastModified = SharePointHelper.toDateInMilliSeconds(driveItem.driveItem().getLastModifiedDateTime());
		}
		return lastModified;
	}

	@Override
	public VFSStatus canCopy() {
		return VFSStatus.YES;
	}

	@Override
	public VFSStatus canWrite() {
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus canMeta() {
		return metadata == null ? VFSStatus.NO : VFSStatus.YES;
	}

	@Override
	public VFSStatus canVersion() {
		return VFSStatus.NO;
	}

	@Override
	public VFSExternalMetadata getMetaInfo() {
		return metadata;
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return parentContainer.getLocalSecurityCallback();
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		//
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if(vfsItem instanceof DriveItemLeaf leaf) {
			return this == leaf;
		}
		return false;
	}

	@Override
	public InputStream getInputStream() {
		return sharePointDao.getItemContent(drive.drive(), driveItem.driveItem(), tokenProvider);
	}

	@Override
	public long getSize() {
		Long size = driveItem.driveItem().getSize();
		return size == null ? -1l : size.longValue();
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		return null;
	}
}
