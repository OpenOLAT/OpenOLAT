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
package org.olat.modules.sharepoint;

import java.util.Locale;

import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 3 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointWebDAVProvider implements WebDAVProvider {
	
	private SharePointService sharePointService;

	public SharePointWebDAVProvider(SharePointService sharePointService) {
		this.sharePointService = sharePointService;
	}

	@Override
	public String getMountPoint() {
		return "sharepoint";
	}

	@Override
	public String getIconCss() {
		return "o_icon_provider_adfs";
	}

	@Override
	public String getName(Locale locale) {
		return "SharePoint";
	}

	@Override
	public boolean hasAccess(UserSession usess) {
		OAuth2Tokens tokens = usess.getOAuth2Tokens();
		return tokens != null && (StringHelper.containsNonWhitespace(tokens.getAccessToken())
				|| StringHelper.containsNonWhitespace(tokens.getRefreshToken()));
	}

	@Override
	public VFSContainer getContainer(UserSession usess) {
		return sharePointService.getSharePointContainer(usess);
	}
}
