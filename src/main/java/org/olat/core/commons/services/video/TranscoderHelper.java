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

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.video.model.TranscoderDeleteGeneratedReply;
import org.olat.core.commons.services.video.model.TranscoderJob;
import org.olat.core.commons.services.video.model.TranscoderJobPostReply;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.olat.core.commons.services.video.model.TranscoderOriginal;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.httpclient.ProtectionProfile;
import org.olat.modules.video.VideoModule;

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
	
	public static final ProtectionProfile PROTECTION_PROFILE = ProtectionProfile.CONFIGURED;

	private static final String HMAC_ALGORITHM = "HmacSHA256";

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

			try (CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE);
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

		try (CloseableHttpClient client = httpClientService.createHttpClient(PROTECTION_PROFILE);
			 CloseableHttpResponse response = client.execute(delete)) {
			int statusCode = response.getStatusLine().getStatusCode();
			log.debug("Delete generated: [url={},  statusCode={}]", url, statusCode);

			if (statusCode == HttpStatus.SC_OK) {
				String json = EntityUtils.toString(response.getEntity(), "UTF-8");
				log.debug("Delete generated reply: [json='{}']", json);
				TranscoderDeleteGeneratedReply reply = objectMapper.readValue(json, TranscoderDeleteGeneratedReply.class);
				log.debug("Delete generated reply: [deleted='{}']", String.join(", ", reply.getDeleted()));
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
		transcoderJob.setGenerateSubtitles(type == TranscoderJobType.videoTranscoding);

		String apiUrl = Settings.getServerContextPathURI() + "/" + TranscoderJob.TRANSCODING_NAMESPACE;
		String token = createCallbackToken(type, uuid, referenceId);
		transcoderJob.setNotifyResultUrl(apiUrl + "/" + TranscoderJob.NOTIFY_RESULT_COMMAND + "/" + token);
		transcoderJob.setNotifyStatusUrl(apiUrl + "/" + TranscoderJob.NOTIFY_STATUS_COMMAND + "/" + token);

		String originalUrl = apiUrl + "/" + type.name() + "/" + uuid;

		TranscoderOriginal original = new TranscoderOriginal();
		original.setUrl(originalUrl);
		original.setSize(originalSize);
		transcoderJob.setOriginal(original);

		return transcoderJob;
	}

	/**
	 * Signs the callback of a job with an HMAC token over its type, uuid and
	 * reference id, so that {@link #verifyCallbackToken} can later recognize a
	 * {@code notifyResult} or {@code notifyStatus} request as genuinely
	 * originating from the job this instance started (OO-9999, WP2). The token
	 * is a path segment of {@code notifyResultUrl} and {@code notifyStatusUrl},
	 * not a query parameter, so it survives, whichever way the transcoder service
	 * handles query strings.
	 *
	 * @param type The job type
	 * @param uuid The job uuid
	 * @param referenceId The reference id of the job
	 * @return The Base64url-encoded token (no padding), safe as a URL path segment
	 */
	private String createCallbackToken(TranscoderJobType type, String uuid, Long referenceId) {
		byte[] mac = hmac(getCallbackSecret(), callbackTokenData(type, uuid, referenceId));
		return Base64.getUrlEncoder().withoutPadding().encodeToString(mac);
	}

	/**
	 * Verifies a callback token extracted from the request path against the
	 * {@code type}, {@code uuid} and {@code referenceId} claimed in the request
	 * body. Call this before any other processing of a {@code notifyResult} or
	 * {@code notifyStatus} request: a request whose body does not match the
	 * triple the token was signed for cannot come from a job this instance
	 * started, whatever values it claims (OO-9999, WP3 uses this method).
	 *
	 * @param token The token from the callback URL path. Can be null or malformed
	 * @param type The type claimed in the request body
	 * @param uuid The uuid claimed in the request body
	 * @param referenceId The reference id claimed in the request body
	 * @return true if the token matches the expected token for this job data
	 */
	public boolean verifyCallbackToken(String token, TranscoderJobType type, String uuid, Long referenceId) {
		if (!StringHelper.containsNonWhitespace(token) || type == null || uuid == null || referenceId == null) {
			return false;
		}
		byte[] actual;
		try {
			actual = Base64.getUrlDecoder().decode(token);
		} catch (IllegalArgumentException e) {
			return false;
		}
		byte[] expected = hmac(getCallbackSecret(), callbackTokenData(type, uuid, referenceId));
		return MessageDigest.isEqual(expected, actual);
	}

	/**
	 * The secret is looked up on demand and not injected: VideoModule depends on
	 * VideoHandler, which depends on VideoManagerImpl, which depends on this
	 * helper, so an {@code @Autowired} VideoModule would close a dependency cycle.
	 *
	 * @return The secret that signs and verifies the callback tokens
	 */
	String getCallbackSecret() {
		return CoreSpringFactory.getImpl(VideoModule.class).getCallbackSecret();
	}

	private static String callbackTokenData(TranscoderJobType type, String uuid, Long referenceId) {
		return type.name() + "|" + uuid + "|" + referenceId;
	}

	private static byte[] hmac(String secret, String data) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			// HmacSHA256 is part of every JVM, and VideoModule.getCallbackSecret() always
			// returns a generated or configured, non-empty secret.
			throw new IllegalStateException("Cannot sign or verify a transcoder callback token", e);
		}
	}
}
