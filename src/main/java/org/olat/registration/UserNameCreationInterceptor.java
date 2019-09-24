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

package org.olat.registration;

import java.util.Map;

/**
 * Initial Date:  5 mars 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface UserNameCreationInterceptor {
	/** 
	 * @param userAttributes a hash map that contains the already collected user attributes. This can be just 
	 * the email address (self registration) or the entire shibboleth attribute map. 
	 * @return A username or NULL if no username has been found. An empty string can also be returned 
	 * when users can change the user name and no proposition can be made. 
	 **/ 
	public String getUsernameFor(Map<String,String> userAttributes); 

	/** 
	 * @return true: users are allowed to change the propesed user name; false: the proposed username can 
	 * not be changed, the username field is not editable. 
	 **/ 
	public boolean allowChangeOfUsername(); 
}
