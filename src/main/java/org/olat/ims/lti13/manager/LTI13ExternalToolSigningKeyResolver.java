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
package org.olat.ims.lti13.manager;

import java.security.Key;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.crypto.CryptoUtil;
import org.olat.ims.lti13.LTI13Key;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.springframework.beans.factory.annotation.Autowired;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;

/**
 * 
 * Initial date: 9 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ExternalToolSigningKeyResolver implements SigningKeyResolver {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13ExternalToolSigningKeyResolver.class);
	
	private LTI13Tool tool;
	
	@Autowired
	private LTI13Service lti13Service;

	public LTI13ExternalToolSigningKeyResolver() {
		CoreSpringFactory.autowireObject(this);
	}
	
	public LTI13Tool getTool() {
		return tool;
	}

	@Override
	public Key resolveSigningKey(@SuppressWarnings("rawtypes") JwsHeader header, Claims claims) {
		try {
			String clientId = claims.get("sub", String.class);
			String iss = claims.get("iss", String.class);
			if(iss != null && iss.equals(clientId)) {
				List<LTI13Tool> tools = lti13Service.getToolsByClientId(clientId);
				if(tools.size() == 1) {
					tool = tools.get(0);
				}
			}
			if(tool == null) {
				tool = lti13Service.getToolBy(iss, clientId);
			}
			if(tool != null) {
				if(tool.getPublicKeyTypeEnum() == PublicKeyType.KEY) {
					String publicKeyContent = tool.getPublicKey();
					return CryptoUtil.string2PublicKey(publicKeyContent);
				} else if(tool.getPublicKeyTypeEnum() == PublicKeyType.URL) {
					String kid = header.getKeyId();
					String publicKeyUrl = tool.getPublicKeyUrl();
					LTI13Key key = lti13Service.getKey(publicKeyUrl, kid);
					return key.getPublicKey();
				}
			}
			
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public Key resolveSigningKey(@SuppressWarnings("rawtypes") JwsHeader header, String plaintext) {
		log.debug("resolveSigningKey plain: {} claims: {}", header, plaintext);
		return null;
	}

}
