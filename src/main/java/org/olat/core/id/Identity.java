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
* <p>
*/ 

package org.olat.core.id;

import org.olat.basesecurity.IdentityRef;


/**
 * Initial Date:  24.04.2004
 *
 * @author Mike Stock
 */
public interface Identity extends CreateInfo, IdentityRef, IdentityLifecycle, Persistable {

	// status = 1..99    User with this status are visible (e.g. user search)
	//          100..199 User with this status are invisible (e.g. user search)
	/** Identity has a permanent olat user account and will be never listen in user-deletion process. */
	public static final Integer STATUS_PERMANENT = 1;
	/** Identity has access to olat-system. */ 
	public static final Integer STATUS_ACTIV = 2;
	/** Limit for visible identities, all identities with status < LIMIT will be listed in search etc. */
	public static final Integer STATUS_VISIBLE_LIMIT = 100;
	/** Identity can not login and will not be listed (only on login-denied list). */
	public static final Integer STATUS_LOGIN_DENIED = 101;
	/** Identity can not login and will not be listed (only on pending list). */
	public static final Integer STATUS_PENDING = 102;
	/** Identity can not BE SEARCHED. */
	public static final Integer STATUS_INACTIVE = 103;
	/** Identity is deleted and has no access to olat-system and is not visible (except administrators). */
	public static final Integer STATUS_DELETED   = 199;
	
    /**
     * @return The username, (login name, nickname..)
     */
   public String getName();
   
   /**
    * @return Reference to an identifier in an external system
    */
   public String getExternalId();

    /**
	 * @return The user object associated with this identity. The user
	 * encapsulates the user data (profile and preferences)
	 */
	public User getUser();
	
}