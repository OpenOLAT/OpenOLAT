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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLRawInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private SingleSelection columnsEl;
	
	private HTMLElement htmlPart;
	private final PageElementStore<HTMLElement> store;
	
	@Autowired
	private DB dbInstance;
	
	public HTMLRawInspectorController(UserRequest ureq, WindowControl wControl, HTMLElement htmlPart, PageElementStore<HTMLElement> store) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.htmlPart = htmlPart;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("inspector.htmlraw");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		SelectionValues columnsValues = new SelectionValues();
		columnsValues.add(SelectionValues.entry("1", translate("text.column.1")));
		columnsValues.add(SelectionValues.entry("2", translate("text.column.2")));
		columnsValues.add(SelectionValues.entry("3", translate("text.column.3")));
		columnsValues.add(SelectionValues.entry("4", translate("text.column.4")));
		
		columnsEl = uifactory.addDropdownSingleselect("num.columns", "num.columns", formLayout,
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
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(columnsEl == source) {
			doSaveSettings();
			fireEvent(ureq, new ChangePartEvent(htmlPart));
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
}
