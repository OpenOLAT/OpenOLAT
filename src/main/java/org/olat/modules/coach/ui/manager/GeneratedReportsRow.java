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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.DateUtils;
import org.olat.modules.coach.model.GeneratedReport;

/**
 * Initial date: 2025-01-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GeneratedReportsRow {
	private String name;
	private Date creationDate;
	private FormLink downloadLink;
	private FormLink copyToButton;
	private FormLink deleteButton;
	private FormLink downloadButton;
	private Date expirationDate;
	private long fileSize;
	private GeneratedReport generatedReport;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public FormLink getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(FormLink downloadLink) {
		this.downloadLink = downloadLink;
	}

	public FormLink getCopyToButton() {
		return copyToButton;
	}

	public void setCopyToButton(FormLink copyToButton) {
		this.copyToButton = copyToButton;
	}

	public FormLink getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(FormLink deleteButton) {
		this.deleteButton = deleteButton;
	}

	public FormLink getDownloadButton() {
		return downloadButton;
	}

	public void setDownloadButton(FormLink downloadButton) {
		this.downloadButton = downloadButton;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public int getExpirationInDays() {
		Date expirationDate = getExpirationDate();
		if (expirationDate == null) {
			return -1;
		}
		int days = (int) DateUtils.countDays(new Date(), expirationDate);
		if (days < 0L) {
			days = 0;
		}
		return days;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setGeneratedReport(GeneratedReport generatedReport) {
		this.generatedReport = generatedReport;
	}

	public GeneratedReport getGeneratedReport() {
		return generatedReport;
	}
}
