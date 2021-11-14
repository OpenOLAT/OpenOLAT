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
package org.olat.modules.library.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.modules.library.ui.comparator.CatalogItemComparator;
import org.olat.modules.library.ui.event.OpenFolderEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the controller for the library catalog. It is basically an expandable
 * tree whose leafs are documents.<br>
 * Events fired:
 * <ul>
 * <li>OpenFolderEvent</li>
 * </ul>
 * <P>
 * Initial Date: Jun 16, 2009 <br>
 * 
 * @author gwassmann
 */
public class CatalogController extends BasicController {
	private static final String COMMAND_OPEN_FOLDER = "cmd.openFolder";

	private DisplayOrDownloadComponent autoDownloadComp;
	private final VelocityContainer contentVC;
	private final String mapperBaseURL;
	private String basePath = "";

	private List<CatalogItem> catalogItems;
	private boolean linkToFolder = false;
	private Link thumbnailSwitch;
	private UserCommentsAndRatingsController commentsController;
	private CloseableModalController commentsModalController;
	private SendCatalogItemByEMailController sendDocController;
	private CloseableModalController sendMailModalController;
	
	private OLATResourceable libraryOres;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	/**
	 * constructor
	 * 
	 * @param ureq
	 * @param control
	 */
	protected CatalogController(UserRequest ureq, WindowControl control, String mapperBaseURL, String thumbnailMapperBaseURL,
			OLATResourceable libraryOres) {
		this(ureq, control, mapperBaseURL, thumbnailMapperBaseURL, false, libraryOres);
	}

	protected CatalogController(UserRequest ureq, WindowControl control, String mapperBaseURL, String thumbnailMapperBaseURL,
			boolean linkToFolder, OLATResourceable libraryOres) {
		this(ureq, control, mapperBaseURL, thumbnailMapperBaseURL, linkToFolder, true, null, libraryOres);
	}
	
	protected CatalogController(UserRequest ureq, WindowControl control, String mapperBaseURL, String thumbnailMapperBaseURL,
			boolean linkToFolder, boolean showFolderInfo, String title, OLATResourceable libraryOres) {
		super(ureq, control);
		this.mapperBaseURL = mapperBaseURL;
		this.linkToFolder = linkToFolder;
		this.libraryOres = libraryOres;
		
		contentVC = createVelocityContainer("catalog");
		Translator metaTranslator = Util.createPackageTranslator(MetaInfoController.class, getLocale());
		contentVC.contextPut("metaTrans", metaTranslator);
		contentVC.contextPut("mapperBaseURL", mapperBaseURL);
		contentVC.contextPut("thumbnailMapperBaseURL", thumbnailMapperBaseURL);
		contentVC.contextPut("linkToFolder", Boolean.valueOf(linkToFolder));
		if(title != null) {
			contentVC.contextPut("customTitle", title);
		}
		contentVC.contextPut("showFolderInfo", Boolean.valueOf(showFolderInfo));
		thumbnailSwitch = LinkFactory.createLink("thumbnails.on", contentVC, this);
		thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle o_on");
		thumbnailSwitch.setIconLeftCSS(null);
		thumbnailSwitch.setIconRightCSS("o_icon o_icon_toggle");
		
		contentVC.contextPut("thumbnails", Boolean.TRUE);
		contentVC.contextPut("thumbnailHelper", new ThumbnailHelper(getTranslator(), thumbnailMapperBaseURL));
		
		putInitialPanel(contentVC);
	}
	
