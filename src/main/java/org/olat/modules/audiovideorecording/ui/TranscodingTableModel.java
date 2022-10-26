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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import java.util.List;
import java.util.Locale;

/**
 * Initial date: 2022-10-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscodingTableModel extends DefaultFlexiTableDataModel<TranscodingTableRow> implements SortableFlexiTableDataModel<TranscodingTableRow> {
	private final Locale locale;

	public TranscodingTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TranscodingTableRow transcoding = getObject(row);
		return getValueAt(transcoding, col);
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<TranscodingTableRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(TranscodingTableRow row, int col) {
		switch (TranscodingTableCols.values()[col]) {
			case id: return row.getId();
			case fileName: return row.getFileName();
			case fileSize: return row.getFileSize();
			case creationDate: return row.getCreationDate();
			case status: return row.getTranscodingStatusString();
			case action: return row.getAction();
			default: return "";
		}
	}
}
