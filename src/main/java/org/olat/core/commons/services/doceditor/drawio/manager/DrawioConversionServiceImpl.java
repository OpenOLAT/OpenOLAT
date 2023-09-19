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
package org.olat.core.commons.services.doceditor.drawio.manager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.drawio.DrawioConversionService;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DrawioConversionServiceImpl implements DrawioConversionService {
	
	private static final Logger log = Tracing.createLoggerFor(DrawioConversionServiceImpl.class);
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private HttpClientService httpClientService;
	
	@Override
	public FinalSize createThumbnail(VFSLeaf inputLeaf, VFSLeaf thumbnailLeaf, int maxWidth, int maxHeight) {
		log.debug("Generate thumbnail for {}, ({})", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
		
		boolean thumbnailCreated = false;
		
		try {
			thumbnailCreated = tryCreateThumbnail(inputLeaf, thumbnailLeaf, maxWidth, maxHeight);
		} catch (Exception e) {
			log.error("Exception when creating thumbnail for {}, ({}).", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName(), e);
		}
		
		if (!thumbnailCreated) {
			log.warn("Thumbnail generation for {}, ({}) failed.", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
			return null;
		}
		
		log.debug("Thumbnail generation for {}, ({}) successful.", inputLeaf.getMetaInfo().getUuid(), inputLeaf.getName());
		return new FinalSize(maxWidth, maxWidth);
	}
	
	private boolean tryCreateThumbnail(VFSLeaf inputLeaf, VFSLeaf thumbnailLeaf, int maxWidth, int maxHeight) throws UnsupportedEncodingException {
		HttpPost request = new HttpPost(drawioModule.getExportUrl());
		request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		
		// see https://github.com/jgraph/draw-image-export2
		List<NameValuePair> params = new ArrayList<>();
		String xml = FileUtils.load(inputLeaf.getInputStream(), "utf-8");
		params.add(new BasicNameValuePair("xml", xml));
		params.add(new BasicNameValuePair("w", String.valueOf(maxWidth)));
		params.add(new BasicNameValuePair("h", String.valueOf(maxHeight)));
		params.add(new BasicNameValuePair("format", "jpg"));
		params.add(new BasicNameValuePair("embedXml", "0"));
		params.add(new BasicNameValuePair("embedData", "0"));
		params.add(new BasicNameValuePair("border", "0"));
		request.setEntity(new UrlEncodedFormEntity(params));
		
		boolean thumbnailCreated = false;
		try (CloseableHttpClient client = httpClientService.createHttpClient();
				CloseableHttpResponse response = client.execute(request)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Status code of create thumbnail request: {}", statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				if (entity != null && entity.getContentLength() > 1) {
					entity.writeTo(thumbnailLeaf.getOutputStream(false));
					thumbnailCreated = true;
				} else {
					log.warn("draw.io conversion returned empty file.");
				}
			} else {
				log.warn("draw.io conversion return status: {}", statusCode);
			}
		} catch (Exception e) {
			log.error("Create thumbnail request error.", e);
		}
		return thumbnailCreated;
	}

}
