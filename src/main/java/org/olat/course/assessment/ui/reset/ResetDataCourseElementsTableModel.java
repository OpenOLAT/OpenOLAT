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
package org.olat.course.assessment.ui.reset;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSelectionDelegate;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataCourseElementsTableModel extends DefaultFlexiTreeTableDataModel<CourseNodeRow>
implements FlexiTableSelectionDelegate<CourseNodeRow> {
	
	private static final ElementsCols[] COLS = ElementsCols.values();
	
	private final Locale locale;
	
	public ResetDataCourseElementsTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// 
	}
	
	@Override
	public boolean isSelectable(int row) {
		return getObject(row) != null;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CourseNodeRow node = getObject(row);
		switch(COLS[col]) {
			case node: return node;
			case shortTitle: return node.getShortTitle();
			case longTitle: return node.getLongTitle();
			case type: return getType(node);
			default: return "ERROR";
		}
	}
	
	@Override
	public boolean hasChildren(int row) {
		CourseNodeRow node = getObject(row);
		return node.hasChildren();
	}
	
	private String getType(CourseNodeRow node) {
		String type = node.getType();
		return CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(type)
				.getLinkText(locale);
	}
	
	@Override
	public List<CourseNodeRow> getSelectedTreeNodes() {
		return backupRows.stream()
				.filter(CourseNodeRow::isSelected)
				.collect(Collectors.toList());
	}

	@Override
	public List<CourseNodeRow> getAllRows() {
		return new ArrayList<>(backupRows);
	}
	
	public enum ElementsCols implements FlexiColumnDef {
		node("table.header.node"),
		shortTitle("table.header.short.title"),
		longTitle("table.header.long.title"),
		type("table.header.type");
		
		private final String i18nKey;
		
		private ElementsCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
