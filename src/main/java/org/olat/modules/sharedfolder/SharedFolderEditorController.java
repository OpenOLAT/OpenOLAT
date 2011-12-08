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

package org.olat.modules.sharedfolder;

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Initial Date:  Aug 29, 2005 <br>
 * @author Alexander Schneider
 */
public class SharedFolderEditorController extends DefaultController {
	private static final String PACKAGE = Util.getPackageName(SharedFolderEditorController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);
	
	private Translator translator;
	private VelocityContainer vcEdit;
	private Link previewButton;
	
	private RepositoryEntry re;
	private OlatNamedContainerImpl sharedFolder;
	private FolderRunController folderRunController;
	private CloseableModalController cmc;
	private Controller controller;
	private SharedFolderDisplayController sfdCtr;

	/**
	 * 
	 * @param res
	 * @param ureq
	 * @param wControl
	 */
	public SharedFolderEditorController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		super(wControl);
		
		translator = new PackageTranslator(PACKAGE, ureq.getLocale());

		vcEdit = new VelocityContainer("main", VELOCITY_ROOT + "/index.html", translator, this);
		previewButton = LinkFactory.createButtonSmall("command.preview", vcEdit, this);
		
		re = RepositoryManager.getInstance().lookupRepositoryEntry(res, true);
		sharedFolder = SharedFolderManager.getInstance().getNamedSharedFolder(re);
		folderRunController = new FolderRunController(sharedFolder, true, true, false, ureq, getWindowControl());
		vcEdit.put("folder", folderRunController.getInitialComponent());
		
		setInitialComponent(vcEdit);

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) {
			VFSContainer sharedFolderPreview = SharedFolderManager.getInstance().getNamedSharedFolder(re);
			sfdCtr = new SharedFolderDisplayController(ureq, getWindowControl(), sharedFolderPreview, re, true);
			cmc = new CloseableModalController(getWindowControl(), translator.translate("close"), sfdCtr.getInitialComponent());
			cmc.activate();
		}
	}
			
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (folderRunController != null) {
			folderRunController.dispose();
			folderRunController = null;
		}
		if (controller != null) {
			sfdCtr.dispose();
			sfdCtr = null;
		}
		if (cmc != null) {
			cmc.dispose();
			cmc = null;
		}
	}
}
