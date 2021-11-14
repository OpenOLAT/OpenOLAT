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

package org.olat.modules.sharedfolder;

import java.util.List;

import org.olat.core.commons.modules.bc.FolderRunController;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date:  Aug 29, 2005 <br>
 * @author Alexander Schneider
 */
public class SharedFolderEditorController extends DefaultController implements Activateable2 {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(SharedFolderEditorController.class);
	
	private Translator translator;
	private VelocityContainer vcEdit;
	private Link previewButton;
	
	private RepositoryEntry re;
	private VFSContainer sharedFolder;
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
	public SharedFolderEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		super(wControl);
		
		translator = Util.createPackageTranslator(SharedFolderEditorController.class, ureq.getLocale());

		vcEdit = new VelocityContainer("main", VELOCITY_ROOT + "/index.html", translator, this);
		previewButton = LinkFactory.createButtonSmall("command.preview", vcEdit, this);
		
		this.re = re;
		sharedFolder = SharedFolderManager.getInstance().getNamedSharedFolder(re, false);
		folderRunController = new FolderRunController(sharedFolder, true, true, false, ureq, getWindowControl());
		vcEdit.put("folder", folderRunController.getInitialComponent());
		
		setInitialComponent(vcEdit);

	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			folderRunController.activate(ureq, entries, state);
		} else {
			String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
			if(StringHelper.containsNonWhitespace(path)) {
				folderRunController.activatePath(ureq, path);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) {
			VFSContainer sharedFolderPreview = SharedFolderManager.getInstance().getNamedSharedFolder(re, false);
			sfdCtr = new SharedFolderDisplayController(ureq, getWindowControl(), sharedFolderPreview, re);
			cmc = new CloseableModalController(getWindowControl(), translator.translate("close"), sfdCtr.getInitialComponent());
			cmc.activate();
		}
	}
			
	@Override
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
        super.doDispose();
	}
}
