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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_2_6 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_2_6.class);

	private static final String VERSION = "OLAT_14.2.6";
	private static final String DELETE_COURSE_EXPORT_DIRECTORY = "DELETE COURSE EXPORT DIRECTORY";
	private static final String DELETE_DOWNLOAD_ZIP = "DELETE DOWNLOAD ZIP";
	
	@Autowired
	private DB dbInstance;

	public OLATUpgrade_14_2_6() {
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
			return false;
		}
		
		boolean allOk = true;
		allOk &= deleteExportDirectory(upgradeManager, uhd);
		allOk &= deleteDownloadZip(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_2_6 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_2_6 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean deleteExportDirectory(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_COURSE_EXPORT_DIRECTORY)) {
			Path courseDirectory = Paths.get(FolderConfig.getCanonicalRoot(), "course");
			
			List<Long> courses = new ArrayList<>();
			try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(courseDirectory)) {
	            for (Path path : directoryStream) {
	            	String name = path.getFileName().toString();
	            	if(StringHelper.containsNonWhitespace(name)) {
	            		Path exportDirectory = path.resolve("export");
	            		if(Files.exists(exportDirectory)) {
	            			courses.add(Long.parseLong(name));
	            		}
	            	}
	            }
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			for(Long course:courses) {
				deleteCourseExportDirectory(course);
			}
			
			uhd.setBooleanDataValue(DELETE_COURSE_EXPORT_DIRECTORY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void deleteCourseExportDirectory(Long courseId) {	
		try {
			Path courseExportDirectory = Paths.get(FolderConfig.getCanonicalRoot(), "course", courseId.toString(), "export");
			if(Files.exists(courseExportDirectory)) {
				FileUtils.deleteDirsAndFiles(courseExportDirectory.toFile(), true, true);
				dbInstance.commitAndCloseSession();
				log.info("Delete export directory of course with id: {}", courseId);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private boolean deleteDownloadZip(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_DOWNLOAD_ZIP)) {
			
			String[] downloadNames = new String[] { "imscp.zip", "blog.zip", "podcast.zip" };
			
			Path repositoryDirectory = Paths.get(FolderConfig.getCanonicalRepositoryHome());
			try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(repositoryDirectory)) {
	            for (Path path : directoryStream) {
	            	String name = path.getFileName().toString();
	            	if(StringHelper.containsNonWhitespace(name)) {
	            		for(String downloadName:downloadNames) {
		            		Path backupZip = path.resolve(downloadName);
		            		if(Files.exists(backupZip)) {
		            			FileUtils.deleteFile(backupZip.toFile());
		        				log.info("Delete download zip {} for resource with id: {}", downloadName, name);
			            		dbInstance.commitAndCloseSession();
		            		}
	            		}
	            	}
	            }
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}

			uhd.setBooleanDataValue(DELETE_DOWNLOAD_ZIP, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
