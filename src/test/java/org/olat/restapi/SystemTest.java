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
package org.olat.restapi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.olat.restapi.system.vo.ClasseStatisticsVO;
import org.olat.restapi.system.vo.DatabaseVO;
import org.olat.restapi.system.vo.EnvironmentInformationsVO;
import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.MemoryStatisticsVO;
import org.olat.restapi.system.vo.MemoryVO;
import org.olat.restapi.system.vo.MonitoringInfosVO;
import org.olat.restapi.system.vo.OpenOLATStatisticsVO;
import org.olat.restapi.system.vo.ReleaseInfosVO;
import org.olat.restapi.system.vo.RepositoryStatisticsVO;
import org.olat.restapi.system.vo.RuntimeStatisticsVO;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.restapi.system.vo.StatusVO;
import org.olat.restapi.system.vo.TasksVO;
import org.olat.restapi.system.vo.ThreadStatisticsVO;
import org.olat.restapi.system.vo.ThreadVO;
import org.olat.restapi.system.vo.ThreadVOes;
import org.olat.restapi.system.vo.ThreadsVO;
import org.olat.restapi.system.vo.UserStatisticsVO;
import org.olat.restapi.system.vo.VFSStatsVO;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SystemTest extends OlatRestTestCase {
	
	@Test
	public void testMonitoringStatus() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("status").build();
		StatusVO stats = conn.get(systemUri, StatusVO.class);
		Assert.assertNotNull(stats);
		Assert.assertTrue(stats.isWriteDb());
		Assert.assertTrue(stats.isWriteFile());
		Assert.assertTrue(stats.getConcurrentDispatchThreads() >= 0l);
		Assert.assertTrue(stats.getSecureAuthenticatedCount() >= 0l);
		conn.shutdown();
	}
	
	@Test
	public void testRuntimeStatisticsInfos() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("runtime").build();
		RuntimeStatisticsVO runtimeStats = conn.get(systemUri, RuntimeStatisticsVO.class);
		assertNotNull(runtimeStats);
		assertNotNull(runtimeStats.getClasses());
		assertNotNull(runtimeStats.getMemory());
		assertNotNull(runtimeStats.getThreads());
		
		conn.shutdown();
	}
	
	@Test
	public void testSystemThreads() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("threads").build();
		ThreadsVO threadInfos = conn.get(systemUri, ThreadsVO.class);

		assertNotNull(threadInfos);
		assertTrue(threadInfos.getDaemonCount() > 0);
		assertTrue(threadInfos.getThreadCount() > 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemThreadDetails() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("threads").path("cpu").build();
		ThreadVOes threadInfos = conn.get(systemUri, ThreadVOes.class);

		Assert.assertNotNull(threadInfos);
		Assert.assertNotNull(threadInfos.getThreads());
		Assert.assertTrue(threadInfos.getTotalCount() > 0);
		Assert.assertEquals(threadInfos.getTotalCount(), threadInfos.getThreads().length);
		
		ThreadVO threadVo = threadInfos.getThreads()[0];
		Assert.assertNotNull(threadVo.getName());
		Assert.assertTrue(threadVo.getCpuTime() >= 0);
		Assert.assertTrue(threadVo.getCpuUsage() >= 0.0f);
		Assert.assertTrue(threadVo.getId() > 0l);
		conn.shutdown();	
	}

	@Test
	public void testSystemMemory() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("memory").build();
		MemoryVO memoryInfos = conn.get(systemUri, MemoryVO.class);

		assertNotNull(memoryInfos);
		assertTrue(memoryInfos.getMaxAvailable() > 0);
		assertTrue(memoryInfos.getTotalMem() > 0);
		assertTrue(memoryInfos.getTotalUsed() > 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemSessions() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("openolat").path("sessions").build();
		SessionsVO sessionInfos = conn.get(systemUri, SessionsVO.class);

		assertNotNull(sessionInfos);
		assertTrue(sessionInfos.getCount() > 0);
		assertTrue(sessionInfos.getAuthenticatedCount() >= 0);
		assertTrue(sessionInfos.getSecureAuthenticatedCount() >= 0);
		assertTrue(sessionInfos.getSecureWebdavCount() >= 0);
		assertTrue(sessionInfos.getWebdavCount() >= 0);
		assertTrue(sessionInfos.getRestCount() >= 0);
		assertTrue(sessionInfos.getSecureRestCount() >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemUserStatistics() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("openolat").path("users").build();
		UserStatisticsVO userStats = conn.get(systemUri, UserStatisticsVO.class);
		assertNotNull(userStats);
		
		long totalUserCount = userStats.getTotalUserCount();
		assertTrue(totalUserCount > 0);
		long activeUserCount = userStats.getActiveUserCount();
		assertTrue(activeUserCount >= 0);
		
		long activeUserCountLastDay = userStats.getActiveUserCountLastDay();
		assertTrue(activeUserCountLastDay >= 0);
		
		long activeUserCountLastWeek = userStats.getActiveUserCountLastWeek();
		assertTrue(activeUserCountLastWeek >= 0);
		
		long activeUserCountLastMonth = userStats.getActiveUserCountLastMonth();
		assertTrue(activeUserCountLastMonth >= 0);
		
		long activeUserCountLast6Month = userStats.getActiveUserCountLast6Month();
		assertTrue(activeUserCountLast6Month >= 0);
		
		long blockedUserCount = userStats.getBlockedUserCount();
		assertTrue(blockedUserCount >= 0);
		
		long deletedUserCount = userStats.getDeletedUserCount();
		assertTrue(deletedUserCount >= 0);
		
		long externalUserCount = userStats.getExternalUserCount();
		assertTrue(externalUserCount >= 0);
		
		long totalGroupCount = userStats.getTotalGroupCount();
		assertTrue(totalGroupCount >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemRepositoryStatistics() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("openolat").path("repository").build();
		RepositoryStatisticsVO repoStats = conn.get(systemUri, RepositoryStatisticsVO.class);

		assertNotNull(repoStats);
		assertTrue(repoStats.getCoursesCount() >= 0);
		assertTrue(repoStats.getPublishedCoursesCount() >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemIndexerStatistics() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("openolat").path("indexer").build();
		IndexerStatisticsVO indexerStats = conn.get(systemUri, IndexerStatisticsVO.class);

		assertNotNull(indexerStats);
		assertNotNull(indexerStats.getStatus());
		assertNotNull(indexerStats.getFullIndexStartedAt());
		assertNotNull(indexerStats.getLastFullIndexTime());
		assertTrue(indexerStats.getIndexedDocumentCount() >= 0);
		assertTrue(indexerStats.getExcludedDocumentCount() >= 0);
		assertTrue(indexerStats.getAvailableFolderIndexerCount() >= 0);
		assertTrue(indexerStats.getRunningFolderIndexerCount() >= 0);
		assertTrue(indexerStats.getIndexingTime() >= 0);
		assertTrue(indexerStats.getIndexSize() >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemOpenOLATStatistics() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("openolat").build();
		OpenOLATStatisticsVO stats = conn.get(systemUri, OpenOLATStatisticsVO.class);

		assertNotNull(stats);
		assertNotNull(stats.getIndexerStatistics());
		assertNotNull(stats.getRepositoryStatistics());
		assertNotNull(stats.getSessions());
		assertNotNull(stats.getUserStatistics());
		assertNotNull(stats.getIndexerStatistics().getStatus());
		assertTrue(stats.getIndexerStatistics().getIndexedDocumentCount() >= 0);
		assertTrue(stats.getIndexerStatistics().getExcludedDocumentCount() >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testEnvironmentSystem() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("environment").build();
		EnvironmentInformationsVO envInfos = conn.get(systemUri, EnvironmentInformationsVO.class);

		assertNotNull(envInfos);
		assertNotNull(envInfos.getArch());
		assertTrue(envInfos.getAvailableProcessors() > 0);
		assertNotNull(envInfos.getOsName());
		assertNotNull(envInfos.getOsVersion());
		assertNotNull(envInfos.getRuntimeName());
		assertNotNull(envInfos.getVmName());
		assertNotNull(envInfos.getVmVendor());
		assertNotNull(envInfos.getVmVersion());

		conn.shutdown();	
	}
	
	@Test
	public void testRuntimeStatsSystem() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("runtime").build();
		RuntimeStatisticsVO runInfos = conn.get(systemUri, RuntimeStatisticsVO.class);

		assertNotNull(runInfos);
		assertNotNull(runInfos.getStartTime());
		assertNotNull(runInfos.getClasses());
		assertNotNull(runInfos.getMemory());
		assertNotNull(runInfos.getThreads());
		assertTrue(runInfos.getSystemLoadAverage() > 0.0d);
		assertTrue(runInfos.getUpTime() > 0);

		conn.shutdown();	
	}
	
	@Test
	public void testRuntimeClassesStatsSystem() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("runtime").path("classes").build();
		ClasseStatisticsVO classesInfos = conn.get(systemUri, ClasseStatisticsVO.class);

		assertNotNull(classesInfos);
		assertTrue(classesInfos.getLoadedClassCount() > 0);
		assertTrue(classesInfos.getTotalLoadedClassCount() > 0);
		assertTrue(classesInfos.getUnloadedClassCount() >= 0);

		conn.shutdown();	
	}
	
	@Test
	public void testRuntimeThreadsStatsSystem() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("runtime").path("threads").build();
		ThreadStatisticsVO threadsInfos = conn.get(systemUri, ThreadStatisticsVO.class);

		assertNotNull(threadsInfos);
		assertTrue(threadsInfos.getDaemonCount() > 0);
		assertTrue(threadsInfos.getPeakThreadCount() > 0);
		assertTrue(threadsInfos.getThreadCount() >= 0);

		conn.shutdown();	
	}
	
	@Test
	public void testRuntimeMemoryStatsSystem() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("runtime").path("memory").build();
		MemoryStatisticsVO memoryInfos = conn.get(systemUri, MemoryStatisticsVO.class);

		assertNotNull(memoryInfos);
		assertTrue(memoryInfos.getFreeMemory() > 0);
		assertTrue(memoryInfos.getTotalMemory() > 0);
		assertTrue(memoryInfos.getUsedMemory() > 0);

		assertTrue(memoryInfos.getInitHeap() >= 0);
		assertTrue(memoryInfos.getInitNonHeap() >= 0);
		assertTrue(memoryInfos.getUsedHeap() > 0);
		assertTrue(memoryInfos.getUsedNonHeap() > 0);
		assertTrue(memoryInfos.getCommittedHeap() > 0);
		assertTrue(memoryInfos.getCommittedNonHeap() > 0);
		assertTrue(memoryInfos.getMaxHeap() > 0);
		assertTrue(memoryInfos.getMaxNonHeap() >= 0);

		conn.shutdown();	
	}
	
	@Test
	public void testReleaseInfos() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("release").build();
		ReleaseInfosVO versionInfos = conn.get(systemUri, ReleaseInfosVO.class);

		assertNotNull(versionInfos);
		assertNotNull(versionInfos.getInstanceID());
		assertNotNull(versionInfos.getBuildVersion());
		assertNotNull(versionInfos.getOlatVersion());
		assertNotNull(versionInfos.getRepoRevision());

		conn.shutdown();	
	}
	
	@Test
	public void testDatabase() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("database").build();
		DatabaseVO databaseInfos = conn.get(systemUri, DatabaseVO.class);
		assertNotNull(databaseInfos);
		assertNotNull(databaseInfos.getConnectionInfos());
		assertNotNull(databaseInfos.getHibernateStatistics());
		//connections
		assertTrue(databaseInfos.getConnectionInfos().getActiveConnectionCount() >= 0);
		assertTrue(databaseInfos.getConnectionInfos().getCurrentConnectionCount() > 0);
		//hibernate
		assertTrue(databaseInfos.getHibernateStatistics().getFailedTransactionsCount() >= 0);
		assertTrue(databaseInfos.getHibernateStatistics().getOpenSessionsCount() >= 0);
		assertTrue(databaseInfos.getHibernateStatistics().getOptimisticFailureCount() >= 0);
		assertTrue(databaseInfos.getHibernateStatistics().getQueryExecutionCount() > 0);
		assertTrue(databaseInfos.getHibernateStatistics().getQueryExecutionMaxTime() > 0);
		assertNotNull(databaseInfos.getHibernateStatistics().getQueryExecutionMaxTimeQueryString());
		assertTrue(databaseInfos.getHibernateStatistics().getSuccessfulTransactionCount() > 0);
		assertTrue(databaseInfos.getHibernateStatistics().getTransactionsCount() > 0);

		conn.shutdown();	
	}
	
	@Test
	public void testTasks() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI tasksUri = conn.getContextURI().path("system").path("monitoring").path("openolat").path("tasks").build();
		TasksVO infos = conn.get(tasksUri, TasksVO.class);
		assertNotNull(infos);
		assertNotNull(infos.getLongRunningTasks());

		conn.shutdown();	
	}
	
	@Test
	public void testMonitoringInfos() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("configuration").build();
		MonitoringInfosVO databaseInfos = conn.get(systemUri, MonitoringInfosVO.class);
		assertNotNull(databaseInfos);
		assertNotNull(databaseInfos.getDependencies());
		assertNotNull(databaseInfos.getProbes());
		assertNotNull(databaseInfos.getType());

		conn.shutdown();	
	}
	
	@Test
	public void testVFSStats() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("monitoring").path("revisionsSize").build();
		VFSStatsVO revisionsInfos = conn.get(systemUri, VFSStatsVO.class);
		assertNotNull(revisionsInfos);
		assertNotNull(revisionsInfos.getRevisionsSize());
		
		conn.shutdown();
	}
}
