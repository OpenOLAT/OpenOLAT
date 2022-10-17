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

import jakarta.servlet.http.HttpSession;

import org.olat.core.id.Identity;

/**
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface RestSecurityBean {
	
	/**
	 * The token is saved in the authentication table
	 * 
	 * @param identity
	 * @param session
	 * @return The security token
	 */
	public String generateToken(Identity identity, HttpSession session);
	
	public boolean isTokenRegistrated(String token, HttpSession session);
	
	public Identity getIdentity(String token);
	
	/**
	 * Bind the token to the specified session
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
	 * 
	 */
	public int removeTooOldRestToken();
}
