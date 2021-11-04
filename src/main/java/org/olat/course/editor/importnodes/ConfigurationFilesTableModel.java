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
package org.olat.course.editor.importnodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 5 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationFilesTableModel extends DefaultFlexiTreeTableDataModel<ConfigurationFileRow> {
	
	private static final FilesCols[] COLS = FilesCols.values();
	
	private final Translator translator;
	
	public ConfigurationFilesTableModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// 
	}
	
	@Override
	public boolean isSelectable(int row) {
		ConfigurationFileRow node = getObject(row);
		return !node.isParentLine();
	}

	public List<ConfigurationFileRow> getAllObjects() {
		return new ArrayList<>(backupRows);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ConfigurationFileRow node = getObject(row);
		switch(COLS[col]) {
			case file: return node;
			case size: return getSize(node);
			case usedBy: return node;
			case messages: return node.isSelected() || node.isParentLine() ? node.getMessage() : null;
			case tools: return node.getToolLink();
			default: return "ERROR";
		}
	}
	
	private String getSize(ConfigurationFileRow node) {
		VFSItem item = node.getItem();
		if(item instanceof VFSLeaf) {
			return Formatter.formatBytes(((VFSLeaf)item).getSize());	
		}
		
		int numOfChildren = node.getNumOfChildren();
		if(numOfChildren > 1) {
			return translator.translate("num.of.elements", new String[] { Integer.toString(node.getNumOfChildren()) });
		}
		return translator.translate("num.of.element", new String[] { Integer.toString(node.getNumOfChildren()) });
	}
	
	@Override
	public boolean hasChildren(int row) {
		ConfigurationFileRow node = getObject(row);
		return node.getItem() instanceof VFSContainer && node.getNumOfChildren() > 0;
	}
	
	@Override
	public ConfigurationFilesTableModel createCopyWithEmptyList() {
		return new ConfigurationFilesTableModel(getTableColumnModel(), translator);
	}
	
	public enum FilesCols implements FlexiColumnDef {
		file("table.header.file"),
		size("table.header.size"),
		usedBy("table.header.used.by"),
		messages("table.header.messages"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private FilesCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
