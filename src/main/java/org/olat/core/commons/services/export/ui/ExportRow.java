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

import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportRow {
	
	private final String creatorFullName;
	private final ExportInfos export;
	
	private String month;
	
	private FormLink downloadLink;
	private FormLink downloadButton;
	private FormLink cancelButton;
	private FormLink deleteButton;
	private FormLink infosButton;
	private ProgressBarItem progressBar;
	
	public ExportRow(ExportInfos export, String creatorFullName) {
		this.export = export;
		this.creatorFullName = creatorFullName;
	}
	
	public boolean isTaskNew() {
		return export.isNew();
	}
	
	public boolean isTaskRunning() {
		return export.isRunning();
	}
	
	public boolean isTaskCancelled() {
		return export.isCancelled();
	}
	
	public String getTitle() {
		return export.getTitle();
	}
	
	public String getDescription() {
		return export.getDescription();
	}
	
	public String getCreatorFullName() {
		return creatorFullName;
	}

	public Date getCreationDate() {
		return export.getCreationDate();
	}
	
	public Date getExpirationDate() {
		return export.getExpirationDate();
	}
	
	public long getArchiveSize() {
		return export.getZipLeaf() == null ? 0l : export.getZipLeaf().getSize();
	}
	
	public VFSLeaf getArchive() {
		return export.getZipLeaf();
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
}
