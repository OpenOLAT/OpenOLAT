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
package org.olat.modules.fo.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.ui.main.AbstractMemberListController;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumMessageDataModel extends DefaultFlexiTableDataModel<MessageLightView>
	implements SortableFlexiTableDataModel<MessageLightView> {
	
	private Translator translator;
	
	public ForumMessageDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			if("natural".equals(orderBy.getKey())) {
				//System.out.println();
			} else {
				List<MessageLightView> views = new ForumMessageDataModelSort(orderBy, this, null).sort();
				super.setObjects(views);
			}
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		MessageLightView view = getObject(row);
		return getValueAt(view, col);
	}

	@Override
	public Object getValueAt(MessageLightView row, int col) {
		if(col >= 0 && col < ForumMessageCols.values().length) {
			switch(ForumMessageCols.values()[col]) {
				case type: return row.getStatusCode();
				case thread: return StringHelper.escapeHtml(row.getTitle());
				case lastModified: return row.getLastModified();
				default: return "ERROR";
			}
		}
		
		int propPos = col - AbstractMemberListController.USER_PROPS_OFFSET;
		if(StringHelper.containsNonWhitespace(row.getPseudonym())) {
			return propPos == 0 ? row.getPseudonym() : null;
		}
		if(row.isGuest()) {
			return propPos == 0 ? translator.translate("guest") : null;
		}
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<MessageLightView> createCopyWithEmptyList() {
		return new ForumMessageDataModel(getTableColumnModel(), translator);
	}
	
	public enum ForumMessageCols implements FlexiSortableColumnDef {
		type("table.header.typeimg"),
		thread("table.thread"),
		lastModified("table.lastModified");
		
		private final String i18nKey;
		
		private ForumMessageCols(String i18nKey) {
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
	
	public class ForumMessageDataModelSort extends SortableFlexiTableModelDelegate<MessageLightView> {
		
		public ForumMessageDataModelSort(SortKey orderBy, SortableFlexiTableDataModel<MessageLightView> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
	}
}