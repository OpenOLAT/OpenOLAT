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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.io.File;
import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FileElementInfos;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 22 avr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UploadFileElementEvent extends FormEvent {

	private static final long serialVersionUID = -767107043693286989L;
	public static final String UPLOAD = "upload-file";
	
	private final List<File> files;
	private final List<FileElementInfos> uploadFilesInfos;
	private final String uploadFolder;
	
	public UploadFileElementEvent(FormItem source, List<File> files, List<FileElementInfos> uploadFilesInfos, String uploadFolder, int action) {
		super(UPLOAD, source, action);
		this.files = files;
		this.uploadFilesInfos = uploadFilesInfos;
		this.uploadFolder = uploadFolder;
	}

	public List<File> getFiles() {
		return files;
	}

	public List<FileElementInfos> getUploadFilesInfos() {
		return uploadFilesInfos;
	}
	
	public String getUploadFolder() {
		return uploadFolder;
	}
}
