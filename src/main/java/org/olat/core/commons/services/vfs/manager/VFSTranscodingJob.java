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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;

import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

	private void doExecute() {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null || !transcodingService.isLocalTranscodingEnabled()) {
			log.debug("Skipping execution of VFS file conversion job. Local VFS file conversion disabled");
			return;
		}

		for (VFSMetadata metadata = getNextMetadata(); metadata != null; metadata = getNextMetadata()) {
			if (needToCancel()) {
				break;
			}
			forkTranscodingProcess(metadata);
		}
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

	private boolean needToCancel() {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null) {
			return true;
		}
		return !transcodingService.isLocalConversionEnabled();
	}

	private void forkTranscodingProcess(VFSMetadata metadata) {
		VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
		if (transcodingService == null) {
			return;
		}

		String destinationFileName = metadata.getFilename();
		VFSItem destinationItem = transcodingService.getDestinationItem(metadata);
		if (destinationItem == null || !destinationItem.exists()) {
			log.error("The item " + destinationItem + " does not exist.");
			updateError(metadata);
			return;
		}

		String destinationDirectoryString = transcodingService.getDirectoryString(destinationItem);
		String masterFileName = VFSTranscodingService.masterFilePrefix + destinationFileName;
		List<String> command = createCommand(destinationDirectoryString, masterFileName, destinationFileName, transcodingService);
		if (command == null) {
			log.error("The destination file '{}' cannot be created from '{}'.", destinationFileName, masterFileName);
		}

		runCommand(command, metadata);
	}



	private List<String> createCommand(String directoryPath, String inputFileName, String outputFileName,
									   VFSTranscodingService transcodingService) {
		String fileSuffix = FileUtils.getFileSuffix(outputFileName);

		if ("mp4".equals(fileSuffix)) {
			return createHandbrakeCommand(directoryPath, inputFileName, outputFileName, transcodingService);
		} else if ("m4a".equals(fileSuffix)) {
			return createFfmpegCommand(directoryPath, inputFileName, outputFileName, transcodingService);
		} else {
			return null;
		}
	}

	private List<String> createHandbrakeCommand(String directoryPath, String inputFileName, String outputFileName,
												VFSTranscodingService transcodingService) {
		if (!transcodingService.isLocalTranscodingEnabled()) {
			log.debug("Local video conversion is disabled.");
			return null;
		}

		ArrayList<String> command = new ArrayList<>();

		String handbrakeCliExecutable = transcodingService.getHandbrakeCliExecutable();

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

	private List<String> createFfmpegCommand(String directoryPath, String inputFileName, String outputFileName,
											 VFSTranscodingService transcodingService) {
		if (!transcodingService.isLocalAudioConversionEnabled()) {
			log.debug("Local audio conversion is disabled.");
			return null;
		}

		ArrayList<String> command = new ArrayList<>();

		String ffmpegExecutable = transcodingService.getFfmpegExecutable();

		if (StringHelper.containsNonWhitespace(ffmpegExecutable)) {
			command.add(ffmpegExecutable);
		} else {
			command.add("ffmpeg");
		}

		command.add("-y");
		command.add("-i");
		command.add(directoryPath + "/" + inputFileName);
		command.add(directoryPath + "/" + outputFileName);

		return command;
	}

	private void runCommand(List<String> command, VFSMetadata vfsMetadata) {
		log.debug("Converting " + vfsMetadata.getFilename());
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		log.debug(command.toString());
		Process process = null;
		try {
			process = processBuilder.start();
			if (log.isDebugEnabled()) {
				debugProcess(process);
			}
			int exitValue = process.waitFor();
			log.debug("Conversion exit value: " + exitValue);
			updateStatus(vfsMetadata, VFSMetadata.TRANSCODING_STATUS_DONE);
			VFSTranscodingService transcodingService = CoreSpringFactory.getImpl(VFSTranscodingService.class);
			if (transcodingService != null) {
				transcodingService.fileDoneEvent(vfsMetadata);
			}
		} catch (IOException e) {
			log.error("Cannot start conversion process", e);
			updateError(vfsMetadata);
		} catch (InterruptedException e) {
			log.error("Conversion process interrupted", e);
			updateError(vfsMetadata);
		} catch (Exception e) {
			log.error("Cannot execute conversion process", e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private void debugProcess(Process process) {
		InputStream inputStream = process.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedInputStreamReader = new BufferedReader(inputStreamReader);
		StringBuilder input = new StringBuilder();

		InputStream errorStream = process.getErrorStream();
		InputStreamReader errorStreamReader = new InputStreamReader(errorStream);
		BufferedReader bufferedErrorStreamReader = new BufferedReader(errorStreamReader);
		StringBuilder errors = new StringBuilder();

		String line;
		try {
			while ((line = bufferedErrorStreamReader.readLine()) != null) {
				errors.append(line);
				log.debug("stderr: " + line);
			}
			while ((line = bufferedInputStreamReader.readLine()) != null) {
				input.append(line);
				log.debug("stdout: " + line);
			}
		} catch (IOException e) {
			//
		}
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
