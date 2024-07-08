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
package org.olat.modules.sharepoint.manager;

import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.modules.sharepoint.SharePointModule;
import org.olat.modules.sharepoint.SharePointService;
import org.olat.modules.sharepoint.model.OneDriveContainer;
import org.olat.modules.sharepoint.model.SharePointContainer;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.core.credential.TokenCredential;

/**
 * 
 * Initial date: 27 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("sharePointService")
public class SharePointServiceImpl implements SharePointService {
	
	@Autowired
	private SharePointDAO sharePointDao;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	@Autowired
	private SharePointModule sharePointModule;

	@Override
	public VFSContainer getSharePointContainer(UserSession usess) {
		if(!sharePointModule.isEnabled() || !sharePointModule.isSitesEnabled()) return null;
		
		OAuth2Tokens tokens = usess.getOAuth2Tokens();
		TokenCredential tokenProvider = microsoftGraphDao.getTokenProvider(tokens);
		return new SharePointContainer(null, "SharePoint", sharePointModule, sharePointDao, tokenProvider);
	}

	@Override
	public VFSContainer getOneDriveContainer(UserSession usess) {
		if(!sharePointModule.isEnabled() || !sharePointModule.isOneDriveEnabled()) return null;
		
		OAuth2Tokens tokens = usess.getOAuth2Tokens();
		TokenCredential tokenProvider = microsoftGraphDao.getTokenProvider(tokens);
		
		OneDriveContainer oneDrive = new OneDriveContainer(sharePointDao, sharePointModule.getExcludeLabels(), tokenProvider);
		oneDrive.setLocalSecurityCallback(new FullAccessCallback());
		return oneDrive;
	}
}
