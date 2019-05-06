/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.gotomeeting.oauth;

import org.olat.core.helpers.Settings;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.modules.gotomeeting.GoToMeetingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 14 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToProvider implements OAuthSPI {
	
	@Autowired
	private GoToMeetingModule goToMeetingModule;

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public OAuthService getScribeProvider() {
		return new ServiceBuilder(goToMeetingModule.getTrainingConsumerKey())
                .apiSecret(goToMeetingModule.getTrainingConsumerSecret())
                .callback(Settings.getServerContextPathURI() + GoToApi.GETGO_CALLBACK)
                .build(new GoToApi());
	}

	@Override
	public String getName() {
		return "GetTo";
	}

	@Override
	public boolean isRootEnabled() {
		return false;
	}

	@Override
	public String getProviderName() {
		return "GETTO";
	}

	@Override
	public String getIconCSS() {
		return "o_icon o_gotomeeting_icon";
	}


	@Override
	public boolean isImplicitWorkflow() {
		return false;
	}

	@Override
	public OAuthUser getUser(OAuthService service, Token accessToken) {
		return null;
	}

	@Override
	public String getIssuerIdentifier() {
		return "https://api.getgo.com";
	}
}
