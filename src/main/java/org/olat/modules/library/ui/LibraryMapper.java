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
package org.olat.modules.library.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.site.LibrarySiteDef;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * <h3>Description:</h3>
 * A cacheable mapper for the library which handle the standard
 * way to download documents by UUID and the deprecated way
 * with the file path. The security callback of the library is checked.
 * 
 * <p>
 * Initial Date:  15 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class LibraryMapper implements Mapper {
	
	private String basePath;
	private final LibraryManager libraryManager;
	
	public LibraryMapper(LibraryManager libraryManager) {
		this.libraryManager = libraryManager;
		RepositoryEntry repoEntry = libraryManager.getCatalogRepoEntry();
		if(repoEntry != null) {
			basePath = libraryManager.getDirectoryPath();
		}
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(basePath == null) {
			return new NotFoundMediaResource();
		}
	
		LibrarySiteDef siteDef = CoreSpringFactory.getImpl(LibrarySiteDef.class);
		SiteDefinitions sitesModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		SiteConfiguration libConfig = sitesModule.getConfigurationSite(siteDef);
		SiteSecurityCallback secCallback = (SiteSecurityCallback)CoreSpringFactory.getBean(libConfig.getSecurityCallbackBeanId());

		UserRequest ureq = new UserRequestImpl(relPath, request, null);
		if(!secCallback.isAllowedToLaunchSite(ureq)) {
			return new ForbiddenMediaResource();
		}
		
		if (relPath.startsWith(basePath)) {
			// increase download counter	
			VFSLeaf file = VFSManager.olatRootLeaf(relPath);
			// update cache and view
			libraryManager.increaseDownloadCount(file);
			// security check - don't deliver files from other locations!
			return new VFSMediaResource(file);
		}
		
		if (relPath.startsWith("library") || relPath.startsWith("/library")) {
			int index = relPath.indexOf("library") + 8;
			if(relPath.length() > index) {
				String uuid = relPath.substring(index);
				//remove the file name if it's in the path
				int nameIndex = uuid.indexOf('/');
				if(nameIndex > 0) {
					uuid = uuid.substring(0, nameIndex);
				}
				VFSLeaf file = libraryManager.getFileByUUID(uuid);
				if(file != null) {
					return new VFSMediaResource(file);
				}
			}
		}
		return new ForbiddenMediaResource();
	}
}