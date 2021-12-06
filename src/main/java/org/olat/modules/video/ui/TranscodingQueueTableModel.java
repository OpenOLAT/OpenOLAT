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
 * Table Model for Transcoding Queue listing 
 *
 * Initial date: 30.09.2016
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class TranscodingQueueTableModel extends DefaultFlexiTableDataModel<TranscodingQueueTableRow>{

	public TranscodingQueueTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		TranscodingQueueTableRow video = getObject(row);
		switch(TranscodingQueueTableCols.values()[col]) {
			case resid: return video.getResid();
			case displayname: return video.getDisplayname();
			case creator: return video.getCreator();
			case creationDate: return video.getCreationDate();
			case dimension: return video.getDimension();
			case size: return video.getSize();
			case format: return video.getFormat();
			case delete: return video.getDeleteLink();
			case retranscode: return video.getRetranscodeLink();
			case failureReason: return video.getFailureReason();
			default: return "";
		}
	}

	public enum TranscodingQueueTableCols implements FlexiSortableColumnDef {
		resid("queue.table.header.resid"),
		displayname("queue.table.header.displayname"),
		creator("queue.table.header.creator"),
		creationDate("queue.table.header.creationDate"),
		dimension("quality.table.header.dimension"),
		size("quality.table.header.size"),
		format("quality.table.header.format"),
		delete("quality.table.header.delete"),
		retranscode("queue.table.header.retranscode"),
		failureReason("queue.table.failure.reason");

		private final String i18nKey;

		private TranscodingQueueTableCols(String i18nKey) {
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