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
package org.olat.modules.library.ui;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.util.vfs.VFSItem;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * TableModel for the ReviewController.
 * 
 * <P>
 * Initial Date:  5 oct. 2009 <br>
 *
 * @author twuersch, srosse, http://www.frentix.com
 */
public class ReviewTableDataModel extends BaseTableDataModelWithoutFilter<VFSItem> {
	
	protected enum Columns {filename, uploader, date, download, reject, accept}
	
	private final List<VFSItem> documents;
	private final DateFormat format;

	public ReviewTableDataModel(List<VFSItem> documents, Locale locale) {
		this.documents = documents;
		format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
	}
	
	@Override
	public int getRowCount() {
		return documents.size();
	}

	@Override
	public int getColumnCount() {
		return Columns.values().length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		VFSItem item = documents.get(row);
		
		switch(Columns.values()[col]) {
			case filename: return item.getName();
			case uploader: {
				VFSMetadata metadata = item.getMetaInfo();
				if(metadata != null && metadata.getFileInitializedBy() != null) {
					return UserManager.getInstance().getUserDisplayName(metadata.getFileInitializedBy());
				}
				return "-";
			}
			case date: {
				VFSMetadata metadata = item.getMetaInfo();
				if(metadata != null) {
					return format.format(metadata.getLastModified());
				}
				return "";
			}
			default: return item;
		}
	}
}