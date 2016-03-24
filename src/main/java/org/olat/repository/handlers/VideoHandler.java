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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieServiceImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.managers.VideoManager;
import org.olat.modules.video.models.VideoMetadata;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial Date:  Mar 27, 2015
 *
 * @author Dirk Furrer
 *
 *
 */
public class VideoHandler extends FileHandler {

	private static final OLog log = Tracing.createLoggerFor(VideoHandler.class);
	@Autowired
	private RepositoryManager repositoryManager;

	private VideoManager videoManager;
	private VideoModule videomodule ;

	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return null;
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Object createObject, Locale locale) {
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
			if (VideoFileResource.validate(filename)) {
				eval.setValid(true);
			}
		}
		return eval;
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Locale locale, File file, String filename) {

		FileResource ores;
		if (VideoFileResource.validate(filename)) {
			ores = new VideoFileResource();
		} else {
			return null;
		}

		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		File videoResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File mediaFolder = new File(videoResourceFileroot, "media");
		if(!mediaFolder.exists()) mediaFolder.mkdir();

		File target = new File(mediaFolder, "video.mp4");


		try {
			Files.move(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("", e);
		}

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();

		// generate Metadata
		videoManager = CoreSpringFactory.getImpl(VideoManager.class);
		videomodule = CoreSpringFactory.getImpl(VideoModule.class);
		File metaDataFile = new File(videoResourceFileroot, "video_metadata.xml");
		try{
		XStreamHelper.writeObject(XStreamHelper.createXStreamInstance(), metaDataFile, new VideoMetadata(resource));
		videoManager.setTitle(resource, displayname);
		Size videoSize = CoreSpringFactory.getImpl(MovieServiceImpl.class).getSize(new LocalFileImpl(target), "mp4");
		videoManager.setVideoSize(resource, videoSize);
		VFSLeaf posterResource = VFSManager.resolveOrCreateLeafFromPath(FileResourceManager.getInstance().getFileResourceMedia(resource), "/poster.jpg");

		videoManager.getFrame(resource, 20, posterResource);

		videoManager.setPosterframe(resource, posterResource);
		}catch(IOException e){
			log.warn("wasnt able to create poster for video"+filename);
		}

		if(videomodule.isTranscodingEnabled()){
			videoManager.optimizeVideoRessource(resource);
		}

		videoManager.setRatingEnabled(resource, false);
		videoManager.setCommentsEnabled(resource, false);
		return re;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		return FileResourceManager.getInstance().getAsDownloadeableMediaResource(res);
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
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copy");
		return target;
	}

}
