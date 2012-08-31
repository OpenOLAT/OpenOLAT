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
	public static final MemoryVO SAMPLE_MEMORYVO = new MemoryVO();
	public static final SessionsVO SAMPLE_SESSIONVO = new SessionsVO();
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
		
		SAMPLE_RUNTIMEVO.setStartTime(new Date());
		SAMPLE_RUNTIMEVO.setSystemLoadAverage(1.16748046875d);
		SAMPLE_RUNTIMEVO.setUpTime(21248);
		
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
		
		SAMPLE_RELEASEVO.setBuildVersion("");
		SAMPLE_RELEASEVO.setOlatVersion("");
		SAMPLE_RELEASEVO.setRepoRevision("");
		SAMPLE_RELEASEVO.setAllowAutoPatch(true);
		SAMPLE_RELEASEVO.setAllowAutoUpdate(false);
		SAMPLE_RELEASEVO.setPatchAvailable(true);
		SAMPLE_RELEASEVO.setUpdateAvailable(false);
		SAMPLE_RELEASEVO.setUpgradeAvailable(false);
	}
}
