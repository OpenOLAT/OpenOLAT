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

import java.util.ArrayList;
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
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.ui.RubricsTotalDataModel.RubricsTotalCols;
import org.olat.modules.forms.ui.model.RubricStatistic;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricsTotalController extends FormBasicController {

	private final Form form;
	private final List<? extends EvaluationFormSessionRef> sessions;
	
	private RubricsTotalDataModel dataModel;
	private FlexiTableElement tableEl;
	
	public RubricsTotalController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions) {
		super(ureq, wControl, LAYOUT_HORIZONTAL);
		this.form = form;
		this.sessions = sessions;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricsTotalCols.name));
		DefaultFlexiColumnModel avgColumn = new DefaultFlexiColumnModel(RubricsTotalCols.avg);
		avgColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(avgColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RubricsTotalCols.scale));
		
		dataModel = new RubricsTotalDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "total", dataModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		loadModel();
	}

	private void loadModel() {
		List<RubricsTotalRow> rubricTotalRows = new ArrayList<>();
		for (AbstractElement element: form.getElements()) {
			if (element instanceof Rubric) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = new RubricStatistic(rubric, sessions);
				String name = translate("rubric.report.total", new String[] {rubric.getName()});	
				Double avg = rubricStatistic.getTotalStatistic().getAvg();
				ScaleType scaleType = rubric.getScaleType();
				double minStep = scaleType.getStepValue(rubric.getSteps(), rubric.getStart());
				double maxStep = scaleType.getStepValue(rubric.getSteps(), rubric.getEnd());
				String scale = translate("rubric.report.scale", new String[] { String.valueOf(minStep), String.valueOf(maxStep)});
				RubricsTotalRow row = new RubricsTotalRow(name, avg, scale);
				rubricTotalRows.add(row);
			}
		}
		dataModel.setObjects(rubricTotalRows);
		tableEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
