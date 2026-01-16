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
package org.olat.core.gui.control.generic.dashboard;

import java.util.HashSet;
import java.util.stream.IntStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs.FilterType;

/**
 * 
 * Initial date: Jan 16, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TableWidgetPreferenceController extends FormBasicController {

	private MultipleSelectionElement keyFiguresEl;
	private SingleSelection filterTypeEl;
	private MultipleSelectionElement filterFiguresEl;
	private SingleSelection numRowsEl;
	private FormLink resetLink;
	
	private final TableWidgetConfigProvider configProvider;
	private TableWidgetConfigPrefs prefs;
	
	public TableWidgetPreferenceController(UserRequest ureq, WindowControl wControl,
			TableWidgetConfigProvider configProvider, TableWidgetConfigPrefs prefs) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.configProvider = configProvider;
		
		initForm(ureq);
		updateUI(prefs);
	}

	public TableWidgetConfigPrefs getPrefs() {
		return prefs;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer headerCont = FormLayoutContainer.createVerticalFormLayout("headerCont", getTranslator());
		headerCont.setFormTitle(translate("settings.header"));
		headerCont.setRootForm(mainForm);
		formLayout.add(headerCont);
		
		SelectionValues figureValues = configProvider.getFigureValues();
		keyFiguresEl = uifactory.addCheckboxesVertical("key.figures", "settings.key.figures", headerCont, figureValues.keys(), figureValues.values(), 1);
		
		FormLayoutContainer tableCont = FormLayoutContainer.createVerticalFormLayout("tableCont", getTranslator());
		tableCont.setFormTitle(translate("settings.table"));
		tableCont.setRootForm(mainForm);
		formLayout.add(tableCont);
		
		SelectionValues filterSV = new SelectionValues();
		filterSV.add(SelectionValues.entry(FilterType.relevant.name(), translate("settings.filter.type.relevant")));
		filterSV.add(SelectionValues.entry(FilterType.custom.name(), translate("settings.filter.type.custom")));
		filterTypeEl = uifactory.addRadiosVertical("settings.filter", tableCont, filterSV.keys(), filterSV.values());
		filterTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		filterFiguresEl = uifactory.addCheckboxesVertical("filter.figures", "settings.key.figures", tableCont, figureValues.keys(), figureValues.values(), 1);
		
		SelectionValues numRowsSV = new SelectionValues();
		IntStream.rangeClosed(5, 15)
			.mapToObj(String::valueOf)
			.forEach(num -> numRowsSV.add(SelectionValues.entry(num, num)));
		numRowsEl = uifactory.addDropdownSingleselect("settings.num.rows", tableCont, numRowsSV.keys(), numRowsSV.values());
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		resetLink = uifactory.addFormLink("reset", buttonCont, Link.BUTTON );
	}
	
	private void updateUI(TableWidgetConfigPrefs prefs) {
		if (prefs.getKeyFigureKeys() != null) {
			prefs.getKeyFigureKeys().forEach(key -> keyFiguresEl.select(key, true));
		} else {
			keyFiguresEl.getKeys().forEach(key -> keyFiguresEl.select(key, false));
		}
		
		filterTypeEl.select(prefs.getFilterType().name(), true);
		
		if (prefs.getFilterFigureKeys() != null) {
			prefs.getFilterFigureKeys().forEach(key -> filterFiguresEl.select(key, true));
		} else {
			filterFiguresEl.getKeys().forEach(key -> filterFiguresEl.select(key, false));
		}
		
		numRowsEl.select(String.valueOf(prefs.getNumRows()), true);
		
		updateUI();
	}

	private void updateUI() {
		filterFiguresEl.setVisible(filterTypeEl.isKeySelected(FilterType.custom.name()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == filterTypeEl) {
			updateUI();
		} else if (source == resetLink) {
			updateUI(configProvider.getDefault());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKeys(new HashSet<>(keyFiguresEl.getSelectedKeys()));
		prefs.setFilterType(FilterType.valueOf(filterTypeEl.getSelectedKey()));
		if (prefs.getFilterType() == FilterType.relevant) {
			prefs.setFilterFigureKeys(null);
		} else {
			HashSet<String> filterFigureKeys = new HashSet<>(filterFiguresEl.getSelectedKeys());
			prefs.setFilterFigureKeys(filterFigureKeys);
			if (filterFigureKeys.isEmpty()) {
				prefs.setFilterType(FilterType.relevant);
			}
		}
		prefs.setNumRows(Integer.valueOf(numRowsEl.getSelectedKey()));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
