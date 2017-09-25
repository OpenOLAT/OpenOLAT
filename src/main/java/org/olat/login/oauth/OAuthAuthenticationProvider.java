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
package org.olat.login.oauth;

import java.util.List;

import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.IdentityEnvironment;
import org.olat.login.auth.AuthenticationProvider;

/**
 *
 * Initial date: 06.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthAuthenticationProvider extends AuthenticationProvider {

	public OAuthAuthenticationProvider(String name, String clazz, boolean isDefault, String iconCssClass) {
		super(name, clazz, true, isDefault, iconCssClass);
	}

	@Override
	public boolean accept(String subProviderName) {
		OAuthLoginModule oauthLoginModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		List<OAuthSPI> spies = oauthLoginModule.getEnableSPIs();
		for(OAuthSPI spi:spies) {
			if(spi.getProviderName().equals(subProviderName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		OAuthLoginModule oauthLoginModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		List<OAuthSPI> spies = oauthLoginModule.getEnableSPIs();
		return spies != null && spies.size() > 0 && super.isEnabled();
	}

	@Override
	public String getIssuerIdentifier(IdentityEnvironment identityEnvironment) {
		String subProviderName = identityEnvironment.getAttributes().get(AuthHelper.ATTRIBUTE_AUTHPROVIDER);
		OAuthLoginModule oauthLoginModule = CoreSpringFactory.getImpl(OAuthLoginModule.class);
		List<OAuthSPI> spies = oauthLoginModule.getEnableSPIs();
		String issuerIdentifier = null;
		for(OAuthSPI spi:spies) {
			if(spi.getProviderName().equals(subProviderName)) {
				issuerIdentifier = spi.getIssuerIdentifier();
			}
		}
		return issuerIdentifier != null? issuerIdentifier: super.getIssuerIdentifier(identityEnvironment);
	}

}
