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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.statistic.daily.DailyStatisticManager;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;


/**
 * 
 * Initial date: 5 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyStatisticUpdateManagerTest extends AbstractStatisticUpdateManagerTest {

	private final SimpleDateFormat dailyFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final DailyStatisticManager dailyStatisticManager = new DailyStatisticManager();
	
	@Test
	public void statistics_daily() {
		Assume.assumeTrue(!isOracleConfigured());
		
		statisticUpdateManager.setEnabled(true);
		cleanUpLog();
		
		// create 2 courses
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("log-1");
		
		RepositoryEntry re1 = JunitTestHelper.deployBasicCourse(id);
		ICourse course1 = CourseFactory.loadCourse(re1);
		CourseNode rootNode1 = course1.getRunStructure().getRootNode();
		CourseNode firstNode1 = (CourseNode)course1.getRunStructure().getRootNode().getChildAt(0);
		
		RepositoryEntry re2 = JunitTestHelper.deployBasicCourse(id);
		ICourse course2 = CourseFactory.loadCourse(re2);
		CourseNode rootNode2 = course2.getRunStructure().getRootNode();
		CourseNode firstNode2 = (CourseNode)course2.getRunStructure().getRootNode().getChildAt(0);
		
		// generate some data
		Calendar ref = Calendar.getInstance();
		String date1 = null;
		String date2 = null;
		
		for(int i=0; i<5; i++) {
			addLogEntry(re1, rootNode1, ref, 0, 0, 0, i + 1);
		}
		for(int i=0; i<4;i++) {
			date1 = addLogEntry(re1, firstNode1, ref, 0, 0, 0, i + 1);
		}
		for(int i=0; i<10; i++) {
			addLogEntry(re1, rootNode1, ref, 2, i, 1, 1);
			addLogEntry(re1, firstNode2, ref, 2, i, 1, 1);
		}
		for(int i=0; i<8; i++) {
			addLogEntry(re1, rootNode1, ref, 3, 5 + i, 1, 1);
			date2 = addLogEntry(re2, rootNode2, ref, 2, i, 1, 1);
		}
		
		setLastUpdate(ref, 2);
		dbInstance.commitAndCloseSession();

		updateStatistics();
		
		// first log analyze
		String date = getDayString(ref, ref.get(Calendar.DATE));
		checkStatistics(course1, rootNode1, date);
		checkStatistics(course2, rootNode2, date2);

		// add log the same day
		Calendar now = Calendar.getInstance();
		addLogEntry(re1, rootNode1, ref, 0, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND) + 1);
		addLogEntry(re1, rootNode1, ref, 0, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND) + 2);
		addLogEntry(re2, firstNode2, ref, 0, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND) + 2);
		dbInstance.commitAndCloseSession();
		sleep(5000);

		//update stats incremental
		updateStatistics();
		checkStatistics(course1, rootNode1, date);
		checkStatistics(course1, firstNode1, date);
		checkStatistics(course1, firstNode1, date1);
		checkStatistics(course2, rootNode2, date2);
		
		//update all statistics
		updateAllStatistics();
		checkStatistics(course1, rootNode1, date);
		checkStatistics(course1, firstNode1, date);
		checkStatistics(course1, firstNode1, date1);
		checkStatistics(course2, rootNode2, date2);
	}
	
	private void checkStatistics(ICourse course, CourseNode node, String date) {
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		StatisticResult updatedResult = dailyStatisticManager.generateStatisticResult(new SyntheticUserRequest(null, Locale.ENGLISH), course, re.getKey());
		Map<String,Integer> updatedRootStats = updatedResult.getStatistics(node);
		Integer updated_stats_inMemory = getInMemoryStatistics(re, node, date);
		Integer updated_stats_today = updatedRootStats.get(date);
		Assert.assertEquals(updated_stats_inMemory, updated_stats_today);
	}
	
	private String getDayString(Calendar start, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, start.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, start.get(Calendar.MONTH));
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return dailyFormat.format(cal.getTime()) + " 00:00:00.0";
	}
	
	protected String addLogEntry(RepositoryEntry repositoryEntry, CourseNode courseNode, Calendar start,
			int dayInPast, int hour, int minute, int second) {
		Calendar cal = addLog(repositoryEntry.getKey(), courseNode.getIdent(), start, dayInPast, hour, minute, second);
		String day = getDayString(cal, cal.get(Calendar.DATE));
		incrementInMemoryStatistics(repositoryEntry.getKey(), courseNode.getIdent(), day);
		return day;
	}
}
