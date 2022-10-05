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
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVConfiguration;
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
	private String recordedFileType;
	private String recordedFileName;
	private File posterFile;
	private final Identity identity;
	private final AVConfiguration config;

	public AVRecordingImpl(Identity identity, String name, String posterName, AVConfiguration config) {
		super(name);
		this.identity = identity;
		this.config = config;
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

		recordedFileType = form.getRequestMultipartFileMimeType(recordedFileId);

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
	public String getFileName() {
		if (needsTranscoding()) {
			return getDestinationFileName(recordedFileName);
		}
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
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, String requestedName) {
		final VFSLeaf leaf;
		if (recordedFile != null && recordedFile.exists()) {
			if (needsTranscoding()) {
				String destinationFileName = getDestinationFileName(destinationContainer, requestedName);
				String masterFileName = VFSTranscodingService.masterFilePrefix + destinationFileName;
				if (destinationContainer instanceof LocalFolderImpl) {
					leaf = moveRecordingToLocalFolderWithTranscoding((LocalFolderImpl) destinationContainer, destinationFileName, masterFileName);
				} else {
					leaf = moveRecordingToVfsContainerWithTranscoding(destinationContainer, destinationFileName, masterFileName);
				}
				CoreSpringFactory.getImpl(DB.class).commitAndCloseSession();
				CoreSpringFactory.getImpl(VFSTranscodingService.class).startTranscodingProcess();
			} else {
				if (destinationContainer instanceof LocalFolderImpl) {
					leaf = moveRecordingToLocalFolder((LocalFolderImpl) destinationContainer, requestedName);
				} else {
					leaf = moveRecordingToVfsContainer(destinationContainer, requestedName);
				}
			}
		} else {
			leaf = null;
		}
		return leaf;
	}

	private VFSLeaf moveRecordingToLocalFolderWithTranscoding(LocalFolderImpl destinationContainer, String destinationFileName, String masterFileName) {
		File destinationDir = destinationContainer.getBasefile();
		File destinationFile = new File(destinationDir, destinationFileName);
		FileUtils.createEmptyFile(destinationFile);
		File masterFile = new File(destinationDir, masterFileName);
		if (FileUtils.copyFileToFile(recordedFile, masterFile, true)) {
			VFSLeaf destinationLeaf = (VFSLeaf) destinationContainer.resolve(destinationFileName);
			CoreSpringFactory.getImpl(VFSTranscodingService.class).itemSavedWithTranscoding(destinationLeaf, identity);
			return destinationLeaf;
		} else {
			log.error("Error copying recording from temporary file to master file. Cannot copy file::" +
					(recordedFile == null ? "NULL" : recordedFile) + " - " + masterFile);
			return null;
		}
	}

	private VFSLeaf moveRecordingToVfsContainerWithTranscoding(VFSContainer destinationContainer, String destinationFileName, String masterFileName) {
		VFSLeaf destinationLeaf = destinationContainer.createChildLeaf(destinationFileName);
		File emptyFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
		FileUtils.createEmptyFile(emptyFile);

		try {
			VFSManager.copyContent(emptyFile, destinationLeaf, identity);
		} catch (Exception e) {
			log.error("Error while copying content from temp file: {}", emptyFile, e);
		}
		FileUtils.deleteFile(emptyFile);

		VFSLeaf masterLeaf = destinationContainer.createChildLeaf(masterFileName);
		boolean success = false;
		try {
			success = VFSManager.copyContent(recordedFile, masterLeaf, identity);
		} catch (Exception e) {
			log.error("Error while copying content from temp file: {}", recordedFile, e);
		}
		if (success) {
			CoreSpringFactory.getImpl(VFSTranscodingService.class).itemSavedWithTranscoding(destinationLeaf, identity);
			FileUtils.deleteFile(recordedFile);
		}
		return destinationLeaf;
	}

	private VFSLeaf moveRecordingToLocalFolder(LocalFolderImpl destinationContainer, String requestedName) {
		VFSItem itemWithSameName = destinationContainer.resolve(requestedName);
		if (itemWithSameName != null) {
			requestedName = VFSManager.rename(destinationContainer, requestedName);
		}

		File destinationDir = destinationContainer.getBasefile();
		File destinationFile = new File(destinationDir, requestedName);
		if (FileUtils.copyFileToFile(recordedFile, destinationFile, true)) {
			VFSLeaf destinationLeaf = (VFSLeaf) destinationContainer.resolve(destinationFile.getName());
			VFSRepositoryService repositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			if (repositoryService != null) {
				repositoryService.itemSaved(destinationLeaf, identity);
			} else {
				log.error("Cannot access VFSRepositoryService");
			}
			return destinationLeaf;
		} else {
			log.error("Error copying recording from temporary file to destination file. Cannot copy file::" +
					(recordedFile == null ? "NULL" : recordedFile) + " - " + destinationFile);
			return null;
		}
	}

	private VFSLeaf moveRecordingToVfsContainer(VFSContainer destinationContainer, String requestedName) {
		VFSItem itemWithSameName = destinationContainer.resolve(requestedName);
		if (itemWithSameName != null) {
			requestedName = VFSManager.rename(destinationContainer, requestedName);
		}
		VFSLeaf destinationLeaf = destinationContainer.createChildLeaf(requestedName);
		boolean success = false;
		try {
			success = VFSManager.copyContent(recordedFile, destinationLeaf, identity);
		} catch (Exception e) {
			log.error("Error while copying content from temp file: {}", recordedFile, e);
		}
		if (success) {
			VFSRepositoryService repositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
			if (repositoryService != null) {
				repositoryService.itemSaved(destinationLeaf, identity);
			} else {
				log.error("Cannot access VFSRepositoryService");
			}
			FileUtils.deleteFile(recordedFile);
		}
		return destinationLeaf;
	}

	private boolean needsTranscoding() {
		if (config.getMode() == AVConfiguration.Mode.video) {
			if (recordedFileType == null || !recordedFileType.startsWith("video/mp4")) {
				return true;
			}
		}
		return false;
	}

	private String getDestinationFileName(String requestedName) {
		if (config.getMode() == AVConfiguration.Mode.video) {
			int indexOfPeriod = requestedName.lastIndexOf('.');
			if (indexOfPeriod != -1 && indexOfPeriod >= (requestedName.length() - 1 - 4)) {
				String baseName = requestedName.substring(0, indexOfPeriod);
				return baseName + ".mp4";
			}
		}
		return requestedName;
	}

	private String getDestinationFileName(VFSContainer destinationContainer, String requestedName) {
		final String destinationFileName = getDestinationFileName(requestedName);
		VFSItem itemWithSameName = destinationContainer.resolve(destinationFileName);
		if (itemWithSameName != null) {
			return VFSManager.rename(destinationContainer, destinationFileName);
		}
		return destinationFileName;
	}
}
