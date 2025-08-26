/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_1_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_3.class);

	private static final String VERSION = "OLAT_20.1.3";
	private static final String DELETE_DUPLICATED_PUBLISHERS = "DELETE DUPLICATED PUBLISHERS";

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
		allOk &= updateCoursesCurriculumDefaultElement(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean updateCoursesCurriculumDefaultElement(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(DELETE_DUPLICATED_PUBLISHERS)) {
			try {
				log.info("Delete duplicated publishers");
				
				List<Publisher> publishers = getPublishers();
				Map<PublisherKey,List<Publisher>> publishersMap = new HashMap<>();
				for(Publisher publisher:publishers) {
					PublisherKey publisherKey = new PublisherKey(publisher.getResName(), publisher.getResId(), publisher.getSubidentifier());
					publishersMap.computeIfAbsent(publisherKey, key -> new ArrayList<>())
						.add(publisher);
				}
				
				for(List<Publisher> duplicateList:publishersMap.values()) {
					for(int i=1; i<duplicateList.size(); i++) {
						deletePublisherOf(duplicateList.get(i).getKey());
					}
				}
				
				log.info("End deduplicate {} publishers", publishers.size());
				allOk = true;
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(DELETE_DUPLICATED_PUBLISHERS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private int deletePublisherOf(Long publisherKey) {
		int rows = 0;
		try {
			String q1 = "delete from notisub sub where sub.publisher.key=:publisherKey";
			rows = dbInstance.getCurrentEntityManager().createQuery(q1)
					.setParameter("publisherKey", publisherKey)
					.executeUpdate();
			
			String q2 = "delete from notipublisher pub where pub.key=:publisherKey";
			rows += dbInstance.getCurrentEntityManager().createQuery(q2)
					.setParameter("publisherKey", publisherKey).executeUpdate();
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			dbInstance.rollbackAndCloseSession();
		}
		return rows;
	}
	
	private List<Publisher> getPublishers() {
		String query = """
				select pub from notipublisher as pub
				where exists (select pub2.key from notipublisher as pub2
					where pub.key<>pub2.key and pub.resName=pub2.resName and pub.resId=pub2.resId and pub.subidentifier=pub2.subidentifier
					and pub2.rootPublisher is null and pub2.parentPublisher is null
				)
				and pub.data is null and pub.rootPublisher is null and pub.parentPublisher is null
				order by pub.resName, pub.resId, pub.key""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Publisher.class).getResultList();
	}
	
	private record PublisherKey(String resName, Long resId, String subidentifier) {

		@Override
		public int hashCode() {
			return Objects.hash(resId, resName, subidentifier);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof PublisherKey key) {
				return resId != null && resId.equals(key.resId)
						&& resName != null && resName.equals(key.resName)
						&& subidentifier != null && subidentifier.equals(key.subidentifier);
			}
			return false;
		}
	}
}
