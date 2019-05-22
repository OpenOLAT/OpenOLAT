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
package org.olat.core.commons.services.doceditor.ui;

import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 1 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorFullscreenController extends BasicController {
	
	private DocEditorController editorCtrl;
	
	private final VFSLeaf vfsLeaf;

	public DocEditorFullscreenController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			DocEditorSecurityCallback secCallback, DocEditorConfigs configs) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		
		editorCtrl = new DocEditorController(ureq, wControl, vfsLeaf, secCallback, configs);
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

	private void doOpenEditor() {
		getWindowControl().pushFullScreen(editorCtrl, "o_doceditor_body");
	}
	
	private void doCloseEditor() {
		getWindowControl().pop();
		removeAsListenerAndDispose(editorCtrl);
		editorCtrl = null;
	}

	@Override
	protected void doDispose() {
		if(editorCtrl != null) {
			doCloseEditor();
		}
	}
}
