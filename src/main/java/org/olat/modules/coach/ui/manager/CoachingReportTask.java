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
package org.olat.modules.coach.ui.manager;

import java.io.Serial;
import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.AbstractExportTask;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.reports.ReportConfiguration;

/**
 * Initial date: 2025-06-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CoachingReportTask extends AbstractExportTask {

	@Serial
	private static final long serialVersionUID = 6361896212294337944L;

	private transient VFSLeaf exportZip;

	private final String title;
	private final Long identityKey;
	private final Locale locale;
	private final ReportConfiguration config;

	public CoachingReportTask(String title, Identity identity, Locale locale, ReportConfiguration config) {
		this.title = title;
		this.identityKey = identity != null ? identity.getKey() : null;
		this.locale = locale;
		this.config = config;
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

	@Override
	public void run() {
		final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		final ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		final TaskExecutorManager taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		final VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		CoachingService coachingService = CoreSpringFactory.getImpl(CoachingService.class);
		
		Identity identity = identityKey == null ? null : securityManager.loadIdentityByKey(identityKey);

		LocalFolderImpl subFolder = coachingService.getGeneratedReportsFolder(identity);

		ExportMetadata metadata = exportManager.getExportMetadataByTask((PersistentTask) task);
		String vfsName = metadata.getFilename();
		exportZip = subFolder.createChildLeaf(vfsName);
		if (exportZip == null) {
			vfsName = VFSManager.rename(subFolder, vfsName);
			exportZip = subFolder.createChildLeaf(vfsName);
		} else {
			String metadataDescription = Formatter.truncate(metadata.getDescription(), 20000);
			Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(new Date(), 10));
			fillMetadata(exportZip, title, metadataDescription, expirationDate);
		}

		if (task.getStatus() == TaskStatus.cancelled) {
			if (exportZip != null) {
				exportZip.deleteSilently();
			}
			return;
		}

		metadata.setFilename(exportZip.getName());
		metadata.setFilePath(exportZip.getRelPath());
		metadata.setMetadata(exportZip.getMetaInfo());
		exportManager.updateMetadata(metadata);
		
		config.generateReport(identity, locale, (LocalFileImpl) exportZip);

		TaskStatus status = taskExecutorManager.getStatus(task);
		if (status == TaskStatus.cancelled) {
			exportZip.deleteSilently();
			taskExecutorManager.delete(task);
		} else {
			vfsRepositoryService.getMetadataFor(exportZip);
		}
	}
}
