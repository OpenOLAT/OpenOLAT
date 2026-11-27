/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskDAO;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.util.DateUtils;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The step "REPORT TASKS FAILED" of {@link OLATUpgrade_21_2_0}.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class OLATUpgrade_21_2_0ReportTasksTest extends OlatTestCase {

	/** The XML of a task class which doesn't exist anymore */
	private static final String UNREADABLE_XML = "<org.olat.modules.coach.ui.manager.RemovedReportTask/>";
	/** The XML of a coaching report task saved before the update, with the removed fields identityKey and config */
	private static final String OLD_COACHING_XML = """
			<org.olat.modules.coach.ui.manager.CoachingReportTask>
			  <title>Report</title>
			  <identityKey>1</identityKey>
			  <locale>en</locale>
			  <config class="org.olat.modules.coach.reports.AbsencesReportConfiguration"/>
			</org.olat.modules.coach.ui.manager.CoachingReportTask>""";
	private static final String READABLE_XML = "<org.olat.modules.coach.ui.manager.CoachingReportTask/>";

	@Autowired
	private DB dbInstance;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;

	@Test
	public void markReportTasksFailed() {
		PersistentTask coachingTask = createTask("coaching", UNREADABLE_XML, TaskStatus.newTask);
		PersistentTask curriculumTask = createTask("curriculum", UNREADABLE_XML, TaskStatus.inWork);
		PersistentTask failedTask = createTask("coaching", UNREADABLE_XML, TaskStatus.failed);
		PersistentTask otherTask = createTask("not-a-report", UNREADABLE_XML, TaskStatus.newTask);
		PersistentTask validTask = createTask("coaching", READABLE_XML, TaskStatus.inWork);
		PersistentTask oldCoachingTask = createTask("coaching", OLD_COACHING_XML, TaskStatus.newTask);
		dbInstance.commitAndCloseSession();

		OLATUpgrade_21_2_0 upgrade = new OLATUpgrade_21_2_0();
		CoreSpringFactory.autowireObject(upgrade);
		int count = upgrade.markReportTasksFailed();
		Assert.assertTrue(count >= 3);

		Assert.assertEquals(TaskStatus.failed, persistentTaskDao.loadTaskById(coachingTask.getKey()).getStatus());
		Assert.assertEquals(TaskStatus.failed, persistentTaskDao.loadTaskById(curriculumTask.getKey()).getStatus());
		Assert.assertEquals(TaskStatus.failed, persistentTaskDao.loadTaskById(failedTask.getKey()).getStatus());
		Assert.assertEquals(TaskStatus.newTask, persistentTaskDao.loadTaskById(otherTask.getKey()).getStatus());
		Assert.assertEquals(TaskStatus.inWork, persistentTaskDao.loadTaskById(validTask.getKey()).getStatus());
		Assert.assertEquals(TaskStatus.failed, persistentTaskDao.loadTaskById(oldCoachingTask.getKey()).getStatus());
		// The XML is read but never changed
		Assert.assertEquals(UNREADABLE_XML, persistentTaskDao.loadTaskById(coachingTask.getKey()).getTask());
		Assert.assertEquals(OLD_COACHING_XML, persistentTaskDao.loadTaskById(oldCoachingTask.getKey()).getTask());
	}

	/**
	 * The task is scheduled in the future, so the task executor ignores it.
	 */
	private PersistentTask createTask(String resSubPath, String taskXml, TaskStatus status) {
		Date scheduledDate = DateUtils.addDays(new Date(), 30);
		PersistentTask task = persistentTaskDao.createTask("report-upgrade", "Placeholder", null, null, resSubPath, scheduledDate);
		task.setTask(taskXml);
		task.setStatus(status);
		return dbInstance.getCurrentEntityManager().merge(task);
	}
}
