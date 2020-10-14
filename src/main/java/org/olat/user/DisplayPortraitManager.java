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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date: Sept 08, 2005 <br>
 * @author Alexander Schneider
 */
public class DisplayPortraitManager implements UserDataDeletable, UserDataExportable {
	private static final Logger log = Tracing.createLoggerFor(DisplayPortraitManager.class);
	
	private static final String LOGO_PREFIX_FILENAME = "logo";
	private static final String LOGO_BIG_FILENAME = LOGO_PREFIX_FILENAME + "_big";
	private static final String LOGO_SMALL_FILENAME = LOGO_PREFIX_FILENAME + "_small";
	private static final String LOGO_MASTER_FILENAME = LOGO_PREFIX_FILENAME + "_master";

	private static final String PORTRAIT_PREFIX_FILENAME = "portrait";
	private static final String PORTRAIT_BIG_FILENAME = PORTRAIT_PREFIX_FILENAME + "_big";
	private static final String PORTRAIT_SMALL_FILENAME = PORTRAIT_PREFIX_FILENAME + "_small";
	private static final String PORTRAIT_MASTER_FILENAME = PORTRAIT_PREFIX_FILENAME + "_master";
	// The following class names refer to CSS class names in olat.css 
	public static final String AVATAR_BIG_CSS_CLASS = "o_portrait_avatar";
	public static final String AVATAR_SMALL_CSS_CLASS = "o_portrait_avatar_small";
	public static final String LOGO_BIG_CSS_CLASS = "o_portrait_logo";
	public static final String LOGO_SMALL_CSS_CLASS = "o_portrait_logo_small";
	public static final String ANONYMOUS_BIG_CSS_CLASS = "o_portrait_anonymous";
	public static final String ANONYMOUS_SMALL_CSS_CLASS = "o_portrait_anonymous_small";
	public static final String DUMMY_BIG_CSS_CLASS = "o_portrait_dummy";
	public static final String DUMMY_SMALL_CSS_CLASS = "o_portrait_dummy_small";
	public static final String DUMMY_FEMALE_BIG_CSS_CLASS = "o_portrait_dummy_female_big";
	public static final String DUMMY_FEMALE_SMALL_CSS_CLASS = "o_portrait_dummy_female_small";
	public static final String DUMMY_MALE_BIG_CSS_CLASS = "o_portrait_dummy_male_big";
	public static final String DUMMY_MALE_SMALL_CSS_CLASS = "o_portrait_dummy_male_small";
	
	public static final int HEIGHT_BIG = 100;  // 4-8 kbytes (jpeg)
	public static final int HEIGHT_SMALL = 30; // 2-4
	
	// If you change the following widths, don't forget to change them in basemod.scss as well.
	public static final int WIDTH_PORTRAIT_BIG = HEIGHT_BIG;  // 4-8 kbytes (jpeg)
	public static final int WIDTH_PORTRAIT_SMALL = HEIGHT_SMALL; // 2-4
	
	public static final int WIDTH_LOGO_BIG = HEIGHT_BIG * 4;  // 4-8 kbytes (jpeg)
	public static final int WIDTH_LOGO_SMALL = HEIGHT_SMALL * 4; // 2-4
	
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public MediaResource getSmallPortraitResource(String username) {
		return getPortraitResource(username, PORTRAIT_SMALL_FILENAME);
	}
	public MediaResource getSmallPortraitResource(Long identityKey) {
		return getPortraitResource(identityKey, PORTRAIT_SMALL_FILENAME);
	}
	
	public MediaResource getBigPortraitResource(String string) {
		return getPortraitResource(string, PORTRAIT_BIG_FILENAME);
	}
	public MediaResource getBigPortraitResource(Long identityKey) {
		return getPortraitResource(identityKey, PORTRAIT_BIG_FILENAME);
	}
	
	public MediaResource getMasterPortraitResource(String string) {
		return getPortraitResource(string, PORTRAIT_MASTER_FILENAME);
	}
	public MediaResource getMasterPortraitResource(Long identityKey) {
		return getPortraitResource(identityKey, PORTRAIT_MASTER_FILENAME);
	}
	
	public MediaResource getSmallLogoResource(String username) {
		return getPortraitResource(username, LOGO_SMALL_FILENAME);
	}
	public MediaResource getSmallLogoResource(Long identityKey) {
		return getPortraitResource(identityKey, LOGO_SMALL_FILENAME);
	}
	
	public MediaResource getBigLogoResource(String username) {
		return getPortraitResource(username, LOGO_BIG_FILENAME);
	}
	public MediaResource getBigLogoResource(Long identityKey) {
		return getPortraitResource(identityKey, LOGO_BIG_FILENAME);
	}
	
