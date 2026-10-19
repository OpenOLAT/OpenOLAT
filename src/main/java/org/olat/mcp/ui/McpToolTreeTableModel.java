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
package org.olat.mcp.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.mcp.tool.McpToolProvider;

/**
 * The tools as a tree: the first level is the pack, the second level the
 * object with its reading tool and its changing tool. In the read-only view
 * only the granted tools are shown, in the editable view every tool has a
 * checkbox and every pack the links to select its tools.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class McpToolTreeTableModel extends DefaultFlexiTreeTableDataModel<McpToolTreeTableModel.ToolTreeRow> {

	private static final ToolTreeCols[] COLS = ToolTreeCols.values();

	public McpToolTreeTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
		setHasOpenCloseAll(false);
	}

	@Override
	public boolean hasChildren(int row) {
		ToolTreeRow treeRow = getObject(row);
		return treeRow != null && treeRow.isPack();
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// no search and no filter
	}

	@Override
	public Object getValueAt(int row, int col) {
		ToolTreeRow treeRow = getObject(row);
		return switch(COLS[col]) {
			case object -> treeRow.getLabel();
			case readTool -> treeRow.isPack()
					? treeRow.getReadLink()
					: getToolValue(treeRow.getReadTool(), treeRow.getReadCheckbox(), treeRow.isReadExists());
			case modifyTool -> treeRow.isPack()
					? treeRow.getAllLink()
					: getToolValue(treeRow.getModifyTool(), treeRow.getModifyCheckbox(), treeRow.isModifyExists());
		};
	}

	/**
	 * @return The tool with its checkbox in the editable view, the granted
	 *         tool in the read-only view, {@link ToolState#notGranted} if the
	 *         object has a tool of this scope which is not granted,
	 *         {@link ToolState#none} if it has no such tool
	 */
	private static Object getToolValue(McpToolProvider tool, MultipleSelectionElement checkbox, boolean exists) {
		if(tool != null) {
			return checkbox == null ? tool : new ToolSelection(tool, checkbox);
		}
		return exists ? ToolState.notGranted : ToolState.none;
	}

	/**
	 * A tool with its checkbox in the editable view.
	 */
	public record ToolSelection(McpToolProvider tool, MultipleSelectionElement checkbox) {
		//
	}

	public enum ToolState {
		notGranted,
		none
	}

	public enum ToolTreeCols implements FlexiColumnDef {
		object("tools.col.object"),
		readTool("tools.col.read"),
		modifyTool("tools.col.modify");

		private final String i18nKey;

		private ToolTreeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

	/**
	 * A pack (no parent, no tools, the selection links in the editable view)
	 * or an object of a pack. The tool of a column is null if the object has
	 * no such tool or if it is not granted, the checkbox is null in the
	 * read-only view.
	 */
	public static class ToolTreeRow implements FlexiTreeTableNode {

		private final ToolTreeRow parent;
		private final String label;
		private McpToolProvider readTool;
		private McpToolProvider modifyTool;
		private boolean readExists;
		private boolean modifyExists;
		private MultipleSelectionElement readCheckbox;
		private MultipleSelectionElement modifyCheckbox;
		private FormLink readLink;
		private FormLink allLink;

		public ToolTreeRow(ToolTreeRow parent, String label) {
			this.parent = parent;
			this.label = label;
		}

		@Override
		public ToolTreeRow getParent() {
			return parent;
		}

		@Override
		public String getCrump() {
			return label;
		}

		public boolean isPack() {
			return parent == null;
		}

		public String getLabel() {
			return label;
		}

		public McpToolProvider getReadTool() {
			return readTool;
		}

		public McpToolProvider getModifyTool() {
			return modifyTool;
		}

		public boolean isReadExists() {
			return readExists;
		}

		public boolean isModifyExists() {
			return modifyExists;
		}

		public MultipleSelectionElement getReadCheckbox() {
			return readCheckbox;
		}

		public MultipleSelectionElement getModifyCheckbox() {
			return modifyCheckbox;
		}

		public FormLink getReadLink() {
			return readLink;
		}

		public FormLink getAllLink() {
			return allLink;
		}

		void setLinks(FormLink readLink, FormLink allLink) {
			this.readLink = readLink;
			this.allLink = allLink;
		}

		/**
		 * @param provider The tool
		 * @param granted true to show the tool, false to mark it as not granted
		 * @param checkbox The checkbox of the tool in the editable view, null in the read-only view
		 * @return true if the column of the scope of the tool was free
		 */
		boolean setTool(McpToolProvider provider, boolean granted, MultipleSelectionElement checkbox) {
			if(provider.getScope().isWrite()) {
				if(modifyExists) {
					return false;
				}
				modifyExists = true;
				modifyTool = granted ? provider : null;
				modifyCheckbox = checkbox;
			} else {
				if(readExists) {
					return false;
				}
				readExists = true;
				readTool = granted ? provider : null;
				readCheckbox = checkbox;
			}
			return true;
		}
	}
}
