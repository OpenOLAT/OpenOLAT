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
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.manager.PageDAO;
import org.olat.modules.portfolio.model.PageImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.05.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_5_1 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_5_1.class);

	private static final String VERSION = "OLAT_15.5.1";
	private static final String ADD_MISSING_ENTRY_TECHNICAL_TYPE = "ADD MISSING ENTRY TECHNICAL TYPE";
	private static final String COPY_PORTFOLIO_PAGE_EDITABLE_FLAG = "COPY PORTFOLIO PAGE EDITABLE FLAG";

	private static final int BATCH_SIZE = 500;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;

	public OLATUpgrade_15_5_1() {
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
		allOk &= addMissingEntryTechnicalType(upgradeManager, uhd);
		allOk &= editablePageFlag(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_5_1 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_5_1 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean editablePageFlag(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(COPY_PORTFOLIO_PAGE_EDITABLE_FLAG)) {
			try {
				List<Page> pages;
				do {
					pages = getPages(0, BATCH_SIZE);
					for(Page page:pages) {
						makePageNotEditable(page);
					}
				} while(pages.size() == BATCH_SIZE);
				dbInstance.commitAndCloseSession();
				log.info("Pass the editable flag to copied portfolio pages.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(COPY_PORTFOLIO_PAGE_EDITABLE_FLAG, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void makePageNotEditable(Page page) {
		if(page.isEditable()) return;
		
		List<Page> linkedPages = pageDao.getPagesBySharedBody(page);
		for(Page linkedPage:linkedPages) {
			if(linkedPage.isEditable()) {
				((PageImpl)linkedPage).setEditable(false);
				pageDao.updatePage(linkedPage);
			}
		}
		dbInstance.commitAndCloseSession();
	}
	
	private List<Page> getPages(int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page from pfpage page")
		  .append(" inner join pfpage copypage on (copypage.body.key=page.body.key and copypage.editable=true)")
		  .append(" where page.editable=false")
		  .append(" order by page.key");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Page.class)
			.setFirstResult(firstResult)
			.setMaxResults(maxResults)
			.getResultList();
	}
	
	private boolean addMissingEntryTechnicalType(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ADD_MISSING_ENTRY_TECHNICAL_TYPE)) {
			try {
				addMissingEntryTechnicalType();
				dbInstance.commitAndCloseSession();
				log.info("Technical type added if missing.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(ADD_MISSING_ENTRY_TECHNICAL_TYPE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void addMissingEntryTechnicalType() {
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
				log.info("Repository entries: Num. of technical types added: {}", migrationCounter);
			}
		}
	}

	private List<RepositoryEntry> getCourseEntries() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select re");
		sb.append("  from repositoryentry re");
		sb.append("       inner join re.olatResource as ores");
		sb.and().append(" ores.resName = 'CourseModule'");
		sb.and().append(" re.technicalType is null");
		
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
			log.warn("CorruptedCourseException in add missing technical type {} ({})",
					repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		} catch (Exception e) {
			log.error("Error in add missing technical type  {} ({}).", repositoryEntry.getKey(),
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
