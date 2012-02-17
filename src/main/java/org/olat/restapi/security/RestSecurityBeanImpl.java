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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for RestSecurityBeanImpl
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestSecurityBeanImpl implements RestSecurityBean {

	private Map<String,Long> tokenToIdentity = new HashMap<String,Long>();
	private Map<String,List<String>> tokenToSessionIds = new HashMap<String,List<String>>();
	private Map<String,String> sessionIdToTokens = new HashMap<String,String>();
	
	@Override
	public String generateToken(Identity identity, HttpSession session) {
		String token = UUID.randomUUID().toString();
		tokenToIdentity.put(token, identity.getKey());
		bindTokenToSession(token, session);
		return token;
	}

	@Override
	public String renewToken(String token) {
		return token;
	}

	@Override
	public boolean isTokenRegistrated(String token) {
		if(!StringHelper.containsNonWhitespace(token)) return false;
		return tokenToIdentity.containsKey(token);
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
		return BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
	}

	@Override
	public void bindTokenToSession(String token, HttpSession session) {
		if(!StringHelper.containsNonWhitespace(token) || session == null) {
			return;
		}
		
		String sessionId = session.getId();
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB
			if(!tokenToSessionIds.containsKey(token)) {
				List<String> sessionIds = new ArrayList<String>();
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
	public void invalidToken(String token) {
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB	
			tokenToIdentity.remove(token);
			List<String> sessionIds = tokenToSessionIds.remove(token);
			if(sessionIds != null) {
				for(String sessionId:sessionIds) {
					sessionIdToTokens.remove(sessionId);
				}
			}
		}
	}

	@Override
	public void unbindTokenToSession(HttpSession session) {
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB
			String sessionId = session.getId();
			String token = sessionIdToTokens.remove(sessionId);
			System.out.println("Unbin token: " + token + " -> " + sessionId);
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
}