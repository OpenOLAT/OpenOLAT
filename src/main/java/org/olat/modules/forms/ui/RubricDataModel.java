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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;

/**
 * 
 * Initial date: 21.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricDataModel extends DefaultFlexiTableDataModel<RubricRow> implements FlexiTableFooterModel {
	
	private final String footerHeader;
	private RubricRow totalRow;
	
	public RubricDataModel(FlexiTableColumnModel columnsModel, String footerHeader) {
		super(columnsModel);
		this.footerHeader = footerHeader;
	}

	@Override
	public String getFooterHeader() {
		return footerHeader;
	}
	
	public void setObjects(List<RubricRow> objects, RubricRow totalRow) {
		super.setObjects(objects);
		this.totalRow = totalRow;
	}

	@Override
	public Object getValueAt(int row, int col) {
		RubricRow rubricRow = getObject(row);
		return getValueAt(rubricRow, col);
	}

	public Object getValueAt(RubricRow rubricRow, int col) {
		int offset = rubricRow.getNumberOfSteps();
		if (col == 0) {
			return rubricRow.getStartLabel();
		}
		if (rubricRow.hasEndLabel()) {
			if (col - offset == 1)  {
				return rubricRow.getEndLabel();
			}
			offset++;
		}
		if (rubricRow.isNoResponseEnabled()) {
			if (col - offset == 1) {
				return rubricRow.getNumberOfNoResponses();
			}
			offset++;
		}
		if (col - offset == 1) {
			return rubricRow.getNumberOfResponses();
		}
		if (rubricRow.hasWeight()) {
			if (col - offset == 2) {
				return rubricRow.getWeight();
			}
			offset++;
		}
		if (col - offset == 2) {
			return EvaluationFormFormatter.formatDouble(rubricRow.getMedian());
		}
		if (col - offset == 3) {
			return EvaluationFormFormatter.formatDouble(rubricRow.getVariance());
		}
		if (col - offset == 4) {
			return EvaluationFormFormatter.formatDouble(rubricRow.getSdtDev());
		}
		if (col - offset == 5) {
			return rubricRow.getAvg();
		}
		return rubricRow.getStepCount(col);
	}

	@Override
	public Object getFooterValueAt(int col) {
		return getValueAt(totalRow, col);
	}
	
	public enum RubricReportCols implements FlexiColumnDef {
		startLabel("rubric.report.start.label.title"),
		endLabel("rubric.report.end.lable.title"),
		numberOfNoResponses("rubric.report.number.no.responses.abrev"),
		numberOfResponses("rubric.report.number.responses.abrev"),
		weight("rubric.report.weight.abrev"),
		median("rubric.report.median.abrev"),
		variance("rubric.report.variance.abrev"),
		stdDev("rubric.report.sdtdev.abrev"),
		avg("rubric.report.avg.abrev");
		
		private final String i18nKey;
		
		private RubricReportCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
