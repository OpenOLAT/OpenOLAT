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

import javax.persistence.Tuple;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.modules.appointments.Organizer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_2_6 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_2_6.class);

	private static final String VERSION = "OLAT_15.2.6";
	private static final String CLEAN_UP_APPOINTMENT_ORGANIZERS = "CLEAN UP APPOINTMENT ORGANIZERS";

	@Autowired
	private DB dbInstance;

	public OLATUpgrade_15_2_6() {
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
		allOk &= cleanAppointmentOrganizers(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_2_6 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_2_6 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean cleanAppointmentOrganizers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_UP_APPOINTMENT_ORGANIZERS)) {
			
			List<Tuple> duplicates = getDuplicates();
			
			for (Tuple tuple : duplicates) {
				try {
					Long topicKey = tuple.get(0, Long.class);
					Long identityKey = tuple.get(1, Long.class);
					deleteDuplicate(topicKey, identityKey);
					dbInstance.commitAndCloseSession();
				} catch (Exception e) {
					dbInstance.rollbackAndCloseSession();
					log.error("", e);
				}
			}
			
			uhd.setBooleanDataValue(CLEAN_UP_APPOINTMENT_ORGANIZERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<Tuple> getDuplicates() {
		StringBuilder sb = new StringBuilder();
		sb.append("select organizer.topic.key, organizer.identity.key");
		sb.append("  from appointmentorganizer organizer");
		sb.append(" group by organizer.topic.key, organizer.identity.key");
		sb.append(" having count(*) > 1");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tuple.class)
				.getResultList();
	}
	
	private void deleteDuplicate(Long topicKey, Long identityKey) {
		List<Organizer> organizers = getOrganizers(topicKey, identityKey);
		
		// Keep the first record and delete all others
		for (int i = 1; i < organizers.size(); i++) {
			Organizer organizer = organizers.get(i);
			dbInstance.getCurrentEntityManager().remove(organizer);
		}
	}

	private List<Organizer> getOrganizers(Long topicKey, Long identityKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select organizer");
		sb.append("  from appointmentorganizer organizer");
		sb.and().append(" organizer.topic.key = :topicKey");
		sb.and().append(" organizer.identity.key = :identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organizer.class)
				.setParameter("topicKey", topicKey)
				.setParameter("identityKey", identityKey)
				.getResultList();
	}


}
