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

import org.apache.commons.lang.StringEscapeUtils;
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
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.modules.webFeed.models.ItemPublishDateComparator;
import org.olat.portfolio.EPUIFactory;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;

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
	private Map<Item,Controller> artefactLinks;
	private Map<Item,Controller> commentsLinks;
	private Link addItemButton, makeInternalButton, makeExternalButton, olderItemsLink, newerItemsLink, startpageLink;
	private FormBasicController itemFormCtr;
	private CloseableModalController cmc;
	private DialogBoxController confirmDialogCtr;
	private Feed feedResource;
	private Item currentItem;
	private FeedViewHelper helper;
	private FeedUIFactory uiFactory;
	private YearNavigationController naviCtr;
	private FeedSecurityCallback callback;
	private Panel mainPanel;
	private ItemController itemCtr;
	//private int allItemsCount = 0;
	private List<ItemId> allItemIds;
	// Only one lock variable is needed, since only one item can be edited
	// at a time.
	private LockResult lock;
	private FeedItemDisplayConfig displayConfig;
	public static Event HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT = new Event("cmd.handle.new.external.feed.dialog");
	public static Event FEED_INFO_IS_DIRTY_EVENT = new Event("cmd.feed.info.is.dirty");
	
	private final UserManager userManager;

	/**
	 * default constructor, with full FeedItemDisplayConfig
	 * @param ureq
	 * @param wControl
	 * @param feed
	 * @param helper
	 * @param uiFactory
	 * @param callback
	 * @param vcRightColumn
	 */
	public ItemsController(final UserRequest ureq, final WindowControl wControl, final Feed feed, final FeedViewHelper helper, final FeedUIFactory uiFactory,
			final FeedSecurityCallback callback, final VelocityContainer vcRightColumn) {
		this(ureq, wControl, feed, helper, uiFactory, callback, vcRightColumn, null);
	}
	
	/**
	 * load items with a given displayconfig
	 * @param ureq
	 * @param wControl
	 * @param feed
	 * @param helper
	 * @param uiFactory
	 * @param callback
	 * @param vcRightColumn
	 * @param displayConfig
	 */
	public ItemsController(final UserRequest ureq, final WindowControl wControl, final Feed feed, final FeedViewHelper helper, final FeedUIFactory uiFactory,
			final FeedSecurityCallback callback, final VelocityContainer vcRightColumn, FeedItemDisplayConfig displayConfig) {
		super(ureq, wControl);
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		if (displayConfig == null) {
			displayConfig = new FeedItemDisplayConfig(true, true, true);
		}
		this.displayConfig = displayConfig;
		this.feedResource = feed;
		this.helper = helper;
		this.uiFactory = uiFactory;
		this.callback = callback;
		setTranslator(uiFactory.getTranslator());

		vcItems = uiFactory.createItemsVelocityContainer(this);
		vcItems.contextPut("feed", feed);
		vcItems.contextPut("callback", callback);
		vcItems.contextPut("helper", helper);

		olderItemsLink = LinkFactory.createLink("feed.older.items", vcItems, this);
		olderItemsLink.setCustomDisplayText("&laquo;");
		olderItemsLink.setCustomEnabledLinkCSS("o_backward");
		olderItemsLink.setTitle("feed.older.items");

		newerItemsLink = LinkFactory.createLink("feed.newer.items", vcItems, this);
		newerItemsLink.setCustomEnabledLinkCSS("o_forward");
		newerItemsLink.setCustomDisplayText("&raquo;");
		newerItemsLink.setTitle("feed.newer.items");
		
		startpageLink = LinkFactory.createLink("feed.startpage", vcItems, this);
		startpageLink.setCustomEnabledLinkCSS("o_first_page");

		if (callback.mayEditItems() || callback.mayCreateItems()) {
			createEditButtons(ureq, feed);
		}
		// Add item details page link
		createItemLinks(feed);
		// Add item user comments link and rating
		if (displayConfig.isShowCRInMinimized()) {
			createCommentsAndRatingsLinks(ureq, feed);
		}
		// Add date components
		createDateComponents(feed);

		// The year/month navigation
		List<Item> items = feed.getFilteredItems(callback, ureq.getIdentity());
		setAllItemIds(items);
		naviCtr = new YearNavigationController(ureq, wControl, getTranslator(), items);
		listenTo(naviCtr);
		if (displayConfig.isShowDateNavigation()){
			vcRightColumn.put("navi", naviCtr.getInitialComponent());
		}

		mainPanel = new Panel("mainPanel");
		mainPanel.setContent(vcItems);
		this.putInitialPanel(mainPanel);
	}
	
	private void setAllItemIds(List<Item> items) {
		allItemIds = new ArrayList<ItemId>();
		for(Item item:items) {
			allItemIds.add(new ItemId(item));
		}
	}
	
	private boolean isSameAllItems(List<Item> items) {
		if(allItemIds == null) return false;
		List<ItemId> itemIds = new ArrayList<ItemId>();
		for(Item item:items) {
			itemIds.add(new ItemId(item));
		}
		return allItemIds.containsAll(itemIds) && itemIds.containsAll(allItemIds);
	}

	/**
	 * Creates all necessary buttons for editing the feed's items
	 * @param feed the current feed object
	 */
	private void createEditButtons(UserRequest ureq, Feed feed) {
		List<Item> items = feed.getCopiedListOfItems();

		editButtons = new ArrayList<Link>();
		deleteButtons = new ArrayList<Link>();
		artefactLinks = new HashMap<Item,Controller>();
		if (feed.isInternal()) {
			addItemButton = LinkFactory.createButtonSmall("feed.add.item", vcItems, this);
			addItemButton.setElementCssClass("o_sel_feed_item_new");
			if (items != null) {
				for (Item item : items) {
					createButtonsForItem(ureq, item);
				}
			}
		} else if (feed.isUndefined()) {
			// The feed is whether internal nor external:
			// That is,
			// - it has just been created,
			// - all items have been removed or
			// - the feed url of an external feed has been set empty.
			// In such a case, the user can decide whether to make it internal or
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
		List<Item> items = feed.getCopiedListOfItems();
		if (items != null) {
			for (Item item : items) {
				// Add rating and commenting controller
				createCommentsAndRatingsLink(ureq, feed, item);
			}			
		}		
	}
	/**
	 * Create comments and rating component link for given feed item
	 * @param ureq
	 * @param feed
	 * @param item
	 */
	private void createCommentsAndRatingsLink(UserRequest ureq, Feed feed, Item item) {
		if(feed == null || item == null) return;//check against concurrent changes
		if (CoreSpringFactory.containsBean(CommentAndRatingService.class)) {
			if(commentsLinks == null) {
				commentsLinks = new HashMap<Item,Controller>();
			} else if(commentsLinks.containsKey(item)) {
				removeAsListenerAndDispose(commentsLinks.get(item));
			}

			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), callback.mayEditMetadata(), anonym);
			UserCommentsAndRatingsController commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), feed, item.getGuid(), secCallback, true, true, false);
			commentsAndRatingCtr.addUserObject(item);
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
		List<Item> items = feed.getCopiedListOfItems();
		if (items != null) {
			for (Item item : items) {
				String guid = item.getGuid();
				if(item.getDate() != null) {
					DateComponentFactory.createDateComponentWithYear("date." + guid, item.getDate(), vcItems);
				}
			}			
		}				
	}
	
	private void createItemLinks(Feed feed) {
		List<Item> items = feed.getCopiedListOfItems();
		itemLinks = new ArrayList<Link>();
		if (items != null) {
			for (Item item : items) {
				createItemLink(item);
			}
		}
	}

	/**
	 * @param item
	 */
	private void createItemLink(Item item) {
		String guid = item.getGuid();
		Link itemLink_more = LinkFactory.createCustomLink("link.to." + guid, "link.to." + guid, "feed.link.more", Link.LINK, vcItems, this);
		itemLink_more.setIconRightCSS("o_icon o_icon_start");
		itemLink_more.setCustomEnabledLinkCSS("o_link_forward");
		itemLink_more.setUserObject(item);
		
		Link itemLink_title = LinkFactory.createCustomLink("titlelink.to." + guid, "titlelink.to." + guid, StringEscapeUtils.escapeHtml(item.getTitle()), Link.NONTRANSLATED, vcItems, this);
		itemLink_title.setUserObject(item);
		
		itemLinks.add(itemLink_title);
		itemLinks.add(itemLink_more);
	}

	/**
	 * Instantiates the makeInternal and the makeExternal-Buttons and puts it to
	 * the items velocity container's context.
	 */
	public void makeInternalAndExternalButtons() {
		makeInternalButton = LinkFactory.createButton("feed.make.internal", vcItems, this);
		makeExternalButton = LinkFactory.createButton("feed.make.external", vcItems, this);
	}

	/**
	 * @param item
	 */
	private void createButtonsForItem(UserRequest ureq, Item item) {
		String guid = item.getGuid();
		Link editButton = LinkFactory.createCustomLink("feed.edit.item." + guid, "feed.edit.item." + guid, "feed.edit.item",
				Link.BUTTON_SMALL, vcItems, this);
		editButton.setElementCssClass("o_sel_feed_item_edit");
		Link deleteButton = LinkFactory.createCustomLink("delete." + guid, "delete." + guid, "delete", Link.BUTTON_SMALL, vcItems, this);
		deleteButton.setElementCssClass("o_sel_feed_item_delete");

		if(feedResource.isInternal() && getIdentity().getKey() != null && getIdentity().getKey().equals(item.getAuthorKey())) {
			String businessPath = BusinessControlFactory.getInstance().getAsString(getWindowControl().getBusinessControl());
			businessPath += "[item=" + item.getGuid() + ":0]";
			Controller artefactCtrl = EPUIFactory.createArtefactCollectWizzardController(ureq, getWindowControl(), feedResource, businessPath);
			if(artefactCtrl != null) {
				artefactLinks.put(item, artefactCtrl);
				vcItems.put("feed.artefact.item." + guid, artefactCtrl.getInitialComponent());
			}
		}
		
		editButton.setUserObject(item);
		deleteButton.setUserObject(item);
		editButtons.add(editButton);
		deleteButtons.add(deleteButton);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// make sure the lock is released
		FeedManager.getInstance().releaseLock(lock);
		// Dispose confirm deletion dialog controller since it isn't listend to.
		if (confirmDialogCtr != null) {
			removeAsListenerAndDispose(confirmDialogCtr);
		}
		
		if(artefactLinks != null) {
			for(Controller ctrl:artefactLinks.values()) {
				ctrl.dispose();
			}
			artefactLinks.clear();
			artefactLinks = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		FeedManager feedManager = FeedManager.getInstance();
		// feed for this event and make sure the updated feed object is in the view
		Feed feed = feedManager.getFeed(feedResource);
		vcItems.contextPut("feed", feed);
		
		if (source == addItemButton) {
			currentItem = new Item();
			currentItem.setDraft(true);
			currentItem.setAuthorKey(ureq.getIdentity().getKey());
			// Generate new GUID for item, needed for media files that are stored relative to the GUID
			currentItem.setGuid(CodeHelper.getGlobalForeverUniqueID()); 
			// Create item and media containers 
			feedManager.createItemContainer(feed, currentItem);
			itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem, feed);
			activateModalDialog(itemFormCtr, uiFactory.getTranslator().translate("feed.edit.item"));

		} else if (editButtons != null && editButtons.contains(source)) {
			currentItem = (Item) ((Link) source).getUserObject();
			// check if still available, maybe deleted by other user in the meantime
			if (feed.getItems().contains(currentItem)) {
				lock = feedManager.acquireLock(feed, currentItem, getIdentity());
				if (lock.isSuccess()) {
					// reload to prevent stale object, then launch editor
					currentItem = feedManager.getItem(feed, currentItem.getGuid());					
					itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem, feed);
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
			// Reload first, could be stale
			item = feedManager.getItem(feed, item.getGuid());					
			if(item != null) {
				displayItemController(ureq, item);
			}
		} else if (source == makeInternalButton) {
			if (feed.isUndefined()) {
				feedManager.updateFeedMode(Boolean.FALSE, feed);				
			} else if (feed.isExternal()) {
				// Very special case: another user concurrently changed feed to external. Do nothing
				vcItems.setDirty(true);
				return;
			}
			// else nothing to do, already set to internal by a concurrent user
			
			// Add temporary item and open edit dialog
			addItemButton = LinkFactory.createButton("feed.add.item", vcItems, this);
			addItemButton.setElementCssClass("o_sel_feed_item_new");
			currentItem = new Item();
			currentItem.setDraft(true);
			currentItem.setAuthorKey(ureq.getIdentity().getKey());
			// Generate new GUID for item, needed for media files that are stored relative to the GUID
			currentItem.setGuid(CodeHelper.getGlobalForeverUniqueID()); 
			// Create item and media containers 
			feedManager.createItemContainer(feed, currentItem);
			itemFormCtr = uiFactory.createItemFormController(ureq, getWindowControl(), currentItem, feed);
			activateModalDialog(itemFormCtr, uiFactory.getTranslator().translate("feed.edit.item"));
			// do logging
			ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(), LoggingResourceable.wrap(feed));
			

		} else if (source == makeExternalButton) {
			if (feed.isUndefined()) {
				feedManager.updateFeedMode(Boolean.TRUE, feed);
				vcItems.setDirty(true);
				// Ask listening FeedMainController to open and handle a new external
				// feed dialog.
				fireEvent(ureq, HANDLE_NEW_EXTERNAL_FEED_DIALOG_EVENT);
				// do logging
				ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_EDIT, getClass(), LoggingResourceable.wrap(feed));
			} 
			// else nothing to do, already set to external by a concurrent user

		} else if (source == olderItemsLink) {
			helper.olderItems();
			if (callback.mayEditItems() || callback.mayCreateItems()) {
				createEditButtons(ureq, feed);
			}
			createCommentsAndRatingsLinks(ureq, feed);
			vcItems.setDirty(true);

		} else if (source == newerItemsLink) {
			helper.newerItems();
			if (callback.mayEditItems() || callback.mayCreateItems()) {
				createEditButtons(ureq, feed);
			}
			createCommentsAndRatingsLinks(ureq, feed);
			vcItems.setDirty(true);

		} else if (source == startpageLink) {
			helper.startpage();
			if (callback.mayEditItems() || callback.mayCreateItems()) {
				createEditButtons(ureq, feed);
			}
			createCommentsAndRatingsLinks(ureq, feed);
			vcItems.setDirty(true);

		} else if (source instanceof Link) {
			// if it's a link try to get attached identity and assume that user wants
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
		if (!isSameAllItems(feed.getFilteredItems(callback, ureq.getIdentity()))) {
			resetItems(ureq, feed);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		FeedManager feedManager = FeedManager.getInstance();
		// reload feed for this event and make sure the updated feed object is in the view
		Feed feed = feedManager.getFeed(feedResource);
		vcItems.contextPut("feed", feed);

		if (source == cmc) {
			if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(itemFormCtr);
				itemFormCtr = null;
				// Check if this item has ever been added to the feed. If not, remove the temp dir
				cleanupTmpItemMediaDir(currentItem, feed, feedManager);
				// If there were no items and the user doesn't want to save the
				// first item, go back to the decision whether to make the feed
				// internally or subscribe to an external feed.
				if (!feed.hasItems()) {
					feedManager.updateFeedMode(null, feed);
					makeInternalAndExternalButtons();
				}
				//release lock
				feedManager.releaseLock(lock);
			}
		} else if (source == confirmDialogCtr && DialogBoxUIFactory.isYesEvent(event)) {
			// The user confirmed that the item shall be deleted
			Item item = (Item) ((DialogBoxController) source).getUserObject();
			lock = feedManager.acquireLock(feed, item, getIdentity());
			if (lock.isSuccess()) {
				// remove the item from the naviCtr
				naviCtr.remove(item);
				// remove the item also from the helper (cached selection)
				helper.removeItem(item);
				// permanently remove item
				feed = feedManager.remove(item, feed);
				// remove delete and edit buttons of this item
				deleteButtons.remove(source);
				for (Link editButton : editButtons) {
					if (item.equals(editButton.getUserObject())) {
						editButtons.remove(editButton);
						break;
					}
				}
				// If the last item has been deleted, provide buttons for adding
				// items manually or from an external source/feed.
				if (!feed.hasItems()) {
					makeInternalAndExternalButtons();
					// The subscription/feed url from the feed info is obsolete
					fireEvent(ureq, ItemsController.FEED_INFO_IS_DIRTY_EVENT);
				} else {
					if (callback.mayEditItems() || callback.mayCreateItems()) {
						createEditButtons(ureq, feed);
					}
					createCommentsAndRatingsLinks(ureq, feed);
				}
				vcItems.setDirty(true);
				// in case we were in single item view, show all items
				mainPanel.setContent(vcItems);
				feedManager.releaseLock(lock);
				lock = null;				
				// do logging
				ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_DELETE, getClass(), LoggingResourceable.wrap(item));

			} else {
				String fullName = userManager.getUserDisplayName(lock.getOwner());
				showInfo("feed.item.is.being.edited.by", fullName);
			}

		} else if (source == itemFormCtr) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				if (event.equals(Event.CHANGED_EVENT)) {
					FileElement mediaFile = currentItem.getMediaFile();
					if (feedManager.getItemContainer(currentItem, feed) == null) {
						// Ups, deleted in the meantime by someone else
						// remove the item from the naviCtr
						naviCtr.remove(currentItem);
						// remove the item also from the helper (cached selection)
						helper.removeItem(currentItem);
					} else {
						if (!feed.getItems().contains(currentItem)) {
							// Add the modified item if it is not part of the feed
							feed = feedManager.addItem(currentItem, mediaFile, feed);
							if(feed == null) {
								//the item could not be added, is not internal
								feed = feedManager.getFeed(feedResource);
								if(!feed.isInternal() && !feed.isExternal() && !feed.hasItems()) {
									feed = feedManager.updateFeedMode(Boolean.FALSE, feed);
									feed = feedManager.addItem(currentItem, mediaFile, feed);
								}
							}
							
							if(feed != null) {
								createButtonsForItem(ureq, currentItem);
								createItemLink(currentItem);
								// Add date component
								String guid = currentItem.getGuid();
								if(currentItem.getDate() != null) {
									DateComponentFactory.createDateComponentWithYear("date." + guid, currentItem.getDate(), vcItems);
								}
								// Add comments and rating
								createCommentsAndRatingsLink(ureq, feed, currentItem);
								// add it to the navigation controller
								naviCtr.add(currentItem);
								// ... and also to the helper
								helper.addItem(currentItem);
								if (feed.getItems() != null && feed.getItems().size() == 1) {
									// First item added, show feed url (for subscription)
									fireEvent(ureq, ItemsController.FEED_INFO_IS_DIRTY_EVENT);
									// Set the base URI of the feed for the current user. All users
									// have unique URIs.
									helper.setURIs();
								}
								// do logging
								ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_CREATE, getClass(), LoggingResourceable.wrap(currentItem));
							}
						} else {
							// Write item file
							feed = feedManager.updateItem(currentItem, mediaFile, feed);
							// Update current item in the users view, replace in helper cache of
							// current selected items.
							helper.updateItem(currentItem); 
							// Do logging
							ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_EDIT, getClass(), LoggingResourceable.wrap(currentItem));
						}						
					}
					vcItems.setDirty(true);
					// if the current item is displayed, update the view
					if (itemCtr != null) {
						itemCtr.getInitialComponent().setDirty(true);
					}

				} else if (event.equals(Event.CANCELLED_EVENT)) {
					// Check if this item has ever been added to the feed. If not, remove the temp dir
					cleanupTmpItemMediaDir(currentItem, feed, feedManager);
					// If there were no items and the user doesn't want to save the
					// first item, go back to the decision whether to make the feed
					// internally or subscribe to an external feed.
					if (!feed.hasItems()) {
						feedManager.updateFeedMode(null, feed);
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
		} else if (source == naviCtr && event instanceof NavigationEvent) {
			List<? extends Dated> selItems = ((NavigationEvent) event).getSelectedItems();
			List<Item> items = new ArrayList<Item>();
			for (Dated item : selItems) {
				if (item instanceof Item) {
					items.add((Item) item);
				}
			}
			// make sure items are sorted properly
			Collections.sort(items, new ItemPublishDateComparator());
			helper.setSelectedItems(items);
			if (callback.mayEditItems() || callback.mayCreateItems()) {
				createEditButtons(ureq, feed);
			}
			createCommentsAndRatingsLinks(ureq, feed);
			vcItems.setDirty(true);
			mainPanel.setContent(vcItems);

		} else if (source == itemCtr) {
			if (event == Event.BACK_EVENT) {
				mainPanel.setContent(vcItems);
			}
			
		} else if (source instanceof UserCommentsAndRatingsController) {
			UserCommentsAndRatingsController commentsRatingsCtr = (UserCommentsAndRatingsController) source;
			if (event == UserCommentsAndRatingsController.EVENT_COMMENT_LINK_CLICKED) {
				// go to details page
				Item item = (Item) commentsRatingsCtr.getUserObject();
				item = feedManager.getItem(feed, item.getGuid());	
				if(item != null) {
					ItemController myItemCtr = displayItemController(ureq, item);
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(ItemController.ACTIVATION_KEY_COMMENTS);
					myItemCtr.activate(ureq, entries, null);
				}
			}
		}
		
		// Check if someone else added an item, reload everything
		if (feed == null) {
			//do something
		} else if(!isSameAllItems(feed.getFilteredItems(callback, getIdentity()))) {
			resetItems(ureq, feed);
		}
	}

	/**
	 * Private helper to remove any temp media files created for this feed
	 * @param tmpItem
	 * @param feed
	 * @param feedManager
	 */
	private void cleanupTmpItemMediaDir(Item tmpItem, Feed feed, FeedManager feedManager) {
		// Add GUID null check to not accidentally delete the entire feed directory
		// in case there is somewhere a programming error
		if (!feed.getItems().contains(tmpItem) && tmpItem.getGuid() != null) {
			VFSContainer itemContainer = feedManager.getItemContainer(tmpItem, feed);
			if (itemContainer != null) {
				itemContainer.delete();
			}
		}					
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

	/**
	 * Sets the items view dirty.
	 * @param ureq
	 * @param feed the current feed
	 */
	public void resetItems(UserRequest ureq, Feed feed) {
		FeedManager.getInstance().loadItems(feed);
		List<Item> items = feed.getFilteredItems(callback, ureq.getIdentity());
		helper.setSelectedItems(items);
		naviCtr.setDatedObjects(items);
		setAllItemIds(items);
		// Add item details page link
		createItemLinks(feed);
		// Add item user comments link and rating
		if (displayConfig.isShowCRInMinimized()) {
			createCommentsAndRatingsLinks(ureq, feed);
		}
		// Add date components
		createDateComponents(feed);
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
		Controller artefactLink =  getArtefactLinkByUserObject(item);
		FeedManager feedManager = FeedManager.getInstance();
		Feed feed = feedManager.getFeed(feedResource);
		itemCtr = new ItemController(ureq, getWindowControl(), item, feed, helper, uiFactory, callback, editButton, deleteButton, artefactLink, displayConfig);
		listenTo(itemCtr);
		mainPanel.setContent(itemCtr.getInitialComponent());
		return itemCtr;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || feedResource == null) return;
		
		String itemId = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(itemId != null && itemId.startsWith("item=")) {
			itemId = itemId.substring(5, itemId.length());
		}
		int index = feedResource.getItemIds().indexOf(itemId);
		if (index >= 0) {
			Item item = feedResource.getItems().get(index);
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
			if(this == obj) {
				return true;
			}
			if(obj instanceof ItemId) {
				ItemId id = (ItemId)obj;
				return guid.equals(id.guid) && ((lastModification == null && id.lastModification == null) ||
						(lastModification != null && lastModification.equals(id.lastModification)));
			}
			
			return false;
		}
	}
}
