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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.onlyoffice.ApiConfig;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OnlyOfficeEditorController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(OnlyOfficeEditorController.class);
	
	private Access access;
	private Long openVfsMetadataKey;
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;
	@Autowired
	private DocEditorService docEditorService;

	public OnlyOfficeEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			final DocEditorConfigs configs, Access runAccess) {
		super(ureq, wControl);
		access = runAccess;
		
		if (Mode.EDIT == access.getMode() && !onlyOfficeService.isEditLicenseAvailable()) {
			access = docEditorService.updateMode(access, Mode.VIEW);
			showWarning("editor.warning.no.edit.license");
		}
		
		if (onlyOfficeService.isLockNeeded(access.getMode())) {
			if (onlyOfficeService.isLockedForMe(vfsLeaf, getIdentity())) {
				access = docEditorService.updateMode(access, Mode.VIEW);
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
			String mediaUrl = null;
			if (configs.isDownloadEnabled() && Mode.EMBEDDED == access.getMode()) {
				mediaUrl = Settings.createServerURI() + registerMapper(ureq, new VFSMediaMapper(configs.getVfsLeaf()));
			}
			ApiConfig apiConfig = onlyOfficeService.getApiConfig(vfsMetadata, getIdentity(), access.getMode(),
					access.isDownload(), configs.isVersionControlled(),  mediaUrl);
			String apiConfigJson = onlyOfficeService.toJson(apiConfig);
			log.debug("OnlyOffice ApiConfig: {}", apiConfigJson);
			
			if (apiConfig == null) {
				mainVC.contextPut("warning", translate("editor.warning.no.api.configs"));
			} else {
				mainVC.contextPut("id", "o_" + CodeHelper.getRAMUniqueID());
				mainVC.contextPut("apiUrl", onlyOfficeModule.getApiUrl());
				mainVC.contextPut("apiConfig", apiConfigJson);
				mainVC.contextPut("mobileEnabled", onlyOfficeModule.getMobileModes().contains(access.getMode()));
				mainVC.contextPut("mobileQuery", onlyOfficeModule.getMobileQuery());
				
				openVfsMetadataKey = vfsMetadata.getKey();
				log.info("Document (key={}) opened with ONLYOFFICE ({}) by {}", openVfsMetadataKey, access.getMode(),
						getIdentity());
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
		deleteAccess();
	}
	
	private void deleteAccess() {
		if (access != null) {
			log.info("Document (key={}) closed with ONLYOFFICE ({}) by {}", openVfsMetadataKey, access.getMode(),
					getIdentity());
			docEditorService.deleteAccess(access);
		}
	}
}
