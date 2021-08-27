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
import org.olat.core.logging.Tracing;
import org.olat.course.style.CourseStyleService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoTranscoding;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.07.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_16_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_0_0.class);

	private static final String VERSION = "OLAT_16.0.0";
	private static final String INIT_CN_TEASER_IMAGES = "INIT CN TEASER IMAGES";
	private static final String REMOVE_UNSUPPORTED_TRANSCODINGS = "REMOVE UNSUPPORTED TRANSCODINGS";
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private DB dbInstance;

	public OLATUpgrade_16_0_0() {
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
		
		allOk &= initCourseNodeTeaserNodes(upgradeManager, uhd);
		allOk &= removeUnsupportedTranscodings(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initCourseNodeTeaserNodes(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_CN_TEASER_IMAGES)) {
			
			try {
				courseStyleService.initProvidedSystemImages();
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(INIT_CN_TEASER_IMAGES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean removeUnsupportedTranscodings(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(REMOVE_UNSUPPORTED_TRANSCODINGS)) {
			int RESOLUTION_240 = 240;
			int RESOLUTION_360 = 360;
			
			try {
				int videoDeletedCount = 0;
				for(VideoTranscoding transcoding : videoManager.getAllVideoTranscodings()) {
					if (transcoding.getResolution() == RESOLUTION_240 || transcoding.getResolution() == RESOLUTION_360) {
						log.info("Video transcoding deleted for video " + transcoding.getVideoResource().getKey() + " - " + transcoding.getResolution() + "p");
						videoManager.deleteVideoTranscoding(transcoding);
						
						if(++videoDeletedCount % 10 == 0) {
							dbInstance.commitAndCloseSession();
						}
						
					}
				}
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(REMOVE_UNSUPPORTED_TRANSCODINGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
}
