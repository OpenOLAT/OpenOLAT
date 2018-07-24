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

package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentMode;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;


/** 
 * Handler to import MP4-files as videoresource
 * Initial Date:  Mar 27, 2015
 *
 * @author Dirk Furrer
 *
 *
 */
public class VideoHandler extends FileHandler {
	
	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return null;
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		return null;
	}

	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}

		ResourceEvaluation eval = new ResourceEvaluation(false);
		String extension = FileUtils.getFileSuffix(filename);
		if(StringHelper.containsNonWhitespace(extension)) {
			VideoFileResource.validate(file, filename, eval);
		}
		return eval;
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String fileName) {

		// 1) Create resource and repository entry
		FileResource ores = new VideoFileResource();
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		RepositoryEntry repoEntry = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "",
				displayname, description, resource, RepositoryEntryStatusEnum.preparation, organisation);
		
		if(fileName == null) {
			fileName = file.getName();
		}
		fileName = fileName.toLowerCase();
		VFSLeaf importFile = new LocalFileImpl(file);	
		long filesize = importFile.getSize();
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);

		if (fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".m4v")) {
			// 2a) import video from raw mp4 master video file
			videoManager.importFromMasterFile(repoEntry, importFile);
			
		} else if (fileName.endsWith(".zip")) {
			// 2b) import video from archive from another OpenOLAT instance
			DBFactory.getInstance().commit();
			videoManager.importFromExportArchive(repoEntry, importFile);			
		}	
		// 3) Persist Meta data
		videoManager.createVideoMetadata(repoEntry, filesize, fileName);
		DBFactory.getInstance().commit();	
		// 4) start transcoding process if enabled
		videoManager.startTranscodingProcessIfEnabled(resource);
		
		return repoEntry;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		RepositoryManager repoManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		RepositoryEntry repoEntry = repoManager.lookupRepositoryEntry(res, false);
		if (repoEntry == null) {
			return new NotFoundMediaResource();
		}
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);		
		return videoManager.getVideoExportMediaResource(repoEntry);
	}

	@Override
	public String getSupportedType() {
		return VideoFileResource.TYPE_NAME;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource) {
		return EditionSupport.no;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re,  RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new VideoRuntimeController(ureq, wControl, re, reSecurity, new RuntimeControllerCreator() {
			@Override
			public Controller create(UserRequest uureq, WindowControl wwControl, TooledStackedPanel toolbarPanel,
					RepositoryEntry entry, RepositoryEntrySecurity rereSecurity, AssessmentMode assessmentMode) {
				return new VideoDisplayController(uureq, wwControl, entry);
			}
		});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		throw new AssertException("a web document is not editable!!! res-id:"+re.getResourceableId());
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	protected String getDeletedFilePrefix() {
		return "del_video_";
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		//nothing to do
		return null;
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		//nothing to do since nothing locked
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}

	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source,
			RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		videoManager.copyVideo(sourceResource, targetResource);
		return target;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		boolean success = super.cleanupOnDelete(entry, res);
		if (success) {
			// remove transcodings
			VideoManager videoManager = CoreSpringFactory.getImpl(VideoManager.class);
			success = videoManager.deleteVideoTranscodings(entry.getOlatResource());
			//remove metadata
			success &= videoManager.deleteVideoMetadata(entry.getOlatResource());
		}
		return success;
	}
	
	

}
