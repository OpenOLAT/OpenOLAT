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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_0_5 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_0_5.class);
	
	private static final String VERSION = "OLAT_14.0.5";
	private static final String UNKOWN_VIDEO_FORMAT = "UPDATE UNKOWN VIDEO FORMAT";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoManager videoManager;

	
	public OLATUpgrade_14_0_5() {
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
		allOk &= updateUnkownVideoFormat(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_0_5 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_0_5 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean updateUnkownVideoFormat(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UNKOWN_VIDEO_FORMAT)) {
			try {
				List<VideoMeta> videoMetadataList = getVideoMetadataWithoutFormat();
				for(VideoMeta videoMetadata:videoMetadataList) {
					videoManager.checkUnkownVideoFormat(videoMetadata);
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(UNKOWN_VIDEO_FORMAT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<VideoMeta> getVideoMetadataWithoutFormat() {
			StringBuilder sb = new StringBuilder(128);
			sb.append("select meta from videometadata as meta")
			  .append(" inner join fetch meta.videoResource as vResource")
			  .append(" where meta.format is null or meta.format = '' or meta.format = 'mov' or meta.format = 'zip'");
			return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(),VideoMeta.class)
					.getResultList();
	}
}
