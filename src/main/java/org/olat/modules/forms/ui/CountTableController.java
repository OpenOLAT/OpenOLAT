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
package org.olat.modules.forms.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.ui.CountDataModel.CountReportCols;
import org.olat.modules.forms.ui.component.PercentCellRenderer;
import org.olat.modules.forms.ui.model.CountDataSource;
import org.olat.modules.forms.ui.model.CountRatioResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CountTableController extends FormBasicController {
	
	private final CountDataSource dataSource;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public CountTableController(UserRequest ureq, WindowControl wControl, CountDataSource dataSource) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.dataSource = dataSource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CountReportCols.name));
		DefaultFlexiColumnModel countModel = new DefaultFlexiColumnModel(CountReportCols.count);
		countModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		countModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(countModel);
		DefaultFlexiColumnModel percentModel = new DefaultFlexiColumnModel(CountReportCols.percent, new PercentCellRenderer());
		percentModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		percentModel.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(percentModel);
		
		CountDataModel dataModel = new CountDataModel(columnsModel);
		
		List<CountRatioResult> calculateRatio = evaluationFormManager.calculateRatio(dataSource.getResponses());
		dataModel.setObjects(calculateRatio);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(),
				"counts" + CodeHelper.getRAMUniqueID(), dataModel, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
