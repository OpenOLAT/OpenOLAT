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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.resource.OLATResource;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * 
 * Initial date: 06.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTranscodingJob extends JobWithDB implements StatefulJob {

	/**
	 * 
	 * @see org.olat.core.commons.services.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	@Override
	public void executeWithDB(JobExecutionContext context) throws JobExecutionException {
		// uses StatefulJob interface to prevent concurrent job execution
		doExecute(context);
	}

	/**
	 * Implementation of job execution
	 * @param context
	 * @return
	 * @throws JobExecutionException
	 */
	private boolean doExecute(JobExecutionContext context) throws JobExecutionException {
		VideoModule videoModule = CoreSpringFactory.getImpl(VideoModule.class);
		if (!videoModule.isTranscodingLocal()) {
			log.debug("Skipping execution of video transcoding job, local transcoding disabled");
			return false;
		}
		
		// Find first one to work with
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodingsPendingAndInProgress();
		VideoTranscoding videoTranscoding = null;
		for (VideoTranscoding videoTrans : videoTranscodings) {
			String transcoder = videoTrans.getTranscoder();
			if (transcoder == null) { 
				log.info("Start transcoding video with resolution::" + videoTrans.getResolution()
					+ " for video resource::" + videoTrans.getVideoResource().getResourceableId());
				videoTrans.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
				videoTranscoding = videoManager.updateVideoTranscoding(videoTrans);				
				break;
			} else if (transcoder.equals(VideoTranscoding.TRANSCODER_LOCAL)) {
				log.info("Continue with transcoding video with resolution::" + videoTrans.getResolution()
					+ " for video resource::" + videoTrans.getVideoResource().getResourceableId());
				videoTranscoding = videoTrans;								
				break;
			}
		}
		
		if (videoTranscoding == null) {
			log.debug("Skipping execution of video transcoding job, no pending video transcoding found in database");
			return false;
		}
		// Ready transcode, forke process now
		boolean success = forkTranscodingProcess(videoTranscoding);
		
		// Transcoding done, call execution again until no more videos to be
		// processed. If an error happend, don't continue to not get into a loop
		if (success) {
			success = doExecute(context); 
		}
		return success;
	}
	
	/**
	 * Internal helper to fork a process with handbrake and read the values from the process
	 * @param videoTranscoding
	 * @return true: all ok; false: an error happend along the way
	 */
	private boolean forkTranscodingProcess(VideoTranscoding videoTranscoding) {
		OLATResource video = videoTranscoding.getVideoResource();
		VideoModule videoModule = CoreSpringFactory.getImpl(VideoModule.class);
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		File masterFile = videoManager.getVideoFile(video);
		File transcodingFolder = ((LocalFolderImpl)videoManager.getTranscodingContainer(video)).getBasefile();
		File transcodedFile = new File(transcodingFolder,  Integer.toString(videoTranscoding.getResolution()) + masterFile.getName());
		// mark this as beeing transcoded by this local transcoder
		videoTranscoding.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
		videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
		
		ArrayList<String> cmd = new ArrayList<>();
		String tasksetConfig = videoModule.getTranscodingTasksetConfig();
		if (tasksetConfig != null && !"Mac OS X".equals(System.getProperty("os.name"))) {
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
		cmd.add(Integer.toString(videoTranscoding.getResolution()));
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
			return updateVideoTranscodingFromProcessOutput(process, videoTranscoding, transcodedFile);
		} catch (IOException e) {
			log.error ("Could not spawn convert sub process", e);
			return false;
		} finally {
			if (process != null) {
				process.destroy();
				process = null;
			}			
		}
	}
	
	
	/**
	 * Internal helper to deal with the handbrake console output and update the transcoding metadata
	 * @param proc
	 * @param videoTranscoding
	 * @param transcodedFile
	 * @return true: everything fine; false: an error happended somewhere
	 */
	private final boolean updateVideoTranscodingFromProcessOutput(Process proc, VideoTranscoding videoTranscoding, File transcodedFile) {
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
						videoTranscoding.setStatus(Integer.parseInt(percent));
						videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
						DBFactory.getInstance().commitAndCloseSession();
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
				videoTranscoding.setWidth(videoSize.getWidth());
				videoTranscoding.setHeight(videoSize.getHeight());
				videoTranscoding.setSize(transcodedFile.length());
				videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_DONE);
				videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
				DBFactory.getInstance().commitAndCloseSession();
				return true;
			} 
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}

	
}
