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
package org.olat.course.style.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 22 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class SystemImageDataModel extends DefaultFlexiTableDataModel<SystemImageRow> {
	
	private static final SystemImageCols[] COLS = SystemImageCols.values();

	public SystemImageDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SystemImageRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	private Object getValueAt(SystemImageRow row, int col) {
		switch(COLS[col]) {
			case preview: return row;
			case filename: return row.getFilename();
			case translaton: return row.getTranslation();
			case edit: return Boolean.TRUE;
			case delete: return Boolean.TRUE;
			default: return null;
		}
	}
	
	public enum SystemImageCols implements FlexiColumnDef {
		preview("system.image.preview"),
		filename("system.image.filename"),
		translaton("system.image.translation"),
		edit("system.image.edit"),
		delete("system.image.delete");
		
		private final String i18nKey;
		
		private SystemImageCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
