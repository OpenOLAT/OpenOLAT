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

	private Map<String,Identity> tokenToIdentity = new HashMap<String,Identity>();
	private Map<String,List<String>> tokenToSessionIds = new HashMap<String,List<String>>();
	private Map<String,String> sessionIdToTokens = new HashMap<String,String>();
	
	@Override
	public String generateToken(Identity identity) {
		String token = UUID.randomUUID().toString();
		tokenToIdentity.put(token, identity);
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
		if(!StringHelper.containsNonWhitespace(token)) return null;
		return tokenToIdentity.get(token);
	}

	@Override
	public void bindTokenToSession(String token, HttpSession session) {
		if(!StringHelper.containsNonWhitespace(token)) return;
		
		synchronized(tokenToSessionIds) {//cluster notOK -> need probably a mapping on the DB
			List<String> sessionIds;
			if(!tokenToSessionIds.containsKey(token)) {
				sessionIds = new ArrayList<String>();
				tokenToSessionIds.put(token, sessionIds);
			} else {
				sessionIds = tokenToSessionIds.get(token);
			}
			sessionIds.add(session.getId());
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
			String token = sessionIdToTokens.get(session.getId());
			List<String> sessionIds = tokenToSessionIds.get(token);
			sessionIds.remove(session.getId());
			if(sessionIds.isEmpty()) {
				tokenToIdentity.remove(token);
			}
		}
	}
}