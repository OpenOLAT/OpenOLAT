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
package org.olat.course.nodes.st;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to configure the system-wide default values for new STCourseNodes
 * (course element "Structure"): the overview display type and, for the
 * automatic overview display types, the number of columns.
 *
 * Initial date: 2026-11-23<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 */
public class STDefaultsEditController extends FormBasicController {

	private SingleSelection displayTypeEl;
	private FormSection layoutSection;
	private SingleSelection layoutEl;
	private FormLink resetDefaultsButton;
	private FormLink backLink;

	private DialogBoxController confirmReset;

	@Autowired
	private STModule stModule;

	public STDefaultsEditController(UserRequest ureq, WindowControl wControl, String title) {
		super(ureq, wControl, "st_def_conf");
		flc.contextPut("title", title);
		initForm(ureq);
		loadDefaultConfigValues();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		resetDefaultsButton = uifactory.addFormLink("reset", "course.node.reset.defaults", null, formLayout, Link.BUTTON);
		resetDefaultsButton.setElementCssClass("o_sel_cal_delete pull-right");
		backLink = uifactory.addFormLink("back", flc);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		FormLayoutContainer stCont = FormLayoutContainer.createDefaultFormLayout("stCont", getTranslator());
		stCont.setFormTitle(translate("config.fieldset.view"));
		stCont.setRootForm(mainForm);
		formLayout.add(stCont);

		SelectionValues typesKV = new SelectionValues();
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC, translate("form.system")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW, translate("form.peekview")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE, translate("form.self")));
		typesKV.add(entry(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE, translate("form.delegate")));
		displayTypeEl = uifactory.addRadiosVertical("selforsystemoverview", stCont, typesKV.keys(), typesKV.values());
		displayTypeEl.addActionListener(FormEvent.ONCLICK);

		layoutSection = uifactory.addFormSection("layout.section", translate("form.system"), stCont, FormSection.Level.SUB_TITLE);

		SelectionValues layoutKV = new SelectionValues();
		layoutKV.add(entry("1", translate("form.layout.single")));
		layoutKV.add(entry("2", translate("form.layout.two")));
		layoutEl = uifactory.addRadiosVertical("form.layout", layoutSection, layoutKV.keys(), layoutKV.values());
		layoutEl.addActionListener(FormEvent.ONCLICK);

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		stCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	private void loadDefaultConfigValues() {
		String displayType = stModule.getDisplayType();
		if (displayTypeEl.containsKey(displayType)) {
			displayTypeEl.select(displayType, true);
		} else {
			displayTypeEl.select(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC, true);
		}

		layoutEl.select(stModule.getColumns() == 2 ? "2" : "1", true);

		updateUI();
	}

	private void updateUI() {
		String selectedKey = displayTypeEl.getSelectedKey();
		layoutSection.setVisible(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC.equals(selectedKey)
				|| STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(selectedKey));
	}

	private void updateDefaultConfigValues() {
		stModule.setDisplayType(displayTypeEl.getSelectedKey());
		if (layoutSection.isVisible()) {
			stModule.setColumns("2".equals(layoutEl.getSelectedKey()) ? 2 : 1);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == displayTypeEl) {
			updateUI();
		} else if (source == resetDefaultsButton) {
			confirmReset = activateYesNoDialog(ureq, null, translate("course.node.confirm.reset"), confirmReset);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateDefaultConfigValues();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmReset) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				stModule.resetProperties();
				loadDefaultConfigValues();
			}
			// Fire this event regardless of yes, no or close
			// Little hack to prevent a dirty form after pressing reset button
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}
