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
* <p>
*/ 

package org.olat.repository.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.util.logging.activity.LoggingResourceable;


/**
 * @author Guido Schnider
 * 
 * Comment:  
 * 
 */
public class SCORMCPHandler extends FileHandler implements RepositoryHandler {
	
	private static final boolean LAUNCHEABLE = true;
	private static final boolean DOWNLOADEABLE = true;
	private static final boolean EDITABLE = false;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List supportedTypes;
	
	/**
	 * 
	 */
	public SCORMCPHandler() {
		//
	}

	static { // initialize supported types
		supportedTypes = new ArrayList(1);
		supportedTypes.add(ScormCPFileResource.TYPE_NAME);
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
	public MainLayoutController createLaunchController(OLATResourceable res, String initialViewIdentifier, UserRequest ureq, WindowControl wControl) {
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(res);
		//Controller realController = new ScormAPIandDisplayController(ureq, wControl, true, null, cpRoot, res.getResourceableId().toString(), null, "browse", "no-credit");
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(res, false);
		if (re!=null) {
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapScormRepositoryEntry(re));
		}
		MainLayoutController realController = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, wControl, true, null, cpRoot, res.getResourceableId().toString(), null, "browse", "no-credit", false, false);
		return realController; 
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createEditorController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get editor for an SCORM CP type where no editor is provided for this type.");
	}

	protected String getDeletedFilePrefix() {
		return "del_scorm_"; 
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
	 * @see org.olat.repository.handlers.RepositoryHandler#isLocked(org.olat.core.id.OLATResourceable)
	 */
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#releaseLock(org.olat.core.util.coordinate.LockResult)
	 */
	public void releaseLock(LockResult lockResult) {
		//nothing to do since nothing locked
	}
	
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		throw new AssertException("not implemented");
	}
}