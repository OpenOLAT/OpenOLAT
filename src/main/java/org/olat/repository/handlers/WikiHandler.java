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
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
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
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.controller.OLATResourceableListeningWrapperController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.wiki.PersistingAssessmentProvider;
import org.olat.modules.wiki.WikiAssessmentProvider;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiModule;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.modules.wiki.WikiToZipUtils;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;


/**
 * Description:<br>
 * Handles the type wiki in the repository
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 */
public class WikiHandler implements RepositoryHandler {

	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return CoreSpringFactory.getImpl(WikiModule.class).isWikiEnabled();
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.wiki";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		WikiResource wikiResource = WikiManager.getInstance().createWiki(initialAuthor);
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(wikiResource);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, WikiManager.WIKI_RESOURCE_FOLDER_NAME, displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return WikiResource.validate(file, filename);
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
		WikiResource wikiResource = new WikiResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(wikiResource);
		File rootDirectory = WikiManager.getInstance().getWikiRootContainer(resource).getBasefile();
		WikiManager.getInstance().importWiki(file, filename, rootDirectory);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, WikiManager.WIKI_RESOURCE_FOLDER_NAME, displayname,
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
		final OLATResource sourceResource = source.getOlatResource();
		final OLATResource targetResource = target.getOlatResource();
		final FileResourceManager frm = FileResourceManager.getInstance();
		File sourceDir = frm.getFileResourceRoot(sourceResource);
		File targetDir = frm.getFileResourceRoot(targetResource);
		WikiManager.getInstance().copyWiki(sourceDir, targetDir);
		return target;
	}

	@Override
	public String getSupportedType() {
		return WikiResource.TYPE_NAME;
	}

	@Override
	public boolean supportsDownload() {
		return true;
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

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		// first handle special case: disabled wiki for security (XSS Attacks) reasons
		WikiModule wikiModule = CoreSpringFactory.getImpl(WikiModule.class); 
		if (!wikiModule.isWikiEnabled()) {
			return RepositoyUIFactory.createRepoEntryDisabledDueToSecurityMessageController(ureq, wControl);
		}

		//check role
		boolean isOLatAdmin = reSecurity.isEntryAdmin();
		boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		boolean isResourceOwner = false;
		if (isOLatAdmin) {
			isResourceOwner = true;
		} else {
			isResourceOwner = reSecurity.isOwner();
		}
		
		OLATResource res = re.getOlatResource();
		BusinessControl bc = wControl.getBusinessControl();
		final ContextEntry ce = bc.popLauncherContextEntry();
		SubscriptionContext subsContext = new SubscriptionContext(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		final WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isOLatAdmin, isGuestOnly, false, isResourceOwner, subsContext);
		WikiAssessmentProvider assessmentProvider = PersistingAssessmentProvider.create(re, ureq.getIdentity());

		return new RepositoryEntryRuntimeController(ureq, wControl, re, reSecurity,
			new RuntimeControllerCreator() {
				@Override
				public Controller create(UserRequest uureq, WindowControl wwControl, TooledStackedPanel toolbarPanel,
						RepositoryEntry entry, RepositoryEntrySecurity security, AssessmentMode assessmentMode) {
					CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
						.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
					Controller controller;
					if (ce != null ) { //jump to a certain context
						OLATResourceable ores = ce.getOLATResourceable();
						String typeName = ores.getResourceableTypeName();
						String page = typeName.substring("page=".length());
						controller = new WikiMainController(uureq, wwControl, entry.getOlatResource(), callback, assessmentProvider, page); 
					} else {
						controller = new WikiMainController(uureq, wwControl, entry.getOlatResource(), callback, assessmentProvider, null);
					}
					return new OLATResourceableListeningWrapperController(uureq, wwControl, entry.getOlatResource(), controller, null, uureq.getIdentity());
				}
			});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		return null;
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(res);
		VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
		return new VFSMediaResource(wikiZip);
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		//delete also notifications
		CoreSpringFactory.getImpl(NotificationsManager.class).deletePublishersOf(res);
		FileResourceManager.getInstance().deleteFileResource(res);
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