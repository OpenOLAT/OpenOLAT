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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 14 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GuiDemoFlexiMultipleSelectionController extends FormBasicController {
	
	public GuiDemoFlexiMultipleSelectionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer defaultCont = uifactory.addDefaultFormLayout("def", null, formLayout);
		defaultCont.setFormTitle("Default");
		initSetOfCheckboxes("def", defaultCont);
		
		FormLayoutContainer verticalCont = uifactory.addVerticalFormLayout("vertical", null, formLayout);
		verticalCont.setFormTitle("Vertical");
		initSetOfCheckboxes("vert", verticalCont);
	}
	
	private void initSetOfCheckboxes(String suffix, FormItemContainer formLayout) {
		SelectionValues values = new SelectionValues();
		values.add(SelectionValues.entry("1", translate("select.1")));
		values.add(SelectionValues.entry("2", translate("select.2")));
		values.add(SelectionValues.entry("3", translate("select.3")));
		values.add(SelectionValues.entry("4", translate("select.4")));
		values.add(SelectionValues.entry("5", translate("select.5")));
		values.add(SelectionValues.entry("6", translate("select.6")));
		values.add(SelectionValues.entry("7", translate("select.7")));
		values.add(SelectionValues.entry("8", translate("select.8")));
		
		// Checkbox vertical one column
		uifactory.addCheckboxesVertical("vert.multi." + suffix, "multi.select", formLayout, values.keys(), values.values(), 1);
		// Checkbox vertical two columns
		uifactory.addCheckboxesVertical("vert.multi." + suffix + ".2col", "multi.select", formLayout, values.keys(), values.values(), 2);

		SelectionValues singleValues = new SelectionValues();
		singleValues.add(SelectionValues.entry("1", translate("select.alt.1")));
		uifactory.addCheckboxesVertical("vert.single." + suffix, "multi.select.single.value", formLayout, singleValues.keys(), singleValues.values(), 1);
		
		SelectionValues singleNoValues = new SelectionValues();
		singleNoValues.add(SelectionValues.entry("on", ""));
		uifactory.addCheckboxesVertical("vert.single.no.value." + suffix, "multi.select.single.no.value", formLayout, singleNoValues.keys(), singleNoValues.values(), 1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}