/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.security;

import javax.servlet.http.HttpSession;

import org.olat.core.id.Identity;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for RestSecurityBean
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface RestSecurityBean {
	
	public String generateToken(Identity identity);
	
	public boolean isTokenRegistrated(String token);
	
	public Identity getIdentity(String token);
	
	/**
	 * Bind a token with the session
	 * @param token
	 * @param session
	 */
	public void bindTokenToSession(String token, HttpSession session);
	
	/**
	 * Remove the token from the specified session
	 * @param session
	 */
	public void unbindTokenToSession(HttpSession session);
	
	/**
	 * Some implementations want perhaps trigger new token regularly
	 * @param token
	 * @return
	 */
	public String renewToken(String token);
	
	/**
	 * Force invalidation of the token
	 * @param token
	 */
	public void invalidToken(String token);
}
