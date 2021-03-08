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
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.ObjectDeletedException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.resource.OLATResource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 06.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class VideoTranscodingJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(VideoTranscodingJob.class);
	private final List<String> resolutionsWithProfile = new ArrayList<>(Arrays.asList("1080", "720", "480"));

	@Override
	public void executeWithDB(JobExecutionContext context) throws JobExecutionException {
		// uses StatefulJob interface to prevent concurrent job execution
		doExecute();
	}

	/**
	 * Implementation of job execution
	 * @param context
	 * @return
	 * @throws JobExecutionException
	 */
	private boolean doExecute() {
		VideoModule videoModule = CoreSpringFactory.getImpl(VideoModule.class);
		if (!videoModule.isTranscodingLocal()) {
			log.debug("Skipping execution of video transcoding job, local transcoding disabled");
			return false;
		}
		
		// Find first one to work with
		boolean allOk = true;
		for(VideoTranscoding videoTranscoding = getNextVideo(); videoTranscoding != null;  videoTranscoding = getNextVideo()) {
			if(cancelTranscoding()) {
				break;
			}
			allOk &= forkTranscodingProcess(videoTranscoding);
		}
		return allOk;
	}
	
	private boolean cancelTranscoding() {
		try {
			VideoModule videoModule = CoreSpringFactory.getImpl(VideoModule.class);
			return (!videoModule.isTranscodingLocal() || !videoModule.isTranscodingEnabled());
		} catch (Exception e) {
			log.error("", e);
			return true;
		}
	}
	
	private VideoTranscoding getNextVideo() {
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodingsPendingAndInProgress();
		VideoTranscoding videoTranscoding = null;
		for (VideoTranscoding videoTrans : videoTranscodings) {
			String transcoder = videoTrans.getTranscoder();
			if (transcoder == null) { 
				log.info("Start transcoding video with resolution: {} for video resource: {}",
						videoTrans.getResolution(), videoTrans.getVideoResource().getResourceableId());
				videoTrans.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
				videoTranscoding = videoManager.updateVideoTranscoding(videoTrans);				
				break;
			} else if (transcoder.equals(VideoTranscoding.TRANSCODER_LOCAL)) {
				log.info("Continue with transcoding video with resolution: {} for video resource: {}",
						videoTrans.getResolution(), videoTrans.getVideoResource().getResourceableId());
				videoTranscoding = videoTrans;								
				break;
			}
		}
		return videoTranscoding;
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
		if(masterFile == null) {
			VideoMeta meta = videoManager.getVideoMetadata(videoTranscoding.getVideoResource());
			if(meta != null && StringHelper.containsNonWhitespace(meta.getUrl())) {
				videoTranscoding.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
				videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_DONE);
			} else {
				videoTranscoding.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
				videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_ERROR);
			}
			videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
			return true; 
		}
		
		File transcodingFolder = ((LocalFolderImpl)videoManager.getTranscodingContainer(video)).getBasefile();
		File transcodedFile = new File(transcodingFolder,  Integer.toString(videoTranscoding.getResolution()) + masterFile.getName());
		// mark this as beeing transcoded by this local transcoder
		videoTranscoding.setTranscoder(VideoTranscoding.TRANSCODER_LOCAL);
		videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
		
		String resolution = Integer.toString(videoTranscoding.getResolution());
		String profile = "Normal"; // Legacy fallback		
		if (resolutionsWithProfile.contains(resolution)) {
			profile = videoModule.getVideoTranscodingProfile() + " " + resolution + "p30";
		}
		
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
		cmd.add("--optimize"); 	// add video infos to header for web "fast start"
		cmd.add("--preset");
		cmd.add(profile);
		cmd.add("--height");
		cmd.add(resolution);
		cmd.add("--crop");		// do not crop
		cmd.add("0:0:0:0");
		
		Process process = null;
		try {
			if(log.isDebugEnabled()) {
				log.debug(cmd.toString());
			}
			ProcessBuilder builder = new ProcessBuilder(cmd);
			process = builder.start();
			return updateVideoTranscodingFromProcessOutput(process, videoTranscoding, transcodedFile);
		} catch (Exception e) {
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
		try {
			if(!updateStatus(proc, videoTranscoding)) {
				return false;
			}
			consumeErrorStream(proc);
			int exitValue = proc.waitFor();
			return updateStatus(videoTranscoding, transcodedFile, exitValue);
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	private boolean updateStatus(VideoTranscoding videoTranscoding, File transcodedFile, int exitCode) {
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		MovieService movieService = CoreSpringFactory.getImpl(MovieService.class);
		videoTranscoding = videoManager.getVideoTranscoding(videoTranscoding.getKey());
		
		Size videoSize = movieService.getSize(new LocalFileImpl(transcodedFile), VideoManagerImpl.FILETYPE_MP4);
		if(videoSize != null) {
			videoTranscoding.setWidth(videoSize.getWidth());
			videoTranscoding.setHeight(videoSize.getHeight());
		} else {
			videoTranscoding.setWidth(0);
			videoTranscoding.setHeight(0);
		}
		if(transcodedFile.exists()) {
			videoTranscoding.setSize(transcodedFile.length());
		} else {
			videoTranscoding.setSize(0);
		}
		if(exitCode == 0) {
			videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_DONE);
		} else {
			log.error("Exit code {}:{}", videoTranscoding, exitCode);
			videoTranscoding.setStatus(VideoTranscoding.TRANSCODING_STATUS_ERROR);
		}
		videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
		DBFactory.getInstance().commitAndCloseSession();
		return exitCode == 0;
	}
	
	/**
	 * Update the transcoding object in database.
	 * 
	 * @param proc The transcoding process
	 * @param videoTranscoding The video transcoding object
	 * @return false if the transcoding object was deleted
	 */
	private boolean updateStatus(Process proc, VideoTranscoding videoTranscoding) {
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		try(InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr)) {
			
			String line = null;
			while ((line = br.readLine()) != null) {
				// Parse the percentage. Logline looks like this:
				// Encoding: task 1 of 1, 85.90 % (307.59 fps, avg 330.35 fps, ETA 00h00m05s)
				int start = line.indexOf(",");
				if (start != -1) {
					line = line.substring(start);
					int end = line.indexOf(".");
					if (end != -1 && end < 5) {
						String percent = line.substring(2, end);
						log.debug("Output: {}", percent);		
						// update version file for UI
						try {
							videoTranscoding.setStatus(Integer.parseInt(percent));
							videoTranscoding = videoManager.updateVideoTranscoding(videoTranscoding);
							DBFactory.getInstance().commitAndCloseSession();							
						} catch (ObjectDeletedException e) {
							// deleted by other process
							proc.destroy();
							br.close();
							return false;
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return true;
	}
	
	/**
	 * Read and ignore errors, Handbrake outputs a lot info on startup. Only
	 * display errors in debug level.
	 * 
	 * @param proc The process
	 */
	private void consumeErrorStream(Process proc) {
		try(	InputStream stderr = proc.getErrorStream();
			InputStreamReader iserr = new InputStreamReader(stderr);
			BufferedReader berr = new BufferedReader(iserr)) {
			
			String line = null;
			while ((line = berr.readLine()) != null) {
				log.debug("Error: {}", line);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
