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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.settings.RepositoryEntryMetadataController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Feb 22, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedMetadataWrapperController extends RepositoryEntryMetadataController {

	@Autowired
	private FeedManager feedManager;

	protected FeedMetadataWrapperController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, entry, readOnly, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Feed feed = feedManager.loadFeed(super.getRepositoryEntry().getOlatResource());
		if (feed != null && feed.isExternal()) {
			uifactory.addStaticTextElement("upload.url", feed.getExternalFeedUrl(), flc);
		}
		super.initForm(formLayout, listener, ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}
}
