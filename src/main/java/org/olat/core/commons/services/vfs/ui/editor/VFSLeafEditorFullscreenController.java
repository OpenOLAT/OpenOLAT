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

import org.olat.core.commons.services.vfs.VFSLeafEditorConfigs;
import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 1 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VFSLeafEditorFullscreenController extends BasicController {
	
	private VFSLeafEditorController editorCtrl;
	
	private final VFSLeaf vfsLeaf;

	public VFSLeafEditorFullscreenController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			VFSLeafEditorSecurityCallback secCallback, VFSLeafEditorConfigs configs) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		
		editorCtrl = new VFSLeafEditorController(ureq, wControl, vfsLeaf, secCallback, configs);
		listenTo(editorCtrl);
		doOpenEditor();
	}
	
	public VFSLeafEditorFullscreenController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			VFSLeafEditorSecurityCallback secCallback, VFSLeafEditorConfigs configs, String cssClass) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		
		editorCtrl = new VFSLeafEditorController(ureq, wControl, vfsLeaf, secCallback, configs, cssClass);
		listenTo(editorCtrl);
		doOpenEditor();
	}

	public VFSLeaf getVfsLeaf() {
		return vfsLeaf;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtrl) {
			doCloseEditor();
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@SuppressWarnings("deprecation")
	private void doOpenEditor() {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		String businessPath = editorCtrl.getWindowControlForDebug().getBusinessControl().getAsString();
		cc.getScreenMode().setMode(Mode.full, businessPath);
		getWindowControl().pushToMainArea(editorCtrl.getInitialComponent());
	}
	
	private void doCloseEditor() {
		getWindowControl().pop();
		String businessPath = getWindowControl().getBusinessControl().getAsString();
		getWindowControl().getWindowBackOffice().getChiefController().getScreenMode().setMode(Mode.standard, businessPath);
		
		removeAsListenerAndDispose(editorCtrl);
		editorCtrl = null;
	}
	

	@Override
	protected void doDispose() {
		//
	}

}
