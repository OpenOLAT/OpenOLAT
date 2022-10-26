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
package org.olat.core.commons.services.vfs.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Initial date: 2022-09-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VFSTranscodingJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(VFSTranscodingJob.class);

	@Override
	public void executeWithDB(JobExecutionContext context) throws JobExecutionException {
		doExecute();
	}

	private boolean doExecute() {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null || !transcodingService.isLocalTranscodingEnabled()) {
			log.info("Skipping execution of VFS transcoding job. Local VFS transcoding disabled");
			return false;
		}

		boolean allOk = true;
		for (VFSMetadata metadata = getNextMetadata(); metadata != null; metadata = getNextMetadata()) {
			if (needToCancelTranscoding()) {
				break;
			}
			allOk &= forkTranscodingProcess(metadata);
		}
		return allOk;
	}

	private VFSMetadata getNextMetadata() {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null) {
			return null;
		}

		List<VFSMetadata> metadatas = transcodingService.getMetadatasInNeedForTranscoding();
		for (VFSMetadata metadata : metadatas) {
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_STARTED);
			return metadata;
		}
		return null;
	}

	private boolean needToCancelTranscoding() {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null) {
			return true;
		}
		return !transcodingService.isLocalTranscodingEnabled();
	}

	private boolean forkTranscodingProcess(VFSMetadata metadata) {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null) {
			return false;
		}

		String destinationFileName = metadata.getFilename();
		VFSItem destinationItem = transcodingService.getDestinationItem(metadata);
		if (destinationItem == null || !destinationItem.exists()) {
			log.error("The item " + destinationItem + " does not exist.");
			updateError(metadata);
			return false;
		}
		String handbrakeCliExecutable = transcodingService.getHandbrakeCliExecutable();
		String destinationDirectoryString = transcodingService.getDirectoryString(destinationItem);
		String masterFileName = VFSTranscodingService.masterFilePrefix + destinationFileName;
		List<String> command = createCommand(handbrakeCliExecutable, destinationDirectoryString, masterFileName, destinationFileName);

		return runCommand(command, metadata);
	}

	private List<String> createCommand(String handbrakeCliExecutable, String directoryPath, String inputFileName, String outputFileName) {
		ArrayList<String> command = new ArrayList<>();

		if (StringHelper.containsNonWhitespace(handbrakeCliExecutable)) {
			command.add(handbrakeCliExecutable);
		} else {
			command.add("HandBrakeCLI");
		}
		command.add("-i");
		command.add(directoryPath + "/" + inputFileName);
		command.add("-o");
		command.add(directoryPath + "/" + outputFileName);
		command.add("--optimize"); // Optimize MP4 files for HTTP streaming

		return command;
	}

	private boolean runCommand(List<String> command, VFSMetadata vfsMetadata) {
		log.info("Transcoding " + vfsMetadata.getFilename());
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		log.debug(command.toString());
		Process process = null;
		try {
			process = processBuilder.start();
			int exitValue = process.waitFor();
			log.debug("Transcoding exit value: " + exitValue);
			updateStatus(vfsMetadata, VFSMetadata.TRANSCODING_STATUS_DONE);
			VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
			if (transcodingService != null) {
				transcodingService.fileDoneEvent(vfsMetadata);
			}
		} catch (IOException e) {
			log.error("Cannot start transcoding process", e);
			updateError(vfsMetadata);
			return false;
		} catch (InterruptedException e) {
			log.error("Transcoding process interrupted", e);
			updateError(vfsMetadata);
			return false;
		} finally {
			if (process != null) {
				process.destroy();
			}
		}

		return true;
	}

	private void updateError(VFSMetadata vfsMetadata) {
		updateStatus(vfsMetadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
	}

	private void updateStatus(VFSMetadata vfsMetadata, int status) {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService != null) {
			transcodingService.setStatus(vfsMetadata, status);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
}
