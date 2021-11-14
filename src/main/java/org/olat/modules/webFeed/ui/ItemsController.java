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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.navigation.Dated;
import org.olat.core.commons.controllers.navigation.NavigationEvent;
import org.olat.core.commons.controllers.navigation.YearNavigationController;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.component.MediaCollectorComponent;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.model.ItemImpl;
import org.olat.modules.webFeed.portfolio.BlogEntryMedia;
import org.olat.modules.webFeed.portfolio.BlogEntryMediaHandler;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is responsible for dealing with items. For internal podcasts,
 * items can be created, edited and deleted.
 *
 * Initial Date: Mar 2, 2009 <br>
 *
 * @author gwassmann
 */
public class ItemsController extends BasicController implements Activateable2 {

	private VelocityContainer vcItems;
	private ArrayList<Link> editButtons;
	private ArrayList<Link> deleteButtons;
	private ArrayList<Link> itemLinks;
	private Map<Item, Controller> artefactLinks;
	private Map<Item, Controller> commentsLinks;
	private Link addItemButton, makeInternalButton, makeExternalButton, olderItemsLink, newerItemsLink, startpageLink;
	private Link externalUrlButton;
	private FormBasicController itemFormCtr;
	private ExternalUrlController externalUrlCtr;
	private CloseableModalController cmc;
	private DialogBoxController confirmDialogCtr;
	private Feed feedResource;
	private List<Item> accessibleItems;
	private List<Long> filteredItemKeys;
	private Item currentItem;
	private FeedViewHelper helper;
	private FeedUIFactory uiFactory;
	private YearNavigationController naviCtr;
	private FeedSecurityCallback callback;
	private Panel mainPanel;
	private ItemController itemCtr;
	// private int allItemsCount = 0;
	private List<ItemId> allItemIds;
	// Only one lock variable is needed, since only one item can be edited
	// at a time.
	private LockResult lock;
	private FeedItemDisplayConfig displayConfig;
	public static final Event HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT = new Event("cmd.handle.new.external.feed.dialog");
	public static final Event FEED_INFO_IS_DIRTY_EVENT = new Event("cmd.feed.info.is.dirty");

	FeedManager feedManager = FeedManager.getInstance();
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private BlogEntryMediaHandler blogMediaHandler;

	/**
	 * default constructor, with full FeedItemDisplayConfig
	 *
	 * @param ureq
	 * @param wControl
	 * @param feed
	 * @param helper
	 * @param uiFactory
	 * @param callback
	 * @param vcRightColumn
	 */
	public ItemsController(final UserRequest ureq, final WindowControl wControl, final Feed feed,
			final FeedViewHelper helper, final FeedUIFactory uiFactory, final FeedSecurityCallback callback,
			final VelocityContainer vcRightColumn) {
		this(ureq, wControl, feed, helper, uiFactory, callback, vcRightColumn, null);
	}

	/**
	 * load items with a given displayconfig
	 *
	 * @param ureq
	 * @param wControl
	 * @param feed
	 * @param helper
	 * @param uiFactory
	 * @param callback
	 * @param vcRightColumn
	 * @param displayConfig
	 */
	public ItemsController(final UserRequest ureq, final WindowControl wControl, final Feed feed,
			final FeedViewHelper helper, final FeedUIFactory uiFactory, final FeedSecurityCallback callback,
			final VelocityContainer vcRightColumn, FeedItemDisplayConfig displayConfig) {
		super(ureq, wControl);

		if (displayConfig == null) {
			displayConfig = new FeedItemDisplayConfig(true, true, true);
		}
		this.displayConfig = displayConfig;
		this.feedResource = feed;
		this.accessibleItems = feedManager.loadFilteredAndSortedItems(feed, filteredItemKeys, callback, ureq.getIdentity());
		this.helper = helper;
		this.uiFactory = uiFactory;
		this.callback = callback;
		setTranslator(uiFactory.getTranslator());

		vcItems = uiFactory.createItemsVelocityContainer(this);
		vcItems.contextPut("feed", feed);
		vcItems.contextPut("items", accessibleItems);
		vcItems.contextPut("callback", callback);
		vcItems.contextPut("helper", helper);

		olderItemsLink = LinkFactory.createLink("feed.older.items", vcItems, this);
		olderItemsLink.setCustomDisplayText("&laquo; " + translate("feed.older.items"));
		olderItemsLink.setCustomEnabledLinkCSS("o_backward");
		olderItemsLink.setTitle("feed.older.items");

		newerItemsLink = LinkFactory.createLink("feed.newer.items", vcItems, this);
		newerItemsLink.setCustomEnabledLinkCSS("o_forward");
		newerItemsLink.setCustomDisplayText(translate("feed.newer.items") + " &raquo;");
		newerItemsLink.setTitle("feed.newer.items");

		startpageLink = LinkFactory.createLink("feed.startpage", vcItems, this);
		startpageLink.setCustomEnabledLinkCSS("o_first_page");

		createEditButtons(feed);

		// Add item details page link
		createItemLinks();
		// Add item user comments link and rating
		if (displayConfig.isShowCRInMinimized()) {
			createCommentsAndRatingsLinks(ureq, feed);
		}
		// Add date components
		createDateComponents(feed);

		// The year/month navigation
		setAllItemIds(accessibleItems);
		naviCtr = new YearNavigationController(ureq, wControl, getTranslator(), accessibleItems);
		listenTo(naviCtr);
		if (displayConfig.isShowDateNavigation()) {
			vcRightColumn.put("navi", naviCtr.getInitialComponent());
		}

		mainPanel = new Panel("mainPanel");
		mainPanel.setContent(vcItems);
		this.putInitialPanel(mainPanel);
	}

