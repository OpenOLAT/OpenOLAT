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


import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorConfigs.Config;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditor;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditorConfig;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.doceditor.drawio.DrawioService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioEditorController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(DrawioEditorController.class);
	
	private Access access;
	private final VFSLeaf vfsLeaf;
	private VFSLeaf svgPreviewLeaf;
	private VFSLeaf pngPreviewLeaf;
	private boolean temporaryLock;

	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioService drawioService;
	@Autowired
	private DrawioEditor drawioEditor;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;

	public DrawioEditorController(UserRequest ureq, WindowControl wControl, DocEditorConfigs configs, Access access) {
		super(ureq, wControl);
		this.access = access;
		this.vfsLeaf = configs.getVfsLeaf();
		Config config = configs.getConfig(DrawioEditorConfig.TYPE);
		if (config instanceof DrawioEditorConfig drawioEditorConfig) {
			svgPreviewLeaf = drawioEditorConfig.getSvgPreviewLeaf();
			pngPreviewLeaf = drawioEditorConfig.getPngPreviewLeaf();
		}
		
		wControl.getWindowBackOffice().getWindow().addListener(this);
		
		boolean isCollaborative = drawioModule.isCollaborationEnabled() && Mode.EMBEDDED != access.getMode();
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
		
		// https://www.drawio.com/doc/faq/embed-mode
		// https://www.drawio.com/doc/faq/supported-url-parameters
		String viewerUrl = drawioModule.getEditorUrl() + "?spin=1&embed=1&proto=json&saveAndExit=0&noSaveBtn=1&noExitBtn=1";
		
		// To enable collaborative support we mimic Nextcloud
		//https://github.com/jgraph/drawio/blob/45f88bc27ebe62f65a09a48d4b1a8d914df3ba97/src/main/webapp/plugins/nextcloud.js
		//https://github.com/jgraph/drawio-nextcloud/blob/570ad029e74c1f6584d3919da8dd67cdab351269/src/editor.js
		if (isCollaborative) {
			viewerUrl += "&embedRT=1";  // real-time
			viewerUrl += "&p=nxtcld"; // Nextcloud plugin
			viewerUrl += "&configure=1";
			viewerUrl += "&keepmodified=1";
		}
		
		// read-only
		if (!isEdit) {
			viewerUrl += "&chrome=0";
		}
		
		//Language 
		String appLang = getIdentity().getUser().getPreferences().getLanguage();
		viewerUrl += "&lang=" + appLang;
		
		// Whiteboard mode
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		if ("dwb".equalsIgnoreCase(suffix)) {
			viewerUrl += "&ui=sketch";
		} else {
			viewerUrl += "&ui=simple";
		}
		mainVC.contextPut("url", viewerUrl);
		
		String xml = drawioService.getContent(vfsLeaf);
		mainVC.contextPut("xml", StringHelper.escapeJavaScriptParam(xml));
		mainVC.contextPut("filename", vfsLeaf.getName());
		mainVC.contextPut("png", "png".equalsIgnoreCase(suffix));
		mainVC.contextPut("svg", "svg".equalsIgnoreCase(suffix));
		mainVC.contextPut("svgPreview", svgPreviewLeaf != null);
		mainVC.contextPut("pngPreview", pngPreviewLeaf != null);
		
		mainVC.contextPut("collaborative", isCollaborative);
		if (isCollaborative) {
			mainVC.contextPut("fileId", vfsLeaf.getMetaInfo().getUuid());
			mainVC.contextPut("instanceId", WebappHelper.getInstanceId());
			mainVC.contextPut("userKey", getIdentity().getKey());
			mainVC.contextPut("userDisplayName", userManager.getUserDisplayName(getIdentity().getKey()));
			mainVC.contextPut("fileInfoUrl", drawioService.getFileInfoUrl(access));
			mainVC.contextPut("fileContentUrl", drawioService.getFileContentUrl(access));
			
			if (log.isDebugEnabled()) {
				mainVC.contextPut("debugEnabled", Boolean.TRUE);
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(event == Window.CLOSE_WINDOW) {
			deleteAccess();
		} else if ("saveXml".equals(event.getCommand())) {
			String xml = ureq.getParameter("xml");
			drawioService.updateContent(access, getIdentity(), xml.getBytes());
			updateExpiresAt();
		} else if ("export".equals(event.getCommand())) {
			String data = ureq.getParameter("data");
			if (data.startsWith(DrawioService.PNG_BASE64_PREFIX)) {
				data = data.substring(DrawioService.PNG_BASE64_PREFIX.length());
				byte[] content = Base64.getDecoder().decode(data);
				if (pngPreviewLeaf != null) {
					updatePngPreview(content);
				} else {
					drawioService.updateContent(access, getIdentity(), content);
					updateExpiresAt();
				}
			} else if (data.startsWith(DrawioService.SVG_BASE64_PREFIX)) {
				data = data.substring(DrawioService.SVG_BASE64_PREFIX.length());
				byte[] content = Base64.getDecoder().decode(data);
				if (svgPreviewLeaf != null) {
					updateSvgPreview(content);
				} else {
					drawioService.updateContent(access, getIdentity(), content);
					updateExpiresAt();
				}
			}
		}
	}
	
	private void updateExpiresAt() {
		if (Mode.EDIT != access.getMode() || access.getEditStartDate() != null) {
			Date expiresAt = Date.from(Instant.now().plus(Duration.ofMinutes(drawioEditor.getAccessDurationMinutes(Mode.EDIT))));
			access = docEditorService.updatetExpiresAt(access, expiresAt);
		}
	}
	
	private void updateSvgPreview(byte[] content) {
		log.debug("Update draw.io svg preview. File: " + vfsLeaf.getRelPath());
		if (content.length > 0) {
			try (ByteArrayInputStream contentStream = new ByteArrayInputStream(content)) {
				VFSManager.copyContent(contentStream, svgPreviewLeaf, getIdentity());
			} catch (Exception e) {
				log.warn("Update draw.io svg preview failed. File: " + svgPreviewLeaf.getRelPath());
				log.error("", e);
			}
		}
	}
	
	private void updatePngPreview(byte[] content) {
		log.debug("Update draw.io png preview. File: " + vfsLeaf.getRelPath());
		if (content.length > 0) {
			try (ByteArrayInputStream contentStream = new ByteArrayInputStream(content)) {
				VFSManager.copyContent(contentStream, pngPreviewLeaf, getIdentity());
			} catch (Exception e) {
				log.warn("Update draw.io png preview failed. File: " + pngPreviewLeaf.getRelPath());
				log.error("", e);
			}
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
