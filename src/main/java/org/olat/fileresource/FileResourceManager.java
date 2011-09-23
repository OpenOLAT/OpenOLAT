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
 * <p>
 */

package org.olat.fileresource;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.fileresource.types.AddingResourceException;
import org.olat.fileresource.types.AnimationFileResource;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.MovieFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.WikiResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.modules.cp.CPOfflineReadableManager;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Initial Date: Apr 8, 2004
 * 
 * @author Mike Stock
 */
public class FileResourceManager extends BasicManager {

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
	 * Add a file resource if the resource type is already known.
	 * 
	 * @param fResource
	 * @param newName
	 * @param knownResource maybe null, FileResource type will be calculated
	 * @return True upon success, false otherwise.
	 */
	public FileResource addFileResource(File fResource, String newName, FileResource knownResource) throws AddingResourceException {

		// ZIPDIR is a reserved name... check
		if (fResource.getName().equals(ZIPDIR)) throw new AssertException("Trying to add FileResource with reserved name '" + ZIPDIR + "'.");

		FileResource tempFr = new FileResource();
		if (knownResource != null) tempFr = knownResource;

		// move file to its new place
		File fResourceFileroot = getFileResourceRoot(tempFr);
		if (!FileUtils.copyFileToDir(fResource, fResourceFileroot, "add file resource")) return null;

		if (!fResource.getName().equals(newName)) { // need to rename file to new
			// name
			File fNewName = new File(fResourceFileroot, newName);
			if (!new File(fResourceFileroot, fResource.getName()).renameTo(fNewName)) {
				FileUtils.deleteDirsAndFiles(fResourceFileroot, true, true);
				return null;
			}
			fResource = fNewName;
		}

		if (knownResource == null) {
			// save resourceableID
			Long resourceableId = tempFr.getResourceableId();
			// extract type
			try {
				if (DocFileResource.validate(fResource)) tempFr = new DocFileResource();
				else if (XlsFileResource.validate(fResource)) tempFr = new XlsFileResource();
				else if (PowerpointFileResource.validate(fResource)) tempFr = new PowerpointFileResource();
				else if (PdfFileResource.validate(fResource)) tempFr = new PdfFileResource();
				else if (ImageFileResource.validate(fResource)) tempFr = new ImageFileResource();
				else if (MovieFileResource.validate(fResource)) tempFr = new MovieFileResource();
				else if (SoundFileResource.validate(fResource)) tempFr = new SoundFileResource();
				else if (AnimationFileResource.validate(fResource)) tempFr = new AnimationFileResource();
				else if (SharedFolderFileResource.validate(fResource)) tempFr = new SharedFolderFileResource();
				// add a wiki copy
				else if (WikiResource.validate(fResource)) tempFr = new WikiResource();
				// add a podcast copy
				else if (PodcastFileResource.validate(fResource)) tempFr = new PodcastFileResource(fResourceFileroot, fResource);
				// add a blog copy
				else if (BlogFileResource.validate(fResource)) tempFr = new BlogFileResource(fResourceFileroot, fResource);
				// add a glossary copy
				else if (GlossaryResource.validate(fResource)) tempFr = new GlossaryResource();
				//add portfolio copy
				else if (EPTemplateMapResource.validate(fResource)) tempFr = new EPTemplateMapResource();
				// the following types need unzippedDir
				else if (fResource.getName().toLowerCase().endsWith(".zip")) {
					File fUnzippedDir = unzipFileResource(tempFr);
					if (fUnzippedDir == null) {
						// in case of failure we forward the error message
						throw new AddingResourceException("resource.error.zip");
					}
					if (TestFileResource.validate(fUnzippedDir)) tempFr = new TestFileResource();
					else if (WikiResource.validate(fUnzippedDir)) tempFr = new WikiResource();
					else if (PodcastFileResource.validate(fUnzippedDir)) tempFr = new PodcastFileResource(fResourceFileroot, fUnzippedDir);
					else if (BlogFileResource.validate(fUnzippedDir)) tempFr = new BlogFileResource(fResourceFileroot, fUnzippedDir);
					else if (SurveyFileResource.validate(fUnzippedDir)) tempFr = new SurveyFileResource();
					// CP must be later entry... Test- and SurveyFileResource may contain
					// imsmanifest.xml as well
					else if (ImsCPFileResource.validate(fUnzippedDir)) tempFr = new ImsCPFileResource();
					// scorm and cp now can throw an exception which helps to show a
					// better error message in case
					// of a failure in adding a new resource
					else if (ScormCPFileResource.validate(fUnzippedDir)) tempFr = new ScormCPFileResource();
					// glossary resources are packaged within zip for import/export
					else if (GlossaryResource.validate(fUnzippedDir)) tempFr = new GlossaryResource();
					// portfolio template are packaged within zip for import/export
					else if (EPTemplateMapResource.validate(fUnzippedDir)) {
						tempFr = new EPTemplateMapResource();
					}
					else {
						// just a generic ZIP file... we can delete the temporary unziped
						// dir...
						throw new AddingResourceException("doesn't matter what error key is declared");
					}
				}
			} catch (AddingResourceException exception) {
				// in case of failure we delete the resource and forward the error
				// message
				deleteFileResource(tempFr);
				throw exception;
			}

			tempFr.overrideResourceableId(resourceableId);
		}

		// add olat resource
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.findOrPersistResourceable(tempFr);

		// make IMS-Content-Packaging offline readable adding a html-frameset
		if (tempFr instanceof ImsCPFileResource) {
			CPOfflineReadableManager cporm = CPOfflineReadableManager.getInstance();
			cporm.makeCPOfflineReadable(ores, newName);
		}
		return tempFr;
	}