	private void setAllItemIds(List<Item> items) {
		allItemIds = new ArrayList<>();
		for (Item item : items) {
			allItemIds.add(new ItemId(item));
		}
	}

	private boolean isSameAllItems(List<Item> items) {
		if (allItemIds == null)
			return false;
		List<ItemId> itemIds = new ArrayList<>();
		for (Item item : items) {
			itemIds.add(new ItemId(item));
		}
		return allItemIds.containsAll(itemIds) && itemIds.containsAll(allItemIds);
	}

	/**
	 * Creates all necessary buttons for editing the feed's items
	 * @param feed
	 *            the current feed object
	 */
	private void createEditButtons(Feed feed) {
		editButtons = new ArrayList<>();
		deleteButtons = new ArrayList<>();
		artefactLinks = new HashMap<>();
		if (feed.isInternal()) {
			addItemButton = LinkFactory.createButtonSmall("feed.add.item", vcItems, this);
			addItemButton.setElementCssClass("o_sel_feed_item_new");
			if (accessibleItems != null) {
				for (Item item : accessibleItems) {
					createButtonsForItem(feed, item);
				}
			}
		} else if (feed.isExternal()) {
			externalUrlButton = LinkFactory.createButtonSmall("feed.external.url", vcItems, this);
			externalUrlButton.setElementCssClass("o_sel_feed_item_new");
		} else if (feed.isUndefined()) {
			// The feed is whether internal nor external:
			// That is,
			// - it has just been created,
			// - all items have been removed or
			// - the feed url of an external feed has been set empty.
			// In such a case, the user can decide whether to make it internal
			// or
			// external.
			makeInternalAndExternalButtons();
		}
	}

	/**
	 * Create the comments and rating components for each feed item
	 *
	 * @param ureq
	 * @param feed
	 */
	private void createCommentsAndRatingsLinks(UserRequest ureq, Feed feed) {
		if (accessibleItems != null) {
			for (Item item : accessibleItems) {
				// Add rating and commenting controller
				createCommentsAndRatingsLink(ureq, feed, item);
			}
		}
	}

