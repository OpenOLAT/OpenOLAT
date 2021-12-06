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
import org.olat.core.gui.translator.Translator;

/**
 *
 * Initial date: 15.11.2016<br>
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class TranscodingTableModel extends DefaultFlexiTableDataModel<TranscodingRow>{

	private Translator translator;
	public TranscodingTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		TranscodingRow resolution = getObject(row);
		switch(TranscodingCols.values()[col]) {
			case resolutions: return translator.translate("quality.resolution." + resolution.getResolution());
			case sumVideos: return resolution.getSumVideos();
			case extern: return resolution.getExtern();
			case numberTranscodings: return resolution.getNumberTranscodings();
			case failedTranscodings: return resolution.getFailedTranscodings();
			case missingTranscodings: return resolution.getMissingTranscodings();
			case transcode: return resolution.isStartTranscodingAvailable();
			case delete: return resolution.getNumberTranscodings() > 0;
			default: return "";
		}
	}

	public enum TranscodingCols implements FlexiSortableColumnDef {
		resolutions("quality.table.header.resolution"),
		sumVideos("sum.video"),
		extern("extern.videos"),
		numberTranscodings("number.transcodings"),
		failedTranscodings("number.transcodings.failed"),
		missingTranscodings("missing.transcodings"),
		transcode("quality.transcode"),
		delete("quality.delete");

		private final String i18nKey;

		private TranscodingCols(String i18nKey) {
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
		
		public String i18nKey() {
			return i18nKey;
		}
	}

}