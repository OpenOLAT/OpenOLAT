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
package org.olat.login.oauth.spi;

import java.util.UUID;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 17 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenericOAuth2Api extends DefaultApi20 {
	
	private final GenericOAuth2Provider provider;

	public GenericOAuth2Api(GenericOAuth2Provider provider) {
		this.provider = provider;
	}

    @Override
    public String getAccessTokenEndpoint() {
        return provider.getTokenBaseUrl();
    }
    
    @Override
	public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
    	if(provider.hasScope("openid")) {
    		return OpenIdJsonTokenExtractor.instance();
    	}
    	return OAuth2AccessTokenJsonExtractor.instance();
	}

    @Override
    public String getAuthorizationBaseUrl() {
    	StringBuilder authorizeUrl = new StringBuilder();
    	authorizeUrl
    		.append(provider.getAuthorizationBaseUrl())
    		.append("?&nonce=").append(UUID.randomUUID().toString());		
    	return authorizeUrl.toString();
    }
}

