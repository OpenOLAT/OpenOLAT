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

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoTranscodingDAO;
import org.olat.modules.video.model.TranscoderJobResult;
import org.olat.resource.OLATResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
	private static final String NOTIFY_RESULT = "notifyResult";

	@Autowired
	private AVModule avModule;
	
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	
	@Autowired
	private VFSTranscodingService vfsTranscodingService;
	
	@Autowired
	private VideoTranscodingDAO videoTranscodingDao;
	
	@Autowired
	private VideoManager videoManager;
	
	@Autowired
	private VideoModule videoModule;
	
	@Autowired
	private HttpClientService httpClientService;
	
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
			} else if (typeUri.startsWith(NOTIFY_RESULT)) {
				handleNotifyResult(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			log.error("", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void handleAudioConversion(HttpServletRequest request, HttpServletResponse response, String keyString) throws IOException {
		if (!avModule.isAudioRecordingEnabled()) {
			log.info("Blocking request for audio conversion masterfile. Audio recording disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		handleConversion(request, response, keyString);
	}

	private void handleConversion(HttpServletRequest request, HttpServletResponse response, String keyString) throws IOException {
		Long key = Long.parseLong(keyString);
		VFSMetadata metadata = vfsMetadataDao.loadMetadata(key);
		if (metadata == null) {
			log.info("Requested media VFS master file for key {} not found. No metadata.", key);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		VFSItem destinationItem = vfsTranscodingService.getDestinationItem(metadata);
		if (destinationItem instanceof LocalFileImpl localFile) {
			File masterFile = vfsTranscodingService.getMasterFile(localFile.getBasefile());
			VFSLeaf masterVfsLeaf = new LocalFileImpl(masterFile);
			VFSMediaResource masterMediaResource = new VFSMediaResource(masterVfsLeaf);
			ServletUtil.serveResource(request, response, masterMediaResource);
			return;
		}

		log.info("Requested media VFS master file for key {} not found.", key);
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void handleVideoConversion(HttpServletRequest request, HttpServletResponse response, String keyString) throws IOException {
		if (!avModule.isVideoRecordingEnabled()) {
			log.info("Blocking request for video conversion masterfile. Video recording disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		handleConversion(request, response, keyString);
	}

	private void handleVideoTranscoding(HttpServletRequest request, HttpServletResponse response, String keyString) throws IOException {
		if (!videoModule.isTranscodingEnabled()) {
			log.info("Blocking request for video transcoding masterfile. Transcoding disabled.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Long key = Long.parseLong(keyString);
		VideoTranscoding videoTranscoding = videoTranscodingDao.getVideoTranscoding(key);
		if (videoTranscoding == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		OLATResource videoResource = videoTranscoding.getVideoResource();
		File masterFile = videoManager.getVideoFile(videoResource);
		if (!masterFile.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		VFSLeaf masterVfsLeaf = new LocalFileImpl(masterFile);
		VFSMediaResource masterMediaResource = new VFSMediaResource(masterVfsLeaf);
		ServletUtil.serveResource(request, response, masterMediaResource);
	}

	private void handleNotifyResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String method = request.getMethod();
		if (!"POST".equalsIgnoreCase(method)) {
			log.error("Notify result method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}
		
		TranscoderJobResult result = readResult(request.getInputStream());
		if (!StringHelper.containsNonWhitespace(result.getUuid())) {
			log.error("Missing uuid in transcoding result");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (result.getType() == null) {
			log.error("Missing type in transcoding result");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		handleNotifyResult(request, response, result);
	}

	private TranscoderJobResult readResult(ServletInputStream inputStream) throws IOException {
		String jsonString =  IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, TranscoderJobResult.class);
	}

	private void handleNotifyResult(HttpServletRequest request, HttpServletResponse response, TranscoderJobResult result) throws IOException {
		if (result.getGenerated() == null || !StringHelper.containsNonWhitespace(result.getGenerated().getUrl())) {
			log.error("'generated.url' missing in result");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (result.getReferenceId() == null) {
			log.error("'referenceId' missing in result");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		switch (result.getType()) {
			case videoConversion, audioConversion:
				handleConversionResult(result);
				break;
			case videoTranscoding:
				handleVideoTranscodingResult(request, response, result);
				break;
		}
	}

	private void handleConversionResult(TranscoderJobResult result) {
		VFSMetadata metadata = vfsMetadataDao.loadMetadata(result.getReferenceId());
		if (metadata == null) {
			log.error("Missing metadata for conversion result {}", result.getUuid());
			return;
		}

		VFSItem destinationItem = vfsTranscodingService.getDestinationItem(metadata);
		if (destinationItem == null) {
			log.error("Missing destination item for transcoding result {}", result.getUuid());
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
			return;
		}

		String targetPath = vfsTranscodingService.getTargetFilePath(metadata);
		log.debug("Writing conversion result to target path {}", targetPath);

		File targetFile = new File(targetPath);

		try {
			download(result, targetFile);
		} catch (Exception e) {
			log.error("Failed to download conversion result to target path {} for job {}", targetPath, 
					result.getUuid(), e);
			updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
		}
	}

	private void updateStatus(VFSMetadata metadata, int status) {
		vfsTranscodingService.setStatus(metadata, status);	
		DBFactory.getInstance().commitAndCloseSession();
	}

	private void handleVideoTranscodingResult(HttpServletRequest request, HttpServletResponse response, TranscoderJobResult result) {
		log.error("Transcoding result type not supported yet: {}", result.getType());
	}

	private void download(TranscoderJobResult result, File targetFile) throws IOException {
		String url = result.getGenerated().getUrl();
		HttpGet get = new HttpGet(url);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
			 CloseableHttpResponse httpResponse = httpClient.execute(get)) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			log.debug("generated file download status code {}", statusCode);

			if (statusCode != HttpServletResponse.SC_OK) {
				log.error("Generated file download status code {} for job {}", statusCode, result.getUuid());
				throw new IOException("Failed to download transcoding result for job");
			}

			InputStream inputStream = httpResponse.getEntity().getContent();
			OutputStream outputStream = new FileOutputStream(targetFile);
			FileUtils.copy(inputStream, outputStream);
		}
	}
}
