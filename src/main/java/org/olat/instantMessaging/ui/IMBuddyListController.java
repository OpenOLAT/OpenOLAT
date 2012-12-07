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
import org.olat.core.util.WebappHelper;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IMBuddyListController extends BasicController {
	
	private final Link toggleOffline, toggleGroups; 
	private final VelocityContainer buddiesList;
	private final VelocityContainer buddiesListContent;

	private int toggleOfflineMode;
	private int toggleGroupsMode;
	private Roster buddyList;
	private final InstantMessagingService imService;
	
	public IMBuddyListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		
		buddiesList = createVelocityContainer("buddies");
		buddiesListContent = createVelocityContainer("buddies_content");
		
		toggleOffline = LinkFactory.createCustomLink("toggleOffline", "cmd.offline", "", Link.NONTRANSLATED, buddiesList, this);
		toggleOffline.setCustomDisplayText(getTranslator().translate("im.show.offline.buddies"));
		toggleOffline.setCustomEnabledLinkCSS("o_instantmessaging_showofflineswitch");
		
		toggleGroups = LinkFactory.createCustomLink("toggleGroups", "cmd.groups", "", Link.NONTRANSLATED, buddiesList, this);
		toggleGroups.setCustomDisplayText(getTranslator().translate("im.show.groups"));
		toggleGroups.setCustomEnabledLinkCSS("o_instantmessaging_showgroupswitch");
		
		buddiesList.contextPut("contextpath", WebappHelper.getServletContextPath());
		buddiesList.contextPut("lang", ureq.getLocale().toString());
		
		buddyList = new Roster();
		buddyList.addBuddies(imService.getBuddies(getIdentity()));
		buddiesList.contextPut("buddyList", buddyList);
		buddiesListContent.contextPut("buddyList", buddyList);
		for(RosterEntry buddy:buddyList.getEntries()) {
			Link buddyLink = LinkFactory.createCustomLink("buddy_" + buddy.getIdentityKey(), "cmd.buddy", "", Link.NONTRANSLATED, buddiesListContent, this);
			buddyLink.setCustomDisplayText(buddy.getName());
			buddyLink.setCustomEnabledLinkCSS(getStatusCss(buddy.getStatus()));
			buddyLink.setUserObject(buddy);
		}
		buddiesList.put("buddiesListContent", buddiesListContent);
		
		putInitialPanel(buddiesList);
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
			if (toggleOfflineMode == 0) {
				toggleOffline.setCustomDisplayText(translate("im.hide.offline.buddies"));
				toggleOfflineMode = 1;
			} else {
				toggleOffline.setCustomDisplayText(translate("im.show.offline.buddies"));
				toggleOfflineMode = 0;
			}
			buddiesListContent.setDirty(true);
			
		} else if (source == toggleGroups) {
			if (toggleGroupsMode == 0) {
				toggleGroups.setCustomDisplayText(translate("im.hide.groups"));
				toggleGroupsMode = 1;
			} else {
				toggleGroups.setCustomDisplayText(translate("im.show.groups"));
				toggleGroupsMode = 0;
			}
			buddiesListContent.setDirty(true);
			
		} else if (source instanceof Link) {
			Link link = (Link)source;
			if("cmd.buddy".equals(link.getCommand())) {
				RosterEntry buddy = (RosterEntry)link.getUserObject();
				fireEvent(ureq, new OpenInstantMessageEvent(ureq, buddy));
			}
		}
	}
}