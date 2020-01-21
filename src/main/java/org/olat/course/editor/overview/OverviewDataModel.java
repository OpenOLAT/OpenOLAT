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
package org.olat.course.editor.overview;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewDataModel extends DefaultFlexiTreeTableDataModel<OverviewRow> {

	public OverviewDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		OverviewRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		OverviewRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	public Object getValueAt(OverviewRow row, int col) {
		switch(OverviewCols.values()[col]) {
			case node: return row;
			case shortTitle: return row.getCourseNode().getShortTitle();
			case longTitle: return row.getCourseNode().getLongTitle();
			case learningObjectives: return row.getCourseNode().getLearningObjectives();
			case display: return row.getTranslatedDisplayOption();
			case duration: return row.getDuration();
			case obligation: return row.getTranslatedObligation();
			case start: return row.getStart();
			case trigger: return row.getTranslatedTrigger();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<OverviewRow> createCopyWithEmptyList() {
		return new OverviewDataModel(getTableColumnModel());
	}
	
	public enum OverviewCols implements FlexiColumnDef {
		node("table.header.node"),
		shortTitle("table.header.short.title"),
		longTitle("table.header.long.title"),
		learningObjectives("table.header.learning.objectives"),
		display("table.header.display"),
		duration("table.header.duration"),
		obligation("table.header.obligation"),
		start("table.header.start"),
		trigger("table.header.trigger");
		
		private final String i18nKey;
		
		private OverviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

	}
}
