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

import org.olat.core.CoreSpringFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 
 * <h3>Description:</h3>
 * This a quartz job which take a sample for the monitoring system
 * every 15 seconds.
 * 
 * <p>
 * <p>
 * Initial Date:  21 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@DisallowConcurrentExecution
public class SamplerJob extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext context)  {
		SystemWebService systemWebService = CoreSpringFactory.getImpl(SystemWebService.class);
		if(systemWebService != null) {// To make sure the service are loaded with Spring
			MonitoringWebService.takeSample();
		}
	}
}