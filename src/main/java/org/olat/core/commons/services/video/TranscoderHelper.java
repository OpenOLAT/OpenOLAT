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
package org.olat.core.commons.services.video;

import java.util.function.Consumer;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.video.model.TranscoderJob;
import org.olat.core.commons.services.video.model.TranscoderJobPostReply;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.olat.core.commons.services.video.model.TranscoderOriginal;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.httpclient.HttpClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2026-01-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscoderHelper {

	private static final Logger log = Tracing.createLoggerFor(TranscoderHelper.class);
			
	public static void postTranscoderJob(TranscoderJob transcoderJob, String url, Long referenceId, Consumer<Integer> statusUpdater) {
		ObjectMapper mapper = new ObjectMapper();
		HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);
		if (httpClientService == null) {
			log.error("Can't post transcoder job as HttpClientService is null!");
			return;
		}

		try {
			HttpPost post = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(mapper.writeValueAsString(transcoderJob), ContentType.APPLICATION_JSON);
			post.setHeader("Accept", "application/json");
			post.setEntity(stringEntity);

			try (CloseableHttpClient client = httpClientService.createHttpClient();
				 CloseableHttpResponse response = client.execute(post)) {
				int statusCode = response.getStatusLine().getStatusCode();
				log.debug("Post job {} status code: {}", transcoderJob.getUuid(), statusCode);

				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");
					log.debug("Post job {} reply JSON: {}", transcoderJob.getUuid(), json);
					TranscoderJobPostReply conversionReply = mapper.readValue(json, TranscoderJobPostReply.class);
					if ("received".equalsIgnoreCase(conversionReply.getStatus())) {
						log.debug("Post job {} reply: {}", transcoderJob.getUuid(), conversionReply.getStatus());
						statusUpdater.accept(VFSMetadata.TRANSCODING_STATUS_STARTED);
					} else {
						log.warn("Post job {} reply: {}", transcoderJob.getUuid(), conversionReply.getStatus());
						statusUpdater.accept(VFSMetadata.TRANSCODING_STATUS_ERROR);
					}
				} else {
					log.warn("Post job {} failed with status code {}", transcoderJob.getUuid(), statusCode);
					statusUpdater.accept( VFSMetadata.TRANSCODING_STATUS_ERROR);
				}
			} catch (Exception e) {
				log.warn("Failed to post job to URL {} for item {}: {}", url, referenceId, e);
			}
		} catch (JsonProcessingException e) {
			log.warn("Failed to create conversion job for item {}: {}", referenceId, e);
		}
	}
	
	public static TranscoderJob createTranscoderJob(String uuid, TranscoderJobType type, Long referenceId, 
													Long originalSize, Integer resolution) {
		TranscoderJob transcoderJob = new TranscoderJob();
		transcoderJob.setUuid(uuid);
		transcoderJob.setType(type);
		String instanceId = WebappHelper.getInstanceId();
		transcoderJob.setInstance(instanceId);
		transcoderJob.setReferenceId(referenceId);

		String apiUrl = Settings.getServerContextPathURI() + "/" + TranscoderJob.TRANSCODING_NAMESPACE;
		transcoderJob.setNotifyResultUrl(apiUrl + "/" + TranscoderJob.NOTIFY_RESULT_COMMAND);

		String originalUrl = apiUrl + "/" + type.name() + "/" + referenceId;

		TranscoderOriginal original = new TranscoderOriginal();
		original.setUrl(originalUrl);
		original.setSize(originalSize);
		transcoderJob.setOriginal(original);
		original.setResolution(resolution);

		return transcoderJob;
	}
}
