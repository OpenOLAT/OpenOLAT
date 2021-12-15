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
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPContentController;
import org.olat.ims.cp.ui.CPEditMainController;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.ims.cp.ui.CPRuntimeController;
import org.olat.modules.cp.CPAssessmentProvider;
import org.olat.modules.cp.CPDisplayController;
import org.olat.modules.cp.PersistingAssessmentProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
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
public class ImsCPHandler extends FileHandler {
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return true;
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		OLATResource resource = OLATResourceManager.getInstance().createOLATResourceInstance("FileResource.IMSCP");
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();

		Translator translator = Util.createPackageTranslator(CPContentController.class, locale);
		String initialPageTitle = translator.translate("cptreecontroller.newpage.title");
		CoreSpringFactory.getImpl(CPManager.class).createNewCP(resource, initialPageTitle);
		return re;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "tools.add.cp";
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ImsCPFileResource.evaluate(file, filename);
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

		ImsCPFileResource cpResource = new ImsCPFileResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(cpResource);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRoot(resource);
		File zipRoot = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipRoot);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		//
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		final CPManager cpManager = CoreSpringFactory.getImpl(CPManager.class);
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRoot(sourceResource);
		File zipRoot = new File(sourceFileroot, FileResourceManager.ZIPDIR);
		
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRoot(targetResource);
		FileUtils.copyFileToDir(zipRoot, targetFileroot, "add file resource");
		
		//copy packaging info
		CPPackageConfig cpConfig = cpManager.getCPPackageConfig(sourceResource);
		if(cpConfig != null) {
			cpManager.setCPPackageConfig(targetResource, cpConfig);
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
		return ImsCPFileResource.TYPE_NAME;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.yes;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		OLATResource res = re.getOlatResource();
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
		final LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
		CPManager cpManager = CoreSpringFactory.getImpl(CPManager.class);
		CPPackageConfig packageConfig = cpManager.getCPPackageConfig(res);
		final DeliveryOptions deliveryOptions = (packageConfig == null ? null : packageConfig.getDeliveryOptions());

		return new CPRuntimeController(ureq, wControl, re, reSecurity,
				(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
			boolean activateFirstPage = true;
			String initialUri = null;

			CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
				.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
			
			CPAssessmentProvider cpAssessmentProvider = PersistingAssessmentProvider.create(re, uureq.getIdentity(), false, false);
			CPDisplayController cpCtr = new CPDisplayController(uureq, wwControl, vfsWrapper, true, true, activateFirstPage, true, deliveryOptions,
					initialUri, entry.getOlatResource(), "", false, cpAssessmentProvider);
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(uureq, wwControl, cpCtr.getMenuComponent(), cpCtr.getInitialComponent(), vfsWrapper.getName());
			ctr.addDisposableChildController(cpCtr);
			ctr.addActivateableDelegate(cpCtr);
			return ctr;
		});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		// only unzips, if not already unzipped
		VFSContainer cpRoot = FileResourceManager.getInstance().unzipContainerResource(re.getOlatResource());

		QuotaManager quotaManager = CoreSpringFactory.getImpl(QuotaManager.class);
		Quota quota = quotaManager.getCustomQuota(cpRoot.getRelPath());
		if (quota == null) {
			Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
			quota = quotaManager.createQuota(cpRoot.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota);
		cpRoot.setLocalSecurityCallback(secCallback);

		return new CPEditMainController(ureq, wControl, toolbar, cpRoot, re);
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}
	
	@Override
	protected String getDeletedFilePrefix() {
		return "del_imscp_"; 
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
