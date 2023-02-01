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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.project.ProjProjectRef;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjFileStorage {
	
	private static final Logger log = Tracing.createLoggerFor(ProjFileStorage.class);
	
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
	
	public VFSLeaf store(ProjProjectRef project, Identity savedBy, String filename, InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		
		try {
			VFSContainer fileContainer = createFileContainer(project);
			return tryToStore(fileContainer, savedBy, filename, inputStream);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private VFSContainer createFileContainer(ProjProjectRef project) {
		File storage = new File(projectDirectory, project.getKey().toString());
		if (!storage.exists()) {
			storage.mkdirs();
		}
		storage = new File(storage, "file");
		if (!storage.exists()) {
			storage.mkdirs();
		}
		
		String relativePath = File.separator + bcrootDirectory.toPath().relativize(storage.toPath()).toString();
		return VFSManager.olatRootContainer(relativePath);
	}
	
	private VFSLeaf tryToStore(VFSContainer container, Identity savedBy, String filename, InputStream inputStream) {
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(container, cleandFilename);
		VFSManager.copyContent(inputStream, vfsLeaf, savedBy);
		return vfsLeaf;
	}

	public boolean exists(ProjProjectRef project, String filename) {
		return createFileContainer(project).resolve(filename) != null? true: false;
	}
	
}
