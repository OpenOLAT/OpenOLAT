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

import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This Controller is responsible for displaying a single feed item for reading.
 * 
 * <P>
 * Initial Date: Sep 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedItemController extends BasicController implements Activateable2 {

	public static final String ACTIVATION_KEY_COMMENTS = "comments";
	private final Link backLink;
	private Link artefactLink;
	private Link editLink;
	private Link deleteLink;
	private final Item item;
	private UserCommentsAndRatingsController commentsCtrl;

	@Autowired
	private PortfolioV2Module portfolioModule;

	/**
	 * @param ureq
	 * @param wControl
	 * @param displayConfig 
	 */
	public FeedItemController(UserRequest ureq, WindowControl wControl, Item item, Feed feed, FeedViewHelper helper, FeedUIFactory feedUIFactory,
							  FeedSecurityCallback callback, FeedItemDisplayConfig displayConfig, VelocityContainer vcItem) {
		super(ureq, wControl);
		// using because each feed type has its own translations
		setTranslator(feedUIFactory.getTranslator());
		this.item = item;
		vcItem.contextPut("item", item);
		vcItem.contextPut("feed", feed);
		vcItem.contextPut("helper", helper);
		vcItem.contextPut("callback", callback);

		boolean ownFeedItem = getIdentity().getKey() != null
				&& item.getAuthorKey() != null
				&& getIdentity().getKey().equals(item.getAuthorKey());
		if (feed.isInternal()) {
			if (ownFeedItem && portfolioModule.isEnabled()) {
				artefactLink = LinkFactory.createLink("artefactButton", "artefactButton", "artefact", "feed.item.artefact", getTranslator(), vcItem, this, Link.BUTTON);
				artefactLink.setTitle("feed.item.artefact");
				artefactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eportfolio_add");
				artefactLink.setGhost(true);
			}
			if (callback.mayEditItems() || ownFeedItem) {
				editLink = LinkFactory.createLink("editButton", "editButton", "edit", "feed.item.edit", getTranslator(), vcItem, this, Link.BUTTON);
				editLink.setTitle("feed.item.edit");
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				editLink.setGhost(true);
			}
			if (callback.mayDeleteItems() || ownFeedItem) {
				deleteLink = LinkFactory.createLink("deleteButton", "deleteButton", "delete","delete", getTranslator(), vcItem, this, Link.BUTTON);
				deleteLink.setTitle("delete");
				deleteLink.setGhost(true);
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");
			}
		}

		backLink = LinkFactory.createLink("back.link", "backLink", getTranslator(), vcItem, this, Link.LINK_BACK);
		// Add date component
		if(item.getDate() != null) {
			DateComponentFactory.createDateComponentWithYear("dateComp", item.getDate(), vcItem);
		}

		// Add rating and commenting controller - only when configured
		if (displayConfig.isShowCRInDetails()) {
			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), callback.mayEditMetadata(), anonym);
			//ratings
			UserCommentsAndRatingsController ratingsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), feed, item.getGuid(), secCallback, null, false, secCallback.canRate(), true);
			listenTo(ratingsCtrl);
			vcItem.put("ratings", ratingsCtrl.getInitialComponent());

			//comments
			commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), feed, item.getGuid(), secCallback, null, secCallback.canViewComments(), false, true);
			listenTo(commentsCtrl);
			vcItem.put("comments", commentsCtrl.getInitialComponent());
		}

		//
		putInitialPanel(vcItem);
		// do logging
		ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_READ, getClass(), LoggingResourceable.wrap(item));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == artefactLink) {
			fireEvent(ureq, new FeedItemEvent(FeedItemEvent.ARTEFACT_FEED_ITEM, item));
		} else if (source == editLink) {
			fireEvent(ureq, new FeedItemEvent(FeedItemEvent.EDIT_FEED_ITEM, item));
		} else if (source == deleteLink) {
			fireEvent(ureq, new FeedItemEvent(FeedItemEvent.DELETE_FEED_ITEM, item));
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if (ACTIVATION_KEY_COMMENTS.equals(type) && commentsCtrl != null) {
				// show comments
				commentsCtrl.expandComments(ureq);
			}
		}
	}
}