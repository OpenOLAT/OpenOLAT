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

package org.olat.user;

import org.olat.core.id.Identity;

/**
 * Description: <br>
 * Displays/Edit information a user wants do show to other users
 * <P>
 * 
 * Initial Date: July 15, 2005 <br>
 * @author Alexander Schneider
 */
public interface HomePageConfigManager extends UserDataDeletable {
	/**
	 * the filename used for saving the configuration information
	 */
	public static final String HOMEPAGECONFIG_XML = "HomePageConfig.xml";

	/**
	 * Load the configuration of homepage. If the configuration is not existing, a
	 * new default homepage configuration is created, initialized with default
	 * values and persisted. Otherwise the configuration is loaded from the file
	 * system. If the <code>HomePageConfig</code>'s version does not match the
	 * loaded configuration version a procedure for converting/migrating to the
	 * new version is initiated.
	 * 
	 * @param userName
	 * @return configuration for a homepage
	 */
	public HomePageConfig loadConfigFor(Identity identity);

	/**
	 * saves the configuration for the given user
	 * 
	 * @param userName
	 * @param homePageConfig
	 */
	public void saveConfigTo(Identity identity, HomePageConfig homePageConfig);

}