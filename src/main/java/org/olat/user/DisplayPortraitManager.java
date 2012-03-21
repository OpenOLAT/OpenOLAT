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

import java.io.File;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;

/**
 * Description: <br>
 * TODO: alex Class Description
 * <P>
 * 
 * Initial Date: Sept 08, 2005 <br>
 * @author Alexander Schneider
 */
public class DisplayPortraitManager extends BasicManager implements UserDataDeletable {

	private static DisplayPortraitManager singleton;
	
	public static final String PORTRAIT_BIG_FILENAME = "portrait_big.jpg";
	public static final String PORTRAIT_SMALL_FILENAME = "portrait_small.jpg";
	// The following class names refer to CSS class names in olat.css 
	public static final String DUMMY_BIG_CSS_CLASS = "o_portrait_dummy";
	public static final String DUMMY_SMALL_CSS_CLASS = "o_portrait_dummy_small";
	public static final String DUMMY_FEMALE_BIG_CSS_CLASS = "o_portrait_dummy_female_big";
	public static final String DUMMY_FEMALE_SMALL_CSS_CLASS = "o_portrait_dummy_female_small";
	public static final String DUMMY_MALE_BIG_CSS_CLASS = "o_portrait_dummy_male_big";
	public static final String DUMMY_MALE_SMALL_CSS_CLASS = "o_portrait_dummy_male_small";
	
	// If you change the following widths, don't forget to change them in basemod.scss as well.
	public static final int WIDTH_PORTRAIT_BIG = 100;  // 4-8 kbytes (jpeg)
	public static final int WIDTH_PORTRAIT_SMALL = 50; // 2-4
	
	/**
	 * [spring]
	 */
	private DisplayPortraitManager(UserDeletionManager userDeletionManager) {
		userDeletionManager.registerDeletableUserData(this);
		singleton = this;
	}
	
	/**
	 * Singleton pattern
	 * 
	 * @return instance
	 */
	public static DisplayPortraitManager getInstance() {
		return singleton;
	}
	
	/**
	 * 
	 * @param identity
	 * @return imageResource portrait
	 */
	public MediaResource getPortrait(Identity identity, String portraitName){
		MediaResource imageResource = null;
		File imgFile = new File(getPortraitDir(identity), portraitName);
		if (imgFile.exists()){
			imageResource = new FileMediaResource(imgFile);	
		}
		return imageResource;
	}
	
	/**
	 * 
	 * @param identity
	 * @return imageResource portrait
	 */
	public MediaResource getPortrait(File uploadDir, String portraitName){
		MediaResource imageResource = null;
		File imgFile = new File(uploadDir, portraitName);
		if (imgFile.exists()){
			imageResource = new FileMediaResource(imgFile);	
		}
		return imageResource;
	}
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public File getPortraitDir(Identity identity){
		String portraitPath = FolderConfig.getCanonicalRoot() + 
		FolderConfig.getUserHomePage(identity.getName()) + "/portrait"; 
		File portraitDir = new File(portraitPath);
		portraitDir.mkdirs();
		return portraitDir;
	}
	
	/**
	 * Delete home-page config-file of a certain user.
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		FileUtils.deleteDirsAndFiles(getPortraitDir(identity), true, true);
		Tracing.logDebug("Homepage-config file deleted for identity=" + identity, this.getClass());
	}

}