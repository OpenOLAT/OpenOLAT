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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.modules.scorm.ScormPackageConfig;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
import org.olat.util.logging.activity.LoggingResourceable;


/**
 * @author Guido Schnider
 * 
 * Comment:  
 * 
 */
public class SCORMCPHandler extends FileHandler {

	private static final List<String> supportedTypes = Collections.singletonList(ScormCPFileResource.TYPE_NAME);

	public SCORMCPHandler() {
		//
	}
	
	@Override
	public boolean isCreate() {
		return false;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return null;
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
		return null;
	}
	
	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}
	
	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ScormCPFileResource.evaluate(file, filename);
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String displayname, String description, boolean withReferences,
			Locale locale, File file, String filename) {
		
		ScormCPFileResource scormResource = new ScormCPFileResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(scormResource);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipRoot = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipRoot);
	
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public void addExtendedEditionControllers(UserRequest ureq, WindowControl wControl,
			AuthoringEditEntrySettingsController pane, RepositoryEntry entry) {
		
		ScormPackageConfig scormConfig = ScormMainManager.getInstance().getScormPackageConfig(entry.getOlatResource());
		DeliveryOptions config = scormConfig == null ? null : scormConfig.getDeliveryOptions();
		final OLATResource resource = entry.getOlatResource();
		final DeliveryOptionsConfigurationController deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, wControl, config);
		pane.appendEditor(pane.getTranslator().translate("tab.layout"), deliveryOptionsCtrl);
		
		deliveryOptionsCtrl.addControllerListener(new ControllerEventListener() {
			@Override
			public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
				if(source == deliveryOptionsCtrl && (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT)) {
					DeliveryOptions newConfig = deliveryOptionsCtrl.getDeliveryOptions();
					ScormPackageConfig scormConfig = ScormMainManager.getInstance().getScormPackageConfig(resource);
					if(scormConfig == null) {
						scormConfig = new ScormPackageConfig();
					}
					scormConfig.setDeliveryOptions(newConfig);
					ScormMainManager.getInstance().setScormPackageConfig(resource, scormConfig);
				}
			}
		});
	}
	
	@Override
	public RepositoryEntry copy(RepositoryEntry source, RepositoryEntry target) {
		final ScormMainManager scormManager = ScormMainManager.getInstance();
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
	public List<String> getSupportedTypes() {
		return supportedTypes;
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
		return false;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		if (re != null) {
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
		}
		OLATResource res = re.getOlatResource();
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
		MainLayoutController realController = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, wControl, true, null, cpRoot,
				res.getResourceableId(), null, "browse", "no-credit", false, null, false, false, false, null);
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, realController);
		return wrapper; 
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
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

	@Override
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("not implemented");
	}
}