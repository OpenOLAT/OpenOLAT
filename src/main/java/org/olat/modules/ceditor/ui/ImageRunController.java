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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageSize;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.MediaVersion;

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

	private final static String DEFAULT_STYLE = "o_image_classic";

	private final ImageComponent imageCmp;
	protected final VelocityContainer mainVC;
	
	private final DataStorage dataStorage;

	private final boolean inForm;

	public ImageRunController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, ImageElement media, RenderingHints hints, boolean inForm) {
		this(ureq, wControl, dataStorage, media.getStoredData(), hints, inForm);

		updateImageSettings(media);
	}

	public ImageRunController(UserRequest ureq, WindowControl wControl, DataStorage dataStorage, StoredData storedData, RenderingHints hints, boolean inForm) {
		super(ureq, wControl, Util.createPackageTranslator(PageEditorV2Controller.class, ureq.getLocale()));
		velocity_root = Util.getPackageVelocityRoot(ImageRunController.class);
		this.dataStorage = dataStorage;
		this.inForm = inForm;

		mainVC = createVelocityContainer("image");
		setBlockLayoutClass(null);
		mainVC.setDomReplacementWrapperRequired(false);
		imageCmp = new ImageComponent(ureq.getUserSession(), "image");
		imageCmp.setDivImageWrapper(false);
		imageCmp.setPreventBrowserCaching(false);
		if(storedData instanceof MediaVersion med && StringHelper.containsNonWhitespace(med.getMedia().getAltText())) {
			imageCmp.setAlt(med.getMedia().getAltText());
		}
		updateImage(storedData);
		
		mainVC.contextPut("extendedMetadata", hints.isExtendedMetadata());

		putInitialPanel(mainVC);
	}

	private void setBlockLayoutClass(ImageSettings imageSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(imageSettings, inForm));
	}

	public void setPreventBrowserCaching(boolean preventBrowserCaching) {
		imageCmp.setPreventBrowserCaching(preventBrowserCaching);
	}

	public void updateImage(StoredData storedData) {
		File mediaFile = dataStorage.getFile(storedData);
		if(mediaFile != null) {
			imageCmp.setMedia(mediaFile);
			
			mainVC.put("image", imageCmp);
			mainVC.contextPut("imageSizeStyle", "none");
			Size imageSize = imageCmp.getRealSize();
			if(imageSize != null) {
				mainVC.contextPut("imageSize", imageSize);
			}
			mainVC.contextPut("media", storedData);
		}
	}

	private void updateImageSettings(ImageElement media) {
		ImageSettings settings = media.getImageSettings();
		if (settings != null) {
			DublinCoreMetadata meta = null;
			if(media.getStoredData() instanceof DublinCoreMetadata dcMetadata) {
				meta = dcMetadata;
			}
			updateImageSettings(settings, meta);
		} else {
			mainVC.contextPut("alignment", ImageHorizontalAlignment.left.name());
			mainVC.contextPut("imageSizeStyle", ImageSize.none.name());
			setBlockLayoutClass(null);
			imageCmp.setCssClasses(DEFAULT_STYLE);
			mainVC.contextPut("style", DEFAULT_STYLE);
			imageCmp.setDirty(true);
		}
	}
	
	private void updateImageSettings(ImageSettings settings, DublinCoreMetadata meta) {
		if(settings.getAlignment() != null) {
			mainVC.contextPut("alignment", settings.getAlignment().name());
		} else {
			mainVC.contextPut("alignment", ImageHorizontalAlignment.left.name());
		}
		if(settings.getSize() != null) {
			mainVC.contextPut("imageSizeStyle", settings.getSize().name());
		} else {
			mainVC.contextPut("imageSizeStyle", ImageSize.none.name());
		}
		if(StringHelper.containsNonWhitespace(settings.getStyle())) {
			imageCmp.setCssClasses(settings.getStyle());
			mainVC.contextPut("style", settings.getStyle());
			imageCmp.setDirty(true);
		} else {
			imageCmp.setCssClasses(DEFAULT_STYLE);
			mainVC.contextPut("style", DEFAULT_STYLE);
			imageCmp.setDirty(true);
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

		setBlockLayoutClass(settings);
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ModalInspectorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			PageElement element = cpe.getElement();
			if(element instanceof ImageElement image) {
				if(event instanceof ChangeVersionPartEvent) {
					doUpdateVersion(image);
				} else {
					doUpdate(image);
				}
				mainVC.setDirty(true);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doUpdateVersion(ImageElement image) {
		File mediaFile = dataStorage.getFile(image.getStoredData());
		if(mediaFile != null) {
			imageCmp.setMedia(mediaFile);
		}
	}
	
	private void doUpdate(ImageElement element) {
		updateImageSettings(element);
	}

	@Override
	protected void doDispose() {
		if(imageCmp != null) {
			imageCmp.dispose();
		}
        super.doDispose();
	}
}
