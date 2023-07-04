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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.ImageRunController;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaMetadataController;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageMediaController extends ImageRunController {
	
	public ImageMediaController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, MediaPart element, RenderingHints hints) {
		super(ureq, wControl, dataStorage, element, hints);
	}

	public ImageMediaController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, MediaVersion version, RenderingHints hints) {
		super(ureq, wControl, dataStorage, version, hints);
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(PageEditorV2Controller.class, getLocale(), getTranslator()));
		
		Media media = version.getMedia();
		if(hints.isExtendedMetadata()) {
			initMetadata(ureq, media);
		}
		
		Object showCaption = mainVC.contextGet("showCaption");
		if(showCaption == null || (showCaption instanceof Boolean sCaption && !sCaption.booleanValue())) {
			boolean showDescription = StringHelper.containsNonWhitespace(media.getDescription());
			mainVC.contextPut("showCaption", Boolean.valueOf(showDescription));
			if(showDescription) {
				mainVC.contextPut("caption", media.getDescription());
			}
		}
	}

	protected void initMetadata(UserRequest ureq, Media media) {
		MediaMetadataController metaCtrl = new MediaMetadataController(ureq, getWindowControl(), media);
		listenTo(metaCtrl);
		mainVC.put("meta", metaCtrl.getInitialComponent());
	}
}
