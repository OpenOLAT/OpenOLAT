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
package org.olat.modules.portfolio.ui.media;

import java.io.File;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.ui.PageEditorController;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.MediaMetadataController;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageMediaController extends BasicController implements PageRunElement {
	
	private final ImageComponent imageCmp;
	private final VelocityContainer mainVC;
	
	public ImageMediaController(UserRequest ureq, WindowControl wControl, MediaPart media, MediaRenderingHints hints) {
		this(ureq, wControl, media.getMedia(), hints);
		
		if(StringHelper.containsNonWhitespace(media.getLayoutOptions())) {
			ImageSettings settings = ContentEditorXStream.fromXml(media.getLayoutOptions(), ImageSettings.class);
			if(settings.getAlignment() != null) {
				mainVC.contextPut("alignment", settings.getAlignment().name());
			}
			if(settings.getSize() != null) {
				mainVC.contextPut("size", settings.getSize().name());
			}
			if(StringHelper.containsNonWhitespace(settings.getStyle())) {
				imageCmp.setCssClasses(settings.getStyle());
			}
			
			if(settings.getSize() != null) {
				mainVC.contextPut("imageSizeStyle", settings.getSize().name());
			}
			
			mainVC.contextPut("someId", CodeHelper.getRAMUniqueID());
			
			boolean hasSource = settings.isShowSource() && StringHelper.containsNonWhitespace(media.getMedia().getSource());
			if(hasSource) {
				mainVC.contextPut("source", media.getMedia().getSource());
				mainVC.contextPut("showSource", Boolean.valueOf(settings.isShowSource()));
			}
			
			boolean hasDescriptionToShow = settings.isShowDescription() && StringHelper.containsNonWhitespace(settings.getDescription());
			mainVC.contextPut("showDescription", Boolean.valueOf(hasDescriptionToShow));
			if(hasDescriptionToShow) {
				mainVC.contextPut("description", settings.getDescription());
			}
			
			boolean hasCaptionToShow = StringHelper.containsNonWhitespace(settings.getCaption());
			mainVC.contextPut("showCaption", Boolean.valueOf(hasCaptionToShow));
			if(hasCaptionToShow) {
				mainVC.contextPut("caption", settings.getCaption());
			}
			
			boolean hasTitle = StringHelper.containsNonWhitespace(settings.getTitle());
			mainVC.contextPut("showTitle", Boolean.valueOf(hasTitle));
			if(hasTitle) {
				mainVC.contextPut("title", settings.getTitle());
				ImageTitlePosition position = settings.getTitlePosition() == null ? ImageTitlePosition.above : settings.getTitlePosition();
				mainVC.contextPut("titlePosition", position.name());
				if(StringHelper.containsNonWhitespace(settings.getTitleStyle())) {
					mainVC.contextPut("titleStyle", settings.getTitleStyle());
				}
			}
		}
	}

	public ImageMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl, Util.createPackageTranslator(PageEditorController.class, ureq.getLocale()));

		mainVC = createVelocityContainer("media_image");
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		File mediaFile = new File(mediaDir, media.getRootFilename());
		imageCmp = new ImageComponent(ureq.getUserSession(), "image");
		imageCmp.setMedia(mediaFile);
		imageCmp.setDivImageWrapper(false);
		
		mainVC.put("image", imageCmp);
		mainVC.contextPut("imageSizeStyle", "none");
		Size imageSize = imageCmp.getRealSize();
		if(imageSize != null) {
			mainVC.contextPut("imageSize", imageSize);
		}
		mainVC.contextPut("media", media);
		mainVC.contextPut("extendedMetadata", hints.isExtendedMetadata());

		boolean showCaption = StringHelper.containsNonWhitespace(media.getDescription());
		mainVC.contextPut("showCaption", Boolean.valueOf(showCaption));
		if(showCaption) {
			mainVC.contextPut("caption", media.getDescription());
		}
		if(hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		mainVC.setDomReplacementWrapperRequired(false);
		StackedPanel mainPanel = putInitialPanel(mainVC);
		mainPanel.setDomReplaceable(false);
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
