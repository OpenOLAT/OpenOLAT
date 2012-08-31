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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WorkThreadInformations;
import org.olat.restapi.system.vo.ThreadVO;
import org.olat.restapi.system.vo.ThreadsVO;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ThreadsWebService implements Sampler {
	
	private static final OLog log = Tracing.createLoggerFor(ThreadsWebService.class);
	
	private final static NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.ENGLISH);
	
	private long prevUpTime;
	private Map<Long,ThreadVO> threadMap = new HashMap<Long,ThreadVO>();

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getThreads() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadsVO vo = new ThreadsVO(bean);
		return Response.ok(vo.toString()).build();
	}
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getThreadsXml() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadsVO vo = new ThreadsVO(bean);
		return Response.ok(vo).build();
	}
	
	@GET
	@Path("cpu")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public synchronized Response getThreadsCpu() {
		List<ThreadVO> threads = new ArrayList<ThreadVO>(threadMap.values());
		Collections.sort(threads);
		ThreadVO[] threadVos = threads.toArray(new ThreadVO[threads.size()]);
		return Response.ok(threadVos).build();
	}
	
  @Override
	public synchronized void takeSample() {
    	updateTimeSeries();
	}

	private void updateTimeSeries() {
		ThreadMXBean threadProxy = ManagementFactory.getThreadMXBean();
		RuntimeMXBean runtimeProxy  = ManagementFactory.getRuntimeMXBean();
		ThreadInfo tis[] = threadProxy.dumpAllThreads(false, false);
		
		List<String> currentThreadNames = new ArrayList<String>();
		
		Set<Long> currentThreadIds = new HashSet<Long>();
		for (ThreadInfo ti : tis) {
			if (!threadMap.containsKey(ti.getThreadId())) {
				ThreadVO threadVO = new ThreadVO();
				threadVO.setId(ti.getThreadId());
				threadVO.setName(ti.getThreadName());
				threadMap.put(ti.getThreadId(), threadVO);
			}
			currentThreadIds.add(ti.getThreadId());
		}
		WorkThreadInformations.currentThreadNames(currentThreadNames);
		
		for (ThreadVO threadVO:threadMap.values()) {
			threadVO.setPrevCpuTime(Math.max(0, threadVO.getCpuTime()));
			threadVO.setCpuTime(Math.max(0, threadProxy.getThreadCpuTime(threadVO.getId())));
		}
		
		long upTime = runtimeProxy.getUptime();
		if (prevUpTime > 0L && upTime > prevUpTime) {
			// elapsedTime is in ms
			long elapsedTime = upTime - prevUpTime;
			for (ThreadVO threadVO:threadMap.values()) {
				// elapsedCpu is in ns
				long elapsedCpu = threadVO.getCpuTime() - threadVO.getPrevCpuTime();
				// cpuUsage could go higher than 100% because elapsedTime
				// and elapsedCpu are not fetched simultaneously. Limit to
				// 99% to avoid Chart showing a scale from 0% to 200%.
				float cpuUsage = Math.min(99f, elapsedCpu / (elapsedTime * 1000000F));
				threadVO.setCpuUsage(cpuUsage);
				threadVO.setCpuUsagePercent(percentFormat.format(cpuUsage));
				
				if(cpuUsage > 0.8) {
					threadVO.setWarningCounter(threadVO.getWarningCounter() + 1);
					if(threadVO.getWarningCounter() >= 2) {
						String currentWork = WorkThreadInformations.get(threadVO.getName());
						if(currentWork == null) {
							currentWork = "unkown";
						}
						log.info("High usage on thread:" + threadVO + " because thread work at: " + currentWork);
					}
				} else {
					threadVO.setWarningCounter(0);
				}
			}
		}
		prevUpTime = upTime;
		
		//clean-up closed threads
		for (Iterator<Map.Entry<Long,ThreadVO>> it=threadMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Long,ThreadVO> entry = it.next();
			if(!currentThreadIds.contains(entry.getKey())) {
				it.remove();
			}
		}
	}
}
