/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.vfs;

import java.util.Date;
import java.util.Objects;

import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * 
 * Initial date: 26 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSMetadataItem implements VFSItem {
	
	private VFSContainer parentContainer;
	private VFSMetadataRef vfsMetadataRef;
	private VFSMetadata vfsMetadata;
	private VFSItem vfsItem;
	private VFSSecurityCallback secCallback;
	protected String namedContainerName;
	
	protected final VFSRepositoryService vfsRepositoryService;
	
	protected VFSMetadataItem(VFSRepositoryService vfsRepositoryService, VFSMetadata vfsMetadata,
			VFSContainer parentContainer, VFSSecurityCallback secCallback) {
		this.vfsRepositoryService = vfsRepositoryService;
		this.vfsMetadataRef = vfsMetadata;
		this.parentContainer = parentContainer;
		this.vfsMetadata = vfsMetadata;
		this.secCallback = secCallback;
	}
	
	public VFSItem getItem() {
		if (vfsItem == null) {
			createItem();
		}
		return vfsItem;
	}

	protected void createItem() {
		vfsItem = vfsRepositoryService.getItemFor(getMetaInfo());
		if (vfsItem != null) {
			vfsItem.setLocalSecurityCallback(secCallback);
			vfsItem.setParentContainer(parentContainer);
			onItemCreated(vfsItem);
		}
	}

	/**
	 * Hook for subclasses
	 * 
	 * @param createdItem the created item
	 */
	protected void onItemCreated(VFSItem createdItem) {
		//
	}

	protected void reset() {
		vfsMetadata = null;
		vfsItem = null;
	}
	
	@Override
	public VFSMetadata getMetaInfo() {
		if (vfsMetadata == null) {
			vfsMetadata = vfsRepositoryService.getMetadata(vfsMetadataRef);
		}
		return vfsMetadata;
	}
	
	@Override
	public String getRelPath() {
		if (getMetaInfo() == null) {
			return null;
		}
		return "/" + getMetaInfo().getRelativePath() + "/" + getMetaInfo().getFilename();
	}
	
	@Override
	public boolean isSame(VFSItem vfsItem) {
		if (vfsItem == null || getMetaInfo() == null) {
			return false;
		}
		// Works for LocalFolderImpl. What about others?
		return Objects.equals(VFSManager.appendLeadingSlash(getRelPath()), vfsItem.getRelPath());
	}

	@Override
	public VFSItem resolve(String path) {
		if (getItem() == null) {
			return null;
		}
		return getItem().resolve(path);
	}
	
	@Override
	public VFSStatus rename(String newname) {
		if (getItem() == null) {
			return VFSStatus.ERROR_FAILED;
		}
		VFSStatus vfsStatus = getItem().rename(newname);
		reset();
		return vfsStatus;
	}

	@Override
	public VFSStatus delete() {
		if (getItem() == null) {
			return VFSStatus.SUCCESS;
		}
		return getItem().delete();
	}

	@Override
	public VFSStatus restore(VFSContainer targetContainer) {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().restore(targetContainer);
	}

	@Override
	public VFSStatus deleteSilently() {
		if (getItem() == null) {
			return VFSStatus.SUCCESS;
		}
		return getItem().deleteSilently();
	}

	@Override
	public boolean exists() {
		if (getItem() == null) {
			return false;
		}
		return getItem().exists();
	}

	@Override
	public boolean isHidden() {
		if (getItem() == null) {
			return true;
		}
		return getItem().isHidden();
	}

	@Override
	public VFSStatus canCopy() {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().canCopy();
	}

	@Override
	public VFSStatus canWrite() {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().canWrite();
	}

	@Override
	public VFSStatus canRename() {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().canRename();
	}

	@Override
	public VFSStatus canDelete() {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().canDelete();
	}

	@Override
	public String getName() {
		if (StringHelper.containsNonWhitespace(namedContainerName)) {
			return namedContainerName;
		}
		if (getMetaInfo() == null) {
			return null;
		}
		return getMetaInfo().getFilename();
	}

	@Override
	public long getLastModified() {
		if (getMetaInfo() == null) {
			return 0l;
		}
		Date fileLastModified = getMetaInfo().getFileLastModified();
		if (fileLastModified == null) {
			return 0l;
		}
		return fileLastModified.getTime();
	}
	
	@Override
	public VFSStatus canMeta() {
		return VFSStatus.YES;
	}
	
	@Override
	public VFSStatus canVersion() {
		if (getItem() == null) {
			return VFSStatus.NO;
		}
		return getItem().canVersion();
	}

	@Override
	public VFSContainer getParentContainer() {
		return parentContainer;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}
	
	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return secCallback;
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		this.secCallback = secCallback;
	}

}
