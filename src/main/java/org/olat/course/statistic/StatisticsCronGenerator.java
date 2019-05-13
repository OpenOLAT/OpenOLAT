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
package org.olat.course.statistic;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.quartz.CronExpression;
import org.springframework.beans.factory.FactoryBean;

/**
 * You can set a fix value for the cron expression, or let it calculated by
 * the generator. The generated value start at 2:00
 * 
 * 
 * Initial date: 15.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticsCronGenerator implements FactoryBean<String> {
	
	private static final Logger log = Tracing.createLoggerFor(StatisticsCronGenerator.class);
	
	private int tomcatId;
	private String cronExpression;
	
	/**
	 * [used by Spring]
	 * @param tomcatId
	 */
	public void setTomcatId(int tomcatId) {
		this.tomcatId = tomcatId;
	}

	
	/**
	 * [used by Spring]
	 * @param cronExpression
	 */
	public void setCronExpression(String cronExpression) {
		if (CronExpression.isValidExpression(cronExpression)) {
			this.cronExpression = cronExpression;			
		} else {
			if (StringHelper.containsNonWhitespace(cronExpression)) {
				// was not empty, so someone tried to set someting here, let user know that it was garbage
				log.warn("Configured cron expression is not valid::"
						+ cronExpression
						+ " check your search.indexing.cronjob.expression property");
			}
			this.cronExpression = null;
		}
	}
	
	public String getCron() {
		if (cronExpression != null) {
			return cronExpression;
		}
		
		String cron = null;
		if(tomcatId >= 980) {
			int shift = 360 + ((tomcatId - 980) * 2);
			long hours = TimeUnit.MINUTES.toHours(shift);
			long remainMinute = shift - TimeUnit.HOURS.toMinutes(hours);
			cron = "0 " + remainMinute + " " + hours + " * * ? *";
		} else {
			int shift = 120 + (tomcatId * 2);
			long hours = TimeUnit.MINUTES.toHours(shift);
			long remainMinute = shift - TimeUnit.HOURS.toMinutes(hours);
			cron = "0 " + remainMinute + " " + hours + " * * ? *";
		}
		if (CronExpression.isValidExpression(cron)) {
			return cron;
		}
		return "0 10 5 * * ?";
	}

	@Override
	public String getObject() throws Exception {
		return getCron();
	}


	@Override
	public Class<?> getObjectType() {
		return String.class;
	}


	@Override
	public boolean isSingleton() {
		return true;
	}
}