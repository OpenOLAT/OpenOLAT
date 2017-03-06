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
package org.olat.modules.qpool.ui;

import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QuestionItemShort;

/**
 * 
 * Initial date: 2 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConversionConfirmationController extends FormBasicController {
	
	private SingleSelection formatEl;
	private FormLayoutContainer exampleHelpEl;
	
	private final Map<String,List<QuestionItemShort>> formatToItems;
	
	public ConversionConfirmationController(UserRequest ureq, WindowControl wControl,
			Map<String,List<QuestionItemShort>> formatToItems) {
		super(ureq, wControl);
		this.formatToItems = formatToItems;
		initForm(ureq);
	}
	
	public String getSelectedFormat() {
		return formatEl.isOneSelected() ? formatEl.getSelectedKey() : null;
	}
	
	public List<QuestionItemShort> getSelectedItems() {
		return formatToItems.get(getSelectedFormat());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] formatKeys = formatToItems.keySet().toArray(new String[formatToItems.size()]);

		formatEl = uifactory.addDropdownSingleselect("format", "convert.format", formLayout, formatKeys, formatKeys, null);
		formatEl.addActionListener(FormEvent.ONCHANGE);
		if(formatKeys.length > 0) {
			formatEl.select(formatKeys[0], true);
		}
		
		// only info about QTI 1.2 to 2.1 as it's the only option for now
		String page = velocity_root + "/example_conversion.html";
		exampleHelpEl = FormLayoutContainer.createCustomFormLayout("example.help", "example.help", getTranslator(), page);
		formLayout.add(exampleHelpEl);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("convert.item", buttonsCont);
		updateInfos();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void updateInfos() {
		String format = formatEl.getSelectedKey();
		List<QuestionItemShort> items = formatToItems.get(format);
		setFormInfo("convert.item.msg", new String[]{ Integer.toString(items.size()) });
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(formatEl == source) {
			updateInfos();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}