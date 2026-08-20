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
package org.olat.repository.ui.list;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryManager;

/**
 * The teaser image/video, split out of the header so it can be placed as its
 * own grid item in the aside column, independent of the header's own height.
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageTeaserImageController extends BasicController {

	private final boolean hasContent;

	public InfoPageTeaserImageController(UserRequest ureq, WindowControl wControl, InfoPageData data) {
		super(ureq, wControl);

		VFSLeaf image = data.getTeaserImage();
		VFSLeaf movie = data.getTeaserMovie();
		hasContent = image != null || movie != null;

		if (hasContent) {
			ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
			if (movie != null) {
				ic.setMedia(movie);
				ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
				if (image != null) {
					ic.setPoster(image);
				}
			} else {
				ic.setMedia(image);
				ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
			}
			putInitialPanel(ic);
		} else {
			putInitialPanel(new Panel("empty"));
		}
	}

	public boolean hasContent() {
		return hasContent;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
