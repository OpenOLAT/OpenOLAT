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
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.sharedfolder.SharedFolderDisplayController;
import org.olat.modules.sharedfolder.SharedFolderEditorController;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.CorruptedCourseController;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;


/**
 * <P>
 * Initial Date: Apr 6 <br>
 * @author gnaegi
 */
public class SharedFolderHandler implements RepositoryHandler {
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return true;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.sharedfolder";
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		SharedFolderFileResource folderResource = SharedFolderManager.getInstance().createSharedFolder();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(folderResource);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, SharedFolderFileResource.RESOURCE_NAME, displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean supportImport() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ResourceEvaluation.notValid();
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
		return null;
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
		VFSContainer sourceContainer = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource);
		VFSContainer targetContainer = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource);
		targetContainer.copyContentOf(sourceContainer);
		return target;
	}

	@Override
	public String getSupportedType() {
		return SharedFolderFileResource.TYPE_NAME;
	}

	@Override
	public boolean supportsDownload() {
		return false;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.embedded;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	/**
	 * @param ureq
	 * @param wControl
	 * @param res
	 * @param initialViewIdentifier
	 * @return Controller
	 */
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new RepositoryEntryRuntimeController(ureq, wControl, re, reSecurity,
				(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
			OLATResource res = entry.getOlatResource();
			VFSContainer sfContainer = SharedFolderManager.getInstance().getSharedFolder(res);
			CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
				.updateUserCourseInformations(res, uureq.getIdentity());
			
			Controller sfdCtr;
			if(sfContainer == null || !sfContainer.exists()) {
				sfdCtr = new CorruptedCourseController(uureq, wwControl);
			} else {
				boolean canEdit = security.isEntryAdmin() || security.isCourseCoach();
				if(canEdit) {
					sfdCtr = new SharedFolderEditorController(entry, uureq, wwControl);
				} else {
					sfdCtr = new SharedFolderDisplayController(uureq, wwControl, sfContainer, res);
				}
			}
			return sfdCtr;
		});
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		return SharedFolderManager.getInstance().getAsMediaResource(res);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		Controller sharedFolderCtr = new SharedFolderEditorController(re, ureq, wControl);
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, sharedFolderCtr);
		layoutCtr.addDisposableChildController(sharedFolderCtr); // dispose content on layout dispose
		return layoutCtr;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		// do not need to notify all current users of this resource, since the only
		// way to access this resource
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		DBFactory.getInstance().commitAndCloseSession();
		SharedFolderManager.getInstance().deleteSharedFolder(res);
		return true;
	}

	@Override
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors) {
		ReferenceManager refM = CoreSpringFactory.getImpl(ReferenceManager.class);
		String referencesSummary = refM.getReferencesToSummary(entry.getOlatResource(), locale);
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			errors.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary, entry.getDisplayname() }));
			return false;
		}
		return true;
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
}
