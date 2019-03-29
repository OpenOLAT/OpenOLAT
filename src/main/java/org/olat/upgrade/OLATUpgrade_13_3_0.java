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
package org.olat.upgrade;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.manager.VFSRepositoryServiceImpl;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_3_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.3.0";
	private static final String MOVE_REPO_IMAGES = "MOVE REPO IMAGES";
	private static final String MIGRATE_FILE_METADATA = "MIGRATE FILE METADATA";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VFSRepositoryServiceImpl vfsRepositoryService;
	
	public OLATUpgrade_13_3_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			vfsRepositoryService.setMigrated(true);
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateRepositoryImages(upgradeManager, uhd);
		allOk &= migrateMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_3_0 successfully!");
		} else {
			log.audit("OLATUpgrade_13_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	@Override
	public boolean doNewSystemInit() {
		vfsRepositoryService.setMigrated(true);
		return super.doNewSystemInit();
	}

	/**
	 * Migrate the images of learn resources and courses.
	 * 
	 * @param upgradeManager The upgrade manage
	 * @param uhd The history
	 * @return true if successful
	 */
	private boolean migrateRepositoryImages(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MOVE_REPO_IMAGES)) {
			
			try {
				int counter = 0;
				
				File repositoryHome = new File(FolderConfig.getCanonicalRepositoryHome());
				File[] images = repositoryHome.listFiles(new ImageFilter());
				for(File image:images) {
					String name = image.getName();
					int index = name.lastIndexOf('.');
					String resourceId = name.substring(0, index);
					if(StringHelper.isLong(resourceId)) {
						migrateRepositoryImage(image, Long.valueOf(resourceId));
					}
					if(counter++ % 50 == 0) {
						log.info("Images of lear resources moved: " + counter);
						dbInstance.commitAndCloseSession();
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MOVE_REPO_IMAGES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateRepositoryImage(File image, Long resourceId) {
		RepositoryEntry resource = repositoryService.loadByKey(resourceId);// yes, resource id is repository entry primary key
		if(resource == null) {
			deleteImage(image);
		} else {
			File home;
			if("CourseModule".equals(resource.getOlatResource().getResourceableTypeName())) {
				home = new File(FolderConfig.getCanonicalRoot(), "course");
			} else {
				home = new File(FolderConfig.getCanonicalRepositoryHome());
			}
			
			try {
				File resourceDir = new File(home, resource.getOlatResource().getResourceableId().toString());
				if(resourceDir.exists()) {
					File mediaDir = new File(resourceDir, "media");
					if(!mediaDir.exists()) {
						mediaDir.mkdir();
					}
					
					File movedImage = new File(mediaDir, image.getName());
					if(movedImage.exists()) {
						deleteImage(image);
					} else {
						Files.move(image.toPath(), movedImage.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} else {
					deleteImage(image);
				}
			} catch (IOException e) {
				log.error("", e);
			}	
		}
	}
	
	private void deleteImage(File image) {
		try {
			Files.delete(image.toPath());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Migrate the metadata
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean migrateMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_FILE_METADATA)) {
			
			try {
				//go through /bcroot/
				File canonicalRoot = new File(FolderConfig.getCanonicalRoot());
				vfsRepositoryService.migrateDirectories(canonicalRoot);
				vfsRepositoryService.setMigrated(true);
			} catch (IOException e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_FILE_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private static class ImageFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			File file = new File(dir, name);
			return file.isFile() && !file.isHidden();
		}
	}
}
