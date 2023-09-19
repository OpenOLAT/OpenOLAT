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

import java.util.List;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
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
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.course.nodes.document.DocumentSecurityCallback;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentRunController extends BasicController {
	
	private static final List<Mode> MODE_EMBEDDED = List.of(Mode.EMBEDDED);
	
	private Link downloadButton;
	private Link openButton;
	private Link fileLink;

	private Controller docEditorCtrl;
	
	private final DocumentSecurityCallback secCallback;
	private final VFSLeaf vfsLeaf;

	@Autowired
	private DocEditorService docEditorService;
	
	public DocumentRunController(UserRequest ureq, WindowControl wControl, DocumentCourseNode courseNode,
		DocumentSecurityCallback secCallback, VFSContainer courseFolderCont, String docEditorCss) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
		this.secCallback = secCallback;

		VelocityContainer mainVC = createVelocityContainer("run");
		
		vfsLeaf = courseNode.getDocumentSource(courseFolderCont).getVfsLeaf();

		RepositoryEntry documentEntry = courseNode.getReferencedRepositoryEntry();
		if (documentEntry != null
				&& (RepositoryEntryStatusEnum.deleted == documentEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == documentEntry.getEntryStatus())) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_filetype_file")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey("error.document.deleted.node")
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			putInitialPanel(emptyStateCmp);
			return;
		} else {
			putInitialPanel(mainVC);
		}

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
			
			VFSMetadata metaInfo = vfsLeaf.getMetaInfo();
			if (metaInfo != null) {
				String title = metaInfo.getTitle();
				mainVC.contextPut("title", title);
				
			}
			
			String height = courseNode.getModuleConfiguration().getStringValue(DocumentCourseNode.CONFIG_KEY_HEIGHT, DocumentCourseNode.CONFIG_HEIGHT_AUTO);
			if (!DocumentCourseNode.CONFIG_HEIGHT_AUTO.equals(height)) {
				mainVC.contextPut("height", height);
			}
			
			String extension = FileUtils.getFileSuffix(filename);
			if ("png".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "gif".equalsIgnoreCase(extension)) {
				String mediaUrl = registerMapper(ureq, new VFSMediaMapper(vfsLeaf));
				mainVC.contextPut("image", filename);
				mainVC.contextPut("mediaUrl", mediaUrl);
			} else if (SoundFileResource.validate(filename)) {
				DeliveryOptions options = new DeliveryOptions();
				String optionsHight = DocumentCourseNode.CONFIG_HEIGHT_AUTO.equals(height)? "40": height;
				options.setHeight(optionsHight);
				IFrameDisplayController idc = new IFrameDisplayController(ureq, getWindowControl(), vfsLeaf.getParentContainer(), null, options);
				listenTo(idc);	
				idc.setCurrentURI(filename);
				mainVC.put("audio", idc.getInitialComponent());
			} else if (hasEmbeddedView(ureq, metaInfo)) {
				DocEditorConfigs docEditorConfigs = DocEditorConfigs.builder()
						.withMode(Mode.EMBEDDED)
						.withDownloadEnabled(secCallback.canDownload())
						.addConfig(new DocEditorConfig(docEditorCss))
						.build(vfsLeaf);
				Access access = docEditorService.createAccess(getIdentity(), ureq.getUserSession().getRoles(), docEditorConfigs);
				docEditorCtrl = new DocEditorController(ureq, wControl, access, docEditorConfigs);
				listenTo(docEditorCtrl);
				mainVC.put("content", docEditorCtrl.getInitialComponent());
				
				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
						ureq.getUserSession().getRoles(), vfsLeaf, metaInfo, true, DocEditorService.modesEditView(secCallback.canEdit()));
				if (editorInfo.isEditorAvailable() && editorInfo.isNewWindow()) {
					openButton = LinkFactory.createCustomLink("run.open", "open", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
					openButton.setCustomDisplayText(editorInfo.getModeButtonLabel(getTranslator()));
					openButton.setIconLeftCSS("o_icon o_icon-lg " + editorInfo.getModeIcon());
					openButton.setNewWindow(true, true);
				}
			} else {
				String fileCssClass = CSSHelper.createFiletypeIconCssClassFor(vfsLeaf.getName());
				fileLink = LinkFactory.createCustomLink("run.file", "run.file", "", Link.LINK_CUSTOM_CSS, mainVC, this);
				fileLink.setCustomDisplayText(filename);
				fileLink.setIconLeftCSS("o_icon " + fileCssClass);
			}
		}
	}
	
	private boolean hasEmbeddedView(UserRequest ureq, VFSMetadata metadata) {
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(),
				ureq.getUserSession().getRoles(), vfsLeaf, metadata, true, MODE_EMBEDDED);
		return editorInfo.isEditorAvailable();
	}
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadButton) {
			doDownload(ureq);
		} else if (source == openButton) {
			doOpen(ureq);
		} else if (source == fileLink) {
			doDownload(ureq);
		}
	}
	
	private void doDownload(UserRequest ureq) {
		VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
		resource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpen(UserRequest ureq) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(Mode.EDIT)
				.withDownloadEnabled(secCallback.canDownload())
				.build(vfsLeaf);
		docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_EDIT_VIEW);
	}
}