	/**
	 * @param fResource
	 * @param newName
	 * @return Newly created file resource
	 */
	public FileResource addFileResource(File fResource, String newName) throws AddingResourceException {
		return addFileResource(fResource, newName, null);
	}

	/**
	 * @param res
	 */
	public void deleteFileResource(OLATResourceable res) {
		// delete resources
		File fResourceFileroot = getFileResourceRoot(res);
		FileUtils.deleteDirsAndFiles(fResourceFileroot, true, true);
		// delete resourceable
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.findResourceable(res);
		if (ores != null) rm.deleteOLATResource(ores);
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
		if (f == null) // folder not existing or no file in it
		throw new OLATRuntimeException(FileResourceManager.class, "could not getFileResource for OLATResourceable " + res.getResourceableId()
				+ ":" + res.getResourceableTypeName(), null);
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
		if (!dir.exists()) return null;
		File zipTargetDir = new File(dir, ZIPDIR);
		if (!zipTargetDir.exists()) {
			// if not unzipped yet, synchronize all unzipping processes
			// o_clusterOK by:ld
			zipTargetDir = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(res, new SyncerCallback<File>() {
				public File execute() {
					File zipTargetDir = null;
					// now we are the only one unzipping. We
					// only need to unzip when the previous
					// threads that aquired this lock have not unzipped "our" version's
					// resources yet
					zipTargetDir = new File(dir, ZIPDIR);
					if (!zipTargetDir.exists()) { // means I am the first to unzip this
						// version's resource
						zipTargetDir.mkdir();
						File zipFile = getFileResource(res);
						if (!ZipUtil.unzip(zipFile, zipTargetDir)) return null;
					}
					return zipTargetDir;
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

	/**
	 * @param res
	 * @return FormBasicController
	 */
	public Controller getDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return new FileDetailsForm(ureq, wControl, res);
	}

	private FileResource getAsGenericFileResource(OLATResourceable res) {
		FileResource fr = new FileResource();
		fr.overrideResourceableId(res.getResourceableId());
		return fr;
	}

	/**
	 * Creates a copy of the given resourceable.
	 * 
	 * @param res
	 * @return Copy of the given resource.
	 */
	public OLATResourceable createCopy(OLATResourceable res) {
		return createCopy(res, null);
	}

	/**
	 * Creates a copy of the given resourceable.
	 * 
	 * @param res
	 * @return Copy of the given resource.
	 */
	public OLATResourceable createCopy(OLATResourceable res, String resourceFolderName) {
		File fResource = getFileResource(res, resourceFolderName);
		if (fResource == null) return null;
		try {
			return addFileResource(fResource, fResource.getName());
		} catch (AddingResourceException e) {
			throw new OLATRuntimeException(FileResourceManager.class, "Error while trying to copy resource with name: " + fResource.getName(), e);
		}
	}

}