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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * Description: <BR/>
 * 
 * @author Felix Jost
 */
public interface Authentication extends CreateInfo, ModifiedInfo, Persistable {

	/**
	 * @return
	 */
	public Identity getIdentity();

	/**
	 * @return
	 */
	public String getProvider();
	
	public String getIssuer();

	/**
	 * @return
	 */
	public String getAuthusername();

	/**
	 * @return
	 */
	public String getCredential();
	
	/**
	 * Salt used to hash the password
	 * @return
	 */
	public String getSalt();
	
	/**
	 * Algoritm used to hash the password
	 * @return
	 */
	public String getAlgorithm();

	/**
	 * @param identity
	 */
	public void setIdentity(Identity identity);

	/**
	 * @param provider
	 */
	public void setProvider(String provider);

	/**
	 * @param authusername
	 */
	public void setAuthusername(String authusername);

	/**
	 * @param credential
	 */
	public void setCredential(String credential);
	
	/**
	 * 
	 * @param salt
	 */
	public void setSalt(String salt);
	
	/**
	 * 
	 * @param algorithm
	 */
	public void setAlgorithm(String algorithm);

}

