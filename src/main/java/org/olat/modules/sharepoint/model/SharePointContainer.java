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

import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.modules.sharepoint.SharePointModule;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointContainer extends MergeSource {

	private final SharePointDAO sharePointDao;
	private final SharePointModule sharePointModule;
	private final TokenCredential tokenProvider;
	
	private final List<String> exclusionsSitesAndDrives;
	private final List<String> exclusionsLabels;
	
	public SharePointContainer(VFSContainer parentContainer, String name,
			SharePointModule sharePointModule, SharePointDAO sharePointDao,
			List<String> exclusionsSitesAndDrives, List<String> exclusionsLabels, TokenCredential tokenProvider) {
		super(parentContainer, name);
		this.sharePointDao = sharePointDao;
		this.sharePointModule = sharePointModule;
		this.tokenProvider = tokenProvider;
		this.exclusionsSitesAndDrives = exclusionsSitesAndDrives;
		this.exclusionsLabels = exclusionsLabels;
		
		setLocalSecurityCallback(new ReadOnlyCallback());
		init();
	}
	
	@Override
	protected void init() {
		String searchQuery = sharePointModule.getSitesSearch();
		if(!StringHelper.containsNonWhitespace(searchQuery)) {
			searchQuery = "frentix";
		}
		List<MicrosoftSite> sites = sharePointDao.getSites(tokenProvider, searchQuery);
		if(sites != null) {
			for(MicrosoftSite site:sites) {
				if(SharePointDAO.accept(site, exclusionsSitesAndDrives)) {
					SiteContainer siteContainer = new SiteContainer(this, site, sharePointDao,
							exclusionsSitesAndDrives, exclusionsLabels, tokenProvider);
					siteContainer.setLocalSecurityCallback(new ReadOnlyCallback());
					addContainer(siteContainer);
				}
			}
		}
	}
}
