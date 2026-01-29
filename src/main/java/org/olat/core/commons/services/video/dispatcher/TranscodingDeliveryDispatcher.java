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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.video.model.TranscoderJob;
import org.olat.core.commons.services.video.model.TranscoderJobResult;
import org.olat.core.commons.services.video.model.TranscoderJobStatus;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
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
				handleNotifyResult(request, response);
			} else if (typeUri.startsWith(TranscoderJob.NOTIFY_STATUS_COMMAND)) {
				handleNotifyStatus(request, response);
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

	private void handleNotifyStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String method = request.getMethod();
		if (!"POST".equalsIgnoreCase(method)) {
			log.warn("Job status: Method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}

		TranscoderJobStatus status = readStatus(request.getInputStream());
		if (!StringHelper.containsNonWhitespace(status.getUuid())) {
			log.warn("Job status: Invalid UUID: {}", status.getUuid());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (status.getType() == null) {
			log.warn("Job status: type missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
		if (status.getReferenceId() == null) {
			log.warn("Job status: 'referenceId' missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

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
	
	private void handleNotifyResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String method = request.getMethod();
		if (!"POST".equalsIgnoreCase(method)) {
			log.warn("Conversion job result: Method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}
		
		TranscoderJobResult result = readResult(request.getInputStream());
		if (!StringHelper.containsNonWhitespace(result.getUuid())) {
			log.warn("Conversion job result: uuid missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (result.getType() == null) {
			log.warn("Conversion job result: type missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
		if (result.getGenerated() == null || !StringHelper.containsNonWhitespace(result.getGenerated().getUrl())) {
			log.warn("Conversion job result: 'generated.url' missing");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (result.getReferenceId() == null) {
			log.warn("Conversion job result: 'referenceId' missing");
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

	private void handleConversionResult(TranscoderJobResult result) {
		VFSMetadata metadata = vfsMetadataDao.loadMetadata(result.getReferenceId());
		if (metadata == null) {
			log.warn("Missing metadata for conversion job result [uuid={}]", result.getUuid());
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
			download(result, targetFile);
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_DONE);
			vfsTranscodingService.fireDoneEvent(metadata);
			vfsTranscodingService.deleteGeneratedInService(result.getType(), result.getUuid());
		} catch (Exception e) {
			log.warn("Failed to download conversion job result [uuid={}, targetPath='{}']: {}", 
					result.getUuid(), targetPath, e);
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
		}
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
		OLATResource videoResource = entry.getOlatResource();
		if (videoResource == null) {
			log.warn("Video resource not found for job result [uuid={}]", result.getUuid());
			return;
		}

		File masterFile = videoManager.getVideoFile(videoResource);
		if (masterFile == null) {
			log.warn("Video master file not found for job result [uuid={}]", result.getUuid());
			return;
		}

		List<VideoTranscoding> videoTranscodings = videoManager.getVideoTranscodings(videoResource);
		if (videoTranscodings == null || videoTranscodings.isEmpty()) {
			log.warn("No video transcodings found [uuid={}, resKey={}]", result.getUuid(), videoResource.getKey());
			return;
		}
		
		int resolution = result.getGenerated().getResolution();
		VideoTranscoding videoTranscoding = videoTranscodings.stream()
				.filter(t -> t.getResolution() == resolution)
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
			download(result, targetFile);
			videoTranscoding.setStatus(status);
			videoTranscoding.setSize(targetFile.length());
			if (result.getGenerated().getWidth() != null && result.getGenerated().getHeight() != null) {
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
		if (allDone(videoTranscodings)) {
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

	private void download(TranscoderJobResult result, File targetFile) throws IOException {
		String url = result.getGenerated().getUrl();
		HttpGet get = new HttpGet(url);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
			 CloseableHttpResponse httpResponse = httpClient.execute(get)) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpServletResponse.SC_OK) {
				log.warn("File download: [uuid={}, statusCode={}]", result.getUuid(), statusCode);
				throw new IOException("Failed to download");
			} else {
				log.debug("File download: [uuid={}, statusCode={}]", result.getUuid(), statusCode);
			}

			InputStream inputStream = httpResponse.getEntity().getContent();
			OutputStream outputStream = new FileOutputStream(targetFile);
			FileUtils.copy(inputStream, outputStream);
		}
	}
}
