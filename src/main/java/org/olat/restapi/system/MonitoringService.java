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
package org.olat.restapi.system;

import org.olat.admin.sysinfo.manager.DatabaseStatsManager;
import org.olat.admin.sysinfo.model.DatabaseConnectionVO;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.search.SearchServiceStatus;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceStatusImpl;
import org.olat.search.service.indexer.FullIndexerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Small stateful service to cache some data.
 * 
 * Initial date: 15 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MonitoringService {
	
	private static final int RENEW_RATE =  60 * 1000;// once an hour
	
	private long start;
	private long activeUserCountCached;
	private long totalGroupCountCached;
	private long publishedCoursesCached;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private DatabaseStatsManager databaseStatsManager;
	
	public Statistics getStatistics() {
		Statistics statistics = new Statistics();
		
		SessionsVO sessionsVo = new OpenOLATStatisticsWebService().getSessionsVO();
		statistics.setSessionsVo(sessionsVo);

		SearchServiceStatus status = SearchServiceFactory.getService().getStatus();
		if(status instanceof SearchServiceStatusImpl) {
			SearchServiceStatusImpl statusImpl = (SearchServiceStatusImpl)status;
			FullIndexerStatus fStatus = statusImpl.getFullIndexerStatus();
			statistics.setLastFullIndexTime(fStatus.getLastFullIndexDateString());
		}
		
		// activeUserCount="88" // registered and activated identities, same as in GUI
		if(start < 1 || (System.currentTimeMillis() - start) > RENEW_RATE) {
			start = System.currentTimeMillis();
			activeUserCountCached = securityManager.countIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, null, Identity.STATUS_ACTIV);
			totalGroupCountCached = businessGroupService.countBusinessGroups(null, null);
			publishedCoursesCached = repositoryManager.countPublished(CourseModule.ORES_TYPE_COURSE);
			dbInstance.commitAndCloseSession();
		}
		statistics.setActiveUserCount(activeUserCountCached);
		statistics.setTotalGroupCount(totalGroupCountCached);
		statistics.setPublishedCourses(publishedCoursesCached);
		
		
		DatabaseConnectionVO connections = databaseStatsManager.getConnectionInfos();
		if(connections != null) {
			statistics.setActiveConnectionCount(connections.getActiveConnectionCount());
			statistics.setCurrentConnectionCount(connections.getCurrentConnectionCount());
		}
		dbInstance.commitAndCloseSession();
		return statistics;
	}

	public class Statistics {
		
		private long activeUserCount = -1;
		private long totalGroupCount = -1;
		private long publishedCourses = -1;
		private SessionsVO sessionsVo;
		private String lastFullIndexTime;
		
		private long activeConnectionCount;
		private long currentConnectionCount;
		
		public long getActiveUserCount() {
			return activeUserCount;
		}
		
		public void setActiveUserCount(long activeUserCount) {
			this.activeUserCount = activeUserCount;
		}
		
		public long getTotalGroupCount() {
			return totalGroupCount;
		}
		
		public void setTotalGroupCount(long totalGroupCount) {
			this.totalGroupCount = totalGroupCount;
		}
		
		public long getPublishedCourses() {
			return publishedCourses;
		}
		
		public void setPublishedCourses(long publishedCourses) {
			this.publishedCourses = publishedCourses;
		}
		
		public SessionsVO getSessionsVo() {
			return sessionsVo;
		}
		
		public void setSessionsVo(SessionsVO sessionsVo) {
			this.sessionsVo = sessionsVo;
		}
		
		public String getLastFullIndexTime() {
			return lastFullIndexTime;
		}
		
		public void setLastFullIndexTime(String lastFullIndexTime) {
			this.lastFullIndexTime = lastFullIndexTime;
		}

		public long getActiveConnectionCount() {
			return activeConnectionCount;
		}

		public void setActiveConnectionCount(long activeConnectionCount) {
			this.activeConnectionCount = activeConnectionCount;
		}

		public long getCurrentConnectionCount() {
			return currentConnectionCount;
		}

		public void setCurrentConnectionCount(long currentConnectionCount) {
			this.currentConnectionCount = currentConnectionCount;
		}
	}
}
