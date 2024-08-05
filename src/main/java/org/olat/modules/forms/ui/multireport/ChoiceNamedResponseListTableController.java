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
package org.olat.modules.forms.ui.multireport;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.multireport.ChoiceNamedResponseListTableModel.ChoiceResponseListCols;

/**
 * 
 * Initial date: 5 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceNamedResponseListTableController extends FormBasicController {

	private FlexiTableElement tableEl;
	private ChoiceNamedResponseListTableModel tableModel;
	
	private final List<String> fullNames;
	
	public ChoiceNamedResponseListTableController(UserRequest ureq, WindowControl wControl, List<String> fullNames, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "response_fullnames_list", mainForm);
		setTranslator(Util.createPackageTranslator(EvaluationFormReportController.class, getLocale(), getTranslator()));
		this.fullNames = fullNames;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceResponseListCols.user));
		
		tableModel = new ChoiceNamedResponseListTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "fullnames", tableModel, 500, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setEmptyTableMessageKey("warning.nobody.choose.answer");
	}
	
	private void loadModel() {
		List<ChoiceNamedResponseRow> rows = fullNames.stream()
				.map(ChoiceNamedResponseRow::new)
				.collect(Collectors.toList());
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
