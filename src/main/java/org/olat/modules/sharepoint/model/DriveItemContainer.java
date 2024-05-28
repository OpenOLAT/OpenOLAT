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

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DriveItemContainer extends AbstractSPContainer {
	
	private final MicrosoftDrive drive;
	private final MicrosoftDriveItem driveItem;
	
	private boolean initialized = false;
	private final List<VFSItem> items = new ArrayList<>();
	
	public DriveItemContainer(VFSContainer parentContainer,
			MicrosoftDrive drive, MicrosoftDriveItem driveItem,
			SharePointDAO sharePointDao, List<String> exclusionsLabels,
			TokenCredential tokenProvider) {
		super(parentContainer, driveItem.name(), sharePointDao, exclusionsLabels, tokenProvider);
		this.drive = drive;
		this.driveItem = driveItem;
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
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
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
}
