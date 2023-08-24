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
package org.olat.modules.cemedia.ui.medias;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler.AddSettings;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.AddMediaEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractAddController extends BasicController {
	
	protected final MediaHandler mediaHandler;
	protected final AddSettings settings;

	protected CloseableModalController cmc;
	protected RepositoryEntryShareController shareCtrl;
	
	protected Media mediaReference;
	
	@Autowired
	private MediaService mediaService;
	
	AbstractAddController(UserRequest ureq, WindowControl wControl, MediaHandler mediaHandler, AddSettings settings) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale()));
		this.mediaHandler = mediaHandler;
		this.settings = settings;
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(shareCtrl);
		removeAsListenerAndDispose(cmc);
		shareCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(shareCtrl == source) {
			mediaReference = mediaService.getMediaByKey(shareCtrl.getMedia().getKey());
			fireEvent(ureq, new AddMediaEvent(false));
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected boolean proposeSharing(Media media) {
		if(settings.baseRepositoryEntry() == null) {
			return false;
		}
		
		if(mediaService.isMediaEditable(getIdentity(), media)) {
			List<MediaShare> shares = mediaService.getMediaShares(media, settings.baseRepositoryEntry());
			return shares.isEmpty();
		}
		return false;
	}
	
	protected void confirmSharing(UserRequest ureq, Media media) {
		shareCtrl = new RepositoryEntryShareController(ureq, getWindowControl(),
				media, settings.baseRepositoryEntry());
		listenTo(shareCtrl);
		
		String title = translate("share.confirm.title." + media.getType());
		cmc = new CloseableModalController(getWindowControl(), null, shareCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
