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
package org.olat.modules.webFeed.ui;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedChangedEvent;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the main feed layout controller. It handles everything from adding
 * episodes to changing title and description.
 * 
 * <P>
 * Initial Date: Feb 5, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedMainController extends BasicController implements Activateable2, GenericEventListener {

	private Feed feed;
	private Link editFeedButton;
	private CloseableModalController cmc;
	private FeedFormController feedFormCtr;
	private VelocityContainer vcMain, vcInfo, vcRightCol;
	private ItemsController itemsCtr;
	private LockResult lock;
	private FeedViewHelper helper;
	private DisplayFeedUrlController displayUrlCtr;
	private FeedUIFactory uiFactory;
	private FeedSecurityCallback callback;
	private ContextualSubscriptionController cSubscriptionCtrl;

	// needed for comparison
	private String oldFeedUrl;
	private SubscriptionContext subsContext;
	private OLATResourceable ores;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private FeedManager feedManager;
	
	/**
	 * Constructor for learning resource (not course nodes)
	 * 
	 * @param ores
	 * @param ureq
	 * @param wControl
	 * @param previewMode Indicates that the content will only be displayed in
	 *          preview and no editing functionality is enabled.
	 */
	public FeedMainController(OLATResourceable ores, UserRequest ureq, WindowControl wControl, FeedUIFactory uiFactory,
			FeedSecurityCallback callback) {
		this(ores, ureq, wControl, null, null, uiFactory, callback, null);
	}

	/**
	 * Constructor for course node
	 * 
	 * @param ores
	 * @param ureq
	 * @param wControl
	 * @param previewMode Indicates that the content will only be displayed in
	 *          preview and no editing functionality is enabled.
	 */
	public FeedMainController(OLATResourceable ores, UserRequest ureq, WindowControl wControl, Long courseId, String nodeId,
			FeedUIFactory uiFactory, FeedSecurityCallback callback, FeedItemDisplayConfig displayConfig) {
		super(ureq, wControl);
		this.uiFactory = uiFactory;
		this.callback = callback;
		this.ores = ores;
		
		subsContext = callback.getSubscriptionContext();
				
		setTranslator(uiFactory.getTranslator());
		feed = feedManager.loadFeed(ores);
		if(feed == null) {
			vcMain = createVelocityContainer("feed_error");
			vcMain.contextPut("errorMessage", translate("feed.error"));
			putInitialPanel(vcMain);
		} else {
			helper = new FeedViewHelper(feed, getIdentity(), ureq.getUserSession().getRoles(), uiFactory.getTranslator(), courseId, nodeId);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), feed);
			display(ureq, wControl, displayConfig);
			// do logging
			ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_READ, getClass(), LoggingResourceable.wrap(feed));
		}
	}

	@Override
	protected void doDispose() {
		feedManager.releaseLock(lock);
		if(feed != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, feed);
		}
        super.doDispose();
	}

	/**
	 * Sets up the velocity container for displaying the view
	 * 
	 * @param ores
	 * @param ureq
	 * @param wControl
	 * @param previewMode
	 * @param isCourseNode
	 */
	private void display(UserRequest ureq, WindowControl wControl, FeedItemDisplayConfig displayConfig) {
		vcMain = createVelocityContainer("feed_main");

		vcInfo = uiFactory.createInfoVelocityContainer(this);
		vcInfo.contextPut("feed", feed);
		vcInfo.contextPut("helper", helper);
		vcInfo.contextPut("suppressCache", "");
		
		if (subsContext != null) {
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData data = new PublisherData(ores.getResourceableTypeName(), ores.getResourceableId().toString(), businessPath);
			cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
			listenTo(cSubscriptionCtrl);
			vcInfo.put("subscription", cSubscriptionCtrl.getInitialComponent());
		}
		
		vcRightCol = uiFactory.createRightColumnVelocityContainer(this);
		vcMain.put("rightColumn", vcRightCol);

		if (callback.mayEditMetadata()) {
			editFeedButton = LinkFactory.createButtonSmall("feed.edit", vcInfo, this);
			editFeedButton.setElementCssClass("o_sel_feed_edit");
		}

		vcInfo.contextPut("callback", callback);

		displayUrlCtr = new DisplayFeedUrlController(ureq, wControl, feed, helper, uiFactory.getTranslator());
		listenTo(displayUrlCtr);
		vcInfo.put("feedUrlComponent", displayUrlCtr.getInitialComponent());

		vcMain.put("info", vcInfo);

		itemsCtr = new ItemsController(ureq, wControl, feed, helper, uiFactory, callback, vcRightCol, displayConfig);
		listenTo(itemsCtr);
		vcMain.put("items", itemsCtr.getInitialComponent());

		putInitialPanel(vcMain);
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// feed for this event and make sure the updated feed object is in the view
		feed = feedManager.loadFeed(feed);
		vcInfo.contextPut("feed", feed);
		
		if (source == editFeedButton) {
			doEditFeedDescription(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				cleanUp();
				// If the user cancels the first time after deciding to subscribe to
				// an external feed, undo his decision
				if (feed.isExternal()) {
					if (oldFeedUrl == null || "".equals(oldFeedUrl)) {
						feed = feedManager.updateFeedMode(null, feed);
						itemsCtr.makeInternalAndExternalButtons();
					}
				}
				//release lock
				feedManager.releaseLock(lock);
			}
		} else if (source == feedFormCtr) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				// Dispose the cmc and the feedFormCtr.
				cmc.deactivate();

				if (event.equals(Event.CHANGED_EVENT)) {
					doSaveChanges(ureq);
				} else if (event.equals(Event.CANCELLED_EVENT)) {
					// If the user cancels the first time after deciding to subscribe to
					// an external feed, undo his decision
					if (feed.isExternal()) {
						if (oldFeedUrl == null || "".equals(oldFeedUrl)) {
							feed = feedManager.updateFeedMode(null, feed);
							itemsCtr.makeInternalAndExternalButtons();
						}
					}
				}
				// release the lock
				feedManager.releaseLock(lock);
				cleanUp();
			}
		} else if (source == itemsCtr) {
			if(event.equals(ItemsController.HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT)) {
				doNewExternalFeedurl(ureq);
			} else if (event.equals(ItemsController.FEED_INFO_IS_DIRTY_EVENT)) {
				vcInfo.setDirty(true);
			}
		} 
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(feedFormCtr);
		removeAsListenerAndDispose(cmc);
		feedFormCtr = null;
		cmc = null;
	}

	/**
	 * @param controller The <code>FormBasicController</code> to be displayed in
	 *          the modal dialog.
	 */
	private void activateModalDialog(FormBasicController controller, String title) {
		listenTo(controller);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		Item item = null;
		String itemId = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(itemId != null && itemId.startsWith("item=")) {
			itemId = itemId.substring(5, itemId.length());
			try {
				Long itemKey = Long.parseLong(itemId);
				item = FeedManager.getInstance().loadItem(itemKey);
			} catch (Exception e) {
				item = FeedManager.getInstance().loadItemByGuid(itemId);
			}
		}
		if (item != null) {
			itemsCtr.activate(ureq, item);
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(feed, true)) {
				dispose();
			}
		} else if (event instanceof FeedChangedEvent) {
			FeedChangedEvent fce = (FeedChangedEvent) event;
			if (fce.getFeedKey().equals(feed.getKey())) {
				feed = feedManager.loadFeed(feed);
				vcInfo.contextPut("suppressCache", "&" + ZonedDateTime.now().toInstant().toEpochMilli());
				vcInfo.contextPut("feed", feed);
				vcInfo.setDirty(true);
			}
		}
	}
	
	private void doNewExternalFeedurl(UserRequest ureq) {
		feed = feedManager.loadFeed(feed);
		oldFeedUrl = feed.getExternalFeedUrl();	
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(feed, false);		
		feedFormCtr = new FeedFormController(ureq, getWindowControl(), feed, uiFactory, repositoryEntry, true);
		activateModalDialog(feedFormCtr, uiFactory.getTranslator().translate("feed.edit"));
	}
	
	private void doEditFeedDescription(UserRequest ureq) {
		lock = feedManager.acquireLock(feed, getIdentity());
		if (lock.isSuccess()) {
			if (feed.isExternal()) {
				oldFeedUrl = feed.getExternalFeedUrl();
			}

			RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(feed, false);
			feedFormCtr = new FeedFormController(ureq, getWindowControl(), feed, uiFactory, repositoryEntry, false);
			activateModalDialog(feedFormCtr, uiFactory.getTranslator().translate("feed.edit"));
		} else {
			String fullName = userManager.getUserDisplayName(lock.getOwner());
			String i18nMsg = lock.isDifferentWindows() ? "feed.is.being.edited.by.same.user" : "feed.is.being.edited.by";
			showInfo(i18nMsg, fullName);
		}
	}
	
	private void doSaveChanges(UserRequest ureq) {
		vcInfo.setDirty(true);
		// For external podcasts, set the feed to undefined if the feed url
		// has been set empty.
		if (feed.isExternal() && feedFormCtr.canChangeUrl()) {
			String newFeed = feed.getExternalFeedUrl();
			displayUrlCtr.setUrl(newFeed);
			if (newFeed == null) {
				feed.setExternal(null);
				itemsCtr.makeInternalAndExternalButtons();
			}
		}
		
		feed = feedManager.updateFeed(feed);
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(feed, false);
		
		//handle image-changes if any
		if (feedFormCtr.isImageDeleted()) {
			feed = feedManager.deleteFeedImage(feed);
			if(re != null) {
				repositoryManager.deleteImage(re);
			}
		} else { // set the image
			doUpdateImage(re, getIdentity());
		}

		itemsCtr.resetItems(ureq, feed);	
		// Set the URIs correctly
		helper.setURIs(feed);
		vcInfo.contextPut("feed", feed);

		if(re != null) {
			repositoryManager.setDescriptionAndName(re, feed.getTitle(), feed.getDescription());
		}
		// do logging
		ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(), LoggingResourceable.wrap(feed));
		
		fireEvent(ureq, new ReloadSettingsEvent(false, false, false, true));
	}
	
	private void doUpdateImage(RepositoryEntry re, Identity updatedBy) {
		FileElement image = feedFormCtr.getFile();
		if(image != null && re != null) {
			VFSContainer tmpHome = new LocalFolderImpl(new File(WebappHelper.getTmpDir()));
			VFSContainer tmpContainer = tmpHome.createChildContainer(UUID.randomUUID().toString());
			VFSLeaf tmpImage = tmpContainer.createChildLeaf(image.getUploadFileName());
			VFSManager.copyContent(image.getUploadFile(), tmpImage, getIdentity());
			repositoryManager.setImage(tmpImage, re, updatedBy);
			tmpContainer.deleteSilently();
		}
		feed = feedManager.replaceFeedImage(feed, image);
	}
}
