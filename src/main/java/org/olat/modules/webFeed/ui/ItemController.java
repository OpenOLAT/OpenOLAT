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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * This Controller is responsible for displaying a single feed item for reading.
 * 
 * <P>
 * Initial Date: Sep 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class ItemController extends BasicController implements Activateable2 {
	public static final String ACTIVATION_KEY_COMMENTS = "comments";
	private Link backLink;
	private UserCommentsAndRatingsController commentsCtr;

	/**
	 * @param ureq
	 * @param wControl
	 * @param displayConfig 
	 */
	public ItemController(UserRequest ureq, WindowControl wControl, Item item, Feed feed, FeedViewHelper helper, FeedUIFactory uiFactory,
			FeedSecurityCallback callback, Link editButton, Link deleteButton, Controller artefactLink, FeedItemDisplayConfig displayConfig) {
		super(ureq, wControl);
		setTranslator(uiFactory.getTranslator());
		VelocityContainer vcItem = uiFactory.createItemVelocityContainer(this);
		vcItem.contextPut("item", item);
		vcItem.contextPut("feed", feed);
		vcItem.contextPut("helper", helper);
		vcItem.contextPut("callback", callback);
		if (feed.isInternal()) {
			if (editButton != null) {
				vcItem.put("editButton", editButton);
			}
			if (deleteButton != null) {
				vcItem.put("deleteButton", deleteButton);
			}
			if (artefactLink != null) {
				vcItem.put("artefactLink", artefactLink.getInitialComponent());
			}
		}
		backLink = LinkFactory.createLinkBack(vcItem, this);
		// Add date component
		if(item.getDate() != null) {
			DateComponentFactory.createDateComponentWithYear("dateComp", item.getDate(), vcItem);
		}
		// Add rating and commenting controller - only when configured
		if (displayConfig.isShowCRInDetails()) {
			boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
			CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), callback.mayEditMetadata(), anonym);
			commentsCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), feed, item.getGuid(), secCallback, null, true, true, true);
			listenTo(commentsCtr);
			vcItem.put("commentsAndRating", commentsCtr.getInitialComponent());				
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
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ACTIVATION_KEY_COMMENTS.equals(type)) {
			// show comments
			if (commentsCtr != null) {
				commentsCtr.expandComments(ureq);
			}
		}		
	}
}