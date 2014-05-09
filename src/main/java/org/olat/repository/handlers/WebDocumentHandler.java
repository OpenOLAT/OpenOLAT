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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.AnimationFileResource;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.MovieFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class WebDocumentHandler extends FileHandler {
	
	private static final OLog log = Tracing.createLoggerFor(WebDocumentHandler.class);
	private static final List<String> supportedTypes;
	static { // initialize supported types
		supportedTypes = new ArrayList<String>(10);
		supportedTypes.add(FileResource.GENERIC_TYPE_NAME);
		supportedTypes.add(DocFileResource.TYPE_NAME);
		supportedTypes.add(XlsFileResource.TYPE_NAME);
		supportedTypes.add(PowerpointFileResource.TYPE_NAME);
		supportedTypes.add(PdfFileResource.TYPE_NAME);
		supportedTypes.add(SoundFileResource.TYPE_NAME);
		supportedTypes.add(MovieFileResource.TYPE_NAME);
		supportedTypes.add(AnimationFileResource.TYPE_NAME);
		supportedTypes.add(ImageFileResource.TYPE_NAME);
	}
	
	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return null;
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
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
			if (DocFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (XlsFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (PowerpointFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (PdfFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (ImageFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (MovieFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (SoundFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (AnimationFileResource.validate(filename)) {
				eval.setValid(true);
			}
		}
		return eval;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String displayname, String description, Locale locale,
			File file, String filename) {
		
		FileResource ores;
		if (DocFileResource.validate(filename)) {
			ores = new DocFileResource();
		} else if (XlsFileResource.validate(filename)) {
			ores = new XlsFileResource();
		} else if (PowerpointFileResource.validate(filename)) {
			ores = new PowerpointFileResource();
		} else if (PdfFileResource.validate(filename)) {
			ores = new PdfFileResource();
		} else if (ImageFileResource.validate(filename)) {
			ores = new ImageFileResource();
		} else if (MovieFileResource.validate(filename)) {
			ores = new MovieFileResource();
		} else if (SoundFileResource.validate(filename)) {
			ores = new SoundFileResource();
		} else if (AnimationFileResource.validate(filename)) {
			ores = new AnimationFileResource();
		} else {
			return null;
		}

		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File target = new File(fResourceFileroot, filename);
		
		try {
			Files.move(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("", e);
		}

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public void addExtendedEditionControllers(UserRequest ureq, WindowControl wControl,
			AuthoringEditEntrySettingsController pane, RepositoryEntry entry) {
		//
	}
	
	@Override
	public RepositoryEntry copy(RepositoryEntry source, RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copy");
		return target;
	}

	@Override
	public List<String> getSupportedTypes() {
		return supportedTypes;
	}

	@Override
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return false;
	}

	@Override
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return false;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re,  UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("a web document is not editable!!! res-id:"+re.getResourceableId());
	}

	protected String getDeletedFilePrefix() {
		return "del_webdoc_"; 
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
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("not implemented");
	}
}