	protected void updateRepositoryEntry(OLATResourceable ores) {
		this.libraryOres = ores;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == contentVC) {
			String command = event.getCommand();
			if (command != null && command.startsWith(COMMAND_OPEN_FOLDER)) {
				int index = Integer.parseInt(command.substring(COMMAND_OPEN_FOLDER.length()));
				if (index >= 0 && index < catalogItems.size()) {
					CatalogItem item = catalogItems.get(index);
					fireEvent(ureq, new OpenFolderEvent(command, item));
				}
			}
		} else if (source == thumbnailSwitch) {
			if(Boolean.TRUE.equals(contentVC.getContext().get("thumbnails"))) {
				thumbnailSwitch.setCustomDisplayText(translate("thumbnails.off"));
				thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle");
				thumbnailSwitch.setIconLeftCSS("o_icon o_icon_toggle");
				thumbnailSwitch.setIconRightCSS(null);
				
				contentVC.contextPut("thumbnails", Boolean.FALSE);
			} else {
				thumbnailSwitch.setCustomDisplayText(translate("thumbnails.on"));
				thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle o_on");
				thumbnailSwitch.setIconLeftCSS(null);
				thumbnailSwitch.setIconRightCSS("o_icon o_icon_toggle");
				
				contentVC.contextPut("thumbnails", Boolean.TRUE);
			}
		} else if (source.getComponentName().startsWith("mail.")) {
			CatalogItem item = (CatalogItem)((Link)source).getUserObject();
			displaySendMailController(ureq, item);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == UserCommentsAndRatingsController.EVENT_COMMENT_LINK_CLICKED) {
			//popup comments
			UserCommentsAndRatingsController cController = (UserCommentsAndRatingsController)source;
			CatalogItem item = (CatalogItem)cController.getUserObject();
			if (item != null) {
				// item is null when triggered from within the modal dialog
				displayCommentsController(ureq, item);				
			}
		} else if (source == commentsModalController) {
			removeAsListenerAndDispose(commentsController);
			removeAsListenerAndDispose(commentsModalController);
			commentsController = null;
			commentsModalController = null;
		} else if (source instanceof UserCommentsAndRatingsController && event == Event.CANCELLED_EVENT) {
			commentsModalController.deactivate();
			removeAsListenerAndDispose(commentsController);
			removeAsListenerAndDispose(commentsModalController);
			commentsController = null;
			commentsModalController = null;			
		} else if (source == sendDocController) {
			sendMailModalController.deactivate();
			removeAsListenerAndDispose(sendDocController);
			removeAsListenerAndDispose(sendMailModalController);
			sendDocController = null;
			sendMailModalController = null;
		} else if (source == sendMailModalController) {
			removeAsListenerAndDispose(sendDocController);
			removeAsListenerAndDispose(sendMailModalController);
			sendDocController = null;
			sendMailModalController = null;
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void displaySendMailController(UserRequest ureq, CatalogItem item) {
		if (sendMailModalController != null) {
			removeAsListenerAndDispose(sendDocController);
			removeAsListenerAndDispose(sendMailModalController);
		}
		
		sendDocController = new SendCatalogItemByEMailController(ureq, getWindowControl(), item);
		listenTo(sendDocController);

		String title = translate("library.catalog.send.mail");
		sendMailModalController = new CloseableModalController(getWindowControl(), translate("close"),
				sendDocController.getInitialComponent(), true, title);
		listenTo(sendMailModalController);
		sendMailModalController.activate();
	}
	
	/**
	 * Displays the upload controller.
	 * 
	 * @param ureq The user request.
	 */
	private void displayCommentsController(UserRequest ureq, CatalogItem item) {
		if (commentsModalController != null) {
			removeAsListenerAndDispose(commentsController);
			removeAsListenerAndDispose(commentsModalController);
		}

		Roles roles = ureq.getUserSession().getRoles();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), roles.isAdministrator(), roles.isGuestOnly());
		commentsController =
				new UserCommentsAndRatingsController(ureq, getWindowControl(), libraryOres, item.getId(), secCallback, null, true, true, true);
		commentsController.expandComments(ureq);
		listenTo(commentsController);

