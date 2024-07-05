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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.settings.RepositoryEntryMetadataController;

/**
 * Initial date: Feb 22, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedMetadataWrapperController extends BasicController {

	protected FeedMetadataWrapperController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);

		FeedMetadataEditFormController feedMetadataEditFormCtrl = new FeedMetadataEditFormController(ureq, wControl, entry);
		RepositoryEntryMetadataController repoMetaDataController = new RepositoryEntryMetadataController(ureq, wControl, entry, readOnly, false);

		VelocityContainer wrapper = createVelocityContainer("metadata_wrapper");

		wrapper.put("feedMetadata", feedMetadataEditFormCtrl.getInitialComponent());
		wrapper.put("repoMetadata", repoMetaDataController.getInitialComponent());

		putInitialPanel(wrapper);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}
}
