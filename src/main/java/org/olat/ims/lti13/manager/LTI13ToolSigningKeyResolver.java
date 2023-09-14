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
import org.olat.core.util.StringHelper;
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
public class LTI13ToolSigningKeyResolver implements SigningKeyResolver {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13ToolSigningKeyResolver.class);
	
	private final LTI13Tool tool;
	private boolean withKid;
	private List<LTI13Key> foundKeys;
	
	@Autowired
	private LTI13Service lti13Service;

	public LTI13ToolSigningKeyResolver(LTI13Tool tool) {
		CoreSpringFactory.autowireObject(this);
		this.tool = tool;
	}
	
	public LTI13Tool getTool() {
		return tool;
	}
	
	public List<LTI13Key> getFoundKeys() {
		return foundKeys;
	}
	
	public boolean hasFoundMultipleKeys() {
		return foundKeys != null && foundKeys.size() > 1;
	}
	
	public boolean isWithKid() {
		return withKid;
	}

	@Override
	public Key resolveSigningKey(JwsHeader header, Claims claims) {
		try {
			if(tool.getPublicKeyTypeEnum() == PublicKeyType.KEY) {
				String publicKeyContent = tool.getPublicKey();
				return CryptoUtil.string2PublicKey(publicKeyContent);
			} else if(tool.getPublicKeyTypeEnum() == PublicKeyType.URL) {
				String kid = header.getKeyId();
				withKid = StringHelper.containsNonWhitespace(kid);
				String alg = header.getAlgorithm();
				String publicKeyUrl = tool.getPublicKeyUrl();
				List<LTI13Key> keys = lti13Service.getKeys(publicKeyUrl, alg, kid);
				if(keys.size() == 1) {
					return keys.get(0).getPublicKey();
				}
				if(keys.size() > 1) {
					foundKeys = keys;
					return keys.get(0).getPublicKey();
				}
				return null;
			}
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public Key resolveSigningKey(JwsHeader header, String plaintext) {
		log.debug("resolveSigningKey plain: {} claims: {}", header, plaintext);
		return null;
	}
}
