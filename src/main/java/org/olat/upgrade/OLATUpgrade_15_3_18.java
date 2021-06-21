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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseXStreamAliases;
import org.olat.course.PersistingCourseImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 18 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_3_18 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_3_18.class);

	private static final String VERSION = "OLAT_15.3.18";
	private static final String OPEN_COURSES = "OPEN COURSES";

	@Autowired
	private DB dbInstance;

	public OLATUpgrade_15_3_18() {
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
		allOk &= openCourse(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_3_18 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_3_18 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean openCourse(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(OPEN_COURSES)) {
			try {
				Path coursesPath = Paths.get(FolderConfig.getCanonicalRoot(), PersistingCourseImpl.COURSE_ROOT_DIR_NAME);
				List<Long> resourceIds = getCourseKeys();
				for(Long resourceId:resourceIds)  {
					checkCourse(coursesPath, resourceId);
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(OPEN_COURSES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void checkCourse(Path coursesPath, Long resourceId) {
		try {
			Path coursePath =  coursesPath.resolve(resourceId.toString());
			Path runStructure = coursePath.resolve(PersistingCourseImpl.RUNSTRUCTURE_XML);
			Path editorTree = coursePath.resolve(PersistingCourseImpl.EDITORTREEMODEL_XML);

			XStream xstream = CourseXStreamAliases.getReadCourseXStream();
			if(Files.exists(runStructure)) {
				XStreamHelper.readObject(xstream, runStructure.toFile());
			}
			if(Files.exists(editorTree)) {
				XStreamHelper.readObject(xstream, editorTree.toFile());
			}
		} catch (Exception e) {
			log.error("Error with course: {}", resourceId, e);
		}
	}
	
	private List<Long> getCourseKeys() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ores.resId from repositoryentry v")
		  .append(" inner join v.olatResource as ores")
		  .and().append(" ores.resName = 'CourseModule'");
		
		List<Long> courseKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		dbInstance.commitAndCloseSession();
		return courseKeys;
	}
}
