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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_10_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_10_3_0.class);
	
	private static final int BATCH_SIZE = 50;
	private static final String TASK_EXPORT_FOLDER = "Clean export folder";
	private static final String VERSION = "OLAT_10.3.0";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;

	
	public OLATUpgrade_10_3_0() {
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
		allOk &= upgradeExportFodler(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_10_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_10_3_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeExportFodler(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_EXPORT_FOLDER)) {
			int counter = 0;
			final Roles roles = Roles.administratorAndManagersRoles();
			final SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
			params.setRoles(roles);
			params.setResourceTypes(Collections.singletonList("CourseModule"));
			
			List<RepositoryEntry> courses;
			do {
				courses = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, BATCH_SIZE, true);
				for(RepositoryEntry course:courses) {
					processExportFolder(course); 
				}
				counter += courses.size();
				log.info(Tracing.M_AUDIT, "Course export folder processed: " + courses.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(courses.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(TASK_EXPORT_FOLDER, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processExportFolder(RepositoryEntry courseRe) {
		try {
			VFSContainer courseFolder = CourseFactory.getCourseBaseContainer(courseRe.getOlatResource().getResourceableId());
			VFSItem exportFolder = courseFolder.resolve(ICourse.EXPORTED_DATA_FOLDERNAME);
			if(exportFolder != null && exportFolder.exists() && exportFolder instanceof LocalImpl) {
				File exportDir = ((LocalImpl)exportFolder).getBasefile();
				if(exportDir.exists()) {
					FileUtils.deleteDirsAndFiles(exportDir.toPath());
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
