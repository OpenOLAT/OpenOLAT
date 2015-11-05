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
package org.olat.restapi.system.vo;

import java.util.Date;

import org.olat.admin.sysinfo.model.DatabaseConnectionVO;

/**
 * 
 * Description:<br>
 * Examples for the REST API documentation
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Examples {
	
	public static final SystemInfosVO SAMPLE_SYSTEMSUMMARYVO = new SystemInfosVO();
	public static final ClasseStatisticsVO SAMPLE_CLASSESVO = new ClasseStatisticsVO();
	public static final EnvironmentInformationsVO SAMPLE_ENVVO = new EnvironmentInformationsVO();
	public static final RuntimeStatisticsVO SAMPLE_RUNTIMEVO = new RuntimeStatisticsVO();
	public static final ClasseStatisticsVO SAMPLE_RUNTIME_CLASSESVO = new ClasseStatisticsVO();
	public static final ThreadStatisticsVO SAMPLE_RUNTIME_THREADSVO = new ThreadStatisticsVO();
	public static final MemoryStatisticsVO SAMPLE_RUNTIME_MEMORYVO = new MemoryStatisticsVO();
	
	public static final DatabaseConnectionVO SAMPLE_DATABASE_CONNECTIONSVO = new DatabaseConnectionVO();
	public static final HibernateStatisticsVO SAMPLE_DATABASE_HIBERNATEVO = new HibernateStatisticsVO();
	public static final DatabaseVO SAMPLE_DATABASEVO = new DatabaseVO();
	
	public static final OpenOLATStatisticsVO SAMPLE_OO_STATSVO = new OpenOLATStatisticsVO();
	public static final IndexerStatisticsVO SAMPLE_OO_INDEXERSTATSVO = new IndexerStatisticsVO();
	public static final RepositoryStatisticsVO SAMPLE_OO_REPOSTATSVO = new RepositoryStatisticsVO();
	public static final UserStatisticsVO SAMPLE_OO_USERSSTATSVO = new UserStatisticsVO();
	public static final SessionsVO SAMPLE_SESSIONVO = new SessionsVO();
	
	public static final MonitoringInfosVO SAMPLE_MONITORINGCONFIGVO = new MonitoringInfosVO();

	public static final MemoryVO SAMPLE_MEMORYVO = new MemoryVO();
	public static final ReleaseInfosVO SAMPLE_RELEASEVO = new ReleaseInfosVO();
	
	static {
		
		SAMPLE_CLASSESVO.setLoadedClassCount(2345);
		SAMPLE_CLASSESVO.setTotalLoadedClassCount(3947);
		SAMPLE_CLASSESVO.setUnloadedClassCount(2939);
		
		SAMPLE_ENVVO.setArch("x86_64");
		SAMPLE_ENVVO.setAvailableProcessors(4);
		SAMPLE_ENVVO.setOsName("Mac OS X");
		SAMPLE_ENVVO.setOsVersion("10.7.2");
		SAMPLE_ENVVO.setRuntimeName("15261@agam.local");
		SAMPLE_ENVVO.setVmName("Java HotSpot(TM) 64-Bit Server VM");
		SAMPLE_ENVVO.setVmVendor("Apple Inc.");
		SAMPLE_ENVVO.setVmVersion("20.4-b02-402");
		
		SAMPLE_RUNTIME_CLASSESVO.setLoadedClassCount(7000);
		SAMPLE_RUNTIME_CLASSESVO.setTotalLoadedClassCount(8500);
		SAMPLE_RUNTIME_CLASSESVO.setUnloadedClassCount(1500);
		
		SAMPLE_RUNTIME_THREADSVO.setDaemonCount(45);
		SAMPLE_RUNTIME_THREADSVO.setPeakThreadCount(123);
		SAMPLE_RUNTIME_THREADSVO.setThreadCount(102);
		
		SAMPLE_RUNTIME_MEMORYVO.setFreeMemory(45);
		SAMPLE_RUNTIME_MEMORYVO.setTotalMemory(56);
		SAMPLE_RUNTIME_MEMORYVO.setUsedMemory(12);
		
		SAMPLE_RUNTIMEVO.setStartTime(new Date());
		SAMPLE_RUNTIMEVO.setSystemLoadAverage(1.16748046875d);
		SAMPLE_RUNTIMEVO.setUpTime(21248);
		SAMPLE_RUNTIMEVO.setClasses(SAMPLE_RUNTIME_CLASSESVO);
		SAMPLE_RUNTIMEVO.setMemory(SAMPLE_RUNTIME_MEMORYVO);
		SAMPLE_RUNTIMEVO.setThreads(SAMPLE_RUNTIME_THREADSVO);

		SAMPLE_DATABASE_CONNECTIONSVO.setActiveConnectionCount(10);
		SAMPLE_DATABASE_CONNECTIONSVO.setCurrentConnectionCount(25);
		
		SAMPLE_DATABASE_HIBERNATEVO.setFailedTransactionsCount(2);
		SAMPLE_DATABASE_HIBERNATEVO.setOpenSessionsCount(12);
		SAMPLE_DATABASE_HIBERNATEVO.setOptimisticFailureCount(23);
		SAMPLE_DATABASE_HIBERNATEVO.setQueryExecutionCount(1237);
		SAMPLE_DATABASE_HIBERNATEVO.setQueryExecutionMaxTime(12000);
		SAMPLE_DATABASE_HIBERNATEVO.setQueryExecutionMaxTimeQueryString("select * from PLock");
		SAMPLE_DATABASE_HIBERNATEVO.setSuccessfulTransactionCount(13980);
		SAMPLE_DATABASE_HIBERNATEVO.setTransactionsCount(13900);
		
		SAMPLE_DATABASEVO.setConnectionInfos(SAMPLE_DATABASE_CONNECTIONSVO);
		SAMPLE_DATABASEVO.setHibernateStatistics(SAMPLE_DATABASE_HIBERNATEVO);
		
		SAMPLE_MEMORYVO.setDate(new Date());
		SAMPLE_MEMORYVO.setMaxAvailable(2000);
		SAMPLE_MEMORYVO.setTotalMem(230);
		SAMPLE_MEMORYVO.setTotalUsed(546);
		
		SAMPLE_SESSIONVO.setAuthenticatedCount(234);
		SAMPLE_SESSIONVO.setCount(234);
		SAMPLE_SESSIONVO.setInstantMessagingCount(123);
		SAMPLE_SESSIONVO.setSecureAuthenticatedCount(234);
		SAMPLE_SESSIONVO.setSecureWebdavCount(12);
		SAMPLE_SESSIONVO.setWebdavCount(23);
		
		SAMPLE_MONITORINGCONFIGVO.setDescription("this is an OpenOLAT instance");
		SAMPLE_MONITORINGCONFIGVO.setType("openolat");
		SAMPLE_MONITORINGCONFIGVO.setProbes(new String[]{"Environment", "System", "Runtime", "Memory"});
		
		MonitoringDependencyVO dep1 = new MonitoringDependencyVO();
		dep1.setType("openfire");
		dep1.setUrl("localhost");
		MonitoringDependencyVO dep2 = new MonitoringDependencyVO();
		dep2.setType("mysql");
		dep2.setUrl("192.168.1.120");
		SAMPLE_MONITORINGCONFIGVO.setDependencies(new MonitoringDependencyVO[] {dep1, dep2});
		
		SAMPLE_RELEASEVO.setBuildVersion("");
		SAMPLE_RELEASEVO.setOlatVersion("");
		SAMPLE_RELEASEVO.setRepoRevision("");
	}
}
