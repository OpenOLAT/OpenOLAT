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
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
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
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.WebDocumentRunController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 */
public class WebDocumentHandler extends FileHandler {

	private static final Logger log = Tracing.createLoggerFor(WebDocumentHandler.class);
	private final String supportedType;
	private final WebDocumentCreateDelegate createDelegate;
	private final WebDocumentEditDelegate editDelegate;
	
	public WebDocumentHandler(String type) {
		this(type, new NullCreateDelegate(), new NullEditDelegate());
	}
	
	public WebDocumentHandler(String type, WebDocumentCreateDelegate createDelegate, WebDocumentEditDelegate editDelegate) {
		this.supportedType = type;
		this.createDelegate = createDelegate;
		this.editDelegate = editDelegate;
	}
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return createDelegate.supportCreate(identity, roles);
	}

	@Override
	public String getCreateLabelI18nKey() {
		return createDelegate.getCreateLabelI18nKey();
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		return createDelegate.createResource(initialAuthor, displayname, description, createObject, organisation, locale);
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
			if (DocFileResource.TYPE_NAME.equals(supportedType) && DocFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (XlsFileResource.TYPE_NAME.equals(supportedType) && XlsFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (PowerpointFileResource.TYPE_NAME.equals(supportedType) && PowerpointFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (PdfFileResource.TYPE_NAME.equals(supportedType) && PdfFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (ImageFileResource.TYPE_NAME.equals(supportedType) && ImageFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (MovieFileResource.TYPE_NAME.equals(supportedType) && MovieFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (SoundFileResource.TYPE_NAME.equals(supportedType) && SoundFileResource.validate(filename)) {
				eval.setValid(true);
			} else if (AnimationFileResource.TYPE_NAME.equals(supportedType) && AnimationFileResource.validate(filename)) {
				eval.setValid(true);
			}
		}
		if (eval.getDisplayname() == null) {
			// use filename, better than nothing
			eval.setDisplayname(filename + " - " + Formatter.formatShortDateFilesystem(new Date()));
		}
		return eval;
	}
	
	@Override
	public boolean supportImportUrl() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(String url) {
		return ResourceEvaluation.notValid();
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String filename) {
		
		FileResource ores;
		if (DocFileResource.TYPE_NAME.equals(supportedType) && DocFileResource.validate(filename)) {
			ores = new DocFileResource();
		} else if (XlsFileResource.TYPE_NAME.equals(supportedType) && XlsFileResource.validate(filename)) {
			ores = new XlsFileResource();
		} else if (PowerpointFileResource.TYPE_NAME.equals(supportedType) && PowerpointFileResource.validate(filename)) {
			ores = new PowerpointFileResource();
		} else if (PdfFileResource.TYPE_NAME.equals(supportedType) && PdfFileResource.validate(filename)) {
			ores = new PdfFileResource();
		} else if (ImageFileResource.TYPE_NAME.equals(supportedType) && ImageFileResource.validate(filename)) {
			ores = new ImageFileResource();
		} else if (MovieFileResource.TYPE_NAME.equals(supportedType) && MovieFileResource.validate(filename)) {
			ores = new MovieFileResource();
		} else if (SoundFileResource.TYPE_NAME.equals(supportedType) && SoundFileResource.validate(filename)) {
			ores = new SoundFileResource();
		} else if (AnimationFileResource.TYPE_NAME.equals(supportedType) && AnimationFileResource.validate(filename)) {
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

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname,
				description, resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copy");
		return target;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		return FileResourceManager.getInstance().getAsDownloadeableMediaResource(res);
	}

	@Override
	public String getSupportedType() {
		return supportedType;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return editDelegate.supportsEdit(identity, roles);
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re,  RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new RepositoryEntryRuntimeController(ureq, wControl, re, reSecurity, (uureq, wwControl, toolbarPanel, entry, rereSecurity, assessmentMode) -> {
			CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
				.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
			return new WebDocumentRunController(uureq, wwControl, entry);
		});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		return editDelegate.createEditorController(re, ureq, wControl, toolbar);
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}
	
	@Override
	public FormBasicController createAuthorSmallDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, Form mainForm) {
		return null;
	}

	@Override
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
	

	
	/**
	 * 
	 * Initial date: 25 Mar 2019<br>
	 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
	 *
	 */
	private static class NullCreateDelegate implements WebDocumentCreateDelegate {

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

	}
	
	private static class NullEditDelegate implements WebDocumentEditDelegate {

		@Override
		public EditionSupport supportsEdit(Identity identity, Roles roles) {
			return EditionSupport.no;
		}

		@Override
		public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
				TooledStackedPanel toolbar) {
			throw new AssertException("This web document is not editable!!! res-id: " + re.getResourceableId());
		}
		
	}
}
