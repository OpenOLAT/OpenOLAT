/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.manager;

import java.io.File;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerStorage {
	
	private static final Logger log = Tracing.createLoggerFor(TopicBrokerStorage.class);
	
	private File bcrootDirectory, rootDirectory, brokerDirectory;
	
	@PostConstruct
	public void initFolders() {
		bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(bcrootDirectory, "topicbroker");
		brokerDirectory = new File(rootDirectory, "broker");
		if (!brokerDirectory.exists()) {
			brokerDirectory.mkdirs();
		}
	}
	
	public void deleteTopicLeaf(TBTopic topic, String identifier) {
		deleteContainer(topic, identifier);
	}
	
	public VFSLeaf getTopicLeaf(TBTopic topic, String identifier) {
		return getFirstLeaf(topic, identifier);
	}
	
	public boolean storeTopicLeaf(TBTopic topic, String path, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		
		try {
			VFSContainer vfsContainer = getOrCreateContainer(topic, path);
			tryToStore(vfsContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		
		return true;
	}
	
	private VFSContainer getOrCreateContainer(TBTopic topic, String path) {
		File storage = new File(brokerDirectory, topic.getBroker().getKey().toString());
		if (!storage.exists()) {
			storage.mkdirs();
		}
		storage = new File(storage, topic.getKey().toString());
		if (!storage.exists()) {
			storage.mkdirs();
		}
		storage = new File(storage, path);
		if (!storage.exists()) {
			storage.mkdirs();
		}
		
		String relativePath = File.separator + bcrootDirectory.toPath().relativize(storage.toPath()).toString();
		return VFSManager.olatRootContainer(relativePath);
	}
	
	private VFSLeaf getFirstLeaf(TBTopic topic, String path) {
		if (topic != null) {
			VFSContainer vfsContainer = getOrCreateContainer(topic, path);
			if (!vfsContainer.getItems().isEmpty()) {
				VFSItem vfsItem = vfsContainer.getItems().get(0);
				if (vfsItem instanceof VFSLeaf) {
					return (VFSLeaf)vfsItem;
				}
			}
		}
		return null;
	}
	
	private void tryToStore(VFSContainer vfsContainer, Identity savedBy, File file, String filename) {
		vfsContainer.deleteSilently();
		
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(vfsContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, savedBy);
	}
	
	private void deleteContainer(TBTopic topic, String path) {
		VFSContainer vfsContainer = getOrCreateContainer(topic, path);
		vfsContainer.deleteSilently();
	}

	public void deleteLeafs(TBBroker broker) {
		File storage = new File(brokerDirectory, broker.getKey().toString());
		String relativePath = File.separator + bcrootDirectory.toPath().relativize(storage.toPath()).toString();
		VFSManager.olatRootContainer(relativePath).deleteSilently();
	}

}
