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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.sharedfolder.CreateNewSharedFolderController;
import org.olat.modules.sharedfolder.SharedFolderDisplayController;
import org.olat.modules.sharedfolder.SharedFolderEditorController;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
import org.olat.resource.references.ReferenceManager;


/**
 * Description:<br>
 * TODO: as Class Description for SharedFolderHandler
 * 
 * <P>
 * Initial Date: Apr 6 <br>
 * @author gnaegi
 */
public class SharedFolderHandler implements RepositoryHandler {

	private static final String PACKAGE = Util.getPackageName(RepositoryManager.class);

	private static final boolean LAUNCHEABLE = true;
	private static final boolean DOWNLOADEABLE = false;
	private static final boolean EDITABLE = true;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List supportedTypes;
	
	/**
	 * Comment for <code>PROCESS_CREATENEW</code>
	 */
	public static final String PROCESS_CREATENEW = "cn";

	/**
	 * Default constructor.
	 */
	public SharedFolderHandler() {
		super();
	}

	static { // initialize supported types
		supportedTypes = new ArrayList(1);
		supportedTypes.add(SharedFolderFileResource.TYPE_NAME);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getSupportedTypes()
	 */
	public List getSupportedTypes() {
		return supportedTypes;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return LAUNCHEABLE;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return DOWNLOADEABLE;
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
	 * @param res
	 * @param initialViewIdentifier
	 * @param ureq
	 * @param wControl
	 * @return Controller
	 */
	public MainLayoutController createLaunchController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		VFSContainer sfContainer = SharedFolderManager.getInstance().getSharedFolder(res);
		SharedFolderDisplayController sfdCtr = new SharedFolderDisplayController(ureq, wControl, sfContainer, res, false);
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, sfdCtr.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(sfdCtr); // dispose content on layout dispose
		//fxdiff VCRP-1: access control of learn resources
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, res, layoutCtr);
		return wrapper;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		return SharedFolderManager.getInstance().getAsMediaResource(res);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable
	 *      org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createEditorController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		Controller sharedFolderCtr = new SharedFolderEditorController(res, ureq, wControl);
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, sharedFolderCtr.getInitialComponent(), null);
		layoutCtr.addDisposableChildController(sharedFolderCtr); // dispose content on layout dispose
		return layoutCtr;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback,
	 *      java.lang.Object, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		return new CreateNewSharedFolderController(callback, ureq, wControl);
	}

	public Controller createDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return null;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#cleanupOnDelete(org.olat.core.id.OLATResourceable
	 *      org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean cleanupOnDelete(OLATResourceable res) {
		// do not need to notify all current users of this resource, since the only
		// way to access this resource
		// FIXME:fj:c to be perfect, still need to notify
		// repositorydetailscontroller and searchresultcontroller....
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		SharedFolderManager.getInstance().deleteSharedFolder(res);
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#readyToDelete(org.olat.core.id.OLATResourceable
	 *      org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = new PackageTranslator(PACKAGE, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references", new String[] { referencesSummary }));
			return false;
		}
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#createCopy(org.olat.core.id.OLATResourceable
	 *      org.olat.core.gui.UserRequest)
	 */
	public OLATResourceable createCopy(OLATResourceable res, UserRequest ureq) {
		return FileResourceManager.getInstance().createCopy(res);
	}

	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		return SharedFolderManager.getInstance().archive(archivFilePath, repoEntry);
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
