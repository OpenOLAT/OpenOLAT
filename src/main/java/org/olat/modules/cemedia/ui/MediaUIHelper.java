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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IconSelectorElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.modules.ceditor.model.AlertBoxIcon;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.BlockLayoutSpacing;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaUsage;

/**
 * 
 * Initial date: 15 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUIHelper {

	public static final String withIconKey = "withIcon";
	public static final String collapsibleKey = "collapsible";

	private MediaUIHelper() {
		//
	}
	
	public static boolean showBusinessPath(String businessPath) {
		return StringHelper.containsNonWhitespace(businessPath) && !businessPath.contains("[MediaCenter:0]");
	}
	
	public static String toMediaCenterBusinessPath(ImageElement imageElement) {
		String businessPath = "[HomeSite:0][MediaCenter:0]";
		if(imageElement instanceof MediaPart part) {
			businessPath += "[Media:" + part.getMedia().getKey() + "]";
		}
		return businessPath;
	}
	
	public static MediaTabComponents addMediaVersionTab(FormItemContainer formLayout, TabbedPaneItem tabbedPane,
			MediaPart mediaPart, List<MediaVersion> versions,
			FormUIFactory uifactory, Translator translator) {
		
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("versions", translator);
		formLayout.add(layoutCont);
		tabbedPane.addTab(translator.translate("tab.media"), layoutCont);
		
		StaticTextElement nameEl = null;
		SingleSelection versionEl = null;
		if(mediaPart.getStoredData() != null) {
			String url = mediaPart.getMediaVersionUrl();
			String filename = mediaPart.getStoredData().getRootFilename();
		    if(StringHelper.containsNonWhitespace(url)) {
				nameEl = uifactory.addStaticTextElement("media.name", "media.name", url, layoutCont);
				nameEl.setElementCssClass("o_ceditor_inspector_wrap");
		    } else if(StringHelper.containsNonWhitespace(filename)) {
				nameEl = uifactory.addStaticTextElement("media.name", "media.name", filename, layoutCont);
				nameEl.setElementCssClass("o_ceditor_inspector_wrap");
			}
			
			if(versions != null && !versions.isEmpty()) {
				List<MediaVersion> versionList = new ArrayList<>(versions);
				
				String selectedKey = null;
				SelectionValues versionsVK = new SelectionValues();
				for(int i=0; i<versionList.size(); i++) {
					MediaVersion version = versionList.get(i);
					String value;
					if(i == 0) {
						value = translator.translate("last.version");
					} else {
						value = translator.translate("version", version.getVersionName());
					}
					
					String versionKey = version.getKey().toString();
					versionsVK.add(SelectionValues.entry(versionKey, value));
					if(mediaPart.getStoredData().equals(version)) {
						selectedKey = versionKey;
					}
				}
				
				versionEl = uifactory.addDropdownSingleselect("image.versions", layoutCont,
						versionsVK.keys(), versionsVK.values());
				versionEl.addActionListener(FormEvent.ONCHANGE);
				if(selectedKey == null && !versionsVK.isEmpty()) {
					versionEl.select(versionsVK.keys()[0], true);
				} else if(selectedKey != null && versionsVK.containsKey(selectedKey)) {
					versionEl.select(selectedKey, true);
				}
			}
		}
		
		FormLink mediaCenterLink = null;
		if(mediaPart.getMedia() != null) {
			mediaCenterLink = uifactory.addFormLink("goto.media.center", layoutCont, Link.LINK);
			mediaCenterLink.setIconLeftCSS("o_icon o_icon_external_link");
		}
		
		return new MediaTabComponents(nameEl, versionEl, mediaCenterLink);
	}
	
	public static MediaVersion getVersion(List<MediaVersion> versions, String selectedKey) {
		for(MediaVersion version:versions) {
			if(selectedKey.equals(version.getKey().toString())) {
				return version;
			}
		}
		return null;
	}
	
	public record MediaTabComponents (StaticTextElement nameEl, SingleSelection versionEl, FormLink mediaCenterLink) {
		//
	}
	
	public static void open(UserRequest ureq, WindowControl wControl, MediaUsage mediaUsage) {
		open(ureq, wControl, mediaUsage.binderKey(), mediaUsage.pageKey(), mediaUsage.repositoryEntryKey(), mediaUsage.subIdent());
	}
	
	public static String businessPath(Long binderKey, Long pageKey, Long repositoryEntryKey, String subIdent) {
		String businessPath = null;
		if(binderKey != null) {
			businessPath = "[HomeSite:0][PortfolioV2:0][MyBinders:0][Binder:" + binderKey + "][Entries:0][Entry:" + pageKey + "]";
		} else if(repositoryEntryKey != null) {
			businessPath = "[RepositoryEntry:" + repositoryEntryKey + "]";
			if(StringHelper.containsNonWhitespace(subIdent)) {
				businessPath += "[CourseNode:" + subIdent + "]";
			}
		} else if(pageKey != null) {
			//http://localhost:8081/auth/HomeSite/720898/PortfolioV2/0/MyPages/0/Entry/89
			businessPath = "[HomeSite:0][PortfolioV2:0][MyPages:0][Entry:" + pageKey + "]";
		} else  {
			businessPath = "[HomeSite:0][PortfolioV2:0]";
		}
		return businessPath;
	}
	
	public static void open(UserRequest ureq, WindowControl wControl, Long binderKey, Long pageKey, Long repositoryEntryKey, String subIdent) {
		String businessPath = businessPath(binderKey, pageKey, repositoryEntryKey, subIdent);
		if(StringHelper.containsNonWhitespace(businessPath)) {
			NewControllerFactory.getInstance().launch(businessPath, ureq, wControl);
		}
	}
	
	public static void setQuota(Quota quota, FileElement fileEl) {
		long uploadLimitKB = quota.getUlLimitKB().longValue();
		long remainingKB = quota.getRemainingSpace().longValue();
		if(uploadLimitKB != Quota.UNLIMITED && remainingKB != Quota.UNLIMITED) {
			long limitKB = Math.min(uploadLimitKB, remainingKB);
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			fileEl.setMaxUploadSizeKB(limitKB, "ULLimitExceeded", new String[] { Formatter.formatKBytes(limitKB), supportAddr });
			
			if(uploadLimitKB > remainingKB) {
				fileEl.setWarningKey("warning.upload.quota", Formatter.formatKBytes(remainingKB));
			}
		}
	}

	public static SelectionValues getAlertBoxTypes(Translator translator) {
		SelectionValues selectionValues = new SelectionValues();
		for (AlertBoxType alertBoxType : AlertBoxType.values()) {
			selectionValues.add(SelectionValues.entry(alertBoxType.name(), translator.translate(alertBoxType.getI18nKey())));
		}
		return selectionValues;
	}

	public static List<IconSelectorElement.Icon> getIcons(Translator translator) {
		return Arrays.stream(AlertBoxIcon.values()).map(i ->
			new IconSelectorElement.Icon(i.name(), translator.translate(i.getI18nKey()), i.getCssClass())
		).toList();
	}

	public static AlertBoxComponents addAlertBoxStyleTab(FormItemContainer formLayout, TabbedPaneItem tabbedPane,
														 FormUIFactory uifactory, AlertBoxSettings alertBoxSettings,
														 ColorService colorService, Locale locale) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, locale);
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("layout", translator);
		formLayout.add(layoutCont);
		tabbedPane.addTab(translator.translate("tab.layout"), layoutCont);
		return MediaUIHelper.addAlertBoxSettings(layoutCont, translator, uifactory,
				alertBoxSettings, colorService, locale);
	}

	public static AlertBoxComponents addAlertBoxSettings(FormLayoutContainer formLayout, Translator translator,
														 FormUIFactory uifactory, AlertBoxSettings alertBoxSettings,
														 ColorService colorService, Locale locale) {
		FormLayoutContainer alertBoxLayout = FormLayoutContainer.createVerticalFormLayout("alertBoxLayout", translator);
		alertBoxLayout.setFormTitle(translator.translate("alert"));
		formLayout.add(alertBoxLayout);

		FormToggle alertBoxToggleEl = uifactory.addToggleButton("showAlertBox", "alert.box",
				translator.translate("on"), translator.translate("off"), alertBoxLayout);
		alertBoxToggleEl.addActionListener(FormEvent.ONCHANGE);
		alertBoxToggleEl.toggle(alertBoxSettings.isShowAlertBox());

		SelectionValues alertBoxTypes = getAlertBoxTypes(translator);
		SingleSelection typeEl = uifactory.addDropdownSingleselect("type", alertBoxLayout, alertBoxTypes.keys(),
				alertBoxTypes.values());
		typeEl.addActionListener(FormEvent.ONCHANGE);
		if (alertBoxSettings.getType() != null) {
			typeEl.select(alertBoxSettings.getType().name(), true);
		} else {
			typeEl.select(typeEl.getKeys()[0], true);
		}

		TextElement titleEl = uifactory.addTextElement("title", 80, "", alertBoxLayout);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		titleEl.setValue(alertBoxSettings.getTitle());

		SelectionValues checkBoxesKV = new SelectionValues();
		checkBoxesKV.add(SelectionValues.entry(withIconKey, translator.translate("alert.with.icon")));
		checkBoxesKV.add(SelectionValues.entry(collapsibleKey, translator.translate("alert.collapsible")));
		MultipleSelectionElement checkBoxesEl = uifactory.addCheckboxesVertical("alert.checkboxes", null,
				alertBoxLayout, checkBoxesKV.keys(), checkBoxesKV.values(),1);
		checkBoxesEl.setAjaxOnly(true);
		checkBoxesEl.addActionListener(FormEvent.ONCHANGE);
		checkBoxesEl.select(withIconKey, alertBoxSettings.isWithIcon());
		checkBoxesEl.select(collapsibleKey, alertBoxSettings.isCollapsible());

		List<IconSelectorElement.Icon> icons = getIcons(translator);
		IconSelectorElement iconEl = uifactory.addIconSelectorElement("icon", "icon", alertBoxLayout,
				icons);
		iconEl.setDropUp(true);
		iconEl.setCompact(true);
		iconEl.addActionListener(FormEvent.ONCHANGE);
		if (alertBoxSettings.getIcon() != null) {
			iconEl.setIcon(alertBoxSettings.getIcon().name());
		} else {
			iconEl.setIcon(icons.get(0).id());
		}

		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorService.getColors(), locale);
		ColorPickerElement colorEl = uifactory.addColorPickerElement("color", "color", alertBoxLayout,
				colors);
		colorEl.setDropUp(true);
		colorEl.addActionListener(FormEvent.ONCHANGE);
		if (alertBoxSettings.getColor() != null) {
			colorEl.setColor(alertBoxSettings.getColor());
		} else {
			colorEl.setColor(colors.get(0).id());
		}

		return new AlertBoxComponents(alertBoxToggleEl, typeEl, titleEl, checkBoxesEl, iconEl, colorEl);
	}

	public static List<FormLink> addContainerLayoutTab(FormItemContainer formLayout, TabbedPaneItem tabbedPane,
													   Translator translator, FormUIFactory uifactory,
													   ContainerSettings containerSettings, String velocity_root) {
		String page = velocity_root + "/container_inspector.html";
		FormLayoutContainer layoutCont = FormLayoutContainer.createCustomFormLayout("layout", translator, page);
		formLayout.add(layoutCont);
		tabbedPane.addTab(translator.translate("tab.layout"), layoutCont);

		List<FormLink> layoutLinks = new ArrayList<>();

		ContainerLayout activeLayout = containerSettings.getType();

		int count = 0;
		for (ContainerLayout layout : ContainerLayout.values()) {
			if (layout.deprecated() && layout != activeLayout) {
				continue;
			}

			String id = "add." + (++count);
			String pseudoIcon = layout.pseudoIcons();
			FormLink layoutLink = uifactory.addFormLink(id, pseudoIcon, null, layoutCont,
					Link.LINK | Link.NONTRANSLATED);
			if (activeLayout == layout) {
				layoutLink.setElementCssClass("active");
			}
			layoutLink.setUserObject(layout);
			layoutLinks.add(layoutLink);
		}

		layoutCont.contextPut("layouts", layoutLinks);

		return layoutLinks;
	}

	public static LayoutTabComponents addLayoutTab(FormItemContainer formLayout, TabbedPaneItem tabbedPane,
												   Translator translator, FormUIFactory uifactory,
												   BlockLayoutSettings layoutSettings, String velocity_root) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("layout", translator);
		formLayout.add(layoutCont);
		tabbedPane.addTab(translator.translate("tab.layout"), layoutCont);

		SelectionValues spacingKV = new SelectionValues();
		SelectionValues spacingWithoutCustomKV = new SelectionValues();
		for (BlockLayoutSpacing spacing : BlockLayoutSpacing.values()) {
			SelectionValues.SelectionValue selectionValue = SelectionValues.entry(spacing.name(), translator.translate(spacing.getI18nKey()));
			spacingKV.add(selectionValue);
			if (spacing.isInCustomSubset()) {
				spacingWithoutCustomKV.add(selectionValue);
			}
		}
		SingleSelection spacingEl = uifactory.addDropdownSingleselect("spacing", "layout.spacing",
				layoutCont, spacingKV.keys(), spacingKV.values());
		spacingEl.addActionListener(FormEvent.ONCHANGE);
		if (layoutSettings != null && layoutSettings.getSpacing() != null) {
			spacingEl.select(layoutSettings.getSpacing().name(), true);
		} else {
			spacingEl.select(BlockLayoutSpacing.normal.name(), true);
		}

		String page = velocity_root + "/spacings.html";
		FormLayoutContainer spacingsCont = FormLayoutContainer.createCustomFormLayout("spacings", translator, page);
		layoutCont.setRootForm(formLayout.getRootForm());
		layoutCont.add(spacingsCont);

		SingleSelection topEl = uifactory.addDropdownSingleselect("top", "layout.spacing.custom.top",
				spacingsCont, spacingWithoutCustomKV.keys(), spacingWithoutCustomKV.values());
		topEl.addActionListener(FormEvent.ONCHANGE);

		SingleSelection rightEl = uifactory.addDropdownSingleselect("right", "layout.spacing.custom.right",
				spacingsCont, spacingWithoutCustomKV.keys(), spacingWithoutCustomKV.values());
		rightEl.addActionListener(FormEvent.ONCHANGE);

		SingleSelection bottomEl = uifactory.addDropdownSingleselect("bottom", "layout.spacing.custom.bottom",
				spacingsCont, spacingWithoutCustomKV.keys(), spacingWithoutCustomKV.values());
		bottomEl.addActionListener(FormEvent.ONCHANGE);

		SingleSelection leftEl = uifactory.addDropdownSingleselect("left", "layout.spacing.custom.left",
				spacingsCont, spacingWithoutCustomKV.keys(), spacingWithoutCustomKV.values());
		leftEl.addActionListener(FormEvent.ONCHANGE);

		spacingsCont.setVisible(false);
		if (layoutSettings != null && layoutSettings.getSpacing() != null) {
			if (layoutSettings.getCustomTopSpacing() != null && topEl.containsKey(layoutSettings.getCustomTopSpacing().name())) {
				topEl.select(layoutSettings.getCustomTopSpacing().name(), true);
			} else {
				topEl.select(topEl.getKeys()[0], true);
			}
			if (layoutSettings.getCustomRightSpacing() != null && rightEl.containsKey(layoutSettings.getCustomRightSpacing().name())) {
				rightEl.select(layoutSettings.getCustomRightSpacing().name(), true);
			} else {
				rightEl.select(rightEl.getKeys()[0], true);
			}
			if (layoutSettings.getCustomBottomSpacing() != null && bottomEl.containsKey(layoutSettings.getCustomBottomSpacing().name())) {
				bottomEl.select(layoutSettings.getCustomBottomSpacing().name(), true);
			} else {
				bottomEl.select(bottomEl.getKeys()[0], true);
			}
			if (layoutSettings.getCustomLeftSpacing() != null && leftEl.containsKey(layoutSettings.getCustomLeftSpacing().name())) {
				leftEl.select(layoutSettings.getCustomLeftSpacing().name(), true);
			} else {
				leftEl.select(leftEl.getKeys()[0], true);
			}
			if (layoutSettings.getSpacing().equals(BlockLayoutSpacing.custom)) {
				spacingsCont.setVisible(true);
			}
		}

		return new LayoutTabComponents(spacingEl, spacingsCont, topEl, rightEl, bottomEl, leftEl);
	}

	public record LayoutTabComponents (SingleSelection spacingEl, FormLayoutContainer spacingsCont, SingleSelection topEl,
									   SingleSelection rightEl, SingleSelection bottomEl, SingleSelection leftEl) {
		public boolean matches(FormItem source) {
			return source == spacingEl() || source == topEl() || source == rightEl() || source == bottomEl() || source == leftEl();
		}

		public void sync(BlockLayoutSettings layoutSettings) {
			BlockLayoutSpacing layoutSpacing = BlockLayoutSpacing.valueOf(spacingEl().getSelectedKey());
			layoutSettings.setSpacing(layoutSpacing);

			if (layoutSpacing.equals(BlockLayoutSpacing.custom)) {
				if (!topEl().isOneSelected()) {
					topEl().select(BlockLayoutSpacing.zero.name(), true);
				}
				layoutSettings.setCustomTopSpacing(BlockLayoutSpacing.valueOf(topEl().getSelectedKey()));
				if (!rightEl().isOneSelected()) {
					rightEl().select(BlockLayoutSpacing.zero.name(), true);
				}
				layoutSettings.setCustomRightSpacing(BlockLayoutSpacing.valueOf(rightEl().getSelectedKey()));
				if (!bottomEl().isOneSelected()) {
					bottomEl().select(BlockLayoutSpacing.zero.name(), true);
				}
				layoutSettings.setCustomBottomSpacing(BlockLayoutSpacing.valueOf(bottomEl().getSelectedKey()));
				if (!leftEl().isOneSelected()) {
					leftEl().select(BlockLayoutSpacing.zero.name(), true);
				}
				layoutSettings.setCustomLeftSpacing(BlockLayoutSpacing.valueOf(leftEl().getSelectedKey()));
			}

			updateVisibility();
		}

		private void updateVisibility() {
			BlockLayoutSpacing layoutSpacing = BlockLayoutSpacing.valueOf(spacingEl.getSelectedKey());
			if (layoutSpacing.equals(BlockLayoutSpacing.custom)) {
				spacingsCont().setVisible(true);
			} else {
				spacingsCont().setVisible(false);
			}
		}
	}

	public record AlertBoxComponents(FormToggle alertBoxToggleEl, SingleSelection typeEl, TextElement titleEl,
									 MultipleSelectionElement checkBoxesEl, IconSelectorElement iconEl,
									 ColorPickerElement colorEl) {

		public AlertBoxComponents(FormToggle alertBoxToggleEl, SingleSelection typeEl, TextElement titleEl,
								  MultipleSelectionElement checkBoxesEl, IconSelectorElement iconEl,
								  ColorPickerElement colorEl) {
			this.alertBoxToggleEl = alertBoxToggleEl;
			this.typeEl = typeEl;
			this.titleEl = titleEl;
			this.checkBoxesEl = checkBoxesEl;
			this.iconEl = iconEl;
			this.colorEl = colorEl;

			updateVisibility();
		}

		public boolean matches(FormItem source) {
			return source == alertBoxToggleEl || source == typeEl || source == titleEl || source == checkBoxesEl ||
					source == iconEl || source == colorEl;
		}

		public void sync(AlertBoxSettings alertBoxSettings) {
			alertBoxSettings.setShowAlertBox(alertBoxToggleEl().isOn());

			AlertBoxType type = AlertBoxType.valueOf(typeEl().getSelectedKey());
			alertBoxSettings.setType(type);

			alertBoxSettings.setTitle(titleEl.getValue());

			alertBoxSettings.setWithIcon(checkBoxesEl.isKeySelected(withIconKey));

			alertBoxSettings.setCollapsible(checkBoxesEl.isKeySelected(collapsibleKey));

			if (iconEl.isVisible() && iconEl.getIcon() != null) {
				alertBoxSettings.setIcon(AlertBoxIcon.valueOf(iconEl.getIcon().id()));
			}

			alertBoxSettings.setColor(colorEl.getColor().id());

			updateVisibility();
		}

		private void updateVisibility() {
			boolean visible = alertBoxToggleEl().isOn();
			typeEl.setVisible(visible);
			titleEl.setVisible(visible);
			checkBoxesEl.setVisible(visible);

			boolean customVisible = visible && AlertBoxType.custom.name().equals(typeEl.getSelectedKey());
			colorEl.setVisible(customVisible);

			boolean iconVisible = customVisible && checkBoxesEl.isKeySelected(withIconKey);
			iconEl.setVisible(iconVisible);

			boolean collapsibleVisible = visible && StringHelper.containsNonWhitespace(titleEl.getValue());
			checkBoxesEl.setVisible(collapsibleKey, collapsibleVisible);
		}
	}
}
