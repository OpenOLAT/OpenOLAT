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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.delete.service.DeleteUserDataTask;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskDAO;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataDeleteManager;
import org.olat.user.manager.UserDataDeleteDAO;
import org.olat.user.model.UserData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.05.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_5_4 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_5_4.class);

	private static final String VERSION = "OLAT_15.5.4";
	private static final String MOVE_USER_FILE_DELETE = "MOVE USER FILE DELETE";

	private static final int BATCH_SIZE = 500;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserDataDeleteDAO userDataDeleteDao;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	@Autowired
	private UserDataDeleteManager userFileDataManager;

	public OLATUpgrade_15_5_4() {
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
		allOk &= moveDeleteUserData(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_5_4 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_5_4 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean moveDeleteUserData(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MOVE_USER_FILE_DELETE)) {
			try {
				List<PersistentTask> tasks;
				List<UserData> userDatas = new ArrayList<>();
				int count = 0;
				do {
					tasks = getTasks(count, BATCH_SIZE);
					for(PersistentTask task:tasks) {
						DeleteUserDataTask delTask = (DeleteUserDataTask)persistentTaskDao.deserializeTask(task);
						userDatas.add(new UserData(delTask.getIdentityKey(), delTask.getNewDeletedUserName()));
					}
					count += tasks.size();
				} while(tasks.size() == BATCH_SIZE);
				dbInstance.commitAndCloseSession();
				
				// save userDatas
				String userDataXml = userFileDataManager.toXML(userDatas);
				userDataDeleteDao.create(userDataXml, "all");
				dbInstance.commitAndCloseSession();
				
				// delete tasks
				deleteTasks();
				dbInstance.commitAndCloseSession();
				log.info("Migrate {} delete user data tasks.", userDatas.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MOVE_USER_FILE_DELETE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private int deleteTasks() {
		String query = "delete from extask as task where task.task like :xml";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("xml", "<org.olat.admin.user.delete.service.DeleteUserDataTask>%")
				.executeUpdate();
	}
	
	private List<PersistentTask> getTasks(int firstResult, int maxResults) {
		String query = "select task from extask as task where task.task like :xml";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, PersistentTask.class)
				.setParameter("xml", "<org.olat.admin.user.delete.service.DeleteUserDataTask>%")
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
