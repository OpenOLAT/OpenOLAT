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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.types.AnimationFileResource;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.MovieFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.AddFileResourceController;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseResourceController;


/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class WebDocumentHandler extends FileHandler implements RepositoryHandler {
	
	private static final boolean LAUNCHEABLE = false;
	private static final boolean DOWNLOADEABLE = true;
	private static final boolean EDITABLE = false;
	private static final boolean WIZARD_SUPPORT = false;
	private static final List<String> supportedTypes;
	
	
	/**
	 * Default constructor.
	 */
	public WebDocumentHandler() { super(); }

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(5);
		supportedTypes.add(FileResource.GENERIC_TYPE_NAME);
		supportedTypes.add(DocFileResource.TYPE_NAME);
		supportedTypes.add(XlsFileResource.TYPE_NAME);
		supportedTypes.add(PowerpointFileResource.TYPE_NAME);
		supportedTypes.add(PdfFileResource.TYPE_NAME);
		supportedTypes.add(SoundFileResource.TYPE_NAME);
		supportedTypes.add(MovieFileResource.TYPE_NAME);
		supportedTypes.add(AnimationFileResource.TYPE_NAME);
		supportedTypes.add(ImageFileResource.TYPE_NAME);
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
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re,  UserRequest ureq, WindowControl wControl) {
		/* For the time beeing, disable launching web ressources...
		FileResourceManager frm = FileResourceManager.getInstance();
		ureq.getDispatchResult().setResultingMediaResource(frm.getAsInlineMediaResource(res));
		*/
		return null;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("a web document is not editable!!! res-id:"+re.getResourceableId());
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback, java.lang.Object, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		return new AddFileResourceController(callback, supportedTypes, ureq, wControl);
	}

	protected String getDeletedFilePrefix() {
		return "del_webdoc_"; 
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
