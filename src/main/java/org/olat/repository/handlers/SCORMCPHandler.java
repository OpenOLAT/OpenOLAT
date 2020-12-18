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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.scorm.ScormConstants;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.modules.scorm.ScormPackageConfig;
import org.olat.modules.scorm.ScormRuntimeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;


/**
 * @author Guido Schnider
 * 
 * Comment:  
 * 
 */
public class SCORMCPHandler extends FileHandler {
	
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
		return ScormCPFileResource.evaluate(file, filename);
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
		
		ScormCPFileResource scormResource = new ScormCPFileResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(scormResource);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class) .create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipRoot = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipRoot);
	
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
		final ScormMainManager scormManager = CoreSpringFactory.getImpl(ScormMainManager.class);
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File zipRoot = new File(sourceFileroot, FileResourceManager.ZIPDIR);
		
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyFileToDir(zipRoot, targetFileroot, "add file resource");
		
		//copy packaging info
		ScormPackageConfig scormConfig = scormManager.getScormPackageConfig(sourceResource);
		if(scormConfig != null) {
			scormManager.setScormPackageConfig(targetResource, scormConfig);
		}
		return target;
	}
	
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		File unzippedDir = FileResourceManager.getInstance().unzipFileResource(res);
		String displayName = CoreSpringFactory.getImpl(RepositoryManager.class)
				.lookupDisplayNameByOLATResourceableId(res.getResourceableId());
		return new ZippedDirectoryMediaResource(displayName, unzippedDir);
	}

	@Override
	public String getSupportedType() {
		return ScormCPFileResource.TYPE_NAME;
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
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		if (re != null) {
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
		}
		
		return new ScormRuntimeController(ureq, wControl, re, reSecurity,
			(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) ->  {
					OLATResource res = entry.getOlatResource();
					CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
						.updateUserCourseInformations(res, uureq.getIdentity());
					File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
					return CoreSpringFactory.getImpl(ScormMainManager.class).createScormAPIandDisplayController(uureq, wwControl, true, null, cpRoot,
							res.getResourceableId(), null, ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT,
							false, null, false, false, false, reSecurity.isEntryAdmin(), null);
			});
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		throw new AssertException("Trying to get editor for an SCORM CP type where no editor is provided for this type.");
	}

	@Override
	protected String getDeletedFilePrefix() {
		return "del_scorm_"; 
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
    //nothing to do
		return null;
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		//nothing to do since nothing locked
	}
}