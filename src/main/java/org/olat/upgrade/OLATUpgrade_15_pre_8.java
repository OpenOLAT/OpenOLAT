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
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.04.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_pre_8 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_pre_8.class);

	private static final String VERSION = "OLAT_15.pre.8";
	private static final String DELETE_PORTFOLIO_V1 = "DELETE PORTFOLIO V1";

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;

	public OLATUpgrade_15_pre_8() {
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
		allOk &= deletePortfolioV1(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_pre_8 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_pre_8 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}


	private boolean deletePortfolioV1(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_PORTFOLIO_V1)) {
			try {
				deletePortfolioRepositoryEntries();
				deletePortfolioRootDirectory();
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(DELETE_PORTFOLIO_V1, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void deletePortfolioRepositoryEntries() {
		List<RepositoryEntry> portfolioV1Entries = loadPortfolioV1Entries();
		log.info("Deletion of {} portfolio V1 repository entries started.", portfolioV1Entries.size());
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry entry : portfolioV1Entries) {
			try {
				repositoryService.deletePermanently(entry, null, null, null);
				dbInstance.commitAndCloseSession();
				migrationCounter.incrementAndGet();
			} catch (Exception e) {
				log.error("Portfolio V1 repository entry not deleted. Id={}", entry.getKey());
			}	
		}
		log.info("{} Portfolio V1 repository entries deleted.", migrationCounter);
	}

	private List<RepositoryEntry> loadPortfolioV1Entries() {
		StringBuilder sb = new StringBuilder();
		sb.append("select v");
		sb.append("  from repositoryentry v");
		sb.append("       join fetch v.olatResource as ores");
		sb.append(" where ores.resName='EPStructuredMapTemplate'");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}

	private void deletePortfolioRootDirectory() {
		File portfolioRoot = VFSManager.olatRootContainer(File.separator + "portfolio", null).getBasefile();
		if(Files.exists(portfolioRoot.toPath())) {
			FileUtils.deleteDirsAndFiles(portfolioRoot, true, true);
			dbInstance.commitAndCloseSession();
			log.info("Delete portfolio v1 root directory.");
		}
	}

}
