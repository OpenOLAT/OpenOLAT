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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.Tracing;
import org.olat.modules.video.VideoTranscoding;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_20_3_12 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_3_12.class);

	private static final String VERSION = "OLAT_20.3.12";
	private static final String RESET_IN_FLIGHT_VIDEO_TRANSCODING_JOBS = "RESET IN-FLIGHT VIDEO TRANSCODING JOBS";
	private static final String RESET_IN_FLIGHT_VFS_CONVERSION_JOBS = "RESET IN-FLIGHT VFS CONVERSION JOBS";

	@Autowired
	private DB dbInstance;

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
		allOk &= resetInFlightVideoTranscodingJobs(upgradeManager, uhd);
		allOk &= resetInFlightVfsConversionJobs(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_3_12 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_3_12 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	/**
	 * OO-9999: jobs dispatched to the external video transcoding service before
	 * this update used a callback URL without the HMAC token this update
	 * introduces (WP1-WP3). That callback is now rejected with 403, so a row
	 * still "in progress" (status 0-99) at the moment this upgrade runs can
	 * never finish on its own: it was necessarily dispatched before the fix,
	 * since every job dispatched after it carries a signed URL. Resetting it
	 * to waiting lets the regular job scheduler dispatch it again. Local-mode
	 * transcoding already self-heals such rows on its own (no callback
	 * involved), so resetting them too is harmless, just redundant.
	 */
	private boolean resetInFlightVideoTranscodingJobs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RESET_IN_FLIGHT_VIDEO_TRANSCODING_JOBS)) {
			try {
				// VideoTranscoding has no named "started" constant: 0..99 is the live HandBrake/service percentage.
				String query = "update videotranscoding set status=:waitingStatus"
						+ " where status>=:startedStatus and status<=:maxInProgressStatus";
				int updated = dbInstance.getCurrentEntityManager().createQuery(query)
						.setParameter("waitingStatus", VideoTranscoding.TRANSCODING_STATUS_WAITING)
						.setParameter("startedStatus", 0)
						.setParameter("maxInProgressStatus", 99)
						.executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("Reset {} in-flight video transcoding job(s) to waiting", updated);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(RESET_IN_FLIGHT_VIDEO_TRANSCODING_JOBS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	/** OO-9999: same reasoning as {@link #resetInFlightVideoTranscodingJobs}, for audio/video recording conversion jobs. */
	private boolean resetInFlightVfsConversionJobs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RESET_IN_FLIGHT_VFS_CONVERSION_JOBS)) {
			try {
				String query = "update filemetadata set transcodingStatus=:waitingStatus"
						+ " where transcodingStatus>=:startedStatus and transcodingStatus<=:maxInProgressStatus";
				int updated = dbInstance.getCurrentEntityManager().createQuery(query)
						.setParameter("waitingStatus", VFSMetadata.TRANSCODING_STATUS_WAITING)
						.setParameter("startedStatus", VFSMetadata.TRANSCODING_STATUS_STARTED)
						.setParameter("maxInProgressStatus", 99)
						.executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("Reset {} in-flight VFS conversion job(s) to waiting", updated);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(RESET_IN_FLIGHT_VFS_CONVERSION_JOBS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
