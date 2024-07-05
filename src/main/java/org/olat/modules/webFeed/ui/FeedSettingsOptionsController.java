/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jul 04, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedSettingsOptionsController extends FormBasicController {

	private final Feed feed;
	private final boolean readOnly;

	private FormToggle ratingToggle;
	private FormToggle commentToggle;

	@Autowired
	private FeedManager feedManager;

	protected FeedSettingsOptionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		this.feed = feedManager.loadFeed(entry.getOlatResource());
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.config.title");

		ratingToggle = uifactory.addToggleButton("feed.rate.entries.toggle", "feed.rate.entries.toggle", translate("on"), translate("off"), formLayout);
		commentToggle = uifactory.addToggleButton("feed.comment.entries.toggle", "feed.comment.entries.toggle", translate("on"), translate("off"), formLayout);
		ratingToggle.toggle(feed.getCanRate());
		commentToggle.toggle(feed.getCanComment());

		if (readOnly) {
			ratingToggle.setEnabled(false);
			commentToggle.setEnabled(false);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormToggle) {
			updateFeedWithFeedback();
		}
	}

	private void updateFeedWithFeedback() {
		feed.setCanRate(ratingToggle.isOn());
		feed.setCanComment(commentToggle.isOn());
		feedManager.updateFeed(feed);
	}
}
