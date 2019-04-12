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
package org.olat.core.commons.services.doceditor.onlyoffice.ui;

import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OnlyOfficeEditorController extends BasicController {

	private final VFSLeaf vfsLeaf;
	private DocEditorSecurityCallback secCallback;
	private Access access;
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;

	public OnlyOfficeEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			final DocEditorSecurityCallback securityCallback) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		this.secCallback = securityCallback;
		
		VelocityContainer mainVC = createVelocityContainer("editor");
		
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		if (vfsMetadata == null) {
			mainVC.contextPut("warning", translate("editor.warning.no.metadata"));
		} else {
			this.access = onlyOfficeService.createAccess(vfsMetadata, getIdentity(), secCallback);
			mainVC.contextPut("key", access.getToken());
			mainVC.contextPut("fileId", access.getFileId());
			mainVC.contextPut("accessToken", access.getToken());
			mainVC.contextPut("id", "o_" + CodeHelper.getRAMUniqueID());
			mainVC.contextPut("apiUrl", onlyOfficeModule.getApiUrl());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		onlyOfficeService.deleteAccess(access);
	}

}
