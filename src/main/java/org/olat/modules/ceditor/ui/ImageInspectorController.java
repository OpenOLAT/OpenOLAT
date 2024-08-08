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

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem.TabIndentation;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentEditorModule;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.ImageHorizontalAlignment;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.ImageSize;
import org.olat.modules.ceditor.model.ImageTitlePosition;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.cemedia.ui.MediaUIHelper.MediaTabComponents;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private static final String[] titlePositionKeys = new String[] {
			ImageTitlePosition.above.name(), ImageTitlePosition.top.name(), ImageTitlePosition.centered.name(), ImageTitlePosition.bottom.name()
		};

	private static final String[] onKeys = new String[] { "on" };
	
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
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
	
	private FormLink mediaCenterLink;
	private SingleSelection versionEl;
	private StaticTextElement nameEl;

	private boolean sharedWithMe;
	private ImageElement imageElement;
	private final PageElementStore<ImageElement> store;
	private final String titleI18nKey;
	private final boolean inForm;

	private List<MediaVersion> versions;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private ContentEditorModule contentEditorModule;
	@Autowired
	private ColorService colorService;

	public ImageInspectorController(UserRequest ureq, WindowControl wControl, ImageElement mediaPart, PageElementStore<ImageElement> store, String titleI18nKey, boolean inForm) {
		super(ureq, wControl, "image_inspector", Util.createPackageTranslator(PageEditorV2Controller.class, ureq.getLocale()));
		this.imageElement = mediaPart;
		this.store = store;
		this.titleI18nKey = titleI18nKey;
		this.inForm = inForm;
		if(imageElement instanceof MediaPart part) {
			versions = mediaService.getVersions(part.getMedia());
			sharedWithMe = mediaService.isMediaShared(getIdentity(), part.getMedia(), null);
		}
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		String filename = imageElement.getStoredData() == null ? "<unkown>" : imageElement.getStoredData().getRootFilename();
		return translate(titleI18nKey, filename);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabIndentation.none);
		formLayout.add("tabs", tabbedPane);
		
		ImageSettings settings = imageElement.getImageSettings();
		//addStyleTab(formLayout);
		initStyleForm(formLayout, tabbedPane, settings);
		initTitleForm(formLayout, tabbedPane, settings);
		initDisplayForm(formLayout, tabbedPane, settings);
		addMediaTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		FormLayoutContainer styleCont = FormLayoutContainer.createVerticalFormLayout("style", getTranslator());
		formLayout.add(styleCont);
		tabbedPane.addTab(getTranslator().translate("tab.style"), styleCont);

		alertBoxComponents = MediaUIHelper.addAlertBoxSettings(styleCont, getTranslator(), uifactory,
				getAlertBoxSettings(getImageSettings()), colorService, getLocale());
	}

	private void addMediaTab(FormItemContainer formLayout) {
		if (imageElement instanceof MediaPart imagePart) {
			MediaTabComponents mediaCmps = MediaUIHelper
					.addMediaVersionTab(formLayout, tabbedPane, imagePart, versions, uifactory, getTranslator());
			mediaCenterLink = mediaCmps.mediaCenterLink();
			if (mediaCenterLink != null) {
				mediaCenterLink.setVisible(sharedWithMe);
			}
			versionEl = mediaCmps.versionEl();
			nameEl = mediaCmps.nameEl();
		}
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getImageSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory, layoutSettings, velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings(ImageSettings imageSettings) {
		if (imageSettings.getLayoutSettings() != null) {
			return imageSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(ImageSettings imageSettings) {
		if (imageSettings.getAlertBoxSettings() != null) {
			return imageSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private ImageSettings getImageSettings() {
		if (imageElement != null) {
			if (imageElement.getImageSettings() != null) {
				return imageElement.getImageSettings();
			}
		}
		return new ImageSettings();
	}

	private void initStyleForm(FormItemContainer formLayout, TabbedPaneItem tPane, ImageSettings settings) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("style", getTranslator());
		layoutCont.setElementCssClass("o_image_inspector_style");
		formLayout.add(layoutCont);
		tPane.addTab(translate("image.style"), layoutCont);
			
		SelectionValues alignmentKeyValues = new SelectionValues();
		alignmentKeyValues.add(SelectionValues.entry(ImageHorizontalAlignment.left.name(), translate("image.align.left")));
		alignmentKeyValues.add(SelectionValues.entry(ImageHorizontalAlignment.middle.name(), translate("image.align.middle")));
		alignmentKeyValues.add(SelectionValues.entry(ImageHorizontalAlignment.right.name(), translate("image.align.right")));
		alignmentKeyValues.add(SelectionValues.entry(ImageHorizontalAlignment.leftfloat.name(), translate("image.align.leftfloat")));
		alignmentKeyValues.add(SelectionValues.entry(ImageHorizontalAlignment.rightfloat.name(), translate("image.align.rightfloat")));
		alignmentEl = uifactory.addDropdownSingleselect("image.align", "image.align", layoutCont,
				alignmentKeyValues.keys(), alignmentKeyValues.values(), null);
		alignmentEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues sizeKeyValues = new SelectionValues();
		sizeKeyValues.add(SelectionValues.entry(ImageSize.none.name(), translate("image.size.none")));
		sizeKeyValues.add(SelectionValues.entry(ImageSize.small.name(), translate("image.size.small")));
		sizeKeyValues.add(SelectionValues.entry(ImageSize.medium.name(), translate("image.size.medium")));
		sizeKeyValues.add(SelectionValues.entry(ImageSize.large.name(), translate("image.size.large")));
		sizeKeyValues.add(SelectionValues.entry(ImageSize.fill.name(), translate("image.size.fill")));
		sizeEl = uifactory.addDropdownSingleselect("image.size", "image.size", layoutCont,
				sizeKeyValues.keys(), sizeKeyValues.values(), null);
		sizeEl.addActionListener(FormEvent.ONCHANGE);
		
		List<String> styleList = contentEditorModule.getImageStyleList();
		SelectionValues styleKeyValues = new SelectionValues();
		for(String style:styleList) {
			String stylename = translate("image.style." + style);
			if(stylename.length() > 32 || stylename.startsWith("image.")) {
				stylename = style;
			}
			styleKeyValues.add(SelectionValues.entry(style, stylename));
		}
		styleEl = uifactory.addDropdownSingleselect("image.style", "image.style", layoutCont,
				styleKeyValues.keys(), styleKeyValues.values(), null);
		styleEl.addActionListener(FormEvent.ONCHANGE);
		

		if(settings != null && settings.getAlignment() != null
				&& alignmentKeyValues.containsKey(settings.getAlignment().name())) {
			alignmentEl.select(settings.getAlignment().name(), true);
		}
		
		if(settings != null && settings.getSize() != null
				&& sizeKeyValues.containsKey(settings.getSize().name())) {
			sizeEl.select(settings.getSize().name(), true);
		}
			
		if(settings != null && StringHelper.containsNonWhitespace(settings.getStyle())
				&& styleKeyValues.containsKey(settings.getStyle())) {
			styleEl.select(settings.getStyle(), true);
		}

		alertBoxComponents = MediaUIHelper.addAlertBoxSettings(layoutCont, getTranslator(), uifactory,
				getAlertBoxSettings(getImageSettings()), colorService, getLocale());
	}
	
	private void initTitleForm(FormItemContainer formLayout, TabbedPaneItem tPane, ImageSettings settings) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("title", getTranslator());
		formLayout.add(layoutCont);
		tPane.addTab(translate("image.title"), layoutCont);
		
		titleEl = uifactory.addTextElement("image.title", 255, null, layoutCont);
		titleEl.setPlaceholderKey("image.title.placeholder", null);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] titlePositionValues = new String[] {
				translate("image.title.position.above"), translate("image.title.position.top"),
				translate("image.title.position.centered"), translate("image.title.position.bottom")
			};
		titlePositionEl = uifactory.addDropdownSingleselect("image.title.position", "image.title.position", layoutCont, titlePositionKeys, titlePositionValues, null);
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
		titleStyleEl = uifactory.addDropdownSingleselect("image.title.style", "image.title.style", layoutCont, titleStyles, titleStylesValues, null);
		titleStyleEl.addActionListener(FormEvent.ONCHANGE);
		
		if(settings != null && StringHelper.containsNonWhitespace(settings.getTitle())) {
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
	}
	
	private void initDisplayForm(FormItemContainer formLayout, TabbedPaneItem tPane, ImageSettings settings) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("display", getTranslator());
		formLayout.add(layoutCont);
		tPane.addTab(translate("image.display"), layoutCont);


		String[] licenseValues = new String[] { translate("image.origin.show") };
		sourceEl = uifactory.addCheckboxesHorizontal("image.origin", "image.origin", layoutCont, onKeys, licenseValues);
		sourceEl.addActionListener(FormEvent.ONCHANGE);
		sourceEl.setVisible(imageElement.getStoredData() instanceof DublinCoreMetadata);
		
		String[] descriptionValues = new String[] { translate("image.description.show") };
		descriptionEnableEl = uifactory.addCheckboxesHorizontal("image.description", "image.description", layoutCont, onKeys, descriptionValues);
		descriptionEnableEl.setAjaxOnly(true);
		descriptionEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		descriptionEl = uifactory.addTextAreaElement("image.description.content", 4, 60, null, layoutCont);
		descriptionEl.addActionListener(FormEvent.ONCHANGE);
		descriptionEl.setVisible(false);
		
		captionEl = uifactory.addTextElement("image.caption", 255, null, layoutCont);
		captionEl.setPlaceholderKey("image.caption.placeholder", null);
		captionEl.addActionListener(FormEvent.ONCHANGE);
		
		if(settings != null) {
			sourceEl.select(onKeys[0], settings.isShowSource());
			descriptionEnableEl.select(onKeys[0], settings.isShowDescription());
			descriptionEl.setValue(settings.getDescription());
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));

			if(StringHelper.containsNonWhitespace(settings.getCaption())) {
				captionEl.setValue(settings.getCaption());
			}
			
			if(StringHelper.containsNonWhitespace(settings.getDescription())) {
				descriptionEl.setValue(settings.getDescription());
			}
		} else if(imageElement instanceof MediaPart part && StringHelper.containsNonWhitespace(part.getMedia().getDescription())) {
			captionEl.setValue(part.getMedia().getDescription());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(descriptionEnableEl == source) {
			descriptionEl.setVisible(descriptionEnableEl.isAtLeastSelected(1));
			doSaveSettings(ureq);
		} else if(styleEl == source || alignmentEl == source || sizeEl == source
				|| sourceEl == source || captionEl == source || descriptionEl == source
				|| titleEl == source || titleStyleEl == source || titlePositionEl == source
				|| tabbedPane == source) {
			doSaveSettings(ureq);
		} else if(versionEl == source) {
			doSetVersion(ureq, versionEl.getSelectedKey());
		} else if(mediaCenterLink == source) {
			doOpenInMediaCenter(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(descriptionEnableEl == fiSrc) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenInMediaCenter(UserRequest ureq) {
		String businessPath = MediaUIHelper.toMediaCenterBusinessPath(imageElement);
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doSetVersion(UserRequest ureq, String selectedKey) {
		if(imageElement instanceof MediaPart part) {
			MediaVersion version = MediaUIHelper.getVersion(versions, selectedKey);
			if(version != null) {
				part.setMediaVersion(version);
				imageElement = store.savePageElement(part);
				nameEl.setValue(version.getRootFilename());
				dbInstance.commit();
			}
		}
		fireEvent(ureq, new ChangeVersionPartEvent(imageElement));
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
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(imageElement));
	}

	private void doChangeLayout(UserRequest ureq) {
		ImageSettings imageSettings = getImageSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(imageSettings);
		layoutTabComponents.sync(layoutSettings);
		imageSettings.setLayoutSettings(layoutSettings);

		String settingsXml = ContentEditorXStream.toXml(imageSettings);
		imageElement.setLayoutOptions(settingsXml);
		imageElement = store.savePageElement(imageElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(imageElement));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		ImageSettings imageSettings = getImageSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(imageSettings);
		alertBoxComponents.sync(alertBoxSettings);
		imageSettings.setAlertBoxSettings(alertBoxSettings);

		String settingsXml = ContentEditorXStream.toXml(imageSettings);
		imageElement.setLayoutOptions(settingsXml);
		imageElement = store.savePageElement(imageElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(imageElement));

		getInitialComponent().setDirty(true);
	}
}
