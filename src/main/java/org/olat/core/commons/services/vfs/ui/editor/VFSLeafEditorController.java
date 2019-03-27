/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs.ui.editor;

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSLeafEditor;
import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 26 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VFSLeafEditorController extends BasicController {
	
	private VelocityContainer mainVC;
	private VFSLeafConfigController configCtrl;
	private Controller editorCtrl;

	private final VFSLeaf vfsLeaf;
	private final FolderComponent folderComponent;
	private final VFSLeafEditorSecurityCallback secCallback;
	
	public VFSLeafEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			FolderComponent folderComponent, VFSLeafEditorSecurityCallback secCallback) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		this.folderComponent = folderComponent;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("editor_main");
		
		configCtrl = new VFSLeafConfigController(ureq, wControl, vfsLeaf, secCallback);
		listenTo(configCtrl);
		mainVC.put("config", configCtrl.getInitialComponent());
		configCtrl.activate(ureq, null, null);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			if (event instanceof VFSEditorSelectionEvent) {
				VFSEditorSelectionEvent esEvent = (VFSEditorSelectionEvent) event;
				VFSLeafEditor editor = esEvent.getEditor();
				doOpenEditor(ureq, editor);
			} else if (event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == editorCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	private void doOpenEditor(UserRequest ureq, VFSLeafEditor editor) {
		removeAsListenerAndDispose(editorCtrl);
		
		if (editorCtrl != null) mainVC.remove("editor");
		editorCtrl = editor.getRunController(ureq, getWindowControl(), vfsLeaf, folderComponent, getIdentity(), secCallback);
		listenTo(editorCtrl);
		mainVC.put("editor", editorCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
