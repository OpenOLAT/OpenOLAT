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
package org.olat.modules.project.manager;

import java.io.File;
import java.io.InputStream;

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
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectRef;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectStorage {
	
	private static final Logger log = Tracing.createLoggerFor(ProjectStorage.class);
	
	private File bcrootDirectory, rootDirectory, projectDirectory;
	
	@PostConstruct
	public void initFolders() {
		bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(bcrootDirectory, "projects");
		projectDirectory = new File(rootDirectory, "project");
		if (!projectDirectory.exists()) {
			projectDirectory.mkdirs();
		}
	}
	
	public boolean storeProjectImage(ProjProjectRef project, ProjProjectImageType type, Identity savedBy, File file, String filename) {
		return storeImage(project, type.name(), savedBy, file, filename);
	}
	
	public void deleteProjectImage(ProjProjectRef project, ProjProjectImageType type) {
		deleteContainer(project, type.name());
	}
	
	public VFSLeaf getProjectImage(ProjProjectRef project, ProjProjectImageType type) {
		return getFirstLeaf(project, type.name());
	}
	
	private boolean storeImage(ProjProjectRef project, String path, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		
		try {
			VFSContainer imageContainer = getOrCreateContainer(project, path);
			tryToStore(imageContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		
		return true;
	}
	
	public VFSLeaf storeFile(ProjProjectRef project, Identity savedBy, String filename, InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		
		try {
			VFSContainer fileContainer = getOrCreateContainer(project, "file");
			return tryToStore(fileContainer, savedBy, filename, inputStream);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public VFSContainer getOrCreateFileContainer(ProjProjectRef project) {
		return getOrCreateContainer(project, "file");
	}

	public boolean exists(ProjProjectRef project, String filename) {
		return getOrCreateFileContainer(project).resolve(filename) != null? true: false;
	}
	
	private VFSContainer getOrCreateContainer(ProjProjectRef project, String path) {
		File storage = new File(projectDirectory, project.getKey().toString());
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
	
	private VFSLeaf getFirstLeaf(ProjProjectRef project, String path) {
		if (project != null) {
			VFSContainer imageContainer = getOrCreateContainer(project, path);
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
		imageContainer.delete();
		
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(imageContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, savedBy);
	}
	
	private VFSLeaf tryToStore(VFSContainer container, Identity savedBy, String filename, InputStream inputStream) {
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(container, cleandFilename);
		VFSManager.copyContent(inputStream, vfsLeaf, savedBy);
		return vfsLeaf;
	}
	
	private void deleteContainer(ProjProjectRef project, String path) {
		VFSContainer imageContainer = getOrCreateContainer(project, path);
		imageContainer.delete();
	}
	
}
