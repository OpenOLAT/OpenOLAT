/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.ui;

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
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * This is the main feed layout controller. It handles everything from adding
 * episodes to changing title and description.
 * 
 * <P>
 * Initial Date: Feb 5, 2009 <br>
 * 
 * @author gwassmann
 */
// ClosableModalController is deprecated. No alternative implemented.
@SuppressWarnings("deprecation")
public class FeedMainController extends BasicController implements Activateable, GenericEventListener {

	private static final FeedManager feedManager = FeedManager.getInstance();
	private Feed feed;
	private Link editFeedButton;
	private CloseableModalController cmc;
	private FormBasicController feedFormCtr;
	private VelocityContainer vcMain, vcInfo, vcRightCol;
	private ItemsController itemsCtr;
	private LockResult lock;
	private FeedViewHelper helper;
	private DisplayFeedUrlController displayUrlCtr;
	private FeedUIFactory uiFactory;
	private FeedSecurityCallback callback;
	// needed for comparison
	private String oldFeedUrl;
	
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
		this(ores, ureq, wControl, null, null, uiFactory, callback);
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
			FeedUIFactory uiFactory, FeedSecurityCallback callback) {
		super(ureq, wControl);
		this.uiFactory = uiFactory;
		this.callback = callback;
		setTranslator(uiFactory.getTranslator());
		feed = feedManager.getFeed(ores);
		helper = new FeedViewHelper(feed, getIdentity(), uiFactory.getTranslator(), courseId, nodeId, callback);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), feed);
		display(ureq, wControl);		
		// do logging
		ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_READ, getClass(), LoggingResourceable.wrap(feed));
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
	private void display(UserRequest ureq, WindowControl wControl) {
		vcMain = createVelocityContainer("feed_main");

		vcInfo = uiFactory.createInfoVelocityContainer(this);
		vcInfo.contextPut("feed", feed);
		vcInfo.contextPut("helper", helper);

		vcRightCol = uiFactory.createRightColumnVelocityContainer(this);
		vcMain.put("rightColumn", vcRightCol);

		// The current user has edit rights if he/she is an administrator or an
		// owner of the resource.
		if (callback.mayEditMetadata()) {
			editFeedButton = LinkFactory.createButtonSmall("feed.edit", vcInfo, this);
		}

		vcInfo.contextPut("callback", callback);

		displayUrlCtr = new DisplayFeedUrlController(ureq, wControl, feed, helper, uiFactory.getTranslator());
		listenTo(displayUrlCtr);
		vcInfo.put("feedUrlComponent", displayUrlCtr.getInitialComponent());

		vcMain.put("info", vcInfo);

		itemsCtr = new ItemsController(ureq, wControl, feed, helper, uiFactory, callback, vcRightCol);
		listenTo(itemsCtr);
		vcMain.put("items", itemsCtr.getInitialComponent());

		this.putInitialPanel(vcMain);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		feedManager.releaseLock(lock);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, feed);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == editFeedButton) {
			lock = feedManager.acquireLock(feed, ureq.getIdentity());
			if (lock.isSuccess()) {
				if (feed.isExternal()) {
					oldFeedUrl = feed.getExternalFeedUrl();
					feedFormCtr = new ExternalFeedFormController(ureq, getWindowControl(), feed, uiFactory.getTranslator());
				} else {
					// Default for podcasts is that they are edited within OLAT
					feedFormCtr = new FeedFormController(ureq, getWindowControl(), feed, uiFactory);
				}
				activateModalDialog(feedFormCtr);
			} else {
				showInfo("feed.is.being.edited.by", lock.getOwner().getName());
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(feedFormCtr);
				feedFormCtr = null;
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
				removeAsListenerAndDispose(cmc);
				cmc = null;

				if (event.equals(Event.CHANGED_EVENT)) {
					vcInfo.setDirty(true);
					// For external podcasts, set the feed to undefined if the feed url
					// has been set empty.
					if (feed.isExternal()) {
						String newFeed = feed.getExternalFeedUrl();
						displayUrlCtr.setUrl(newFeed);
						if (newFeed == null) {
							feed.setExternal(null);
							itemsCtr.makeInternalAndExternalButtons();
							// No more episodes to display
							itemsCtr.resetItems(ureq, feed);
						} else if (!newFeed.equals(oldFeedUrl)) {
							// Set the episodes dirty since the feed url changed.							
							itemsCtr.resetItems(ureq, feed);
						}
						// Set the URIs correctly
						helper.setURIs();
					} else {
						if (feedFormCtr instanceof FeedFormController) {
							FeedFormController internalFormCtr = (FeedFormController) feedFormCtr;
							if (internalFormCtr.imageDeleted()) {
								feedManager.deleteImage(feed);
							} else {
								// set the image
								FileElement image = null;
								image = internalFormCtr.getFile();
								feedManager.setImage(image, feed);
							}							
						} else {
							// it's an external feed form, nothing to do in this case
						}
					}
					// Eventually update the feed
					feed = feedManager.updateFeedMetadata(feed);
					// Dispose the feedFormCtr
					removeAsListenerAndDispose(feedFormCtr);
					feedFormCtr = null;
					// do logging
					ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(), LoggingResourceable.wrap(feed));
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
			}
		} else if (source == itemsCtr && event.equals(ItemsController.HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT)) {
			oldFeedUrl = feed.getExternalFeedUrl();			
			feedFormCtr = new ExternalFeedFormController(ureq, getWindowControl(), feed, uiFactory.getTranslator());
			activateModalDialog(feedFormCtr);
		} else if (source == itemsCtr && event.equals(ItemsController.FEED_INFO_IS_DIRTY_EVENT)) {
			vcInfo.setDirty(true);
		}
	}

	/**
	 * @param controller The <code>FormBasicController</code> to be displayed in
	 *          the modal dialog.
	 */
	private void activateModalDialog(FormBasicController controller) {
		listenTo(controller);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest, java.lang.String)
	 */
	public void activate(UserRequest ureq, String itemId) {
		int index = feed.getItemIds().indexOf(itemId);
		if (index >= 0) {
			Item item = feed.getItems().get(index);
			itemsCtr.activate(ureq, item);
		}
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(feed, true)) {
				dispose();
			}
		}
	}
}
