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

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
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

	private static final OLog log = Tracing.createLoggerFor(OnlyOfficeEditorController.class);
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;

	public OnlyOfficeEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			final DocEditorSecurityCallback securityCallback) {
		super(ureq, wControl);

		DocEditorSecurityCallback secCallback = securityCallback;
		
		if (onlyOfficeService.isLockNeeded(secCallback.getMode())) {
			if (onlyOfficeService.isLockedForMe(vfsLeaf, getIdentity())) {
				secCallback = DocEditorSecurityCallbackBuilder.clone(secCallback)
						.withMode(Mode.VIEW)
						.build();
				showWarning("editor.warning.locked");
			} else {
				onlyOfficeService.lock(vfsLeaf, getIdentity());
			}
		}
		VelocityContainer mainVC = createVelocityContainer("editor");
		
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		if (vfsMetadata == null) {
			mainVC.contextPut("warning", translate("editor.warning.no.metadata"));
		} else {
			String apiConfig = ApiConfigBuilder.builder(vfsMetadata, getIdentity())
					.withEdit(Mode.EDIT.equals(secCallback.getMode()))
					.buildJson();
			log.debug("OnlyOffice ApiConfig: " + apiConfig);
			
			if (apiConfig == null) {
				mainVC.contextPut("warning", translate("editor.warning.no.api.configs"));
			} else {
				mainVC.contextPut("id", "o_" + CodeHelper.getRAMUniqueID());
				mainVC.contextPut("apiUrl", onlyOfficeModule.getApiUrl());
				mainVC.contextPut("apiConfig", apiConfig);
			}
		}
		
		putInitialPanel(mainVC);
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
