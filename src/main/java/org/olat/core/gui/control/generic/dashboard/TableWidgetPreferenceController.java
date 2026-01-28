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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Jan 16, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TableWidgetPreferenceController extends FormBasicController {

	private SingleSelection keyFigureEl;
	private MultipleSelectionElement figuresEl;
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
		keyFigureEl = uifactory.addDropdownSingleselect("settings.main.figure", headerCont, figureValues.keys(), figureValues.values());
		keyFigureEl.addActionListener(FormEvent.ONCHANGE);
		
		figuresEl = uifactory.addCheckboxesVertical("figures", "settings.figures", headerCont, figureValues.keys(), figureValues.values(), 1);
		
		FormLayoutContainer tableCont = FormLayoutContainer.createVerticalFormLayout("tableCont", getTranslator());
		tableCont.setFormTitle(translate("settings.table"));
		tableCont.setRootForm(mainForm);
		formLayout.add(tableCont);
		
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
		String keyFigureKey = prefs.getKeyFigureKey();
		if (!StringHelper.containsNonWhitespace(keyFigureKey) || !Arrays.asList(keyFigureEl.getKeys()).contains(keyFigureKey)) {
			keyFigureKey = keyFigureEl.getKey(1);
		}
		keyFigureEl.select(keyFigureKey, true);
		
		
		Set<String> focusFigureKeys = Objects.requireNonNullElse(prefs.getFocusFigureKeys(), Set.of());
		figuresEl.getKeys().forEach(key -> figuresEl.select(key, focusFigureKeys.contains(key)));
		
		numRowsEl.select(String.valueOf(prefs.getNumRows()), true);
		
		updateUI();
	}

	private void updateUI() {
		figuresEl.setEnabled(figuresEl.getKeys(), true);
		figuresEl.setEnabled(keyFigureEl.getSelectedKey(), false);
		figuresEl.select(keyFigureEl.getSelectedKey(), true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == keyFigureEl) {
			updateUI();
		} else if (source == resetLink) {
			updateUI(configProvider.getDefault());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey(keyFigureEl.getSelectedKey());
		
		HashSet<String> visibleFigureKeys = new HashSet<>(figuresEl.getSelectedKeys());
		visibleFigureKeys.remove(keyFigureEl.getSelectedKey());
		prefs.setFocusFigureKeys(visibleFigureKeys);
		
		prefs.setNumRows(Integer.valueOf(numRowsEl.getSelectedKey()));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
