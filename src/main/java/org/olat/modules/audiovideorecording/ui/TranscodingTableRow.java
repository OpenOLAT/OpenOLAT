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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.translator.Translator;

import java.util.Date;

/**
 * Initial date: 2022-10-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscodingTableRow {
	private final Translator translator;
	private final String id;
	private final String fileName;
	private final long fileSize;
	private final Date creationDate;
	private final Integer transcodingStatus;
	private FormLink retranscodeLink = null;

	public TranscodingTableRow(Translator translator, String id, String fileName, long fileSize, Date creationDate,
							   Integer transcodingStatus) {
		this.translator = translator;
		this.id = id;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.creationDate = creationDate;
		this.transcodingStatus = transcodingStatus;
	}

	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Integer getTranscodingStatus() {
		return transcodingStatus;
	}

	public String getTranscodingStatusString() {
		if (transcodingStatus == null) {
			return "-";
		}
		switch (transcodingStatus) {
			case VFSMetadata.TRANSCODING_STATUS_DONE:
				return translator.translate("transcoding.status.done");
			case VFSMetadata.TRANSCODING_STATUS_WAITING:
				return translator.translate("transcoding.status.waiting");
			case VFSMetadata.TRANSCODING_STATUS_ERROR:
				return translator.translate("transcoding.status.error");
			case VFSMetadata.TRANSCODING_STATUS_TIMEOUT:
				return translator.translate("transcoding.status.timeout");
			default:
				return translator.translate("transcoding.status.progress", transcodingStatus.toString());
		}
	}

	public FormLink getRetranscodeLink() {
		return retranscodeLink;
	}

	public void setRetranscodeLink(FormLink retranscodeLink) {
		this.retranscodeLink = retranscodeLink;
	}

	public Object getAction() {
		if (retranscodeLink != null) {
			return retranscodeLink;
		}
		return null;
	}
}
