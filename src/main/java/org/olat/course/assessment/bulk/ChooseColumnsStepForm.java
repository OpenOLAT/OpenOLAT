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
package org.olat.course.assessment.bulk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.assessment.model.BulkAssessmentColumnSettings;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentRow;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.ui.AssessedIdentityListController;

/**
 *
 * Initial date: 9.1.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseColumnsStepForm extends StepFormBasicController {

	private int numOfColumns;
	private SingleSelection scoreColumnEl;
	private SingleSelection passedColumnEl;
	private SingleSelection commentColumnEl;
	private SingleSelection userNameColumnEl;
	private OverviewDataModel overviewDataModel;
	private final BulkAssessmentColumnSettings columnsSettings;
	
	private final String translatedPassed;
	private final String translatedFailed;

	public ChooseColumnsStepForm(UserRequest ureq, WindowControl wControl, BulkAssessmentColumnSettings columnsSettings,
			StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.columnsSettings = columnsSettings;

		@SuppressWarnings("unchecked")
		List<String[]> splittedRows = (List<String[]>)getFromRunContext("splittedRows");
		if(!splittedRows.isEmpty()) {
			numOfColumns = splittedRows.get(0).length;
		}

		translatedPassed = FilterFactory.getHtmlTagsFilter().filter(translate("passed.true")).trim();
		translatedFailed = FilterFactory.getHtmlTagsFilter().filter(translate("passed.false")).trim();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("chooseColumns.title");
		setFormDescription("chooseColumns.description");
		setFormContextHelp("Using Course Tools#bulkassessment_map");

		CourseNode courseNode = (CourseNode)getFromRunContext("courseNode");
		BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode);

		String[] usernameKeys = new String[numOfColumns];
		String[] usernameValues = new String[numOfColumns];
		String[] otherKeys = new String[numOfColumns + 1];
		String[] otherValues = new String[numOfColumns + 1];
		for(int i=0; i<numOfColumns; i++) {
			usernameKeys[i] = "col" + i;
			usernameValues[i] = translate("column",  Integer.toString(i + 1));
			otherKeys[i] = "col" + i;
			otherValues[i] = translate("column",  Integer.toString(i + 1));
		}
		otherKeys[otherKeys.length - 1] = "col9999";
		otherValues[otherValues.length - 1] = translate("column.dontuse");

		FormLayoutContainer choosersCont = FormLayoutContainer.createDefaultFormLayout("choosers", getTranslator());
		choosersCont.setElementCssClass("o_sel_bulk_assessment_columns");
		choosersCont.setRootForm(mainForm);
		formLayout.add(choosersCont);

		int pos = 0;
		userNameColumnEl = uifactory.addDropdownSingleselect("table.header.identifier", choosersCont, usernameKeys, usernameValues, null);
		if(columnsSettings != null && columnsSettings.getUsernameColumn() < usernameKeys.length) {
			userNameColumnEl.select(getSelectedKey(pos++, columnsSettings.getUsernameColumn(), usernameKeys), true);
		} else if(usernameKeys.length > 0){
			userNameColumnEl.select(usernameKeys[Math.min(pos++, usernameKeys.length - 1)], true);
		}
		if(settings.isHasScore()) {
			scoreColumnEl = uifactory.addDropdownSingleselect("table.header.score", choosersCont, otherKeys, otherValues, null);
			if(columnsSettings != null && columnsSettings.getScoreColumn() < otherKeys.length) {
				scoreColumnEl.select(getSelectedKey(pos++, columnsSettings.getScoreColumn(), otherKeys), true);
			} else if(otherKeys.length > 0) {
				scoreColumnEl.select(otherKeys[Math.min(pos++, otherKeys.length - 1)], true);
			}
		}
		if(settings.isHasPassed() && settings.getCut() == null) {
			passedColumnEl = uifactory.addDropdownSingleselect("table.header.passed", choosersCont, otherKeys, otherValues, null);
			if(columnsSettings != null && columnsSettings.getPassedColumn() < otherKeys.length) {
				passedColumnEl.select(getSelectedKey(pos++, columnsSettings.getPassedColumn(), otherKeys), true);
			} else if(otherKeys.length > 0) {
				passedColumnEl.select(otherKeys[Math.min(pos++, otherKeys.length - 1)], true);
			}
		}
		if(settings.isHasUserComment()) {
			commentColumnEl = uifactory.addDropdownSingleselect("table.header.comment", choosersCont, otherKeys, otherValues, null);
			if(columnsSettings != null && columnsSettings.getCommentColumn() < otherKeys.length) {
				commentColumnEl.select(getSelectedKey(pos++, columnsSettings.getCommentColumn(), otherKeys), true);
			} else if(otherKeys.length > 0) {
				commentColumnEl.select(otherKeys[Math.min(pos++, otherKeys.length - 1)], true);
			}
		}

		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		for(int i=0; i<numOfColumns; i++) {
			DefaultFlexiColumnModel colModel = new DefaultFlexiColumnModel("column", i);
			colModel.setHeaderLabel(translate("column", Integer.toString(i + 1)));
			tableColumnModel.addFlexiColumnModel(colModel);
		}

		@SuppressWarnings("unchecked")
		List<String[]> splittedRows = (List<String[]>)getFromRunContext("splittedRows");
		overviewDataModel = new OverviewDataModel(splittedRows, tableColumnModel);
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), "overviewList", overviewDataModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}

	private String getSelectedKey(int pos, int settings, String[] theKeys) {
		int selectionPos = pos;
		if(settings >= 0 && settings < theKeys.length) {
			selectionPos = settings;
		}
		return theKeys[selectionPos];
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(userNameColumnEl != null) {
			userNameColumnEl.clearError();
			if(!userNameColumnEl.isOneSelected()) {
				userNameColumnEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		BulkAssessmentDatas datas = (BulkAssessmentDatas)getFromRunContext("datas");
		List<BulkAssessmentRow> rows = datas.getRows();
		Map<String, BulkAssessmentRow> assessedIdToRow = new HashMap<>();
		for(BulkAssessmentRow row:rows) {
			assessedIdToRow.put(row.getAssessedId(), row);
		}

		BulkAssessmentColumnSettings settings = datas.getColumnsSettings();
		if(settings == null) {
			settings = new BulkAssessmentColumnSettings();
			datas.setColumnsSettings(settings);
		}
		settings.setUsernameColumn(getColumnPosition(userNameColumnEl));
		settings.setScoreColumn(getColumnPosition(scoreColumnEl));
		settings.setPassedColumn(getColumnPosition(passedColumnEl));
		settings.setCommentColumn(getColumnPosition(commentColumnEl));

		List<String[]> splittedRows = overviewDataModel.getObjects();
		for(String[] values:splittedRows) {
			createRow(values, settings, rows, assessedIdToRow);
		}

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private int getColumnPosition(SingleSelection el) {
		if(el == null) return 9999;
		String selectedKey = el.getSelectedKey();
		if(selectedKey == null || selectedKey.length() < 4) return 9999;
		String pos = selectedKey.substring(3);
		if(StringHelper.isLong(pos)) {
			return Integer.parseInt(pos);
		}
		return 9999;
	}


	/**
	 * Create a row object from an array of strings. The array
	 * is assessed identity identifier, score, status, comment.
	 * @param values
	 * @return
	 */
	private void createRow(String[] values, BulkAssessmentColumnSettings settings,
			List<BulkAssessmentRow> rows, Map<String, BulkAssessmentRow> assessedIdToRow) {
		int valuesLength = values.length;
		if(valuesLength <= 0 || valuesLength <= settings.getUsernameColumn()) {
			return;
		}

		String identifyer = values[settings.getUsernameColumn()];
		identifyer = identifyer.trim();
		if (!StringHelper.containsNonWhitespace(identifyer)) {
			identifyer = "-";
		}

		BulkAssessmentRow row;
		if(assessedIdToRow.containsKey(identifyer)) {
			row = assessedIdToRow.get(identifyer);
		} else {
			row = new BulkAssessmentRow();
			row.setAssessedId(identifyer);
			rows.add(row);
		}

		if(valuesLength > settings.getScoreColumn()) {
			String scoreStr = values[settings.getScoreColumn()];
			scoreStr= scoreStr.trim();
			Float score;
			if (StringHelper.containsNonWhitespace(scoreStr)) {
				try {
					// accept writing with , or .
					score = Float.parseFloat(scoreStr.replace(',', '.'));
				} catch (NumberFormatException e) {
					score = null;
				}
			} else {
				// only set new numbers, ignore everything else
				score = null;
			}
			row.setScore(score);
		}

		if(valuesLength > settings.getPassedColumn()) {
			String passedStr = values[settings.getPassedColumn()];
			passedStr = passedStr.trim();
			
			

			Boolean passed;
			if ("y".equalsIgnoreCase(passedStr)
					|| "yes".equalsIgnoreCase(passedStr)
					|| "passed".equalsIgnoreCase(passedStr)
					|| "true".equalsIgnoreCase(passedStr)
					|| "1".equalsIgnoreCase(passedStr)
					|| translatedPassed.equalsIgnoreCase(passedStr)) {
				passed = Boolean.TRUE;
			} else if ("n".equalsIgnoreCase(passedStr)
					|| "no".equalsIgnoreCase(passedStr)
					|| "false".equalsIgnoreCase(passedStr)
					|| "failed".equalsIgnoreCase(passedStr)
					|| "0".equalsIgnoreCase(passedStr)
					|| translatedFailed.equalsIgnoreCase(passedStr)) {
				passed = Boolean.FALSE;
			} else {
				// only set defined values, ignore everything else
				passed = null;
			}
			row.setPassed(passed);
		}

		if(valuesLength > settings.getCommentColumn()) {
			String commentStr = values[settings.getCommentColumn()];
			commentStr= commentStr.trim();
			if(commentStr.isEmpty()) {
				// ignore empty values
				row.setComment(null);
			} else if("\"\"".equals(commentStr) || "''".equals(commentStr)) {
				row.setComment("");
			} else {
				row.setComment(commentStr);
			}
		}
	}

	private static class OverviewDataModel extends DefaultFlexiTableDataModel<String[]> {
		
		public OverviewDataModel(List<String[]> nodes, FlexiTableColumnModel columnModel) {
			super(nodes, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			String[] data = getObject(row);
			if(data != null && col >= 0 && col < data.length) {
				return data[col];
			}
			return null;
		}
	}
}