	/**
	 * Get the portrait media resource by identity name (username)
	 * @param identity
	 * @return imageResource portrait
	 */
	private MediaResource getPortraitResource(String username, String portraitName) {
		MediaResource imageResource = null;
		File imgFile = getPortraitFile(username, portraitName);
		if (imgFile != null && imgFile.exists()){
			imageResource = new FileMediaResource(imgFile);	
		}
		return imageResource;
	}

	/**
	 * Alternate method to get the portrait resource by identity key. 
	 * @param identityKey
	 * @param portraitName
	 * @return
	 */
	private MediaResource getPortraitResource(Long identityKey, String portraitName) {
		Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
		if (identity != null) {			
			return getPortraitResource(identity.getName(), portraitName);
		}
		return null;
	}

	public File getSmallPortrait(Identity identity) {
		return getPortraitFile(identity, PORTRAIT_SMALL_FILENAME);
	}
	
	public File getBigPortrait(Identity identity) {
		return getPortraitFile(identity, PORTRAIT_BIG_FILENAME);
	}
	
	public File getMasterPortrait(Identity identity) {
		return getPortraitFile(identity, PORTRAIT_MASTER_FILENAME);
	}
	
	public VFSLeaf getLargestVFSPortrait(Identity username) {
		VFSLeaf portrait = getPortraitLeaf(username, PORTRAIT_MASTER_FILENAME);
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitLeaf(username, PORTRAIT_BIG_FILENAME);
		}
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitLeaf(username, PORTRAIT_SMALL_FILENAME);
		}
		return portrait;
	}
	
	public File getLargestPortrait(Identity identity) {
		File portrait = getPortraitFile(identity, PORTRAIT_MASTER_FILENAME);
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitFile(identity, PORTRAIT_BIG_FILENAME);
		}
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitFile(identity, PORTRAIT_SMALL_FILENAME);
		}
		return portrait;
	}
	
	public File getSmallLogo(Identity identity) {
		return getPortraitFile(identity, LOGO_SMALL_FILENAME);
	}
	
	public File getBigLogo(Identity identity) {
		return getPortraitFile(identity, LOGO_BIG_FILENAME);
	}
	
	public File getLargestLogo(Identity identity) {
		File portrait = getPortraitFile(identity, LOGO_MASTER_FILENAME);
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitFile(identity, LOGO_BIG_FILENAME);
		}
		if(portrait == null || !portrait.exists()) {
			portrait = getPortraitFile(identity, LOGO_SMALL_FILENAME);
		}
		return portrait;
	}
	
	public boolean hasPortrait(Identity identity) {
		File portraitDir = getPortraitDir(identity, false);
		if(portraitDir != null && portraitDir.exists()) {
			File[] portraits = portraitDir.listFiles();
			if(portraits.length > 0) {
				for(File file:portraits) {
					if(file.getName().startsWith(PORTRAIT_PREFIX_FILENAME)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private File getPortraitFile(Identity identity, String prefix) {
		return getPortraitFile(identity.getName(), prefix);
	}

	private File getPortraitFile(String username, String prefix) {
		File portraitDir = getPortraitDir(username, true);
		if(portraitDir != null) {
			File[] portraits = portraitDir.listFiles();
			if(portraits.length > 0) {
				for(File file:portraits) {
					if(file.getName().startsWith(prefix)) {
						return file;
					}
				}
			}
		}
		return null;
	}
	
	private VFSLeaf getPortraitLeaf(Identity identity, String prefix) {
		return getPortraitLeaf(identity.getName(), prefix);
	}
	
	private VFSLeaf getPortraitLeaf(String username, String prefix) {
		VFSContainer portraitDir = getPortraitFolder(username);
		if(portraitDir != null) {
			List<VFSItem> portraits = portraitDir.getItems();
			for(VFSItem file:portraits) {
				if(file.getName().startsWith(prefix) && file instanceof VFSLeaf) {
					return (VFSLeaf)file;
				}
			}
		}
		return null;
	}
	
	public void setPortrait(File file, String filename, Identity identity) {
		setImage(file, filename, identity, PORTRAIT_PREFIX_FILENAME,
				PORTRAIT_MASTER_FILENAME, PORTRAIT_BIG_FILENAME, PORTRAIT_SMALL_FILENAME,
				WIDTH_PORTRAIT_BIG, WIDTH_PORTRAIT_SMALL);
	}
	
	public void setLogo(File file, String filename, Identity identity) {
		setImage(file, filename, identity, LOGO_PREFIX_FILENAME,
				LOGO_MASTER_FILENAME, LOGO_BIG_FILENAME, LOGO_SMALL_FILENAME,
				WIDTH_LOGO_BIG, WIDTH_LOGO_SMALL);
	}

	private void setImage(File file, String filename, Identity identity, String prefix,
			String masterImagePrefix, String largeImagePrefix, String smallImagePrefix,
			int maxBigWidth, int maxSmallWidth) {
		File directory = getPortraitDir(identity, true);
		if(directory != null) {
			for(File currentImage:directory.listFiles()) {
				if(currentImage.equals(file)) {
					// do nothing
				} else if(currentImage.getName().startsWith(prefix)) {
					FileUtils.deleteFile(currentImage);
				}
			}
		}
		
		String extension = FileUtils.getFileSuffix(file.getName());
		if(!StringHelper.containsNonWhitespace(extension)) {
			if(StringHelper.containsNonWhitespace(filename)) {
				extension = FileUtils.getFileSuffix(filename);
			}
			if(!StringHelper.containsNonWhitespace(extension)) {
				extension = "png";
			}
		}

		try {
			File masterFile = new File(directory, masterImagePrefix + "." + extension);
			Files.copy(file.toPath(), masterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("", e);
		}
		
		File bigFile = new File(directory, largeImagePrefix + "." + extension);
		File smallFile = new File(directory, smallImagePrefix + "." + extension);
		ImageService imageHelper = CoreSpringFactory.getImpl(ImageService.class);
		Size size = imageHelper.scaleImage(file, extension, bigFile, maxBigWidth, HEIGHT_BIG , false);
		if(size != null) {
			imageHelper.scaleImage(file, extension, smallFile, maxSmallWidth, HEIGHT_SMALL, false);
		}
		
		VFSLeaf vfsPortrait = getLargestVFSPortrait(identity);
		if(vfsPortrait.canMeta() == VFSConstants.YES) {
			vfsRepositoryService.resetThumbnails(vfsPortrait);
		}
	}

	public void deletePortrait(Identity identity) {
		deleteImages(identity, PORTRAIT_PREFIX_FILENAME);
	}
	
	public void deleteLogo(Identity identity) {
		deleteImages(identity, LOGO_PREFIX_FILENAME);
	}
	
	private void deleteImages(Identity identity, String prefix) {
		File directory = getPortraitDir(identity.getName(), false);
		if(directory != null && directory.exists()) {
			for(File file:directory.listFiles()) {
				String filename = file.getName();
				if(filename.startsWith(prefix)) {
					FileUtils.deleteFile(file);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param identity
	 * @return imageResource portrait
	 */
	public MediaResource getPortrait(File uploadDir, String portraitName){
		FileMediaResource imageResource = null;
		File imgFile = new File(uploadDir, portraitName);
		if (imgFile.exists()){
			imageResource = new FileMediaResource(imgFile);
			imageResource.setCacheControlDuration(ServletUtil.CACHE_ONE_DAY);
		}
		return imageResource;
	}
	
	public File getPortraitDir(Identity identity, boolean create) {
		return getPortraitDir(identity.getName(), create);
	}
	
	/**
	 * 
	 * @param identity
	 * @return
	 */
	public File getPortraitDir(String identityName, boolean create) {
		String portraitPath = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(identityName); 
		File portraitDir = new File(portraitPath, "portrait");
		if(create) {
			portraitDir.mkdirs();
		}
		return portraitDir;
	}
	
	public LocalFolderImpl getPortraitFolder(String identityName) {
		LocalFolderImpl folder = VFSManager.olatRootContainer(FolderConfig.getUserHomePage(identityName) + "/portrait", null); 
		if(!folder.exists()) {
			folder.getBasefile().mkdirs();
		}
		return folder;
	}

	@Override
	public int deleteUserDataPriority() {
		// must have higher priority than HomePageConfigManager
		return 650;
	}
		
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		File portraitDir = getPortraitDir(identity, false);
		if(portraitDir.exists()) {
			FileUtils.deleteDirsAndFiles(portraitDir, true, true);
		}
		log.debug("Homepage-config file deleted for identity={}", identity.getKey());
	}
	@Override
	public String getExporterID() {
		return "display.portrait";
	}
	
	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File portraitDir = getPortraitDir(identity.getName(), false);
		if(portraitDir.exists()) {
			File archivePortrait = new File(archiveDirectory, "portrait");
			File[] portraits = portraitDir.listFiles(new SystemFilenameFilter(true, false));
			for(File portrait:portraits) {
				manifest.appendFile("portrait/" + portrait.getName());
				FileUtils.copyFileToDir(portrait, archivePortrait, false, null, "Archive portrait");
			}
		}
	}
}