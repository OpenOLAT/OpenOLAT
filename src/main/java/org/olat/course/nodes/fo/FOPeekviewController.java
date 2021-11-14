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
package org.olat.course.nodes.fo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
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
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.MessagePeekview;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> The forum peekview controller displays the configurable
 * amount of the newest forum messages.
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
public class FOPeekviewController extends BasicController implements Controller {
	// the current course node id
	private final String nodeId;
	private final Link allItemsLink;
	private final RepositoryEntry courseEntry;

	@Autowired
	private ForumManager forumManager;

	/**
	 * Constructor
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param forum The forum instance
	 * @param nodeId The course node ID
	 * @param itemsToDisplay number of items to be displayed, must be > 0
	 */
	public FOPeekviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, Forum forum, String nodeId, int itemsToDisplay) {		
		// Use fallback translator from forum
		super(ureq, wControl, Util.createPackageTranslator(Forum.class, ureq.getLocale()));
		this.nodeId = nodeId;
		this.courseEntry = courseEntry;
	
		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		// add items, only as many as configured
		List<MessagePeekview> messages = forumManager.getPeekviewMessages(forum, itemsToDisplay);
		// only take the configured amount of messages
		List<MessageView> views = new ArrayList<>(itemsToDisplay);
		for (MessagePeekview message :messages) {
			// add link to item
			// Add link to jump to course node
			Link nodeLink = LinkFactory.createLink("nodeLink_" + message.getKey(), peekviewVC, this);
			nodeLink.setCustomDisplayText(StringHelper.escapeHtml(message.getTitle()));
			nodeLink.setIconLeftCSS("o_icon o_icon_post");
			nodeLink.setCustomEnabledLinkCSS("o_gotoNode");
			nodeLink.setUserObject(message);	
			
			String body = message.getBody();
			if(body.length() > 256) {
				String truncateBody = FilterFactory.getHtmlTagsFilter().filter(body);
				truncateBody = StringHelper.unescapeHtml(truncateBody);// remove entities
				if(truncateBody.length() < 256) {
					body = StringHelper.xssScan(body);
				} else {
					truncateBody = Formatter.truncate(truncateBody, 256);// truncate
					body = StringHelper.escapeHtml(truncateBody);//ok because html tags are filtered
				}
			} else {
				body = StringHelper.xssScan(body);
			}
			views.add(new MessageView(message.getKey(), message.getCreationDate(), body));
		}
		peekviewVC.contextPut("messages", views);
		// Add link to show all items (go to node)
		allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
		allItemsLink.setIconRightCSS("o_icon o_icon_start");
		allItemsLink.setCustomEnabledLinkCSS("pull-right");
		// Add Formatter for proper date formatting
		peekviewVC.contextPut("formatter", Formatter.getInstance(getLocale()));
		putInitialPanel(peekviewVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(allItemsLink == source) {
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
		} else if (source instanceof Link) {
			Link nodeLink = (Link) source;
			Object uobject = nodeLink.getUserObject();
			if (uobject instanceof MessagePeekview) {
				MessagePeekview message = (MessagePeekview)uobject;
				String businessPath = "[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + nodeId + "][Message:" + message.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
			}
		}
	}

	public static class MessageView {
		
		private final Long key;
		private final String body;
		private final Date creationDate;
		
		public MessageView(Long key, Date creationDate, String body) {
			this.key = key;
			this.body = body;
			this.creationDate = creationDate;
		}
		
		public Long getKey() {
			return key;
		}
		
		public Date getCreationDate() {
			return creationDate;
		}

		public String getBody() {
			return body;
		}
	}
}