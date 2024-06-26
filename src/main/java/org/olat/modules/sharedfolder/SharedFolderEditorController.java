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

import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date:  Aug 29, 2005 <br>
 * @author Alexander Schneider
 */
public class SharedFolderEditorController extends BasicController implements Activateable2 {
	
	private static final FolderControllerConfig FOLEDER_CONFIG = FolderControllerConfig.builder()
			.withMail(FolderEmailFilter.never)
			.build();
			
	private VelocityContainer vcEdit;
	private Link previewButton;
	
	private RepositoryEntry re;
	private VFSContainer sharedFolder;
	private FolderController folderCtrl;
	private CloseableModalController cmc;
	private Controller controller;
	private SharedFolderDisplayController sfdCtr;

	public SharedFolderEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.re = re;
		
		vcEdit = createVelocityContainer("index");
		putInitialPanel(vcEdit);
		
		previewButton = LinkFactory.createButtonSmall("command.preview", vcEdit, this);
		
		sharedFolder = SharedFolderManager.getInstance().getNamedSharedFolder(re, false);
		folderCtrl = new FolderController(ureq, wControl, sharedFolder, FOLEDER_CONFIG);
		listenTo(folderCtrl);
		vcEdit.put("folder", folderCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		folderCtrl.activate(ureq, entries, state);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) {
			VFSContainer sharedFolderPreview = SharedFolderManager.getInstance().getNamedSharedFolder(re, false);
			sfdCtr = new SharedFolderDisplayController(ureq, getWindowControl(), sharedFolderPreview, re);
			listenTo(controller);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), sfdCtr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
}
