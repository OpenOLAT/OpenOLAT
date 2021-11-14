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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.vfs.VFSMetadata;
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
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.nodes.gta.ui.component.DownloadDocumentMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DirectoryController extends BasicController implements Activateable2 {
	
	private Link bulkReviewLink;
	private final VelocityContainer mainVC;
	private final DisplayOrDownloadComponent download;
	
	
	private final String zipName;
	private final String mapperUri;
	private final File documentsDir;
	private final VFSContainer documentsContainer;
	
	private CloseableModalController cmc;
	private SinglePageController previewCtrl;
	
	@Autowired
	private UserManager userManager;
	
	private final Formatter format;
	
	public DirectoryController(UserRequest ureq, WindowControl wControl,
			File documentsDir, VFSContainer documentsContainer, String i18nDescription) {
		this(ureq, wControl, documentsDir, documentsContainer, i18nDescription, null, null);
	}
	
	public DirectoryController(UserRequest ureq, WindowControl wControl,
			File documentsDir, VFSContainer documentsContainer,
			String i18nDescription, String i18nBulkDownload, String zipName) {
		super(ureq, wControl);
		this.zipName = zipName;
		this.documentsDir = documentsDir;
		this.documentsContainer = documentsContainer;
		
		format = Formatter.getInstance(ureq.getLocale());
		
		mainVC = createVelocityContainer("documents_readonly");
		mainVC.contextPut("description", translate(i18nDescription));
		
		mapperUri = registerMapper(ureq, new DownloadDocumentMapper(documentsDir));
		download = new DisplayOrDownloadComponent("download", null);
		mainVC.put("download", download);
		
		if(StringHelper.containsNonWhitespace(i18nBulkDownload)) {
			bulkReviewLink = LinkFactory.createCustomLink("bulk", "bulk", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
			bulkReviewLink.setIconLeftCSS("o_icon o_icon_download");
			bulkReviewLink.setCustomDisplayText(translate(i18nBulkDownload));
			bulkReviewLink.setTitle(zipName + ".zip");
		}
		
		List<DocumentInfos> linkNames = new ArrayList<>();
		File[] documents = documentsDir.listFiles(SystemFileFilter.FILES_ONLY);
		for(File document:documents) {
			String linkId = "doc-" + CodeHelper.getRAMUniqueID();
			Link link = LinkFactory.createLink(linkId, "download", getTranslator(), mainVC, this, Link.NONTRANSLATED);
			link.setCustomDisplayText(StringHelper.escapeHtml(document.getName()));
			String cssClass = CSSHelper.createFiletypeIconCssClassFor(document.getName());
			link.setIconLeftCSS("o_icon o_icon-fw " + cssClass);
			link.setUserObject(document);
			if(!document.getName().endsWith(".html")) {
				link.setTarget("_blank");
			}
			
			String createdBy = null;
			String lastModified = null;
			if(documentsContainer != null) {
				VFSItem item = documentsContainer.resolve(document.getName());
				lastModified = format.formatDateAndTime(new Date(item.getLastModified()));
				if(item.canMeta() == VFSConstants.YES) {
					VFSMetadata metaInfo = item.getMetaInfo();
					if(metaInfo != null && metaInfo.getFileInitializedBy() != null) {
						createdBy = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
					}
				}
			}

			linkNames.add(new DocumentInfos(link.getComponentName(), createdBy, lastModified));
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
			doBulkdownload(ureq);
		} else if(source instanceof Link && "download".equals(((Link)source).getCommand())) {
			Link downloadLink = (Link)source;
			doDownload(ureq, (File)downloadLink.getUserObject());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		cmc = null;
		previewCtrl = null;
	}

	private void doDownload(UserRequest ureq, File file) {
		if(file.getName().endsWith(".html")) {
			previewCtrl = new SinglePageController(ureq, getWindowControl(), documentsContainer, file.getName(), false);
			listenTo(previewCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(), true, file.getName());
			listenTo(cmc);
			cmc.activate();
		} else {
			MediaResource mdr = new FileMediaResource(file, true);
			ureq.getDispatchResult().setResultingMediaResource(mdr);
		}
	}
	
	private void doBulkdownload(UserRequest ureq) {
		MediaResource mdr = new ZippedDirectoryMediaResource(zipName, documentsDir);
		ureq.getDispatchResult().setResultingMediaResource(mdr);
	}
	
	public static final class DocumentInfos {
		
		private final String linkName;
		private final String createdBy;
		private final String lastModified;
		
		public DocumentInfos(String linkName, String createdBy, String lastModified) {
			this.linkName = linkName;
			this.createdBy = createdBy;
			this.lastModified = lastModified;
		}

		public String getLinkName() {
			return linkName;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public String getLastModified() {
			return lastModified;
		}
	}
}