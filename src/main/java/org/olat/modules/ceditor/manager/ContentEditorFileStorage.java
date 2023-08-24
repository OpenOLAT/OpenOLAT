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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.Assignment;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.cemedia.Media;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ContentEditorFileStorage implements InitializingBean {
	
	private static final int MAX_SUB_DIRECTORIES = 32000;
	
	private File rootDirectory;
	private File bcrootDirectory;
	private File pagesPostersDirectory;
	private File binderPostersDirectory;
	private File mediaDirectory;
	private File assignmentDirectory;
	
	@Override
	public void afterPropertiesSet() {
		bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(FolderConfig.getCanonicalRoot(), "portfolio");
		if(!rootDirectory.exists()) {
			rootDirectory.mkdirs();
		}
		
		File postersDirectory = new File(rootDirectory, "posters");
		binderPostersDirectory = new File(postersDirectory, "binders");
		pagesPostersDirectory = new File(postersDirectory, "pages");
		mediaDirectory = new File(rootDirectory, "artefacts");
		assignmentDirectory = new File(rootDirectory, "assignments");
	}
	
	public File getRootDirectory() {
		return bcrootDirectory;
	}
	
	public String getRelativePath(File file) {
		Path relPath = bcrootDirectory.toPath().relativize(file.toPath());
		return relPath.toString();
	}
	
	public File generateBinderSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(binderPostersDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public File generatePageSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(pagesPostersDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public VFSContainer generatePreviewSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		VFSContainer rootContainer = VFSManager.olatRootContainer("/portfolio");
		VFSContainer previewContainer = VFSManager.getOrCreateContainer(rootContainer, "previews");
		return VFSManager.getOrCreateContainer(previewContainer, firstToken);
	}
	
	/**
	 * Assignment have a directory for them alone.
	 * 
	 * @return
	 */
	public File generateAssignmentSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File parentDir = new File(assignmentDirectory, firstToken);
		File dir = new File(parentDir, "00001");
		if(!dir.exists()) {
			dir.mkdirs();
		} else {
			String[] children = parentDir.list();
			Set<String> names = new HashSet<>();
			for(String child:children) {
				names.add(child);
			}
			
			for(int i=1; i<MAX_SUB_DIRECTORIES; i++) {
				String potentielName = Integer.toString(i);
				if(potentielName.length() == 1) {
					potentielName = "0000" + potentielName;
				} else if(potentielName.length() == 2) {
					potentielName = "000" + potentielName;
				} else if(potentielName.length() == 3) {
					potentielName = "00" + potentielName;
				} else if(potentielName.length() == 4) {
					potentielName = "0" + potentielName;
				}

				if(!names.contains(potentielName)) {
					dir = new File(parentDir, potentielName);
					dir.mkdirs();
					break;
				}
			}
		}
		return dir;
	}
	
	public VFSContainer getAssignmentContainer(Assignment assignment) {
		if(assignment == null || assignment.getKey() == null
				|| !StringHelper.containsNonWhitespace(assignment.getStorage())) {
			return null;
		}
		return VFSManager.olatRootContainer("/" + assignment.getStorage(), null);
	}
	
	public File getAssignmentDirectory(Assignment assignment) {
		if(StringHelper.containsNonWhitespace(assignment.getStorage())) {
			return new File(bcrootDirectory, assignment.getStorage());
		}
		return null;
	}
	
	/**
	 * 
	 * @param assignment The assignment
	 * @return The first relevant document in the assignment directory or null.
	 */
	public File getAssignmentFirstFile(Assignment assignment) {
		File dir = getAssignmentDirectory(assignment);
		File[] files = dir.listFiles(new SystemFilenameFilter(true, false));
		if(files != null && files.length >= 1) {
			return files[0];
		}
		return null;
	}
	
	public File getMediaDirectory(StoredData data) {
		return new File(FolderConfig.getCanonicalRoot(), data.getStoragePath());
	}
	
	public File getMediaRootFile(StoredData data) {
		if(data != null && StringHelper.containsNonWhitespace(data.getStoragePath())
				&& StringHelper.containsNonWhitespace(data.getRootFilename())) {
			File directory = getMediaDirectory(data);
			return new File(directory, data.getRootFilename());
		}
		return null;
	}
	
	public VFSContainer getMediaContainer(StoredData data) {
		return VFSManager.olatRootContainer("/" + data.getStoragePath(), null);
	}
	
	public VFSItem getMediaRootItem(StoredData data) {
		VFSContainer storageContainer = getMediaContainer(data);
		return storageContainer.resolve(data.getRootFilename());
	}
	
	public VFSMetadata getMediaRootItemMetadata(StoredData data) {
		VFSContainer storageContainer = getMediaContainer(data);
		VFSItem item = storageContainer.resolve(data.getRootFilename());
		if(item != null && item.canMeta() == VFSConstants.YES) {
			return item.getMetaInfo();
		}
		return null;
	}

	public File generateMediaSubDirectory(Media media) {
		File mediaDir = null;
		for(int i=0; i<1000; i++) {
			File subDirectory = generateMediaSubDirectory();
			mediaDir = new File(subDirectory, media.getKey().toString());
			if(!mediaDir.exists()) {
				mediaDir.mkdirs();
				break;
			}
		}
		return mediaDir;
	}
	
	private File generateMediaSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(mediaDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
