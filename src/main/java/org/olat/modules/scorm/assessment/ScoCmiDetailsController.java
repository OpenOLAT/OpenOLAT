/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.scorm.assessment;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.scorm.assessment.ScoCmiDetailsTableModel.CmiDataCols;

/**
 * 
 * Initial date: 19 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScoCmiDetailsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private ScoCmiDetailsTableModel tableModel;
	
	private final List<CmiData> cmiData;
	
	public ScoCmiDetailsController(UserRequest ureq, WindowControl wControl, List<CmiData> cmiData) {
		super(ureq, wControl, "cmi_data");
		this.cmiData = cmiData;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CmiDataCols.itemId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CmiDataCols.translatedKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CmiDataCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CmiDataCols.value));
		
		tableModel = new ScoCmiDetailsTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel() {
		tableModel.setObjects(cmiData);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
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