		commentsModalController = new CloseableModalController(getWindowControl(), translate("close"), commentsController.getInitialComponent());
		listenTo(commentsModalController);
		commentsModalController.activate();
	}

	/**
	 * Displays the container metadata and all its leafs.
	 * 
	 * @param container The container to display
	 * @param fileName the name of a file that is in the given directory. If not
	 *          NULL, a download of this will will be triggered automatically
	 * @param locale the users locale
	 */
	public void display(VFSContainer container, String fileName, UserRequest ureq) {
		VFSMetadata folderInfo = vfsRepositoryService.getMetadataFor(container);
		contentVC.contextPut("folderInfo", folderInfo);
		String whenTheFolderWasLastModified = DateFormat.getDateInstance(DateFormat.FULL, getLocale()).format(folderInfo.getLastModified());
		contentVC.contextPut("whenTheFolderWasLastModified", whenTheFolderWasLastModified);
		
		List<VFSMetadata> metadata = vfsRepositoryService.getChildren(folderInfo);
		Map<String,VFSMetadata> filenameToMetadata = metadata.stream()
				.collect(Collectors.toMap(VFSMetadata::getFilename, m -> m, (u,v) -> u));
		
		catalogItems = new ArrayList<>();
		int count = 0;
		for (VFSItem vfsItem : container.getItems(new VFSLeafButSystemFilter())) {
			if(vfsItem instanceof VFSLeaf) {
				String name = vfsItem.getName();
				VFSMetadata meta = filenameToMetadata.get(name);
				boolean thumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(vfsItem, meta);
				CatalogItem item = new CatalogItem((VFSLeaf)vfsItem, meta, thumbnailAvailable, getLocale());
				item.setSelected(fileName != null && fileName.equals(name));
				catalogItems.add(item);
				addSendMailLink(item, count++);
				addCommentsController(ureq, item);
			}
		}
		// Sort items
		Collections.sort(catalogItems, new CatalogItemComparator(getLocale()));
		contentVC.contextPut("items", catalogItems);
		// Trigger automatic file download if available
		if (fileName != null) {
			if (container.resolve(fileName) != null) {
				String url = mapperBaseURL + container.getRelPath() + "/" + fileName;
				if (autoDownloadComp == null) {
					// create on demand, only used when coming from the fulltext search
					autoDownloadComp = new DisplayOrDownloadComponent("autoDownloadComp", url);
					contentVC.put("autoDownloadComp", autoDownloadComp);
				} else {
					autoDownloadComp.triggerFileDownload(url);					
				}
			} else {
				showWarning("library.catalog.file.not.found");
			}
		}
	}

	public void display(List<CatalogItem> items, UserRequest ureq) {
		catalogItems = items;
		contentVC.contextPut("items", catalogItems);
		contentVC.contextPut("folderInfo", "");

		int count = 0;
		Date lastModified = null;
		for (CatalogItem item : catalogItems) {
			if (lastModified == null || lastModified.compareTo(item.getMetaInfo().getLastModified()) > 0) {
				lastModified = item.getMetaInfo().getLastModified();
			}
			addSendMailLink(item, count++);
			addCommentsController(ureq, item);
		}

		String whenTheFolderWasLastModified = Formatter.getInstance(getLocale()).formatDateAndTimeLong(lastModified);
		contentVC.contextPut("whenTheFolderWasLastModified", whenTheFolderWasLastModified);
	}
	
	public void sort(Comparator<CatalogItem> comparator) {
		if(catalogItems != null && catalogItems.size() > 1) {
			Collections.sort(catalogItems, comparator);
			contentVC.contextPut("items", catalogItems);
		}
	}
	
	private void addSendMailLink(CatalogItem item, int count) {
		Link sendMail = LinkFactory.createButton("mail." + count, contentVC, this);
		sendMail.setCustomDisplayText(translate("library.catalog.send.mail"));
		sendMail.setUserObject(item);
		sendMail.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		item.setSendMailLink(sendMail);
	}
	
	private void addCommentsController(UserRequest ureq, CatalogItem item) {
		removeAsListenerAndDispose(item.getCommentsAndRatingCtr());	

		Roles roles = ureq.getUserSession().getRoles();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), roles.isAdministrator(), roles.isGuestOnly());
		UserCommentsAndRatingsController commentsAndRatingCtr =
				new UserCommentsAndRatingsController(ureq, getWindowControl(), libraryOres, item.getId(), secCallback, null, true, true, false);
		commentsAndRatingCtr.setUserObject(item);
		listenTo(commentsAndRatingCtr);
		contentVC.put("comments_" + item.getId(), commentsAndRatingCtr.getInitialComponent());
		
		item.setCommentsAndRatingCtr(commentsAndRatingCtr);
	}

	public String getBasePath() {
		return basePath;
	}

	public boolean isLinkToFolder() {
		return linkToFolder;
	}

	public void setLinkToFolder(boolean linkToFolder) {
		this.linkToFolder = linkToFolder;
	}
}