	/**
	 * Create comments and rating component link for given feed item
	 *
	 * @param ureq
	 * @param feed
	 * @param item
	 */
	private void createCommentsAndRatingsLink(UserRequest ureq, Feed feed, Item item) {
		if (feed == null || item == null)
			return;// check against concurrent changes
		if (CoreSpringFactory.containsBean(CommentAndRatingService.class)) {
			if (commentsLinks == null) {
				commentsLinks = new HashMap<>();
			} else if (commentsLinks.containsKey(item)) {
				removeAsListenerAndDispose(commentsLinks.get(item));
			}

			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(),
					callback.mayEditMetadata(), anonym);
			UserCommentsAndRatingsController commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq,
					getWindowControl(), feed, item.getGuid(), secCallback, null, true, true, false);
			commentsAndRatingCtr.setUserObject(item);
			listenTo(commentsAndRatingCtr);
			commentsLinks.put(item, commentsAndRatingCtr);
			String guid = item.getGuid();
			vcItems.put("commentsAndRating." + guid, commentsAndRatingCtr.getInitialComponent());
		}
	}

	/**
	 * Create a GUI component to display a nicely formatted date
	 *
	 * @param ureq
	 * @param feed
	 */
	private void createDateComponents(Feed feed) {
		if (accessibleItems != null) {
			for (Item item : accessibleItems) {
				String guid = item.getGuid();
				if (item.getDate() != null) {
					DateComponentFactory.createDateComponentWithYear("date." + guid, item.getDate(), vcItems);
				}
			}
		}
	}

	private void createItemLinks() {
		itemLinks = new ArrayList<>();
		if (accessibleItems != null) {
			for (Item item : accessibleItems) {
				createItemLink(item);
			}
		}
	}

	/**
	 * @param item
	 */
	private void createItemLink(Item item) {
		String guid = item.getGuid();
		Link itemLink_more = LinkFactory.createCustomLink("link.to." + guid, "link.to." + guid, "feed.link.more",
				Link.LINK, vcItems, this);
		itemLink_more.setIconRightCSS("o_icon o_icon_start");
		itemLink_more.setCustomEnabledLinkCSS("o_link_forward");
		itemLink_more.setUserObject(item);

		Link itemLink_title = LinkFactory.createCustomLink("titlelink.to." + guid, "titlelink.to." + guid,
				StringHelper.escapeHtml(item.getTitle()), Link.NONTRANSLATED, vcItems, this);
		itemLink_title.setUserObject(item);

		itemLinks.add(itemLink_title);
		itemLinks.add(itemLink_more);
	}

	/**
	 * Instantiates the makeInternal and the makeExternal-Buttons and puts it to
	 * the items velocity container's context.
	 */
	public void makeInternalAndExternalButtons() {
		if (callback.mayEditItems() || callback.mayCreateItems()) {
			makeInternalButton = LinkFactory.createButton("feed.make.internal", vcItems, this);
			makeExternalButton = LinkFactory.createButton("feed.make.external", vcItems, this);
		}
	}

	private void createButtonsForItem(Feed feed, Item item) {
		boolean author = getIdentity().getKey().equals(item.getAuthorKey());
		boolean edit = callback.mayEditItems() || (author && callback.mayEditOwnItems());
		boolean delete = callback.mayDeleteItems() || (author && callback.mayDeleteOwnItems());

		String guid = item.getGuid();
		String editId = "feed.edit.item.".concat(guid);
		Link editButton = LinkFactory.createCustomLink(editId, editId, "feed.edit.item", Link.BUTTON_SMALL, vcItems,
				this);
		editButton.setElementCssClass("o_sel_feed_item_edit");
		editButton.setEnabled(edit);
		editButton.setVisible(edit);

		String deleteId = "delete.".concat(guid);
		Link deleteButton = LinkFactory.createCustomLink(deleteId, deleteId, "delete", Link.BUTTON_SMALL, vcItems,
				this);
		deleteButton.setElementCssClass("o_sel_feed_item_delete");
		deleteButton.setEnabled(delete);
		deleteButton.setVisible(delete);

		if (feedResource.isInternal() && getIdentity().getKey() != null
				&& getIdentity().getKey().equals(item.getAuthorKey())) {
			String businessPath = BusinessControlFactory.getInstance()
					.getAsString(getWindowControl().getBusinessControl());
			businessPath += "[item=" + item.getKey() + ":0]";

			if (portfolioModule.isEnabled()) {
				String name = "feed.artefact.item.".concat(guid);
				BlogEntryMedia media = new BlogEntryMedia(feed, item);
				MediaCollectorComponent collectorCmp = new MediaCollectorComponent(name, getWindowControl(), media,
						blogMediaHandler, businessPath);
				vcItems.put(name, collectorCmp);
			}
		}

		editButton.setUserObject(item);
		deleteButton.setUserObject(item);
		editButtons.add(editButton);
		deleteButtons.add(deleteButton);
	}

	@Override
	protected void doDispose() {
		// make sure the lock is released
		feedManager.releaseLock(lock);
		// Dispose confirm deletion dialog controller since it isn't listend to.
		if (confirmDialogCtr != null) {
			removeAsListenerAndDispose(confirmDialogCtr);
		}

		if (artefactLinks != null) {
			for (Controller ctrl : artefactLinks.values()) {
				ctrl.dispose();
			}
			artefactLinks.clear();
			artefactLinks = null;
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// feed for this event and make sure the updated feed object is in the
		// view
		feedResource = feedManager.loadFeed(feedResource);
		accessibleItems = feedManager.loadFilteredAndSortedItems(feedResource, filteredItemKeys, callback, ureq.getIdentity());

		if (source == addItemButton) {
			currentItem = new ItemImpl(feedResource);
			currentItem.setDraft(true);
			currentItem.setAuthorKey(ureq.getIdentity().getKey());
			// Generate new GUID for item, needed for media files that are
			// stored relative to the GUID
			currentItem.setGuid(CodeHelper.getGlobalForeverUniqueID());
			itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem);
			activateModalDialog(itemFormCtr, uiFactory.getTranslator().translate("feed.edit.item"));

		} else if (editButtons != null && editButtons.contains(source)) {
			currentItem = (Item) ((Link) source).getUserObject();
			// check if still available, maybe deleted by other user in the
			// meantime
			if (accessibleItems.contains(currentItem)) {
				lock = feedManager.acquireLock(feedResource, currentItem, getIdentity());
				if (lock.isSuccess()) {
					// reload to prevent stale object, then launch editor
					currentItem = feedManager.loadItem(currentItem.getKey());
					itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem);
					activateModalDialog(itemFormCtr, uiFactory.getTranslator().translate("feed.edit.item"));
				} else {
					String fullName = userManager.getUserDisplayName(lock.getOwner());
					showInfo("feed.item.is.being.edited.by", fullName);
				}
			} else {
				showInfo("feed.item.is.being.edited.by", "unknown");
			}

		} else if (deleteButtons != null && deleteButtons.contains(source)) {
			Item item = (Item) ((Link) source).getUserObject();
			confirmDialogCtr = activateYesNoDialog(ureq, null, translate("feed.item.confirm.delete"), confirmDialogCtr);
			confirmDialogCtr.setUserObject(item);

		} else if (itemLinks != null && itemLinks.contains(source)) {
			Item item = (Item) ((Link) source).getUserObject();
			if (item != null) {
				displayItemController(ureq, item);
			}
		} else if (source == externalUrlButton) {
			externalUrlCtr = uiFactory.createExternalUrlController(ureq, getWindowControl(), feedResource);
			activateModalDialog(externalUrlCtr, uiFactory.getTranslator().translate("feed.external.url"));
		} else if (source == makeInternalButton) {
			if (feedResource.isUndefined()) {
				feedResource = feedManager.updateFeedMode(Boolean.FALSE, feedResource);
			} else if (feedResource.isExternal()) {
				externalUrlButton = LinkFactory.createButtonSmall("feed.external.url", vcItems, this);
				externalUrlButton.setElementCssClass("o_sel_feed_item_new");
				// Very special case: another user concurrently changed feed to
				// external. Do nothing
				vcItems.setDirty(true);
				return;
			}
			// else nothing to do, already set to internal by a concurrent user

			// Add temporary item and open edit dialog
			addItemButton = LinkFactory.createButton("feed.add.item", vcItems, this);
			addItemButton.setElementCssClass("o_sel_feed_item_new");
			currentItem = new ItemImpl(feedResource);
			currentItem.setDraft(true);
			currentItem.setAuthorKey(ureq.getIdentity().getKey());
			// Generate new GUID for item, needed for media files that are
			// stored relative to the GUID
			currentItem.setGuid(CodeHelper.getGlobalForeverUniqueID());
			itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem);
			activateModalDialog(itemFormCtr, uiFactory.getTranslator().translate("feed.edit.item"));
			// do logging
			ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(),
					LoggingResourceable.wrap(feedResource));

		} else if (source == makeExternalButton) {
			if (feedResource.isUndefined()) {
				externalUrlButton = LinkFactory.createButtonSmall("feed.external.url", vcItems, this);
				externalUrlButton.setElementCssClass("o_sel_feed_item_new");
				feedResource = feedManager.updateFeedMode(Boolean.TRUE, feedResource);
				vcItems.setDirty(true);
				// Ask listening FeedMainController to open and handle a new
				// external
				// feed dialog.
				fireEvent(ureq, HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT);
				// do logging
				ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(),
						LoggingResourceable.wrap(feedResource));
			}
		} else if (source == olderItemsLink) {
			helper.olderItems();
			createEditButtons(feedResource);
			createCommentsAndRatingsLinks(ureq, feedResource);
			vcItems.setDirty(true);

		} else if (source == newerItemsLink) {
			helper.newerItems();
			createEditButtons(feedResource);
			createCommentsAndRatingsLinks(ureq, feedResource);
			vcItems.setDirty(true);

		} else if (source == startpageLink) {
			helper.startpage();
			createEditButtons(feedResource);
			createCommentsAndRatingsLinks(ureq, feedResource);
			vcItems.setDirty(true);

		} else if (source instanceof Link) {
			// if it's a link try to get attached identity and assume that user
			// wants
			// to see the users home page
			Link userLink = (Link) source;
			Object userObject = userLink.getUserObject();
			if (userObject instanceof Identity) {
				Identity chosenIdentity = (Identity) userObject;
				String bPath = "[HomePage:" + chosenIdentity.getKey() + "]";
				NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
			}
		}

		// Check if someone else added an item, reload everything
		List<Item> items = feedManager.loadFilteredAndSortedItems(feedResource, null, callback, ureq.getIdentity());
		if (!isSameAllItems(items)) {
			resetItems(ureq, feedResource);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// reload feed for this event and make sure the updated feed object is
		// in the view
		feedResource = feedManager.loadFeed(feedResource);
		accessibleItems = feedManager.loadFilteredAndSortedItems(feedResource, filteredItemKeys, callback, ureq.getIdentity());

		if (source == cmc) {
			if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(itemFormCtr);
				itemFormCtr = null;
				// Check if this item has ever been added to the feed. If not,
				// remove the temp dir
				cleanupTmpItemMediaDir(currentItem);
				// If there were no items and the user doesn't want to save the
				// first item, go back to the decision whether to make the feed
				// internally or subscribe to an external feed.
				if (!feedManager.hasItems(feedResource)) {
					feedResource = feedManager.updateFeedMode(null, feedResource);
					makeInternalAndExternalButtons();
				}
				// release lock
				feedManager.releaseLock(lock);
			}
		} else if (source == confirmDialogCtr && DialogBoxUIFactory.isYesEvent(event)) {
			// The user confirmed that the item shall be deleted
			Item item = (Item) ((DialogBoxController) source).getUserObject();
			lock = feedManager.acquireLock(feedResource, item, getIdentity());
			if (lock.isSuccess()) {
				// remove the item from the naviCtr
				naviCtr.remove(item);
				// permanently remove item
				feedResource = feedManager.deleteItem(item);
				// remove delete and edit buttons of this item
				for (Link deleteButton : deleteButtons) {
					if (item.equals(deleteButton.getUserObject())) {
						deleteButtons.remove(deleteButton);
						break;
					}
				}
				for (Link editButton : editButtons) {
					if (item.equals(editButton.getUserObject())) {
						editButtons.remove(editButton);
						break;
					}
				}
				// If the last item has been deleted, provide buttons for adding
				// items manually or from an external source/feed.
				if (!feedManager.hasItems(feedResource)) {
					makeInternalAndExternalButtons();
					// The subscription/feed url from the feed info is obsolete
					fireEvent(ureq, ItemsController.FEED_INFO_IS_DIRTY_EVENT);
				} else {
					if (callback.mayEditItems() || callback.mayCreateItems()) {
						createEditButtons(feedResource);
					}
					createCommentsAndRatingsLinks(ureq, feedResource);
				}
				vcItems.setDirty(true);
				// in case we were in single item view, show all items
				mainPanel.setContent(vcItems);
				feedManager.releaseLock(lock);
				lock = null;
				// do logging
				ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_DELETE, getClass(),
						LoggingResourceable.wrap(item));

			} else {
				String fullName = userManager.getUserDisplayName(lock.getOwner());
				showInfo("feed.item.is.being.edited.by", fullName);
			}

		} else if (source == itemFormCtr) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				if (event.equals(Event.CHANGED_EVENT)) {
					FileElement mediaFile = currentItem.getMediaFile();
					if (feedManager.getItemContainer(currentItem) == null) {
						// Ups, deleted in the meantime by someone else
						// remove the item from the naviCtr
						naviCtr.remove(currentItem);
					} else {
						if (!accessibleItems.contains(currentItem)) {
							// Add the modified item if it is not part of the
							// feed
							feedResource = feedManager.createItem(feedResource, currentItem, mediaFile);
							if (feedResource != null) {
								createButtonsForItem(feedResource, currentItem);
								createItemLink(currentItem);
								// Add date component
								String guid = currentItem.getGuid();
								if (currentItem.getDate() != null) {
									DateComponentFactory.createDateComponentWithYear("date." + guid,
											currentItem.getDate(), vcItems);
								}
								// Add comments and rating
								createCommentsAndRatingsLink(ureq, feedResource, currentItem);
								// add it to the navigation controller
								naviCtr.add(currentItem);
								accessibleItems = feedManager.loadFilteredAndSortedItems(feedResource, null,
										callback, ureq.getIdentity());
								if (accessibleItems != null && accessibleItems.size() == 1) {
									// First item added, show feed url (for
									// subscription)
									fireEvent(ureq, ItemsController.FEED_INFO_IS_DIRTY_EVENT);
									// Set the base URI of the feed for the
									// current user. All users
									// have unique URIs.
									helper.setURIs(currentItem.getFeed());
								}
							}
						} else {
							// Write item file
							currentItem = feedManager.updateItem(currentItem, mediaFile);
							if (itemCtr != null) {
								displayItemController(ureq, currentItem);
							}
							ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_EDIT, getClass(),
									LoggingResourceable.wrap(currentItem));
						}
					}
					vcItems.setDirty(true);
					// if the current item is displayed, update the view
					if (itemCtr != null) {
						itemCtr.getInitialComponent().setDirty(true);
					}

				} else if (event.equals(Event.CANCELLED_EVENT)) {
					// Check if this item has ever been added to the feed. If
					// not, remove the temp dir
					cleanupTmpItemMediaDir(currentItem);
					// If there were no items and the user doesn't want to save
					// the
					// first item, go back to the decision whether to make the
					// feed
					// internally or subscribe to an external feed.
					if (!feedManager.hasItems(feedResource)) {
						feedResource = feedManager.updateFeedMode(null, feedResource);
						makeInternalAndExternalButtons();
					}
				}
				// release the lock
				feedManager.releaseLock(lock);

				// Dispose the cmc and the podcastFormCtr.
				cmc.deactivate();
				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(itemFormCtr);
				itemFormCtr = null;
			}
		} else if (source == externalUrlCtr) {
			if (event.equals(Event.CHANGED_EVENT))  {
				String externalUrl = externalUrlCtr.getExternalFeedUrlEl();
				feedManager.updateExternalFeedUrl(feedResource, externalUrl);
			} else if (event.equals(Event.CHANGED_EVENT)) {
				// nothing to do
			}
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			removeAsListenerAndDispose(externalUrlCtr);
			externalUrlCtr = null;
		} else if (source == naviCtr && event instanceof NavigationEvent) {
			List<? extends Dated> selItems = ((NavigationEvent) event).getSelectedItems();
			filteredItemKeys = new ArrayList<>();
			for (Dated selItem : selItems) {
				if (selItem instanceof Item) {
					Item item = (Item) selItem;
					filteredItemKeys.add(item.getKey());
				}
			}
			if (callback.mayEditItems() || callback.mayCreateItems()) {
				createEditButtons(feedResource);
			}
			createCommentsAndRatingsLinks(ureq, feedResource);
			vcItems.setDirty(true);
			mainPanel.setContent(vcItems);
		} else if (source == itemCtr) {
			if (event == Event.BACK_EVENT) {
				mainPanel.setContent(vcItems);
				removeAsListenerAndDispose(itemCtr);
				itemCtr = null;
			}

		} else if (source instanceof UserCommentsAndRatingsController) {
			UserCommentsAndRatingsController commentsRatingsCtr = (UserCommentsAndRatingsController) source;
			if (event == UserCommentsAndRatingsController.EVENT_COMMENT_LINK_CLICKED) {
				// go to details page
				Item item = (Item) commentsRatingsCtr.getUserObject();
				if (item != null) {
					ItemController myItemCtr = displayItemController(ureq, item);
					List<ContextEntry> entries = BusinessControlFactory.getInstance()
							.createCEListFromResourceType(ItemController.ACTIVATION_KEY_COMMENTS);
					myItemCtr.activate(ureq, entries, null);
				}
			}
		}

		// reload everything
		if (feedResource != null) {
			resetItems(ureq, feedResource);
		}
	}

	/**
	 * Private helper to remove any temp media files created for this feed
	 *
	 * @param tmpItem
	 */
	private void cleanupTmpItemMediaDir(Item tmpItem) {
		if(tmpItem == null) return;
		String guid = tmpItem.getGuid();
		if (guid == null) return;

		// delete the dir only if the item is not saved.
		Long key = tmpItem.getKey();
		if (key == null) {
			feedManager.deleteItem(tmpItem);
		}
	}

	/**
	 * @param controller
	 *            The <code>FormBasicController</code> to be displayed in the
	 *            modal dialog.
	 */
	private void activateModalDialog(FormBasicController controller, String title) {
		listenTo(controller);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}

	/**
	 * Sets the items view dirty.
	 *
	 * @param ureq
	 * @param feed
	 *            the current feed
	 */
	public void resetItems(UserRequest ureq, Feed feed) {
		feedResource = feedManager.loadFeed(feedResource);
		accessibleItems = feedManager.loadFilteredAndSortedItems(feed, filteredItemKeys, callback, ureq.getIdentity());
		vcItems.contextPut("feed", feedResource);
		vcItems.contextPut("items", accessibleItems);

		List<Item> naviItems = accessibleItems;
		if (filteredItemKeys != null && !filteredItemKeys.isEmpty()) {
			naviItems = feedManager.loadFilteredAndSortedItems(feed, Collections.emptyList(), callback, ureq.getIdentity());
		}
		naviCtr.setDatedObjects(naviItems);
		setAllItemIds(accessibleItems);
		// Add item details page link
		createItemLinks();
		// Add item user comments link and rating
		if (displayConfig.isShowCRInMinimized()) {
			createCommentsAndRatingsLinks(ureq, feedResource);
		}
		// Add date components
		createDateComponents(feedResource);

		vcItems.setDirty(true);
	}

	/**
	 * Displays the item in the mainPanel of this controller.
	 *
	 * @param ureq
	 * @param item
	 */
	private ItemController displayItemController(UserRequest ureq, Item item) {
		removeAsListenerAndDispose(itemCtr);
		Link editButton = getButtonByUserObject(item, editButtons);
		Link deleteButton = getButtonByUserObject(item, deleteButtons);
		Controller artefactLink = getArtefactLinkByUserObject(item);
		Feed feed = feedManager.loadFeed(feedResource);
		item = feedManager.loadItem(item.getKey());
		if (item != null) {
			itemCtr = new ItemController(ureq, getWindowControl(), item, feed, helper, uiFactory, callback, editButton,
					deleteButton, artefactLink, displayConfig);
			listenTo(itemCtr);
			mainPanel.setContent(itemCtr.getInitialComponent());
		}
		return itemCtr;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty() || feedResource == null)
			return;

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
			activate(ureq, item);
		}
	}

	/**
	 * @param item
	 */
	public void activate(UserRequest ureq, Item item) {
		displayItemController(ureq, item);
	}

	/**
	 * @param item
	 * @param buttons
	 * @return The Link in buttons which has the item attached as user object or
	 *         null
	 */
	private Link getButtonByUserObject(Item item, List<Link> buttons) {
		Link result = null;
		if (buttons != null && item != null) {
			for (Link button : buttons) {
				if (item.equals(button.getUserObject())) {
					result = button;
					break;
				}
			}
		}
		return result;
	}

	private Controller getArtefactLinkByUserObject(Item item) {
		Controller result = null;
		if (artefactLinks != null && artefactLinks.containsKey(item)) {
			return artefactLinks.get(item);
		}
		return result;
	}

	private class ItemId {
		private final String guid;
		private final Date lastModification;

		public ItemId(Item item) {
			guid = item.getGuid();
			lastModification = item.getLastModified();
		}

		@Override
		public int hashCode() {
			return guid.hashCode() + (lastModification == null ? -483 : lastModification.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof ItemId) {
				ItemId id = (ItemId) obj;
				return guid.equals(id.guid) && ((lastModification == null && id.lastModification == null)
						|| (lastModification != null && lastModification.equals(id.lastModification)));
			}

			return false;
		}
	}
}
