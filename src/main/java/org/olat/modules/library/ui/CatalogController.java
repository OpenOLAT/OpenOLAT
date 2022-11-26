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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsCountChangedEvent;
import org.olat.core.commons.services.commentAndRating.ui.UserRatingChangedEvent;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.model.CatalogItem;
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
public class CatalogController extends FormBasicController implements GenericEventListener {
	private static final String COMMAND_OPEN_FOLDER = "cmd.openFolder";

	private FormLink thumbnailSwitch;
	
	private DisplayOrDownloadComponent autoDownloadComp;

	private final boolean guestOnly;
	private String basePath = "";
	private final String mapperBaseURL;
	private String thumbnailMapperBaseURL;
	private boolean linkToFolder;
	private boolean showFolderInfo;
	private String title;
	private OLATResourceable libraryOres;
	private List<CatalogItem> catalogItems;
	
	private UserCommentsAndRatingsController commentsController;
	private CloseableModalController commentsModalController;
	private SendCatalogItemByEMailController sendDocController;
	private CloseableModalController sendMailModalController;

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private LibraryManager libraryManager;
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
		super(ureq, control, "catalog", Util.createPackageTranslator(UserCommentsAndRatingsController.class, ureq.getLocale()));
		this.guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		this.mapperBaseURL = mapperBaseURL;
		this.thumbnailMapperBaseURL = thumbnailMapperBaseURL;
		this.linkToFolder = linkToFolder;
		this.libraryOres = libraryOres;
		this.showFolderInfo = showFolderInfo;
		this.title = title;

