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
package org.olat.modules.coach.ui.manager;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.manager.PersistentTaskDAO;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.reports.AbstractReportTask;
import org.olat.modules.coach.reports.ReportParameters;
import org.olat.modules.coach.reports.ReportTimeRange;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CoachingReportTaskTest extends OlatTestCase {

	private static final String COACHING_REPORT_IDENT = "coaching";

	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportManager exportManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;

	@Test
	public void serializeWithoutTemplate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("report-task-1");
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.custom);
		params.setFrom(DateUtils.addDays(new Date(), -10));
		params.setTo(new Date());
		CoachingReportTask task = new CoachingReportTask("Report", id, Locale.ENGLISH, "coaching.absences", params);

		PersistentTask persistentTask = persistentTaskDao.createTask("report-task-1", task);
		dbInstance.commitAndCloseSession();

		PersistentTask reloadedTask = persistentTaskDao.loadTaskById(persistentTask.getKey());
		Assert.assertFalse(reloadedTask.getTask().contains("AbsencesReportConfiguration"));

		Runnable runnable = persistentTaskDao.deserializeTask(reloadedTask);
		Assert.assertTrue(runnable instanceof CoachingReportTask);
		CoachingReportTask reloadedReportTask = (CoachingReportTask)runnable;
		Assert.assertEquals("coaching.absences", reloadedReportTask.getTemplateId());
		Assert.assertEquals(ReportTimeRange.custom, reloadedReportTask.getParameters().getTimeRange());
		Assert.assertEquals(params.getFrom(), reloadedReportTask.getParameters().getFrom());
		Assert.assertEquals(params.getTo(), reloadedReportTask.getParameters().getTo());
	}

	@Test
	public void runReport() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("report-task-2");
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.last4Weeks);
		ExportMetadata metadata = startReport(coach, "coaching.certificates", params);

		waitForTaskEnd(metadata);

		// Done tasks are deleted
		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByKey(metadata.getKey());
		Assert.assertNull(reloadedMetadata.getTask());
		LocalFileImpl file = VFSManager.olatRootLeaf(reloadedMetadata.getFilePath());
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.getSize() > 0);
	}

	@Test
	public void runReportUnknownTemplate() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("report-task-3");
		ExportMetadata metadata = startReport(coach, "coaching.unknown", new ReportParameters());

		PersistentTask task = waitForTaskEnd(metadata);
		Assert.assertNotNull(task);
		Assert.assertEquals(TaskStatus.failed, task.getStatus());
	}

	@Test
	public void runReportFailedDeletesFile() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("report-task-4");
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.timePeriod);
		params.setTimePeriodKey(Long.valueOf(-1l)); // Time period doesn't exist
		ExportMetadata metadata = startReport(coach, "coaching.absences", params);

		PersistentTask task = waitForTaskEnd(metadata);
		Assert.assertNotNull(task);
		Assert.assertEquals(TaskStatus.failed, task.getStatus());

		// No partial file left (the folder can contain files of former test runs with the same identity key)
		LocalFolderImpl folder = coachingService.getGeneratedReportsFolder(coach);
		Assert.assertNull(folder.resolve(metadata.getFilename()));
	}

	private ExportMetadata startReport(Identity coach, String templateId, ReportParameters params) {
		String fileName = "Report_" + templateId + "_" + System.nanoTime() + ".xlsx";
		Date expirationDate = DateUtils.addDays(new Date(), AbstractReportTask.RETENTION_DAYS);
		CoachingReportTask task = new CoachingReportTask("Report", coach, Locale.ENGLISH, templateId, params);
		ExportMetadata metadata = exportManager.startExport(task, "Report", "Description", fileName, ArchiveType.COACHING,
				expirationDate, false, COACHING_REPORT_IDENT, coach);
		dbInstance.commitAndCloseSession();
		return metadata;
	}

	/**
	 * @return The task if it failed, null if it is done (done tasks are deleted)
	 */
	private PersistentTask waitForTaskEnd(ExportMetadata metadata) {
		Long taskKey = metadata.getTask().getKey();
		boolean ended = waitForCondition(() -> {
			PersistentTask task = persistentTaskDao.loadTaskById(taskKey);
			dbInstance.commitAndCloseSession();
			return task == null || task.getStatus() == TaskStatus.failed;
		}, 30000);
		Assert.assertTrue(ended);

		PersistentTask task = persistentTaskDao.loadTaskById(taskKey);
		dbInstance.commitAndCloseSession();
		return task;
	}
}
