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
package org.olat.core.commons.services.export.ui;

import java.util.Date;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportRow {
	
	private Task runningTask;
	private final ExportInfos export;
	private final ExportMetadata metadata;
	private final String creatorFullName;

	private String month;
	private final String type;
	
	private FormLink downloadLink;
	private FormLink downloadButton;
	private FormLink cancelButton;
	private FormLink deleteButton;
	private FormLink infosButton;
	private FormLink toolsButton;
	private ProgressBarItem progressBar;
	
	public ExportRow(ExportInfos export, String type, String creatorFullName) {
		runningTask = export.getTask();
		metadata = export.getExportMetadata();
		this.type = type;
		this.export = export;
		this.creatorFullName = creatorFullName;
	}
	
	public ExportRow(ExportMetadata metadata, String type, String creatorFullName) {
		runningTask = metadata.getTask();
		this.metadata = metadata;
		this.type = type;
		export = null;
		this.creatorFullName = creatorFullName;
	}
	
	public boolean isTaskNew() {
		return runningTask != null && TaskStatus.newTask.equals(runningTask.getStatus());
	}
	
	public boolean isTaskRunning() {
		return runningTask != null && TaskStatus.inWork.equals(runningTask.getStatus());
	}
	
	public boolean isTaskCancelled() {
		return runningTask != null && TaskStatus.cancelled.equals(runningTask.getStatus());
	}

	public String getTitle() {
		if(metadata != null) {
			return metadata.getTitle();
		}
		return export == null ? null : export.getTitle();
	}
	
	public Long getMetadataKey() {
		return metadata == null ? null : metadata.getKey();
	}
	
	public ExportMetadata getMetadata() {
		return metadata;
	}
	
	public String getRepositoryEntryDisplayname() {
		if(metadata != null && metadata.getEntry() != null) {
			return metadata.getEntry().getDisplayname();
		}
		return null;
	}
	
	public String getRepositoryEntryExternalRef() {
		if(metadata != null && metadata.getEntry() != null) {
			return metadata.getEntry().getExternalRef();
		}
		return null;
	}
	
	public boolean isOnlyAdministrators() {
		return metadata == null ? false : metadata.isOnlyAdministrators();
	}
	
	public String getType() {
		return type;
	}
	
	public ArchiveType getArchiveType() {
		return metadata == null ? null : metadata.getArchiveType();
	}
	
	public String getDescription() {
		return export == null ? null : export.getDescription();
	}
	
	public String getCreatorFullName() {
		return creatorFullName;
	}

	public Date getCreationDate() {
		if(metadata != null) {
			return metadata.getCreationDate();
		}
		return export == null ? null : export.getCreationDate();
	}
	
	public Task getRunningTask() {
		if(metadata != null) {
			return metadata.getTask();
		}
		if(export != null) {
			return export.getTask();
		}
		return null;
	}
	
	public Date getExpirationDate() {
		if(metadata != null) {
			return metadata.getExpirationDate();
		}
		return export.getExpirationDate();
	}
	
	public long getArchiveSize() {
		if(metadata != null) {
			if(metadata.getMetadata() == null) {
				return 0l;
			}
			return metadata.getMetadata().getFileSize();
		}
		VFSLeaf archive = getArchive();
		return archive == null ? 0l : archive.getSize();
	}
	
	public VFSLeaf getArchive() {
		if(metadata != null && StringHelper.containsNonWhitespace(metadata.getFilePath())) {
			return VFSManager.olatRootLeaf(metadata.getFilePath());
		}
		if(export != null) {
			return export.getZipLeaf();
		}
		return null;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public ExportInfos getExport() {
		return export;
	}

	public FormLink getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(FormLink downloadLink) {
		this.downloadLink = downloadLink;
	}

	public FormLink getDownloadButton() {
		return downloadButton;
	}

	public void setDownloadButton(FormLink downloadButton) {
		this.downloadButton = downloadButton;
	}

	public FormLink getCancelButton() {
		return cancelButton;
	}

	public void setCancelButton(FormLink cancelButton) {
		this.cancelButton = cancelButton;
	}

	public FormLink getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(FormLink deleteButton) {
		this.deleteButton = deleteButton;
	}

	public FormLink getInfosButton() {
		return infosButton;
	}

	public void setInfosButton(FormLink infosButton) {
		this.infosButton = infosButton;
	}
	
	public ProgressBarItem getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBarItem progressBar) {
		this.progressBar = progressBar;
	}

	public int getExpirationInDays() {
		Date expirationDate = getExpirationDate();
		if(expirationDate == null) {
			return -1;
		}
		int days = (int)DateUtils.countDays(new Date(), expirationDate);
		if(days < 0l) {
			days = 0;
		}
		return days;
	}

	public FormLink getToolsButton() {
		return toolsButton;
	}

	public void setToolsButton(FormLink toolsButton) {
		this.toolsButton = toolsButton;
	}
}
