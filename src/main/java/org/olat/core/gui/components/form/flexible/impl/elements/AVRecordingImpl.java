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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.AVRecording;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Disposable;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.*;

import java.io.File;
import java.util.Set;

/**
 * Initial date: 2022-09-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVRecordingImpl extends FormItemImpl implements AVRecording, Disposable {

	private static final Logger log = Tracing.createLoggerFor(AVRecordingImpl.class);

	private final AVRecordingComponent component;

	private File recordedFile;
	private String recordedFileName;
	private File posterFile;
	private final Identity identity;

	public AVRecordingImpl(Identity identity, String name, String posterName) {
		super(name);
		this.identity = identity;
		component = new AVRecordingComponent(name, posterName);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		String recordedFileId = component.getRecordedFileId();
		String posterFileId = component.getPosterFileId();

		Set<String> keys = form.getRequestMultipartFilesSet();
		if (keys.isEmpty() || !keys.contains(recordedFileId)) {
			return;
		}

		deleteFiles();

		recordedFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
		moveOrCopyFile(form.getRequestMultipartFile(recordedFileId), recordedFile);
		recordedFileName = form.getRequestMultipartFileName(recordedFileId);

		if (keys.contains(posterFileId)) {
			posterFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
			moveOrCopyFile(form.getRequestMultipartFile(posterFileId), posterFile);
		}
	}

	private void deleteFiles() {
		if (recordedFile != null && recordedFile.exists()) {
			FileUtils.deleteFile(recordedFile);
		}
		if (posterFile != null && posterFile.exists()) {
			FileUtils.deleteFile(posterFile);
		}
	}

	private void moveOrCopyFile(File from, File to) {
		boolean success = from.renameTo(to);

		// The rename call fails when source and target are on different volumes
		if (!success) {
			FileUtils.copyFileToFile(from, to, true);
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public long getMaxUploadSizeKB() {
		return 0;
	}

	@Override
	public boolean isUploadSuccess() {
		return false;
	}

	@Override
	public File getRecordedFile() {
		return recordedFile;
	}

	@Override
	public String getRecordedFileName() {
		return recordedFileName;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
	}

	@Override
	public void dispose() {
		deleteFiles();
	}

	@Override
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
		VFSLeaf targetLeaf = null;
		if (recordedFile != null && recordedFile.exists()) {
			VFSItem itemWithSameName = destinationContainer.resolve(recordedFileName);
			if (itemWithSameName != null) {
				recordedFileName = VFSManager.rename(destinationContainer, recordedFileName);
			}
			if (destinationContainer instanceof LocalFolderImpl) { // optimize for local folders
				LocalFolderImpl folderContainer = (LocalFolderImpl) destinationContainer;
				File destinationDir = folderContainer.getBasefile();
				File targetFile = new File(destinationDir, recordedFileName);
				if (FileUtils.copyFileToFile(recordedFile, targetFile, true)) {
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					VFSRepositoryService repositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
					if (repositoryService != null) {
						repositoryService.itemSaved(targetLeaf, identity);
					} else {
						log.error("Cannot access VFSRepositoryService");
					}
				} else {
					log.error("Error copying content from temp file. Cannot copy file::" +
							(recordedFile == null ? "NULL" : recordedFile) + " - " + targetFile);
				}
			} else {
				VFSLeaf leaf = destinationContainer.createChildLeaf(recordedFileName);
				boolean success = false;
				try {
					success = VFSManager.copyContent(recordedFile, leaf, identity);
				} catch (Exception e) {
					log.error("Error while copying content from temp file: {}", recordedFile, e);
				}
				if (success) {
					FileUtils.deleteFile(recordedFile);
					targetLeaf = leaf;
				}
			}
		}
		return targetLeaf;
	}
}
