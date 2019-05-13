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
package org.olat.admin.sysinfo.manager;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.admin.sysinfo.model.ThreadView;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WorkThreadInformations;
import org.olat.restapi.system.Sampler;
import org.springframework.stereotype.Service;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@Service
public class ThreadInfosManager implements Sampler {
	
	private static final Logger log = Tracing.createLoggerFor(ThreadInfosManager.class);
	
	private final static NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.ENGLISH);
	
	private long prevUpTime;
	private Map<Long,ThreadView> threadMap = new ConcurrentHashMap<Long,ThreadView>();

	public List<ThreadView> getThreadViews() {
		if(threadMap.isEmpty()) {
			takeSample();
		}

		List<ThreadView> threads = new ArrayList<ThreadView>(threadMap.values());
		Collections.sort(threads);
		return threads;
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
			Long threadId = new Long(ti.getThreadId());
			if (threadMap.containsKey(threadId)) {
				ThreadView threadVO = threadMap.get(threadId);
				threadVO.setState(ti.getThreadState());
			} else {
				ThreadView threadVO = new ThreadView();
				threadVO.setId(threadId);
				threadVO.setName(ti.getThreadName());
				threadVO.setState(ti.getThreadState());
				threadMap.put(threadId, threadVO);
			}
			currentThreadIds.add(threadId);
		}
		WorkThreadInformations.currentThreadNames(currentThreadNames);
		
		for (ThreadView threadVO:threadMap.values()) {
			threadVO.setPrevCpuTime(Math.max(0, threadVO.getCpuTime()));
			threadVO.setCpuTime(Math.max(0, threadProxy.getThreadCpuTime(threadVO.getId())));
		}
		
		long upTime = runtimeProxy.getUptime();
		if (prevUpTime > 0L && upTime > prevUpTime) {
			// elapsedTime is in ms
			long elapsedTime = upTime - prevUpTime;
			for (ThreadView threadVO:threadMap.values()) {
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
		for (Iterator<Map.Entry<Long,ThreadView>> it=threadMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Long,ThreadView> entry = it.next();
			if(!currentThreadIds.contains(entry.getKey())) {
				it.remove();
			}
		}
	}
}
