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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;


/**
 * 
 * Mark a datamodel as exportable
 * 
 * Initial date: 11.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportableFlexiTableDataModelDelegate  {
	
	public MediaResource export(FlexiTableComponent ftC, Translator translator) {
		List<FlexiColumnModel> columns = getColumnModels(ftC.getFormItem());
		return export(ftC, columns, translator);
	}
	
	public MediaResource export(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator) {
		XlsFlexiTableExporter exporter = new XlsFlexiTableExporter();
		return exporter.export(ftC, columns, translator);
	}
	
	private final List<FlexiColumnModel> getColumnModels(FlexiTableElementImpl tableEl) {
		FlexiTableDataModel<?> dataModel = tableEl.getTableDataModel();
		FlexiTableColumnModel columnModel = dataModel.getTableColumnModel();
		int numOfColumns = columnModel.getColumnCount();
		List<FlexiColumnModel> columns = new ArrayList<>(numOfColumns);
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnModel.getColumnModel(i);
			if((tableEl.isColumnModelVisible(column)) && column.isExportable()) {
				columns.add(column);
			}
		}
		return columns;
	}
}
