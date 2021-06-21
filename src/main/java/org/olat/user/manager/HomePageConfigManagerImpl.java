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

package org.olat.user.manager;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial Date: Jun 3, 2005 <br>
 * @author Alexander Schneider
 */
@Service
public class HomePageConfigManagerImpl implements HomePageConfigManager {

	private static final Logger log = Tracing.createLoggerFor(HomePageConfigManagerImpl.class);
	
	private static XStream homeConfigXStream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				HomePageConfig.class
		};
		homeConfigXStream.addPermission(new ExplicitTypePermission(types));
	}

	/**
	 * 
	 * @param userName
	 * @return homePageConfig
	 */
	@Override
	public HomePageConfig loadConfigFor(Identity identity) {
		String userName = identity.getName();
		HomePageConfig retVal = null;
		File configFile = getConfigFile(userName);
		if (!configFile.exists()) {
			// config file does not exist! create one, init the defaults, save it.
			retVal = loadAndSaveDefaults(identity);
		} else {
			// file exists, load it with XStream, resolve version
			try {
				Object tmp = homeConfigXStream.fromXML(configFile);
				if (tmp instanceof HomePageConfig) {
					retVal = (HomePageConfig)tmp;
					if(retVal.resolveVersionIssues()) {
						saveConfigTo(identity, retVal);
					}
				}
			} catch (Exception e) {
				log.error("Error while loading homepage config from path::{}, fallback to default configuration",
						configFile.getAbsolutePath(), e);
				FileUtils.deleteFile(configFile);
				retVal = loadAndSaveDefaults(identity);
				// show message to user
			}
		}
		return retVal;
	}

	/**
	 * Private helper to load and create a default homepage configuration
	 * 
	 * @param userName
	 * @return
	 */
	private HomePageConfig loadAndSaveDefaults(Identity identity) {
		HomePageConfig retVal = new HomePageConfig();
		retVal.initDefaults();
		saveConfigTo(identity, retVal);
		return retVal;
	}

	/**
	 * @param userName
	 * @param homePageConfig
	 */
	@Override
	public void saveConfigTo(Identity identity, HomePageConfig homePageConfig) {
		File configFile = getConfigFile(identity.getName());
		XStreamHelper.writeObject(homeConfigXStream, configFile, homePageConfig);
	}

	/**
	 * the configuration is saved in the user home
	 * @param userName
	 * @return the configuration file
	 */
	static File getConfigFile(String userName) {
		File userHomePage = getUserHomePageDir(userName);
		return new File(userHomePage, HomePageConfigManager.HOMEPAGECONFIG_XML);
	}
	
	private static File getUserHomePageDir(String userName) {
		String pathHomePage = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(userName);
		File userHomePage = new File(pathHomePage);
		if(!userHomePage.exists()) {
			userHomePage.mkdirs();
		}
		return userHomePage;
	}

	
	@Override
	public int deleteUserDataPriority() {
		// must have lower priority than DisplayPortraitManager (otherwise portrait archive does not work)
		return 600;
	}

	/**
	 * Delete home-page config-file of a certain user.
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		String home = FolderConfig.getUserHomePage(identity);
		String pathHomePage = FolderConfig.getCanonicalRoot() + home;
		File userHomePage = new File(pathHomePage);
		if(userHomePage.exists()) {
			VFSContainer homeContainer = VFSManager.olatRootContainer(home, null);
			homeContainer.deleteSilently();
		}
		log.info(Tracing.M_AUDIT, "Homepage-config file and homepage-dir deleted for identity={}", identity);
	}
}