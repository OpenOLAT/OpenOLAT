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
package org.olat.course.nodes.feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.resource.OLATResource;

/**
 * <h3>Description:</h3> The feed peekview controller displays the configurable
 * amount of the most recent feed items.
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>OlatCmdEvent to notify that a jump to the course node is desired</li>
 * </ul>
 * <p>
 * Initial Date: 29.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class FeedPeekviewController extends BasicController implements Controller {
	// the current course node id
	private final String nodeId;

	/**
	 * Constructor for the feed peekview controller
	 * 
	 * @param olatResource The feed olat resource
	 * @param ureq the User request
	 * @param wControl The window control
	 * @param callback the feed security callback
	 * @param courseId The course ID in which the feed is used
	 * @param nodeId The current course node ID
	 * @param feedUIFactory The feed UI factory
	 * @param itemsToDisplay number of items to be displayed, must be > 0
	 * @param wrapperCssClass An optional wrapper CSS class that is added to the
	 *          wrapper DIV to style icons etc
	 */
	public FeedPeekviewController(OLATResource olatResource, UserRequest ureq, WindowControl wControl, FeedSecurityCallback callback,
			Long courseId, String nodeId, FeedUIFactory feedUIFactory, int itemsToDisplay, String wrapperCssClass) {
		super(ureq, wControl);
		this.nodeId = nodeId;
		Feed feed = FeedManager.getInstance().loadFeed(olatResource);

		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		if(feed == null) {
			peekviewVC.contextPut("items", new ArrayList<>(1));
			peekviewVC.contextPut("errorMessage", translate("peekview.error"));
		} else {
			peekviewVC.contextPut("wrapperCssClass", wrapperCssClass != null ? wrapperCssClass : "");
			// add gui helper
			FeedViewHelper helper = new FeedViewHelper(feed, getIdentity(), ureq.getUserSession().getRoles(), getTranslator(), courseId, nodeId);
			peekviewVC.contextPut("helper", helper);
			// add items, only as many as configured
			List<Item> allItems = FeedManager.getInstance().loadFilteredAndSortedItems(feed, Collections.emptyList(), callback, getIdentity());
			List<Item> items = new ArrayList<>();
			for (int i = 0; i < allItems.size(); i++) {
				if (items.size() == itemsToDisplay) {
					break;
				}
				// add item itself if published
				Item item = allItems.get(i);
				if (item.isPublished()) {
					items.add(item);
					// add link to item
					// Add link to jump to course node
					Link nodeLink = LinkFactory.createLink("nodeLink_" + item.getGuid(), peekviewVC, this);
					nodeLink.setCustomDisplayText(StringHelper.escapeHtml(item.getTitle()));
					nodeLink.setIconLeftCSS("o_icon o_" + feed.getResourceableTypeName().replace(".", "-") + "_icon");
					nodeLink.setCustomEnabledLinkCSS("o_gotoNode");
					nodeLink.setUserObject(Long.toString(item.getKey()));
				}
			}
			peekviewVC.contextPut("items", items);
			// Add link to show all items (go to node)
			Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
			allItemsLink.setIconRightCSS("o_icon o_icon_start");
			allItemsLink.setCustomEnabledLinkCSS("pull-right");
			// Add Formatter for proper date formatting
			peekviewVC.contextPut("formatter", Formatter.getInstance(getLocale()));
		}
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link nodeLink = (Link) source;
			String itemId = (String) nodeLink.getUserObject();
			if (itemId == null) {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
			} else {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId + "/" + itemId));
			}
		}
	}

}
