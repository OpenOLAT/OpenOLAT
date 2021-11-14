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
package org.olat.modules.ceditor.ui;

import java.io.File;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.model.StoredData;

/**
 * The controller show the image and its metadata, caption, description...<br>
 * The velocity root is hard coded for this class and controller which extends
 * it will have to update the "image.html" template of this package to maintain
 * a consistency across the different frontend of the content editor.
 * 
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageRunController extends BasicController implements PageRunElement {
	
	private final ImageComponent imageCmp;
	protected final VelocityContainer mainVC;
	
	public ImageRunController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, ImageElement media, PageElementRenderingHints hints) {
		this(ureq, wControl, dataStorage, media.getStoredData(), hints);
		
		ImageSettings settings = media.getImageSettings();
		if(settings != null) {
			DublinCoreMetadata meta = null;
			if(media.getStoredData() instanceof DublinCoreMetadata) {
				meta = (DublinCoreMetadata)media.getStoredData();
			}
			updateImageSettings(settings, meta);
		}
	}

	public ImageRunController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, StoredData storedData, PageElementRenderingHints hints) {
		super(ureq, wControl, Util.createPackageTranslator(PageEditorV2Controller.class, ureq.getLocale()));
		velocity_root = Util.getPackageVelocityRoot(ImageRunController.class);

		mainVC = createVelocityContainer("image");
		File mediaFile = dataStorage.getFile(storedData);
		imageCmp = new ImageComponent(ureq.getUserSession(), "image");
		imageCmp.setMedia(mediaFile);
		imageCmp.setDivImageWrapper(false);
		imageCmp.setPreventBrowserCaching(false);
		
		mainVC.put("image", imageCmp);
		mainVC.contextPut("imageSizeStyle", "none");
		Size imageSize = imageCmp.getRealSize();
		if(imageSize != null) {
			mainVC.contextPut("imageSize", imageSize);
		}
		mainVC.contextPut("media", storedData);
		mainVC.contextPut("extendedMetadata", hints.isExtendedMetadata());


		boolean showCaption = StringHelper.containsNonWhitespace(storedData.getDescription());
		mainVC.contextPut("showCaption", Boolean.valueOf(showCaption));
		if(showCaption) {
			mainVC.contextPut("caption", storedData.getDescription());
		}
		
		if(hints.isExtendedMetadata()) {
			initMetadata(ureq, storedData);
		}
		
		mainVC.setDomReplacementWrapperRequired(false);
		putInitialPanel(mainVC);
	}
	
	public void updateImageSettings(ImageSettings settings, DublinCoreMetadata meta) {
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
		
		if(meta != null) {
			boolean hasSource = settings.isShowSource() && StringHelper.containsNonWhitespace(meta.getSource());
			if(hasSource) {
				mainVC.contextPut("source", meta.getSource());
				mainVC.contextPut("showSource", Boolean.valueOf(settings.isShowSource()));
			}
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
	
	/**
	 * @param ureq The user request
	 * @param storedData To be extended 
	 */
	protected void  initMetadata(UserRequest ureq, StoredData storedData) {
		//
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
		if(imageCmp != null) {
			imageCmp.dispose();
		}
        super.doDispose();
	}
}
