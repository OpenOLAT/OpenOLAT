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
package org.olat.modules.video.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.commons.services.taskexecutor.Sequential;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.model.VideoQualityVersion;
import org.olat.resource.OLATResource;

/**
 * This task implements transcoding of a single video file using the Handbrake CLI. 
 * 
 * Initial date: 22.04.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class VideoTranscodingTask implements LongRunnable, Sequential {
	private static final long serialVersionUID = 2982868860465334552L;
	private static final OLog log = Tracing.createLoggerFor(VideoTranscodingTask.class);
	private OLATResource video;
	private VideoQualityVersion version;
	private File transcodedFile;
	
	/**
	 * 
	 * @param video
	 * @param version
	 */
	VideoTranscodingTask(OLATResource video, VideoQualityVersion version) {
		this.video = video;
		this.version = version;
	}

	
	@Override
	public void run() {
		VideoModule videoModule = CoreSpringFactory.getImpl(VideoModule.class);
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		File masterFile = videoManager.getVideoFile(video);
		File transcodingFolder = ((LocalFolderImpl)videoManager.getTranscodingContainer(video)).getBasefile();
		transcodedFile = new File(transcodingFolder,  Integer.toString(version.getResolution()) + masterFile.getName());
		
		ArrayList<String> cmd = new ArrayList<>();
		String tasksetConfig = videoModule.getTranscodingTasksetConfig();
		if (tasksetConfig != null) {
			cmd.add("taskset");
			cmd.add("-c");
			cmd.add(tasksetConfig);			
		}
		cmd.add("HandBrakeCLI");
		cmd.add("-i"); 
		cmd.add(masterFile.getAbsolutePath());
		cmd.add("-o"); 
		cmd.add(transcodedFile.getAbsolutePath());
		cmd.add("--optimize");
		cmd.add("--preset");
		cmd.add("Normal");
		cmd.add("--height");
		cmd.add(Integer.toString(version.getResolution()));
		cmd.add("--deinterlace");
		cmd.add("--crop");
		cmd.add("0:0:0:0");
		
		Process process = null;
		try {
			if(log.isDebug()) {
				log.debug(cmd.toString());
			}
			ProcessBuilder builder = new ProcessBuilder(cmd);
			process = builder.start();
			executeProcess(process);
		} catch (IOException e) {
			log.error ("Could not spawn convert sub process", e);
			if (process != null) {
				process.destroy();
				process = null;
			}
			//TODO: remove version file, cleanup, remove job
		}
	}


	/**
	 * Internal helper to deal with the handbrake console output and update the transcoding metadata
	 * @param proc
	 */
	private final void executeProcess(Process proc) {
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		
		StringBuilder errors = new StringBuilder();
		StringBuilder output = new StringBuilder();
		String line;
		
		// Read from standard input and parse percentages of transcoding process
		InputStream stdout = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdout);
		BufferedReader br = new BufferedReader(isr);
		line = null;
		try {
			while ((line = br.readLine()) != null) {
				output.append(line);
				// Parse the percentage. Logline looks like this:
				// Encoding: task 1 of 1, 85.90 % (307.59 fps, avg 330.35 fps, ETA 00h00m05s)
				int start = line.indexOf(",");
				if (start != -1) {
					line = line.substring(start);
					int end = line.indexOf(".");
					if (end != -1 && end < 5) {
						String percent = line.substring(2, end);
						log.debug("Output: " + percent);		
						// update version file for UI
						version.setTranscodingStatus(Integer.parseInt(percent));
						videoManager.updateVersion(video, version);
					}
				}
			}
		} catch (IOException e) {
			//
		}

		// Read and ignore errors, Handbrake outputs a lot info on startup. Only
		// display errors in debug level
 		InputStream stderr = proc.getErrorStream();
		InputStreamReader iserr = new InputStreamReader(stderr);
		BufferedReader berr = new BufferedReader(iserr);
		line = null;
		try {
			while ((line = berr.readLine()) != null) {
				errors.append(line);
				log.debug("Error: " + line);
			}
		} catch (IOException e) {
			//
		}

		try {
			// On finish, update metadata file
			int exitValue = proc.waitFor();
			if (exitValue == 0) {
				MovieService movieService = CoreSpringFactory.getImpl(MovieService.class);
				Size videoSize = movieService.getSize(new LocalFileImpl(transcodedFile), VideoManagerImpl.FILETYPE_MP4);
				version.setDimension(videoSize);
				version.setFileSize(Formatter.formatBytes(transcodedFile.length()));
				version.setTranscodingStatus(VideoQualityVersion.TRANSCODING_STATUS_DONE);
				videoManager.updateVersion(video, version);
			}
		} catch (InterruptedException e) {
			//
			//TODO: do I need to remove task from DB?
		}
	}
}
