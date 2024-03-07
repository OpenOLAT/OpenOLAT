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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.ParagraphElement;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLRawInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private SingleSelection columnsEl;
	
	private HTMLElement htmlPart;
	private final PageElementStore<HTMLElement> store;
	private final boolean inForm;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public HTMLRawInspectorController(UserRequest ureq, WindowControl wControl, HTMLElement htmlPart, PageElementStore<HTMLElement> store, boolean inForm) {
		super(ureq, wControl, "tabs_inspector");
		this.htmlPart = htmlPart;
		this.store = store;
		this.inForm = inForm;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		if (htmlPart instanceof ParagraphElement) {
			return translate("inspector.htmlraw");
		} else {
			return translate("add.htmlraw");
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		if (htmlPart instanceof ParagraphElement) {
			addStyleTab(formLayout);
		}
		addLayoutTab(formLayout);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("style", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.style"), layoutCont);

		SelectionValues columnsValues = new SelectionValues();
		columnsValues.add(SelectionValues.entry("1", translate("text.column.1")));
		columnsValues.add(SelectionValues.entry("2", translate("text.column.2")));
		columnsValues.add(SelectionValues.entry("3", translate("text.column.3")));
		columnsValues.add(SelectionValues.entry("4", translate("text.column.4")));

		columnsEl = uifactory.addDropdownSingleselect("num.columns", "num.columns", layoutCont,
				columnsValues.keys(), columnsValues.values());
		columnsEl.addActionListener(FormEvent.ONCHANGE);

		if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
			TextSettings settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
			String selectedCols = Integer.toString(settings.getNumOfColumns());
			if(columnsValues.containsKey(selectedCols)) {
				columnsEl.select(selectedCols, true);
			} else {
				columnsEl.select("1", true);
			}
		} else {
			columnsEl.select("1", true);
		}

		alertBoxComponents = MediaUIHelper.addAlertBoxSettings(layoutCont, getTranslator(), uifactory,
				getAlertBoxSettings(getTextSettings()), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getTextSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory, layoutSettings, velocity_root);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(columnsEl == source) {
			doSaveSettings();
			fireEvent(ureq, new ChangePartEvent(htmlPart));
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSaveSettings() {
		TextSettings settings;
		if(StringHelper.containsNonWhitespace(htmlPart.getLayoutOptions())) {
			settings = ContentEditorXStream.fromXml(htmlPart.getLayoutOptions(), TextSettings.class);
		} else {
			settings = new TextSettings();
		}
		
		int numOfColumns = 1;
		if(columnsEl.isOneSelected()) {
			numOfColumns = Integer.parseInt(columnsEl.getSelectedKey());
		}
		settings.setNumOfColumns(numOfColumns);

		String settingsXml = ContentEditorXStream.toXml(settings);
		htmlPart.setLayoutOptions(settingsXml);
		htmlPart = store.savePageElement(htmlPart);
		dbInstance.commit();
	}

	private void doChangeLayout(UserRequest ureq) {
		TextSettings textSettings = getTextSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(textSettings);
		layoutTabComponents.sync(layoutSettings);
		textSettings.setLayoutSettings(layoutSettings);

		String settingsXml = ContentEditorXStream.toXml(textSettings);
		htmlPart.setLayoutOptions(settingsXml);
		htmlPart = store.savePageElement(htmlPart);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(htmlPart));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		TextSettings textSettings = getTextSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(textSettings);
		alertBoxComponents.sync(alertBoxSettings);
		textSettings.setAlertBoxSettings(alertBoxSettings);

		String settingsXml = ContentEditorXStream.toXml(textSettings);
		htmlPart.setLayoutOptions(settingsXml);
		htmlPart = store.savePageElement(htmlPart);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(htmlPart));

		getInitialComponent().setDirty(true);
	}

	private BlockLayoutSettings getLayoutSettings(TextSettings textSettings) {
		if (textSettings.getLayoutSettings() != null) {
			return textSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(TextSettings textSettings) {
		if (textSettings.getAlertBoxSettings() != null) {
			return textSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private TextSettings getTextSettings() {
		if (htmlPart.getTextSettings() != null) {
			return htmlPart.getTextSettings();
		}
		return new TextSettings();
	}
}
