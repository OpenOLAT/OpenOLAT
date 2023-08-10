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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.apache.logging.log4j.Logger;
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
import org.olat.core.logging.Tracing;
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
	
	private static final Logger log = Tracing.createLoggerFor(DrawioEditorController.class);
	
	private static final String PNG_BASE64_PREFIX = "data:image/png;base64,";
	
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
		
		String xml;
		if (isPng) {
			xml = Base64.getEncoder().encodeToString(loadPng(vfsLeaf.getInputStream()));
			xml = PNG_BASE64_PREFIX + xml;
		} else {
			xml = FileUtils.load(vfsLeaf.getInputStream(), "utf-8");
		}
		System.out.println(xml);
		mainVC.contextPut("xml", StringHelper.escapeJavaScriptParam(xml));
		mainVC.contextPut("filename", vfsLeaf.getName());
		mainVC.contextPut("png", isPng);
	}
	
	private byte[] loadPng(InputStream inputStream) {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			FileUtils.copy(inputStream, buffer);
			return buffer.toByteArray();
		} catch (IOException e) {
			log.warn("Cannot load png file ", vfsLeaf.getRelPath());
			log.warn("", e);
		} finally {
			FileUtils.closeSafely(inputStream);
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
				System.out.println(png);
				drawioService.updateContent(access, getIdentity(), Base64.getDecoder().decode(png));
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
