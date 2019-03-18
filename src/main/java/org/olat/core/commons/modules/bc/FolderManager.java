/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.helpers.Settings;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * Initial Date:  18.12.2002
 *
 * @author Mike Stock
 */
public class FolderManager {
	
	private static FolderModule folderModule;

	/**
	 * Get this path as a full WebDAV link
	 * @return Full link representation.
	 */
	public static String getWebDAVHttp() {
		if(Settings.isInsecurePortAvailable()) {
			return Settings.getInsecureServerContextPathURI() + WebappHelper.getServletContextPath() + "/webdav";
		}
		return null;
	}
	
	public static String getWebDAVHttps() {
		if(Settings.isSecurePortAvailable()) {
			return Settings.getSecureServerContextPathURI() + WebappHelper.getServletContextPath() + "/webdav";
		}
		return null;
	}
	
	/**
	 * @param bcBase
	 * @param newerThan
	 * @return a List of FileInfo
	 */
	public static List<FileInfo> getFileInfos(final String olatRelPath, Date newerThan) {
		
		final List<FileInfo> fileInfos = new ArrayList<>();
		final long newerThanLong = newerThan.getTime();
		LocalFolderImpl rootFolder = VFSManager.olatRootContainer(olatRelPath, null);
		getFileInfosRecursively(rootFolder, fileInfos, newerThanLong, olatRelPath.length());
		return fileInfos;
	}

	private static void getFileInfosRecursively(VFSItem relPath, List<FileInfo> fileInfos, long newerThan, int basePathlen) {
		if (relPath instanceof VFSLeaf) {
			// is a file
			VFSLeaf leaf = (VFSLeaf)relPath;
			if(leaf.canMeta() == VFSConstants.YES) {
				long lastModified = leaf.getLastModified();
				if (lastModified > newerThan) {
					VFSMetadata meta = leaf.getMetaInfo();
					String bcrootPath = relPath.getRelPath();
					String bcRelPath = bcrootPath.substring(basePathlen);
					fileInfos.add(new FileInfo(bcRelPath, meta, new Date(lastModified)));
				}
			}
		} else if(relPath instanceof VFSContainer) {
			// is a folder
			VFSContainer container = (VFSContainer)relPath;
			for (VFSItem item : container.getItems(new VFSSystemItemFilter())) {
				getFileInfosRecursively(item, fileInfos, newerThan, basePathlen);
			}
		}
	}
	
	/**
	 * Check if a file is offered as a download or as inline rendered. If
	 * security is enabled in the module, this will return true for all file
	 * types. If disabled it will depend on the mime type.
	 * 
	 * @param name the File name (including mime type extension, e.g. "index.html"
	 * @return true: force file download; false: open in new browser window
	 */
	public static boolean isDownloadForcedFileType(String name) {
		if (folderModule == null) {
			// Load only once and keep. Not best practice, in the long run the
			// folder manager needs a full spring bean refactoring, but for now
			// this is good enough. The not synchronized nature of the
			// assignment is not a problem here.
			folderModule = CoreSpringFactory.getImpl(FolderModule.class);
		}
		// If enabled in module, no further checks necessary. 
		boolean download = folderModule.isForceDownload();
		if (!download) {
			// Additional check if not an html or txt page. Only HTML pages are
			// displayed in browser, all other should be downloaded.
			// Excel, Word and PowerPoint not allowed to open inline, they will show
			// an unsupported WebDAV loginpromt!
			String mimeType = WebappHelper.getMimeType(name);
			if (mimeType != null && !"text/html".equals(mimeType) && !"application/xhtml+xml".equals(mimeType)) {
				download = true;
			}					
		}
		return download;
	}
	
}
