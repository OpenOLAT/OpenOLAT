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
public class SiteContainer extends AbstractSPContainer {
	
	private final MicrosoftSite site;
	
	private boolean initialized = false;
	private List<VFSItem> drives = new ArrayList<>();
	private final List<String> exclusionsSitesAndDrives;
	private List<MicrosoftDrive> microsoftDrives = new ArrayList<>();
	
	public SiteContainer(SharePointContainer parentContainer, MicrosoftSite site,
			SharePointDAO sharePointDao, List<String> exclusionsSitesAndDrives,
			List<String> exclusionsLabels, TokenCredential tokenProvider) {
		super(parentContainer, site.name(), sharePointDao, exclusionsLabels, tokenProvider);
		this.site = site;
		this.exclusionsSitesAndDrives = exclusionsSitesAndDrives;
	}
	
	@Override
	public String getIconCSS() {
		return "o_icon_sharepoint_drive";
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			drives.clear();
			
			List<MicrosoftDrive> mDrives = sharePointDao.getDrives(site.id(), tokenProvider);
			if(mDrives != null) {
				for(MicrosoftDrive mDrive:mDrives) {
					if(SharePointDAO.accept(mDrive, exclusionsSitesAndDrives)) {
						drives.add(new SiteDriveContainer(this, mDrive, sharePointDao, exclusionsLabels, tokenProvider));
					}
				}
			}
			microsoftDrives = mDrives;
			initialized = true;
		}
		return drives;
	}
	
	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		List<VFSItem> foundItems = new ArrayList<>();
		for(MicrosoftDrive drive:microsoftDrives) {
			List<MicrosoftDriveItem> driveItems = sharePointDao.searchDriveItems(drive.drive(), "", tokenProvider);
			foundItems.addAll(toVFS(drive, driveItems));
		}
		return foundItems;
	}

	@Override
	public String getRelPath() {
		return site.name();
	}
	
	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if(vfsItem instanceof SiteContainer siteContainer) {
			return this == siteContainer;
		}
		return false;
	}
}
