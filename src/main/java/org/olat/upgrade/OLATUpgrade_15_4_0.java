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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_4_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_4_0.class);

	private static final String VERSION = "OLAT_15.4.0";
	private static final String INIT_VFS_MODIFIED_BY = "INIT VFS MODIFIED BY";
	private static final String INIT_ENTRY_TECHNICAL_TYPE = "INIT ENTRY TECHNICAL TYPE";
	
	@Autowired
	private DB dbInstance;

	public OLATUpgrade_15_4_0() {
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
		allOk &= initVfsModifiedBy(upgradeManager, uhd);
		allOk &= initEntryTechnicalType(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_4_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_4_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initVfsModifiedBy(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_VFS_MODIFIED_BY)) {
			
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("update filemetadata meta");
				sb.append("   set meta.fileLastModifiedBy = meta.author");
				sb.append(" where meta.author is not null");
				dbInstance.getCurrentEntityManager()
						.createQuery(sb.toString())
						.executeUpdate();
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				dbInstance.rollbackAndCloseSession();
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(INIT_VFS_MODIFIED_BY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean initEntryTechnicalType(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_ENTRY_TECHNICAL_TYPE)) {
			try {
				initEntryTechnicalType();
				dbInstance.commitAndCloseSession();
				log.info("Technical types of all repository entries initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(INIT_ENTRY_TECHNICAL_TYPE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void initEntryTechnicalType() {
		List<RepositoryEntry> courseEntries = getCourseEntries();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry repositoryEntry : courseEntries) {
			initEntryTechnicalType(repositoryEntry);
			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Repository entries: Num. of technical types initialized: {}", migrationCounter);
			}
		}
	}

	private List<RepositoryEntry> getCourseEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re");
		sb.append("  from repositoryentry re");
		sb.append("       inner join re.olatResource as ores");
		sb.and().append(" ores.resName = 'CourseModule'");
		sb.append(" order by re.statistics.lastUsage desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}

	private void initEntryTechnicalType(RepositoryEntry repositoryEntry) {
		try {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			if (course != null) {
				String technicalType = course.getCourseConfig().getNodeAccessType().getType();
				updateTechnicalType(repositoryEntry, technicalType);
			}
		} catch (CorruptedCourseException cce) {
			log.warn("CorruptedCourseException in repository entry technical type init {} ({})",
					repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		} catch (Exception e) {
			log.error("Error in repository entry technical type init {} ({}).", repositoryEntry.getKey(),
					repositoryEntry.getDisplayname());
			log.error("", e);
		}
	}

	private void updateTechnicalType(RepositoryEntry repositoryEntry, String technicalType) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update repositoryentry re");
		sb.append("   set re.technicalType = :technicalType");
		sb.and().append("re.key = :key");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("key", repositoryEntry.getKey())
				.setParameter("technicalType", technicalType)
				.executeUpdate();
	}
	
}
