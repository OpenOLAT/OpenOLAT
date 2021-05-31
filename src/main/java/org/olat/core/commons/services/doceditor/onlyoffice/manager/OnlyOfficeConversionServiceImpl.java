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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeConversionService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.model.ConversionParams;
import org.olat.core.commons.services.doceditor.onlyoffice.model.ConversionResult;
import org.olat.core.commons.services.doceditor.onlyoffice.model.Thumbnail;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 4 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeConversionServiceImpl implements OnlyOfficeConversionService {
	
	private static final Logger log = Tracing.createLoggerFor(OnlyOfficeConversionServiceImpl.class);
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeSecurityService onlyOfficeSecurityService;
	@Autowired
	private HttpClientService httpClientService;
	@Autowired
	private MapperService mapperService;

	@Override
	public FinalSize createThumbnail(VFSLeaf inputLeaf, VFSLeaf thumbnailLeaf, int maxWidth, int maxHeight) {
		log.debug("Generate thumbnail for {}, ({})", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
		
		boolean thumbnailCreated = false;
		
		Mapper mapper = new VFSMediaMapper(inputLeaf);
		MapperKey mapperKey = mapperService.register(null, mapper);
		String url = Settings.createServerURI() + mapperKey.getUrl();
		log.debug("Input leaf mapper url: {}", url);
		
		ConversionParams conversionParams = createConversionParams(inputLeaf, thumbnailLeaf, maxWidth, maxHeight, url);
		signConversionParams(conversionParams);

		try {
			thumbnailCreated = tryCreateThumbnail(conversionParams, thumbnailLeaf);
		} catch (Exception e) {
			log.error("Exception when creating thumbnail for {}, ({}).", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName(), e);
		}
		
		mapperService.cleanUp(Collections.singletonList(mapperKey));
		
		if (!thumbnailCreated) {
			log.warn("Thumbnail generation for {}, ({}) failed.", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
			return null;
		}
		
		log.debug("Thumbnail generation for {}, ({}) successful.", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
		return new FinalSize(maxWidth, maxWidth);
	}
	
	private ConversionParams createConversionParams(VFSLeaf inputLeaf, VFSLeaf thumbnailLeaf, int maxWidth,
			int maxHeight, String url) {
		ConversionParams conversionParams = new ConversionParams();
		
		//Defines the document identifier used to unambiguously identify the document file.
		conversionParams.setKey(inputLeaf.getMetaInfo().getUuid() + Formatter.formatDatetimeFilesystemSave(new Date()));
		conversionParams.setUrl(url);
		
		String suffix = FileUtils.getFileSuffix(thumbnailLeaf.getName());
		conversionParams.setOutputtype(suffix);
		// Defines the converted file name.
		conversionParams.setTitle(thumbnailLeaf.getName());
		
		Thumbnail thumbnail = new Thumbnail();
		thumbnail.setAspect(1); // Keep aspect
		thumbnail.setFirst(Boolean.TRUE); // First page only
		thumbnail.setWidth(Integer.valueOf(maxWidth));
		thumbnail.setHeight(Integer.valueOf(maxHeight));
		conversionParams.setThumbnail(thumbnail);
	
		return conversionParams;
	}
	
	private void signConversionParams(ConversionParams conversionParams) {
		@SuppressWarnings("unchecked")
		Map<String, Object> clainmsMap = objectMapper.convertValue(conversionParams, Map.class);
		String token = onlyOfficeSecurityService.getToken(clainmsMap);
		conversionParams.setToken(token);
	}

	private boolean tryCreateThumbnail(ConversionParams conversionParams, VFSLeaf thumbnailLeaf) throws JsonProcessingException {
		boolean thumbnailCreated = false;
		
		ConversionResult conversionResult = sendCreateThumbnailRequest(conversionParams);
		if (conversionResult != null) {
			Integer error = conversionResult.getError();
			if (error == null) {
				if (conversionResult.getEndConvert() != null && conversionResult.getEndConvert().booleanValue()) {
					String fileUrl = conversionResult.getFileUrl();
					if (StringHelper.containsNonWhitespace(fileUrl)) {
						thumbnailCreated = fetchThumbnail(thumbnailLeaf, fileUrl);
					}
				}
			} else {
				logConversionError(error);
			}
		}
		
		return thumbnailCreated;
	}

	private ConversionResult sendCreateThumbnailRequest(ConversionParams conversionParams)
			throws JsonProcessingException {
		HttpPost request = new HttpPost(onlyOfficeModule.getConversionUrl());
		request.setHeader("Accept", "application/json");
		request.addHeader("Authorization", getAutorisationHeader(conversionParams));
		
		StringEntity requestEntity = new StringEntity(objectMapper.writeValueAsString(conversionParams), ContentType.APPLICATION_JSON);
		request.setEntity(requestEntity);

		ConversionResult conversionResult = null;
		try (CloseableHttpClient client = httpClientService.createHttpClient();
				CloseableHttpResponse response = client.execute(request)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of create thumbnail request: {}", statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				String json = EntityUtils.toString(response.getEntity(), "UTF-8");
				log.debug("Conversion response: {}", json);
				conversionResult = objectMapper.readValue(json, ConversionResult.class);
			}
		} catch (Exception e) {
			log.error("Create thumbnail request error.", e);
		}
		return conversionResult;
	}
	
	private String getAutorisationHeader(ConversionParams conversionParams) {
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("payload", conversionParams);
		String payloadToken = onlyOfficeSecurityService.getToken(payloadMap);
		return "Bearer " + payloadToken;
	}
	
	private boolean fetchThumbnail(VFSLeaf thumbnailLeaf, String fileUrl) {
		boolean thumbnailCreated = false;
		
		HttpGet downLoadRequest = new HttpGet(fileUrl);
		try (CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse httpResponse = httpClient.execute(downLoadRequest);) {
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				InputStream content = httpResponse.getEntity().getContent();
				thumbnailCreated = VFSManager.copyContent(content, thumbnailLeaf);
			} else {
				log.warn("Get thumbnail from ONLYOFICE failed. URL: {}", fileUrl);
			}
		} catch (Exception e) {
			log.error("Get thumbnail from ONLYOFICE failed. URL: {}", fileUrl, e);
		}
		return thumbnailCreated;
	}

	/**
	 * See https://api.onlyoffice.com/editors/conversionapi#error
	 *
	 * @param error
	 */
	private void logConversionError(Integer error) {
		String description;
		switch (error.intValue()) {
		case -1: description = "Unknown error.";
			break;
		case -2: description = "Conversion timeout error.";
			break;
		case -3: description = "Conversion error.";
			break;
		case -4: description = "Error while downloading the document file to be converted.";
			break;
		case -5: description = "Incorrect password.";
			break;
		case -6: description = "Error while accessing the conversion result database.";
			break;
		case -7: description = "Input error.";
			break;
		case -8: description = "Invalid token.";
			break;
		default:
			description = "???.";
			break;
		}
		log.warn("ONLYOFFICE conversion response error. Code {}: {}", error, description);
	}

}