		initForm(ureq);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), libraryManager.getLibraryResourceable());
	}
	
	@Override
	protected void doDispose() {
		// Remove event listener
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, libraryManager.getLibraryResourceable());
        super.doDispose();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			Translator metaTranslator = Util.createPackageTranslator(MetaInfoController.class, getLocale());
			layoutCont.contextPut("metaTrans", metaTranslator);
			layoutCont.contextPut("mapperBaseURL", mapperBaseURL);
			layoutCont.contextPut("thumbnailMapperBaseURL", thumbnailMapperBaseURL);
			layoutCont.contextPut("linkToFolder", Boolean.valueOf(linkToFolder));
			if(title != null) {
				layoutCont.contextPut("customTitle", title);
			}
			layoutCont.contextPut("showFolderInfo", Boolean.valueOf(showFolderInfo));
			layoutCont.contextPut("thumbnails", Boolean.TRUE);
			layoutCont.contextPut("thumbnailHelper", new ThumbnailHelper(getTranslator(), thumbnailMapperBaseURL));
		}
		
		thumbnailSwitch = uifactory.addFormLink("thumbnails.on", formLayout);
		thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle o_on");
		thumbnailSwitch.setIconLeftCSS(null);
		thumbnailSwitch.setIconRightCSS("o_icon o_icon_toggle");
	}
	
	protected void updateRepositoryEntry(OLATResourceable ores) {
		this.libraryOres = ores;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(Event event) {
		if (event instanceof UserCommentsCountChangedEvent) {
			UserCommentsCountChangedEvent changedEvent = (UserCommentsCountChangedEvent)event;
			if(!changedEvent.isSentByMyself(this)) {
				processUserChangeEvent(changedEvent.getOresSubPath());
			}
		} else if (event instanceof UserRatingChangedEvent) {
			UserRatingChangedEvent changedEvent = (UserRatingChangedEvent) event;
			if(!changedEvent.isSentByMyself(this)) {
				processUserChangeEvent(changedEvent.getOresSubPath());
			}
		}
	}
	
	private void processUserChangeEvent(String oresSubPath) {
		String id = "comments_".concat(oresSubPath);
		FormItem commentsItem = flc.getFormComponent(id);
		if(commentsItem != null) {
			updateCatalogItem(oresSubPath);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == flc.getComponent()) {
			String command = event.getCommand();
			if (command != null && command.startsWith(COMMAND_OPEN_FOLDER)) {
				int index = Integer.parseInt(command.substring(COMMAND_OPEN_FOLDER.length()));
				if (index >= 0 && index < catalogItems.size()) {
					CatalogItem item = catalogItems.get(index);
					fireEvent(ureq, new OpenFolderEvent(command, item));
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == thumbnailSwitch) {
			toggleThumbnail();
		} else if(source instanceof RatingWithAverageFormItem && event instanceof RatingFormEvent
				&& ((RatingWithAverageFormItem)source).getUserObject() instanceof CatalogItem) {
			CatalogItem item = (CatalogItem)((RatingWithAverageFormItem)source).getUserObject();
			doRating(item, ((RatingFormEvent)event).getRating());
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("comments".equals(link.getCmd()) && link.getUserObject() instanceof CatalogItem) {
				displayCommentsController(ureq, (CatalogItem)link.getUserObject());
			} else if("mail".equals(link.getCmd()) && link.getUserObject() instanceof CatalogItem) {
				displaySendMailController(ureq, (CatalogItem)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == commentsModalController) {
			cleanUp();
		} else if (source == commentsController && event == Event.CANCELLED_EVENT) {
			commentsModalController.deactivate();
			CatalogItem item = (CatalogItem)commentsController.getUserObject();
			updateCatalogItem(item.getMetaInfo().getUuid());
			cleanUp();
		} else if (source == sendDocController) {
			sendMailModalController.deactivate();
			cleanUp();
		} else if (source == sendMailModalController) {
			cleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(sendDocController);
		removeAsListenerAndDispose(commentsController);
		removeAsListenerAndDispose(sendMailModalController);
		removeAsListenerAndDispose(commentsModalController);
		sendDocController = null;
		commentsController = null;
		sendMailModalController = null;
		commentsModalController = null;	
	}
	
	private void toggleThumbnail() {
		if(Boolean.TRUE.equals(flc.contextGet("thumbnails"))) {
			thumbnailSwitch.getComponent().setCustomDisplayText(translate("thumbnails.off"));
			thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle");
			thumbnailSwitch.setIconLeftCSS("o_icon o_icon_toggle");
			thumbnailSwitch.setIconRightCSS(null);
			flc.contextPut("thumbnails", Boolean.FALSE);
		} else {
			thumbnailSwitch.getComponent().setCustomDisplayText(translate("thumbnails.on"));
			thumbnailSwitch.setCustomEnabledLinkCSS("o_button_toggle o_on");
			thumbnailSwitch.setIconLeftCSS(null);
			thumbnailSwitch.setIconRightCSS("o_icon o_icon_toggle");
			flc.contextPut("thumbnails", Boolean.TRUE);
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
		commentsController.setUserObject(item);
		listenTo(commentsController);

		String title = translate("comment.title", item.getName());
		commentsModalController = new CloseableModalController(getWindowControl(), translate("close"), commentsController.getInitialComponent(), true, title);
		listenTo(commentsModalController);
		commentsModalController.activate();
	}
	
	protected void doRating(CatalogItem item, float rating) {
		userRatingsDao.updateRating(getIdentity(), libraryOres, item.getMetaInfo().getUuid(), Math.round(rating));
		dbInstance.commitAndCloseSession();
		updateCatalogItem(item.getMetaInfo().getUuid());
		UserRatingChangedEvent changedEvent = new UserRatingChangedEvent(this, item.getMetaInfo().getUuid());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedEvent, libraryOres);
	}

	/**
	 * Displays the container metadata and all its leafs.
	 * 
	 * @param container The container to display
	 * @param fileName the name of a file that is in the given directory. If not
	 *          NULL, a download of this will will be triggered automatically
	 * @param locale the users locale
	 */
	public void display(VFSContainer container, String fileName) {
		VFSMetadata folderInfo = vfsRepositoryService.getMetadataFor(container);
		flc.contextPut("folderInfo", folderInfo);
		String whenTheFolderWasLastModified = DateFormat.getDateInstance(DateFormat.FULL, getLocale()).format(folderInfo.getLastModified());
		flc.contextPut("whenTheFolderWasLastModified", whenTheFolderWasLastModified);
		
		catalogItems = libraryManager.getCatalogItems(folderInfo, getIdentity());
		Map<String,CatalogItem> filenameToItems = catalogItems.stream()
				.collect(Collectors.toMap(CatalogItem::getFilename, m -> m, (u,v) -> u));

		for (VFSItem vfsItem : container.getItems(new VFSLeafButSystemFilter())) {
			if(vfsItem instanceof VFSLeaf) {
				String name = vfsItem.getName();
				CatalogItem item = filenameToItems.get(name);
				item.setSelected(fileName != null && fileName.equals(name));
				forgeSendMailLink(item);
				forgeComment(item);
				forgeRating(item);
			}
		}
		
		// Sort items
		Collections.sort(catalogItems, new CatalogItemComparator(getLocale()));
		flc.contextPut("items", catalogItems);
		
		// Trigger automatic file download if available
		if (fileName != null) {
			if (container.resolve(fileName) != null) {
				String url = mapperBaseURL + container.getRelPath() + "/" + fileName;
				if (autoDownloadComp == null) {
					// create on demand, only used when coming from the fulltext search
					autoDownloadComp = new DisplayOrDownloadComponent("autoDownloadComp", url);
					flc.put("autoDownloadComp", autoDownloadComp);
				} else {
					autoDownloadComp.triggerFileDownload(url);					
				}
			} else {
				showWarning("library.catalog.file.not.found");
			}
		}
	}

	public void display(List<CatalogItem> items) {
		catalogItems = items;
		flc.contextPut("items", catalogItems);
		flc.contextPut("folderInfo", "");

		Date lastModified = null;
		for (CatalogItem item : catalogItems) {
			if (lastModified == null || lastModified.compareTo(item.getMetaInfo().getLastModified()) > 0) {
				lastModified = item.getMetaInfo().getLastModified();
			}
			forgeSendMailLink(item);
			forgeComment(item);
			forgeRating(item);
		}

		String whenTheFolderWasLastModified = Formatter.getInstance(getLocale()).formatDateAndTimeLong(lastModified);
		flc.contextPut("whenTheFolderWasLastModified", whenTheFolderWasLastModified);
	}
	
	private void forgeRating(CatalogItem item) {
		String id = "ratings_".concat(item.getId());
		float averageRating = item.getRatings().getAverageOfRatings();

		if(guestOnly) {
			RatingFormItem ratingItem = uifactory.addRatingItem(id, null, averageRating, 5, false, flc);
			ratingItem.setShowRatingAsText(true);
			ratingItem.setUserObject(item);
		} else {
			int myRating = item.getRatings().getMyRating();
			int numOfRatings = (int)item.getRatings().getNumOfRatings();
			RatingWithAverageFormItem ratingItem = uifactory.addRatingItemWithAverage(id, null, myRating,
					averageRating, numOfRatings, 5, flc);
			ratingItem.setShowRatingAsText(true);
			ratingItem.setUserObject(item);
		}
	}
	
	private void forgeComment(CatalogItem item) {
		long numOfComments = item.getNumOfComments();
		String id = "comments_".concat(item.getId());
		String commentLabel = translate("comments.count", Long.toString(numOfComments));
		FormLink commentsLink = uifactory.addFormLink(id, "comments", commentLabel, null, flc, Link.NONTRANSLATED);
		String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
		commentsLink.setCustomEnabledLinkCSS("o_comments");
		commentsLink.setUserObject(item);
		commentsLink.setIconLeftCSS(css);
	}
	
	private void updateCatalogItem(String uuid) {
		try {
			CatalogItem updatedItem = libraryManager.getCatalogItemByUUID(uuid, getIdentity());
			
			int index = catalogItems.indexOf(updatedItem);
			if(index >= 0) {
				catalogItems.set(index, updatedItem);
			}
			
			FormItem commentsItem = flc.getFormComponent("comments_" + uuid);
			if(commentsItem instanceof FormLink) {
				FormLink commentsLink = (FormLink)commentsItem;
				String commentLabel = translate("comments.count", Long.toString(updatedItem.getNumOfComments()));
				commentsLink.setI18nKey(commentLabel);
				commentsLink.setUserObject(updatedItem);
			}

			FormItem ratingsItem = flc.getFormComponent("ratings_" + uuid);
			if(ratingsItem instanceof RatingWithAverageFormItem) {
				RatingWithAverageFormItem rwa = (RatingWithAverageFormItem)ratingsItem;
				rwa.setUserRating(updatedItem.getRatings().getMyRating());
				rwa.setAverageRating(updatedItem.getRatings().getAverageOfRatings());
			} else if(ratingsItem instanceof RatingFormItem) {
				RatingFormItem rfi = (RatingFormItem)ratingsItem;
				rfi.setCurrentRating(updatedItem.getRatings().getAverageOfRatings());
			}
		} catch (Exception e) {
			getLogger().error("Cannot update a catalog item: {}", uuid, e);
		}
	}
	
	private void forgeSendMailLink(CatalogItem item) {
		String id = "mail_".concat(item.getId());
		FormLink sendMail = uifactory.addFormLink(id, "mail", "library.catalog.send.mail", null, flc, Link.BUTTON);
		sendMail.setUserObject(item);
		sendMail.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
	}
	
	public void sort(Comparator<CatalogItem> comparator) {
		if(catalogItems != null && catalogItems.size() > 1) {
			Collections.sort(catalogItems, comparator);
			flc.contextPut("items", catalogItems);
		}
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
