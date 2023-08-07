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
package org.olat.core.commons.services.doceditor.drawio.ui;


import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.doceditor.drawio.DrawioService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioEditorController extends BasicController {
	
	private final Access access;
	private final VFSLeaf vfsLeaf;
	private boolean temporaryLock;

	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioService drawioService;
	@Autowired
	private DocEditorService docEditorService;

	public DrawioEditorController(UserRequest ureq, WindowControl wControl, DocEditorConfigs configs, Access access) {
		super(ureq, wControl);
		this.access = access;
		this.vfsLeaf = configs.getVfsLeaf();
		
		wControl.getWindowBackOffice().getWindow().addListener(this);
		
		boolean isEdit = Mode.EDIT.equals(configs.getMode());
		if (isEdit) {
			if(drawioService.isLockedForMe(vfsLeaf, ureq.getIdentity())) {
				isEdit = false;
				access = docEditorService.updateMode(access, Mode.VIEW);
			} else {
				drawioService.lock(vfsLeaf, getIdentity());
				temporaryLock = true;
			}
		}
		
		VelocityContainer mainVC = createVelocityContainer("view");
		putInitialPanel(mainVC);
		
		String iframeId = "o_drawio_" + CodeHelper.getRAMUniqueID();
		mainVC.contextPut("iframeId", iframeId);
		
		String viewerUrl = drawioModule.getEditorUrl() + "?embed=1&spin=1&proto=json&saveAndExit=0&noSaveBtn=1&noExitBtn=1";
		
		// read-only
		if (!isEdit) {
			viewerUrl += "&chrome=0";
		}
		
		//Language 
		String appLang = getIdentity().getUser().getPreferences().getLanguage();
		viewerUrl += "&lang=" + appLang;
		
		// Whiteboard mode
		if ("dwb".equalsIgnoreCase(FileUtils.getFileSuffix(vfsLeaf.getName()))) {
			viewerUrl += "&ui=sketch";
		}
		mainVC.contextPut("url", viewerUrl);
		
		String xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
		mainVC.contextPut("xml", StringHelper.escapeJavaScriptParam(xml));
		mainVC.contextPut("filename", vfsLeaf.getName());
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(event == Window.CLOSE_WINDOW) {
			deleteAccess();
		} else if ("save".equals(event.getCommand())) {
			String xml = ureq.getParameter("xml");
			drawioService.updateContent(vfsLeaf, getIdentity(), xml, access.isVersionControlled());
		}
	}

	@Override
	protected void doDispose() {
		deleteAccess();
		super.doDispose();
	}
	
	private void deleteAccess() {
		if (temporaryLock) {
			drawioService.unlock(vfsLeaf, getIdentity());
			temporaryLock = false;
		}
		docEditorService.deleteAccess(access);
	}

}
