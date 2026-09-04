/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.manager;

import java.io.InputStream;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsRecording;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 26 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TeamsRecordingStorage {
	
	private static final Logger log = Tracing.createLoggerFor(TeamsRecordingStorage.class);

	private VFSContainer recordingsDirectory;
	
	private TeamsModule teamsModule;
	private VFSRepositoryService vfsRepositoryService;
	
	@Autowired
	public TeamsRecordingStorage(TeamsModule teamsModule, VFSRepositoryService vfsRepositoryService) {
		this.teamsModule = teamsModule;
		this.vfsRepositoryService = vfsRepositoryService;
	}
	
	@PostConstruct
	public void initFolders() {
		String recordingsPath = teamsModule.getRecordingsDir();
		recordingsDirectory = VFSManager.olatRootContainer(recordingsPath);
	}

	public VFSContainer getRecordingsContainer(TeamsMeeting meeting) {
		String subPath = meeting.getKey().toString();
		VFSItem item = recordingsDirectory.resolve(subPath);
		if(item == null) {
			return recordingsDirectory.createChildContainer(subPath);
		}
		if(item instanceof VFSContainer container) {
			return container;
		}
		log.error("File found : {} ({})", subPath, item);
		return null;
	}
	
	protected void deleteRecording(TeamsRecording recording) {
		if(recording.getRecordingMetadata() == null) return;
		
		VFSItem item = vfsRepositoryService.getItemFor(recording.getRecordingMetadata());
		if(item != null) {
			VFSContainer container = item.getParentContainer();
			item.deleteSilently();
			if(container != null && container.getItems().isEmpty()) {
				container.deleteSilently();
			}
		}
		recording.setRecordingMetadata(null);
	}

	protected VFSLeaf storeRecording(TeamsMeeting meeting, String filename, Identity savedBy, InputStream stream) {
		if (stream == null) {
			return null;
		}
		
		VFSLeaf recordingLeaf = null;
		try {
			VFSContainer vfsContainer = getRecordingsContainer(meeting);
			if(vfsContainer.resolve(filename) != null) {
				filename = VFSManager.rename(vfsContainer, filename);
			}
			recordingLeaf = vfsContainer.createChildLeaf(filename);
			VFSManager.copyContent(stream, recordingLeaf, savedBy);
		} catch (Exception e) {
			log.error("", e);
		}
		return recordingLeaf;
	}
}
