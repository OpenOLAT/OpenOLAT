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

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.model.TranscoderJobResult;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoTranscodingDAO;
import org.olat.resource.OLATResource;
import org.apache.commons.io.IOUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
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
			log.info("Received method not allowed: {}", method);
			DispatcherModule.sendForbidden(response);
			return;
		}
		
		TranscoderJobResult result = readResult(request.getInputStream());
		if (!StringHelper.containsNonWhitespace(result.getUuid())) {
			log.info("Missing uuid in transcoding result");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (!StringHelper.containsNonWhitespace(result.getUrl())) {
			log.info("Missing url in transcoding result: {}", result.getUuid());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		HttpGet get = new HttpGet(result.getUrl());
		try (CloseableHttpClient httpClient = HttpClients.createDefault(); 
			 CloseableHttpResponse httpResponse = httpClient.execute(get)) {
			File targetFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
			download(httpResponse, targetFile);
		}
	}

	private TranscoderJobResult readResult(ServletInputStream inputStream) throws IOException {
		String jsonString =  IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(jsonString);
		return new TranscoderJobResult(jsonObject);
	}

	private void download(CloseableHttpResponse httpResponse, File targetFile) {
		try (InputStream inputStream = httpResponse.getEntity().getContent(); 
			 OutputStream outputStream = new FileOutputStream(targetFile)) {
			FileUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			log.error("Error downloading transcoding result", e);
		}
	}
}
