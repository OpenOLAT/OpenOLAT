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

import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.restapi.system.vo.ClasseStatisticsVO;
import org.olat.restapi.system.vo.MemoryStatisticsVO;
import org.olat.restapi.system.vo.RuntimeStatisticsVO;
import org.olat.restapi.system.vo.ThreadStatisticsVO;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RuntimeWebService {
	
	private static final int mb = 1024*1024;

	public RuntimeWebService() {
		//make Spring happy
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSystemSummaryVO(@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return null;
		}
		
		RuntimeStatisticsVO stats = new RuntimeStatisticsVO();
		stats.setMemory(getMemoryStatistics());
		stats.setThreads(getThreadStatistics());
		stats.setClasses(getClasseStatistics());
		
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		stats.setSystemLoadAverage(os.getSystemLoadAverage());
		stats.setStartTime(new Date(runtime.getStartTime()));
		stats.setUpTime(runtime.getUptime());

		return Response.ok(stats).build();
	}
	
	@GET
	@Path("memory")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMemoryStatistics(@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return null;
		}
		MemoryStatisticsVO stats = getMemoryStatistics();
		return Response.ok(stats).build();
	}
	
	@GET
	@Path("threads")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getThreadStatistics(@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return null;
		}
		ThreadStatisticsVO stats = getThreadStatistics();
		return Response.ok(stats).build();
	}
	
	/**
	 * Return some informations about the number of Java classes...
	 * @response.representation.200.qname {http://www.example.com}classesVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc A short summary of the number of classes
   * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_CLASSESVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param request The HTTP request
	 * @return The information about the classes
	 */
	@GET
	@Path("classes")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCompilationXml(@Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return null;
		}
		ClasseStatisticsVO stats = getClasseStatistics();
		return Response.ok(stats).build();
	}
	
	private ThreadStatisticsVO getThreadStatistics() {
		ThreadStatisticsVO stats = new ThreadStatisticsVO();
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		stats.setDaemonCount(threadBean.getDaemonThreadCount());
		stats.setThreadCount(threadBean.getThreadCount());
		stats.setPeakThreadCount(threadBean.getPeakThreadCount());
		return stats;
	}
	
	private MemoryStatisticsVO getMemoryStatistics() {
		MemoryStatisticsVO stats = new MemoryStatisticsVO();
		
    Runtime runtime = Runtime.getRuntime();
    stats.setUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / mb);
    stats.setFreeMemory(runtime.freeMemory() / mb);
    stats.setTotalMemory(runtime.totalMemory() / mb);
		return stats;
	}
	
	private ClasseStatisticsVO getClasseStatistics() {
		ClasseStatisticsVO stats = new ClasseStatisticsVO();
		ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
		stats.setLoadedClassCount(bean.getLoadedClassCount());
		stats.setTotalLoadedClassCount(bean.getTotalLoadedClassCount());
		stats.setUnloadedClassCount(bean.getUnloadedClassCount());
		return stats;
	}

	
	

}
