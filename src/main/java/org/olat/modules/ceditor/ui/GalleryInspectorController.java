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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.GalleryElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.GalleryType;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryInspectorController extends FormBasicController implements PageElementInspectorController {
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private GalleryElement galleryElement;
	private final PageElementStore<GalleryElement> store;
	private TextElement titleEl;
	private SingleSelection typeEl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public GalleryInspectorController(UserRequest ureq, WindowControl wControl, GalleryElement galleryElement,
									  PageElementStore<GalleryElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.galleryElement = galleryElement;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.gallery");
	}

	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addGalleryTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addGalleryTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("gallery", getTranslator());
		layoutCont.setElementCssClass("o_gallery_inspector_tab");
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.gallery"), layoutCont);

		titleEl = uifactory.addTextElement("gallery.title", 80, "", layoutCont);
		titleEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues typeKV = new SelectionValues();
		for (GalleryType galleryType : GalleryType.values()) {
			typeKV.add(SelectionValues.entry(galleryType.name(), translate(galleryType.getI18nKey())));
		}
		typeEl = uifactory.addDropdownSingleselect("gallery.type", "gallery.type", layoutCont,
				typeKV.keys(), typeKV.values(), null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(getGallerySettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory,
				getLayoutSettings(getGallerySettings()), velocity_root);
	}

	private void updateUI() {
		GallerySettings gallerySettings = galleryElement.getSettings();
		titleEl.setValue(gallerySettings.getTitle());
		typeEl.select(gallerySettings.getType().name(), true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		} else if (titleEl == source) {
			doSaveSettings(ureq);
		} else if (typeEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private void doSaveSettings(UserRequest ureq) {
		GallerySettings gallerySettings = galleryElement.getSettings();
		gallerySettings.setTitle(StringHelper.xssScan(titleEl.getValue()));
		galleryElement.setSettings(gallerySettings);
		store.savePageElement(galleryElement);
		dbInstance.commit();
		updateUI();
		fireEvent(ureq, new ChangePartEvent(galleryElement));
	}

	private void doSave(UserRequest ureq) {
		galleryElement = store.savePageElement(galleryElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(galleryElement));
	}

	private void doChangeLayout(UserRequest ureq) {
		GallerySettings gallerySettings = getGallerySettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(gallerySettings);
		layoutTabComponents.sync(layoutSettings);
		gallerySettings.setLayoutSettings(layoutSettings);

		galleryElement.setSettings(gallerySettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		GallerySettings gallerySettings = getGallerySettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(gallerySettings);
		alertBoxComponents.sync(alertBoxSettings);
		gallerySettings.setAlertBoxSettings(alertBoxSettings);

		galleryElement.setSettings(gallerySettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private BlockLayoutSettings getLayoutSettings(GallerySettings gallerySettings) {
		if (gallerySettings.getLayoutSettings() != null) {
			return gallerySettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(GallerySettings gallerySettings) {
		if (gallerySettings.getAlertBoxSettings() != null) {
			return gallerySettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private GallerySettings getGallerySettings() {
		if (galleryElement.getSettings() != null) {
			return galleryElement.getSettings();
		}
		return new GallerySettings();
	}
}
