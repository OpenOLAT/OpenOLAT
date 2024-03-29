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
package org.olat.restapi.security;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Manage a mapping between the security token generated by
 * OpenOLAT and HTTP sessions. The security token are saved on
 * the database using the Authentication table with the specific
 * provider "REST".</br>
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("restSecurityBean")
public class RestSecurityBeanImpl implements RestSecurityBean {
	
	public static final String REST_AUTH_PROVIDER = "REST";

	private Map<String,Long> tokenToIdentity = new ConcurrentHashMap<>();
	private Map<String,List<String>> tokenToSessionIds = new ConcurrentHashMap<>();
	private Map<String,String> sessionIdToTokens = new ConcurrentHashMap<>();
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationDAO authenticationDao;

	@Override
	public String generateToken(Identity identity, HttpSession session) {
		String token = UUID.randomUUID().toString();
		tokenToIdentity.put(token, identity.getKey());
		bindTokenToSession(token, session);
		
		Authentication auth = securityManager.findAuthentication(identity, REST_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
		if(auth == null) {
			securityManager.createAndPersistAuthentication(identity, REST_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER, null,
					identity.getName(), token, null);
		} else {
			authenticationDao.updateCredential(auth, token);
		}
		return token;
	}

	@Override
	public String renewToken(String token) {
		if(token == null || token.length() > 40) {
			return null;
		}
		// don't regex, never
		for(char c:token.toCharArray()) {
			if(c == '-'
					|| (c >= 48 && c <= 57)
					|| (c >= 65 && c <= 90)
					|| (c >= 97 && c <= 122)) {
				continue;
			}
			return null;
		}
		return token;
	}

	@Override
	public boolean isTokenRegistrated(String token, HttpSession session) {
		if(!StringHelper.containsNonWhitespace(token)) return false;
		boolean registrated = tokenToIdentity.containsKey(token);
		if(!registrated) {
			List<Authentication> auths = securityManager.findAuthenticationByToken(REST_AUTH_PROVIDER, token);
			if(auths.size() == 1) {
				Authentication auth = auths.get(0);
				tokenToIdentity.put(token, auth.getIdentity().getKey());
				bindTokenToSession(token, session);
				registrated = true;
			}
		}
		return registrated;
	}
	
	@Override
	public Identity getIdentity(String token) {
		if(!StringHelper.containsNonWhitespace(token)) {
			return null;
		}
		Long identityKey = tokenToIdentity.get(token);
		if(identityKey == null) {
			return null;
		}
		return securityManager.loadIdentityByKey(identityKey, false);
	}

	@Override
	public void bindTokenToSession(String token, HttpSession session) {
		if(!StringHelper.containsNonWhitespace(token) || session == null) {
			return;
		}
		
		String sessionId = session.getId();
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB
			if(!tokenToSessionIds.containsKey(token)) {
				List<String> sessionIds = new ArrayList<>();
				sessionIds.add(session.getId());
				tokenToSessionIds.put(token, sessionIds);
			} else {
				List<String> sessionIds = tokenToSessionIds.get(token);
				if(!sessionIds.contains(sessionId)) {
					sessionIds.add(session.getId());
				}
			}
			sessionIdToTokens.put(sessionId, token);
		}
	}

	@Override
	public void unbindTokenToSession(HttpSession session) {
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB
			String sessionId = session.getId();
			String token = sessionIdToTokens.remove(sessionId);
			if(token != null) {
				List<String> sessionIds = tokenToSessionIds.get(token);
				sessionIds.remove(sessionId);
				if(sessionIds.isEmpty()) {
					tokenToIdentity.remove(token);
					tokenToSessionIds.remove(token);
				}
			}
		}
	}

	@Override
	public int removeTooOldRestToken() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, -1);
		Date limit = cal.getTime();

		List<Authentication> authentications = securityManager.findOldAuthentication(REST_AUTH_PROVIDER, limit);
		for(Authentication authentication:authentications) {
			String token = authentication.getCredential();
			if(tokenToIdentity.containsKey(token)) {
				continue;//don't delete authentication in use
			}
			securityManager.deleteAuthentication(authentication);
		}
		return authentications.size();
	}

	public void clearCaches() {
		tokenToIdentity.clear();
		tokenToSessionIds.clear();
		sessionIdToTokens.clear();
	}
}