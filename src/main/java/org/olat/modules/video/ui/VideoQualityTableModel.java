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
package org.olat.modules.video.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Tablemodel for qualityversions listing 
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoQualityTableModel extends DefaultFlexiTableDataModel<QualityTableRow>{

	public VideoQualityTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		QualityTableRow video = getObject(row);
		switch(QualityTableCols.values()[col]) {
			case resolution: return video.getResolution();
			case dimension: return video.getDimension();
			case size: return video.getSize();
			case format: return video.getFormat();
			case delete: return video.getDeleteLink();
			default: return "";
		}
	}

	public enum QualityTableCols implements FlexiSortableColumnDef {
		resolution("quality.table.header.resolution"),
		dimension("quality.table.header.dimension"),
		size("quality.table.header.size"),
		format("quality.table.header.format"),
		delete("quality.table.header.delete");

		private final String i18nKey;

		private QualityTableCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String i18nKey() {
			return i18nKey;
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