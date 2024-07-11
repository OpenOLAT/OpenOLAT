/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.nodes.gta.ui.component.DownloadDocumentMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DirectoryController extends BasicController implements Activateable2 {
	
	private Link bulkReviewLink;
	private final DisplayOrDownloadComponent download;

	private final String zipName;
	private final String mapperUri;
	private final File documentsDir;
	private final VFSContainer documentsContainer;
	
	private CloseableModalController cmc;
	private SinglePageController previewCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	private Controller docEditorCtrl;

	@Autowired
	private UserManager userManager;

	@Autowired
	private DocEditorService docEditorService;
	
	public DirectoryController(UserRequest ureq, WindowControl wControl,
			File documentsDir, VFSContainer documentsContainer, String i18nDescription) {
		this(ureq, wControl, documentsDir, documentsContainer, i18nDescription, "bulk.submitted.documents", null);
	}
	
	public DirectoryController(UserRequest ureq, WindowControl wControl,
			File documentsDir, VFSContainer documentsContainer,
			String i18nDescription, String i18nBulkDownload, String zipName) {
		super(ureq, wControl, Util.createPackageTranslator(DocEditorController.class, ureq.getLocale()));
		this.zipName = zipName;
		this.documentsDir = documentsDir;
		this.documentsContainer = documentsContainer;

		Formatter format = Formatter.getInstance(ureq.getLocale());

		VelocityContainer mainVC = createVelocityContainer("documents_readonly");
		mainVC.contextPut("description", translate(i18nDescription));
		
		mapperUri = registerMapper(ureq, new DownloadDocumentMapper(documentsDir));
		download = new DisplayOrDownloadComponent("download", null);
		mainVC.put("download", download);
		
		if(StringHelper.containsNonWhitespace(i18nBulkDownload) && documentsContainer.getItems(new VFSSystemItemFilter()).size() > 1) {
			bulkReviewLink = LinkFactory.createCustomLink("bulk", "bulk", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
			bulkReviewLink.setIconLeftCSS("o_icon o_icon_download");
			bulkReviewLink.setCustomDisplayText(translate(i18nBulkDownload));
			bulkReviewLink.setTitle(zipName + ".zip");
		}
		
		List<DocumentInfos> linkNames = new ArrayList<>();
		File[] documents = documentsDir.listFiles(SystemFileFilter.FILES_ONLY);
		for(File document:documents) {
			VFSLeaf vfsLeaf = (VFSLeaf)documentsContainer.resolve(document.getName());

			// Explicit download link
			String downloadLinkId = "dow-" + CodeHelper.getRAMUniqueID();
			Link downoadLink = LinkFactory.createLink(downloadLinkId, "download", getTranslator(), mainVC, this, Link.NONTRANSLATED);
			downoadLink.setCustomDisplayText(translate("download"));
			downoadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
			downoadLink.setElementCssClass("btn btn-default btn-xs o_button_ghost");
			downoadLink.setAriaRole("button");
			downoadLink.setUserObject(vfsLeaf);
			
			// Link to preview the file (if possible)
			String previewLinkCompName = null;
			Link link = null;
			String linkId = "doc-" + CodeHelper.getRAMUniqueID();
			DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf,
					vfsLeaf.getMetaInfo(), true, DocEditorService.MODES_VIEW);
			if (editorInfo.isEditorAvailable()) {
				// Link to download the file in filename
				link = LinkFactory.createLink(linkId, "preview", getTranslator(), mainVC, this, Link.NONTRANSLATED);
				link.setUserObject(vfsLeaf);

				String previewLinkId = "prev-" + CodeHelper.getRAMUniqueID();
				Link previewLink = LinkFactory.createLink(previewLinkId, "preview", getTranslator(), mainVC, this, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
				previewLink.setCustomDisplayText(editorInfo.getModeButtonLabel(getTranslator()));
				previewLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
				previewLink.setAriaRole("button");
				previewLink.setGhost(true);
				previewLink.setUserObject(vfsLeaf);
				if (editorInfo.isNewWindow() && !document.getName().endsWith(".html")) {
					previewLink.setNewWindow(true, true);
					link.setNewWindow(true, true);
				}
				previewLinkCompName = previewLink.getComponentName();
			} else {
				link = LinkFactory.createLink(linkId, "download", getTranslator(), mainVC, this, Link.NONTRANSLATED);
				link.setUserObject(vfsLeaf);
			}
			link.setCustomDisplayText(StringHelper.escapeHtml(document.getName()));
			
			String createdBy = null;
			String lastModified = format.formatDateAndTime(new Date(vfsLeaf.getLastModified()));
			if(vfsLeaf.canMeta() == VFSStatus.YES) {
				VFSMetadata metaInfo = vfsLeaf.getMetaInfo();
				if(metaInfo != null && metaInfo.getFileInitializedBy() != null) {
					createdBy = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
				}
			}

			String cssClass = CSSHelper.createFiletypeIconCssClassFor(document.getName());
			linkNames.add(new DocumentInfos(link.getComponentName(), downoadLink.getComponentName(), previewLinkCompName, createdBy, lastModified, cssClass));
		}
		mainVC.contextPut("linkNames", linkNames);
		if(bulkReviewLink != null) {
			bulkReviewLink.setVisible(!linkNames.isEmpty());
		}

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
		File document = new File(documentsDir, path);
		if(document.exists()) {
			String url = mapperUri + "/" + document.getName();
			download.triggerFileDownload(url);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(bulkReviewLink == source) {
			doBulkDownload(ureq);
		} else if(source instanceof Link link) {
			if ("download".equalsIgnoreCase(link.getCommand())) {
				doDownload(ureq, (VFSLeaf) link.getUserObject());
			} else if ("preview".equalsIgnoreCase(link.getCommand())) {
				doOpenPreview(ureq, (VFSLeaf) link.getUserObject());
			}
		} 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source || source == docEditorCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(docEditorCtrl);
		cmc = null;
		previewCtrl = null;
		videoAudioPlayerController = null;
		docEditorCtrl = null;
	}

	private void doDownload(UserRequest ureq, VFSLeaf leaf) {
		VFSMediaResource vdr = new VFSMediaResource(leaf);
		vdr.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(vdr);
	}
	
	private void doBulkDownload(UserRequest ureq) {
		MediaResource mdr = new ZippedDirectoryMediaResource(zipName, documentsDir);
		ureq.getDispatchResult().setResultingMediaResource(mdr);
	}

	private void doOpenPreview(UserRequest ureq, VFSLeaf vfsLeaf) {
		if (vfsLeaf.getName().endsWith(".html")) {
			previewCtrl = new SinglePageController(ureq, getWindowControl(), documentsContainer, vfsLeaf.getName(), false);
			listenTo(previewCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(), true, vfsLeaf.getName());
			listenTo(cmc);
			cmc.activate();
		} else {
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(this.documentsContainer, vfsLeaf, vfsLeaf.getName(), Mode.VIEW, null);
			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW).getController();
			listenTo(docEditorCtrl);
		}
	}

	public record DocumentInfos(String linkName, String downloadLinkName, String previewLinkName, String createdBy,
								String lastModified, String cssClass) {
	}
}