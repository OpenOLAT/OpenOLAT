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
package org.olat.modules.curriculum.ui.widgets;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;

/**
 * 
 * Initial date: 9 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlocksWidgetTableModel extends DefaultFlexiTableDataModel<LectureBlockWidgetRow> implements FlexiTableCssDelegate {

	private static final BlockCols[] COLS = BlockCols.values();
	
	private Date now = new Date();
	
	public LectureBlocksWidgetTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}
	
	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		now = new Date();
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int row) {
		final LectureBlockWidgetRow blockRow = getObject(row);
		if(blockRow != null && blockRow.getLectureBlock().getStartDate() != null) {
			Date startDate = blockRow.getLectureBlock().getStartDate();
			if(DateUtils.isSameDay(startDate, now)) {
				if(blockRow.isNextScheduledEvent()) {
					return "o_curriculum_today o_next";	
				}
				
				LectureBlockVirtualStatus status = blockRow.getVirtualStatus();
				if(status == LectureBlockVirtualStatus.RUNNING) {
					return "o_curriculum_today o_running";	
				}
				return "o_curriculum_today";
			}
		}
		return "o_curriculum_date";
	}

	@Override
	public Object getValueAt(int row, int col) {
		final LectureBlockWidgetRow blockRow = getObject(row);
		final LectureBlock lectureBlock = blockRow.getLectureBlock();
		return switch(COLS[col]) {
			case key -> lectureBlock.getKey();
			case externalId -> lectureBlock.getExternalId();
			case title -> lectureBlock.getTitle();
			case location -> blockRow;
			case date -> lectureBlock.getStartDate();
			case startTime -> lectureBlock.getStartDate();
			case status -> blockRow;
			default -> "ERROR";
		};
	}
	
	public enum BlockCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		externalId("table.header.external.id"),
		title("table.header.title"),
		location("table.header.location"),
		date("table.header.date"),
		startTime("table.header.start.time"),
		status("table.header.status");
		
		private final String i18nHeaderKey;
		
		private BlockCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
