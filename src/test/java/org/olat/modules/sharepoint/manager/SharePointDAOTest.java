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

import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.basesecurity.model.OAuth2TokensImpl;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.modules.sharepoint.model.MicrosoftDrive;
import org.olat.modules.sharepoint.model.MicrosoftDriveItem;
import org.olat.modules.sharepoint.model.MicrosoftSite;
import org.olat.modules.teams.manager.OAuth2TokenCredential;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.DriveItem;

/**
 * 
 * Initial date: 27 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointDAOTest extends OlatTestCase {
	
	@Autowired
	private SharePointDAO sharePointDao;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	
	@Test
	public void getMeOneDrive() throws Exception {
		String refreshToken = System.getProperty("test.env.azure.adfs.refresh.token");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(oauthLoginModule.getAzureAdfsApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(refreshToken));
		
		OAuth2TokensImpl oauth2Tokens = new OAuth2TokensImpl();
		oauth2Tokens.setRefreshToken(refreshToken);
		TokenCredential credential = new OAuth2TokenCredential(oauth2Tokens);
		
		MicrosoftDrive oneDrive = sharePointDao.getMeOneDrive(credential);
		Assert.assertNotNull(oneDrive);
		Assert.assertNotNull(oneDrive.drive());
		Assert.assertNotNull(oneDrive.drive().getId());
		Assert.assertNotNull("OneDrive", oneDrive.drive().getName());
	}
	
	@Test
	public void getRootDriveItem() throws Exception {
		String refreshToken = System.getProperty("test.env.azure.adfs.refresh.token");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(oauthLoginModule.getAzureAdfsApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(refreshToken));
		
		OAuth2TokensImpl oauth2Tokens = new OAuth2TokensImpl();
		oauth2Tokens.setRefreshToken(refreshToken);
		TokenCredential credential = new OAuth2TokenCredential(oauth2Tokens);
		
		MicrosoftDrive oneDrive = sharePointDao.getMeOneDrive(credential);
		Assert.assertNotNull(oneDrive);
		
		DriveItem oneDriveRootItem = sharePointDao.getRootDriveItem(oneDrive.drive(), credential);
		Assert.assertNotNull(oneDriveRootItem);
		Assert.assertNotNull(oneDriveRootItem.getId());
		Assert.assertNotNull(oneDriveRootItem.getName());
	}
	
	@Test
	public void getDriveItems() throws Exception {
		String refreshToken = System.getProperty("test.env.azure.adfs.refresh.token");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(oauthLoginModule.getAzureAdfsApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(refreshToken));
		
		OAuth2TokensImpl oauth2Tokens = new OAuth2TokensImpl();
		oauth2Tokens.setRefreshToken(refreshToken);
		TokenCredential credential = new OAuth2TokenCredential(oauth2Tokens);
		
		MicrosoftDrive oneDrive = sharePointDao.getMeOneDrive(credential);
		Assert.assertNotNull(oneDrive);
		
		DriveItem oneDriveRootItem = sharePointDao.getRootDriveItem(oneDrive.drive(), credential);
		Assert.assertNotNull(oneDriveRootItem);
		
		List<MicrosoftDriveItem> items = sharePointDao.getDriveItems(oneDrive.drive(), oneDriveRootItem, credential);
		Assert.assertNotNull(items);
	}
	
	@Test
	public void getSites() throws Exception {
		String refreshToken = System.getProperty("test.env.azure.adfs.refresh.token");
		Assume.assumeTrue(StringHelper.containsNonWhitespace(oauthLoginModule.getAzureAdfsApiKey()));
		Assume.assumeTrue(StringHelper.containsNonWhitespace(refreshToken));
		
		OAuth2TokensImpl oauth2Tokens = new OAuth2TokensImpl();
		oauth2Tokens.setRefreshToken(refreshToken);
		TokenCredential credential = new OAuth2TokenCredential(oauth2Tokens);
		
		List<MicrosoftSite> sites = sharePointDao.getSites(credential, "frentix");
		Assert.assertNotNull(sites);
		// Normally there is always some sites available
		Assert.assertFalse(sites.isEmpty());
	}
}
