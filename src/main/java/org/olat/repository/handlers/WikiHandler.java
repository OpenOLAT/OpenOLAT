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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiContainer;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.modules.wiki.WikiToZipUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
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
	
	private static final OLog log = Tracing.createLoggerFor(WikiHandler.class);
	private static final List<String> supportedTypes = Collections.singletonList(WikiResource.TYPE_NAME);

	/**
	 * Comment for <code>PROCESS_CREATENEW</code>
	 */
	public static final String PROCESS_CREATENEW = "cn";
	public static final String PROCESS_UPLOAD = "pu";
	
	@Override
	public boolean isCreate() {
		return true;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.wiki";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		WikiResource wikiResource = WikiManager.getInstance().createWiki();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(wikiResource);
		RepositoryEntry re = repositoryService.create(initialAuthor, WikiManager.WIKI_RESOURCE_FOLDER_NAME,
				displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return WikiResource.validate(file, filename);
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String displayname, String description, boolean withReferences,
			Locale locale, File file, String filename) {
		WikiResource wikiResource = new WikiResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(wikiResource);
		File rootDirectory = WikiManager.getInstance().getWikiRootContainer(resource).getBasefile();
		WikiManager.getInstance().importWiki(file, filename, rootDirectory);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
			.create(initialAuthor, WikiManager.WIKI_RESOURCE_FOLDER_NAME, displayname, description, resource, RepositoryEntry.ACC_OWNERS);
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
		final OLATResource sourceResource = source.getOlatResource();
		final OLATResource targetResource = target.getOlatResource();
		final FileResourceManager frm = FileResourceManager.getInstance();
		
		VFSContainer sourceWikiContainer = WikiManager.getInstance().getWikiContainer(sourceResource, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		if(sourceWikiContainer == null) {
			//if the wiki container is null, let the WikiManager to create one
			WikiManager.getInstance().getOrLoadWiki(sourceResource);
			sourceWikiContainer = WikiManager.getInstance().getWikiContainer(sourceResource, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		}
		
		VFSContainer targetRootContainer = frm.getFileResourceRootImpl(targetResource);
		VFSContainer targetWikiContainer = VFSManager.getOrCreateContainer(targetRootContainer, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		VFSManager.copyContent(sourceWikiContainer, targetWikiContainer);
		
		VFSContainer sourceRootContainer = sourceWikiContainer.getParentContainer();
		
		//create versions folder
		targetRootContainer.createChildContainer(WikiManager.VERSION_FOLDER_NAME);
		
		//create media folders and copy it
		VFSContainer targetMediaContainer = VFSManager.getOrCreateContainer(targetRootContainer, WikiContainer.MEDIA_FOLDER_NAME); 
		VFSItem sourceMediaItem = sourceRootContainer.resolve(WikiContainer.MEDIA_FOLDER_NAME);
		if(sourceMediaItem instanceof VFSContainer) {
			VFSContainer sourceMediaContainer = (VFSContainer)sourceMediaItem;
			VFSManager.copyContent(sourceMediaContainer, targetMediaContainer);
		}

		//reset properties files to default values
		String[] filteredSuffix = new String[]{ WikiManager.WIKI_PROPERTIES_SUFFIX };
		List<VFSItem> items = targetWikiContainer.getItems(new VFSItemSuffixFilter(filteredSuffix));
		for (VFSItem item: items) {
			if(item instanceof VFSLeaf) {
				VFSLeaf leaf = (VFSLeaf)item;
				WikiPage page = Wiki.assignPropertiesToPage(leaf);
				//reset the copied pages to a the default values
				page.resetCopiedPage();
				WikiManager.getInstance().updateWikiPageProperties(targetResource, page);
			}
		}
		
		return target;
	}

	@Override
	public List<String> getSupportedTypes() {
		return supportedTypes;
	}

	@Override
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return true;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		// first handle special case: disabled wiki for security (XSS Attacks) reasons
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class); 
		if (!securityModule.isWikiEnabled()) {
			return RepositoyUIFactory.createRepoEntryDisabledDueToSecurityMessageController(ureq, wControl);
		}
		// proceed with standard case
		Controller controller = null;
		
		//check role
		boolean isOLatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		boolean isResourceOwner = false;
		if (isOLatAdmin) {
			isResourceOwner = true;
		} else {
			RepositoryManager repoMgr = RepositoryManager.getInstance();
			isResourceOwner = repoMgr.isOwnerOfRepositoryEntry(ureq.getIdentity(), re);
		}
		
		OLATResource res = re.getOlatResource();
		BusinessControl bc = wControl.getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		SubscriptionContext subsContext = new SubscriptionContext(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		WikiSecurityCallback callback = new WikiSecurityCallbackImpl(null, isOLatAdmin, isGuestOnly, false, isResourceOwner, subsContext);
		
		if ( ce != null ) { //jump to a certain context
			OLATResourceable ores = ce.getOLATResourceable();
			String typeName = ores.getResourceableTypeName();
			String page = typeName.substring("page=".length());
			controller = WikiManager.getInstance().createWikiMainControllerDisposeOnOres(ureq, wControl, res, callback, page);
		} else {
			controller = WikiManager.getInstance().createWikiMainControllerDisposeOnOres(ureq, wControl, res, callback, null);
		}
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, controller);
		layoutCtr.addDisposableChildController(controller); // dispose content on layout dispose
		if(controller instanceof Activateable2) {
			layoutCtr.addActivateableDelegate((Activateable2)controller);
		}
		
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, layoutCtr);	
		return wrapper;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		return createLaunchController(re, ureq, wControl);
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(res);
		VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
		return new VFSMediaResource(wikiZip);
	}

	@Override
	public boolean cleanupOnDelete(OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		//delete also notifications
		NotificationsManager.getInstance().deletePublishersOf(res);
		FileResourceManager.getInstance().deleteFileResource(res);
		return true;
	}

	@Override
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary }));
			return false;
		}
		return true;
	}

	@Override
	public Controller createDetailsForm( UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
	}

	@Override
	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(repoEntry.getOlatResource());
		VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
		String exportFileName = "del_wiki_" + repoEntry.getOlatResource().getResourceableId() + ".zip";
		String fullFilePath = archivFilePath + File.separator + exportFileName;
		
		File fExportZIP = new File(fullFilePath);
		InputStream fis = wikiZip.getInputStream();
		
		try {
			FileUtils.bcopy(wikiZip.getInputStream(), fExportZIP, "archive wiki");
		} catch (FileNotFoundException e) {
			log.warn("Can not archive wiki repoEntry=" + repoEntry);
		} catch (IOException ioe) {
			log.warn("Can not archive wiki repoEntry=" + repoEntry);
		} finally {
			FileUtils.closeSafely(fis);
		}
		return exportFileName;
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