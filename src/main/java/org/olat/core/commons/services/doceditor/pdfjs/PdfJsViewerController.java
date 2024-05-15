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
package org.olat.core.commons.services.doceditor.pdfjs;

import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jul 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PdfJsViewerController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(PdfJsViewerController.class);
	
	private final Access access;
	
	@Autowired
	private DocEditorService docEditorService;

	public PdfJsViewerController(UserRequest ureq, WindowControl wControl, DocEditorConfigs configs, Access access) {
		super(ureq, wControl);
		this.access = access;
		
		VelocityContainer mainVC = createVelocityContainer("view");
		putInitialPanel(mainVC);
		
		VFSLeaf vfsLeaf = configs.getVfsLeaf();
		
		StringOutput sb = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(sb, "js/pdfjs/web/viewer.html");
		
		// https://mozilla.github.io/pdf.js/examples/
		// https://github.com/mozilla/pdf.js/wiki/Frequently-Asked-Questions#can-i-specify-a-different-pdf-in-the-default-viewer
		boolean base64Pdf = false;
		if (base64Pdf) {
			// Empty url to load the PDF file later.
			sb.append("?file=");
			byte[] pdfBytes = load(vfsLeaf);
			if (pdfBytes != null) {
				String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
				mainVC.contextPut("pdfBase64", pdfBase64);
			}
		} else {
			VFSMediaMapper mapper = new VFSMediaMapper(vfsLeaf);
			String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(vfsLeaf.getRelPath() + vfsLeaf.getLastModified()));
			String pdfUrl = registerCacheableMapper(ureq, mapperId, mapper);
			sb.append("?file=").append(pdfUrl);
		}
		
		String viewerUrl = sb.toString();
		mainVC.contextPut("url", viewerUrl);
		
		String iframeId = "pdfjs" + CodeHelper.getRAMUniqueID();
		mainVC.contextPut("iframeId", iframeId);
		
		mainVC.contextPut("modeCss", access.getMode() == Mode.EMBEDDED? "o_pdfjs_embedded": "o_pdfjs_full");
		mainVC.contextPut("hideDownload", Boolean.valueOf(!access.isDownload()));
	}

	
	private byte[] load(VFSLeaf vfsLeaf) {
		try {
			return FileUtils.loadAsBytes(vfsLeaf.getInputStream());
		} catch (Exception e) {
			log.warn("Cannot load pdf file ", vfsLeaf.getRelPath());
			log.warn("", e);
		}
		return null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void doDispose() {
		deleteAccess();
		super.doDispose();
	}
	
	private void deleteAccess() {
		docEditorService.deleteAccess(access);
	}

}
