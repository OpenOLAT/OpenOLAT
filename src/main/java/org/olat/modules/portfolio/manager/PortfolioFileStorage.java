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
package org.olat.modules.portfolio.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaLight;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioFileStorage implements InitializingBean {
	
	private File rootDirectory, bcrootDirectory;
	private File pagesPostersDirectory, binderPostersDirectory, mediaDirectory;
	
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
	}
	
	protected File getRootDirectory() {
		return bcrootDirectory;
	}
	
	public String getRelativePath(File file) {
		Path relPath = bcrootDirectory.toPath().relativize(file.toPath());
		return relPath.toString();
	}
	
	protected File generateBinderSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(binderPostersDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	protected File generatePageSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(pagesPostersDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public VFSContainer getMediaContainer(MediaLight media) {
		return new OlatRootFolderImpl("/" + media.getStoragePath(), null);
	}
	
	public File generateMediaSubDirectory(Media media) {
		File subDirectory = generateMediaSubDirectory();
		return new File(subDirectory, media.getKey().toString());
	}
	
	protected File generateMediaSubDirectory() {
		String cleanUuid = UUID.randomUUID().toString().replace("-", "");
		String firstToken = cleanUuid.substring(0, 2).toLowerCase();
		File dir = new File(mediaDirectory, firstToken);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
}
