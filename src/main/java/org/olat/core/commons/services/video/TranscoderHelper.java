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

import java.util.List;
import java.util.function.Consumer;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.video.model.TranscoderDeleteGeneratedReply;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2026-01-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class TranscoderHelper {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger log = Tracing.createLoggerFor(TranscoderHelper.class);
			
	@Autowired
	private HttpClientService httpClientService;
	
	/**
	 * 
	 * @param transcoderJob The transcoder job to post.
	 * @param url The URL to post the transcoder job to.
	 * @param referenceId The reference ID for the transcoder job. The semantics of this reference ID are specific to 
	 *                       the transcoder type and may be used for tracking or identification purposes.
	 * @param statusUpdater The consumer to receive the status update of the transcoder job.
	 */
	public void postTranscoderJob(TranscoderJob transcoderJob, String url, Consumer<Integer> statusUpdater) {
		try {
			HttpPost post = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(transcoderJob), ContentType.APPLICATION_JSON);
			post.setHeader("Accept", "application/json");
			post.setEntity(stringEntity);

			try (CloseableHttpClient client = httpClientService.createHttpClient();
				 CloseableHttpResponse response = client.execute(post)) {
				int statusCode = response.getStatusLine().getStatusCode();
				log.debug("Post job {} status code: {}", transcoderJob.getUuid(), statusCode);

				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");
					log.debug("Post job {} reply JSON: {}", transcoderJob.getUuid(), json);
					TranscoderJobPostReply conversionReply = objectMapper.readValue(json, TranscoderJobPostReply.class);
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
				log.warn("Failed to post job: [url='{}', referenceId={}]: {}", url, transcoderJob.getReferenceId(), e);
			}
		} catch (JsonProcessingException e) {
			log.warn("Failed to serialize transcoder job: [referenceId={}]: {}", transcoderJob.getReferenceId(), e);
		}
	}
	
	public void deleteGenerated(String url) {
		HttpDelete delete = new HttpDelete(url);
		delete.setHeader("Accept", "application/json");

		try (CloseableHttpClient client = httpClientService.createHttpClient();
			 CloseableHttpResponse response = client.execute(delete)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Delete generated: [url={},  statusCode={}]", url, statusCode);

			if (statusCode == HttpStatus.SC_OK) {
				String json = EntityUtils.toString(response.getEntity(), "UTF-8");
				log.debug("Delete generated reply: [json='{}']", json);
				TranscoderDeleteGeneratedReply reply = objectMapper.readValue(json, TranscoderDeleteGeneratedReply.class);
				if ("deleted".equalsIgnoreCase(reply.getStatus())) {
					log.debug("Delete generated reply: [status={}]", reply.getStatus());
				} else {
					log.warn("Delete generated reply: [url={}, status={}]", url, reply.getStatus());
				}
			} else {
				log.warn("Delete generated error: [url={}, statusCode={}]", url, statusCode);
			}
		} catch (Exception e) {
			log.warn("Delete generated exception: [url={}]: {}", url, e);
		}
	}
	
	public TranscoderJob createTranscoderJob(String uuid, TranscoderJobType type, Long referenceId, 
											 Long originalSize, List<Integer> resolutions) {
		TranscoderJob transcoderJob = new TranscoderJob();
		transcoderJob.setUuid(uuid);
		transcoderJob.setType(type);
		String instanceId = WebappHelper.getInstanceId();
		transcoderJob.setInstance(instanceId);
		transcoderJob.setReferenceId(referenceId);
		transcoderJob.setResolutions(resolutions);

		String apiUrl = Settings.getServerContextPathURI() + "/" + TranscoderJob.TRANSCODING_NAMESPACE;
		transcoderJob.setNotifyResultUrl(apiUrl + "/" + TranscoderJob.NOTIFY_RESULT_COMMAND);
		transcoderJob.setNotifyStatusUrl(apiUrl + "/" + TranscoderJob.NOTIFY_STATUS_COMMAND);

		String originalUrl = apiUrl + "/" + type.name() + "/" + uuid;

		TranscoderOriginal original = new TranscoderOriginal();
		original.setUrl(originalUrl);
		original.setSize(originalSize);
		transcoderJob.setOriginal(original);

		return transcoderJob;
	}
}
