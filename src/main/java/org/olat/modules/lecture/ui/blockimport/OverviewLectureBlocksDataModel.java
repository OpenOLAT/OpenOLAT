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
package org.olat.modules.lecture.ui.blockimport;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 15 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewLectureBlocksDataModel extends DefaultFlexiTableDataModel<ImportedLectureBlock> {
	
	public OverviewLectureBlocksDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ImportedLectureBlock block = getObject(row);
		switch(BlockCols.values()[col]) {
			case status: return block;
			case externalId: return block.getLectureBlock().getExternalId();
			case title: return block.getLectureBlock().getTitle();
			case plannedLectures: return block.getLectureBlock().getPlannedLecturesNumber();
			case date:
			case startTime: return block.getLectureBlock().getStartDate();
			case endTime: return block.getLectureBlock().getEndDate();
			case compulsory: return block.getLectureBlock().isCompulsory();
			case teachers: return block;
			case participants: return block;
			case location: return block.getLectureBlock().getLocation();
			case description: return block.getLectureBlock().getDescription();
			case preparation: return block.getLectureBlock().getPreparation();
			case comment: return block.getLectureBlock().getComment();
			default: return "ERROR";
		}
	}
	
	public enum BlockCols implements FlexiSortableColumnDef {
		status("table.header.import.status"),
		externalId("table.header.external.ref"),
		title("lecture.title"),
		plannedLectures("table.header.planned.lectures"),
		date("table.header.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		compulsory("table.header.compulsory"),
		teachers("table.header.teachers"),
		participants("table.header.participants"),
		location("table.header.location"),
		description("table.header.description"),
		preparation("table.header.preparation"),
		comment("table.header.comment");

		private final String i18nKey;
		
		private BlockCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
