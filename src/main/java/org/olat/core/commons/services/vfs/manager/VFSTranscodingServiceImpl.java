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
package org.olat.core.commons.services.vfs.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.model.TranscoderJob;
import org.olat.modules.video.model.TranscoderJobPostReply;
import org.olat.modules.video.model.TranscoderJobType;
import org.olat.modules.video.model.TranscoderOriginal;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2022-09-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class VFSTranscodingServiceImpl implements VFSTranscodingService {

	private static final Logger log = Tracing.createLoggerFor(VFSTranscodingServiceImpl.class);
	
	private static final String POST_JOB_COMMAND = "postJob";

	private final JobKey vfsJobKey = new JobKey("vfsTranscodingJobDetail", Scheduler.DEFAULT_GROUP);

	private static ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	@Autowired
	private FolderModule folderModule;

	@Autowired
	private Scheduler scheduler;
	
	@Autowired
	private AVModule avModule;

	@Autowired
	private HttpClientService httpClientService;
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public boolean isLocalVideoConversionEnabled() {
		return avModule.isLocalVideoConversionEnabled();
	}

	@Override
	public boolean isVideoConversionServiceConfigured() {
		return StringHelper.containsNonWhitespace(avModule.getVideoConversionServiceUrl());
	}
	
	@Override
	public boolean isAudioConversionServiceConfigured() {
		return StringHelper.containsNonWhitespace(avModule.getAudioConversionServiceUrl());
	}

	@Override
	public boolean isLocalAudioConversionEnabled() {
		return avModule.isLocalAudioConversionEnabled();
	}

	@Override
	public boolean isConversionJobEnabled() {
		if (StringHelper.containsNonWhitespace(avModule.getVideoConversionServiceUrl()) || 
				StringHelper.containsNonWhitespace(avModule.getAudioConversionServiceUrl())) {
			return true;
		}
		return avModule.isLocalVideoConversionEnabled() || avModule.isLocalAudioConversionEnabled();
	}

	@Override
	public List<VFSMetadata> getMetadatasInNeedForTranscoding() {
		return vfsMetadataDAO.getMetadatasInNeedForTranscoding();
	}

	@Override
	public List<VFSMetadata> getMetadatasWithUnresolvedTranscodingStatus() {
		return vfsMetadataDAO.getMetadatasWithUnresolvedTranscodingStatus();
	}

	@Override
	public VFSItem getDestinationItem(VFSMetadata vfsMetadata) {
		return vfsRepositoryService.getItemFor(vfsMetadata);
	}

	@Override
	public String getDirectoryString(VFSItem vfsItem) {
		String relativePath = vfsItem.getMetaInfo().getRelativePath();
		Path directoryPath = Paths.get(folderModule.getCanonicalRoot(), relativePath);
		return directoryPath.toString();
	}

	@Override
	public void setStatus(VFSMetadata vfsMetadata, int status) {
		vfsMetadataDAO.setTranscodingStatus(vfsMetadata.getKey(), status);
		if (status == VFSMetadata.TRANSCODING_STATUS_WAITING) {
			if (vfsRepositoryService.getItemFor(vfsMetadata) instanceof LocalFileImpl mediaLeaf && mediaLeaf.getSize() == 0) {
				File masterFile = getMasterFile(mediaLeaf.getBasefile());
				if (masterFile != null) {
					vfsMetadataDAO.setFileSize(vfsMetadata.getKey(), masterFile.length());
				}
			}
		} else if (status == VFSMetadata.TRANSCODING_STATUS_DONE) {
			if (vfsRepositoryService.getItemFor(vfsMetadata) instanceof VFSLeaf leaf && leaf.getSize() > 0) {
				long fileSize = leaf.getSize();
				vfsMetadataDAO.setFileSize(vfsMetadata.getKey(), fileSize);
			}
		}
	}

	@Override
	public void itemSavedWithTranscoding(VFSLeaf leaf, Identity savedBy) {
		vfsRepositoryService.itemSaved(leaf, savedBy);
		setStatus(leaf.getMetaInfo(), VFSMetadata.TRANSCODING_STATUS_WAITING);
	}

	@Override
	public void startTranscodingProcess() {
		if (!isConversionJobEnabled()) {
			return;
		}

		try {
			scheduler.triggerJob(vfsJobKey);
		} catch (SchedulerException e) {
			log.error("Cannot start VFS transcoding job", e);
		}
	}

	@Override
	public void fileDoneEvent(VFSMetadata vfsMetadata) {
		VFSTranscodingDoneEvent doneEvent = new VFSTranscodingDoneEvent(vfsMetadata.getFilename());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(doneEvent, ores);
	}

	@Override
	public File getMasterFile(File mediaFile) {
		if (!mediaFile.exists()) {
			return null;
		}
		String parent = mediaFile.getParent();
		if (parent == null) {
			return null;
		}
		String masterFileName = masterFilePrefix + mediaFile.getName();
		File masterFile = new File(parent, masterFileName);
		if (!masterFile.exists()) {
			return null;
		}
		return masterFile;
	}

	@Override
	public void deleteMasterFile(VFSItem item) {
		if (item != null && item.canMeta() == VFSStatus.YES) {
			VFSMetadata metaInfo = item.getMetaInfo();
			if (metaInfo != null && metaInfo.isTranscoded()) {
				VFSContainer parentContainer = item.getParentContainer();
				String name = item.getName();
				String metaName = masterFilePrefix + name;
				VFSItem masterItem = parentContainer.resolve(metaName);
				if (masterItem != null) {
					masterItem.deleteSilently();
				}
			}
		}
	}

	@Override
	public String getHandBrakeCliExecutable() {
		return avModule.getHandBrakeCliCommandPath();
	}

	@Override
	public String getFfmpegExecutable() {
		return avModule.getFfmpegPath();
	}

	@Override
	public void registerForJobDoneEvent(GenericEventListener listener) {
		if (isConversionJobEnabled()) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(listener, null,
					VFSTranscodingService.ores);
		}
	}

	@Override
	public void deregisterForJobDoneEvent(GenericEventListener listener) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(listener,
				VFSTranscodingService.ores);
	}
	
	@Override
	public void postConversionJob(VFSMetadata metadata, TranscoderJobType type) {
		TranscoderJob transcodingJob = createTranscoderJob(metadata, type);

		try {
			String url = getConversionServiceUrl(type) + "/" + POST_JOB_COMMAND;
			HttpPost post = new HttpPost(url);
			StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(transcodingJob), ContentType.APPLICATION_JSON);
			post.setHeader("Accept", "application/json");
			post.setEntity(stringEntity);

			try (CloseableHttpClient client = httpClientService.createHttpClient();
				 CloseableHttpResponse response = client.execute(post)) {
				int statusCode = response.getStatusLine().getStatusCode();
				log.debug("Post job {} status code: {}", transcodingJob.getUuid(), statusCode);

				if (statusCode == HttpStatus.SC_OK) {
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");
					log.debug("Post job {} reply JSON: {}", transcodingJob.getUuid(), json);
					TranscoderJobPostReply conversionReply = objectMapper.readValue(json, TranscoderJobPostReply.class);
					if ("received".equalsIgnoreCase(conversionReply.getStatus())) {
						log.debug("Post job {} reply: {}", transcodingJob.getUuid(), conversionReply.getStatus());
						updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_STARTED);
					} else {
						log.error("Post job {} reply: {}", transcodingJob.getUuid(), conversionReply.getStatus());
						updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
					}
				} else {
					log.error("Post job {} failed with status code {}", transcodingJob.getUuid(), statusCode);
					updateStatus(metadata, VFSMetadata.TRANSCODING_STATUS_ERROR);
				}
			} catch (Exception e) {
				log.error("Failed to post job to URL {} for metadata item {}: {}", url, metadata.getKey(), e);
			}
		} catch (JsonProcessingException e) {
			log.error("Failed to create conversion job for metadata item {}: {}", metadata.getKey(), e);
		}
	}

	private void updateStatus(VFSMetadata metadata, int status) {
		setStatus(metadata, status);
		dbInstance.commitAndCloseSession();
	}


	private TranscoderJob createTranscoderJob(VFSMetadata metadata, TranscoderJobType type) {
		TranscoderJob transcodingJob = new TranscoderJob();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		transcodingJob.setUuid(uuid);
		transcodingJob.setType(type);
		String instanceId = WebappHelper.getInstanceId();
		transcodingJob.setInstance(instanceId);
		transcodingJob.setReferenceId(metadata.getKey());

		String apiUrl = Settings.getServerContextPathURI() + "/" + TRANSCODING_URL_PART;

		transcodingJob.setNotifyResultUrl(apiUrl + "/" + NOTIFY_RESULT_URL_PART);

		String originalUrl = apiUrl + "/" + type.name() + "/" + metadata.getKey();

		TranscoderOriginal original = new TranscoderOriginal();
		original.setUrl(originalUrl);
		original.setSize(getOriginalSize(metadata));
		transcodingJob.setOriginal(original);

		return transcodingJob;
	}

	private Long getOriginalSize(VFSMetadata metadata) {
		String masterFileName = masterFilePrefix + metadata.getFilename();
		Path directoryPath = Paths.get(folderModule.getCanonicalRoot(), metadata.getRelativePath(), masterFileName);
		if (!Files.exists(directoryPath)) {
			return null;
		}
		File file = directoryPath.toFile();
		return FileUtils.sizeOf(file);
	}

	private String getConversionServiceUrl(TranscoderJobType type) {
		if (TranscoderJobType.videoTranscoding.equals(type)) {
			return avModule.getVideoConversionServiceUrl();
		}
		if (TranscoderJobType.audioConversion.equals(type)) {
			return avModule.getAudioConversionServiceUrl();
		}
		return null;
	}
}
