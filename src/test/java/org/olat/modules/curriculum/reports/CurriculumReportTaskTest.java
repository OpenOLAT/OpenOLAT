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
package org.olat.modules.curriculum.reports;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
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
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.coach.reports.AbstractReportTask;
import org.olat.modules.coach.reports.ReportParameters;
import org.olat.modules.coach.reports.ReportTimeRange;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.reports.CurriculumReportsListController;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CurriculumReportTaskTest extends OlatTestCase {

	private static final String BOOKING_ORDERS = "courseplanner.bookingorders";

	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportManager exportManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private PersistentTaskDAO persistentTaskDao;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void serializeWithoutTemplate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-report-task-1");
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.lastFiscalYear);
		params.setChangedSince(new Date());
		CurriculumReportTask task = new CurriculumReportTask("Report", null, null, id, Locale.ENGLISH, BOOKING_ORDERS, params);

		PersistentTask persistentTask = persistentTaskDao.createTask("cur-report-task-1", task);
		dbInstance.commitAndCloseSession();

		PersistentTask reloadedTask = persistentTaskDao.loadTaskById(persistentTask.getKey());
		Assert.assertFalse(reloadedTask.getTask().contains("AccountingReportConfiguration"));

		Runnable runnable = persistentTaskDao.deserializeTask(reloadedTask);
		Assert.assertTrue(runnable instanceof CurriculumReportTask);
		CurriculumReportTask reloadedReportTask = (CurriculumReportTask)runnable;
		Assert.assertEquals(BOOKING_ORDERS, reloadedReportTask.getTemplateId());
		Assert.assertEquals(ReportTimeRange.lastFiscalYear, reloadedReportTask.getParameters().getTimeRange());
		Assert.assertEquals(params.getChangedSince(), reloadedReportTask.getParameters().getChangedSince());
	}

	/**
	 * The report of an implementation, started without its curriculum.
	 */
	@Test
	public void runReportImplementation() throws IOException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-report-task-2");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-REPORT-2", "Report curriculum", "Curriculum",
				false, organisationService.getDefaultOrganisation());
		CurriculumElement element = curriculumService.createCurriculumElement("IMPL-REPORT-2", "Report implementation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.all);
		ExportMetadata metadata = startReport(doer, null, element, BOOKING_ORDERS, params);

		PersistentTask task = waitForTaskEnd(metadata);
		Assert.assertNull(task);// Done tasks are deleted

		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByKey(metadata.getKey());
		LocalFileImpl file = VFSManager.olatRootLeaf(reloadedMetadata.getFilePath());
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.getSize() > 0);
		
		// The worksheet with the additional information
		String content = readXlsx(file);
		Assert.assertTrue(content.contains("name=\"Additional information\""));
		Assert.assertTrue(content.contains("Course Planner"));
		Assert.assertTrue(content.contains("Report curriculum [CUR-REPORT-2]/Report implementation [IMPL-REPORT-2]"));
		Assert.assertTrue(content.contains("All time"));
	}

	@Test
	public void runReportNotCurriculumTemplate() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-report-task-3");
		ExportMetadata metadata = startReport(doer, null, null, "coaching.absences", new ReportParameters());

		PersistentTask task = waitForTaskEnd(metadata);
		Assert.assertNotNull(task);
		Assert.assertEquals(TaskStatus.failed, task.getStatus());
	}

	@Test
	public void runReportFailed() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-report-task-4");
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.timePeriod);
		params.setTimePeriodKey(Long.valueOf(-1l)); // Time period doesn't exist
		ExportMetadata metadata = startReport(doer, null, null, BOOKING_ORDERS, params);

		PersistentTask task = waitForTaskEnd(metadata);
		Assert.assertNotNull(task);
		Assert.assertEquals(TaskStatus.failed, task.getStatus());
		// No partial file left (the folder can contain files of former test runs with the same task key)
		VFSItem file = VFSManager.olatRootContainer("/reports/curriculum/" + task.getKey() + "/")
				.resolve(metadata.getFilename());
		Assert.assertNull(file);
	}

	/**
	 * @return The text of all the XML files of the workbook
	 */
	private String readXlsx(LocalFileImpl file) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (ZipFile zip = new ZipFile(file.getBasefile())) {
			for (ZipEntry entry : Collections.list(zip.entries())) {
				try (InputStream in = zip.getInputStream(entry)) {
					sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
				}
			}
		}
		return sb.toString();
	}

	private ExportMetadata startReport(Identity doer, Curriculum curriculum, CurriculumElement element,
			String templateId, ReportParameters params) {
		String fileName = "Report_" + templateId + "_" + System.nanoTime() + ".xlsx";
		Date expirationDate = DateUtils.addDays(new Date(), AbstractReportTask.RETENTION_DAYS);
		CurriculumReportTask task = new CurriculumReportTask("Report", curriculum, element, doer, Locale.ENGLISH, templateId, params);
		ExportMetadata metadata = exportManager.startExport(task, "Report", "Description", fileName, ArchiveType.COURSEPLANNER,
				expirationDate, false, element == null ? null : element.getResource(),
				CurriculumReportsListController.CURRICULUM_REPORT_IDENT, doer);
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
