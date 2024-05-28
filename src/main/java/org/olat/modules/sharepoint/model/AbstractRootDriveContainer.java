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

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.DriveItem;

/**
 * 
 * Initial date: 27 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractRootDriveContainer extends AbstractSPContainer {
	
	private boolean initialized = false;
	private MicrosoftDrive drive;
	
	private DriveItem rootItem;
	private List<VFSItem> items = new ArrayList<>();
	
	public AbstractRootDriveContainer(VFSContainer parentContainer, String name, SharePointDAO sharePointDao,
			List<String> exclusionsLabels, TokenCredential tokenProvider) {
		super(parentContainer, name, sharePointDao, exclusionsLabels, tokenProvider);
	}
	
	public abstract MicrosoftDrive getDrive();

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			items.clear();
			drive = getDrive();
			
			rootItem = sharePointDao.getRootDriveItem(drive.drive(), tokenProvider);
			List<MicrosoftDriveItem> driveItems = sharePointDao.getDriveItems(drive.drive(), rootItem, tokenProvider);
			items = toVFS(drive, driveItems);
			initialized = true;
		}
		return items;
	}

	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		List<MicrosoftDriveItem> driveItems = sharePointDao.searchDriveItems(drive.drive(), "", tokenProvider);
		return toVFS(drive, driveItems);
	}

	@Override
	public String getRelPath() {
		return getName();
	}
	
	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if(vfsItem instanceof AbstractRootDriveContainer container) {
			return drive != null && container.drive != null && drive.id().equals(container.drive.id());
		}
		return false;
	}
}
