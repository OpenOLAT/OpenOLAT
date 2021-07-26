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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageSize;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.portfolio.model.StandardMediaRenderingHints;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String[] titlePositionKeys = new String[] {
			ImageTitlePosition.above.name(), ImageTitlePosition.top.name(), ImageTitlePosition.centered.name(), ImageTitlePosition.bottom.name()
		};
	private static final String[] alignmentKeys = new String[]{
			ImageHorizontalAlignment.left.name(), ImageHorizontalAlignment.middle.name(),ImageHorizontalAlignment.right.name(), ImageHorizontalAlignment.leftfloat.name(), ImageHorizontalAlignment.rightfloat.name()
		};
	private static final String[] sizeKeys = new String[] {
			ImageSize.none.name(), ImageSize.small.name(), ImageSize.medium.name(), ImageSize.large.name(), ImageSize.fill.name()
		};
	private static final String[] onKeys = new String[] { "on" };
	
	private SingleSelection sizeEl;
	private SingleSelection styleEl;
	private SingleSelection alignmentEl;
	private MultipleSelectionElement sourceEl;
	private MultipleSelectionElement descriptionEnableEl;
	
	private TextElement titleEl;
	private SingleSelection titlePositionEl;
	private SingleSelection titleStyleEl;
	
	private TextElement captionEl;
	private TextElement descriptionEl;
	
	private ImageRunController imagePreview;
	
	private boolean editMode;
	private DataStorage dataStorage;
	private ImageElement imageElement;
	private final PageElementStore<ImageElement> store;
	
	@Autowired
	private ContentEditorModule contentEditorModule;
	
	public ImageEditorController(UserRequest ureq, WindowControl wControl, ImageElement mediaPart, DataStorage storage, PageElementStore<ImageElement> store) {
		super(ureq, wControl, "image_editor", Util.createPackageTranslator(PageEditorV2Controller.class, ureq.getLocale()));
		this.imageElement = mediaPart;
		this.dataStorage = storage;
		this.store = store;
		initForm(ureq);
	}
	
	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		alignmentEl.setVisible(editMode);
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {		
		imagePreview = new ImageRunController(ureq, getWindowControl(), dataStorage, imageElement, new StandardMediaRenderingHints());
		listenTo(imagePreview);
		((FormLayoutContainer)formLayout).getFormItemComponent().put("imagePreview", imagePreview.getInitialComponent());
		
		titleEl = uifactory.addTextElement("image.title", 255, null, formLayout);
		titleEl.setPlaceholderKey("image.title.placeholder", null);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] titlePositionValues = new String[] {
				translate("image.title.position.above"), translate("image.title.position.top"),
				translate("image.title.position.centered"), translate("image.title.position.bottom")
			};
		titlePositionEl = uifactory.addDropdownSingleselect("image.title.position", "image.title.position", formLayout, titlePositionKeys, titlePositionValues, null);
		titlePositionEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> titleStyleList = contentEditorModule.getImageTitleStyleList();
		String[] titleStyles = titleStyleList.toArray(new String[titleStyleList.size()]);
		String[] titleStylesValues = new String[titleStyles.length];
		for(int i=titleStyles.length; i-->0; ) {
			String stylename = translate("image.title.style." + titleStyles[i]);
			if(stylename.length() < 32 && !stylename.startsWith("image.title.")) {
				titleStylesValues[i] = stylename;
			} else {
				titleStylesValues[i] = titleStyles[i];
			}
		}
		titleStyleEl = uifactory.addDropdownSingleselect("image.title.style", "image.title.style", formLayout, titleStyles, titleStylesValues, null);
		titleStyleEl.addActionListener(FormEvent.ONCHANGE);

		captionEl = uifactory.addTextElement("image.caption", 255, null, formLayout);
		captionEl.setPlaceholderKey("image.caption.placeholder", null);
		captionEl.addActionListener(FormEvent.ONCHANGE);

		String[] alignmentValues = new String[] {
				translate("image.align.left"), translate("image.align.middle"), translate("image.align.right"), translate("image.align.leftfloat"), translate("image.align.rightfloat")	
		};
		alignmentEl = uifactory.addDropdownSingleselect("image.align", "image.align", formLayout, alignmentKeys, alignmentValues, null);
		alignmentEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] sizeValues = new String[] {
				translate("image.size.none"), translate("image.size.small"), translate("image.size.medium"), translate("image.size.large"), translate("image.size.fill")	
		};
		sizeEl = uifactory.addDropdownSingleselect("image.size", "image.size", formLayout, sizeKeys, sizeValues, null);
		sizeEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> styleList = contentEditorModule.getImageStyleList();
		String[] styles = styleList.toArray(new String[styleList.size()]);
		String[] stylesValues = new String[styles.length];
		for(int i=styles.length; i-->0; ) {
			String stylename = translate("image.style." + styles[i]);
			if(stylename.length() < 32 && !stylename.startsWith("image.")) {
				stylesValues[i] = stylename;
			} else {
				stylesValues[i] = styles[i];
			}
		}
		styleEl = uifactory.addDropdownSingleselect("image.style", "image.style", formLayout, styles, stylesValues, null);
		styleEl.addActionListener(FormEvent.ONCHANGE);

		String[] licenseValues = new String[] { translate("image.origin.show") };
		sourceEl = uifactory.addCheckboxesHorizontal("image.origin", "image.origin", formLayout, onKeys, licenseValues);
		sourceEl.addActionListener(FormEvent.ONCHANGE);
		sourceEl.setVisible(imageElement.getStoredData() instanceof DublinCoreMetadata);
		
		String[] descriptionValues = new String[] { translate("image.description.show") };
		descriptionEnableEl = uifactory.addCheckboxesHorizontal("image.description", "image.description", formLayout, onKeys, descriptionValues);
		descriptionEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		descriptionEl = uifactory.addTextAreaElement("image.description.content", 4, 60, null, formLayout);
		descriptionEl.addActionListener(FormEvent.ONCHANGE);
		descriptionEl.setVisible(false);
		
		ImageSettings settings = imageElement.getImageSettings();
		if(settings != null) {
			if(settings.getAlignment() != null) {
				for(String alignmentKey:alignmentKeys) {
					if(settings.getAlignment().name().equals(alignmentKey)) {
						alignmentEl.select(alignmentKey, true);
					}
				}
			}
			if(settings.getSize() != null) {
				for(String sizeKey:sizeKeys) {
					if(settings.getSize().name().equals(sizeKey)) {
						sizeEl.select(sizeKey, true);
					}
				}
			}
			
			if(StringHelper.containsNonWhitespace(settings.getStyle())) {
				for(int i=styles.length; i-->0; ) {
					if(styles[i].equals(settings.getStyle())) {
						styleEl.select(styles[i], true);
					}
				}
			}
			
			sourceEl.select(onKeys[0], settings.isShowSource());
			descriptionEnableEl.select(onKeys[0], settings.isShowDescription());
			descriptionEl.setValue(settings.getDescription());
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));
			
			if(StringHelper.containsNonWhitespace(settings.getTitle())) {
				titleEl.setValue(settings.getTitle());
				
				if(StringHelper.containsNonWhitespace(settings.getTitleStyle())) {
					for(String titleStyle:titleStyles) {
						if(titleStyle.equals(settings.getTitleStyle())) {
							titleStyleEl.select(titleStyle, true);
						}
					}
				}
				
				if(settings.getTitlePosition() != null) {
					titlePositionEl.select(settings.getTitlePosition().name(), true);
				}
			}
			
			if(StringHelper.containsNonWhitespace(settings.getCaption())) {
				captionEl.setValue(settings.getCaption());
			}
			
			if(StringHelper.containsNonWhitespace(settings.getDescription())) {
				descriptionEl.setValue(settings.getDescription());
			}
		} else {
			if(StringHelper.containsNonWhitespace(imageElement.getStoredData().getDescription())) {
				captionEl.setValue(imageElement.getStoredData().getDescription());
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(descriptionEnableEl == source) {
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));
			doSaveSettings(ureq);
		} else if(styleEl == source || alignmentEl == source || sizeEl == source
				|| sourceEl == source || captionEl == source || descriptionEl == source
				|| titleEl == source || titleStyleEl == source || titlePositionEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSaveSettings(UserRequest ureq) {
		ImageSettings settings = imageElement.getImageSettings();
		if(settings == null) {
			settings = new ImageSettings();
		}
		
		//title
		if(StringHelper.containsNonWhitespace(titleEl.getValue())) {
			settings.setTitle(titleEl.getValue());
			if(titleStyleEl.isOneSelected()) {
				settings.setTitleStyle(titleStyleEl.getSelectedKey());
			} else {
				settings.setTitleStyle(null);
			}
			
			if(titlePositionEl.isOneSelected()) {
				settings.setTitlePosition(ImageTitlePosition.valueOf(titlePositionEl.getSelectedKey()));
			} else {
				settings.setTitlePosition(null);
			}
		} else {
			settings.setTitle(null);
			settings.setTitleStyle(null);
			settings.setTitlePosition(null);
		}
		
		if(StringHelper.containsNonWhitespace(captionEl.getValue())) {
			settings.setCaption(captionEl.getValue());
		} else {
			settings.setCaption(null);
		}
		
		if(alignmentEl.isOneSelected()) {
			ImageHorizontalAlignment alignment = ImageHorizontalAlignment.valueOf(alignmentEl.getSelectedKey());
			settings.setAlignment(alignment);
		}
		if(sizeEl.isOneSelected()) {
			ImageSize size = ImageSize.valueOf(sizeEl.getSelectedKey());
			settings.setSize(size);
		}
		settings.setShowSource(sourceEl.isAtLeastSelected(1));
		
		settings.setShowDescription(descriptionEnableEl.isAtLeastSelected(1));
		if(descriptionEl.isVisible()) {
			settings.setDescription(descriptionEl.getValue());
		} else {
			settings.setDescription(null);
		}
		
		if(styleEl.isOneSelected()) {
			settings.setStyle(styleEl.getSelectedKey());
		} else {
			settings.setStyle(null);
		}
		
		String settingsXml = ContentEditorXStream.toXml(settings);
		imageElement.setLayoutOptions(settingsXml);
		imageElement = store.savePageElement(imageElement);
		settings = imageElement.getImageSettings();
		
		DublinCoreMetadata meta = null;
		if(imageElement.getStoredData() instanceof DublinCoreMetadata) {
			meta = (DublinCoreMetadata)imageElement.getStoredData();
		}
		imagePreview.updateImageSettings(settings, meta);
		
		fireEvent(ureq, new ChangePartEvent(imageElement));
	}
}
