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

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-08-31<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_20_3_9 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_3_9.class);

	private static final int BATCH_SIZE = 1000;
	private static final String VERSION = "OLAT_20.3.9";
	private static final String MIGRATE_MEDIA_TO_PAGE_PART_VERSION = "MIGRATE MEDIA TO PAGE PART VERSION";

	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDao;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= migrateMediaToPagePartVersions(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_3_9 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_3_9 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	/**
	 * Backfills MediaToPagePart.mediaVersion for relations created before authoring Gallery/Image
	 * Comparison blocks always resolved a real version (see ImageComparisonInspectorController,
	 * GalleryEditorController). A relation without its own version is resolved correctly at
	 * display/export time by falling back to the media's own first version, but Media.versions is
	 * never part of the exported page.xml (xstream.omitField(MediaImpl.class, "versions")), so such
	 * a relation permanently loses its image on export/import. Pinning the version here removes
	 * that gap for existing content.
	 */
	private boolean migrateMediaToPagePartVersions(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_MEDIA_TO_PAGE_PART_VERSION)) {
			try {
				log.info("Migration media to page part version");

				int updated = 0;
				int skipped = 0;
				Long lastKey = 0L;
				List<MediaToPagePart> relations = mediaToPagePartDao.loadRelationsWithoutMediaVersion(lastKey, BATCH_SIZE);
				while (!relations.isEmpty()) {
					for (MediaToPagePart relation : relations) {
						List<MediaVersion> versions = relation.getMedia().getVersions();
						if (versions != null && !versions.isEmpty()) {
							mediaToPagePartDao.updateMediaVersion(relation, versions.get(0), null);
							updated++;
						} else {
							skipped++;
						}
						lastKey = relation.getKey();
					}
					dbInstance.commitAndCloseSession();
					relations = mediaToPagePartDao.loadRelationsWithoutMediaVersion(lastKey, BATCH_SIZE);
				}

				log.info("End migration media to page part version: {} updated, {} skipped (media without any version)", updated, skipped);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_MEDIA_TO_PAGE_PART_VERSION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
