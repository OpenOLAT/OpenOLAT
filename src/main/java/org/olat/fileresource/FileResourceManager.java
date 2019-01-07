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

package org.olat.fileresource;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.fileresource.types.FileResource;

/**
 * Initial Date: Apr 8, 2004
 * 
 * @author Mike Stock
 */
public class FileResourceManager {

	public static final String ZIPDIR = "_unzipped_";
	private static FileResourceManager INSTANCE;

	/**
	 * spring
	 */
	private FileResourceManager() {
		INSTANCE = this;
	}

	/**
	 * @return Singleton.
	 */
	public static final FileResourceManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @param res
	 */
	public void deleteFileResource(OLATResourceable res) {
		// delete resources
		VFSContainer rootContainer = getFileResourceRootImpl(res);
		rootContainer.deleteSilently();// will delete versions and metadata
	}

	/**
	 * @param res
	 * @return Canonical root of file resource
	 */
	public File getFileResourceRoot(OLATResourceable res) {
		return getFileResourceRootImpl(res).getBasefile();
	}

	/**
	 * @param res
	 * @return olat root folder implementation of file resource
	 */
	public OlatRootFolderImpl getFileResourceRootImpl(OLATResourceable res) {
		return new OlatRootFolderImpl(FolderConfig.getRepositoryHome() + "/" + res.getResourceableId(), null);
	}
	
	public VFSContainer getFileResourceMedia(OLATResourceable res) {
		VFSContainer folder = getFileResourceRootImpl(res);
		VFSItem item = folder.resolve("media");
		VFSContainer mediaContainer;
		if(item == null) {
			mediaContainer = folder.createChildContainer("media");
		} else if(item instanceof VFSContainer) {
			mediaContainer = (VFSContainer)item;
		} else {
			mediaContainer = null;
		}
		return mediaContainer;
	}

	/**
	 * @param res
	 * @return Get resourceable as file.
	 */
	public File getFileResource(OLATResourceable res) {
		return getFileResource(res, null);
	}

	/**
	 * @param res
	 * @return Get resourceable as file.
	 */
	private File getFileResource(OLATResourceable res, String resourceFolderName) {
		FileResource fr = getAsGenericFileResource(res);
		File f = getFile(fr, resourceFolderName);
		if (f == null) {// folder not existing or no file in it
			throw new OLATRuntimeException(FileResourceManager.class, "could not getFileResource for OLATResourceable " + res.getResourceableId()
				+ ":" + res.getResourceableTypeName(), null);
		}
		return f;
	}

	/**
	 * Get the specified file or the first zip archive.
	 * 
	 * @param fr
	 * @return The specified file, the first zip archive or null
	 */
	private File getFile(FileResource fr) {
		return getFile(fr, null);
	}

	/**
	 * Get the specified file or the first zip archive.
	 * 
	 * @param fr
	 * @param resourceFolderName
	 * @return The specified file, the first zip archive or null
	 */
	private File getFile(FileResource fr, String resourceFolderName) {
		File fResourceFileroot = getFileResourceRoot(fr);
		if (!fResourceFileroot.exists()) return null;
		File[] contents = fResourceFileroot.listFiles();
		File firstFile = null;
		for (int i = 0; i < contents.length; i++) {
			File file = contents[i];
			if (file.getName().equals(ZIPDIR)) continue; // skip ZIPDIR

			if (resourceFolderName != null) {
				// search for specific file name
				if (file.getName().equals(resourceFolderName)) { return file; }
			} else if (file.getName().toLowerCase().endsWith(".zip")) {
				// we use first zip file we find
				return file;
			} else if (firstFile == null) {
				// store the first file to be able to return it later. this is needed
				// for wikis.
				firstFile = file;
			}

		}
		// Neither the specified resource nor any zip file could be found. Return
		// the first file that is not ZIPDIR or null.
		return firstFile;
	}

	/**
	 * @param res
	 * @return File resource as downloadeable media resource.
	 */
	public MediaResource getAsDownloadeableMediaResource(OLATResourceable res) {
		FileResource fr = getAsGenericFileResource(res);
		File f = getFile(fr);
		if (f == null) // folder not existing or no file in it
		throw new OLATRuntimeException(FileResourceManager.class, "could not get File for OLATResourceable " + res.getResourceableId() + ":"
				+ res.getResourceableTypeName(), null);
		return new DownloadeableMediaResource(f);
	}

	/**
	 * @param res
	 * @return Directory wherer unzipped files of file resourcea are located.
	 */
	public String getUnzippedDirRel(OLATResourceable res) {
		return res.getResourceableId() + "/" + ZIPDIR;
	}

	/**
	 * Unzips a resource and returns the unzipped folder's root.
	 * 
	 * @param res
	 * @return Unzip contents of ZIP file resource.
	 */
	public File unzipFileResource(final OLATResourceable res) {
		final File dir = getFileResourceRoot(res);
		return unzipFileResource(res, dir);
	}
	
	public OlatRootFolderImpl unzipContainerResource(final OLATResourceable res) {
		OlatRootFolderImpl container = getFileResourceRootImpl(res);
		File dir = container.getBasefile();
		File unzipDir = unzipFileResource(res, dir);
		if(unzipDir == null) {
			return null;
		}
		return (OlatRootFolderImpl)container.resolve(unzipDir.getName());
	}
	
	private final File unzipFileResource(final OLATResourceable res, final File dir) {
		if (!dir.exists()) {
			return null;
		}
		File zipTargetDir = new File(dir, ZIPDIR);
		if (!zipTargetDir.exists()) {
			// if not unzipped yet, synchronize all unzipping processes
			// o_clusterOK by:ld
			zipTargetDir = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(res, new SyncerCallback<File>() {
				public File execute() {
					File targetDir = null;
					// now we are the only one unzipping. We
					// only need to unzip when the previous
					// threads that aquired this lock have not unzipped "our" version's
					// resources yet
					targetDir = new File(dir, ZIPDIR);
					if (!targetDir.exists()) { // means I am the first to unzip this
						// version's resource
						targetDir.mkdir();
						File zipFile = getFileResource(res);
						if (!ZipUtil.unzip(zipFile, targetDir)) {
							return null;
						}
					}
					return targetDir;
				}
			});
		}
		return zipTargetDir;
	}

	/**
	 * Deletes the contents of the last unzip operation.
	 * 
	 * @param res
	 * @return True upon success.
	 */
	public boolean deleteUnzipContent(OLATResourceable res) {
		File dir = getFileResourceRoot(res);
		if (!dir.exists()) return false;
		File zipTargetDir = new File(dir, ZIPDIR);
		return FileUtils.deleteDirsAndFiles(zipTargetDir, true, true);
	}

	private FileResource getAsGenericFileResource(OLATResourceable res) {
		FileResource fr = new FileResource();
		fr.overrideResourceableId(res.getResourceableId());
		return fr;
	}
}