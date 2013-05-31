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
import java.util.ArrayList;
import java.util.List;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPEditMainController;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.ims.cp.ui.CreateNewCPController;
import org.olat.modules.cp.CPUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class ImsCPHandler extends FileHandler implements RepositoryHandler {
	
	private static final OLog log = Tracing.createLoggerFor(ImsCPHandler.class);
	
	public static final String PROCESS_CREATENEW = "new";
	public static final String PROCESS_IMPORT = "add";
	
	private static final boolean LAUNCHEABLE = true;
	private static final boolean DOWNLOADEABLE = true;
	private static final boolean EDITABLE = true;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List<String> supportedTypes;
	
	/**
	 * 
	 */
	public ImsCPHandler() {
		//
	}

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(1);
		supportedTypes.add(ImsCPFileResource.TYPE_NAME);
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getSupportedTypes()
	 */
	public List<String> getSupportedTypes() {
		return supportedTypes;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	public boolean supportsLaunch(RepositoryEntry repoEntry) { return LAUNCHEABLE; }
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public boolean supportsDownload(RepositoryEntry repoEntry) { return DOWNLOADEABLE; }
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsEdit()
	 */
	public boolean supportsEdit(RepositoryEntry repoEntry) { return EDITABLE; }
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
	 * @see org.olat.repository.handlers.RepositoryHandler#getLaunchController(org.olat.core.id.OLATResourceable java.lang.String, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public MainLayoutController createLaunchController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
		LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
		
		// jump to either the forum or the folder if the business-launch-path says so.
		BusinessControl bc = wControl.getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		MainLayoutController layoutCtr;
		
		CPPackageConfig packageConfig = CPManager.getInstance().getCPPackageConfig(res);
		DeliveryOptions deliveryOptions = (packageConfig == null ? null : packageConfig.getDeliveryOptions());
		if ( ce != null ) { // a context path is left for me
			log.debug("businesscontrol (for further jumps) would be:"+bc);
			OLATResourceable ores = ce.getOLATResourceable();
			log.debug("OLATResourceable=" + ores);
			String typeName = ores.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if  (path.length() > 0) {
			  log.debug("direct navigation to container-path=" + path);
			  layoutCtr = CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper, true, false, deliveryOptions, path);
			} else {
				layoutCtr = CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper, deliveryOptions);
			}
		} else {
			layoutCtr = CPUIFactory.getInstance().createMainLayoutResourceableListeningWrapperController(res, ureq, wControl, vfsWrapper, deliveryOptions);
		}
		//fxdiff VCRP-1: access control of learn resources
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, res, layoutCtr);
		return wrapper;
	}


	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable
	 *      org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createEditorController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		// only unzips, if not already unzipped
		OlatRootFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(res);

		Quota quota = QuotaManager.getInstance().getCustomQuota(cpRoot.getRelPath());
		if (quota == null) {
			Quota defQuota = QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
			quota = QuotaManager.getInstance().createQuota(cpRoot.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota);
		cpRoot.setLocalSecurityCallback(secCallback);

		return new CPEditMainController(ureq, wControl, cpRoot, res);
	}

	/**
	 * 
	 * @see org.olat.repository.handlers.FileHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback,
	 *      java.lang.Object, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		if (userObject == null || userObject.equals(PROCESS_CREATENEW)) {
			return new CreateNewCPController(callback, ureq, wControl);
		} else {
			return super.createAddController(callback, userObject, ureq, wControl);
		}
	}

	
	protected String getDeletedFilePrefix() {
		return "del_imscp_"; 
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
