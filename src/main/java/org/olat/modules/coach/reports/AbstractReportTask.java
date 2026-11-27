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
package org.olat.modules.coach.reports;

import java.io.Serial;
import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.export.AbstractExportTask;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * Common run logic of the report tasks: creation of the file, metadata,
 * cancellation and error handling. The task stores only the identifier of
 * the template and the parameters, never the template itself.
 * <p>
 * If the generation fails, the partial file is deleted and the exception
 * leaves {@link #run()}, so the task executor marks the task as failed.
 *
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public abstract class AbstractReportTask extends AbstractExportTask {

	@Serial
	private static final long serialVersionUID = -2694384118542380312L;
	
	private static final Logger log = Tracing.createLoggerFor(AbstractReportTask.class);

	/**
	 * How long a generated report is kept.
	 */
	public static final int RETENTION_DAYS = 10;

	private transient VFSLeaf exportZip;

	private final String title;
	private final Long doerKey;
	private final Locale locale;
	private final String templateId;
	private final ReportParameters params;

	protected AbstractReportTask(String title, IdentityRef doer, Locale locale, String templateId, ReportParameters params) {
		this.title = title;
		this.doerKey = doer == null ? null : doer.getKey();
		this.locale = locale;
		this.templateId = templateId;
		this.params = params;
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public VFSLeaf getExportZip() {
		return exportZip;
	}

	public String getTemplateId() {
		return templateId;
	}

	public ReportParameters getParameters() {
		return params;
	}

	@Override
	public void run() {
		final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		final ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		final TaskExecutorManager taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		final VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);

		ReportConfiguration template = CoreSpringFactory.getImpl(ReportTemplatesRegistry.class).getTemplate(templateId);
		if(template == null) {
			throw new ReportGenerationException("Report template not found: " + templateId);
		}

		Identity doer = doerKey == null ? null : securityManager.loadIdentityByKey(doerKey);
		VFSContainer subFolder = getOutputFolder(doer);

		ExportMetadata metadata = exportManager.getExportMetadataByTask((PersistentTask)task);
		String vfsName = metadata.getFilename();
		exportZip = subFolder.createChildLeaf(vfsName);
		if(exportZip == null) {
			vfsName = VFSManager.rename(subFolder, vfsName);
			exportZip = subFolder.createChildLeaf(vfsName);
			if(exportZip == null) {
				throw new ReportGenerationException("Cannot create the file of the report: " + vfsName);
			}
		}
		String metadataDescription = Formatter.truncate(metadata.getDescription(), 31000);
		Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(new Date(), RETENTION_DAYS));
		fillMetadata(exportZip, title, metadataDescription, expirationDate);

		if(task.getStatus() == TaskStatus.cancelled) {
			if(exportZip != null) {
				exportZip.deleteSilently();
			}
			return;
		}

		metadata.setFilename(exportZip.getName());
		metadata.setFilePath(exportZip.getRelPath());
		metadata.setMetadata(exportZip.getMetaInfo());
		metadata = exportManager.updateMetadata(metadata);

		try {
			generateReport(template, doer, params, locale, exportZip, metadata);
		} catch(RuntimeException | Error e) {
			deletePartialFile();
			throw e;
		}

		TaskStatus status = taskExecutorManager.getStatus(task);
		if(status == TaskStatus.cancelled) {
			exportZip.deleteSilently();
			taskExecutorManager.delete(task);
		} else {
			vfsRepositoryService.getMetadataFor(exportZip);
		}
	}

	/**
	 * Never leave a partial file which looks complete. The uncommitted changes
	 * of the export metadata reference the file and are rolled back first. An error
	 * here is logged and doesn't hide the error of the report.
	 */
	private void deletePartialFile() {
		try {
			DBFactory.getInstance().rollbackAndCloseSession();
			exportZip.deleteSilently();
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			DBFactory.getInstance().rollbackAndCloseSession();
			log.error("Cannot delete the partial report: {}", exportZip.getRelPath(), e);
		}
	}

	/**
	 * @param doer The person who started the report
	 * @return The folder where the report is written
	 */
	protected abstract VFSContainer getOutputFolder(Identity doer);

	/**
	 * Generates the report into the output file. Implementations must not catch
	 * the errors of the template.
	 *
	 * @param template The report template
	 * @param doer The person who started the report
	 * @param parameters The parameters of the report
	 * @param locale The locale
	 * @param output The output file
	 * @param metadata The export metadata
	 */
	protected abstract void generateReport(ReportConfiguration template, Identity doer, ReportParameters parameters,
			Locale locale, VFSLeaf output, ExportMetadata metadata);
}
