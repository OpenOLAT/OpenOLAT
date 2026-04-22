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
package org.olat.modules.ceditor.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.util.StringHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.TocElement;
import org.olat.modules.ceditor.model.TocSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TocInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private int selectedTabIndex = 0;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private TextElement titleEl;
	private MultipleSelectionElement headingLevelsEl;

	private TocElement tocElement;
	private final PageElementStore<TocElement> store;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public TocInspectorController(UserRequest ureq, WindowControl wControl, TocElement tocElement,
								  PageElementStore<TocElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.tocElement = tocElement;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("inspector.toc");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addTocTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addTocTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("toc", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.toc"), layoutCont);

		TocSettings settings = tocElement.getTocSettings();

		titleEl = uifactory.addTextElement("toc.title", "title", 255, settings.getTitle(), layoutCont);
		titleEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues headingLevelsKV = new SelectionValues();
		headingLevelsKV.add(SelectionValues.entry("1", translate("toc.heading.level.1")));
		headingLevelsKV.add(SelectionValues.entry("2", translate("toc.heading.level.2")));
		headingLevelsKV.add(SelectionValues.entry("3", translate("toc.heading.level.3")));
		headingLevelsKV.add(SelectionValues.entry("4", translate("toc.heading.level.4")));
		headingLevelsKV.add(SelectionValues.entry("5", translate("toc.heading.level.5")));

		headingLevelsEl = uifactory.addCheckboxesVertical("toc.heading.levels", "toc.heading.levels",
				layoutCont, headingLevelsKV.keys(), headingLevelsKV.values(), 1);
		headingLevelsEl.addActionListener(FormEvent.ONCHANGE);

		for (int i = 1; i <= 5; i++) {
			headingLevelsEl.select(String.valueOf(i), settings.getVisibleLevels().contains(i));
		}
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(getTocSettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory,
				getLayoutSettings(getTocSettings()), velocity_root);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		} else if (titleEl == source || headingLevelsEl == source) {
			doSaveSettings(ureq);
		} else {
			if (source == tabbedPane) {
				selectedTabIndex = tabbedPane.getSelectedPane();
			}
			updateTocTab();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		switch (selectedTabIndex) {
			case 0 -> doSaveSettings(ureq);
			case 1 -> doChangeAlertBoxSettings(ureq);
			case 2 -> doChangeLayout(ureq);
		}
	}

	private void doSaveSettings(UserRequest ureq) {
		TocSettings settings = getTocSettings();
		settings.setTitle(titleEl.getValue());
		Set<Integer> visibleLevels = new LinkedHashSet<>();
		for (int i = 1; i <= 5; i++) {
			if (headingLevelsEl.isSelected(i - 1)) {
				visibleLevels.add(i);
			}
		}
		settings.setVisibleLevels(visibleLevels);
		tocElement.setTocSettings(settings);
		tocElement = store.savePageElement(tocElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(tocElement));
	}

	private void doChangeLayout(UserRequest ureq) {
		TocSettings settings = getTocSettings();
		BlockLayoutSettings layoutSettings = getLayoutSettings(settings);
		layoutTabComponents.sync(layoutSettings);
		settings.setLayoutSettings(layoutSettings);
		tocElement.setTocSettings(settings);
		doSave(ureq);
		updateTocTab();
		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		TocSettings settings = getTocSettings();
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(settings);
		alertBoxComponents.sync(alertBoxSettings);
		if (alertBoxSettings.isShowAlertBox()
				&& !StringHelper.containsNonWhitespace(alertBoxSettings.getTitle())
				&& StringHelper.containsNonWhitespace(settings.getTitle())) {
			alertBoxSettings.setTitle(settings.getTitle());
			alertBoxComponents.titleEl().setValue(settings.getTitle());
		}
		settings.setAlertBoxSettings(alertBoxSettings);
		tocElement.setTocSettings(settings);
		doSave(ureq);
		updateTocTab();
		getInitialComponent().setDirty(true);
	}

	private void updateTocTab() {
		TocSettings settings = getTocSettings();
		titleEl.setValue(settings.getTitle() != null ? settings.getTitle() : "");
		for (int i = 1; i <= 5; i++) {
			headingLevelsEl.select(String.valueOf(i), settings.getVisibleLevels().contains(i));
		}
	}

	private void doSave(UserRequest ureq) {
		tocElement = store.savePageElement(tocElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(tocElement));
	}

	private BlockLayoutSettings getLayoutSettings(TocSettings settings) {
		if (settings.getLayoutSettings() != null) {
			return settings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(TocSettings settings) {
		if (settings.getAlertBoxSettings() != null) {
			return settings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private TocSettings getTocSettings() {
		if (tocElement.getTocSettings() != null) {
			return tocElement.getTocSettings();
		}
		return new TocSettings();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
}
