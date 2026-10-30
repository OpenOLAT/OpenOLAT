/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_2_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_2_0.class);

	private static final int BATCH_SIZE = 5000;
	
	private static final String VERSION = "OLAT_21.2.0";
	
	private static final String INITIALIZE_USER_COURSE_ACTIVITY = "INITIALIZE USER COURSE ACTIVITY";


	@Autowired
	private DB dbInstance;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;

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
		allOk &= initializeUserCourseActivity(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initializeUserCourseActivity(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INITIALIZE_USER_COURSE_ACTIVITY)) {
			try {
				log.info("Start initializing users courses activity");

				int count = 0;
				Long lastKey = Long.valueOf(0l);
				List<UserCourseInfosImpl> userCourseInfosList = getUsersCoursesInformations(lastKey, BATCH_SIZE);
				while (!userCourseInfosList.isEmpty()) {
					for (UserCourseInfosImpl userCourseInfos : userCourseInfosList) {
						lastKey = userCourseInfos.getKey();
						int visit = Math.min(20, userCourseInfos.getVisit());
						Date date = userCourseInfos.getRecentLaunch() == null
								? userCourseInfos.getCreationDate()
								: userCourseInfos.getRecentLaunch();
						double activity = userCourseInformationsManager.calculateActivity(visit, date);
						if(activity != userCourseInfos.getActivity()) {
							userCourseInfos.setActivity(activity);
							dbInstance.getCurrentEntityManager().merge(userCourseInfos);
						}
						if (count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					dbInstance.commitAndCloseSession();
					userCourseInfosList = getUsersCoursesInformations(lastKey, BATCH_SIZE);
				}

				log.info("End initializing users courses activity: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INITIALIZE_USER_COURSE_ACTIVITY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<UserCourseInfosImpl> getUsersCoursesInformations(Long lastKey, int maxResults) {
		String sb = """
			select infos from usercourseinfos as infos
			where infos.key>:lastKey
			order by infos.key""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, UserCourseInfosImpl.class)
				.setParameter("lastKey", lastKey)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
