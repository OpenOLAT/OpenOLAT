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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.spi.youtube.YoutubeProvider;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/** 
 * Handler to import MP4-files as videoresource
 * Initial Date:  Mar 27, 2015
 *
 * @author Dirk Furrer
 *
 *
 */
@Service("videoRepositoryHandler")
public class VideoHandler extends FileHandler {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private YoutubeProvider youtubeProvider;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
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
	public boolean supportImport() {
		return true;
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
	public boolean supportImportUrl() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(String url) {
		ResourceEvaluation eval = null;
		VideoFormat format = VideoFormat.valueOfUrl(url);
		if(format == VideoFormat.youtube && youtubeProvider.isEnabled()) {
			eval = youtubeProvider.evaluate(url);
		}
		
		if(eval == null) {
			eval = new ResourceEvaluation();
		}
		if(format != null) {
			eval.setValid(true);
		}
		return eval;
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String fileName) {

		// 1) Create resource and repository entry
		FileResource ores = new VideoFileResource();
		OLATResource resource = resourceManager.createAndPersistOLATResourceInstance(ores);
		RepositoryEntry repoEntry = repositoryService.create(initialAuthor, null, "",
				displayname, description, resource, RepositoryEntryStatusEnum.preparation, organisation);
		
		if(fileName == null) {
			fileName = file.getName();
		}
		fileName = fileName.toLowerCase();
		VFSLeaf importFile = new LocalFileImpl(file);	

		VideoMeta videoMeta = null;
		if (fileName.endsWith(".mp4") || fileName.endsWith(".mov") || fileName.endsWith(".m4v")) {
			// 2a) import video from raw mp4 master video file
			videoMeta = videoManager.importFromMasterFile(repoEntry, importFile, initialAuthor);
		} else if (fileName.endsWith(".zip")) {
			// 2b) import video from archive from another OpenOLAT instance
			videoMeta = videoManager.importFromExportArchive(repoEntry, importFile, initialAuthor);
		}
		dbInstance.commit();
		
		// 4) start transcoding process if enabled
		if(videoMeta != null && !StringHelper.containsNonWhitespace(videoMeta.getUrl())) {
			videoManager.startTranscodingProcessIfEnabled(resource);
		}
		return repoEntry;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		VideoFormat format = VideoFormat.valueOfUrl(url);
		if(format == null) {
			return null;// cannot understand the URL
		}
		
		// 1) Create resource and repository entry
		FileResource ores = new VideoFileResource();
		OLATResource resource = resourceManager.createAndPersistOLATResourceInstance(ores);
		RepositoryEntry repoEntry = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		
		if(format == VideoFormat.panopto) {
			url = videoManager.toPodcastVideoUrl(url);
		} 

		// 3) Persist Meta data
		videoManager.createVideoMetadata(repoEntry, url, format);
		dbInstance.commit();
		
		repoEntry = videoManager.updateVideoMetadata(repoEntry, url, format);
		return repoEntry;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(res, false);
		if (repoEntry == null) {
			return new NotFoundMediaResource();
		}	
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
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.no;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re,  RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new VideoRuntimeController(ureq, wControl, re, reSecurity, (uureq, wwControl, toolbarPanel, entry, rereSecurity, assessmentMode) -> 
			new VideoDisplayController(uureq, wwControl, entry)
		);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		throw new AssertException("a web document is not editable!!! res-id:"+re.getResourceableId());
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
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
		videoManager.copyVideo(source, target);
		return target;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		boolean success = super.cleanupOnDelete(entry, res);
		if (success) {
			// remove transcodings
			success = videoManager.deleteVideoTranscodings(entry.getOlatResource());
			//remove metadata
			success &= videoManager.deleteVideoMetadata(entry.getOlatResource());
		}
		return success;
	}
}
