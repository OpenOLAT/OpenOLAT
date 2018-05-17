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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.FlushMode;
import org.junit.Assert;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.core.util.CodeHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.statistic.daily.DailyStat;
import org.olat.course.statistic.dayofweek.DayOfWeekStat;
import org.olat.course.statistic.hourofday.HourOfDayStat;
import org.olat.course.statistic.weekly.WeeklyStat;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 5 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractStatisticUpdateManagerTest extends OlatTestCase {

	private Map<String,Map<String,AtomicInteger>> inMemoryStatistics = new HashMap<>();
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	protected StatisticUpdateManager statisticUpdateManager;
	
	protected void cleanUpLog() {
		String deleteLogQuery = "delete from " + LoggingObject.class.getName() + " log";
		dbInstance.createQuery(deleteLogQuery)
			.executeUpdate(FlushMode.AUTO);
		
		String deleteDailyQuery = "delete from " + DailyStat.class.getName() + " log";
		dbInstance.createQuery(deleteDailyQuery)
			.executeUpdate(FlushMode.AUTO);
		
		String deleteWeeklyQuery = "delete from " + WeeklyStat.class.getName() + " log";
		dbInstance.createQuery(deleteWeeklyQuery)
			.executeUpdate(FlushMode.AUTO);
		
		String deleteDayOfWeekQuery = "delete from " + DayOfWeekStat.class.getName() + " log";
		dbInstance.createQuery(deleteDayOfWeekQuery)
			.executeUpdate(FlushMode.AUTO);
		
		String deleteHourOfDayQuery = "delete from " + HourOfDayStat.class.getName() + " log";
		dbInstance.createQuery(deleteHourOfDayQuery)
			.executeUpdate(FlushMode.AUTO);

		dbInstance.commitAndCloseSession();
	}
	
	protected void incrementInMemoryStatistics(Long repositoryEntryKey, String nodeIdent, String date) {
		String key = repositoryEntryKey + "- "+ nodeIdent;
		
		Map<String,AtomicInteger> node = inMemoryStatistics.get(key);
		if(node == null) {
			node = new HashMap<>();
			inMemoryStatistics.put(key, node);
		}
		
		AtomicInteger counter = node.get(date);
		if(counter == null) {
			counter = new AtomicInteger(0);
			node.put(date, counter);
		}
		counter.incrementAndGet();	
	}
	
	protected Integer getInMemoryStatistics(RepositoryEntry repositoryEntry, CourseNode courseNode, String date) {
		String key = repositoryEntry.getKey() + "- "+ courseNode.getIdent();
		Map<String,AtomicInteger> node = inMemoryStatistics.get(key);
		if(node == null) {
			return null;
		}
		AtomicInteger counter = node.get(date);
		return counter == null ? null : new Integer(counter.get());	
	}
	
	protected void updateStatistics() {
		StatisticsWait countDown = new StatisticsWait();
		statisticUpdateManager.updateStatistics(false, countDown);
		countDown.waitStatistics();
	}
	
	protected void updateAllStatistics() {
		StatisticsWait countDown = new StatisticsWait();
		statisticUpdateManager.updateStatistics(true, countDown);
		countDown.waitStatistics();
	}
	
	protected Calendar addLog(Long repositoryEntryKey, String courseNodeIdent, Calendar start, int dayInPast, int hour, int minute, int second) {
		String sessionId = "session-" + CodeHelper.getGlobalForeverUniqueID();
		LoggingObject logObj = new LoggingObject(sessionId, 123l, "u", "launch", "node");

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, start.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, start.get(Calendar.MONTH));
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		if(dayInPast > 0) {
			cal.add(Calendar.DATE, -dayInPast);
		}

		logObj.setCreationDate(cal.getTime());
		logObj.setResourceAdminAction(Boolean.FALSE);
		logObj.setBusinessPath("[RepositoryEntry:" + repositoryEntryKey + "][CourseNode:" + courseNodeIdent + "]");
		dbInstance.saveObject(logObj);
		return cal;
	}
	
	protected void setLastUpdate(Calendar ref, int dayInThePast) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, ref.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, ref.get(Calendar.MONTH));
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DATE, -dayInThePast);
		long lastUpdated = cal.getTimeInMillis();

		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, null, "STATISTICS_PROPERTIES", "LAST_UPDATED");
		if(p == null) {
			p = pm.createPropertyInstance(null, null, null, "STATISTICS_PROPERTIES", "LAST_UPDATED", null, lastUpdated, null, null);	
		} else {
			p.setLongValue(lastUpdated);
		}
		pm.saveProperty(p);
	}
	
	public static class StatisticsWait implements Runnable {
		
		private CountDownLatch countDown = new CountDownLatch(1);
		
		@Override
		public void run() {
			countDown.countDown();
		}
		
		public void waitStatistics() {
			try {
				countDown.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Assert.fail("Test takes too long (more than 10s)");
			}
		}
	}
}
