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
package org.olat.course.nodes.ms;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 1 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSStatisticDataModel extends DefaultFlexiTableDataModel<MSStatisticRow>
implements FlexiTableFooterModel, SortableFlexiTableDataModel<MSStatisticRow> {

	private static final String FORMAT_THREE_DECIMALS = "%.3f";
	private static final String FORMAT_TWO_DECIMALS = "%.2f";
	private static final String FORMAT_NO_DECIMALS = "%.0f";
	
	private final Locale locale;
	private final String footerHeader;
	private List<Double> footerValues;
	private String valueFormat = FORMAT_THREE_DECIMALS;
	
	MSStatisticDataModel(FlexiTableColumnModel columnsModel, String footerHeader, Locale locale) {
		super(columnsModel);
		this.footerHeader = footerHeader;
		this.locale = locale;
	}

	@Override
	public String getFooterHeader() {
		return footerHeader;
	}
	
	public void setObjects(List<MSStatisticRow> objects, List<Double> footerValues) {
		super.setObjects(objects);
		if (hasDoubles(objects)) {
			valueFormat = FORMAT_THREE_DECIMALS;
		} else {
			valueFormat = FORMAT_NO_DECIMALS;
		}
		
		this.footerValues = footerValues;
	}

	private boolean hasDoubles(List<MSStatisticRow> objects) {
		for (MSStatisticRow msStatisticRow : objects) {
			for (Double value: msStatisticRow.getRubricValues()) {
				if (value != null && value.doubleValue() % 1 != 0) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<MSStatisticRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		MSStatisticRow statistic = getObject(row);
		return getValueAt(statistic, col);
	}

	@Override
	public Object getValueAt(MSStatisticRow row, int col) {
		if (col >= MSStatisticController.RUBRIC_OFFSET) {
			int pos = col - MSStatisticController.RUBRIC_OFFSET;
			return formatValue(row.getRubricValue(pos));
		}
		if (col >= AssessmentToolConstants.USER_PROPS_OFFSET) {
			int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return null;
	}
	
	private String formatValue(Double value) {
		if (value == null || Double.isNaN(value)) {
			return null;
		}
		return String.format(valueFormat, value);
	}
	
	@Override
	public Object getFooterValueAt(int col) {
		if (footerValues != null && col >= MSStatisticController.RUBRIC_OFFSET) {
			int pos = col - MSStatisticController.RUBRIC_OFFSET;
			String format = FORMAT_THREE_DECIMALS.equals(valueFormat)? FORMAT_THREE_DECIMALS: FORMAT_TWO_DECIMALS;
			Double value = footerValues.get(pos);
			return value != null? String.format(format, value): null;
		}
		return null;
	}

}
