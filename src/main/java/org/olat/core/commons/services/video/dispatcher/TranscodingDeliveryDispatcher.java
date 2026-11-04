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
package org.olat.core.commons.services.video.dispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.commons.services.video.TranscoderHelper;
import org.olat.core.commons.services.video.model.TranscoderJob;
import org.olat.core.commons.services.video.model.TranscoderJobResult;
import org.olat.core.commons.services.video.model.TranscoderJobStatus;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoManagerImpl;
import org.olat.modules.video.manager.VideoSubtitlesHelper;
import org.olat.modules.video.manager.VideoTranscodingDAO;
import org.olat.modules.video.model.VideoTranscodingMode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Initial date: 2026-01-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service("transcodingDispatcherBean")
public class TranscodingDeliveryDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(TranscodingDeliveryDispatcher.class);

	private static final String AUDIO_CONVERSION_TYPE = "audioConversion/";
	private static final String VIDEO_CONVERSION_TYPE = "videoConversion/";
	private static final String VIDEO_TRANSCODING_TYPE = "videoTranscoding/";

	/** OO-9999, WP4 H7. Margin added on top of 2x the original file size. Exact value: open point O2 of the plan. */
	private static final long DOWNLOAD_SIZE_MARGIN_BYTES = 50L * 1024 * 1024;
	
	/** OO-9999, WP4 H7. Subtitle tracks are small text files, so a fixed cap is enough; there is no "original" to size against. */
	private static final long SUBTITLES_MAX_DOWNLOAD_SIZE_BYTES = 10L * 1024 * 1024;

	@Autowired
	private AVModule avModule;
	
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	
	@Autowired
	private VFSTranscodingService vfsTranscodingService;
	
	@Autowired
	private VideoManager videoManager;
	
	@Autowired
	private VideoModule videoModule;
	
	@Autowired
	private HttpClientService httpClientService;
	
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	
	@Autowired
	private MovieService movieService;
	
	@Autowired
	private TextService textService;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Autowired
	private TranscoderHelper transcoderHelper;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
			String requestUri = request.getRequestURI();
			String typeUriWithTrailingForwardSlash = requestUri.substring(uriPrefix.length());
			String typeUri = StringUtils.trimTrailingCharacter(typeUriWithTrailingForwardSlash, '/');
			log.debug("Method: {}, URI prefix: {}, request URI: {}, typeUri: {}", request.getMethod(), 
					uriPrefix, requestUri, typeUri);
			
			if (typeUri.startsWith(AUDIO_CONVERSION_TYPE)) {
				handleAudioConversion(request, response, typeUri.substring(AUDIO_CONVERSION_TYPE.length()));
			} else if (typeUri.startsWith(VIDEO_CONVERSION_TYPE)) {
				handleVideoConversion(request, response, typeUri.substring(VIDEO_CONVERSION_TYPE.length()));
			} else if (typeUri.startsWith(VIDEO_TRANSCODING_TYPE)) {
				handleVideoTranscoding(request, response, typeUri.substring(VIDEO_TRANSCODING_TYPE.length()));
			} else if (typeUri.startsWith(TranscoderJob.NOTIFY_RESULT_COMMAND)) {
				handleNotifyResult(request, response, extractCallbackToken(typeUri, TranscoderJob.NOTIFY_RESULT_COMMAND));
			} else if (typeUri.startsWith(TranscoderJob.NOTIFY_STATUS_COMMAND)) {
				handleNotifyStatus(request, response, extractCallbackToken(typeUri, TranscoderJob.NOTIFY_STATUS_COMMAND));
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			log.warn("", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void handleAudioConversion(HttpServletRequest request, HttpServletResponse response, String uuid) throws IOException {
		if (!avModule.isAudioRecordingEnabled() || !VideoTranscodingMode.service.equals(avModule.getAudioConversionMode())) {
			log.info("Blocking request for audio conversion original. Feature disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		handleConversion(request, response, uuid);
	}

	private void handleConversion(HttpServletRequest request, HttpServletResponse response, String uuid) throws IOException {
		if (!isValidDashlessUuid(uuid)) {
			log.warn("Get original [uuid={}]: Blocking request for conversion original. Invalid UUID format.", uuid);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		uuid = addDashesToUuid(uuid);

		VFSMetadata metadata = vfsMetadataDao.getMetadata(uuid);
		if (metadata == null) {
			log.warn("Get original [uuid={}]: No metadata found.", uuid);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		VFSItem destinationItem = vfsTranscodingService.getDestinationItem(metadata);
		if (destinationItem instanceof LocalFileImpl localFile) {
			File masterFile = vfsTranscodingService.getMasterFile(localFile.getBasefile());
			VFSLeaf masterVfsLeaf = new LocalFileImpl(masterFile);
			VFSMediaResource masterMediaResource = new VFSMediaResource(masterVfsLeaf);
			log.debug("Get original [uuid={}, masterFie={}]", uuid, masterFile.getAbsolutePath());
			ServletUtil.serveResource(request, response, masterMediaResource);
			return;
		}

		log.warn("Get original [uuid={}]: No file found.", uuid);
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Extracts the callback token from the path segment that follows the
	 * {@code notifyResult} or {@code notifyStatus} command, e.g. {@code <token>}
	 * in {@code notifyResult/<token>}. OO-9999, WP3.
	 *
	 * @param typeUri The dispatched URI, without the dispatcher's own prefix
	 * @param command {@link TranscoderJob#NOTIFY_RESULT_COMMAND} or {@link TranscoderJob#NOTIFY_STATUS_COMMAND}
	 * @return The token, or null if the path carries none
	 */
	private static String extractCallbackToken(String typeUri, String command) {
		String remainder = typeUri.substring(command.length());
		if (remainder.length() <= 1 || remainder.charAt(0) != '/') {
			return null;
		}
		return remainder.substring(1);
	}

	/**
	 * Feature gate (OO-9999, WP4 H1): a callback for a type that is disabled, or
	 * not running in {@code service} mode, cannot come from a transcoder service
	 * this instance is configured to use.
	 */
	private boolean isEnabledForService(TranscoderJobType type) {
		return switch (type) {
			case videoTranscoding -> videoModule.isTranscodingEnabled()
					&& VideoTranscodingMode.service.equals(videoModule.getVideoTranscodingMode());
			case videoConversion -> avModule.isVideoRecordingEnabled()
					&& VideoTranscodingMode.service.equals(avModule.getVideoConversionMode());
			case audioConversion -> avModule.isAudioRecordingEnabled()
					&& VideoTranscodingMode.service.equals(avModule.getAudioConversionMode());
		};
	}

	/** The configured transcoder/conversion service URL for a job type (OO-9999, WP4 H4). */
	private String getServiceUrl(TranscoderJobType type) {
		return switch (type) {
			case videoTranscoding -> videoModule.getTranscodingServiceUrl();
			case videoConversion -> avModule.getVideoConversionServiceUrl();
			case audioConversion -> avModule.getAudioConversionServiceUrl();
		};
	}

	private static boolean isValidDashlessUuid(String dashlessUuid) {
		return dashlessUuid != null && dashlessUuid.matches("^[0-9a-fA-F]{32}$");
	}

	private static String addDashesToUuid(String dashlessUuid) {
		StringBuilder sb = new StringBuilder(dashlessUuid);
		sb.insert(8, '-');
		sb.insert(13, '-');
		sb.insert(18, '-');
		sb.insert(23, '-');
		return sb.toString();
	}

	private void handleVideoConversion(HttpServletRequest request, HttpServletResponse response, String referenceIdString) throws IOException {
		if (!avModule.isVideoRecordingEnabled() || !VideoTranscodingMode.service.equals(avModule.getVideoConversionMode())) {
			log.info("Blocking request for video conversion original. Feature disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		handleConversion(request, response, referenceIdString);
	}

	private void handleVideoTranscoding(HttpServletRequest request, HttpServletResponse response, String uuid) throws IOException {
		if (!videoModule.isTranscodingEnabled() || !VideoTranscodingMode.service.equals(videoModule.getVideoTranscodingMode())) {
			log.info("Blocking request for video transcoding master file. Feature disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		VideoTranscoding videoTranscoding = videoTranscodingDao.getOneVideoTranscoding(uuid);
		if (videoTranscoding == null) {
			log.warn("Get original [uuid={}]: No video transcoding entry found", uuid);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		OLATResource videoResource = videoTranscoding.getVideoResource();
		File masterFile = videoManager.getVideoFile(videoResource);
		if (!masterFile.exists()) {
			log.warn("Get original [uuid={}]: No master video file found", uuid);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		VFSLeaf masterVfsLeaf = new LocalFileImpl(masterFile);
		VFSMediaResource masterMediaResource = new VFSMediaResource(masterVfsLeaf);
		log.debug("Get transcoding original [uuid={}, masterFie={}]", uuid, masterFile.getAbsolutePath());
		ServletUtil.serveResource(request, response, masterMediaResource);
	}

	private void handleNotifyStatus(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
		String method = request.getMethod();
		if (!"POST".equalsIgnoreCase(method)) {
			log.warn("Job status: Method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}

		TranscoderJobStatus status = readStatus(request.getInputStream());
		if (!transcoderHelper.verifyCallbackToken(token, status.getType(), status.getUuid(), status.getReferenceId())) {
			log.warn("Job status: Missing or invalid callback token [remoteAddr={}]", request.getRemoteAddr());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		if (!isEnabledForService(status.getType())) {
			log.warn("Job status: Feature disabled [type={}, remoteAddr={}]", status.getType(), request.getRemoteAddr());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		handleNotifyStatus(response, status);
	}

	private TranscoderJobStatus readStatus(ServletInputStream inputStream) throws IOException {
		String jsonString =  IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		log.debug("Job status: [json='{}']", jsonString);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, TranscoderJobStatus.class);
	}

	private void handleNotifyStatus(HttpServletResponse response, TranscoderJobStatus status) throws IOException {
		Float percentage = status.getStatus();
		if (percentage == null) {
			log.warn("Job status: 'status' missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		switch (status.getType()) {
			case videoConversion, audioConversion:
				vfsTranscodingService.handleConversionJobStatus(status);
				break;
			case videoTranscoding:
				videoManager.handleVideoTranscodingJobStatus(status);
				break;
		}
	}
	
	private void handleNotifyResult(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
		String method = request.getMethod();
		if (!"POST".equalsIgnoreCase(method)) {
			log.warn("Conversion job result: Method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}
		
		TranscoderJobResult result = readResult(request.getInputStream());
		if (!transcoderHelper.verifyCallbackToken(token, result.getType(), result.getUuid(), result.getReferenceId())) {
			log.warn("Conversion job result: Missing or invalid callback token [remoteAddr={}]", request.getRemoteAddr());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		if (!isEnabledForService(result.getType())) {
			log.warn("Conversion job result: Feature disabled [type={}, remoteAddr={}]", result.getType(), request.getRemoteAddr());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		handleNotifyResult(response, result);
	}

	private TranscoderJobResult readResult(ServletInputStream inputStream) throws IOException {
		String jsonString =  IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		log.debug("Conversion job result: [json='{}']", jsonString);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, TranscoderJobResult.class);
	}

	private void handleNotifyResult(HttpServletResponse response, TranscoderJobResult result) throws IOException {
		if (!resultHasDataUrl(result)) {
			log.warn("Conversion job result: no data");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (result.getGenerated().getStatus() == null) {
			log.warn("Conversion job result: 'generated.status' missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		switch (result.getType()) {
			case videoConversion, audioConversion:
				handleConversionResult(result);
				break;
			case videoTranscoding:
				handleVideoTranscodingResult(result);
				break;
		}
	}
	
	private boolean resultHasDataUrl(TranscoderJobResult result) {
		if (result.getGenerated() == null) {
			return false;
		}
		
		if (StringHelper.containsNonWhitespace(result.getGenerated().getUrl())) {
			return true;
		}
		
		if (StringHelper.containsNonWhitespace(result.getGenerated().getSubtitlesUrl())) {
			return true;
		}
		
		return false;
	}

	private void handleConversionResult(TranscoderJobResult result) {
		if (!StringHelper.containsNonWhitespace(result.getGenerated().getUrl())) {
			log.warn("Missing 'generated.url'");
			return;
		}
		
		VFSMetadata metadata = vfsMetadataDao.loadMetadata(result.getReferenceId());
		if (metadata == null) {
			log.warn("Missing metadata for conversion job result [uuid={}]", result.getUuid());
			return;
		}

		if (!isPendingConversion(metadata.getTranscodingStatus())) {
			log.info("Ignoring conversion job result: not pending [uuid={}, status={}]", result.getUuid(), metadata.getTranscodingStatus());
			return;
		}

		VFSItem destinationItem = vfsTranscodingService.getDestinationItem(metadata);
		if (destinationItem == null) {
			log.warn("Missing destination item for conversion job result [uuid={}]", result.getUuid());
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
			return;
		}

		String targetPath = vfsTranscodingService.getTargetFilePath(metadata);
		log.debug("Writing conversion job result to target path [uuid={}, targetPath='{}']", result.getUuid(),
				targetPath);

		File targetFile = new File(targetPath);

		try {
			download(result, targetFile, originalFileSize(destinationItem));
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_DONE);
			vfsTranscodingService.fireDoneEvent(metadata);
			vfsTranscodingService.deleteGeneratedInService(result.getType(), result.getUuid());
		} catch (Exception e) {
			log.warn("Failed to download conversion job result [uuid={}, targetPath='{}']: {}",
					result.getUuid(), targetPath, e);
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
		}
	}

	/** OO-9999, WP4 H2: accept a conversion result only while the metadata is waiting for, or in, transcoding. */
	private static boolean isPendingConversion(Integer status) {
		return status != null && (status == VFSMetadata.TRANSCODING_STATUS_WAITING
				|| (status >= VFSMetadata.TRANSCODING_STATUS_STARTED && status <= 99));
	}

	/** The size of the original (pre-conversion) file, or 0 if it cannot be determined (OO-9999, WP4 H7). */
	private long originalFileSize(VFSItem destinationItem) {
		if (destinationItem instanceof LocalFileImpl localFile) {
			File masterFile = vfsTranscodingService.getMasterFile(localFile.getBasefile());
			if (masterFile != null) {
				return masterFile.length();
			}
		}
		return 0L;
	}

	private void updateStatus(VFSMetadata metadata, int status) {
		vfsTranscodingService.setStatus(metadata, status);	
		DBFactory.getInstance().commitAndCloseSession();
	}

	private void handleVideoTranscodingResult(TranscoderJobResult result) {
		int status = result.getGenerated().getStatus();
		if (status > VideoTranscoding.TRANSCODING_STATUS_WAITING && status < VideoTranscoding.TRANSCODING_STATUS_DONE) {
			log.info("Ignoring transcoding progress results: [uuid={}, status={}]", result.getUuid(), status);
			return;
		}

		RepositoryEntry entry = repositoryEntryDao.loadByResourceKey(result.getReferenceId());
		if (entry == null) {
			log.warn("Repository entry not found for job result [uuid={}, referenceId={}]", result.getUuid(), result.getReferenceId());
			return;
		}
		OLATResource videoResource = entry.getOlatResource();
		if (videoResource == null) {
			log.warn("Video resource not found for job result [uuid={}]", result.getUuid());
			return;
		}

		// OO-9999, WP4 H3: accept a result only for a row this instance is actually waiting on for this uuid.
		List<VideoTranscoding> jobTranscodings = videoTranscodingDao.getVideoTranscodings(videoResource.getKey(), result.getUuid());
		boolean hasPendingJob = jobTranscodings.stream().anyMatch(t -> VideoTranscoding.isPending(t.getStatus()));
		if (!hasPendingJob) {
			log.info("Ignoring transcoding job result: no pending job for this uuid [uuid={}, resKey={}]", result.getUuid(), videoResource.getKey());
			return;
		}

		// H5: subtitles only after the job check above has passed.
		handleVideoTranscodingSubtitles(result, videoResource);
		handleVideoTranscodingMedia(result, videoResource, jobTranscodings, status);
	}

	private void handleVideoTranscodingSubtitles(TranscoderJobResult result, OLATResource videoResource) {
		String subtitlesUrl = result.getGenerated().getSubtitlesUrl();
		if (!StringHelper.containsNonWhitespace(subtitlesUrl)) {
			return;
		}
		
		VFSContainer masterContainer = videoManager.getMasterContainer(videoResource);
		String tmpTrackName = VideoManagerImpl.TRACK + "tmp" + VideoManager.DOT + VideoManager.FILETYPE_VTT;
		File masterFolder = ((LocalFolderImpl) masterContainer).getBasefile();
		File tmpFile = new File(masterFolder, tmpTrackName);

		try {
			download(TranscoderJobType.videoTranscoding, subtitlesUrl, tmpFile, SUBTITLES_MAX_DOWNLOAD_SIZE_BYTES);
			String text = FileUtils.load(tmpFile, "UTF-8");
			Locale locale = textService.detectLocale(text);
			if (locale == null) {
				log.warn("Cannot find locale for subtitles [uuid={}]", result.getUuid());
				return;
			}
			String trackName = VideoManagerImpl.TRACK + locale.getLanguage() + VideoManager.DOT + VideoManager.FILETYPE_VTT;
			VFSItem oldFile = masterContainer.resolve(trackName);
			if (oldFile instanceof VFSLeaf && oldFile.exists()) {
				log.info("Subtitle file exists. Ignoring request. [videoResource={}, trackName={}]", videoResource, trackName);
				tmpFile.delete();
				return;
			}

			if (VideoSubtitlesHelper.isVtt(tmpFile)) {
				VFSLeaf target = VFSManager.resolveOrCreateLeafFromPath(masterContainer, trackName);
				VFSManager.copyContent(tmpFile, target, null);
				log.debug("Stored VTT subtitles [videoResource={}, trackName={}]",
						videoResource.getResourceableId(), trackName);
			} else if (VideoSubtitlesHelper.isConvertibleSrt(tmpFile)) {
				VFSLeaf newTrack = masterContainer.createChildLeaf(trackName);
				try (FileInputStream fileInputStream = new FileInputStream(tmpFile)) {
					VideoSubtitlesHelper.convertSrtToVtt(fileInputStream, newTrack.getOutputStream(false));
					vfsRepositoryService.itemSaved(newTrack, null);
					log.debug("Converted SRT file to VTT subtitles [videoResource={}, trackName={}]", 
							videoResource.getResourceableId(), trackName);
				} catch (Exception e) {
					log.error("Cannot convert non-VTT to VTT", e);
				}
			}
		} catch (IOException e) {
			log.warn("Failed to download subtitles for video [uuid={}]", result.getUuid(), e);
		}

		tmpFile.delete();
	}
	
	private void handleVideoTranscodingMedia(TranscoderJobResult result, OLATResource videoResource,
			List<VideoTranscoding> jobTranscodings, int status) {
		String url = result.getGenerated().getUrl();
		if (!StringHelper.containsNonWhitespace(url)) {
			return;
		}

		File masterFile = videoManager.getVideoFile(videoResource);
		if (masterFile == null) {
			log.warn("Video master file not found for job result [uuid={}]", result.getUuid());
			return;
		}

		// OO-9999, WP4 H3: pick the row among this uuid's own rows, and only if it is still pending
		// (not already done or failed: a replay or a late result must not revert a terminal row).
		int resolution = result.getGenerated().getResolution();
		VideoTranscoding videoTranscoding = jobTranscodings.stream()
				.filter(t -> t.getResolution() == resolution)
				.filter(t -> VideoTranscoding.isPending(t.getStatus()))
				.findFirst()
				.orElse(null);
		if (videoTranscoding == null) {
			log.warn("Video transcoding not found for job result [uuid={}, resolution={}]", result.getUuid(), resolution);
			return;
		}
		
		if (status < VideoTranscoding.TRANSCODING_STATUS_WAITING) {
			log.warn("Video transcoding job resulted in error status: [uuid={}, status={}]", result.getUuid(), status);
			updateStatus(videoTranscoding, status, result.getUuid());
			return;
		}

		File transcodingFolder = ((LocalFolderImpl) videoManager.getTranscodingContainer(videoResource)).getBasefile();
		File targetFile = new File(transcodingFolder, resolution + masterFile.getName());

		try {
			download(result, targetFile, masterFile.length());
			videoTranscoding.setStatus(status);
			videoTranscoding.setSize(targetFile.length());
			Size displaySize = movieService.getSize(new LocalFileImpl(targetFile), VideoManagerImpl.FILETYPE_MP4);
			if (displaySize != null) {
				videoTranscoding.setWidth(displaySize.getWidth());
				videoTranscoding.setHeight(displaySize.getHeight());
			} else if (result.getGenerated().getWidth() != null && result.getGenerated().getHeight() != null) {
				videoTranscoding.setWidth(result.getGenerated().getWidth());
				videoTranscoding.setHeight(result.getGenerated().getHeight());
			} else {
				log.warn("Transcoding result missing width and height for video [uuid={}]", result.getUuid());
				videoTranscoding.setWidth(0);
				videoTranscoding.setHeight(0);
			}
			update(videoTranscoding, result.getUuid());
		} catch (Exception e) {
			log.warn("Failed to download transcoding job result [uuid={}, targetPath='{}']: {}",
					result.getUuid(), targetFile.getAbsolutePath(), e);
			updateError(videoTranscoding, result.getUuid());
		}
		
		videoManager.optimizeMemoryForVideo(videoResource);
		if (allDone(videoManager.getVideoTranscodings(videoResource))) {
			videoManager.deleteGeneratedInService(result.getUuid());
			videoManager.fireVideoTranscodingStatusEvent(videoResource.getKey());
		}
	}

	private boolean allDone(List<VideoTranscoding> videoTranscodings) {
		if (videoTranscodings == null || videoTranscodings.isEmpty()) {
			return false;
		}
		
		for  (VideoTranscoding videoTranscoding : videoTranscodings) {
			if (videoTranscoding.getStatus() < VideoTranscoding.TRANSCODING_STATUS_DONE) {
				return false;
			}
		}
		return true;
	}

	private void updateError(VideoTranscoding videoTranscoding, String uuid) {
		updateStatus(videoTranscoding, VFSMetadata.TRANSCODING_STATUS_ERROR, uuid);
	}

	private void updateStatus(VideoTranscoding videoTranscoding, int status, String uuid) {
		videoTranscoding.setStatus(status);
		update(videoTranscoding, uuid);
	}

	private void update(VideoTranscoding videoTranscoding, String uuid) {
		videoTranscoding.setTranscoder(uuid);
		videoManager.updateVideoTranscoding(videoTranscoding);
		DBFactory.getInstance().commitAndCloseSession();
	}

	private void download(TranscoderJobResult result, File targetFile, long originalSize) throws IOException {
		download(result.getType(), result.getGenerated().getUrl(), targetFile, maxDownloadSize(originalSize));
	}

	/** OO-9999, WP4 H7: 2x the original size plus a margin, so a transcoding can legitimately grow a little. */
	private static long maxDownloadSize(long originalSize) {
		return 2 * originalSize + DOWNLOAD_SIZE_MARGIN_BYTES;
	}

	private void download(TranscoderJobType type, String url, File targetFile, long maxSizeBytes) throws IOException {
		String serviceUrl = getServiceUrl(type);
		if (!isAllowedDownloadOrigin(url, serviceUrl)) {
			log.warn("File download: URL is not the configured transcoder service [url={}, serviceUrl={}]", url, serviceUrl);
			throw new IOException("Download URL is not the configured transcoder service");
		}

		// OO-9999, WP4 H8: download to a temporary file next to the target, and only replace the
		// target once the download fully succeeded, so a failed download never destroys an existing file.
		// The dot prefix hides the temporary file from VFS listings (VFSSystemItemFilter).
		File tmpFile = File.createTempFile(".transcoder-download-", ".tmp", targetFile.getAbsoluteFile().getParentFile());
		try {
			HttpGet get = new HttpGet(url);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient(TranscoderHelper.PROTECTION_PROFILE);
				 CloseableHttpResponse httpResponse = httpClient.execute(get)) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpServletResponse.SC_OK) {
					log.warn("File download: [url={}, statusCode={}]", url, statusCode);
					throw new IOException("Failed to download");
				} else {
					log.debug("File download: [url={}, statusCode={}]", url, statusCode);
				}

				try (InputStream inputStream = httpResponse.getEntity().getContent();
					 OutputStream outputStream = new FileOutputStream(tmpFile)) {
					copyWithLimit(inputStream, outputStream, maxSizeBytes, url);
				}
			}
			Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} finally {
			tmpFile.delete();
		}
	}

	/** OO-9999, WP4 H7: stop and let the caller delete the partial download once it exceeds the limit. */
	private void copyWithLimit(InputStream inputStream, OutputStream outputStream, long maxSizeBytes, String url) throws IOException {
		byte[] buffer = new byte[8192];
		long total = 0;
		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			total += read;
			if (total > maxSizeBytes) {
				log.warn("File download: exceeded size limit, aborting [url={}, limitBytes={}]", url, maxSizeBytes);
				throw new IOException("Download exceeds the configured size limit");
			}
			outputStream.write(buffer, 0, read);
		}
	}

	/** OO-9999, WP4 H4: the download must come from the configured transcoder/conversion service, not anywhere else. */
	private static boolean isAllowedDownloadOrigin(String url, String serviceUrl) {
		if (!StringHelper.containsNonWhitespace(url) || !StringHelper.containsNonWhitespace(serviceUrl)) {
			return false;
		}
		try {
			URI downloadUri = new URI(url);
			URI serviceUri = new URI(serviceUrl);
			return sameOrigin(downloadUri, serviceUri);
		} catch (URISyntaxException e) {
			return false;
		}
	}

	private static boolean sameOrigin(URI a, URI b) {
		return equalsIgnoreCaseNullSafe(a.getScheme(), b.getScheme())
				&& equalsIgnoreCaseNullSafe(a.getHost(), b.getHost())
				&& effectivePort(a) == effectivePort(b);
	}

	private static boolean equalsIgnoreCaseNullSafe(String a, String b) {
		return a != null && a.equalsIgnoreCase(b);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() != -1) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}
}
