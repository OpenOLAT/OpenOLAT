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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.TocElement;
import org.olat.modules.ceditor.model.TocSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TocInspectorController extends FormBasicController implements PageElementInspectorController {

	private TextElement titleEl;
	private MultipleSelectionElement headingLevelsEl;

	private TocElement tocElement;
	private final PageElementStore<TocElement> store;

	@Autowired
	private DB dbInstance;

	public TocInspectorController(UserRequest ureq, WindowControl wControl, TocElement tocElement,
								  PageElementStore<TocElement> store) {
		super(ureq, wControl, LAYOUT_VERTICAL);
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
		TocSettings settings = tocElement.getTocSettings();

		titleEl = uifactory.addTextElement("toc.title", "title", 255, settings.getTitle(), formLayout);
		titleEl.addActionListener(FormEvent.ONBLUR);

		SelectionValues headingLevelsKV = new SelectionValues();
		headingLevelsKV.add(SelectionValues.entry("1", translate("toc.heading.level.1")));
		headingLevelsKV.add(SelectionValues.entry("2", translate("toc.heading.level.2")));
		headingLevelsKV.add(SelectionValues.entry("3", translate("toc.heading.level.3")));
		headingLevelsKV.add(SelectionValues.entry("4", translate("toc.heading.level.4")));
		headingLevelsKV.add(SelectionValues.entry("5", translate("toc.heading.level.5")));

		headingLevelsEl = uifactory.addCheckboxesVertical("toc.heading.levels", "toc.heading.levels",
				formLayout, headingLevelsKV.keys(), headingLevelsKV.values(), 1);
		headingLevelsEl.addActionListener(FormEvent.ONCHANGE);

		for (int i = 1; i <= 5; i++) {
			headingLevelsEl.select(String.valueOf(i), settings.getVisibleLevels().contains(i));
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (titleEl == source || headingLevelsEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSaveSettings(ureq);
	}

	private void doSaveSettings(UserRequest ureq) {
		TocSettings settings = tocElement.getTocSettings();
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
}