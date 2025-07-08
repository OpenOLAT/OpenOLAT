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
package org.olat.modules.curriculum.manager;

import java.io.File;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Dec 3, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumStorage {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumStorage.class);
	
	private File bcrootDirectory;
	private File elementsDirectory;
	
	@Autowired
	private FolderModule folderModule;

	@PostConstruct
	public void initFolders() {
		bcrootDirectory = new File(folderModule.getCanonicalRoot());
		File rootDirectory = new File(bcrootDirectory, "curriculum");
		elementsDirectory = new File(rootDirectory, "element");
		if (!elementsDirectory.exists()) {
			elementsDirectory.mkdirs();
		}
	}

	public VFSContainer getMediaContainer(CurriculumElementRef curriculumElement) {
		VFSContainer elementsContainer = VFSManager.olatRootContainer(elementsDirectory.getPath());
		VFSItem elementItem = elementsContainer.resolve(curriculumElement.getKey().toString());
		VFSContainer elementContainer;
		if (elementItem instanceof VFSContainer container) {
			elementContainer = container;
		} else {
			elementContainer = elementsContainer.createChildContainer(curriculumElement.getKey().toString());
		}
		
		elementContainer = VFSManager.olatRootContainer(elementContainer.getRelPath());
		
		VFSItem item = elementContainer.resolve("media");
		VFSContainer mediaContainer;
		if (item instanceof VFSContainer container) {
			mediaContainer = container;
		} else {
			mediaContainer = elementContainer.createChildContainer("media");
		}
		return mediaContainer;
	}
	
	public boolean storeCurriculumElementFile(CurriculumElementRef element, CurriculumElementFileType type, Identity savedBy, File file, String filename) {
		return storeImage(element, type.name(), savedBy, file, filename);
	}
	
	public void deleteCurriculumElementFile(CurriculumElementRef element, CurriculumElementFileType type) {
		deleteContainer(element, type.name());
	}
	
	public VFSLeaf getCurriculumElementFile(CurriculumElementRef element, CurriculumElementFileType type) {
		return getFirstLeaf(element, type.name());
	}
	
	private boolean storeImage(CurriculumElementRef element, String path, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		
		try {
			VFSContainer imageContainer = getOrCreateContainer(element, path);
			tryToStore(imageContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		
		return true;
	}
	
	private VFSContainer getOrCreateContainer(CurriculumElementRef element, String path) {
		File storage = new File(elementsDirectory, element.getKey().toString());
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
	
	private VFSLeaf getFirstLeaf(CurriculumElementRef element, String path) {
		if (element != null) {
			VFSContainer imageContainer = getOrCreateContainer(element, path);
			if (!imageContainer.getItems().isEmpty()) {
				VFSItem vfsItem = imageContainer.getItems().get(0);
				if (vfsItem instanceof VFSLeaf) {
					return (VFSLeaf)vfsItem;
				}
			}
		}
		return null;
	}
	
	private void tryToStore(VFSContainer imageContainer, Identity savedBy, File file, String filename) {
		imageContainer.deleteSilently();
		
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(imageContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, savedBy);
	}
	
	private void deleteContainer(CurriculumElementRef element, String path) {
		VFSContainer imageContainer = getOrCreateContainer(element, path);
		imageContainer.deleteSilently();
	}

}
