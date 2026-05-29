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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

/**
 * Initial date: 2026-05-27<br>
 *
 * @author uhensler, urs.hensler@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiDemoObjectSelectionBrowserController extends FormBasicController {

	private final List<Identity> identities;
	private IdentityModel model;
	private FlexiTableElement tableEl;

	public GuiDemoObjectSelectionBrowserController(UserRequest ureq, WindowControl wControl, List<Identity> identities) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identities = identities;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel cols = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		cols.addFlexiColumnModel(new DefaultFlexiColumnModel("browser.name", 0));

		model = new IdentityModel(cols);
		model.setObjects(identities);
		tableEl = uifactory.addTableElement(getWindowControl(), "browser.table", model, 24, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);

		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttons);
		uifactory.addFormSubmitButton("browser.select", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selected = tableEl.getMultiSelectedIndex();
		List<String> keys = new ArrayList<>(selected.size());
		for (Integer idx : selected) {
			keys.add(model.getObject(idx).getKey().toString());
		}
		fireEvent(ureq, new ObjectSelectionBrowserEvent(keys));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private static class IdentityModel extends DefaultFlexiTableDataModel<Identity> {

		public IdentityModel(FlexiTableColumnModel cols) {
			super(cols);
		}

		@Override
		public Object getValueAt(int row, int col) {
			Identity identity = getObject(row);
			return identity.getUser().getProperty(UserConstants.FIRSTNAME) + " " + identity.getUser().getProperty(UserConstants.LASTNAME);
		}
	}
}
