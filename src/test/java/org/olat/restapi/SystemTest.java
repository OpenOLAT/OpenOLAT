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

import org.junit.Test;
import org.olat.restapi.system.vo.DatabaseVO;
import org.olat.restapi.system.vo.EnvironmentInformationsVO;
import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.MemoryVO;
import org.olat.restapi.system.vo.OpenOLATStatisticsVO;
import org.olat.restapi.system.vo.ReleaseInfosVO;
import org.olat.restapi.system.vo.RepositoryStatisticsVO;
import org.olat.restapi.system.vo.RuntimeStatisticsVO;
import org.olat.restapi.system.vo.SessionsVO;
import org.olat.restapi.system.vo.ThreadsVO;
import org.olat.restapi.system.vo.UserStatisticsVO;
import org.olat.test.OlatJerseyTestCase;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SystemTest extends OlatJerseyTestCase {
	
	@Test
	public void testRuntimeStatisticsInfos() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("runtime").build();
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
		
		URI systemUri = conn.getContextURI().path("system").path("threads").build();
		ThreadsVO threadInfos = conn.get(systemUri, ThreadsVO.class);

		assertNotNull(threadInfos);
		assertTrue(threadInfos.getDaemonCount() > 0);
		assertTrue(threadInfos.getThreadCount() > 0);
		
		conn.shutdown();	
	}

	@Test
	public void testSystemMemory() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("memory").build();
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
		
		URI systemUri = conn.getContextURI().path("system").path("openolat").path("sessions").build();
		SessionsVO sessionInfos = conn.get(systemUri, SessionsVO.class);

		assertNotNull(sessionInfos);
		assertTrue(sessionInfos.getCount() > 0);
		assertTrue(sessionInfos.getAuthenticatedCount() >= 0);
		assertTrue(sessionInfos.getInstantMessagingCount() >= 0);
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
		
		URI systemUri = conn.getContextURI().path("system").path("openolat").path("users").build();
		UserStatisticsVO userStats = conn.get(systemUri, UserStatisticsVO.class);

		assertNotNull(userStats);
		assertTrue(userStats.getTotalUserCount() > 0);
		assertTrue(userStats.getTotalGroupCount() >= 0);
		
		conn.shutdown();	
	}
	
	@Test
	public void testSystemRepositoryStatistics() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("openolat").path("repository").build();
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
		
		URI systemUri = conn.getContextURI().path("system").path("openolat").path("indexer").build();
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
		
		URI systemUri = conn.getContextURI().path("system").path("openolat").build();
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
		
		URI systemUri = conn.getContextURI().path("system").path("runtime").build();
		RuntimeStatisticsVO runInfos = conn.get(systemUri, RuntimeStatisticsVO.class);

		assertNotNull(runInfos);
		assertNotNull(runInfos.getStartTime());
		assertTrue(runInfos.getSystemLoadAverage() > 0.0d);
		assertTrue(runInfos.getUpTime() > 0);
		
		System.out.println(runInfos);
		
		conn.shutdown();	
	}
	
	@Test
	public void testUpdate() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("release").build();
		ReleaseInfosVO versionInfos = conn.get(systemUri, ReleaseInfosVO.class);

		assertNotNull(versionInfos);
		assertNotNull(versionInfos.getBuildVersion());
		assertNotNull(versionInfos.getOlatVersion());

		conn.shutdown();	
	}
	
	@Test
	public void testDatabase() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI systemUri = conn.getContextURI().path("system").path("database").build();
		DatabaseVO databaseInfos = conn.get(systemUri, DatabaseVO.class);
		assertNotNull(databaseInfos);
		assertNotNull(databaseInfos.getConnectionInfos());
		assertNotNull(databaseInfos.getHibernateStatistics());

		conn.shutdown();	
	}
}
