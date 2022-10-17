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

import java.net.URI;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_2_7 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_2_7.class);

	private static final String VERSION = "OLAT_14.2.7";
	private static final String BIGBLUEBUTTON_TO_DB = "BIGBLUEBUTTON SERVER TO DB";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	public OLATUpgrade_14_2_7() {
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
		allOk &= migrateBigBlueButtonServer(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_2_7 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_2_67not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateBigBlueButtonServer(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(BIGBLUEBUTTON_TO_DB)) {
			
			if(bigBlueButtonModule.isEnabled()) {
				URI uri = bigBlueButtonModule.getBigBlueButtonURI();
				String sharedSecret = bigBlueButtonModule.getSharedSecret();
				if(uri != null && sharedSecret != null) {
					String uriStr = uri.toString();
					BigBlueButtonServer server = null;;
					if(!bigBlueButtonManager.hasServer(uriStr)) {
						server = bigBlueButtonManager.createServer(uriStr, null, sharedSecret);
						dbInstance.commitAndCloseSession();
					} else {
						List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
						for(BigBlueButtonServer potentialServer:servers) {
							if(uriStr.startsWith(potentialServer.getUrl()) || potentialServer.getUrl().startsWith(uriStr)) {
								server = potentialServer;
							}
						}
					}
					if(server != null) {
						migrateBigBlueButtonServer(server);
						dbInstance.commitAndCloseSession();
					}
				}
			}

			uhd.setBooleanDataValue(BIGBLUEBUTTON_TO_DB, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateBigBlueButtonServer(BigBlueButtonServer server) {
		List<BigBlueButtonMeeting> meetings = getMeetingsToMigrate();
		for(BigBlueButtonMeeting meeting:meetings) {
			((BigBlueButtonMeetingImpl)meeting).setServer(server);
			bigBlueButtonManager.updateMeeting(meeting);
		}
	}
	
	private List<BigBlueButtonMeeting> getMeetingsToMigrate() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.server.key is null and (meeting.permanent=true or meeting.startDate<=:now)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("now", new Date(), TemporalType.TIMESTAMP)
				.getResultList();
	}
}
