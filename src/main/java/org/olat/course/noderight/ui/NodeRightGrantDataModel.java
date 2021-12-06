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
package org.olat.course.noderight.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.ui.NodeRightGrantDataModel.NodeRightGrantRow;

/**
 * 
 * Initial date: 28.10.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightGrantDataModel extends DefaultFlexiTableDataModel<NodeRightGrantRow>
implements SortableFlexiTableDataModel<NodeRightGrantRow> {
	
	private static final GrantCols[] COLS = GrantCols.values();
	private final Locale locale;
	
	public NodeRightGrantDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		NodeRightGrantRow right = getObject(row);
		return getValueAt(right, col);
	}

	@Override
	public Object getValueAt(NodeRightGrantRow row, int col) {
		switch(COLS[col]) {
			case type: return row.getType();
			case name: return row.getName();
			case start: return row.getStartEl();
			case end: return row.getEndEl();
			case delete: return row.getDeleteLink();
			default: return null;
		}
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<NodeRightGrantRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	public enum GrantCols implements FlexiSortableColumnDef {
		type("grant.type"),
		name("grant.name"),
		start("grant.start"),
		end("grant.end"),
		delete("delete");
		
		private final String i18nKey;
		
		private GrantCols(String i18nKey) {
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
	
	public static final class NodeRightGrantRow {

		private final NodeRightGrant grant;
		private NodeRightWrapper wrapper;
		private String type;
		private String name;
		private DateChooser startEl;
		private DateChooser endEl;
		private FormLink deleteLink;

		public NodeRightGrantRow(NodeRightGrant grant) {
			this.grant = grant;
		}

		public NodeRightGrant getGrant() {
			return grant;
		}

		public NodeRightWrapper getWrapper() {
			return wrapper;
		}

		public void setWrapper(NodeRightWrapper wrapper) {
			this.wrapper = wrapper;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public DateChooser getStartEl() {
			return startEl;
		}

		public void setStartEl(DateChooser startEl) {
			this.startEl = startEl;
		}

		public DateChooser getEndEl() {
			return endEl;
		}

		public void setEndEl(DateChooser endEl) {
			this.endEl = endEl;
		}

		public FormLink getDeleteLink() {
			return deleteLink;
		}

		public void setDeleteLink(FormLink deleteLink) {
			this.deleteLink = deleteLink;
		}

	}
}
