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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Choice;
import org.olat.modules.forms.model.xml.Choices;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.EvaluationFormReportController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.forms.ui.multireport.ChoiceResponsesTableModel.ChoiceResponseCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 5 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceResponsesTableController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FlexiTableElement tableEl;
	private ChoiceResponsesTableModel tableModel;
	private final VelocityContainer detailsVC;
	
	private final String choiceId;
	private final Choices choices;
	private final SessionFilter filter;
	private final ReportHelper reportHelper;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public ChoiceResponsesTableController(UserRequest ureq, WindowControl wControl,
			SingleChoice choice, SessionFilter filter, ReportHelper reportHelper, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "questions_table", rootForm);
		setTranslator(Util.createPackageTranslator(EvaluationFormReportController.class, getLocale(), getTranslator()));
		
		choiceId = choice.getId();
		choices = choice.getChoices();
		this.filter = filter;
		this.reportHelper = reportHelper;

		detailsVC = createVelocityContainer("response_details");
		
		initForm(ureq);
		loadModel();
	}
	
	public ChoiceResponsesTableController(UserRequest ureq, WindowControl wControl,
			MultipleChoice choice, SessionFilter filter, ReportHelper reportHelper, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "questions_table", rootForm);
		setTranslator(Util.createPackageTranslator(EvaluationFormReportController.class, getLocale(), getTranslator()));

		choiceId = choice.getId();
		choices = choice.getChoices();
		this.filter = filter;
		this.reportHelper = reportHelper;

		detailsVC = createVelocityContainer("response_details");
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if(rowObject instanceof ChoiceResponseRow sliderRow) {
			if(sliderRow.getDetailsControllerComponent() != null) {
				components.add(sliderRow.getDetailsControllerComponent());
			}
		}
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel choiceColumn = new DefaultFlexiColumnModel(ChoiceResponseCols.choiceResponse);
		choiceColumn.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.antisamy));
		columnsModel.addFlexiColumnModel(choiceColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChoiceResponseCols.numOfResponses));

		tableModel = new ChoiceResponsesTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "questions", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setMultiDetails(true);
	}
	
	private void loadModel() {
		List<EvaluationFormResponse> responses = evaluationFormManager.getResponses(List.of(choiceId), false, filter, Limit.all());

		List<Choice> choiceList = choices.asList();
		List<ChoiceResponseRow> rows = new ArrayList<>(choices.size());
		for(Choice choice:choiceList) {
			rows.add(forgeRow(choice, responses));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ChoiceResponseRow forgeRow(Choice choice, List<EvaluationFormResponse> responses) {
		ChoiceResponseRow row = new ChoiceResponseRow(choice);

		for(EvaluationFormResponse response:responses) {
			String responseId = response.getStringuifiedResponse();
			if(choice.getId().equals(responseId)) {
				Legend legend = reportHelper.getLegend(response.getSession());
				String fullName = legend == null ? "???" : legend.getName();
				row.addResponse(fullName);
			}
		}

		return row;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof DetailsToggleEvent toggleEvent) {
				ChoiceResponseRow row = tableModel.getObject(toggleEvent.getRowIndex());
				doOpenDetails(ureq, row);
			}
		}
	}
	
	private void doOpenDetails(UserRequest ureq, ChoiceResponseRow row) {
		if(row.getDetailsCtrl() != null) {
			removeAsListenerAndDispose(row.getDetailsCtrl());
			row.setDetailsCtrl(null);
		}
		
		List<String> fullNames = row.getResponsesFullNames();	
		ChoiceNamedResponseListTableController responsesListCtrl = new ChoiceNamedResponseListTableController(ureq, getWindowControl(),
				fullNames, mainForm);
		listenTo(responsesListCtrl);

		flc.add(responsesListCtrl.getInitialFormItem());
		row.setDetailsCtrl(responsesListCtrl);
		detailsVC.put(row.getDetailsControllerName(), row.getDetailsControllerComponent());
	}
	
}
