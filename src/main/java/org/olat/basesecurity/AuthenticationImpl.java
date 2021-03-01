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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.basesecurity;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class AuthenticationImpl extends PersistentObject implements Authentication {

	private static final long serialVersionUID = 7969409958077836798L;
	private Date lastModified;
	private Identity identity;
	private String provider;
	private String issuer;
	private String authusername;
	private String credential;
	private String salt;
	private String algorithm;

	/**
	 * for hibernate only
	 */
	protected AuthenticationImpl() {
	//  
	}
	
	AuthenticationImpl(Identity identity, String provider, String issuer, String authusername, String credentials) {
		
		if (provider.length() > 8) {
			// this implementation allows only 8 characters, as defined in hibernate file
			throw new AssertException("Authentication provider '" + provider + "' to long, only 8 characters supported!");
		}
		this.identity = identity;
		this.provider = provider;
		this.issuer = issuer;
		this.authusername = authusername;
		this.credential = credentials;
	}

	AuthenticationImpl(Identity identity, String provider, String issuer,
			String authusername, String credential, String salt, String algorithm) {
		
		if (provider.length() > 8) {
			// this implementation allows only 8 characters, as defined in hibernate file
			throw new AssertException("Authentication provider '" + provider + "' to long, only 8 characters supported!");
		}
		this.identity = identity;
		this.provider = provider;
		this.issuer = issuer;
		this.authusername = authusername;
		this.credential = credential;
		this.salt = salt;
		this.algorithm = algorithm;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return
	 */
	@Override
	public String getAuthusername() {
		return authusername;
	}

	/**
	 * @return
	 */
	@Override
	public String getProvider() {
		return provider;
	}

	/**
	 * for hibernate only (can never be changed, but set only)
	 * 
	 * @param string
	 */
	@Override
	public void setAuthusername(String string) {
		authusername = string;
	}

	/**
	 * for hibernate only (can never be changed, but set only)
	 * 
	 * @param string
	 */
	@Override
	public void setProvider(String string) {
		provider = string;
	}
	
	@Override
	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * for hibernate only
	 * 
	 * @return
	 */
	@Override
	public String getCredential() {
		return credential;
	}

	/**
	 * @param string
	 */
	@Override
	public void setCredential(String string) {
		credential = string;
	}

	@Override
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public String getAlgorithm() {
		return algorithm;
	}

	@Override
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "auth: provider:" + provider + " ,authusername:" + authusername + ", hashpwd:" + credential + " ," + super.toString();
	}

	/**
	 * @see org.olat.basesecurity.Authentication#getIdentity()
	 */
	@Override
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @see org.olat.basesecurity.Authentication#setIdentity(org.olat.core.id.Identity)
	 */
	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 20818 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Authentication) {
			Authentication auth = (Authentication)obj;
			return getKey() != null && getKey().equals(auth.getKey());
		}
		return false;
	}
}