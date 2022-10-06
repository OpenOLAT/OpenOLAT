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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Initial date: 2022-09-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class VFSTranscodingServiceImpl implements VFSTranscodingService {

	private static final Logger log = Tracing.createLoggerFor(VFSTranscodingServiceImpl.class);

	private final JobKey vfsJobKey = new JobKey("vfsTranscodingJobDetail", Scheduler.DEFAULT_GROUP);

	@Value("${vfs.local.transcoding.enabled:false}")
	private boolean localTranscodingEnabled;

	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	@Autowired
	private FolderModule folderModule;

	@Autowired
	private Scheduler scheduler;

	public boolean isLocalTranscodingEnabled() {
		return localTranscodingEnabled;
	}

	@Override
	public List<VFSMetadata> getMetadatasInNeedForTranscoding() {
		return vfsMetadataDAO.getMetadatasInNeedForTranscoding();
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

	public void setStatus(VFSMetadata vfsMetadata, int status) {
		vfsMetadataDAO.setTranscodingStatus(vfsMetadata.getKey(), status);
	}

	@Override
	public void itemSavedWithTranscoding(VFSLeaf leaf, Identity savedBy) {
		vfsRepositoryService.itemSaved(leaf, savedBy);
		setStatus(leaf.getMetaInfo(), VFSMetadata.TRANSCODING_STATUS_WAITING);
	}

	@Override
	public void startTranscodingProcess() {
		if (!isLocalTranscodingEnabled()) {
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
	public void deleteMasterFile(VFSItem item) {
		if (item != null && item.canMeta() == VFSConstants.YES) {
			VFSMetadata metaInfo = item.getMetaInfo();
			if (metaInfo != null && metaInfo.isTranscoded()) {
				VFSContainer parentContainer = item.getParentContainer();
				String name = item.getName();
				String metaName = masterFilePrefix + name;
				VFSItem masterItem = parentContainer.resolve(metaName);
				if (masterItem != null) {
					masterItem.delete();
				}
			}
		}
	}
}
