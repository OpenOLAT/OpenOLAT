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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
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
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.fileresource.FileResourceManager;
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
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.controllers.AddFileResourceController;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.OLATResource;
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

	private static final boolean LAUNCHEABLE = true;
	private static final boolean DOWNLOADEABLE = true;
	private static final boolean EDITABLE = false;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List<String> supportedTypes;

	/**
	 * Comment for <code>PROCESS_CREATENEW</code>
	 */
	public static final String PROCESS_CREATENEW = "cn";
	public static final String PROCESS_UPLOAD = "pu";

	public WikiHandler() {
		//
	}
	

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(1);
		supportedTypes.add(WikiResource.TYPE_NAME);
	}

	public List<String> getSupportedTypes() {
		return supportedTypes;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return DOWNLOADEABLE;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return LAUNCHEABLE;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsEdit()
	 */
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return EDITABLE;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsWizard(org.olat.repository.RepositoryEntry)
	 */
	public boolean supportsWizard(RepositoryEntry repoEntry) { return WIZARD_SUPPORT; }
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getCreateWizardController(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getLaunchController(org.olat.resource.OLATResourceable,
	 *      java.lang.String, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
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
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, controller.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(controller); // dispose content on layout dispose
		if(controller instanceof Activateable2) {
			layoutCtr.addActivateableDelegate((Activateable2)controller);
		}
		
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, layoutCtr);	
		return wrapper;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.resource.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		//edit is always part of a wiki
		return null;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.resource.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(res);
		VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
		return new VFSMediaResource(wikiZip);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#cleanupOnDelete(org.olat.resource.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public boolean cleanupOnDelete(OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		//delete also notifications
		NotificationsManager.getInstance().deletePublishersOf(res);
		FileResourceManager.getInstance().deleteFileResource(res);
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#readyToDelete(org.olat.resource.OLATResourceable,
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
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

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#createCopy(org.olat.resource.OLATResourceable,
	 *      org.olat.core.gui.UserRequest)
	 */
	public OLATResourceable createCopy(OLATResourceable res, UserRequest ureq) {
		FileResourceManager frm = FileResourceManager.getInstance();
		VFSContainer wikiContainer = WikiManager.getInstance().getWikiContainer(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		if(wikiContainer==null) {
			//if the wiki container is null, let the WikiManager to create one
			WikiManager.getInstance().getOrLoadWiki(res);
		}
		OLATResourceable copy = frm.createCopy(res, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		VFSContainer rootContainer = frm.getFileResourceRootImpl(copy);
		//create folders
		VFSContainer newMediaCont = rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME); 
		rootContainer.createChildContainer(WikiManager.VERSION_FOLDER_NAME);
		//copy media files to folders
		VFSContainer origRootContainer = frm.getFileResourceRootImpl(res);
		VFSContainer origMediaCont = (VFSContainer)origRootContainer.resolve(WikiContainer.MEDIA_FOLDER_NAME);
		List<VFSItem> mediaFiles = origMediaCont.getItems();
		for (Iterator<VFSItem> iter = mediaFiles.iterator(); iter.hasNext();) {
			VFSLeaf element = (VFSLeaf) iter.next();
			newMediaCont.copyFrom(element);
		}

		//reset properties files to default values
		VFSContainer wikiCont = (VFSContainer)rootContainer.resolve(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		List<VFSItem> leafs = wikiCont.getItems(new VFSItemSuffixFilter(new String[]{WikiManager.WIKI_PROPERTIES_SUFFIX}));
		for (Iterator<VFSItem> iter = leafs.iterator(); iter.hasNext();) {
			VFSLeaf leaf = (VFSLeaf) iter.next();
			WikiPage page = Wiki.assignPropertiesToPage(leaf);
			//reset the copied pages to a the default values
			page.resetCopiedPage();
			WikiManager.getInstance().updateWikiPageProperties(copy, page);
		}
		
		return copy;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback,
	 *      java.lang.Object, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		if (userObject == null || userObject.equals(WikiHandler.PROCESS_UPLOAD))
			return new AddFileResourceController(callback, supportedTypes, new String[] {"zip"}, ureq, wControl);
		else
			return new WikiCreateController(callback, ureq, wControl);
	}

	public Controller createDetailsForm( UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
	}

	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(repoEntry.getOlatResource());
		VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(rootContainer);
		String exportFileName = "del_wiki_" + repoEntry.getOlatResource().getResourceableId() + ".zip";
		String fullFilePath = archivFilePath + File.separator + exportFileName;
		
		File fExportZIP = new File(fullFilePath);

		try (InputStream fis = wikiZip.getInputStream()) {
			FileUtils.bcopy(fis, fExportZIP, "archive wiki");
		} catch (FileNotFoundException e) {
			log.warn("Can not archive wiki repoEntry=" + repoEntry);
		} catch (IOException ioe) {
			log.warn("Can not archive wiki repoEntry=" + repoEntry);
		}
		return exportFileName;
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#acquireLock(org.olat.core.id.OLATResourceable, org.olat.core.id.Identity)
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
    //nothing to do
		return null;
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#releaseLock(org.olat.core.util.coordinate.LockResult)
	 */
	public void releaseLock(LockResult lockResult) {
		//nothing to do since nothing locked
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#isLocked(org.olat.core.id.OLATResourceable)
	 */
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}
	
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("not implemented");
	}
	
}
