/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.jmx;

import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.scheduler.JobWithDB;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * Description:<br>
 * a job which regularly dumps jmx data to the log
 * 
 * @author Felix Jost
 */
public class DumpJMXJob extends JobWithDB {
	
	@Override
	public void executeWithDB(JobExecutionContext context)
			throws JobExecutionException {
		boolean enabled = context.getMergedJobDataMap().getBooleanFromString("enabled");
		String[] keys = context.getMergedJobDataMap().getKeys();
		// loop over all 
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (key.endsWith("Bean")) {
				// ok, key is a bean name => dump this bean
				String beanName = context.getMergedJobDataMap().getString(key);
				if (enabled) {
					List<String> jmxDumpList = JMXManager.getInstance().dumpJmx(beanName);
					StringBuilder buf = new StringBuilder();
					for (Iterator iterator = jmxDumpList.iterator(); iterator.hasNext();) {
						String jmxDump = (String) iterator.next();
						buf.append(jmxDump);
						buf.append(";");
					}
					log.info(key + ":" + buf.toString());
				}
				
			}
		}
	}

}
