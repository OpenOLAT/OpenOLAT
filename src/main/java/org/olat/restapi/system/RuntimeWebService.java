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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.restapi.system.vo.ClasseStatisticsVO;
import org.olat.restapi.system.vo.MemoryStatisticsVO;
import org.olat.restapi.system.vo.RuntimeStatisticsVO;
import org.olat.restapi.system.vo.ThreadStatisticsVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RuntimeWebService {
	
	private static final int mb = 1024*1024;

	public RuntimeWebService() {
		//make Spring happy
	}

	/**
	 * Return the statistics about runtime: uptime, classes loaded, memory
	 * summary, threads count...
	 * 
   * @param request The HTTP request
	 * @return The informations about runtime, uptime, classes loaded, memory summary...
	 */
	@GET
	@Operation(summary = "Return the statistics about runtime", description = "Return the statistics about runtime: uptime, classes loaded, memory\n" + 
			" summary, threads count...")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RuntimeStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RuntimeStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getSystemSummaryVO() {
		RuntimeStatisticsVO stats = new RuntimeStatisticsVO();
		stats.setMemory(getMemoryStatisticsVO());
		stats.setThreads(getThreadStatisticsVO());
		stats.setClasses(getClasseStatisticsVO());
		
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		stats.setSystemLoadAverage(os.getSystemLoadAverage());
		stats.setStartTime(new Date(runtime.getStartTime()));
		stats.setUpTime(runtime.getUptime());

		return Response.ok(stats).build();
	}
	
	/**
	 * Return the statistics about memory
	 * 
   * @param request The HTTP request
	 * @return The informations about runtime, uptime, classes loaded, memory summary...
	 */
	@GET
	@Path("memory")
	@Operation(summary = "Return the statistics about memory", description = "Return the statistics about memory")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = MemoryStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = MemoryStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMemoryStatistics() {
		MemoryStatisticsVO stats = getMemoryStatisticsVO();
		return Response.ok(stats).build();
	}
	
	/**
	 * Return the statistics about threads
	 * 
   * @param request The HTTP request
	 * @return The informations about threads count
	 */
	@GET
	@Path("threads")
	@Operation(summary = "Return the statistics about threads", description = "Return the statistics about threads")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ThreadStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ThreadStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getThreadStatistics() {
		ThreadStatisticsVO stats = getThreadStatisticsVO();
		return Response.ok(stats).build();
	}
	
	/**
	 * Return some informations about the number of Java classes...
	 * 
   * @param request The HTTP request
	 * @return The information about the classes
	 */
	@GET
	@Path("classes")
	@Operation(summary = "Return some informations about the number of Java classes", description = "Return some informations about the number of Java classes...")
	@ApiResponse(responseCode = "200", description = "A short summary of the number of classes", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ClasseStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ClasseStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCompilationXml() {
		ClasseStatisticsVO stats = getClasseStatisticsVO();
		return Response.ok(stats).build();
	}
	
	private ThreadStatisticsVO getThreadStatisticsVO() {
		ThreadStatisticsVO stats = new ThreadStatisticsVO();
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		stats.setDaemonCount(threadBean.getDaemonThreadCount());
		stats.setThreadCount(threadBean.getThreadCount());
		stats.setPeakThreadCount(threadBean.getPeakThreadCount());
		return stats;
	}
	
	private MemoryStatisticsVO getMemoryStatisticsVO() {
		MemoryStatisticsVO stats = new MemoryStatisticsVO();
		
    Runtime runtime = Runtime.getRuntime();
    stats.setUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / mb);
    stats.setFreeMemory(runtime.freeMemory() / mb);
    stats.setTotalMemory(runtime.totalMemory() / mb);

    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    stats.setInitHeap(memoryBean.getHeapMemoryUsage().getInit() / mb);
    stats.setInitNonHeap(memoryBean.getNonHeapMemoryUsage().getInit() / mb);
    stats.setUsedHeap(memoryBean.getHeapMemoryUsage().getUsed() / mb);
    stats.setUsedNonHeap(memoryBean.getNonHeapMemoryUsage().getUsed() / mb);
    stats.setCommittedHeap(memoryBean.getHeapMemoryUsage().getCommitted() / mb);
    stats.setCommittedNonHeap(memoryBean.getNonHeapMemoryUsage().getCommitted() / mb);
    stats.setMaxHeap(memoryBean.getHeapMemoryUsage().getMax() / mb);
    stats.setMaxNonHeap(memoryBean.getNonHeapMemoryUsage().getMax() / mb);
    
    long collectionTime = 0l;
    long collectionCount = 0l;
    List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    for(GarbageCollectorMXBean gcBean:gcBeans) {
    	collectionCount += gcBean.getCollectionCount();
    	collectionTime += gcBean.getCollectionTime();
    }
    stats.setGarbageCollectionCount(collectionCount);
    stats.setGarbageCollectionTime(collectionTime);

		return stats;
	}
	
	private ClasseStatisticsVO getClasseStatisticsVO() {
		ClasseStatisticsVO stats = new ClasseStatisticsVO();
		ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
		stats.setLoadedClassCount(bean.getLoadedClassCount());
		stats.setTotalLoadedClassCount(bean.getTotalLoadedClassCount());
		stats.setUnloadedClassCount(bean.getUnloadedClassCount());
		return stats;
	}
}