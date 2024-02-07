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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedResourceSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.08.2014<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class FeedRuntimeController extends RepositoryEntryRuntimeController {

	private Link externalUrlLink;
	private Feed feedResource;

	private final FeedMainController feedMainCtrl;
	private ExternalUrlController externalUrlCtrl;
	private CloseableModalController cmc;

	@Autowired
	protected FeedManager feedManager;

	public FeedRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
								 RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);

		if (getRuntimeController() instanceof FeedMainController feedMainController) {
			feedMainCtrl = feedMainController;
		} else {
			feedMainCtrl = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		// reload feed for this event and make sure the updated feed object is in the view
		feedResource = feedManager.loadFeed(feedResource);

		if (source instanceof FeedMainController) {
			if (event instanceof ReloadSettingsEvent reloadSettingsEvent) {
				processReloadSettingsEvent(reloadSettingsEvent);
			}
		} else if (source == externalUrlCtrl && event == Event.CHANGED_EVENT) {
			String externalUrl = externalUrlCtrl.getExternalFeedUrlEl();
			feedManager.updateExternalFeedUrl(feedResource, externalUrl);
			cmc.deactivate();
		}
		removeAsListenerAndDispose(cmc);
		cmc = null;
		removeAsListenerAndDispose(externalUrlCtrl);
		externalUrlCtrl = null;

		// reload everything
		if (feedMainCtrl != null && feedResource != null) {
			feedMainCtrl.getItemsCtrl().resetItems(ureq, feedResource);
		}

		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (externalUrlLink == source && feedMainCtrl != null) {
			doChangeExternalUrl(ureq);
		}
		super.event(ureq, source, event);
	}

	private void doChangeExternalUrl(UserRequest ureq) {
		FeedUIFactory feedUIFactory = feedMainCtrl.getFeedUIFactory();
		externalUrlCtrl = feedUIFactory.createExternalUrlController(ureq, getWindowControl(), feedResource);
		listenTo(externalUrlCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), externalUrlCtrl.getInitialComponent(),
				true, translate("feed.external.url"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void initToolsMenuEdition(Dropdown toolsDropdown) {
		FeedSecurityCallback callback = new FeedResourceSecurityCallback(reSecurity.isEntryAdmin());
		feedResource = feedManager.loadFeed(getOlatResourceable());
		if (toolsDropdown.size() > 0) {
			toolsDropdown.addComponent(new Dropdown.Spacer("copy-download"));
		}
		if (callback.mayCreateItems() && feedResource.isExternal()) {
			externalUrlLink = LinkFactory.createToolLink("feedExternalUrl", translate("feed.external.url"),
					this, "o_icon o_icon-fw o_icon_link");
			externalUrlLink.setElementCssClass("o_sel_feed_item_new");
			toolsDropdown.addComponent(externalUrlLink);
		}
		super.initToolsMenuEdition(toolsDropdown);
	}

	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new FeedSettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, refreshedEntry);
	}


}