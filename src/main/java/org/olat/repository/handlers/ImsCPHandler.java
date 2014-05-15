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

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
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
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPContentController;
import org.olat.ims.cp.ui.CPEditMainController;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.modules.cp.CPOfflineReadableManager;
import org.olat.modules.cp.CPUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class ImsCPHandler extends FileHandler {
	
	private static final OLog log = Tracing.createLoggerFor(ImsCPHandler.class);
	
	@Override
	public boolean isCreate() {
		return true;
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
		OLATResource resource = OLATResourceManager.getInstance().createOLATResourceInstance("FileResource.IMSCP");
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();

		Translator translator = Util.createPackageTranslator(CPContentController.class, locale);
		String initialPageTitle = translator.translate("cptreecontroller.newpage.title");
		CPManager.getInstance().createNewCP(resource, initialPageTitle);
		return re;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "tools.add.cp";
	}
	
	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}
	
	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ImsCPFileResource.evaluate(file, filename);
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String displayname, String description, boolean withReferences,
			Locale locale, File file, String filename) {

		ImsCPFileResource cpResource = new ImsCPFileResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(cpResource);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipRoot = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipRoot);
		CPOfflineReadableManager.getInstance().makeCPOfflineReadable(cpResource, displayname);

		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public void addExtendedEditionControllers(UserRequest ureq, WindowControl wControl,
			AuthoringEditEntrySettingsController pane, RepositoryEntry entry) {
		
		final OLATResource resource = entry.getOlatResource();
		final CPManager cpManager = CPManager.getInstance();
		QuotaManager qm = QuotaManager.getInstance();
		if (qm.hasQuotaEditRights(ureq.getIdentity())) {
			OlatRootFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(resource);
			Controller quotaCtrl = qm.getQuotaEditorInstance(ureq, wControl, cpRoot.getRelPath(), false);
			pane.appendEditor(pane.getTranslator().translate("tab.quota.edit"), quotaCtrl);
		}
		
		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(resource);
		DeliveryOptions config = cpConfig == null ? null : cpConfig.getDeliveryOptions();
		final DeliveryOptionsConfigurationController deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, wControl, config);
		pane.appendEditor(pane.getTranslator().translate("tab.layout"), deliveryOptionsCtrl);
		deliveryOptionsCtrl.addControllerListener(new ControllerEventListener() {

			@Override
			public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
				if(source == deliveryOptionsCtrl
						&& (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT)) {
					DeliveryOptions newConfig = deliveryOptionsCtrl.getDeliveryOptions();
					CPPackageConfig cpConfig = cpManager.getCPPackageConfig(resource);
					if(cpConfig == null) {
						cpConfig = new CPPackageConfig();
					}
					cpConfig.setDeliveryOptions(newConfig);
					cpManager.setCPPackageConfig(resource, cpConfig);
				}
			}
		});
	}
	
	@Override
	public RepositoryEntry copy(RepositoryEntry source, RepositoryEntry target) {
		final CPManager cpManager = CPManager.getInstance();
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File zipRoot = new File(sourceFileroot, FileResourceManager.ZIPDIR);
		
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyFileToDir(zipRoot, targetFileroot, "add file resource");
		
		//copy packaging info
		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(sourceResource);
		if(cpConfig != null) {
			cpManager.setCPPackageConfig(targetResource, cpConfig);
		}

		CPOfflineReadableManager.getInstance().makeCPOfflineReadable(targetResource, target.getDisplayname() + ".zip");
		return target;
	}

	@Override
	public String getSupportedType() {
		return ImsCPFileResource.TYPE_NAME;
	}

	@Override
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		OLATResource res = re.getOlatResource();
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
		
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, layoutCtr);
		return wrapper;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		// only unzips, if not already unzipped
		OlatRootFolderImpl cpRoot = FileResourceManager.getInstance().unzipContainerResource(re.getOlatResource());

		Quota quota = QuotaManager.getInstance().getCustomQuota(cpRoot.getRelPath());
		if (quota == null) {
			Quota defQuota = QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
			quota = QuotaManager.getInstance().createQuota(cpRoot.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota);
		cpRoot.setLocalSecurityCallback(secCallback);

		return new CPEditMainController(ureq, wControl, cpRoot, re.getOlatResource());
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
