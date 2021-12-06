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
package org.olat.course.nodes.cl.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxAssessmentDataModel extends DefaultFlexiTableDataModel<CheckboxAssessmentRow>
	implements SortableFlexiTableDataModel<CheckboxAssessmentRow> {
	
	private final Locale locale;

	public CheckboxAssessmentDataModel(List<CheckboxAssessmentRow> datas, FlexiTableColumnModel columnModel, Locale locale) {
		super(datas, columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		CheckboxAssessmentDataModelSorter sorter = new CheckboxAssessmentDataModelSorter(orderBy, this, locale);
		List<CheckboxAssessmentRow> views = sorter.sort();
		super.setObjects(views);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CheckboxAssessmentRow box = getObject(row);
		return getValueAt(box, col);
	}
		
	@Override
	public Object getValueAt(CheckboxAssessmentRow row, int col) {
		if(col == Cols.check.ordinal()) {
			return row.getCheckedEl();
		} else if(col == Cols.points.ordinal()) {
			return row.getPointEl();
		} else if(col >= CheckListAssessmentDataModel.USER_PROPS_OFFSET && col < CheckListAssessmentDataModel.CHECKBOX_OFFSET) {
			int propIndex = col - CheckListAssessmentDataModel.USER_PROPS_OFFSET;
			return row.getIdentityProp(propIndex);
		}
		return row;
	}
	
	public enum Cols {
		check("box.check"),
		points("box.points");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
