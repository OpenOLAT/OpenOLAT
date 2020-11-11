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
*/

package org.olat.modules.sharedfolder;

import java.io.File;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedContainerMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SharedFolderSecurityCallback;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Initial Date:  Aug 29, 2005 <br>
 * @author Alexander Schneider
 */
public class SharedFolderManager {
	
	public static final String SHAREDFOLDERREF = "sharedfolderref";

	private static final SharedFolderManager INSTANCE = new SharedFolderManager();
	/**
	 * name of the folder on the filesystem, not visible for the user
	 */
	private static final String FOLDER_NAME = "_sharedfolder_";
	
	private SharedFolderManager() {
		// singleton
	}
	
	public static SharedFolderManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @param re The repository entry of the shared folder
	 * @param urlCompliant Encode the name to be file save
	 * @return The container
	 */
	public VFSContainer getNamedSharedFolder(RepositoryEntry re, boolean urlCompliant) {
		String name = re.getDisplayname();
		if(urlCompliant) {
			name = RequestUtil.normalizeFilename(name);
		}
		VFSContainer folder = getSharedFolder(re.getOlatResource());
		return folder == null ? null : new NamedContainerImpl(name, folder);
	}

	public LocalFolderImpl getSharedFolder(OLATResourceable res) {
		LocalFolderImpl rootFolderImpl = (LocalFolderImpl)FileResourceManager.getInstance().getFileResourceRootImpl(res).resolve(SharedFolderManager.FOLDER_NAME);
		if (rootFolderImpl != null) {
			rootFolderImpl.setLocalSecurityCallback(new SharedFolderSecurityCallback(rootFolderImpl.getRelPath()));
		}
		return rootFolderImpl;
	}

	public MediaResource getAsMediaResource(OLATResourceable res) {
		String exportFileName = res.getResourceableId() + ".zip";
		VFSContainer sharedFolder = getSharedFolder(res);
		// do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();
		return new ZippedContainerMediaResource(exportFileName, sharedFolder, true);
	}

	public boolean exportSharedFolder(String sharedFolderSoftkey, String path, ZipOutputStream zout) {
		RepositoryEntry re = RepositoryManager.getInstance()
				.lookupRepositoryEntryBySoftkey(sharedFolderSoftkey, false);
		if (re == null) return false;

		// do intermediate commit to avoid transaction timeout
		DBFactory.getInstance().intermediateCommit();

		// export properties
		RepositoryEntryImportExport reImportExport = new RepositoryEntryImportExport(re, null);
		return reImportExport.exportDoExport(path, zout);
	}
	
	public RepositoryEntryImportExport getRepositoryImportExport(File importDataDir) {
		File fImportBaseDirectory = new File(importDataDir, "sharedfolder");
		return new RepositoryEntryImportExport(fImportBaseDirectory);
	}

	public void deleteSharedFolder(OLATResourceable res) {
		FileResourceManager.getInstance().deleteFileResource(res);
	}
	
	public SharedFolderFileResource createSharedFolder() {
		SharedFolderFileResource resource = new SharedFolderFileResource();
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
		if (rootContainer.createChildContainer(FOLDER_NAME) == null) return null;
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.createOLATResourceInstance(resource);
		rm.saveOLATResource(ores);
		return resource;
	}

	public boolean validate(File f) {
		String name = f.getName();
		return name.equals(FOLDER_NAME) || name.equals(FOLDER_NAME + ".zip");
	}
}