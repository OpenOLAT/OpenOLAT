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
package org.olat.course.nodes.document.ui;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.nodes.document.DocumentSecurityCallback;
import org.olat.fileresource.types.SoundFileResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentRunController extends BasicController {
	
	private Link downloadButton;
	private Link editButton;
	private Link viewButton;
	private Link fileLink;

	private Controller docEditorCtrl;
	
	private final DocumentSecurityCallback secCallback;
	private final VFSLeaf vfsLeaf;

	@Autowired
	private DocEditorService docEditorService;
	
	public DocumentRunController(UserRequest ureq, WindowControl wControl, DocumentCourseNode courseNode,
		DocumentSecurityCallback secCallback, VFSContainer courseFolderCont, String docEditorCss) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		
		VelocityContainer mainVC = createVelocityContainer("run");
		putInitialPanel(mainVC);
		
		vfsLeaf = courseNode.getDocumentSource(courseFolderCont).getVfsLeaf();
		if (vfsLeaf == null) {
			String title = translate("run.no.document.title");
			String text = translate("run.no.document.text");
			MessageController infoMessage = MessageUIFactory.createInfoMessage(ureq, wControl, title, text);
			mainVC.put("content", infoMessage.getInitialComponent());
		} else {
			if (secCallback.canDownload()) {
				downloadButton = LinkFactory.createButton("run.download", mainVC, this);
				downloadButton.setIconLeftCSS("o_icon o_icon-lg o_icon_download");
			}
					
			String filename = vfsLeaf.getName();
			mainVC.contextPut("filename", filename);
			String lowerFilename = filename.toLowerCase();
			String cssClass = CSSHelper.createFiletypeIconCssClassFor(lowerFilename);
			mainVC.contextPut("cssClass", cssClass);
			
			String extension = FileUtils.getFileSuffix(filename);
			if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension) || "gif".equals(extension)) {
				String mediaUrl = registerMapper(ureq, new VFSMediaMapper(vfsLeaf));
				mainVC.contextPut("image", filename);
				mainVC.contextPut("mediaUrl", mediaUrl);
			} else if (SoundFileResource.validate(filename)) {
				DeliveryOptions options = new DeliveryOptions();
				options.setHeight("40");
				IFrameDisplayController idc = new IFrameDisplayController(ureq, getWindowControl(), vfsLeaf.getParentContainer(), null, options);
				listenTo(idc);	
				idc.setCurrentURI(filename);
				mainVC.put("audio", idc.getInitialComponent());
			} else if (hasEditor(ureq, extension, Mode.EMBEDDED)) {
				DocEditorConfigs docEditorConfigs = DocEditorConfigs.builder()
						.withMode(Mode.EMBEDDED)
						.withDownloadEnabled(secCallback.canDownload())
						.addConfig(new DocEditorConfig(docEditorCss))
						.build(vfsLeaf);
				Access access = docEditorService.createAccess(getIdentity(), ureq.getUserSession().getRoles(), docEditorConfigs);
				docEditorCtrl = new DocEditorController(ureq, wControl, access, docEditorConfigs);
				listenTo(docEditorCtrl);
				mainVC.put("content", docEditorCtrl.getInitialComponent());
				
				if (secCallback.canEdit() && hasEditor(ureq, extension, Mode.EDIT)) {
					editButton = LinkFactory.createButton("run.edit", mainVC, this);
					editButton.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
					editButton.setNewWindow(true, true);
				} else if (hasEditor(ureq, extension, Mode.VIEW)) {
					viewButton = LinkFactory.createButton("run.view", mainVC, this);
					viewButton.setIconLeftCSS("o_icon o_icon-lg o_icon_preview");
					viewButton.setNewWindow(true, true);
				}
			} else {
				String fileCssClass = CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName());
				fileLink = LinkFactory.createCustomLink("run.file", "run.file", "", Link.LINK_CUSTOM_CSS, mainVC, this);
				fileLink.setCustomDisplayText(filename);
				fileLink.setIconLeftCSS("o_icon " + fileCssClass);
			}
		}
	}
	
	private boolean hasEditor(UserRequest ureq, String extension, Mode mode) {
		return docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), extension, mode, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadButton) {
			doDownload(ureq);
		} else if (source == editButton) {
			doOpen(ureq, Mode.EDIT);
		} else if (source == viewButton) {
			doOpen(ureq, Mode.VIEW);
		} else if (source == fileLink) {
			doDownload(ureq);
		}
	}
	
	private void doDownload(UserRequest ureq) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpen(UserRequest ureq, Mode mode) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(mode)
				.withDownloadEnabled(secCallback.canDownload())
				.build(vfsLeaf);
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	@Override
	protected void doDispose() {
		//
	}

}
