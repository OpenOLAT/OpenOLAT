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
import java.io.InputStream;
import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorConfigs.Config;
import org.olat.core.commons.services.doceditor.DocEditorService;
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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioEditorController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(DrawioEditorController.class);
	
	private static final String PNG_BASE64_PREFIX = "data:image/png;base64,";
	private static final String SVG_BASE64_PREFIX = "data:image/svg+xml;base64,";
	
	private final Access access;
	private final VFSLeaf vfsLeaf;
	private VFSLeaf svgPreviewLeaf;
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
		Config config = configs.getConfig(DrawioEditorConfig.TYPE);
		if (config instanceof DrawioEditorConfig drawioEditorConfig) {
			svgPreviewLeaf = drawioEditorConfig.getSvgPreviewLeaf();
		}
		
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
		
		// https://www.drawio.com/doc/faq/embed-mode
		// https://www.drawio.com/doc/faq/supported-url-parameters
		String viewerUrl = drawioModule.getEditorUrl() + "?spin=1&embed=1&proto=json&saveAndExit=0&noSaveBtn=1&noExitBtn=1";
		
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
		
		boolean isPng = "png".equalsIgnoreCase(suffix);
		boolean isSvg = "svg".equalsIgnoreCase(suffix);
		
		String xml;
		if (isPng) {
			xml = Base64.getEncoder().encodeToString(loadPng(vfsLeaf.getInputStream()));
			xml = PNG_BASE64_PREFIX + xml;
		} else if (isSvg) {
			xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
			xml = Base64.getEncoder().encodeToString(xml.getBytes());
			xml = SVG_BASE64_PREFIX + xml;
		} else {
			xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
		}
		mainVC.contextPut("xml", StringHelper.escapeJavaScriptParam(xml));
		mainVC.contextPut("filename", vfsLeaf.getName());
		mainVC.contextPut("png", isPng);
		mainVC.contextPut("svg", isSvg);
		mainVC.contextPut("svgPreview", svgPreviewLeaf != null);
	}
	
	private byte[] loadPng(InputStream inputStream) {
		try {
			return FileUtils.loadAsBytes(inputStream);
		} catch (Exception e) {
			log.warn("Cannot load png file ", vfsLeaf.getRelPath());
			log.warn("", e);
		}
		return new byte[0];
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(event == Window.CLOSE_WINDOW) {
			deleteAccess();
		} else if ("saveXml".equals(event.getCommand())) {
			String xml = ureq.getParameter("xml");
			drawioService.updateContent(access, getIdentity(), xml.getBytes());
		} else if ("saveXmlPng".equals(event.getCommand())) {
			String png = ureq.getParameter("xmlpng");
			if (png.startsWith(PNG_BASE64_PREFIX)) {
				png = png.substring(PNG_BASE64_PREFIX.length());
				drawioService.updateContent(access, getIdentity(), Base64.getDecoder().decode(png));
			}
		} else if ("saveXmlSvg".equals(event.getCommand())) {
			String svg = ureq.getParameter("xmlsvg");
			if (svg.startsWith(SVG_BASE64_PREFIX)) {
				svg = svg.substring(SVG_BASE64_PREFIX.length());
				byte[] content = Base64.getDecoder().decode(svg);
				if (svgPreviewLeaf != null) {
					updateSvgPreview(content);
				} else {
					drawioService.updateContent(access, getIdentity(), content);
				}
			}
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
