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
package org.olat.modules.webFeed.ui;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedChangedEvent;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
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
	private VelocityContainer vcMain;
	private VelocityContainer vcInfo;
	private FeedItemListController feedItemListCtrl;
	private FeedViewHelper helper;
	private final FeedUIFactory feedUIFactory;
	private final FeedSecurityCallback callback;

	private final SubscriptionContext subsContext;
	private final ModuleConfiguration moduleConfig;
	private final FeedItemDisplayConfig displayConfig;
	private final OLATResourceable ores;
	
	@Autowired
	private RepositoryManager repositoryManager;
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
							  FeedSecurityCallback callback, ModuleConfiguration moduleConfig) {
		this(ores, ureq, wControl, null, null, uiFactory, callback, null, moduleConfig);
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
							  FeedUIFactory uiFactory, FeedSecurityCallback callback,
							  FeedItemDisplayConfig displayConfig, ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		this.feedUIFactory = uiFactory;
		this.callback = callback;
		this.ores = ores;
		this.displayConfig = Objects.requireNonNullElseGet(displayConfig,
				() -> new FeedItemDisplayConfig(true, true, true));
		this.moduleConfig = moduleConfig;
		
		subsContext = callback.getSubscriptionContext();


		setTranslator(uiFactory.getTranslator());
		feed = feedManager.loadFeed(ores);

		if(feed == null) {
			vcMain = createVelocityContainer("feed_error");
			vcMain.contextPut("errorMessage", translate("feed.error"));
			putInitialPanel(vcMain);
		} else {
			RepositoryEntry feedEntry = repositoryManager.lookupRepositoryEntry(feed, false);
			if (RepositoryEntryStatusEnum.deleted == feedEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == feedEntry.getEntryStatus()) {
				EmptyStateConfig emptyState = EmptyStateConfig.builder()
						.withIconCss("o_icon o_" + feed.getResourceableTypeName().replace(".", "-") + "_icon")
						.withIndicatorIconCss("o_icon_deleted")
						.withMessageI18nKey("error.feed.deleted")
						.build();
				EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
				emptyStateCmp.setTranslator(uiFactory.getTranslator());
				putInitialPanel(emptyStateCmp);
				return;
			}

			helper = new FeedViewHelper(feed, getIdentity(), ureq.getUserSession().getRoles(), uiFactory.getTranslator(), courseId, nodeId);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), feed);
			display(ureq, wControl);
			// do logging
			ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_READ, getClass(), LoggingResourceable.wrap(feed));
		}
	}

	@Override
	protected void doDispose() {
		if(feed != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, feed);
		}
        super.doDispose();
	}

	/**
	 * Sets up the velocity container for displaying the view
	 *
	 * @param ureq
	 * @param wControl
	 */
	private void display(UserRequest ureq, WindowControl wControl) {
		vcMain = createVelocityContainer("feed_main");

		vcInfo = feedUIFactory.createInfoVelocityContainer(this);
		setInfos(feed, ureq, wControl);

		putInitialPanel(vcMain);
	}

	protected void setInfos(Feed feed, UserRequest ureq, WindowControl wControl) {
		this.feed = feed;
		// update helper uris with given feed
		helper.setURIs(feed);

		vcInfo.contextPut("feed", feed);
		vcInfo.contextPut("helper", helper);
		vcInfo.contextPut("suppressCache", "");
		// if there is no moduleConfig (e.g. learning resource itself) then always show metadata
		if (moduleConfig != null) {
			vcInfo.contextPut("showFeedDesc", moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_DESC, false));
			vcInfo.contextPut("showFeedTitle", moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_TITLE, false));
			vcInfo.contextPut("showFeedImage", moduleConfig.getBooleanSafe(AbstractFeedCourseNode.CONFIG_KEY_SHOW_FEED_IMAGE, false));
		}

		if (subsContext != null) {
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData data = new PublisherData(ores.getResourceableTypeName(), ores.getResourceableId().toString(), businessPath);
			ContextualSubscriptionController cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
			listenTo(cSubscriptionCtrl);
			vcInfo.put("subscription", cSubscriptionCtrl.getInitialComponent());
		}

		if (callback.mayEditMetadata()) {
			Link editFeedButton = LinkFactory.createButtonSmall("feed.edit", vcInfo, this);
			editFeedButton.setElementCssClass("o_sel_feed_edit");
		}

		vcInfo.contextPut("callback", callback);

		DisplayFeedUrlController displayUrlCtrl = new DisplayFeedUrlController(ureq, wControl, feed, helper, feedUIFactory.getTranslator());
		listenTo(displayUrlCtrl);
		vcInfo.put("feedUrlComponent", displayUrlCtrl.getInitialComponent());

		vcMain.put("info", vcInfo);

		feedItemListCtrl = new FeedItemListController(ureq, wControl, feed, callback, feedUIFactory, vcMain, vcInfo, displayConfig, helper);
		listenTo(feedItemListCtrl);

		vcMain.put("items", feedItemListCtrl.getInitialComponent());
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// feed for this event and make sure the updated feed object is in the view
		feed = feedManager.loadFeed(feed);
		vcInfo.contextPut("feed", feed);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		Item item = null;
		String itemId = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(itemId != null && itemId.startsWith("item=")) {
			itemId = itemId.substring(5);
			try {
				Long itemKey = Long.parseLong(itemId);
				item = FeedManager.getInstance().loadItem(itemKey);
			} catch (Exception e) {
				item = FeedManager.getInstance().loadItemByGuid(itemId);
			}
		}
		if (item != null) {
			feedItemListCtrl.displayFeedItem(ureq, item);
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent ojde) {
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(feed, true)) {
				dispose();
			}
		} else if (event instanceof FeedChangedEvent fce && (fce.getFeedKey().equals(feed.getKey()))) {
			feed = feedManager.loadFeed(feed);
			vcInfo.contextPut("suppressCache", "&" + ZonedDateTime.now().toInstant().toEpochMilli());
			vcInfo.contextPut("feed", feed);
			vcInfo.setDirty(true);
		}
	}

	protected FeedUIFactory getFeedUIFactory() {
		return feedUIFactory;
	}
}
