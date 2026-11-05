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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupController;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.RepeatableFieldGroupRow;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 28 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GuiDemoFlexiRepeatableFieldGroupController extends FormBasicController
		implements RepeatableFieldGroupDelegate<GuiDemoFlexiRepeatableFieldGroupController.DemoRow> {
	
	private RepeatableFieldGroupController<DemoRow> groupCtrl;
	
	public GuiDemoFlexiRepeatableFieldGroupController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo.rfg.title");
		
		groupCtrl = new RepeatableFieldGroupController<>(ureq, getWindowControl(), mainForm, getTranslator(),
				translate("guidemo.rfg.object"), translate("guidemo.rfg.add"), this);
		listenTo(groupCtrl);
		formLayout.add("appointments", groupCtrl.getInitialFormItem());
		for(int i = 0; i < 3; i++) {
			DemoRow row = groupCtrl.addRow();
			row.getDateEl().setDate(DateUtils.addDays(DateUtils.setTime(ureq.getRequestTimestamp(), 0, 0, 0), i));
			row.getValue1El().setValue(translate("guidemo.rfg.value1"));
			row.getValue2El().setValue(translate("guidemo.rfg.value2"));
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	@Override
	public DemoRow createRow(String id, FormItemContainer container) {
		DateChooser dateEl = uifactory.addDateChooser(id + "_date", "guidemo.rfg.date", null, container);
		dateEl.setMandatory(true);
		TextElement value1El = uifactory.addTextElement(id + "_value1", "guidemo.rfg.label1", 100, "", container);
		value1El.setMandatory(true);
		TextElement value2El = uifactory.addTextElement(id + "_value2", "guidemo.rfg.label2", 100, "", container);
		return new DemoRow(dateEl, value1El, value2El);
	}
	
	@Override
	public void copyRow(DemoRow source, DemoRow target) {
		target.getDateEl().setDate(source.getDateEl().getDate());
		target.getValue1El().setValue(source.getValue1El().getValue());
		target.getValue2El().setValue(source.getValue2El().getValue());
	}
	
	@Override
	public String getColumnCssClass(int column) {
		return column == 0 ? "o_rfg_col_s" : null;
	}
	
	@Override
	public boolean validateRow(DemoRow row) {
		boolean allOk = true;
		if(!row.getDateEl().hasError() && row.getDateEl().getDate() == null) {
			row.getDateEl().setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		if(!StringHelper.containsNonWhitespace(row.getValue1El().getValue())) {
			row.getValue1El().setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		showInfo("guidemo.rfg.saved", Integer.toString(groupCtrl.getRows().size()));
	}
	
	public static class DemoRow implements RepeatableFieldGroupRow {
		
		private final DateChooser dateEl;
		private final TextElement value1El;
		private final TextElement value2El;
		
		private DemoRow(DateChooser dateEl, TextElement value1El, TextElement value2El) {
			this.dateEl = dateEl;
			this.value1El = value1El;
			this.value2El = value2El;
		}
		
		public DateChooser getDateEl() {
			return dateEl;
		}
		
		public TextElement getValue1El() {
			return value1El;
		}
		
		public TextElement getValue2El() {
			return value2El;
		}
		
		@Override
		public List<FormItem> getFormItems() {
			return List.of(dateEl, value1El, value2El);
		}
	}
	
}
