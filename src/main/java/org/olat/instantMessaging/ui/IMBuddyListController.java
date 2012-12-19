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
package org.olat.instantMessaging.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IMBuddyListController extends BasicController {
	
	private Link toggleOffline, toggleGroups; 
	private final VelocityContainer mainVC;
	private final VelocityContainer buddiesListContent;

	private Roster buddyList;
	private ViewMode viewMode;
	
	private final InstantMessagingModule imModule;
	private final InstantMessagingService imService;
	
	public IMBuddyListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		
		mainVC = createVelocityContainer("buddies");
		buddiesListContent = createVelocityContainer("buddies_content");
		
		if(imModule.isOnlineUsersEnabled()) {
			toggleOffline = LinkFactory.createCustomLink("toggleOnline", "cmd.online", "", Link.NONTRANSLATED, mainVC, this);
			toggleOffline.setCustomDisplayText(translate("im.show.online.buddies"));
			toggleOffline.setCustomEnabledLinkCSS("o_instantmessaging_showofflineswitch");
			viewMode = ViewMode.onlineUsers;
		}
		
		if(imModule.isGroupEnabled() || imModule.isCourseEnabled()) {
			toggleGroups = LinkFactory.createCustomLink("toggleGroups", "cmd.groups", "", Link.NONTRANSLATED, mainVC, this);
			toggleGroups.setCustomDisplayText(getTranslator().translate("im.show.groups"));
			toggleGroups.setCustomEnabledLinkCSS("o_instantmessaging_showgroupswitch");
			viewMode = ViewMode.groups;
		}

		buddyList = new Roster(getIdentity().getKey());
		mainVC.contextPut("buddyList", buddyList);
		buddiesListContent.contextPut("buddyList", buddyList);
		loadRoster(viewMode);
		mainVC.put("buddiesListContent", buddiesListContent);
		putInitialPanel(mainVC);
	}
	
	private String getStatusCss(String status) {
		return "o_instantmessaging_" + status + "_icon";
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {	
		//buddies list
		if (source == toggleOffline) {
			if (viewMode == ViewMode.groups) {
				toggleOffline.setCustomDisplayText(translate("im.hide.offline.buddies"));
				loadRoster(ViewMode.onlineUsers);
			} else {
				toggleOffline.setCustomDisplayText(translate("im.show.offline.buddies"));
				loadRoster(ViewMode.groups);
			}
		} else if (source == toggleGroups) {
			if (viewMode == ViewMode.onlineUsers) {
				toggleGroups.setCustomDisplayText(translate("im.hide.groups"));
				loadRoster(ViewMode.groups);
			} else {
				toggleGroups.setCustomDisplayText(translate("im.show.groups"));
				loadRoster(ViewMode.onlineUsers);
			}	
		} else if (source instanceof Link) {
			Link link = (Link)source;
			if("cmd.buddy".equals(link.getCommand())) {
				Buddy buddy = (Buddy)link.getUserObject();
				fireEvent(ureq, new OpenInstantMessageEvent(ureq, buddy));
			}
		}
	}
	
	private void loadRoster(ViewMode mode) {
		this.viewMode = mode;

		buddyList.clear();
		if(viewMode == ViewMode.onlineUsers) {
			if(imModule.isViewOnlineUsersEnabled()) {
				buddyList.addBuddies(imService.getOnlineBuddies());
			}
		} else {
			buddyList.addBuddies(imService.getBuddies(getIdentity()));
		}
		
		for(Buddy buddy:buddyList.getEntries()) {
			String linkId = "buddy_" + buddy.getIdentityKey();
			if(buddiesListContent.getComponent(linkId) == null) {
				Link buddyLink = LinkFactory.createCustomLink(linkId, "cmd.buddy", "", Link.NONTRANSLATED, buddiesListContent, this);
				buddyLink.setCustomDisplayText(buddy.getName());
				buddyLink.setCustomEnabledLinkCSS(getStatusCss(buddy.getStatus()));
				buddyLink.setUserObject(buddy);
			}
		}
		buddiesListContent.setDirty(true);
	}
	
	private enum ViewMode {
		onlineUsers,
		groups,
	}
}