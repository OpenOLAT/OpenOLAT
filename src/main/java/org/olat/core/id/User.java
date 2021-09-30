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

import java.util.Locale;
import java.util.Map;

/**
 * Description:
 * <p>
 * The user represents a real world user with the following elements:
 * <ul>
 * <li>profile: a list of user properties</li>
 * <li>preferences: a list of user settings</li>
 * </ul>
 * <p>
 * 
 * @author Florian Gnägi
 */
public interface User extends CreateInfo, Persistable {
	
	
	public String getFirstName();
	
	public String getLastName();
	
	public String getNickName();
	
	public String getEmail();
	
	public String getInstitutionalEmail();
	
	/**
	 * 
	 * @return The real value, don't show it on any user interface
	 */
	public String getSmsTelMobile();

	/**
	 * Get the users prefereces object
	 * 
	 * @return The users preferences object
	 */
	public Preferences getPreferences();

	/**
	 * Set the users prefereces
	 * 
	 * @param prefs The users new preferences
	 */
	public void setPreferences(Preferences prefs);

	/**
	 * Set the value for the given user property identifier
	 * 
	 * @param propertyName The user property identifyer
	 * @param propertyValue The new value or NULL if no value is used
	 */
	public void setProperty(String propertyName, String propertyValue);

	/**
	 * Get a user property value for the given property identifier. The local
	 * might be used to format the returned value if it is an internationalized
	 * value
	 * 
	 * @param propertyName The user property identifier
	 * @param locale The locale used for proper display or NULL if the default
	 *          locale should be used. In many cases it is ok to use NULL in any
	 *          case, e.g. the users firstname will not be internationalized in
	 *          anyway. Make sure you use a locale whenever you query for a date
	 *          property.
	 * @return The value or NULL if no value is set
	 */
	public String getProperty(String propertyName, Locale locale);
	
	/**
	 * Get a user property value for the given property identifier. 
	 * This method should not used in GUI views, instead use
	 * {@link #getProperty(propertyName, locale) getProperty}.
	 *
	 * @param propertyName
	 * @return
	 */
	public String getProperty(String propertyName);

	/**
	 * internal use only.
	 * @param identEnvAttribs
	 */
	public void setIdentityEnvironmentAttributes(Map<String,String> identEnvAttribs);
	/**
	 * returns the property value, which is looked up first in the db stored user properties and if not available there, if it can be found in the
	 * volatile identity attributes which get set once per session during the login process.
	 * <p>
	 * Usage so far is during Shibboleth Login (ShibbolethDispatcher), where the shibboleth attributes are extracted and set in the identity environment.
	 * 
	 * @since introducing user tracking it was needed to expose the volatile identity environments attribute also in the user for the UserActivityLogger(Impl). 
	 * @param next
	 * @param locale
	 * @return
	 */
	public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale);
	
}